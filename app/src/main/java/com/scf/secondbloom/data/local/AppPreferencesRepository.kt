package com.scf.secondbloom.data.local

import android.content.Context
import com.scf.secondbloom.domain.model.AppLanguage

interface AppPreferencesRepository {
    fun getAppLanguage(): AppLanguage

    fun setAppLanguage(language: AppLanguage)
}

class SharedPrefsAppPreferencesRepository(
    context: Context
) : AppPreferencesRepository {
    private val sharedPreferences = context.getSharedPreferences(
        "second_bloom_preferences",
        Context.MODE_PRIVATE
    )

    override fun getAppLanguage(): AppLanguage =
        AppLanguage.fromWire(sharedPreferences.getString(KEY_APP_LANGUAGE, AppLanguage.ENGLISH.wireValue))

    override fun setAppLanguage(language: AppLanguage) {
        sharedPreferences.edit()
            .putString(KEY_APP_LANGUAGE, language.wireValue)
            .apply()
    }

    private companion object {
        const val KEY_APP_LANGUAGE = "app_language"
    }
}

object AppPreferencesRepositoryFactory {
    fun create(context: Context): AppPreferencesRepository =
        SharedPrefsAppPreferencesRepository(context.applicationContext)
}

