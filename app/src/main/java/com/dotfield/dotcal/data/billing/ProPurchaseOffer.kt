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
    val yearly = offers
        .filter { it.plan == ProPurchasePlan.Yearly }
        .maxWithOrNull(compareBy<ProPurchaseOffer> { it.hasFreeTrial }.thenBy { it.offerId == YEARLY_TRIAL_OFFER_ID })
    val monthly = offers.firstOrNull { it.plan == ProPurchasePlan.Monthly }
    val lifetime = offers.firstOrNull { it.plan == ProPurchasePlan.Lifetime }
    return listOfNotNull(yearly, monthly, lifetime)
}

internal const val MONTHLY_BASE_PLAN_ID = "monthly"
internal const val YEARLY_BASE_PLAN_ID = "yearly"
internal const val YEARLY_TRIAL_OFFER_ID = "7day-free"

internal fun planForBasePlan(basePlanId: String?): ProPurchasePlan? = when (basePlanId) {
    MONTHLY_BASE_PLAN_ID -> ProPurchasePlan.Monthly
    YEARLY_BASE_PLAN_ID -> ProPurchasePlan.Yearly
    else -> null
}
