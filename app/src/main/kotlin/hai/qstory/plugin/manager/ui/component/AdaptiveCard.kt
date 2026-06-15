package hai.qstory.plugin.manager.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card as M3Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hai.qstory.plugin.manager.ui.theme.LocalUiMode
import hai.qstory.plugin.manager.ui.theme.UiMode
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun AdaptiveCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    when (LocalUiMode.current) {
        UiMode.Material -> {
            M3Card(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                content()
            }
        }
        UiMode.Miuix -> {
            MiuixCard(
                modifier = modifier,
                pressFeedbackType = PressFeedbackType.Sink,
                showIndication = true,
                onClick = onClick,
            ) {
                content()
            }
        }
    }
}

@Composable
fun AdaptiveCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    when (LocalUiMode.current) {
        UiMode.Material -> {
            M3Card(
                modifier = modifier,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                content()
            }
        }
        UiMode.Miuix -> {
            MiuixCard(modifier = modifier) {
                content()
            }
        }
    }
}
