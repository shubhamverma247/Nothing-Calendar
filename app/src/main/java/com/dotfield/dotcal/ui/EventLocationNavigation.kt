package com.dotfield.dotcal.ui

import android.content.Context
import android.content.Intent
import android.content.ActivityNotFoundException
import android.net.Uri
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal fun eventLocationNavigationUri(location: String): String? {
    val destination = location.trim().takeIf { it.isNotBlank() } ?: return null
    val encoded = URLEncoder.encode(destination, StandardCharsets.UTF_8.name()).replace("+", "%20")
    return "geo:0,0?q=$encoded"
}

internal fun eventLocationNavigationIntent(location: String): Intent? {
    val uri = eventLocationNavigationUri(location) ?: return null
    return Intent(Intent.ACTION_VIEW, Uri.parse(uri))
}

internal fun openEventLocationNavigation(context: Context, location: String): Boolean {
    val intent = eventLocationNavigationIntent(location) ?: return false
    return try {
        context.startActivity(Intent.createChooser(intent, null))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
