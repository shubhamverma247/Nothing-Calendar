package com.dotfield.dotcal.data.billing

enum class ProPurchasePlan {
    Yearly,
    Monthly,
    Lifetime,
}

data class ProPurchaseOffer(
    val plan: ProPurchasePlan,
    val formattedPrice: String,
    val productId: String,
    val productType: String,
    val priceAmountMicros: Long? = null,
    val comparisonFormattedPrice: String? = null,
    val offerToken: String? = null,
    val offerId: String? = null,
    val purchaseOptionId: String? = null,
    val basePlanId: String? = null,
    val offerTags: List<String> = emptyList(),
    val hasFreeTrial: Boolean = false,
    val trialBillingPeriod: String? = null,
    val billingPeriod: String? = null,
    val finePrint: String? = null,
)

val ProPurchaseOffer.selectionKey: String
    get() = listOfNotNull(
        plan.name,
        productId,
        basePlanId,
        offerId,
        purchaseOptionId,
        offerToken,
    ).joinToString("|")

val ProPurchaseOffer.isSevenDayTrial: Boolean
    get() = hasFreeTrial || offerId == YEARLY_TRIAL_OFFER_ID || offerTags.contains(YEARLY_TRIAL_OFFER_ID)

enum class PurchaseEntitlement {
    None,
    Pending,
    Purchased,
}

data class ProEntitlement(
    val isPro: Boolean,
    val hasLifetime: Boolean,
    val hasActiveSubscription: Boolean,
)

internal fun resolveProEntitlement(
    lifetime: PurchaseEntitlement,
    subscription: PurchaseEntitlement,
): ProEntitlement {
    val hasLifetime = lifetime == PurchaseEntitlement.Purchased
    val hasActiveSubscription = subscription == PurchaseEntitlement.Purchased
    return ProEntitlement(
        isPro = hasLifetime || hasActiveSubscription,
        hasLifetime = hasLifetime,
        hasActiveSubscription = hasActiveSubscription,
    )
}

internal fun shouldSyncQueriedProEntitlement(
    lifetime: PurchaseEntitlement?,
    subscription: PurchaseEntitlement?,
): Boolean {
    val hasCompleteQuery = lifetime != null && subscription != null
    val hasQueriedPurchase = lifetime == PurchaseEntitlement.Purchased ||
        subscription == PurchaseEntitlement.Purchased
    return hasCompleteQuery || hasQueriedPurchase
}

internal fun preferredProPurchaseOffer(offers: List<ProPurchaseOffer>): ProPurchaseOffer? {
    return offers.firstOrNull { it.plan == ProPurchasePlan.Yearly && it.hasFreeTrial }
        ?: offers.firstOrNull { it.plan == ProPurchasePlan.Yearly }
        ?: offers.firstOrNull()
}

internal fun selectProPurchaseOffer(
    offers: List<ProPurchaseOffer>,
    selectedOfferKey: String?,
): ProPurchaseOffer? {
    return offers.firstOrNull { offer ->
        offer.selectionKey == selectedOfferKey || offer.offerToken == selectedOfferKey
    } ?: preferredProPurchaseOffer(offers)
}

internal fun orderedProPurchaseOffers(offers: List<ProPurchaseOffer>): List<ProPurchaseOffer> {
    val yearlyOffers = offers.filter { it.plan == ProPurchasePlan.Yearly }
    val monthlyOffers = offers.filter { it.plan == ProPurchasePlan.Monthly }
    val lifetimeOffers = offers.filter { it.plan == ProPurchasePlan.Lifetime }
    val yearly = yearlyOffers
        .maxWithOrNull(
            compareBy<ProPurchaseOffer> { it.hasFreeTrial }
                .thenBy { it.offerId == YEARLY_TRIAL_OFFER_ID }
                .thenByDescending { it.priceAmountMicros ?: Long.MAX_VALUE },
        )
        ?.withComparisonPrice(yearlyOffers)
    val monthly = monthlyOffers
        .minWithOrNull(compareBy<ProPurchaseOffer> { it.priceAmountMicros ?: Long.MAX_VALUE })
        ?.withComparisonPrice(monthlyOffers)
    val lifetime = lifetimeOffers
        .minWithOrNull(compareBy<ProPurchaseOffer> { it.priceAmountMicros ?: Long.MAX_VALUE })
        ?.withComparisonPrice(lifetimeOffers)
    return listOfNotNull(yearly, monthly, lifetime)
}

private fun ProPurchaseOffer.withComparisonPrice(planOffers: List<ProPurchaseOffer>): ProPurchaseOffer {
    val selectedPrice = priceAmountMicros ?: return this
    val comparison = planOffers
        .filter { it.priceAmountMicros != null && it.priceAmountMicros > selectedPrice }
        .maxByOrNull { it.priceAmountMicros ?: Long.MIN_VALUE }
        ?.formattedPrice
        ?.takeUnless { it == formattedPrice }
    return copy(comparisonFormattedPrice = comparison)
}

internal const val MONTHLY_BASE_PLAN_ID = "monthly"
internal const val YEARLY_BASE_PLAN_ID = "yearly"
internal const val YEARLY_TRIAL_OFFER_ID = "7day-free"

internal fun planForBasePlan(basePlanId: String?): ProPurchasePlan? = when (basePlanId) {
    MONTHLY_BASE_PLAN_ID -> ProPurchasePlan.Monthly
    YEARLY_BASE_PLAN_ID -> ProPurchasePlan.Yearly
    else -> null
}
