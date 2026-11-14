package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val IlmektenIslerLightColorScheme = lightColorScheme(
    primary = DeepSagePrimary,
    onPrimary = Color.White,
    primaryContainer = SoftSageContainer,
    onPrimaryContainer = EspressoText,
    secondary = YarnGreenSecondary,
    onSecondary = Color.White,
    secondaryContainer = SoftAlmondSurface,
    onSecondaryContainer = EspressoText,
    tertiary = TerracottaTertiary,
    onTertiary = Color.White,
    tertiaryContainer = SoftTerracottaContainer,
    onTertiaryContainer = EspressoText,
    background = WarmCreamBackground,
    onBackground = EspressoText,
    surface = SoftAlmondSurface,
    onSurface = EspressoText,
    surfaceVariant = WarmLinenSurfaceHigh,
    onSurfaceVariant = MutedEspressoText,
    surfaceContainerLowest = WarmCreamBackground,
    surfaceContainerLow = SoftAlmondSurface,
    surfaceContainer = SoftAlmondSurface,
    surfaceContainerHigh = WarmLinenSurfaceHigh,
    surfaceContainerHighest = WarmLinenSurfaceHigh,
    inverseSurface = EspressoText,
    inverseOnSurface = WarmCreamBackground,
    outline = CraftOutline,
    outlineVariant = CraftOutline,
    error = ErrorTerracotta,
    onError = Color.White,
    errorContainer = SoftTerracottaContainer,
    onErrorContainer = ErrorTerracotta
)

private val IlmektenIslerDarkColorScheme = darkColorScheme(
    primary = DarkSagePrimary,
    onPrimary = DarkOnSagePrimary,
    primaryContainer = DarkSageContainer,
    onPrimaryContainer = DarkOnSageContainer,
    secondary = DarkYarnSecondary,
    onSecondary = DarkOnYarnSecondary,
    secondaryContainer = DarkStudioSurfaceHigh,
    onSecondaryContainer = DarkCreamText,
    tertiary = DarkTerracottaTertiary,
    onTertiary = DarkOnTerracotta,
    tertiaryContainer = DarkTerracottaContainer,
    onTertiaryContainer = DarkOnTerracottaContainer,
    background = DarkStudioBackground,
    onBackground = DarkCreamText,
    surface = DarkStudioSurface,
    onSurface = DarkCreamText,
    surfaceVariant = DarkStudioSurfaceHigh,
    onSurfaceVariant = DarkMutedCreamText,
    surfaceContainerLowest = DarkStudioBackground,
    surfaceContainerLow = DarkStudioSurface,
    surfaceContainer = DarkStudioSurface,
    surfaceContainerHigh = DarkStudioSurfaceHigh,
    surfaceContainerHighest = DarkStudioSurfaceHigh,
    inverseSurface = DarkStudioSurfaceHigh,
    inverseOnSurface = DarkCreamText,
    outline = DarkCraftOutline,
    outlineVariant = DarkCraftOutline,
    error = DarkErrorCoral,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer
)

val IlmektenIslerShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun IlmektenIslerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        IlmektenIslerDarkColorScheme
    } else {
        IlmektenIslerLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = IlmektenIslerTypography,
        shapes = IlmektenIslerShapes,
        content = content
    )
}
