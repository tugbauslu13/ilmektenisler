package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.SavedRecipe
import com.example.ui.UiState
import com.example.ui.components.CraftBadge
import com.example.ui.components.CraftCard
import com.example.ui.components.CraftPillButton
import com.example.ui.components.CraftPostImage
import com.example.ui.components.RecipeDetailBottomSheet
import com.example.ui.components.RecipeSheetData

private enum class SavedFilter(val label: String) {
    ALL("Tümü"),
    FAVORITES("Favoriler"),
    AI_ONLY("AI Tarifleri")
}

@Composable
fun SavedRecipesScreen(
    uiState: UiState<List<SavedRecipe>>,
    recipeRowCounts: Map<String, Int> = emptyMap(),
    onIncrementRecipeRow: (String) -> Unit = {},
    onResetRecipeRow: (String) -> Unit = {},
    onToggleFavorite: (SavedRecipe) -> Unit,
    onDeleteRecipe: (String) -> Unit,
    onGoToAiTab: () -> Unit,
    onGoToDiscoverTab: () -> Unit,
    onGoToAtelierTab: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(SavedFilter.ALL) }
    var activeRecipe by remember { mutableStateOf<SavedRecipe?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SavedFilter.entries) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("saved_filter_${filter.name}")
                    )
                }
            }

            when (uiState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CraftCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Tarifler Yüklenemedi",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = uiState.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val filtered = remember(uiState.data, selectedFilter) {
                        when (selectedFilter) {
                            SavedFilter.ALL -> uiState.data
                            SavedFilter.FAVORITES -> uiState.data.filter { it.isFavorite }
                            SavedFilter.AI_ONLY -> uiState.data.filter { it.sourceType == "ai" }
                        }
                    }

                    if (filtered.isEmpty()) {
                        EmptySavedRecipesState(
                            hasAnySaved = uiState.data.isNotEmpty(),
                            onGoToAiTab = onGoToAiTab,
                            onGoToDiscoverTab = onGoToDiscoverTab
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filtered, key = { it.id }) { recipe ->
                                SavedRecipeRowCard(
                                    recipe = recipe,
                                    onClick = { activeRecipe = recipe },
                                    onFavoriteClick = { onToggleFavorite(recipe) },
                                    onDeleteClick = { onDeleteRecipe(recipe.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    activeRecipe?.let { recipe ->
        RecipeDetailBottomSheet(
            recipe = RecipeSheetData(
                id = recipe.id,
                title = recipe.title,
                authorOrSource = recipe.authorName.ifBlank {
                    if (recipe.sourceType == "ai") "İlmekten İşler AI" else "Topluluk Tarifi"
                },
                category = recipe.category,
                difficulty = recipe.difficulty,
                yarnType = recipe.yarnType,
                hookSize = recipe.hookSize,
                estimatedDuration = recipe.estimatedDuration,
                abbreviationsNote = recipe.abbreviationsNote,
                steps = recipe.steps,
                imageKey = recipe.imageKey,
                imageUrl = recipe.imageUrl
            ),
            onDismiss = { activeRecipe = null },
            onSaveClick = null,
            rowCount = recipeRowCounts[recipe.id] ?: 1,
            onIncrementRow = { onIncrementRecipeRow(recipe.id) },
            onResetRow = { onResetRecipeRow(recipe.id) },
            onGoToAtelier = onGoToAtelierTab
        )
    }
}

@Composable
private fun SavedRecipeRowCard(
    recipe: SavedRecipe,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    CraftCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_recipe_card_${recipe.id}"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CraftPostImage(
                imageUrl = recipe.imageUrl,
                imageKey = recipe.imageKey,
                contentDescription = recipe.title,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CraftBadge(
                        text = if (recipe.sourceType == "ai") "AI Tarif" else recipe.category,
                        isTerracotta = recipe.sourceType == "ai"
                    )
                    CraftBadge(
                        text = recipe.difficulty,
                        isTerracotta = false
                    )
                }

                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${recipe.hookSize} • ${recipe.steps.size} adım",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.testTag("favorite_recipe_${recipe.id}")
                ) {
                    Icon(
                        imageVector = if (recipe.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (recipe.isFavorite) "Favorilerden çıkar" else "Favorilere ekle",
                        tint = if (recipe.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.testTag("delete_recipe_${recipe.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Tarifi sil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySavedRecipesState(
    hasAnySaved: Boolean,
    onGoToAiTab: () -> Unit,
    onGoToDiscoverTab: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CraftCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Text(
                    text = if (hasAnySaved) "Bu filtrede tarif bulunamadı" else "Henüz Kaydedilmiş Tarifin Yok",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (hasAnySaved) {
                        "Diğer sekmeleri seçerek kayıtlı tüm örgü tariflerini görebilirsin."
                    } else {
                        "Keşfet akışındaki beğendiğin modelleri yer imine ekle veya AI Tarif Çıkar ile fotoğraftan kendi tarifini oluştur."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                if (!hasAnySaved) {
                    Spacer(modifier = Modifier.height(4.dp))
                    CraftPillButton(
                        text = "AI Tarif Çıkar",
                        onClick = onGoToAiTab,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("empty_go_ai_button"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    CraftPillButton(
                        text = "Keşfet'e Göz At",
                        onClick = onGoToDiscoverTab,
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("empty_go_discover_button")
                    )
                }
            }
        }
    }
}
