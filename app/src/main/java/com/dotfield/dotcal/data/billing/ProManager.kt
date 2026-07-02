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

    private var cachedProductDetails: ProductDetails? = null
    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

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
                val purchase = purchases?.firstOrNull { it.products.contains(PRODUCT_ID_PRO) }
                if (purchase != null) {
                    scope.launch { handlePurchased(purchase, fromFlow = true) }
                } else {
                    purchaseResults.value = PurchaseResult.Error(GENERIC_ERROR)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                purchaseResults.value = PurchaseResult.Cancelled
            else ->
                purchaseResults.value = PurchaseResult.Error(GENERIC_ERROR)
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
        )
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
        val result = runCatching {
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build(),
            )
        }.getOrNull() ?: return

        val proPurchase = result.purchasesList.firstOrNull {
            it.products.contains(PRODUCT_ID_PRO) &&
                it.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        if (proPurchase != null) {
            handlePurchased(proPurchase, fromFlow = false)
        } else {
            _isPro.value = false
            repository.setIsPro(false)
        }
    }

    private suspend fun handlePurchased(purchase: Purchase, fromFlow: Boolean) {
        if (!purchase.isAcknowledged) {
            runCatching {
                billingClient.acknowledgePurchase(
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build(),
                )
            }
        }
        _isPro.value = true
        repository.setIsPro(true)
        runCatching { com.dotfield.dotcal.widget.WidgetUpdateWorker.enqueue(appContext) }
        if (fromFlow) purchaseResults.value = PurchaseResult.Success
    }

    private suspend fun queryAndCacheProductDetails(): ProductDetails? {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID_PRO)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build()
        val result = runCatching { billingClient.queryProductDetails(params) }.getOrNull()
        val details = result?.productDetailsList?.firstOrNull { it.productId == PRODUCT_ID_PRO }
        if (details != null) {
            cachedProductDetails = details
            _productDetails.value = details
        }
        return details
    }

    /** Launches the Play purchase flow. Result is delivered through [purchaseResults]. */
    suspend fun launchPurchaseFlow(activity: Activity): PurchaseResult {
        if (_billingState.value != BillingConnectionState.Connected) {
            return PurchaseResult.Error("Billing not available. Please try again.")
        }
        val details = cachedProductDetails ?: queryAndCacheProductDetails()
        if (details == null) {
            return PurchaseResult.Error("Product not found. Please update the app or try again later.")
        }
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        val launch = runCatching { billingClient.launchBillingFlow(activity, flowParams) }.getOrNull()
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
        val result = runCatching {
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build(),
            )
        }.getOrNull() ?: return _isPro.value

        val proPurchase = result.purchasesList.firstOrNull {
            it.products.contains(PRODUCT_ID_PRO) &&
                it.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        return if (proPurchase != null) {
            handlePurchased(proPurchase, fromFlow = false)
            true
        } else {
            _isPro.value = false
            repository.setIsPro(false)
            false
        }
    }

    companion object {
        const val PRODUCT_ID_PRO = "dotcal_pro"
        private const val MAX_CONNECT_ATTEMPTS = 3
        private const val GENERIC_ERROR = "Something went wrong. Please try again."
    }
}
