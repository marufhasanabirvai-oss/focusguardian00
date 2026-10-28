package com.focusguardian.app.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {

    fun setAppLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)

        val config = context.resources.configuration
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    /**
     * Converts western digits to Bengali numerals: 0 -> ০, 1 -> ১, etc.
     */
    fun toBengaliDigits(number: Long): String {
        val bengaliDigits = arrayOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
        return number.toString().map { char ->
            if (char.isDigit()) bengaliDigits[char.digitToInt()] else char.toString()
        }.joinToString("")
    }
}
