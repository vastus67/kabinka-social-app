package app.kabinka.frontend.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.kabinka.social.model.Attachment
import coil.compose.AsyncImage
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.ChevronLeftSolid
import compose.icons.lineawesomeicons.ChevronRightSolid

@Composable
fun ImageViewerDialog(
    attachments: List<Attachment>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(initialIndex) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Image content
            val currentAttachment = attachments.getOrNull(currentIndex)
            if (currentAttachment != null) {
                AsyncImage(
                    model = currentAttachment.url,
                    contentDescription = currentAttachment.description,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                if (scale > 1f) {
                                    offset = Offset(
                                        x = (offset.x + pan.x).coerceIn(
                                            -(size.width * (scale - 1) / 2),
                                            size.width * (scale - 1) / 2
                                        ),
                                        y = (offset.y + pan.y).coerceIn(
                                            -(size.height * (scale - 1) / 2),
                                            size.height * (scale - 1) / 2
                                        )
                                    )
                                } else {
                                    offset = Offset.Zero
                                }
                            }
                        },
                    contentScale = ContentScale.Fit
                )
            }
            
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
            
            // Image counter if multiple images
            if (attachments.size > 1) {
                Text(
                    text = "${currentIndex + 1} / ${attachments.size}",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
                
                // Navigation between images
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Previous button
                    if (currentIndex > 0) {
                        IconButton(
                            onClick = { 
                                currentIndex--
                                scale = 1f
                                offset = Offset.Zero
                            },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = LineAwesomeIcons.ChevronLeftSolid,
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(80.dp))
                    }
                    
                    // Next button
                    if (currentIndex < attachments.size - 1) {
                        IconButton(
                            onClick = { 
                                currentIndex++
                                scale = 1f
                                offset = Offset.Zero
                            },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = LineAwesomeIcons.ChevronRightSolid,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(80.dp))
                    }
                }
            }
        }
    }
}
