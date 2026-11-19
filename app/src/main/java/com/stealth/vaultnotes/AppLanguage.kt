package com.stealth.vaultnotes

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import java.util.Locale

/**
 * Card Wallet is English-only. The app locale and layout direction are fixed
 * to English/LTR regardless of the device language or any legacy saved choice.
 */
object AppLanguage {
    const val ENGLISH = "en"

    fun getLanguage(context: Context): String = ENGLISH

    fun text(context: Context, english: String, arabic: String): String = english

    // Kept as a compatibility no-op for any remaining legacy call sites.
    fun setLanguage(context: Context, language: String) = Unit

    fun updateLauncherAlias(context: Context, language: String = ENGLISH) {
        val packageManager = context.packageManager
        val activityPackage = MainActivity::class.java.name.substringBeforeLast('.')
        val arabicAlias = ComponentName(context, "$activityPackage.MainActivityArabic")
        val englishAlias = ComponentName(context, "$activityPackage.MainActivityEnglish")
        val flags = PackageManager.DONT_KILL_APP or PackageManager.SYNCHRONOUS

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.setComponentEnabledSettings(
                    listOf(
                        PackageManager.ComponentEnabledSetting(
                            englishAlias,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            flags
                        ),
                        PackageManager.ComponentEnabledSetting(
                            arabicAlias,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            flags
                        )
                    )
                )
            } else {
                packageManager.setComponentEnabledSetting(
                    englishAlias,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    flags
                )
                packageManager.setComponentEnabledSetting(
                    arabicAlias,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    flags
                )
            }
        } catch (error: Exception) {
            Log.e("AppLanguage", "Could not enforce the English launcher entry", error)
        }
    }

    fun wrap(context: Context): Context {
        val locale = Locale.ENGLISH
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }
}
