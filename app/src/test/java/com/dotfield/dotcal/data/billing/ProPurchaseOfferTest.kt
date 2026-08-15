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
        priceAmountMicros = 1_990_000,
        offerToken = "monthly-token",
        basePlanId = "monthly",
    )
    private val discountedMonthlyOffer = ProPurchaseOffer(
        plan = ProPurchasePlan.Monthly,
        formattedPrice = "$0.99",
        productId = "dotcal_pro_subscription",
        productType = "subs",
        priceAmountMicros = 990_000,
        offerToken = "monthly-discount-token",
        basePlanId = "monthly",
        offerId = "monthly-sale",
    )
    private val yearlyOffer = ProPurchaseOffer(
        plan = ProPurchasePlan.Yearly,
        formattedPrice = "$14.99",
        productId = "dotcal_pro_subscription",
        productType = "subs",
        priceAmountMicros = 14_990_000,
        offerToken = "yearly-token",
        basePlanId = "yearly",
    )
    private val discountedYearlyOffer = ProPurchaseOffer(
        plan = ProPurchasePlan.Yearly,
        formattedPrice = "$9.99",
        productId = "dotcal_pro_subscription",
        productType = "subs",
        priceAmountMicros = 9_990_000,
        offerToken = "yearly-discount-token",
        basePlanId = "yearly",
        offerId = "yearly-sale",
    )
    private val yearlyTrialOffer = ProPurchaseOffer(
        plan = ProPurchasePlan.Yearly,
        formattedPrice = "$14.99",
        productId = "dotcal_pro_subscription",
        productType = "subs",
        priceAmountMicros = 14_990_000,
        offerToken = "yearly-trial-token",
        basePlanId = "yearly",
        offerId = "7day-free",
        hasFreeTrial = true,
    )
    private val discountedLifetimeOffer = ProPurchaseOffer(
        plan = ProPurchasePlan.Lifetime,
        formattedPrice = "$2.99",
        productId = "dotcal_pro",
        productType = "inapp",
        priceAmountMicros = 2_990_000,
        offerToken = "discount-token",
        purchaseOptionId = "discount",
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

    @Test
    fun orderedOffersPreferDiscountedLifetimePurchaseOption() {
        val ordered = orderedProPurchaseOffers(
            listOf(
                lifetimeOffer.copy(priceAmountMicros = 4_990_000),
                discountedLifetimeOffer,
            ),
        )

        assertEquals(listOf(discountedLifetimeOffer.copy(comparisonFormattedPrice = "$4.99")), ordered)
    }

    @Test
    fun orderedOffersPreferDiscountedMonthlyPurchaseOption() {
        val ordered = orderedProPurchaseOffers(listOf(monthlyOffer, discountedMonthlyOffer))

        assertEquals(listOf(discountedMonthlyOffer.copy(comparisonFormattedPrice = "$1.99")), ordered)
    }

    @Test
    fun orderedOffersPreferDiscountedYearlyWhenNoTrialExists() {
        val ordered = orderedProPurchaseOffers(listOf(yearlyOffer, discountedYearlyOffer))

        assertEquals(listOf(discountedYearlyOffer.copy(comparisonFormattedPrice = "$14.99")), ordered)
    }

    @Test
    fun orderedOffersKeepYearlyTrialAheadOfDiscountedYearly() {
        val ordered = orderedProPurchaseOffers(listOf(discountedYearlyOffer, yearlyTrialOffer))

        assertEquals(listOf(yearlyTrialOffer), ordered)
    }
}
