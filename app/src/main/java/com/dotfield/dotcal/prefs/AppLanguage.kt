package com.dotfield.dotcal.prefs

/**
 * App language choice. [tag] is a BCP-47 language tag applied as a per-app locale; the empty tag
 * means "follow the device language". Deliberately Android-free so it stays unit-testable.
 *
 * Translating the rest of the UI is a follow-up — the Compose screens are still overwhelmingly
 * hardcoded English (see handoff). This wires the selection plus the per-app locale plumbing so
 * localized resources apply the moment those strings are extracted.
 */
internal enum class AppLanguage(val tag: String, val label: String, val native: String) {
    System("", "System default", "System default"),
    English("en", "English", "English"),
    Spanish("es", "Spanish", "Español"),
    Portuguese("pt", "Portuguese", "Português"),
    Indonesian("id", "Indonesian", "Bahasa Indonesia"),
    German("de", "German", "Deutsch"),
    French("fr", "French", "Français"),
    Russian("ru", "Russian", "Русский"),
    Turkish("tr", "Turkish", "Türkçe"),
    Arabic("ar", "Arabic", "العربية");

    companion object {
        /**
         * Resolves a stored or OS-reported tag. Region and script subtags are dropped, so a device
         * reporting `pt-BR` or `zh-Hans-CN` still maps onto the language we ship. Unknown, blank,
         * and null tags fall back to [System].
         */
        fun fromTag(tag: String?): AppLanguage {
            val language = tag?.trim().orEmpty()
                .substringBefore('-')
                .substringBefore('_')
                .lowercase()
            if (language.isEmpty()) return System
            return entries.firstOrNull { it.tag.isNotEmpty() && it.tag == language } ?: System
        }
    }
}
