package com.dotfield.dotcal.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.dotfield.dotcal.data.DotCalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Owns the Google Play Billing connection and the app's Pro entitlement state.
 *
 * Never crashes the app: every billing call is wrapped, and [isPro] always falls back to the last
 * DataStore value when billing is unavailable (sideloaded APK, no Play Store, etc.). Raw exceptions
 * are never surfaced to the UI — they are mapped to friendly [PurchaseResult.Error] messages.
 *
 * Held as an Application-scoped singleton (same manual-DI pattern as DotCalRepository), initialized
 * from DotCalApplication.onCreate.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ProManager(
    context: Context,
    private val repository: DotCalRepository,
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _billingState = MutableStateFlow<BillingConnectionState>(BillingConnectionState.Disconnected)
    val billingState: StateFlow<BillingConnectionState> = _billingState.asStateFlow()

    private val cachedProductDetailsById = mutableMapOf<String, ProductDetails>()
    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()
    private val _purchaseOffers = MutableStateFlow<List<ProPurchaseOffer>>(emptyList())
    val purchaseOffers: StateFlow<List<ProPurchaseOffer>> = _purchaseOffers.asStateFlow()
    private val _hasActiveSubscription = MutableStateFlow(false)
    val hasActiveSubscription: StateFlow<Boolean> = _hasActiveSubscription.asStateFlow()

    private val purchaseResults = MutableStateFlow<PurchaseResult?>(null)

    sealed class BillingConnectionState {
        object Connecting : BillingConnectionState()
        object Connected : BillingConnectionState()
        object Disconnected : BillingConnectionState()
        data class Error(val message: String) : BillingConnectionState()
    }

    sealed class PurchaseResult {
        object Success : PurchaseResult()
        object Cancelled : PurchaseResult()
        data class Error(val message: String) : PurchaseResult()
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val proPurchases = purchases.orEmpty().filter { it.containsKnownProProduct() }
                if (proPurchases.isNotEmpty()) {
                    scope.launch { handleUpdatedPurchases(proPurchases, fromFlow = true) }
                } else {
                    purchaseResults.value = PurchaseResult.Error(GENERIC_ERROR)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                purchaseResults.value = PurchaseResult.Cancelled
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                scope.launch {
                    val restored = restorePurchases()
                    purchaseResults.value = if (restored) PurchaseResult.Success else PurchaseResult.Error(GENERIC_ERROR)
                }
            }
            else ->
                purchaseResults.value = PurchaseResult.Error(GENERIC_ERROR)
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
        )
        .enableAutoServiceReconnection()
        .build()

    /** Reads cached entitlement for an instant offline read, then connects to Play Billing. */
    fun initialize() {
        scope.launch {
            runCatching { _isPro.value = repository.readIsPro() }
            connectWithRetry()
        }
    }

    private suspend fun connectWithRetry() {
        var attempt = 0
        var backoffMs = 1000L
        while (attempt < MAX_CONNECT_ATTEMPTS) {
            attempt++
            _billingState.value = BillingConnectionState.Connecting
            val connected = runCatching { startConnectionOnce() }.getOrDefault(false)
            if (connected) {
                _billingState.value = BillingConnectionState.Connected
                refreshPurchases()
                return
            }
            _billingState.value = BillingConnectionState.Disconnected
            if (attempt < MAX_CONNECT_ATTEMPTS) {
                delay(backoffMs)
                backoffMs *= 2
            }
        }
        _billingState.value = BillingConnectionState.Error("Billing unavailable")
    }

    private suspend fun startConnectionOnce(): Boolean = suspendConnect()

    private suspend fun suspendConnect(): Boolean =
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            var resumed = false
            try {
                billingClient.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        if (resumed) return
                        resumed = true
                        cont.resume(result.responseCode == BillingClient.BillingResponseCode.OK) {}
                    }

                    override fun onBillingServiceDisconnected() {
                        if (resumed) return
                        resumed = true
                        cont.resume(false) {}
                    }
                })
            } catch (t: Throwable) {
                if (!resumed) {
                    resumed = true
                    cont.resume(false) {}
                }
            }
        }

    /** Queries live purchases and syncs [_isPro] + DataStore, trusting the live query over cache. */
    private suspend fun refreshPurchases() {
        runCatching { queryAndCacheProductDetails() }
        val inAppPurchases = queryPurchases(BillingClient.ProductType.INAPP)
        val subscriptionPurchases = queryPurchases(BillingClient.ProductType.SUBS)
        syncEntitlementIfQueryAllows(inAppPurchases, subscriptionPurchases, fromFlow = false)
    }

    private suspend fun handleUpdatedPurchases(purchases: List<Purchase>, fromFlow: Boolean) {
        purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }.forEach { purchase ->
            acknowledgeIfNeeded(purchase)
        }
        refreshPurchases()
        if (fromFlow) {
            purchaseResults.value = if (_isPro.value) PurchaseResult.Success else PurchaseResult.Error(GENERIC_ERROR)
        }
    }

    private suspend fun acknowledgeIfNeeded(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED || purchase.isAcknowledged) return
        runCatching {
            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build(),
            )
        }
    }

    private suspend fun queryAndCacheProductDetails(): List<ProductDetails> {
        val details = queryProductDetails(
            productType = BillingClient.ProductType.INAPP,
            productIds = listOf(PRODUCT_ID_PRO),
        ) + queryProductDetails(
            productType = BillingClient.ProductType.SUBS,
            productIds = PRODUCT_IDS_PRO_SUBSCRIPTION,
        )
        if (details.isNotEmpty()) {
            cachedProductDetailsById.clear()
            details.forEach { cachedProductDetailsById[it.productId] = it }
            _productDetails.value = cachedProductDetailsById[PRODUCT_ID_PRO]
            _purchaseOffers.value = buildProPurchaseOffers(
                lifetimeDetails = cachedProductDetailsById[PRODUCT_ID_PRO],
                subscriptionDetails = PRODUCT_IDS_PRO_SUBSCRIPTION.mapNotNull(cachedProductDetailsById::get),
            )
        }
        return details
    }

    private suspend fun queryProductDetails(productType: String, productIds: List<String>): List<ProductDetails> {
        val params = runCatching {
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    productIds.map { productId ->
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(productType)
                            .build()
                    },
                )
                .build()
        }.getOrNull() ?: return emptyList()
        return runCatching { billingClient.queryProductDetails(params).productDetailsList.orEmpty() }
            .getOrDefault(emptyList())
    }

    /** Launches the Play purchase flow. Result is delivered through [purchaseResults]. */
    suspend fun launchPurchaseFlow(activity: Activity, selectedOfferKey: String? = null): PurchaseResult {
        if (_billingState.value != BillingConnectionState.Connected) {
            return PurchaseResult.Error("Billing not available. Please try again.")
        }
        if (cachedProductDetailsById.isEmpty()) queryAndCacheProductDetails()
        val selectedOffer = selectProPurchaseOffer(_purchaseOffers.value, selectedOfferKey)
        val details = selectedOffer?.let { cachedProductDetailsById[it.productId] }
            ?: cachedProductDetailsById[PRODUCT_ID_PRO]
        if (details == null || selectedOffer == null) {
            return PurchaseResult.Error("Product not found. Please update the app or try again later.")
        }
        val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
        selectedOffer?.offerToken?.takeIf { it.isNotBlank() }?.let(productParamsBuilder::setOfferToken)
        val productParams = productParamsBuilder.build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        val launch = runCatching { billingClient.launchBillingFlow(activity, flowParams) }.getOrNull()
        if (launch?.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            val restored = restorePurchases()
            if (restored) purchaseResults.value = PurchaseResult.Success
            return if (restored) PurchaseResult.Success else PurchaseResult.Error(GENERIC_ERROR)
        }
        if (launch == null || launch.responseCode != BillingClient.BillingResponseCode.OK) {
            return PurchaseResult.Error(GENERIC_ERROR)
        }
        // Actual outcome (Success / Cancelled / Error) arrives via the PurchasesUpdatedListener.
        return PurchaseResult.Success
    }

    /** Consume the next purchase-flow outcome emitted by the listener, or null if none yet. */
    fun consumePurchaseResult(): PurchaseResult? {
        val value = purchaseResults.value
        purchaseResults.value = null
        return value
    }

    val purchaseResultFlow: StateFlow<PurchaseResult?> = purchaseResults.asStateFlow()

    fun pushPurchaseResult(result: PurchaseResult) {
        purchaseResults.value = result
    }

    fun clearPurchaseResult() {
        purchaseResults.value = null
    }

    /** Re-queries purchases and re-syncs state. Returns true if a Pro purchase was found. */
    suspend fun restorePurchases(): Boolean {
        if (_billingState.value != BillingConnectionState.Connected) {
            runCatching { connectWithRetry() }
        }
        val inAppPurchases = queryPurchases(BillingClient.ProductType.INAPP)
        val subscriptionPurchases = queryPurchases(BillingClient.ProductType.SUBS)
        return syncEntitlementIfQueryAllows(inAppPurchases, subscriptionPurchases, fromFlow = false)?.isPro
            ?: _isPro.value
    }

    private suspend fun queryPurchases(productType: String): List<Purchase>? {
        val result = runCatching {
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(productType)
                    .build(),
            )
        }.getOrNull()
        return result?.purchasesList
    }

    private suspend fun syncEntitlement(
        inAppPurchases: List<Purchase>,
        subscriptionPurchases: List<Purchase>,
        fromFlow: Boolean,
    ): ProEntitlement {
        val lifetime = inAppPurchases.entitlementFor(PRODUCT_ID_PRO)
        val subscription = subscriptionPurchases.entitlementForAny(PRODUCT_IDS_PRO_SUBSCRIPTION)
        (inAppPurchases + subscriptionPurchases)
            .filter { it.containsKnownProProduct() && it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .forEach { acknowledgeIfNeeded(it) }

        val entitlement = resolveProEntitlement(lifetime, subscription)
        val changed = _isPro.value != entitlement.isPro || _hasActiveSubscription.value != entitlement.hasActiveSubscription
        _isPro.value = entitlement.isPro
        _hasActiveSubscription.value = entitlement.hasActiveSubscription
        repository.setIsPro(entitlement.isPro)
        if (changed) runCatching { com.dotfield.dotcal.widget.WidgetUpdateWorker.enqueue(appContext) }
        if (fromFlow && entitlement.isPro) purchaseResults.value = PurchaseResult.Success
        return entitlement
    }

    private suspend fun syncEntitlementIfQueryAllows(
        inAppPurchases: List<Purchase>?,
        subscriptionPurchases: List<Purchase>?,
        fromFlow: Boolean,
    ): ProEntitlement? {
        val lifetime = inAppPurchases?.entitlementFor(PRODUCT_ID_PRO)
        val subscription = subscriptionPurchases?.entitlementForAny(PRODUCT_IDS_PRO_SUBSCRIPTION)
        if (!shouldSyncQueriedProEntitlement(lifetime, subscription)) return null
        return syncEntitlement(
            inAppPurchases = inAppPurchases.orEmpty(),
            subscriptionPurchases = subscriptionPurchases.orEmpty(),
            fromFlow = fromFlow,
        )
    }

    companion object {
        const val PRODUCT_ID_PRO = "dotcal_pro"
        const val PRODUCT_ID_PRO_SUBSCRIPTION = "dotcal_pro_subscription"
        val PRODUCT_IDS_PRO_SUBSCRIPTION = listOf(PRODUCT_ID_PRO_SUBSCRIPTION, "dotcal_pro_sub")
        private const val MAX_CONNECT_ATTEMPTS = 3
        private const val GENERIC_ERROR = "Something went wrong. Please try again."
    }
}

