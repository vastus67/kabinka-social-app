package app.kabinka.frontend.magazine

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.launch

/**
 * Magazine Interactive Ad Card
 * Wrapper composable that handles tap interaction
 */
@Composable
fun MagazineInteractiveAdCard(
    ad: MagazineAd,
    isSaved: Boolean,
    onOpenDetail: (MagazineAd) -> Unit,
    onSave: (MagazineAd) -> Unit,
    onDismiss: (MagazineAd) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    // Simple scale animation for tap feedback
    val scale = remember { Animatable(1f) }
    
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        scope.launch {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            scale.animateTo(0.99f, tween(50))
                            scale.animateTo(1f, tween(50))
                        }
                        onOpenDetail(ad)
                    }
                )
            }
    ) {
        content()
    }
}
