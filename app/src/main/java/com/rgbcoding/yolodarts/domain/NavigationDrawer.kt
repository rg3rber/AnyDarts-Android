package com.rgbcoding.yolodarts.domain

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.reflect.KFunction1

@Composable
fun DrawerHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Menu", fontSize = 32.sp, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun DrawerBody(
    items: List<MenuItem>,
    modifier: Modifier = Modifier,
    itemTextStyle: TextStyle = TextStyle(fontSize = 18.sp),
) {
    // remove cursor once done
    val focusManager = LocalFocusManager.current
    LazyColumn(modifier) {
        items(items) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.contentDescription
                )
                if (item.type == MenuItemType.BUTTON) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, end = 32.dp),

                        onClick = item.buttonAction,
                        colors = buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        //Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            item.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                } else {

                    TextField(
                        value = item.value,
                        singleLine = true,
                        onValueChange = item.onValueChange,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        label = {
                            Text(
                                item.text,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                //fontSize = 12.sp,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        keyboardOptions = item.keyboardOptions ?: KeyboardOptions.Default,
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}


fun dummyKFunction(input: String) {
    // DO NOTHING
}

@Preview(showBackground = true)
@Composable
fun previewDrawer() {
    Box(
        modifier = Modifier.fillMaxSize()
    )
    DrawerHeader()
    DrawerBody(
        items = listOf(
            MenuItem(
                id = "home",
                value = "My Home",
                text = "Home",
                onValueChange = ::dummyKFunction,
                contentDescription = "Go to home screen",
                icon = Icons.Default.Home,
            ),
            MenuItem(
                id = "settings",
                value = "My Settings",
                onValueChange = ::dummyKFunction,
                text = "Settings",
                contentDescription = "Go to settings screen",
                icon = Icons.Default.Settings
            ),
            MenuItem(
                id = "help",
                value = "My Help",
                onValueChange = ::dummyKFunction,
                text = "Help",
                contentDescription = "Get help",
                icon = Icons.Default.Info
            ),
            MenuItem(
                type = MenuItemType.BUTTON,
                id = "button",
                value = "My Button",
                onValueChange = ::dummyKFunction,
                text = "Button",
                contentDescription = "Get help",
                icon = Icons.Default.Info
            ),
        ),
    )
}

enum class MenuItemType(val type: Int) {
    TEXTFIELD(0),
    BUTTON(1)
}

data class MenuItem(
    val id: String,
    val type: MenuItemType = MenuItemType.TEXTFIELD,
    val value: String,
    val text: String,
    val buttonAction: () -> Unit = {},
    val keyboardOptions: KeyboardOptions? = null,
    val onValueChange: KFunction1<String, Unit> = ::dummyKFunction,
    val contentDescription: String,
    val icon: ImageVector
)