private fun List<Purchase>.entitlementFor(productId: String): PurchaseEntitlement {
    val relevant = filter { it.products.contains(productId) }
    return when {
        relevant.any { it.purchaseState == Purchase.PurchaseState.PURCHASED } -> PurchaseEntitlement.Purchased
        relevant.any { it.purchaseState == Purchase.PurchaseState.PENDING } -> PurchaseEntitlement.Pending
        else -> PurchaseEntitlement.None
    }
}

private fun List<Purchase>.entitlementForAny(productIds: List<String>): PurchaseEntitlement {
    val relevant = filter { purchase -> productIds.any { purchase.products.contains(it) } }
    return when {
        relevant.any { it.purchaseState == Purchase.PurchaseState.PURCHASED } -> PurchaseEntitlement.Purchased
        relevant.any { it.purchaseState == Purchase.PurchaseState.PENDING } -> PurchaseEntitlement.Pending
        else -> PurchaseEntitlement.None
    }
}

private fun Purchase.containsKnownProProduct(): Boolean {
    return products.contains(ProManager.PRODUCT_ID_PRO) ||
        ProManager.PRODUCT_IDS_PRO_SUBSCRIPTION.any(products::contains)
}

private fun buildProPurchaseOffers(
    lifetimeDetails: ProductDetails?,
    subscriptionDetails: List<ProductDetails>,
): List<ProPurchaseOffer> {
    val offers = buildList {
        subscriptionDetails.flatMap { it.subscriptionPurchaseOffers() }.let(::addAll)
        lifetimeDetails?.lifetimePurchaseOffers()?.let(::addAll)
    }
    return orderedProPurchaseOffers(offers)
}

