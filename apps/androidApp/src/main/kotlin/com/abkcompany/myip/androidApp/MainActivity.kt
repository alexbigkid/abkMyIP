package com.abkcompany.myip.androidApp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.abk.myip.AbkMyIp
import com.abk.myip.domain.GeoLocation
import com.abk.myip.domain.IpInfo
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay

private const val PREFS_NAME = "settings"
private const val KEY_APPEARANCE = "appearance"

enum class Appearance(val icon: ImageVector, val label: String) {
    SYSTEM(Icons.Outlined.SettingsBrightness, "System"),
    LIGHT(Icons.Filled.LightMode, "Light"),
    DARK(Icons.Filled.DarkMode, "Dark");

    companion object {
        fun fromStored(value: String?): Appearance =
            entries.firstOrNull { it.name == value } ?: DARK
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", Context.MODE_PRIVATE),
        )
        Configuration.getInstance().userAgentValue = packageName

        setContent {
            val prefs = remember { getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
            var appearance by remember {
                mutableStateOf(Appearance.fromStored(prefs.getString(KEY_APPEARANCE, null)))
            }
            val darkTheme = when (appearance) {
                Appearance.SYSTEM -> isSystemInDarkTheme()
                Appearance.LIGHT -> false
                Appearance.DARK -> true
            }
            MaterialTheme(colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppRoot(
                        appearance = appearance,
                        onAppearanceChange = {
                            appearance = it
                            prefs.edit().putString(KEY_APPEARANCE, it.name).apply()
                        },
                        darkTheme = darkTheme,
                    )
                }
            }
        }
    }
}

private data class UiState(
    val info: IpInfo? = null,
    val error: String? = null,
    val loading: Boolean = true,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRoot(
    appearance: Appearance,
    onAppearanceChange: (Appearance) -> Unit,
    darkTheme: Boolean,
) {
    var state by remember { mutableStateOf(UiState()) }
    var refreshTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshTick) {
        state = state.copy(loading = true, error = null)
        state = try {
            UiState(info = AbkMyIp().getMyIpInfo(), loading = false)
        } catch (t: Throwable) {
            state.copy(loading = false, error = t.message ?: "Failed to load IP info")
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My IP") },
                actions = { AppearanceMenu(appearance, onAppearanceChange) },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.loading,
            onRefresh = { refreshTick++ },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when {
                state.error != null -> ErrorContent(state.error!!) { refreshTick++ }
                state.info != null -> Content(state.info!!, darkTheme = darkTheme)
                state.loading -> LoadingContent()
            }
        }
    }
}

@Composable
private fun AppearanceMenu(current: Appearance, onSelect: (Appearance) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(current.icon, contentDescription = "Theme: ${current.label}")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        Appearance.entries.forEach { option ->
            DropdownMenuItem(
                text = { Text(option.label) },
                leadingIcon = { Icon(option.icon, contentDescription = null) },
                onClick = {
                    onSelect(option)
                    expanded = false
                },
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Filled.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.height(48.dp),
            )
            Text("Couldn't fetch your IP info", style = MaterialTheme.typography.titleMedium)
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

private data class LabeledRow(val label: String, val value: String)

@Composable
private fun Content(info: IpInfo, darkTheme: Boolean) {
    val landscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    if (landscape) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 8.dp)) {
            InfoPanel(
                info = info,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            OsmMap(
                location = info.location,
                darkTheme = darkTheme,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp)),
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item { InfoPanel(info) }
            item {
                OsmMap(
                    location = info.location,
                    darkTheme = darkTheme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
            }
        }
    }
}

@Composable
private fun InfoPanel(info: IpInfo, modifier: Modifier = Modifier) {
    val rows = buildList {
        add(LabeledRow("Timezone", info.timezone))
        info.org?.takeIf { it.isNotEmpty() }?.let { add(LabeledRow("ISP", it)) }
        add(LabeledRow("Coordinates", "%.4f, %.4f".format(info.location.latitude, info.location.longitude)))
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(
            text = info.ip,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            maxLines = 1,
        )
        Text(
            text = "${info.city}, ${info.region} · ${info.countryCode}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider()
        rows.forEach { InfoRow(it.label, it.value) }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            value,
            modifier = Modifier.weight(1f).padding(start = 16.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun OsmMap(location: GeoLocation, darkTheme: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.0)
        }
    }
    AndroidView(
        factory = { mapView },
        update = { view ->
            view.overlayManager.tilesOverlay.setColorFilter(
                if (darkTheme) TilesOverlay.INVERT_COLORS else null,
            )
            val point = GeoPoint(location.latitude, location.longitude)
            view.controller.setCenter(point)
            view.overlays.clear()
            view.overlays.add(
                Marker(view).apply {
                    position = point
                    title = "My IP"
                },
            )
            view.invalidate()
        },
        modifier = modifier,
    )
}
