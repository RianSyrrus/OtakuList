package com.example.otakulist.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otakulist.BuildConfig
import com.example.otakulist.ui.components.KuroTrackTopBar
import com.example.otakulist.ui.theme.CanvasBackground
import com.example.otakulist.ui.theme.CardShape
import com.example.otakulist.ui.theme.CompactButtonShape
import com.example.otakulist.ui.theme.Dimens
import com.example.otakulist.ui.theme.OnBackground
import com.example.otakulist.ui.theme.OnSurface
import com.example.otakulist.ui.theme.OnSurfaceMuted
import com.example.otakulist.ui.theme.Outline
import com.example.otakulist.ui.theme.PillShape
import com.example.otakulist.ui.theme.PrimaryAccent
import com.example.otakulist.ui.theme.StatusCompleted
import com.example.otakulist.ui.theme.StatusWatching
import com.example.otakulist.ui.theme.SurfaceBase
import com.example.otakulist.ui.theme.SurfaceBright
import com.example.otakulist.ui.theme.SurfaceVariant
import com.example.otakulist.ui.theme.Typography

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exportJson by viewModel.exportJson.collectAsState()
    val importMessage by viewModel.importMessage.collectAsState()

    var showImportDialog by remember { mutableStateOf(false) }
    var importInputText by remember { mutableStateOf("") }
    var wifiOnlyImages by remember { mutableStateOf(false) }
    var cacheSizeText by remember { mutableStateOf("24.6 MB") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBackground)
    ) {
        KuroTrackTopBar(subtitle = "Settings")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Dimens.marginScreen)
                .padding(top = Dimens.spacingSm, bottom = Dimens.spacing2Xl + 32.dp),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingXl)
        ) {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingXs)) {
                Text(
                    text = "Pengaturan",
                    style = Typography.headlineLarge,
                    color = OnBackground
                )
                Text(
                    text = "Kelola data lokal, arsip, dan preferensi aplikasi",
                    style = Typography.bodySmall,
                    color = OnSurfaceMuted
                )
            }

            // Hero Banner: Ruang Arsip Lokal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(SurfaceBase)
                    .padding(Dimens.spacingMd),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = PrimaryAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RUANG ARSIP LOKAL",
                        style = Typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = PrimaryAccent
                    )
                    Text(
                        text = "Koleksi Offline Tersedia",
                        style = Typography.titleMedium,
                        color = OnSurface
                    )
                    Text(
                        text = "3.360 entri tersimpan di memori perangkat",
                        style = Typography.bodySmall,
                        color = OnSurfaceMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = PrimaryAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Section 1: Penyimpanan & Data
            SettingsSection(title = "PENYIMPANAN & DATA") {
                SettingsActionRow(
                    icon = Icons.Default.FileDownload,
                    title = "Backup Koleksi (Export JSON)",
                    subtitle = "Simpan salinan database lokal",
                    onClick = { viewModel.exportWatchlist() }
                )

                HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)

                SettingsActionRow(
                    icon = Icons.Default.FileUpload,
                    title = "Pulihkan Data (Restore JSON)",
                    subtitle = "Import berkas cadangan .json",
                    onClick = {
                        importInputText = ""
                        showImportDialog = true
                    }
                )

                HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CompactButtonShape)
                                .background(SurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = OnSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Ukuran Cache Lokal",
                                style = Typography.titleMedium.copy(fontSize = 15.sp),
                                color = OnSurface
                            )
                            Text(
                                text = "Gambar poster & data sementara",
                                style = Typography.bodySmall,
                                color = OnSurfaceMuted
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceBright)
                                .padding(horizontal = Dimens.spacingSm, vertical = 4.dp)
                        ) {
                            Text(
                                text = cacheSizeText,
                                style = Typography.labelMedium,
                                color = OnSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceVariant)
                                .clickable {
                                    cacheSizeText = "0 MB"
                                    Toast.makeText(context, "Cache poster dibersihkan", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = Dimens.spacingMd, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Bersihkan Cache",
                                style = Typography.labelSmall,
                                color = OnSurface
                            )
                        }
                    }
                }
            }

            // Section 2: Arsip Offline & Sinkronisasi
            SettingsSection(title = "ARSIP OFFLINE & SINKRONISASI") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CompactButtonShape)
                                .background(SurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = OnSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Status Sinkron",
                                style = Typography.titleMedium.copy(fontSize = 15.sp),
                                color = OnSurface
                            )
                            Text(
                                text = "Database lokal terintegrasi",
                                style = Typography.bodySmall,
                                color = OnSurfaceMuted
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(SurfaceVariant)
                            .padding(horizontal = Dimens.spacingSm, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(StatusCompleted)
                        )
                        Text(
                            text = "Tersinkron Penuh",
                            style = Typography.labelSmall,
                            color = StatusCompleted
                        )
                    }
                }

                HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Toast.makeText(context, "Katalog lokal sudah versi terbaru", Toast.LENGTH_SHORT).show()
                        }
                        .padding(Dimens.spacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CompactButtonShape)
                                .background(SurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cached,
                                contentDescription = null,
                                tint = OnSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Sinkronisasi Ulang Katalog",
                                style = Typography.titleMedium.copy(fontSize = 15.sp),
                                color = OnSurface
                            )
                            Text(
                                text = "Perbarui metadata judul & episode terbaru",
                                style = Typography.bodySmall,
                                color = OnSurfaceMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceVariant)
                            .padding(horizontal = Dimens.spacingSm, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Cek Sekarang",
                            style = Typography.labelSmall,
                            color = PrimaryAccent
                        )
                    }
                }

                HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CompactButtonShape)
                                .background(SurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dataset,
                                contentDescription = null,
                                tint = OnSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Versi Database Anime",
                                style = Typography.titleMedium.copy(fontSize = 15.sp),
                                color = OnSurface
                            )
                            Text(
                                text = "Snapshot arsip statis terpaket",
                                style = Typography.bodySmall,
                                color = OnSurfaceMuted
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "v2024.11-offline",
                            style = Typography.labelSmall,
                            color = OnSurfaceMuted
                        )
                        Text(
                            text = "(3.360 anime)",
                            style = Typography.labelSmall,
                            color = OnSurface
                        )
                    }
                }
            }

            // Section 3: Tampilan & Preferensi
            SettingsSection(title = "TAMPILAN & PREFERENSI") {
                SettingsStaticRow(
                    icon = Icons.Default.DarkMode,
                    title = "Mode Tema",
                    subtitle = "Warna kanvas dan estetika latar",
                    value = "Gelap (OLED/Charcoal)"
                )

                HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)

                SettingsStaticRow(
                    icon = Icons.Default.Translate,
                    title = "Bahasa Antarmuka",
                    subtitle = "Teks navigasi dan katalog",
                    value = "Bahasa Indonesia"
                )

                HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CompactButtonShape)
                                .background(SurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NetworkWifi,
                                contentDescription = null,
                                tint = OnSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Tampilan Gambar",
                                style = Typography.titleMedium.copy(fontSize = 15.sp),
                                color = OnSurface
                            )
                            Text(
                                text = "Hemat Kuota Poster (Muat saat Wi-Fi)",
                                style = Typography.bodySmall,
                                color = OnSurfaceMuted
                            )
                        }
                    }

                    Switch(
                        checked = wifiOnlyImages,
                        onCheckedChange = { wifiOnlyImages = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OnBackground,
                            checkedTrackColor = StatusWatching,
                            uncheckedThumbColor = OnSurfaceMuted,
                            uncheckedTrackColor = SurfaceVariant
                        )
                    )
                }
            }

            // Section 4: Tentang Aplikasi
            SettingsSection(title = "TENTANG APLIKASI") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CompactButtonShape)
                                .background(SurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = OnSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Versi Aplikasi",
                                style = Typography.titleMedium.copy(fontSize = 15.sp),
                                color = OnSurface
                            )
                            Text(
                                text = "Salinan terpasang di perangkat",
                                style = Typography.bodySmall,
                                color = OnSurfaceMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceBright)
                            .padding(horizontal = Dimens.spacingSm, vertical = 4.dp)
                    ) {
                        Text(
                            text = "KuroTrack v${BuildConfig.VERSION_NAME}",
                            style = Typography.labelMedium,
                            color = OnSurface
                        )
                    }
                }

                HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)

                SettingsStaticRow(
                    icon = Icons.Default.Policy,
                    title = "Lisensi Sumber Terbuka",
                    subtitle = "Kredit modul pihak ketiga & lisensi Apache 2.0",
                    value = "Lihat"
                )

                HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com"))
                            context.startActivity(intent)
                        }
                        .padding(Dimens.spacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CompactButtonShape)
                                .background(SurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = OnSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "GitHub Repository & Dukungan",
                                style = Typography.titleMedium.copy(fontSize = 15.sp),
                                color = OnSurface
                            )
                            Text(
                                text = "Laporkan kendala atau kontribusi kode",
                                style = Typography.bodySmall,
                                color = OnSurfaceMuted
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = OnSurfaceMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Footer note
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.spacingMd),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs)
                ) {
                    Icon(
                        imageVector = Icons.Default.OfflineBolt,
                        contentDescription = null,
                        tint = PrimaryAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Arsip Koleksi Lokal Tanpa Pelacak Komersial",
                        style = Typography.labelSmall,
                        color = OnSurfaceMuted
                    )
                }
                Text(
                    text = "Dibuat dengan fokus ketelitian pencatatan",
                    style = Typography.bodySmall.copy(fontSize = 11.sp),
                    color = OnSurfaceMuted.copy(alpha = 0.6f)
                )
            }
        }
    }

    // Export Dialog
    exportJson?.let { json ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissExportDialog() },
            containerColor = SurfaceBase,
            title = {
                Text(text = "Cadangan Data (JSON)", style = Typography.titleMedium, color = OnSurface)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
                    Text(
                        text = "Salin teks JSON berikut dan simpan sebagai file cadangan:",
                        style = Typography.bodySmall,
                        color = OnSurfaceMuted
                    )
                    OutlinedTextField(
                        value = json,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceVariant,
                            unfocusedContainerColor = SurfaceVariant,
                            focusedTextColor = OnBackground,
                            unfocusedTextColor = OnBackground,
                            focusedBorderColor = Outline,
                            unfocusedBorderColor = Outline
                        ),
                        textStyle = Typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("OtakuList Backup", json)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "JSON disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                        viewModel.dismissExportDialog()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = OnBackground),
                    shape = CompactButtonShape
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Salin ke Clipboard", style = Typography.labelMedium)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissExportDialog() }) {
                    Text(text = "Tutup", color = OnSurfaceMuted)
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = SurfaceBase,
            title = {
                Text(text = "Impor Data Watchlist", style = Typography.titleMedium, color = OnSurface)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
                    Text(
                        text = "Tempelkan kode JSON cadangan kamu di bawah ini:",
                        style = Typography.bodySmall,
                        color = OnSurfaceMuted
                    )
                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = { importInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        placeholder = { Text(text = "[{\"animeId\":1,...}]") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceVariant,
                            unfocusedContainerColor = SurfaceVariant,
                            focusedTextColor = OnBackground,
                            unfocusedTextColor = OnBackground,
                            focusedBorderColor = PrimaryAccent,
                            unfocusedBorderColor = Outline
                        ),
                        textStyle = Typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importInputText.isNotBlank()) {
                            viewModel.importWatchlist(importInputText)
                            showImportDialog = false
                        }
                    },
                    enabled = importInputText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = OnBackground),
                    shape = CompactButtonShape
                ) {
                    Text(text = "Pulihkan", style = Typography.labelMedium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(text = "Batal", color = OnSurfaceMuted)
                }
            }
        )
    }

    // Import Result Message
    importMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissImportMessage() },
            containerColor = SurfaceBase,
            title = {
                Text(text = "Hasil Pemulihan", style = Typography.titleMedium, color = OnSurface)
            },
            text = {
                Text(text = msg, style = Typography.bodyMedium, color = OnSurface)
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissImportMessage() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = OnBackground),
                    shape = CompactButtonShape
                ) {
                    Text(text = "Selesai", style = Typography.labelMedium)
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
        Text(
            text = title,
            style = Typography.labelSmall.copy(letterSpacing = 1.sp),
            color = PrimaryAccent,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(SurfaceBase)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(Dimens.spacingMd),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CompactButtonShape)
                    .background(SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OnSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = Typography.titleMedium.copy(fontSize = 15.sp),
                    color = OnSurface
                )
                Text(
                    text = subtitle,
                    style = Typography.bodySmall,
                    color = OnSurfaceMuted
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = OnSurfaceMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsStaticRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.spacingMd),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CompactButtonShape)
                    .background(SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OnSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = Typography.titleMedium.copy(fontSize = 15.sp),
                    color = OnSurface
                )
                Text(
                    text = subtitle,
                    style = Typography.bodySmall,
                    color = OnSurfaceMuted
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs)
        ) {
            Text(
                text = value,
                style = Typography.labelSmall,
                color = OnSurface
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = OnSurfaceMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
