package com.example.myapplication.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController

@Preview(showBackground = true)
@Composable
fun HomeRouteScreenPreview1() {
    HomeRouteScreenPreview2(currentRoute = TodoDestinations.BATTERY_ROUTE)
}

@Preview(showBackground = true)
@Composable
fun HomeRouteScreenPreview2(currentRoute: String = TodoDestinations.APP_LISTS_ROUTE) {
    val navController = rememberNavController()
    val navigationActions = remember { TodoNavigationActions(navController) }

    HomeRouteScreen(
        currentRoute = currentRoute,
        navActions = navigationActions,
    ) {
        ChargingAlarmScreenPreview()
    }
}

@Composable
fun HomeRouteScreen(
    currentRoute: String,
    navActions: TodoNavigationActions,
    content: @Composable BoxScope.() -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        val buttonTextStyle = MaterialTheme.typography.titleMedium
        val tabButtonsHeight = buttonTextStyle.lineHeight.value.dp + 30.dp
        val surfaceHeight = maxHeight - tabButtonsHeight

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(surfaceHeight),
                content = content
            )

            TabButtons(
                tabButtonsHeight,
                navActions,
                currentRoute,
                buttonTextStyle
            )
        }
    }
}

@Composable
private fun TabButtons(
    tabButtonsHeight: Dp,
    navActions: TodoNavigationActions,
    currentRoute: String,
    buttonTextStyle: TextStyle
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(tabButtonsHeight),
    ) {
        val buttonWidth = maxWidth / 2

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondaryContainer),
        ) {
            TabButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(buttonWidth),
                onClick = { navActions.navigateToChargingAlarm() },
                isActive = TodoDestinations.BATTERY_ROUTE == currentRoute,
            ) {
                Text(
                    text = "Charging alarm",
                    style = buttonTextStyle,
                )
            }

            TabButton(
                modifier = Modifier
                    .fillMaxSize()
                    .width(buttonWidth),
                onClick = { navActions.navigateToAppListDetail() },
                isActive = TodoDestinations.APP_LISTS_ROUTE == currentRoute,
            ) {
                Text(
                    text = "App killer",
                    style = buttonTextStyle,
                )
            }
        }
    }
}

@Composable
private fun TabButton(
    modifier: Modifier,
    onClick: () -> Unit,
    isActive: Boolean,
    content: @Composable (RowScope.() -> Unit),
) {
    if (isActive)
        Button(
            modifier = modifier,
            onClick = onClick,
            content = content,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp),
            shape = androidx.compose.ui.graphics.RectangleShape,
        )
    else
        TextButton(
            modifier = modifier,
            onClick = onClick,
            content = content,
        )
}