private fun ProductDetails.lifetimePurchaseOffers(): List<ProPurchaseOffer> {
    val offers = oneTimePurchaseOfferDetailsList.orEmpty().ifEmpty {
        oneTimePurchaseOfferDetails?.let(::listOf).orEmpty()
    }
    val mapped = offers.map { offer ->
        ProPurchaseOffer(
            plan = ProPurchasePlan.Lifetime,
            formattedPrice = offer.formattedPrice,
            productId = productId,
            productType = BillingClient.ProductType.INAPP,
            priceAmountMicros = offer.priceAmountMicros,
            offerToken = offer.offerToken,
            offerId = offer.offerId,
            purchaseOptionId = offer.purchaseOptionId,
        )
    }
    return mapped.ifEmpty {
        oneTimePurchaseOfferDetails?.formattedPrice?.let { price ->
            listOf(
                ProPurchaseOffer(
                    plan = ProPurchasePlan.Lifetime,
                    formattedPrice = price,
                    productId = productId,
                    productType = BillingClient.ProductType.INAPP,
                    priceAmountMicros = oneTimePurchaseOfferDetails?.priceAmountMicros,
                ),
            )
        }.orEmpty()
    }
}

private fun ProductDetails.subscriptionPurchaseOffers(): List<ProPurchaseOffer> {
    val offers = subscriptionOfferDetails.orEmpty().mapNotNull { offer ->
        val plan = planForBasePlan(offer.basePlanId) ?: return@mapNotNull null
        val paidPhase = offer.pricingPhases.pricingPhaseList.lastOrNull { it.priceAmountMicros > 0 }
            ?: offer.pricingPhases.pricingPhaseList.lastOrNull()
            ?: return@mapNotNull null
        val trialPhase = offer.pricingPhases.pricingPhaseList.firstOrNull {
            it.priceAmountMicros == 0L && it.billingPeriod == "P7D"
        }
        val hasSevenDayTrial = trialPhase != null ||
            offer.offerId == YEARLY_TRIAL_OFFER_ID ||
            offer.offerTags.contains(YEARLY_TRIAL_OFFER_ID)
        ProPurchaseOffer(
            plan = plan,
            formattedPrice = paidPhase.formattedPrice,
            productId = productId,
            productType = BillingClient.ProductType.SUBS,
            priceAmountMicros = paidPhase.priceAmountMicros,
            offerToken = offer.offerToken,
            offerId = offer.offerId,
            basePlanId = offer.basePlanId,
            offerTags = offer.offerTags,
            hasFreeTrial = hasSevenDayTrial,
            trialBillingPeriod = trialPhase?.billingPeriod,
            billingPeriod = paidPhase.billingPeriod,
        )
    }
    return orderedProPurchaseOffers(offers)
}
