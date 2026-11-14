package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.model.UserProfile
import com.example.data.preferences.AppThemeMode
import com.example.ui.screens.SettingsScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DarkThemeUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `IlmektenIslerTheme applies warm espresso dark palette when darkTheme is true`() {
        var capturedScheme: ColorScheme? = null
        composeTestRule.setContent {
            IlmektenIslerTheme(darkTheme = true) {
                capturedScheme = MaterialTheme.colorScheme
            }
        }

        val scheme = requireNotNull(capturedScheme)
        assertEquals(DarkStudioBackground, scheme.background)
        assertEquals(DarkStudioSurface, scheme.surface)
        assertEquals(DarkSagePrimary, scheme.primary)
        assertEquals(DarkCreamText, scheme.onSurface)
        assertEquals(DarkTerracottaTertiary, scheme.tertiary)
    }

    @Test
    fun `IlmektenIslerTheme applies warm cream light palette when darkTheme is false`() {
        var capturedScheme: ColorScheme? = null
        composeTestRule.setContent {
            IlmektenIslerTheme(darkTheme = false) {
                capturedScheme = MaterialTheme.colorScheme
            }
        }

        val scheme = requireNotNull(capturedScheme)
        assertEquals(WarmCreamBackground, scheme.background)
        assertEquals(SoftAlmondSurface, scheme.surface)
        assertEquals(DeepSagePrimary, scheme.primary)
        assertEquals(EspressoText, scheme.onSurface)
        assertEquals(TerracottaTertiary, scheme.tertiary)
    }

    @Test
    fun `SettingsScreen renders theme options and triggers selection callback`() {
        var selectedMode: AppThemeMode? = null
        val sampleProfile = UserProfile(
            userId = "u1",
            displayName = "Tuba Örgü",
            username = "@tuba"
        )

        composeTestRule.setContent {
            IlmektenIslerTheme(darkTheme = true) {
                SettingsScreen(
                    profile = sampleProfile,
                    userEmail = "tuba@example.com",
                    isAdmin = false,
                    themeMode = AppThemeMode.DARK,
                    onSelectThemeMode = { selectedMode = it },
                    onBackClick = {},
                    onEditProfileClick = {},
                    onToggleNotifyLikes = {},
                    onToggleNotifyComments = {},
                    onExternalLinkError = {},
                    onSignOutClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Görünüm & Gece Modu").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_theme_system").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_theme_light").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_theme_dark").assertIsDisplayed()

        composeTestRule.onNodeWithTag("settings_theme_light").performClick()
        assertEquals(AppThemeMode.LIGHT, selectedMode)
    }
}
