package com.chrissytopher.source

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.border 

@Composable
fun MoreScreen() {
    val kvault = LocalKVault.current
    val json = LocalJson.current
    val sourceDataState = LocalSourceData.current
    val navHost = LocalNavHost.current

    Column {
        Box (onClick = { navHost?.navigate(NavScreen.GPA.name)}) {
            Row ( modifier = Modifier
                    .padding(25.dp)
                    .border(2.dp, SolidColor(Color.Black),shape = RoundedCornerShape(15.dp))
                    
                ) {
                Text("GPA Calculator", modifier = Modifier.padding(10.dp))
                Spacer( modifier = Modifier.weight(1f) )
                Icon(Icons.Outlined.ChevronRight, contentDescription = "right arrow", modifier = Modifier.padding(10.dp))
            }
            
        }
    }
}