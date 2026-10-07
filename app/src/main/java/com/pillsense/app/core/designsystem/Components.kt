@file:OptIn(ExperimentalMaterial3Api::class)

package com.pillsense.app.core.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==================== PsButton ====================
/**
 * PillSense Button: iOS-style with spring animation on press.
 * - Primary (filled)
 * - Secondary (tonal)
 * - Destructive (red)
 * - Text (no background)
 */
@Composable
fun PsButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: PsButtonStyle = PsButtonStyle.Primary,
    height: Int = 56,
    isLoading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release -> isPressed = false
                is PressInteraction.Cancel -> isPressed = false
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        label = "button_scale"
    )

    val (backgroundColor, textColor) = when (style) {
        PsButtonStyle.Primary -> Pair(MaterialTheme.colorScheme.primary, Color.White)
        PsButtonStyle.Secondary -> Pair(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.onSurface
        )
        PsButtonStyle.Destructive -> Pair(Color(0xFFFF3B30), Color.White)
        PsButtonStyle.Text -> Pair(Color.Transparent, MaterialTheme.colorScheme.primary)
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = height.coerceAtLeast(48).dp)
            .scale(scale),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.5f)
        ),
        shape = PillSenseShape.large,
        interactionSource = interactionSource
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = textColor,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

enum class PsButtonStyle {
    Primary, Secondary, Destructive, Text
}

// ==================== PsTextField ====================
/**
 * PillSense TextField: iOS-style input field with subtle borders and no ripple.
 */
@Composable
fun PsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(PillSenseShape.medium)
                .background(
                    if (isFocused)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
                .border(
                    width = 0.5.dp,
                    color = if (isFocused)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = PillSenseShape.medium
                )
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused },
                enabled = enabled && !readOnly,
                readOnly = readOnly,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = keyboardOptions,
                visualTransformation = visualTransformation,
                singleLine = singleLine,
                decorationBox = { innerTextField ->
                    if (value.isEmpty() && !isFocused) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

// ==================== PsCard ====================
/**
 * PillSense Card: iOS-style surface with subtle border and soft shadow.
 */
@Composable
fun PsCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(PillSenseShape.extraLarge)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                shape = PillSenseShape.extraLarge
            ),
        color = backgroundColor,
        shape = PillSenseShape.extraLarge,
        shadowElevation = 2.dp
    ) {
        content()
    }
}

// ==================== PsListRow ====================
/**
 * PillSense List Row: iOS-style row with separator at the bottom.
 */
@Composable
fun PsListRow(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(enabled = onClick != null, onClick = onClick ?: {})
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (leadingContent != null) {
                leadingContent()
            }
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 56.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    }
}

// ==================== PsSegmentedControl ====================
/**
 * PillSense Segmented Control: iOS-style segmented picker.
 */
@Composable
fun PsSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(PillSenseShape.large)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = PillSenseShape.large
            )
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items.forEachIndexed { index, item ->
            Button(
                onClick = { onSelectionChange(index) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (index == selectedIndex)
                        MaterialTheme.colorScheme.background
                    else
                        Color.Transparent,
                    contentColor = if (index == selectedIndex)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = PillSenseShape.large,
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

// ==================== PsBadge ====================
/**
 * PillSense Badge: iOS-style chip/badge.
 */
@Composable
fun PsBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .clip(PillSenseShape.large),
        color = backgroundColor,
        shape = PillSenseShape.large
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (leadingIcon != null) {
                leadingIcon()
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = textColor
            )
        }
    }
}

// ==================== PsSectionHeader ====================
/**
 * PillSense Section Header: iOS-style section divider with title.
 */
@Composable
fun PsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        modifier = modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// ==================== PsBottomBar ====================
/**
 * PillSense Bottom Navigation Bar: iOS-style with semi-transparent background.
 */
@Composable
fun PsBottomBar(
    items: List<PsBottomBarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                PsBottomBarItemContent(
                    item = item,
                    isSelected = index == selectedIndex,
                    onClick = { onItemSelected(index) }
                )
            }
        }
    }
}

data class PsBottomBarItem(
    val icon: @Composable () -> Unit,
    val label: String,
    val prominent: Boolean = false,
)

@Composable
private fun PsBottomBarItemContent(
    item: PsBottomBarItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(64.dp)
            .heightIn(min = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (item.prominent) 48.dp else 28.dp),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(
                LocalContentColor provides if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                item.icon()
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}
