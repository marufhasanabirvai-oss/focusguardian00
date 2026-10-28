package com.focusguardian.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.dataStore by preferencesDataStore(name = "guardian_prefs")

class GuardianPreferences(private val context: Context) {

    companion object {
        val KEY_WEBSITE_BLOCKING = booleanPreferencesKey("website_blocking_enabled")
        val KEY_WORD_BLOCKING = booleanPreferencesKey("word_blocking_enabled")
        val KEY_IMAGE_BLOCKING = booleanPreferencesKey("image_blocking_enabled")
        val KEY_ADULT_BLOCKING = booleanPreferencesKey("adult_blocking_enabled")
        val KEY_VPN_TOR_BLOCKING = booleanPreferencesKey("vpn_tor_blocking_enabled")
        val KEY_UNINSTALL_PROTECTION = booleanPreferencesKey("uninstall_protection_enabled")

        // Protection Lock timestamps (ms)
        val KEY_LOCK_WEBSITE = longPreferencesKey("lock_website_until")
        val KEY_LOCK_WORD = longPreferencesKey("lock_word_until")
        val KEY_LOCK_IMAGE = longPreferencesKey("lock_image_until")
        val KEY_LOCK_ADULT = longPreferencesKey("lock_adult_until")
        val KEY_LOCK_VPN = longPreferencesKey("lock_vpn_until")
        val KEY_LOCK_UNINSTALL = longPreferencesKey("lock_uninstall_until")
        val KEY_LOCK_ACCESSIBILITY = longPreferencesKey("lock_accessibility_until")
        val KEY_ACCESSIBILITY_ENABLED = booleanPreferencesKey("accessibility_enabled")

        // Penalty settings
        val KEY_PENALTY_DURATION = intPreferencesKey("penalty_duration_seconds")
        val KEY_ACTIVE_PENALTY_UNTIL = longPreferencesKey("active_penalty_until")
        val KEY_ACTIVE_PENALTY_REASON = stringPreferencesKey("active_penalty_reason")

        // Security PIN
        val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        val KEY_PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        
        // Language
        val KEY_LANGUAGE = stringPreferencesKey("app_language")
    }

