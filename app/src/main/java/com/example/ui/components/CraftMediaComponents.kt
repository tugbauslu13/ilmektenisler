package com.example.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.resolveCraftDrawable

@Composable
fun CraftPostImage(
    imageUrl: String,
    imageKey: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val fallbackRes = remember(imageKey) { resolveCraftDrawable(imageKey) }

    if (imageUrl.startsWith("data:image")) {
        val decodedBitmap = remember(imageUrl) {
            try {
                val base64Part = imageUrl.substringAfter("base64,", "")
                if (base64Part.isNotBlank()) {
                    val bytes = Base64.decode(base64Part, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }
        if (decodedBitmap != null) {
            Image(
                bitmap = decodedBitmap,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
            return
        }
    }

    if (imageUrl.isNotBlank() && !imageUrl.startsWith("data:image")) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            placeholder = painterResource(id = fallbackRes),
            error = painterResource(id = fallbackRes),
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Image(
            painter = painterResource(id = fallbackRes),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@Composable
fun UserAvatar(
    photoUrl: String,
    displayName: String,
    size: Dp = 28.dp,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val initial = displayName.trim().take(1).uppercase().ifBlank { "Ö" }
    Surface(
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.size(size)
    ) {
        if (photoUrl.startsWith("data:image")) {
            val decodedBitmap = remember(photoUrl) {
                try {
                    val base64Part = photoUrl.substringAfter("base64,", "")
                    if (base64Part.isNotBlank()) {
                        val bytes = Base64.decode(base64Part, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                    } else {
                        null
                    }
                } catch (_: Exception) {
                    null
                }
            }
            if (decodedBitmap != null) {
                Image(
                    bitmap = decodedBitmap,
                    contentDescription = "$displayName profil fotoğrafı",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initial,
                        style = if (size >= 48.dp) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.labelSmall,
                        color = contentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else if (photoUrl.isNotBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "$displayName profil fotoğrafı",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = initial,
                    style = if (size >= 48.dp) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
