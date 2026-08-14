package com.dotfield.dotcal.data.billing

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProEntitlementTest {
    @Test
    fun noLifetimeAndNoSubscriptionIsNotPro() {
        assertFalse(resolveProEntitlement(PurchaseEntitlement.None, PurchaseEntitlement.None).isPro)
    }

    @Test
    fun lifetimePurchaseIsPro() {
        assertTrue(resolveProEntitlement(PurchaseEntitlement.Purchased, PurchaseEntitlement.None).isPro)
    }

    @Test
    fun activeMonthlySubscriptionIsPro() {
        assertTrue(resolveProEntitlement(PurchaseEntitlement.None, PurchaseEntitlement.Purchased).isPro)
    }

    @Test
    fun activeYearlySubscriptionIsPro() {
        assertTrue(resolveProEntitlement(PurchaseEntitlement.None, PurchaseEntitlement.Purchased).isPro)
    }

    @Test
    fun activeYearlyTrialIsPro() {
        assertTrue(resolveProEntitlement(PurchaseEntitlement.None, PurchaseEntitlement.Purchased).isPro)
    }

    @Test
    fun lifetimeAndExpiredSubscriptionStaysPro() {
        assertTrue(resolveProEntitlement(PurchaseEntitlement.Purchased, PurchaseEntitlement.None).isPro)
    }

    @Test
    fun lifetimeAndActiveSubscriptionIsPro() {
        assertTrue(resolveProEntitlement(PurchaseEntitlement.Purchased, PurchaseEntitlement.Purchased).isPro)
    }

    @Test
    fun expiredSubscriptionWithoutLifetimeIsNotPro() {
        assertFalse(resolveProEntitlement(PurchaseEntitlement.None, PurchaseEntitlement.None).isPro)
    }

    @Test
    fun pendingSubscriptionIsNotPro() {
        assertFalse(resolveProEntitlement(PurchaseEntitlement.None, PurchaseEntitlement.Pending).isPro)
    }

    @Test
    fun canceledSubscriptionWithoutCurrentEntitlementIsNotPro() {
        assertFalse(resolveProEntitlement(PurchaseEntitlement.None, PurchaseEntitlement.None).isPro)
    }

    @Test
    fun restoreLifetimeIsPro() {
        assertTrue(resolveProEntitlement(PurchaseEntitlement.Purchased, PurchaseEntitlement.None).isPro)
    }

    @Test
    fun restoreMonthlyIsPro() {
        assertTrue(resolveProEntitlement(PurchaseEntitlement.None, PurchaseEntitlement.Purchased).isPro)
    }

    @Test
    fun restoreYearlyIsPro() {
        assertTrue(resolveProEntitlement(PurchaseEntitlement.None, PurchaseEntitlement.Purchased).isPro)
    }

    @Test
    fun promoCodeSubscriptionMatchesNormalSubscriptionEntitlement() {
        val normal = resolveProEntitlement(PurchaseEntitlement.None, PurchaseEntitlement.Purchased)
        val promo = resolveProEntitlement(PurchaseEntitlement.None, PurchaseEntitlement.Purchased)

        assertTrue(normal.isPro)
        assertTrue(promo.isPro)
    }

    @Test
    fun subscriptionExpirationCannotClearLifetimeEntitlement() {
        assertTrue(resolveProEntitlement(PurchaseEntitlement.Purchased, PurchaseEntitlement.None).isPro)
    }
}
