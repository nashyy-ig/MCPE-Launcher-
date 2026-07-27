package com.example.mcpelauncher

import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.mcpelauncher.ui.theme.BlockLauncherTheme
import com.example.mcpelauncher.ui.theme.DiamondCyan
import com.example.mcpelauncher.ui.theme.GoldOre
import com.example.mcpelauncher.ui.theme.GrassGreen
import com.example.mcpelauncher.ui.theme.Obsidian
import com.example.mcpelauncher.ui.theme.DeepSlate
import java.io.File
import java.util.UUID

class MainActivity : ComponentActivity() {

    private lateinit var store: VersionStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = VersionStore(this)

        setContent {
            BlockLauncherTheme {
                LauncherScreen(store = store, activity = this)
            }
        }
    }
}

private const val NYL_UI_URL = "https://msha.ke/cyph3rw0rks.org"
private const val NYL_CLIENT_URL = "https://msha.ke/cyph3rw0rks_projects"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherScreen(store: VersionStore, activity: ComponentActivity) {
    var versions by remember { mutableStateOf(store.all()) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    val pm = activity.packageManager

    fun refresh() { versions = store.all() }
    fun openUrl(url: String) { activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }

    val pickApk = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        addVersionFromUri(activity, store, uri)
        refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NYL UI", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("NYL UI") },
                                leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    openUrl(NYL_UI_URL)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("About") },
                                leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    showAbout = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = GrassGreen
                )
            )
        },
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { pickApk.launch(arrayOf("application/vnd.android.package-archive")) },
                containerColor = GrassGreen,
                contentColor = Color.Black,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("ADD VERSION", fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(20.dp)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(DeepSlate, Obsidian))
                )
        ) {
            if (versions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Extension, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(12.dp))
                        Text("No versions yet.\nTap ADD VERSION to import an MCPE APK\nyou already have.", textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(versions, key = { it.id }) { version ->
                        val isInstalled = remember(version, versions) { isPackageInstalled(pm, version.packageName) }
                        val installedVersionName = remember(version, versions) { installedVersionName(pm, version.packageName) }
                        val isActive = isInstalled && installedVersionName == version.versionName

                        VersionCard(
                            version = version,
                            isActive = isActive,
                            isInstalled = isInstalled,
                            onLaunch = {
                                pm.getLaunchIntentForPackage(version.packageName)?.let { activity.startActivity(it) }
                            },
                            onSwitch = {
                                if (isInstalled && !isActive) {
                                    val uninstall = Intent(Intent.ACTION_DELETE).apply {
                                        data = Uri.parse("package:${version.packageName}")
                                    }
                                    activity.startActivity(uninstall)
                                } else {
                                    installApk(activity, store, version)
                                }
                            },
                            onRemove = {
                                store.remove(version)
                                refresh()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("Close") }
            },
            title = { Text("About", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("NYL UI is brought to you by CYPHERW0RKS.")
                    Spacer(Modifier.height(8.dp))
                    Text("Pillars of this project:", fontWeight = FontWeight.Bold, color = GoldOre)
                    Text("• Asa_Yuki")
                    Text("• AM4IM0N")
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(NYL_CLIENT_URL))) }) {
                        Icon(Icons.Filled.Link, contentDescription = null, tint = DiamondCyan)
                        Spacer(Modifier.width(6.dp))
                        Text("Nyl Client", color = DiamondCyan)
                    }
                }
            }
        )
    }
}

@Composable
fun VersionCard(
    version: McpeVersion,
    isActive: Boolean,
    isInstalled: Boolean,
    onLaunch: () -> Unit,
    onSwitch: () -> Unit,
    onRemove: () -> Unit,
) {
    ElevatedCard(
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.surface, Obsidian)
                ),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Brush.linearGradient(
                                if (isActive) listOf(GrassGreen, DiamondCyan) else listOf(DiamondCyan, GoldOre)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ViewInAr, contentDescription = null, tint = Color.Black)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(version.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("v${version.versionName}", style = MaterialTheme.typography.bodySmall, color = GoldOre)
                }
                if (isActive) {
                    AssistChip(
                        onClick = {},
                        label = { Text("ACTIVE") },
                        shape = RoundedCornerShape(50),
                        colors = AssistChipDefaults.assistChipColors(labelColor = GrassGreen)
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (isActive) {
                    Button(
                        onClick = onLaunch,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GrassGreen)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("PLAY")
                    }
                } else {
                    Button(
                        onClick = onSwitch,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DiamondCyan)
                    ) {
                        Icon(Icons.Filled.SwapHoriz, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(if (isInstalled) "REMOVE OTHER BUILD" else "SWITCH TO THIS")
                    }
                }
                OutlinedButton(onClick = onRemove, shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                }
            }
        }
    }
}

private fun isPackageInstalled(pm: PackageManager, packageName: String): Boolean =
    try { pm.getPackageInfo(packageName, 0); true } catch (e: PackageManager.NameNotFoundException) { false }

private fun installedVersionName(pm: PackageManager, packageName: String): String? =
    try { pm.getPackageInfo(packageName, 0).versionName } catch (e: PackageManager.NameNotFoundException) { null }

private fun addVersionFromUri(activity: ComponentActivity, store: VersionStore, uri: Uri) {
    val fileName = "mcpe_${UUID.randomUUID()}.apk"
    val dest = File(store.apkDir(), fileName)
    activity.contentResolver.openInputStream(uri)?.use { input ->
        dest.outputStream().use { output -> input.copyTo(output) }
    }

    val pm = activity.packageManager
    val info = pm.getPackageArchiveInfo(dest.absolutePath, 0)
    if (info == null) {
        dest.delete()
        return
    }
    val appInfo = info.applicationInfo
    appInfo?.sourceDir = dest.absolutePath
    appInfo?.publicSourceDir = dest.absolutePath
    val label = appInfo?.let { pm.getApplicationLabel(it).toString() } ?: "Minecraft"

    store.add(
        McpeVersion(
            id = UUID.randomUUID().toString(),
            label = label,
            versionName = info.versionName ?: "unknown",
            packageName = info.packageName,
            apkFileName = fileName,
        )
    )
}

private fun installApk(activity: ComponentActivity, store: VersionStore, version: McpeVersion) {
    val file = store.apkFile(version)
    val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    activity.startActivity(intent)
}
