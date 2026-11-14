package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CuratedCatalog
import com.example.data.model.AtelierProduct
import com.example.ui.components.CraftBadge
import com.example.ui.components.CraftCard
import com.example.ui.components.CraftOutlinedPillButton
import com.example.ui.components.CraftPillButton
import com.example.ui.components.CraftPostImage
import com.example.ui.utils.RecipeShareFormatter

@Composable
fun AtelierScreen(
    products: List<AtelierProduct> = CuratedCatalog.atelierProducts,
    isAdmin: Boolean = false,
    onAddProductClick: () -> Unit = {},
    onDeleteProductClick: (AtelierProduct) -> Unit = {},
    onExternalLinkError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCustomOrderSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AtelierHeaderCard()
            }

            item(key = "custom_order_request_button_item") {
                CraftPillButton(
                    text = "Kişiye Özel Tasarım Siparişi Ver",
                    onClick = { showCustomOrderSheet = true },
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_order_request_button"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            if (isAdmin) {
                item(key = "admin_add_atelier_button_item") {
                    CraftPillButton(
                        text = "+ Yeni Atölye Ürünü Ekle",
                        onClick = onAddProductClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_add_atelier_product_button")
                    )
                }
            }

            items(products, key = { it.id }) { product ->
                AtelierProductCard(
                    product = product,
                    isAdmin = isAdmin,
                    onDeleteClick = { onDeleteProductClick(product) },
                    onShopierClick = {
                        openExternalShopUrl(context, product.shopierUrl, onExternalLinkError)
                    },
                    onDolapClick = {
                        openExternalShopUrl(context, product.dolapUrl, onExternalLinkError)
                    }
                )
            }

            item {
                AtelierCustomOrderContactCard(
                    onShopierClick = {
                        openExternalShopUrl(context, "https://www.shopier.com/ilmektenisler", onExternalLinkError)
                    },
                    onDolapClick = {
                        openExternalShopUrl(context, "https://dolap.com/profil/ilmektenisler", onExternalLinkError)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showCustomOrderSheet) {
        CustomAtelierOrderSheet(
            onDismiss = { showCustomOrderSheet = false },
            onSubmitOrder = { productType, colors, note, channel ->
                val message = RecipeShareFormatter.formatCustomOrderMessage(
                    productType = productType,
                    colors = colors,
                    note = note
                )
                RecipeShareFormatter.copyToClipboard(context, "Özel Sipariş Talebi", message)
                showCustomOrderSheet = false
                if (channel == CustomOrderChannel.WHATSAPP) {
                    val waUrl = "https://wa.me/?text=${Uri.encode(message)}"
                    Toast.makeText(
                        context,
                        "Sipariş mesajınız hazırlandı ve WhatsApp açılıyor.",
                        Toast.LENGTH_SHORT
                    ).show()
                    openExternalShopUrl(context, waUrl, onExternalLinkError)
                } else {
                    Toast.makeText(
                        context,
                        "Sipariş mesajınız panoya kopyalandı, @ilmektenisler DM'e yapıştırabilirsiniz.",
                        Toast.LENGTH_LONG
                    ).show()
                    openExternalShopUrl(context, "https://ig.me/m/ilmektenisler", onExternalLinkError)
                }
            }
        )
    }
}

@Composable
private fun AtelierHeaderCard() {
    CraftCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CraftBadge(text = "@ilmektenisler", isTerracotta = true)
                CraftBadge(text = "El Emeği Vitrin", isTerracotta = false)
            }

            Text(
                text = "@ilmektenisler Atölye",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Her biri tek tek elde örülmüş, doğal pamuk ve merinos yününden üretilen hazır koleksiyon parçalarını Shopier ve Dolap mağazalarımızdan güvenle sipariş verebilirsiniz.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AtelierProductCard(
    product: AtelierProduct,
    isAdmin: Boolean = false,
    onDeleteClick: () -> Unit = {},
    onShopierClick: () -> Unit,
    onDolapClick: () -> Unit
) {
    CraftCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("atelier_product_${product.id}")
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (product.imageUrl.isNotBlank() || product.imageKey.isNotBlank()) {
                CraftPostImage(
                    imageUrl = product.imageUrl,
                    imageKey = product.imageKey,
                    contentDescription = product.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = product.imageRes),
                    contentDescription = product.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                CraftBadge(text = product.tag, isTerracotta = true)
            }

            if (isAdmin) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onDeleteClick)
                        .testTag("admin_delete_atelier_${product.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Ürünü sil",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Sil",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                Text(
                    text = product.priceFormatted,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onTertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = product.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Materyal",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = product.yarnDetail,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ölçü",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = product.dimensions,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CraftPillButton(
                    text = "Shopier'de İncele",
                    onClick = onShopierClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shopier_order_${product.id}"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                CraftOutlinedPillButton(
                    text = "Dolap'ta İncele",
                    onClick = onDolapClick,
                    borderColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dolap_order_${product.id}"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun AtelierCustomOrderContactCard(
    onShopierClick: () -> Unit,
    onDolapClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("atelier_custom_order_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Özel Sipariş & İletişim",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = "Beğendiğin bir tarifi örmek zor mu geldi veya istediğin renklerde kişiye özel tasarım mı arıyorsun? @ilmektenisler atölyesi olarak senin için özenle hazırlayalım.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.88f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CraftPillButton(
                    text = "Shopier Mağaza",
                    onClick = onShopierClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("atelier_contact_shopier"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                CraftOutlinedPillButton(
                    text = "Dolap Profili",
                    onClick = onDolapClick,
                    borderColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("atelier_contact_dolap"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

private enum class CustomOrderChannel {
    WHATSAPP,
    INSTAGRAM
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CustomAtelierOrderSheet(
    onDismiss: () -> Unit,
    onSubmitOrder: (productType: String, colors: String, note: String, channel: CustomOrderChannel) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val productTypes = remember { listOf("Amigurumi", "Hırka", "Çanta", "Anahtarlık") }
    var selectedType by rememberSaveable { mutableStateOf(productTypes.first()) }
    var colorsDraft by rememberSaveable { mutableStateOf("") }
    var noteDraft by rememberSaveable { mutableStateOf("") }
    var selectedChannel by rememberSaveable { mutableStateOf(CustomOrderChannel.WHATSAPP) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CraftBadge(text = "@ilmektenisler", isTerracotta = true)
                    Text(
                        text = "Kişiye Özel Tasarım Siparişi",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_custom_order_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Sipariş formunu kapat",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                text = "İstediğin ürün türünü ve renkleri belirle; hazır sipariş mesajınla doğrudan atölye iletişim kanalına yönlendirelim.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Ürün Türü",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                productTypes.forEach { type ->
                    val selected = selectedType == type
                    FilterChip(
                        selected = selected,
                        onClick = { selectedType = type },
                        label = {
                            Text(
                                text = type,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("custom_order_type_$type")
                    )
                }
            }

            OutlinedTextField(
                value = colorsDraft,
                onValueChange = { colorsDraft = it },
                label = { Text("İstediğiniz Renkler") },
                placeholder = { Text("Örn: Adaçayı yeşili, krem ve tarçın tonları") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_order_colors_input")
            )

            OutlinedTextField(
                value = noteDraft,
                onValueChange = { noteDraft = it },
                label = { Text("Kısa Notunuz / Ölçü Talebi") },
                placeholder = { Text("Örn: 38 beden, %100 organik pamuklu ip ile olsun...") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_order_note_input")
            )

            Text(
                text = "İletişim Kanalı",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = selectedChannel == CustomOrderChannel.WHATSAPP,
                    onClick = { selectedChannel = CustomOrderChannel.WHATSAPP },
                    label = {
                        Text(
                            text = "WhatsApp",
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        1.dp,
                        if (selectedChannel == CustomOrderChannel.WHATSAPP) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("custom_order_channel_whatsapp")
                )

                FilterChip(
                    selected = selectedChannel == CustomOrderChannel.INSTAGRAM,
                    onClick = { selectedChannel = CustomOrderChannel.INSTAGRAM },
                    label = {
                        Text(
                            text = "Instagram (@ilmektenisler)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        1.dp,
                        if (selectedChannel == CustomOrderChannel.INSTAGRAM) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                        selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    modifier = Modifier
                        .weight(1.35f)
                        .heightIn(min = 48.dp)
                        .testTag("custom_order_channel_instagram")
                )
            }

            CraftPillButton(
                text = "Talep Oluştur",
                onClick = {
                    onSubmitOrder(selectedType, colorsDraft, noteDraft, selectedChannel)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_custom_order_button"),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

private fun openExternalShopUrl(
    context: Context,
    url: String,
    onError: (String) -> Unit
) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        onError("İletişim bağlantısı açılamadı. Tarayıcı veya uygulamayı kontrol edin.")
    }
}
