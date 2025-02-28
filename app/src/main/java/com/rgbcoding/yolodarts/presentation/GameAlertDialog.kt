package com.rgbcoding.yolodarts.presentation

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun GameAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    alertCode: AlertCode?,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Alert Icon")
        },
        title = {
            Text(
                text = when (alertCode) {
                    AlertCode.INVALID_SCORE -> "Invalid score format."
                    AlertCode.OVERSHOT -> "Overshot"
                    AlertCode.GAME_OVER -> "Congratulations"
                    else -> {
                        Log.e("Alerts", "AlertDialog called with unkonw alertCode: $alertCode")
                        "Internal Error"
                    }
                }
            )
        },
        text = {
            Text(
                text = when (alertCode) {
                    AlertCode.INVALID_SCORE -> "Only Integers from 0 to 180 are permitted"
                    AlertCode.OVERSHOT -> "You must finish exactly with Score 0 left"
                    AlertCode.GAME_OVER -> "You Won The Game"
                    else -> {
                        Log.e("Alerts", "AlertDialog called with unkonwn alertCode: $alertCode")
                        "Internal Error"
                    }
                }
            )
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = { },
//            TextButton(
//                onClick = {
//                    onConfirmation()
//                }
//            ) {
//                Text("OK")
//            }
//        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("OK")
            }
        }
    )
}

enum class AlertCode(code: String) {
    VALID_SCORE("Valid Score"),
    GAME_OVER("Game Over"),
    INVALID_SCORE("Invalid Score"),
    OVERSHOT("Overshot")
}