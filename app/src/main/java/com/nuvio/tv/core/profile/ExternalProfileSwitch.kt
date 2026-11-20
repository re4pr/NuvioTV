package com.nuvio.tv.core.profile

import android.content.Intent
import java.net.URI
import java.net.URLDecoder

data class ExternalProfileRequest(
    val profileId: Int,
    val sequence: Long
)

object ExternalProfileSwitch {
    const val EXTRA_PROFILE_ID = "profileId"
    private const val EXTRA_PROFILE_ID_SNAKE = "profile_id"

    fun extractProfileId(intent: Intent?): Int? {
        if (intent == null) return null

        val extras = intent.extras
        if (extras != null) {
            for (key in listOf(EXTRA_PROFILE_ID, EXTRA_PROFILE_ID_SNAKE)) {
                @Suppress("DEPRECATION")
                val parsed = parsePositiveProfileId(extras.get(key))
                if (parsed != null) return parsed
            }
        }

        return extractProfileIdFromUrl(intent.dataString)
    }

    fun extractProfileIdFromUrl(rawUrl: String?): Int? {
        val trimmed = rawUrl?.trim()?.takeIf(String::isNotEmpty) ?: return null
        val parsed = runCatching { URI(trimmed) }.getOrNull() ?: return null
        if (!parsed.scheme.equals("nuvio", ignoreCase = true)) return null

        if (parsed.host.equals("profile", ignoreCase = true)) {
            val pathProfileId = parsed.rawPath
                ?.split('/')
                .orEmpty()
                .firstNotNullOfOrNull { seg -> decode(seg).trim().takeIf(String::isNotEmpty) }
                ?.let(::parsePositiveProfileId)
            if (pathProfileId != null) return pathProfileId
        }

        val queryParams = parseQueryParameters(parsed.rawQuery)
        for (key in listOf("profileId", "profile_id", "profile")) {
            val value = queryParams[key] ?: continue
            val parsedId = parsePositiveProfileId(value)
            if (parsedId != null) return parsedId
        }
        return null
    }

    fun isProfileOnlyDeepLink(rawUrl: String?): Boolean {
        val trimmed = rawUrl?.trim()?.takeIf(String::isNotEmpty) ?: return false
        val parsed = runCatching { URI(trimmed) }.getOrNull() ?: return false
        return parsed.scheme.equals("nuvio", ignoreCase = true) &&
            parsed.host.equals("profile", ignoreCase = true)
    }

    private fun parsePositiveProfileId(raw: Any?): Int? {
        val num = when (raw) {
            is Int -> raw
            is Long -> raw.toInt()
            is String -> raw.trim().toIntOrNull()
            else -> null
        } ?: return null
        return num.takeIf { it in 1..ProfileManager.MAX_PROFILES }
    }

    private fun parseQueryParameters(rawQuery: String?): Map<String, String> {
        if (rawQuery.isNullOrBlank()) return emptyMap()
        val result = LinkedHashMap<String, String>()
        for (pair in rawQuery.split('&')) {
            val eqIdx = pair.indexOf('=')
            if (eqIdx <= 0) continue
            val key = decode(pair.substring(0, eqIdx)).trim()
            val value = decode(pair.substring(eqIdx + 1)).trim()
            if (key.isNotEmpty() && value.isNotEmpty()) {
                result[key] = value
            }
        }
        return result
    }

    private fun decode(value: String): String =
        runCatching { URLDecoder.decode(value, "UTF-8") }.getOrDefault(value)
}
