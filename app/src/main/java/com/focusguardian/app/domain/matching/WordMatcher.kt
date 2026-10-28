package com.focusguardian.app.domain.matching

import java.util.Locale
import java.util.regex.Pattern

object WordMatcher {

    /**
     * Checks if screenText contains blockedWord.
     * - isExactMatch = true: Requires whole word boundary (e.g. "bet" matches "placed a bet" but not "better")
     * - isExactMatch = false: Matches anywhere as substring
     */
    fun matches(screenText: CharSequence?, blockedWord: String, isExactMatch: Boolean): Boolean {
        if (screenText.isNullOrEmpty() || blockedWord.isBlank()) return false

        val text = screenText.toString()
        val target = blockedWord.trim()

        if (isExactMatch) {
            // Regex word boundary pattern \b...\b with case insensitivity and Unicode support
            val escaped = Pattern.quote(target)
            val regex = Pattern.compile("(?i)(?U)\\b$escaped\\b")
            return regex.matcher(text).find()
        } else {
            return text.contains(target, ignoreCase = true)
        }
    }

    /**
     * Checks a collection of active words against candidate text.
     * Returns the first matched word or null.
     */
    fun findFirstMatch(
        screenText: CharSequence?,
        words: List<Pair<String, Boolean>> // Pair(word, isExactMatch)
    ): String? {
        if (screenText.isNullOrEmpty()) return null
        for ((word, isExact) in words) {
            if (matches(screenText, word, isExact)) {
                return word
            }
        }
        return null
    }
}
