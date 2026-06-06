package com.abk.myip.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.abk.myip.AbkMyIp
import com.abk.myip.domain.IpInfo
import com.abk.myip.domain.StaticMapUrl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    IpScreen()
                }
            }
        }
    }
}

@Composable
private fun IpScreen() {
    var info by remember { mutableStateOf<IpInfo?>(null) }
    var map by remember { mutableStateOf<StaticMapUrl?>(null) }

    LaunchedEffect(Unit) {
        val app = AbkMyIp()
        val fetched = app.getMyIpInfo()
        info = fetched
        map = app.buildStaticMapUrl(fetched.location)
    }

    Column(modifier = Modifier.padding(24.dp)) {
        val current = info
        if (current == null) {
            Text("Looking up your IP…", style = MaterialTheme.typography.bodyLarge)
        } else {
            Text("IP: ${current.ip}", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("${current.city}, ${current.region}")
            Text("${current.country} (${current.countryCode})")
            Text("Timezone: ${current.timezone}")
            Text("Location: ${current.location.latitude}, ${current.location.longitude}")
            Spacer(Modifier.height(16.dp))
            map?.let { url ->
                AsyncImage(model = url.value, contentDescription = "map of your location")
            }
        }
    }
}
