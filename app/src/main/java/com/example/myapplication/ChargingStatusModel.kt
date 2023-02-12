package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp

class ChargingStatusModel(
    val isCharging: Boolean = false,
    val levelPercentage: Int = 0,
)

class ChargingStatusModelProvider : PreviewParameterProvider<ChargingStatusModel> {
    override val values = sequenceOf(
        ChargingStatusModel(true, 10),
        ChargingStatusModel(false, 50),
    )
}

@Preview(showBackground = true)
@Composable
fun ChargingStatus(
    @PreviewParameter(ChargingStatusModelProvider::class) chargingStatus: ChargingStatusModel,
) {
    Row(
        modifier = Modifier
            .padding(vertical = 5.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.wrapContentHeight()
        )

        ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
            Icon(
                painter = painterResource(R.drawable.flash),
                contentDescription = null,
                tint =
                if (chargingStatus.isCharging)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.secondary,
            )

            Text(
                text = "${chargingStatus.levelPercentage}%",
                modifier = Modifier.padding(horizontal = 5.dp),
                textAlign = TextAlign.Center,
            )

            Text(
                if (chargingStatus.isCharging)
                    "(Charging)"
                else
                    "(Not Charging)",
                modifier = Modifier.padding(horizontal = 5.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}
