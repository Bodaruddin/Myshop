package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ShopProfile
import com.example.ui.components.GlassInputField
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassChip
import com.example.ui.theme.CoralFlame
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.NeonCyan

@Composable
fun SettingsScreen(
    profile: ShopProfile?,
    currentDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onSaveProfile: (ShopProfile) -> Unit,
    onExportBackup: ((String) -> Unit) -> Unit,
    onImportBackup: (String) -> Unit,
    onResetData: () -> Unit
) {
    val context = LocalContext.current
    val effective = profile ?: ShopProfile()

    var shopName by remember(profile) { mutableStateOf(effective.shopName) }
    var tagline by remember(profile) { mutableStateOf(effective.tagline) }
    var address by remember(profile) { mutableStateOf(effective.address) }
    var phoneNumber by remember(profile) { mutableStateOf(effective.phoneNumber) }
    var email by remember(profile) { mutableStateOf(effective.email) }
    var gstin by remember(profile) { mutableStateOf(effective.gstin) }
    var currencySymbol by remember(profile) { mutableStateOf(effective.currencySymbol) }
    var invoicePrefix by remember(profile) { mutableStateOf(effective.invoicePrefix) }
    var invoiceFooterNote by remember(profile) { mutableStateOf(effective.invoiceFooterNote) }

    var showResetDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var jsonImportText by remember { mutableStateOf("") }
    var showBackupResultDialog by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // 100% Offline Database Guarantee Banner
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentGlow = EmeraldMint,
                borderColor = EmeraldMint.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(EmeraldMint.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Offline Mode",
                            tint = EmeraldMint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "100% Local SQLite Database",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "All products, sales, and stock reside exclusively on this device. Zero cloud dependency.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Theme Mode Section
        item {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "App Theme & Appearance",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LiquidGlassChip(
                            text = "Liquid Dark",
                            selected = currentDarkTheme,
                            onClick = { onToggleDarkTheme(true) },
                            icon = Icons.Default.DarkMode,
                            modifier = Modifier.weight(1f)
                        )
                        LiquidGlassChip(
                            text = "Crystal Light",
                            selected = !currentDarkTheme,
                            onClick = { onToggleDarkTheme(false) },
                            icon = Icons.Default.LightMode,
                            accentColor = ElectricBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Shop Profile & Tax Details
        item {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Store, contentDescription = null, tint = NeonCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Shop & Invoice Profile",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    GlassInputField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = "Shop Name",
                        placeholder = "VoltTech Electronics"
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    GlassInputField(
                        value = tagline,
                        onValueChange = { tagline = it },
                        label = "Tagline / Slogan",
                        placeholder = "Premium Gadgets Hub"
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    GlassInputField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Address",
                        placeholder = "Shop 12, Techno Arcade, MG Road..."
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassInputField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = "Phone Number",
                            placeholder = "+91 98765 43210",
                            modifier = Modifier.weight(1f)
                        )
                        GlassInputField(
                            value = gstin,
                            onValueChange = { gstin = it },
                            label = "GSTIN / Tax ID",
                            placeholder = "29ABCDE1234F1Z5",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassInputField(
                            value = currencySymbol,
                            onValueChange = { currencySymbol = it },
                            label = "Currency Symbol",
                            placeholder = "₹",
                            modifier = Modifier.weight(1f)
                        )
                        GlassInputField(
                            value = invoicePrefix,
                            onValueChange = { invoicePrefix = it },
                            label = "Invoice Prefix",
                            placeholder = "INV-",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    GlassInputField(
                        value = invoiceFooterNote,
                        onValueChange = { invoiceFooterNote = it },
                        label = "Invoice Terms & Footer Note",
                        placeholder = "Warranty & return policy...",
                        singleLine = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LiquidGlassButton(
                        text = "Save Shop Settings",
                        icon = Icons.Default.Save,
                        onClick = {
                            onSaveProfile(
                                effective.copy(
                                    shopName = shopName,
                                    tagline = tagline,
                                    address = address,
                                    phoneNumber = phoneNumber,
                                    email = email,
                                    gstin = gstin,
                                    currencySymbol = currencySymbol,
                                    invoicePrefix = invoicePrefix,
                                    invoiceFooterNote = invoiceFooterNote,
                                    themeMode = if (currentDarkTheme) "DARK" else "LIGHT"
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Backup & Restore Database Section
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentGlow = NeonCyan
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = NeonCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Offline Database Backup & Restore",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Export the entire database (products, sales, categories, stock transactions) as JSON. Restore or share it anytime.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LiquidGlassButton(
                            text = "Export JSON Backup",
                            icon = Icons.Default.Backup,
                            onClick = {
                                onExportBackup { json ->
                                    showBackupResultDialog = json
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        LiquidGlassButton(
                            text = "Restore / Import",
                            icon = Icons.Default.Restore,
                            onClick = { showImportDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CoralFlame.copy(alpha = 0.1f))
                            .border(1.dp, CoralFlame.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable { showResetDialog = true }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = CoralFlame, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reset to Sample Electronics Inventory",
                                color = CoralFlame,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset to Sample Data", fontWeight = FontWeight.Bold) },
            text = { Text("This will reset your database and restore the realistic starter electronics catalog and sample sales. Continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetData()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = CoralFlame, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Backup Result Dialog (Copy or Share JSON)
    if (showBackupResultDialog != null) {
        val json = showBackupResultDialog!!
        AlertDialog(
            onDismissRequest = { showBackupResultDialog = null },
            title = { Text("Backup Generated", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Your complete shop database has been exported to JSON (${json.length} characters).")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = json.take(150) + "...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, json)
                            putExtra(Intent.EXTRA_SUBJECT, "ElectroGlass_Database_Backup.json")
                            type = "application/json"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share JSON Backup via"))
                        showBackupResultDialog = null
                    }
                ) {
                    Text("Share / Save", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("ElectroGlass Backup", json))
                        showBackupResultDialog = null
                    }
                ) {
                    Text("Copy to Clipboard")
                }
            }
        )
    }

    // Import JSON Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Database JSON", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Paste your exported JSON database string below:")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = jsonImportText,
                        onValueChange = { jsonImportText = it },
                        placeholder = { Text("{\"app\": \"ElectroGlass\", ...}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (jsonImportText.isNotBlank()) {
                            onImportBackup(jsonImportText)
                            showImportDialog = false
                            jsonImportText = ""
                        }
                    }
                ) {
                    Text("Import & Restore", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
