package hai.qstory.plugin.manager.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text as M3Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import hai.qstory.plugin.manager.ui.theme.LocalUiMode
import hai.qstory.plugin.manager.ui.theme.UiMode
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator as MiuixInfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.Card as MiuixCard

@Composable
fun AdaptiveText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    style: TextStyle? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    when (LocalUiMode.current) {
        UiMode.Material -> {
            M3Text(
                text = text,
                modifier = modifier,
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight,
                style = style ?: MaterialTheme.typography.bodyMedium,
                maxLines = maxLines,
            )
        }
        UiMode.Miuix -> {
            MiuixText(
                text = text,
                modifier = modifier,
                color = if (color != Color.Unspecified) color else MiuixTheme.colorScheme.onBackground,
                fontSize = fontSize,
                fontWeight = fontWeight,
                style = style ?: MiuixTheme.textStyles.body1,
                maxLines = maxLines,
            )
        }
    }
}

@Composable
fun AdaptiveSmallTitle(text: String) {
    when (LocalUiMode.current) {
        UiMode.Material -> {
            M3Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
            )
        }
        UiMode.Miuix -> {
            SmallTitle(text = text)
        }
    }
}

@Composable
fun AdaptiveInfiniteProgressIndicator(modifier: Modifier = Modifier) {
    when (LocalUiMode.current) {
        UiMode.Material -> {
            CircularProgressIndicator(modifier = modifier)
        }
        UiMode.Miuix -> {
            MiuixInfiniteProgressIndicator(
                modifier = modifier,
                color = MiuixTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AdaptiveTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
) {
    when (LocalUiMode.current) {
        UiMode.Material -> {
            androidx.compose.material3.OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier,
                label = { M3Text(text = label) },
                singleLine = true,
            )
        }
        UiMode.Miuix -> {
            top.yukonga.miuix.kmp.basic.TextField(
                value = value,
                onValueChange = onValueChange,
                label = label,
                modifier = modifier,
            )
        }
    }
}

@Composable
fun AdaptiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    when (LocalUiMode.current) {
        UiMode.Material -> {
            androidx.compose.material3.Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
            ) {
                content()
            }
        }
        UiMode.Miuix -> {
            top.yukonga.miuix.kmp.basic.Button(
                onClick = onClick,
                modifier = modifier,
            ) {
                content()
            }
        }
    }
}

@Composable
fun adaptiveOnSurfaceSecondary(): Color = when (LocalUiMode.current) {
    UiMode.Material -> MaterialTheme.colorScheme.onSurfaceVariant
    UiMode.Miuix -> MiuixTheme.colorScheme.onSurfaceSecondary
}

@Composable
fun adaptiveOnSurfaceVariantSummary(): Color = when (LocalUiMode.current) {
    UiMode.Material -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f)
    UiMode.Miuix -> MiuixTheme.colorScheme.onSurfaceVariantSummary
}

@Composable
fun adaptivePrimaryColor(): Color = when (LocalUiMode.current) {
    UiMode.Material -> MaterialTheme.colorScheme.primary
    UiMode.Miuix -> MiuixTheme.colorScheme.primary
}

@Composable
fun adaptiveBackgroundColor(): Color = when (LocalUiMode.current) {
    UiMode.Material -> MaterialTheme.colorScheme.background
    UiMode.Miuix -> MiuixTheme.colorScheme.background
}

@Composable
fun adaptiveOnSecondaryContainer(): Color = when (LocalUiMode.current) {
    UiMode.Material -> MaterialTheme.colorScheme.onSecondaryContainer
    UiMode.Miuix -> MiuixTheme.colorScheme.onSecondaryContainer
}

@Composable
fun adaptiveSecondaryContainer(): Color = when (LocalUiMode.current) {
    UiMode.Material -> MaterialTheme.colorScheme.secondaryContainer
    UiMode.Miuix -> MiuixTheme.colorScheme.secondaryContainer
}

@Composable
fun AdaptiveDropdownField(
    selectedText: String,
    items: List<String>,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }

    when (LocalUiMode.current) {
        UiMode.Material -> {
            Box(modifier = modifier) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (expanded)
                                Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(0.dp)
                                )
                            else
                                Modifier.border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(0.dp)
                                )
                        )
                        .clickable(enabled = enabled) { expanded = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    M3Text(
                        text = selectedText,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Icon(
                        imageVector = Icons.Rounded.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    shape = RoundedCornerShape(0.dp),
                ) {
                    items.forEachIndexed { index, text ->
                        DropdownMenuItem(
                            text = {
                                M3Text(
                                    text = text,
                                    color = if (text == selectedText)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            onClick = {
                                onItemSelected(index)
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
        UiMode.Miuix -> {
            Column(modifier = modifier) {
                top.yukonga.miuix.kmp.basic.Button(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    MiuixText(text = selectedText)
                }
                if (expanded) {
                    Popup(
                        onDismissRequest = { expanded = false },
                    ) {
                        MiuixCard {
                            Column {
                                items.forEachIndexed { index, text ->
                                    val isSelected = text == selectedText
                                    MiuixText(
                                        text = text,
                                        color = if (isSelected)
                                            MiuixTheme.colorScheme.primary
                                        else
                                            MiuixTheme.colorScheme.onSurface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onItemSelected(index)
                                                expanded = false
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
