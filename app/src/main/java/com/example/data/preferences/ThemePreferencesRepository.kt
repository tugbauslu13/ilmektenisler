package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class AppThemeMode(val storageValue: String, val labelTr: String, val descriptionTr: String) {
    SYSTEM(
        storageValue = "system",
        labelTr = "Sistem",
        descriptionTr = "Cihazın sistem temasını otomatik takip eder"
    ),
    LIGHT(
        storageValue = "light",
        labelTr = "Açık",
        descriptionTr = "Sıcak krem ve adaçayı atölye teması"
    ),
    DARK(
        storageValue = "dark",
        labelTr = "Gece Modu",
        descriptionTr = "Gece örgü örerken göz yormayan sıcak koyu tema"
    );

    companion object {
        fun fromStorageValue(raw: String?): AppThemeMode {
            return entries.firstOrNull { it.storageValue == raw } ?: SYSTEM
        }
    }
}

private val Context.ilmekThemeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ilmekten_isler_theme_prefs"
)

class ThemePreferencesRepository(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.applicationContext.ilmekThemeDataStore)

    private object Keys {
        val THEME_MODE = stringPreferencesKey("app_theme_mode")
    }

    val themeModeFlow: Flow<AppThemeMode> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            AppThemeMode.fromStorageValue(prefs[Keys.THEME_MODE])
        }

    suspend fun setThemeMode(mode: AppThemeMode) {
        dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.storageValue
        }
    }
}
