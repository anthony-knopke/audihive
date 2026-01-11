package com.darkaquadigital.audihive.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun OldCustomLinearProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.LightGray,
    progressColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp) // Adjust height as needed
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = progress.coerceIn(0f, 1f)) // Ensures progress stays in range
                .background(progressColor)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomLinearProgressIndicator(
    progress: Float, // 0f to 1f
    duration: Long,
    modifier: Modifier = Modifier,
    onSeek: (Long) -> Unit,
    backgroundColor: Color = Color.LightGray,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    handleColor: Color = MaterialTheme.colorScheme.secondary
) {
    var sliderPosition by remember { mutableFloatStateOf(progress) }
    var isUserSeeking by remember { mutableStateOf(false) }

    LaunchedEffect(progress) {
        if (!isUserSeeking) {
            sliderPosition = progress
        }
    }

    Slider(
        value = sliderPosition,
        onValueChange = {
            isUserSeeking = true
            sliderPosition = it
        },
        onValueChangeFinished = {
            isUserSeeking = false
            val seekPositionMs = (sliderPosition * duration).toLong()
            onSeek(seekPositionMs)
        },
        valueRange = 0f..1f,
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp) // Slimmer than default slider
            .padding(vertical = 4.dp), // Reduce top/bottom spacing
        colors = SliderDefaults.colors(
            thumbColor = Color.Transparent, // Hide the thumb
            activeTrackColor = progressColor,
            inactiveTrackColor = backgroundColor,
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent
        ),
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                thumbTrackGapSize = 0.dp
            )
        },
        thumb = {
            Spacer(modifier = Modifier.size(0.dp)) // or just {}
        }
    )
}

