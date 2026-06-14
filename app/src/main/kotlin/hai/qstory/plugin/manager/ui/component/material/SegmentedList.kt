package hai.qstory.plugin.manager.ui.component.material

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SegmentedColumn(
    modifier: Modifier = Modifier,
    title: String = "",
    visibleLen: Int = 0,
    content: List<@Composable () -> Unit>,
) {
    if (content.isEmpty()) return

    Column(modifier = modifier) {
        if (title.isNotEmpty()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }
        Column {
            content.forEachIndexed { index, _ ->
                val shape = when {
                    content.size == 1 -> RoundedCornerShape(24.dp)
                    index == 0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    index == content.lastIndex -> RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    else -> RoundedCornerShape(0.dp)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                ) {
                    content[index]()
                }
            }
        }
    }
}

@Composable
fun SegmentedListItem(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = headlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        modifier = modifier.clickable(
            enabled = enabled,
            onClick = { onClick?.invoke() },
        ),
        colors = ListItemDefaults.colors(
            containerColor = colorScheme.surfaceColorAtElevation(1.dp),
        ),
    )
}

@Composable
fun SegmentedSwitchItem(
    icon: ImageVector? = null,
    title: String,
    summary: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = summary?.let { { Text(it) } },
        leadingContent = icon?.let { { Icon(it, title) } },
        trailingContent = {
            ExpressiveSwitch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = { newValue ->
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onCheckedChange(newValue)
                },
            )
        },
        modifier = Modifier.clickable(enabled = enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onCheckedChange(!checked)
        },
        colors = ListItemDefaults.colors(
            containerColor = colorScheme.surfaceColorAtElevation(1.dp),
        ),
    )
}

@Composable
fun SegmentedDropdownItem(
    icon: ImageVector? = null,
    title: String,
    summary: String? = null,
    items: List<String>,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }

    val hasItems = items.isNotEmpty()
    val safeIndex = if (hasItems) {
        selectedIndex.coerceIn(0, items.lastIndex)
    } else {
        -1
    }

    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = summary?.let { { Text(it) } },
        leadingContent = icon?.let { { Icon(it, title) } },
        trailingContent = {
            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                Text(
                    text = if (hasItems && safeIndex >= 0) items[safeIndex] else "",
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(0.3f),
                    color = if (enabled) colorScheme.primary else colorScheme.onSurfaceVariant
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    items.forEachIndexed { index, text ->
                        DropdownMenuItem(
                            text = {
                                Text(text, color = if (index == safeIndex) colorScheme.primary else colorScheme.onSurface)
                            },
                            onClick = {
                                if (index in items.indices) {
                                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                    onItemSelected(index)
                                }
                                expanded = false
                            }
                        )
                    }
                }
            }
        },
        modifier = Modifier.clickable(
            enabled = enabled,
            onClick = {
                onClick?.invoke()
                expanded = true
            },
        ),
        colors = ListItemDefaults.colors(
            containerColor = colorScheme.surfaceColorAtElevation(1.dp),
        ),
    )
}

@Composable
fun SegmentedRadioItem(
    title: String,
    summary: String? = null,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = summary?.let { { Text(it) } },
        leadingContent = {
            RadioButton(
                selected = selected,
                onClick = null,
                enabled = enabled
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = colorScheme.surfaceColorAtElevation(1.dp),
        ),
    )
}

@Composable
fun SegmentedCheckboxItem(
    title: String,
    summary: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = summary?.let { { Text(it) } },
        leadingContent = {
            Checkbox(
                checked = checked,
                enabled = enabled,
                onCheckedChange = null,
                interactionSource = interactionSource,
                modifier = Modifier.size(24.dp)
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = colorScheme.surfaceColorAtElevation(1.dp),
        ),
    )
}
