package com.rgbcoding.yolodarts.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rgbcoding.yolodarts.data.Player

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerCard(
    player: Player,
    isItsTurn: Boolean,
    modifier: Modifier
) {
    val elevation by animateFloatAsState(
        targetValue = if (isItsTurn) 8f else 2f,
        animationSpec = tween(durationMillis = 300)
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = if (isItsTurn) 
            MaterialTheme.colorScheme.primaryContainer
        else 
            MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = elevation.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = player.name.value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = if (isItsTurn) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isItsTurn) 
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                AnimatedVisibility(
                    visible = isItsTurn,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Score Left",
                    value = player.scoreLeft.value.toString(),
                    modifier = Modifier.weight(1f),
                    isHighlighted = isItsTurn
                )
                StatItem(
                    label = "Average",
                    value = String.format("%.1f", player.throws.value.average()),
                    modifier = Modifier.weight(1f),
                    isHighlighted = isItsTurn
                )
                StatItem(
                    label = "Darts",
                    value = player.throws.value.size.toString(),
                    modifier = Modifier.weight(1f),
                    isHighlighted = isItsTurn
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isHighlighted)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isHighlighted)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
fun previewPlayerCard() {
    val player1 = Player("Player 1")
    val player2 = Player("Player 2")
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            PlayerCard(player1, false, Modifier.weight(1f))
            PlayerCard(player2, true, Modifier.weight(1f))
        }
    }
}
