package com.hornet.movies.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.hornet.movies.util.TextVisionFinder

@Composable
fun PosterModal(
    posterUrl: String,
    movieTitle: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    /****************** Code Addition
     ******************/
    val textFinder = remember{TextVisionFinder()}
    textFinder.findText(posterUrl)
    /****************** Code Addition
     ******************/

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = posterUrl,
                contentDescription = "Full poster for $movieTitle",
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onDismiss() }
                    /****************** Code Addition
                     ******************/
                    .drawWithContent {
                        //Draw Original Content
                        drawContent()
                        //Additional Draws
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(50f,50f),
                            size = Size(150f,150f)
                        )
                    },
                    /****************** Code Addition
                     ******************/
                contentScale = ContentScale.Fit
            )
        }
    }
}