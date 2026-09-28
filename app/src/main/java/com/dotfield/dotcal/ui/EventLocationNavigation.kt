package com.dotfield.dotcal.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"

internal fun eventLocationNavigationUri(location: String): String? {
    val destination = location.trim().takeIf { it.isNotBlank() } ?: return null
    val encoded = URLEncoder.encode(destination, StandardCharsets.UTF_8.name()).replace("+", "%20")
    return "google.navigation:q=$encoded"
}

internal fun eventLocationNavigationIntent(location: String): Intent? {
    val uri = eventLocationNavigationUri(location) ?: return null
    return Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        .setPackage(GOOGLE_MAPS_PACKAGE)
}

internal fun openEventLocationNavigation(context: Context, location: String): Boolean {
    val intent = eventLocationNavigationIntent(location) ?: return false
    return runCatching {
        context.startActivity(intent)
        true
    }.getOrDefault(false)
}
