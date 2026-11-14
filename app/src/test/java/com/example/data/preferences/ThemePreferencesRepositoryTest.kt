package com.example.data.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ThemePreferencesRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `default theme mode is SYSTEM`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tempFolder.newFile("theme_default.preferences_pb") }
        )
        val repository = ThemePreferencesRepository(dataStore)

        assertEquals(AppThemeMode.SYSTEM, repository.themeModeFlow.first())
    }

    @Test
    fun `setThemeMode persists DARK LIGHT and SYSTEM values`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tempFolder.newFile("theme_persist.preferences_pb") }
        )
        val repository = ThemePreferencesRepository(dataStore)

        repository.setThemeMode(AppThemeMode.DARK)
        assertEquals(AppThemeMode.DARK, repository.themeModeFlow.first())

        repository.setThemeMode(AppThemeMode.LIGHT)
        assertEquals(AppThemeMode.LIGHT, repository.themeModeFlow.first())

        repository.setThemeMode(AppThemeMode.SYSTEM)
        assertEquals(AppThemeMode.SYSTEM, repository.themeModeFlow.first())
    }

    @Test
    fun `fromStorageValue falls back to SYSTEM for unknown values`() {
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromStorageValue(null))
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromStorageValue("unknown"))
        assertEquals(AppThemeMode.DARK, AppThemeMode.fromStorageValue("dark"))
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.fromStorageValue("light"))
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromStorageValue("system"))
    }
}
