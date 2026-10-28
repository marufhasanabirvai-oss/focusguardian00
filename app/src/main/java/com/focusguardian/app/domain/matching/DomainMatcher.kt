package com.focusguardian.app.domain.matching

import java.util.Calendar
import java.util.Locale

object DomainMatcher {

    /**
     * Clean and normalize a host string:
     * - lowercases
     * - removes http:// and https://
     * - removes leading and trailing slashes / path
     * - strips port (e.g. example.com:8080 -> example.com)
     */
    fun normalizeDomain(rawInput: String): String {
        var host = rawInput.trim().lowercase(Locale.ROOT)
        if (host.startsWith("http://")) host = host.removePrefix("http://")
        if (host.startsWith("https://")) host = host.removePrefix("https://")
        val slashIndex = host.indexOf('/')
        if (slashIndex != -1) host = host.substring(0, slashIndex)
        val colonIndex = host.indexOf(':')
        if (colonIndex != -1) host = host.substring(0, colonIndex)
        return host
    }

    /**
     * Checks if requestedHost matches blockedDomain rule.
     * Supports exact matches and subdomain matching:
     * e.g., if blockedDomain is "example.com", then:
     * - "example.com" -> MATCH
     * - "www.example.com" -> MATCH
     * - "sub.example.com" -> MATCH
     * - "notexample.com" -> NO MATCH
     * - "example.org" -> NO MATCH
     */
    fun isHostBlocked(requestedHost: String, blockedDomain: String): Boolean {
        val req = normalizeDomain(requestedHost)
        val rule = normalizeDomain(blockedDomain)

        if (req.isEmpty() || rule.isEmpty()) return false

        // 1. Exact match
        if (req == rule) return true

        // 2. Subdomain check: req ends with ".rule"
        if (req.endsWith(".$rule")) return true

        // 3. www check if rule had www and request didn't, or vice-versa
        val reqNoWww = req.removePrefix("www.")
        val ruleNoWww = rule.removePrefix("www.")
        if (reqNoWww == ruleNoWww) return true
        if (reqNoWww.endsWith(".$ruleNoWww")) return true

        return false
    }

    /**
     * Checks if a scheduled rule is active at the given timestamp.
     * daysOfWeekMask: Bitmask 1..7 (Monday=1, Sunday=7)
     */
    fun isScheduleActive(
        currentTimeMillis: Long,
        daysOfWeekMask: Int,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ): Boolean {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
        // Calendar.DAY_OF_WEEK: Sunday=1, Monday=2 ... Saturday=7
        val dayOfWeekIndex = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }

        // Check if bit is set (e.g. 1 shl (dayOfWeekIndex - 1))
        val isDayActive = (daysOfWeekMask and (1 shl (dayOfWeekIndex - 1))) != 0
        if (!isDayActive) return false

        val currentMinuteOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val startMinuteOfDay = startHour * 60 + startMinute
        val endMinuteOfDay = endHour * 60 + endMinute

        return if (startMinuteOfDay <= endMinuteOfDay) {
            currentMinuteOfDay in startMinuteOfDay..endMinuteOfDay
        } else {
            // Wraps around midnight (e.g., 22:00 to 06:00)
            currentMinuteOfDay >= startMinuteOfDay || currentMinuteOfDay <= endMinuteOfDay
        }
    }
}
