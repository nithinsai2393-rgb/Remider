package com.example.remider.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remider.R
import kotlinx.coroutines.delay

@Composable
fun CustomDropdown(
    modifier: Modifier = Modifier,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var parentSize by remember { mutableStateOf(IntSize.Zero) }

    val density = LocalDensity.current

    Box(
        modifier = modifier
            .padding(6.dp)
            .fillMaxWidth()
            .onGloballyPositioned {
                parentSize = it.size
            }
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { expanded = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedOption,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Image(
                    painter = painterResource(id = R.drawable.outline_keyboard_arrow_down_24),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(density) { parentSize.width.toDp() })
                .background(MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(10.dp)
        ) {
            options.forEach { option ->

                val isSelected = option == selectedOption

                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        expanded = false
                        onOptionSelected(option)
                    },
                    modifier = Modifier.background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
fun Modifier.snakeBorder(
    enabled: Boolean,
    shape: Shape = RoundedCornerShape(28.dp),
    color: Color = Color(0xFF4361EE)
): Modifier = if (!enabled) this else {
    val infiniteTransition = rememberInfiniteTransition(label = "snake")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    this.then(
        Modifier
            .clip(shape)
            .drawWithContent {
                rotate(angle) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            0.0f to Color.Transparent,
                            0.2f to color,
                            0.4f to Color.Transparent,
                            1.0f to Color.Transparent
                        ),
                        radius = size.maxDimension * 1.5f
                    )
                }
                drawContent()
            }
    )
}

@Composable
fun CustomRow(
    modifier: Modifier = Modifier,
    options: List<String>,
    selectedOption: String,
    isScrollable: Boolean = true,
    optionSelected: (String) -> Unit = {}
) {
    val accentColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .then(
                if (isScrollable) Modifier.horizontalScroll(rememberScrollState())
                else Modifier.fillMaxWidth()
            )
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption

            Box(
                modifier = Modifier
                    .then(if (isScrollable) Modifier.wrapContentWidth() else Modifier.weight(1f))
                    .height(40.dp)
                    .snakeBorder(
                        enabled = isSelected,
                        color = accentColor,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .background(
                        if (isSelected) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(20.dp)
                    )
                    .then(
                        if (isSelected) Modifier
                            .padding(2.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
                        else Modifier
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        optionSelected(option)
                    }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    textAlign = TextAlign.Center,
                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CustomTaskCounter(
    number: Int = 0
){
    Card(
        modifier = Modifier
            .width(80.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            text = "$number tasks",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
fun BottomPopup(
    message: String,
    backgroundColor: Color = Color(0xFFE2F14E),
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(3000)
        onDismiss()
    }

    val icon = if (message.contains("⚠️")) {
        R.drawable.baseline_close_24 // Use a generic error/warning icon
    } else {
        R.drawable.outline_nest_clock_farsight_analog_24 // Success icon
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp, start = 20.dp, end = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                            if (backgroundColor == Color(0xFFE2F14E)) Color.Black else Color.White
                        )
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = message.replace("⚠️ ", "").replace("✅ ", ""),
                    color = if (backgroundColor == Color(0xFFE2F14E)) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )

                Image(
                    painter = painterResource(id = R.drawable.baseline_close_24),
                    contentDescription = "Dismiss",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDismiss() },
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                        if (backgroundColor == Color(0xFFE2F14E)) Color.Black.copy(0.4f) else Color.White.copy(0.4f)
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomRowPreview() {
    CustomRow(
        modifier = Modifier,
        options = listOf("High", "Medium", "Low"),
        selectedOption = "Medium"
    )
}

@Preview
@Composable
private fun CustomTaskCounterPreview() {
    CustomTaskCounter(
        number = 5
    )
}

@Preview
@Composable
private fun BottomPopUpPreview() {
    BottomPopup(
        message = "Task added successfully",
        onDismiss = {}
    )
}