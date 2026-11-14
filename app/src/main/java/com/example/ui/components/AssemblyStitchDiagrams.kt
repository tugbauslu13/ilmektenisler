package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AssemblyDiagramType
import com.example.data.model.AssemblyGuideStep

val defaultAtelierAssemblySteps = listOf(
    AssemblyGuideStep(
        stepTitle = "1. Parçaları Toplu İğne ile Hizalama",
        instruction = "Amigurumi veya hırka parçalarını dikmeden önce ilmek sıralarını sayarak toplu iğnelerle sabitleyin. Simetrinin her açıdan eşit olduğundan emin olduktan sonra dikime geçin.",
        stitchTechnique = "Amigurumi & Parça Hizalama",
        diagramType = AssemblyDiagramType.AMIGURUMI_JOIN
    ),
    AssemblyGuideStep(
        stepTitle = "2. Görünmez Matras (Merdiven) Dikişi",
        instruction = "Hırka, kazak ve düz panellerde iki kenarı yan yana getirin. Etamin iğnesiyle sağ ve sol parçanın kenar ilmek çubuklarından sırayla yatay geçiş yapın ve her 3-4 dikişte ipi nazikçe çekin.",
        stitchTechnique = "Matras Dikişi",
        diagramType = AssemblyDiagramType.MATTRESS_STITCH
    ),
    AssemblyGuideStep(
        stepTitle = "3. Tığ İle İlmek Kaydırma (cc) Birleştirme",
        instruction = "Motifli hırka ve çantalarda iki motifin dış ilmek (BLO) kanatlarını birbirine bakacak şekilde tutun; aradan ince bir tığ ile ilmek kaydırma (cc) örerek dümdüz bir hat oluşturun.",
        stitchTechnique = "BLO İlmek Kaydırma",
        diagramType = AssemblyDiagramType.SLIP_STITCH_JOIN
    ),
    AssemblyGuideStep(
        stepTitle = "4. İp Uçlarını Gizleme ve Bloklama",
        instruction = "İp uçlarını arka yüzde en az 3 farklı yönde ilmek altlarından yürütüp kesin. Son dokunuşta örgüye zarar vermeyen ıslak veya hafif buharlı bloklama ile köşeleri sabitleyin.",
        stitchTechnique = "İp Gizleme & Bloklama",
        diagramType = AssemblyDiagramType.FINISHING_BLOCKING
    )
)