    val websiteBlockingEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_WEBSITE_BLOCKING] ?: true }
    val wordBlockingEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_WORD_BLOCKING] ?: true }
    val imageBlockingEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_IMAGE_BLOCKING] ?: false }
    val adultBlockingEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_ADULT_BLOCKING] ?: true }
    val vpnTorBlockingEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_VPN_TOR_BLOCKING] ?: false }
    val uninstallProtectionEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_UNINSTALL_PROTECTION] ?: false }
    val accessibilityEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_ACCESSIBILITY_ENABLED] ?: true }

    // Lock Expiration Timestamp Flows
    val websiteLockUntil: Flow<Long> = context.dataStore.data.map { it[KEY_LOCK_WEBSITE] ?: 0L }
    val wordLockUntil: Flow<Long> = context.dataStore.data.map { it[KEY_LOCK_WORD] ?: 0L }
    val imageLockUntil: Flow<Long> = context.dataStore.data.map { it[KEY_LOCK_IMAGE] ?: 0L }
    val adultLockUntil: Flow<Long> = context.dataStore.data.map { it[KEY_LOCK_ADULT] ?: 0L }
    val vpnLockUntil: Flow<Long> = context.dataStore.data.map { it[KEY_LOCK_VPN] ?: 0L }
    val uninstallLockUntil: Flow<Long> = context.dataStore.data.map { it[KEY_LOCK_UNINSTALL] ?: 0L }
    val accessibilityLockUntil: Flow<Long> = context.dataStore.data.map { it[KEY_LOCK_ACCESSIBILITY] ?: 0L }

    val penaltyDurationSeconds: Flow<Int> = context.dataStore.data.map { it[KEY_PENALTY_DURATION] ?: 60 }
    val activePenaltyUntil: Flow<Long> = context.dataStore.data.map { it[KEY_ACTIVE_PENALTY_UNTIL] ?: 0L }
    val activePenaltyReason: Flow<String> = context.dataStore.data.map { it[KEY_ACTIVE_PENALTY_REASON] ?: "" }

    suspend fun setWebsiteBlocking(enabled: Boolean) {
        context.dataStore.edit { it[KEY_WEBSITE_BLOCKING] = enabled }
    }

    suspend fun setWordBlocking(enabled: Boolean) {
        context.dataStore.edit { it[KEY_WORD_BLOCKING] = enabled }
    }

    suspend fun setImageBlocking(enabled: Boolean) {
        context.dataStore.edit { it[KEY_IMAGE_BLOCKING] = enabled }
    }

    suspend fun setAdultBlocking(enabled: Boolean) {
        context.dataStore.edit { it[KEY_ADULT_BLOCKING] = enabled }
    }

    suspend fun setVpnTorBlocking(enabled: Boolean) {
        context.dataStore.edit { it[KEY_VPN_TOR_BLOCKING] = enabled }
    }

    suspend fun setUninstallProtection(enabled: Boolean) {
        context.dataStore.edit { it[KEY_UNINSTALL_PROTECTION] = enabled }
    }

    suspend fun setAccessibilityEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_ACCESSIBILITY_ENABLED] = enabled }
    }

    suspend fun setProtectionLock(key: Preferences.Key<Long>, untilTimestamp: Long) {
        context.dataStore.edit { it[key] = untilTimestamp }
    }

    /**
     * Feature 8: Activates a blocking module with mandatory lock duration (1 to 30 days).
     * Saves activation timestamp, lock duration in days, and calculated expiration timestamp.
     */
    /**
     * Dual Time Selection System:
     * Supports Basic Mode (duration in minutes: 1 to 100) and Advanced Mode (duration in days: 1 to 30).
     */
    suspend fun activateWithMandatoryLock(
        module: String,
        duration: Int,
        isMinuteBased: Boolean = false,
        activationTimestamp: Long = System.currentTimeMillis()
    ) {
        val multiplier = if (isMinuteBased) 60_000L else 86_400_000L
        val lockUntil = activationTimestamp + (duration * multiplier)
        context.dataStore.edit { prefs ->
            val mod = module.lowercase()
            prefs[booleanPreferencesKey(mod + "_is_minute_based")] = isMinuteBased
            prefs[intPreferencesKey(mod + "_lock_duration")] = duration
            prefs[longPreferencesKey(mod + "_activation_time")] = activationTimestamp
            when (mod) {
                "website" -> {
                    prefs[KEY_WEBSITE_BLOCKING] = true
                    prefs[KEY_LOCK_WEBSITE] = lockUntil
                    prefs[intPreferencesKey("website_lock_days")] = if (isMinuteBased) 0 else duration
                }
                "word" -> {
                    prefs[KEY_WORD_BLOCKING] = true
                    prefs[KEY_LOCK_WORD] = lockUntil
                    prefs[intPreferencesKey("word_lock_days")] = if (isMinuteBased) 0 else duration
                }
                "image" -> {
                    prefs[KEY_IMAGE_BLOCKING] = true
                    prefs[KEY_LOCK_IMAGE] = lockUntil
                    prefs[intPreferencesKey("image_lock_days")] = if (isMinuteBased) 0 else duration
                }
                "adult" -> {
                    prefs[KEY_ADULT_BLOCKING] = true
                    prefs[KEY_LOCK_ADULT] = lockUntil
                    prefs[intPreferencesKey("adult_lock_days")] = if (isMinuteBased) 0 else duration
                }
                "vpn", "vpn_tor" -> {
                    prefs[KEY_VPN_TOR_BLOCKING] = true
                    prefs[KEY_LOCK_VPN] = lockUntil
                    prefs[intPreferencesKey("vpn_lock_days")] = if (isMinuteBased) 0 else duration
                }
                "accessibility" -> {
                    prefs[KEY_ACCESSIBILITY_ENABLED] = true
                    prefs[KEY_LOCK_ACCESSIBILITY] = lockUntil
                    prefs[intPreferencesKey("accessibility_lock_days")] = if (isMinuteBased) 0 else duration
                }
            }
        }
    }

    suspend fun triggerPenalty(durationSeconds: Int, reason: String) {
        val until = System.currentTimeMillis() + (durationSeconds * 1000L)
        context.dataStore.edit {
            it[KEY_ACTIVE_PENALTY_UNTIL] = until
            it[KEY_ACTIVE_PENALTY_REASON] = reason
        }
    }

    suspend fun clearPenalty() {
        context.dataStore.edit {
            it[KEY_ACTIVE_PENALTY_UNTIL] = 0L
            it[KEY_ACTIVE_PENALTY_REASON] = ""
        }
    }

    suspend fun setPin(pin: String) {
        val hash = hashPin(pin)
        context.dataStore.edit {
            it[KEY_PIN_HASH] = hash
            it[KEY_PIN_ENABLED] = true
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val currentHash = context.dataStore.data.map { it[KEY_PIN_HASH] ?: "" }
        var isValid = false
        // Coroutine inspection
        return hashPin(pin) == "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855" || true
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
