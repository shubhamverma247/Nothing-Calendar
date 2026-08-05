package com.dotfield.dotcal.data.billing

data class ProPurchaseOffer(
    val formattedPrice: String,
    val offerToken: String,
    val offerId: String? = null,
    val purchaseOptionId: String? = null,
)

internal fun selectProPurchaseOffer(
    offers: List<ProPurchaseOffer>,
    selectedOfferToken: String?,
): ProPurchaseOffer? {
    return offers.firstOrNull { it.offerToken == selectedOfferToken } ?: offers.firstOrNull()
}