@Composable
fun AssemblyGuideContent(
    steps: List<AssemblyGuideStep>,
    patternTitle: String?,
    modifier: Modifier = Modifier
) {
    val activeSteps = steps.ifEmpty { defaultAtelierAssemblySteps }
    val checklistItems = remember {
        listOf(
            "Dikiş için her parçada örgü ipinden en az 3 kat uzunlukta pay bıraktım.",
            "Parçaları dikmeden önce toplu iğne veya ilmek belirleyici ile sabitledim.",
            "İp uçlarını dikiş hattından en az 3 yöne yürütüp sıfırdan kestim.",
            "Nemli bez veya buharlı bloklama ile son formu sabitledim."
        )
    }
    val checkedMap = remember(patternTitle) { mutableStateMapOf<Int, Boolean>() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Birleştirme ve son dokunuş rehberi",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (patternTitle != null) {
                            "$patternTitle • Birleştirme ve Son Dokunuş"
                        } else {
                            "Birleştirme ve Son Dokunuş Rehberi"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Ana parçalar örüldükten sonra dikişlerin görünmez, hizaların simetrik ve formun atölye kalitesinde olması için adım adım görsel diyagramları takip edin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.88f)
                    )
                }
            }
        }

        activeSteps.forEachIndexed { index, step ->
            CraftCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("assembly_step_card_${index + 1}")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = step.stepTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.size(6.dp))
                        CraftBadge(text = step.stitchTechnique, isTerracotta = true)
                    }

                    AssemblyDiagramCard(diagramType = step.diagramType)

                    Text(
                        text = step.instruction,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        CraftCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("finishing_checklist_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Atölye Son Kontrol Listesi",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                checklistItems.forEachIndexed { idx, label ->
                    val isDone = checkedMap[idx] == true
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDone) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background,
                        border = BorderStroke(
                            1.dp,
                            if (isDone) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { checkedMap[idx] = !isDone }
                            .testTag("finishing_check_item_$idx")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDone) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                contentDescription = if (isDone) "Tamamlandı" else "Tamamla",
                                tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssemblyDiagramCard(diagramType: AssemblyDiagramType) {
    val colors = MaterialTheme.colorScheme
    val caption = when (diagramType) {
        AssemblyDiagramType.MATTRESS_STITCH -> "Şema: Sağ ve sol ilmek çubukları arasından yatay zikzak geçiş"
        AssemblyDiagramType.AMIGURUMI_JOIN -> "Şema: Toplu iğne ile sabitleme ve karşılıklı 1'er ilmekten dikiş"
        AssemblyDiagramType.SLIP_STITCH_JOIN -> "Şema: Dış ilmek kanatlarından (BLO) tığ ile cc birleştirme"
        AssemblyDiagramType.FINISHING_BLOCKING -> "Şema: İpi arka yüzde 3 yöne gizleme ve köşelerden bloklama"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.background,
        border = BorderStroke(1.dp, colors.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .semantics { contentDescription = caption }
            ) {
                val w = size.width
                val h = size.height
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)

                val stepX = w / 8f
                val stepY = h / 4f
                for (gx in 1..7) {
                    for (gy in 1..3) {
                        drawCircle(
                            color = colors.outline.copy(alpha = 0.55f),
                            radius = 2f,
                            center = Offset(gx * stepX, gy * stepY)
                        )
                    }
                }

                when (diagramType) {
                    AssemblyDiagramType.MATTRESS_STITCH -> {
                        val leftX = w * 0.34f
                        val rightX = w * 0.66f
                        val blockW = w * 0.24f

                        drawRoundRect(
                            color = colors.primaryContainer,
                            topLeft = Offset(leftX - blockW, h * 0.12f),
                            size = Size(blockW, h * 0.76f),
                            cornerRadius = CornerRadius(14f, 14f)
                        )
                        drawRoundRect(
                            color = colors.surface,
                            topLeft = Offset(rightX, h * 0.12f),
                            size = Size(blockW, h * 0.76f),
                            cornerRadius = CornerRadius(14f, 14f)
                        )
                        drawRoundRect(
                            color = colors.primary,
                            topLeft = Offset(leftX - blockW, h * 0.12f),
                            size = Size(blockW, h * 0.76f),
                            cornerRadius = CornerRadius(14f, 14f),
                            style = Stroke(width = 3f)
                        )
                        drawRoundRect(
                            color = colors.secondary,
                            topLeft = Offset(rightX, h * 0.12f),
                            size = Size(blockW, h * 0.76f),
                            cornerRadius = CornerRadius(14f, 14f),
                            style = Stroke(width = 3f)
                        )

                        val ladderPath = Path()
                        val rows = 4
                        for (r in 0 until rows) {
                            val y = h * (0.24f + r * 0.17f)
                            val yNext = y + h * 0.08f
                            drawCircle(colors.primary, 5f, Offset(leftX - 8f, y))
                            drawCircle(colors.primary, 5f, Offset(rightX + 8f, yNext))

                            if (r == 0) {
                                ladderPath.moveTo(leftX - 8f, y)
                            } else {
                                ladderPath.lineTo(leftX - 8f, y)
                            }
                            ladderPath.lineTo(rightX + 8f, yNext)
                        }
                        drawPath(
                            path = ladderPath,
                            color = colors.tertiary,
                            style = Stroke(
                                width = 4.5f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                                pathEffect = dashEffect
                            )
                        )
                        drawLine(
                            color = colors.onSurface,
                            start = Offset(rightX + 8f, h * 0.83f),
                            end = Offset(rightX + blockW * 0.55f, h * 0.92f),
                            strokeWidth = 5f,
                            cap = StrokeCap.Round
                        )
                    }

                    AssemblyDiagramType.AMIGURUMI_JOIN -> {
                        val bodyCenter = Offset(w * 0.5f, h * 0.66f)
                        val headCenter = Offset(w * 0.5f, h * 0.30f)

                        drawOval(
                            color = colors.primaryContainer,
                            topLeft = Offset(bodyCenter.x - w * 0.22f, bodyCenter.y - h * 0.24f),
                            size = Size(w * 0.44f, h * 0.48f)
                        )
                        drawOval(
                            color = colors.primary,
                            topLeft = Offset(bodyCenter.x - w * 0.22f, bodyCenter.y - h * 0.24f),
                            size = Size(w * 0.44f, h * 0.48f),
                            style = Stroke(width = 3f)
                        )

                        drawCircle(
                            color = colors.surface,
                            radius = h * 0.22f,
                            center = headCenter
                        )
                        drawCircle(
                            color = colors.primary,
                            radius = h * 0.22f,
                            center = headCenter,
                            style = Stroke(width = 3f)
                        )

                        val leftPinBase = Offset(w * 0.39f, h * 0.47f)
                        val rightPinBase = Offset(w * 0.61f, h * 0.47f)
                        drawLine(colors.onSurface, leftPinBase, Offset(w * 0.31f, h * 0.35f), strokeWidth = 3f)
                        drawCircle(colors.tertiary, 7f, Offset(w * 0.31f, h * 0.35f))
                        drawLine(colors.onSurface, rightPinBase, Offset(w * 0.69f, h * 0.35f), strokeWidth = 3f)
                        drawCircle(colors.tertiary, 7f, Offset(w * 0.69f, h * 0.35f))

                        val seamPath = Path().apply {
                            moveTo(w * 0.40f, h * 0.50f)
                            cubicTo(w * 0.44f, h * 0.44f, w * 0.46f, h * 0.55f, w * 0.50f, h * 0.49f)
                            cubicTo(w * 0.54f, h * 0.44f, w * 0.56f, h * 0.55f, w * 0.60f, h * 0.50f)
                        }
                        drawPath(
                            path = seamPath,
                            color = colors.tertiary,
                            style = Stroke(width = 4.5f, cap = StrokeCap.Round, pathEffect = dashEffect)
                        )
                    }

                    AssemblyDiagramType.SLIP_STITCH_JOIN -> {
                        val gap = w * 0.08f
                        val squareSize = Size(w * 0.28f, h * 0.72f)
                        val leftTopLeft = Offset(w * 0.5f - gap / 2f - squareSize.width, h * 0.14f)
                        val rightTopLeft = Offset(w * 0.5f + gap / 2f, h * 0.14f)

                        drawRoundRect(colors.primaryContainer, leftTopLeft, squareSize, CornerRadius(12f, 12f))
                        drawRoundRect(colors.primary, leftTopLeft, squareSize, CornerRadius(12f, 12f), Stroke(3f))
                        drawRoundRect(colors.tertiaryContainer, rightTopLeft, squareSize, CornerRadius(12f, 12f))
                        drawRoundRect(colors.tertiary, rightTopLeft, squareSize, CornerRadius(12f, 12f), Stroke(3f))

                        val joinLeftX = leftTopLeft.x + squareSize.width
                        val joinRightX = rightTopLeft.x
                        val chainPath = Path()
                        for (i in 0..4) {
                            val y = h * (0.22f + i * 0.14f)
                            drawCircle(colors.primary, 4.5f, Offset(joinLeftX, y))
                            drawCircle(colors.tertiary, 4.5f, Offset(joinRightX, y))
                            if (i == 0) {
                                chainPath.moveTo(joinLeftX, y)
                            } else {
                                chainPath.lineTo(w * 0.5f, y - h * 0.06f)
                            }
                            chainPath.lineTo(joinRightX, y)
                        }
                        drawPath(
                            path = chainPath,
                            color = colors.onSurface,
                            style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                        drawLine(
                            color = colors.tertiary,
                            start = Offset(w * 0.5f, h * 0.22f),
                            end = Offset(w * 0.58f, h * 0.06f),
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )
                    }

                    AssemblyDiagramType.FINISHING_BLOCKING -> {
                        val weavePanel = Size(w * 0.36f, h * 0.74f)
                        val weaveTop = Offset(w * 0.08f, h * 0.13f)
                        drawRoundRect(colors.primaryContainer, weaveTop, weavePanel, CornerRadius(14f, 14f))
                        drawRoundRect(colors.primary, weaveTop, weavePanel, CornerRadius(14f, 14f), Stroke(3f))

                        val weavePath = Path().apply {
                            moveTo(w * 0.13f, h * 0.32f)
                            lineTo(w * 0.36f, h * 0.32f)
                            lineTo(w * 0.18f, h * 0.52f)
                            lineTo(w * 0.36f, h * 0.72f)
                        }
                        drawPath(
                            path = weavePath,
                            color = colors.tertiary,
                            style = Stroke(
                                width = 4.5f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                                pathEffect = dashEffect
                            )
                        )

                        val blockTop = Offset(w * 0.54f, h * 0.18f)
                        val blockSize = Size(w * 0.36f, h * 0.66f)
                        drawRoundRect(colors.surface, blockTop, blockSize, CornerRadius(10f, 10f))
                        drawRoundRect(
                            color = colors.onSurface.copy(alpha = 0.7f),
                            topLeft = blockTop,
                            size = blockSize,
                            cornerRadius = CornerRadius(10f, 10f),
                            style = Stroke(width = 2.5f, pathEffect = dashEffect)
                        )

                        val corners = listOf(
                            blockTop,
                            Offset(blockTop.x + blockSize.width, blockTop.y),
                            Offset(blockTop.x, blockTop.y + blockSize.height),
                            Offset(blockTop.x + blockSize.width, blockTop.y + blockSize.height)
                        )
                        corners.forEach { c ->
                            drawCircle(colors.tertiary, 7f, c)
                        }

                        for (s in 0..2) {
                            val sx = blockTop.x + blockSize.width * (0.28f + s * 0.22f)
                            val steamPath = Path().apply {
                                moveTo(sx, blockTop.y + 18f)
                                quadraticTo(sx - 8f, blockTop.y + 2f, sx + 4f, blockTop.y - 12f)
                            }
                            drawPath(
                                path = steamPath,
                                color = colors.secondary,
                                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }

            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
