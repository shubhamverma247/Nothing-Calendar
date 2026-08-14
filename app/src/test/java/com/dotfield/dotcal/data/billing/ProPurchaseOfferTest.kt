package com.dotfield.dotcal.data.billing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProPurchaseOfferTest {
    private val lifetimeOffer = ProPurchaseOffer(
        plan = ProPurchasePlan.Lifetime,
        formattedPrice = "$4.99",
        productId = "dotcal_pro",
        productType = "inapp",
        offerToken = "base-token",
        purchaseOptionId = "base",
    )
    private val monthlyOffer = ProPurchaseOffer(
        plan = ProPurchasePlan.Monthly,
        formattedPrice = "$1.99",
        productId = "dotcal_pro_subscription",
        productType = "subs",
        offerToken = "monthly-token",
        basePlanId = "monthly",
    )
    private val yearlyTrialOffer = ProPurchaseOffer(
        plan = ProPurchasePlan.Yearly,
        formattedPrice = "$14.99",
        productId = "dotcal_pro_subscription",
        productType = "subs",
        offerToken = "yearly-trial-token",
        basePlanId = "yearly",
        offerId = "7day-free",
        hasFreeTrial = true,
    )

    @Test
    fun defaultSelectionPrefersYearlyTrial() {
        assertEquals(yearlyTrialOffer, selectProPurchaseOffer(listOf(lifetimeOffer, monthlyOffer, yearlyTrialOffer), null))
    }

    @Test
    fun explicitSelectionUsesMatchingSelectionKey() {
        assertEquals(monthlyOffer, selectProPurchaseOffer(listOf(lifetimeOffer, monthlyOffer), monthlyOffer.selectionKey))
    }

    @Test
    fun staleSelectionFallsBackToFirstEligibleOffer() {
        assertEquals(lifetimeOffer, selectProPurchaseOffer(listOf(lifetimeOffer, monthlyOffer), "expired-token"))
    }

    @Test
    fun emptyOffersHaveNoSelection() {
        assertNull(selectProPurchaseOffer(emptyList(), "discount-token"))
    }
}
