package com.dotfield.dotcal.data.billing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProPurchaseOfferTest {
    private val baseOffer = ProPurchaseOffer(
        formattedPrice = "$4.99",
        offerToken = "base-token",
        purchaseOptionId = "base",
    )
    private val discountOffer = ProPurchaseOffer(
        formattedPrice = "$1.99",
        offerToken = "discount-token",
        offerId = "launch-discount",
        purchaseOptionId = "discount",
    )

    @Test
    fun defaultSelectionUsesFirstEligibleOffer() {
        assertEquals(baseOffer, selectProPurchaseOffer(listOf(baseOffer, discountOffer), null))
    }

    @Test
    fun explicitSelectionUsesMatchingOfferToken() {
        assertEquals(discountOffer, selectProPurchaseOffer(listOf(baseOffer, discountOffer), "discount-token"))
    }

    @Test
    fun staleSelectionFallsBackToFirstEligibleOffer() {
        assertEquals(baseOffer, selectProPurchaseOffer(listOf(baseOffer, discountOffer), "expired-token"))
    }

    @Test
    fun emptyOffersHaveNoSelection() {
        assertNull(selectProPurchaseOffer(emptyList(), "discount-token"))
    }
}
