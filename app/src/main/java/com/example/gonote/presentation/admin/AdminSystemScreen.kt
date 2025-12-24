package com.example.gonote.presentation.admin

import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gonote.ui.theme.AccentBlue
import java.io.File
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSystemScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    
    // Get system info
    val appVersion = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
    
    val appVersionCode = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }
        } catch (e: Exception) {
            "1"
        }
    }

    val androidVersion = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
    val deviceBrand = Build.BRAND

    // Database size
    val databaseSize = remember {
        try {
            val dbFile = context.getDatabasePath("gonote_database")
            if (dbFile.exists()) {
                formatFileSize(dbFile.length())
            } else {
                "N/A"
            }
        } catch (e: Exception) {
            "N/A"
        }
    }

    // Photos folder size
    val photosFolderSize = remember {
        try {
            val photosDir = File(context.filesDir, "photos")
            if (photosDir.exists()) {
                val totalSize = photosDir.walkTopDown()
                    .filter { it.isFile }
                    .map { it.length() }
                    .sum()
                formatFileSize(totalSize)
            } else {
                "0 B"
            }
        } catch (e: Exception) {
            "N/A"
        }
    }

    // Total app storage
    val totalStorage = remember {
        try {
            val filesSize = context.filesDir.walkTopDown()
                .filter { it.isFile }
                .map { it.length() }
                .sum()
            val dbSize = context.getDatabasePath("gonote_database").length()
            formatFileSize(filesSize + dbSize)
        } catch (e: Exception) {
            "N/A"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "System Information",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Info Section
            SectionHeader("Application")
            
            InfoCard {
                InfoRow(
                    icon = Icons.Default.Apps,
                    label = "App Name",
                    value = "GoNote"
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                InfoRow(
                    icon = Icons.Default.Numbers,
                    label = "Version",
                    value = "$appVersion ($appVersionCode)"
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                InfoRow(
                    icon = Icons.Default.Code,
                    label = "Package",
                    value = context.packageName
                )
            }

            // Device Info Section
            SectionHeader("Device")
            
            InfoCard {
                InfoRow(
                    icon = Icons.Default.Android,
                    label = "Android Version",
                    value = androidVersion
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                InfoRow(
                    icon = Icons.Default.PhoneAndroid,
                    label = "Device Model",
                    value = deviceModel
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                InfoRow(
                    icon = Icons.Default.Business,
                    label = "Brand",
                    value = deviceBrand.replaceFirstChar { it.uppercase() }
                )
            }

            // Storage Info Section
            SectionHeader("Storage")
            
            InfoCard {
                InfoRow(
                    icon = Icons.Default.Storage,
                    label = "Database Size",
                    value = databaseSize
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                InfoRow(
                    icon = Icons.Default.Photo,
                    label = "Photos Storage",
                    value = photosFolderSize
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                InfoRow(
                    icon = Icons.Default.Folder,
                    label = "Total App Storage",
                    value = totalStorage
                )
            }

            // Build Info
            SectionHeader("Build Info")
            
            InfoCard {
                InfoRow(
                    icon = Icons.Default.Build,
                    label = "Build Type",
                    value = if (isDebugBuild()) "Debug" else "Release"
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                InfoRow(
                    icon = Icons.Default.Architecture,
                    label = "Architecture",
                    value = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = AccentBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    val formatter = DecimalFormat("#,##0.##")
    return "${formatter.format(size / Math.pow(1024.0, digitGroups.toDouble()))} ${units[digitGroups]}"
}

private fun isDebugBuild(): Boolean {
    return try {
        Class.forName("com.example.gonote.BuildConfig")
            .getField("DEBUG")
            .getBoolean(null)
    } catch (e: Exception) {
        false
    }
}





















