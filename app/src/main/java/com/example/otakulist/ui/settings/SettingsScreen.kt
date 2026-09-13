package com.example.otakulist.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Storage
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otakulist.BuildConfig
import com.example.otakulist.data.local.AppThemeMode
import com.example.otakulist.ui.components.OtakuTopBar
import com.example.otakulist.ui.theme.AccentPresets
import com.example.otakulist.ui.theme.CanvasBackground
import com.example.otakulist.ui.theme.OtakuTheme
import com.example.otakulist.ui.theme.parseHexColor
import com.example.otakulist.ui.theme.CardShape
import com.example.otakulist.ui.theme.CompactButtonShape
import com.example.otakulist.ui.theme.Dimens
import com.example.otakulist.ui.theme.Error
import com.example.otakulist.ui.theme.OnBackground
import com.example.otakulist.ui.theme.OnSurface
import com.example.otakulist.ui.theme.OnSurfaceMuted
import com.example.otakulist.ui.theme.Outline
import com.example.otakulist.ui.theme.PillShape
import com.example.otakulist.ui.theme.PrimaryAccent
import com.example.otakulist.ui.theme.StatusCompleted
import com.example.otakulist.ui.theme.SurfaceBase
import com.example.otakulist.ui.theme.SurfaceContainer
import com.example.otakulist.ui.theme.SurfaceContainerHigh
import com.example.otakulist.ui.theme.SurfaceVariant
import com.example.otakulist.ui.theme.Typography

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAdultContentEnabled by viewModel.isAdultContentEnabled.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val accentColorHex by viewModel.accentColorHex.collectAsState()
    val regularWatchlistCount by viewModel.regularWatchlistCount.collectAsState()
    val adultWatchlistCount by viewModel.adultWatchlistCount.collectAsState()
    val exportJson by viewModel.exportJson.collectAsState()
    val importMessage by viewModel.importMessage.collectAsState()
    val actionFeedback by viewModel.actionFeedback.collectAsState()

    var showImportDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showCustomColorDialog by remember { mutableStateOf(false) }
    var importInputText by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    LaunchedEffect(actionFeedback) {
        actionFeedback?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.dismissFeedback()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OtakuTheme.colors.canvasBackground)
    ) {
        OtakuTopBar(subtitle = "Settings")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Dimens.marginScreen)
                .padding(top = Dimens.spacingSm, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingLg)
        ) {
            // Screen Header
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Pengaturan",
                    style = Typography.headlineLarge,
                    color = OtakuTheme.colors.onBackground
                )
                Text(
                    text = "Kelola preferensi konten, cadangan data, dan basis data offline",
                    style = Typography.bodySmall,
                    color = OtakuTheme.colors.onSurfaceMuted
                )
            }

            // Hero System Status Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(OtakuTheme.colors.surfaceBase)
                    .border(1.dp, OtakuTheme.colors.outline.copy(alpha = 0.25f), CardShape)
                    .padding(Dimens.spacingMd)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OtakuTheme.colors.surfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Storage,
                            contentDescription = null,
                            tint = OtakuTheme.colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(StatusCompleted)
                            )
                            Text(
                                text = "DATABASE OFFLINE LOKAL",
                                style = Typography.labelSmall.copy(letterSpacing = 0.8.sp, fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = StatusCompleted
                            )
                        }

                        Text(
                            text = if (isAdultContentEnabled) {
                                "3.360 Reguler · 1.637 Dewasa"
                            } else {
                                "3.360 Judul Anime Tersedia"
                            },
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                            color = OtakuTheme.colors.onSurface
                        )

                        val watchlistSummary = if (isAdultContentEnabled) {
                            "$regularWatchlistCount tersimpan (Reguler) · $adultWatchlistCount tersimpan (18+)"
                        } else {
                            "$regularWatchlistCount judul tersimpan di watchlist"
                        }
                        Text(
                            text = watchlistSummary,
                            style = Typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = OtakuTheme.colors.onSurfaceMuted
                        )
                    }
                }
            }

            // Seksi 0: Tampilan & Personalisasi
            SettingsSection(title = "Tampilan & Personalisasi") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacingMd),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
                ) {
                    // Mode Tampilan
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
                        Text(
                            text = "Mode Tampilan",
                            style = Typography.titleMedium.copy(fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold),
                            color = OtakuTheme.colors.onSurface
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(OtakuTheme.colors.surfaceContainer)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val modes = listOf(
                                Triple(AppThemeMode.DARK, "Gelap", Icons.Outlined.DarkMode),
                                Triple(AppThemeMode.LIGHT, "Terang", Icons.Outlined.LightMode),
                                Triple(AppThemeMode.SYSTEM, "Sistem", Icons.Outlined.BrightnessAuto)
                            )
                            modes.forEach { (mode, label, iconVector) ->
                                val isSelected = themeMode == mode
                                val animBgColor by animateColorAsState(
                                    targetValue = if (isSelected) OtakuTheme.colors.primary.copy(alpha = 0.16f) else Color.Transparent,
                                    label = "themeModeBg"
                                )
                                val animBorderColor by animateColorAsState(
                                    targetValue = if (isSelected) OtakuTheme.colors.primary else Color.Transparent,
                                    label = "themeModeBorder"
                                )
                                val animContentColor by animateColorAsState(
                                    targetValue = if (isSelected) OtakuTheme.colors.primary else OtakuTheme.colors.onSurfaceMuted,
                                    label = "themeModeContent"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(animBgColor)
                                        .border(
                                            width = if (isSelected) 1.2.dp else 0.dp,
                                            color = animBorderColor,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.setThemeMode(mode) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = iconVector,
                                            contentDescription = null,
                                            tint = animContentColor,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Text(
                                            text = label,
                                            style = Typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                fontSize = 12.5.sp
                                            ),
                                            color = animContentColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = OtakuTheme.colors.outline.copy(alpha = 0.15f))

                    // Warna Template / Aksen
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Warna Aksen",
                                style = Typography.titleMedium.copy(fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold),
                                color = OtakuTheme.colors.onSurface
                            )
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(OtakuTheme.colors.primary.copy(alpha = 0.12f))
                                    .clickable { showCustomColorDialog = true }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Palette,
                                    contentDescription = null,
                                    tint = OtakuTheme.colors.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Kustom HEX",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
                                    color = OtakuTheme.colors.primary
                                )
                            }
                        }

                        // Presets Swatches
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AccentPresets.forEach { preset ->
                                val isSelected = accentColorHex.equals(preset.hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(preset.color)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 0.dp,
                                            color = if (isSelected) OtakuTheme.colors.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.setAccentColorHex(preset.hex) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Terpilih",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Seksi 1: Preferensi Konten
            SettingsSection(title = "Preferensi Konten") {
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isAdultContentEnabled) {
                                        if (OtakuTheme.colors.isDark) Color(0xFF3E1F24) else Color(0xFFFFEBEE)
                                    } else {
                                        OtakuTheme.colors.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (isAdultContentEnabled) {
                                    if (OtakuTheme.colors.isDark) Color(0xFFFF6B6B) else Color(0xFFD32F2F)
                                } else {
                                    OtakuTheme.colors.onSurfaceVariant
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.padding(end = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Katalog Anime Dewasa (18+)",
                                style = Typography.titleMedium.copy(fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold),
                                color = OtakuTheme.colors.onSurface
                            )
                            Text(
                                text = if (isAdultContentEnabled) {
                                    "Aktif — Tab 18+ dapat diakses di Katalog dan Watchlist (1.637 judul)"
                                } else {
                                    "Nonaktif — Seluruh konten 18+ disembunyikan dari aplikasi"
                                },
                                style = Typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = OtakuTheme.colors.onSurfaceMuted
                            )
                        }
                    }

                    Switch(
                        checked = isAdultContentEnabled,
                        onCheckedChange = { viewModel.setAdultContentEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFE53935),
                            uncheckedThumbColor = OtakuTheme.colors.onSurfaceMuted,
                            uncheckedTrackColor = OtakuTheme.colors.surfaceVariant
                        )
                    )
                }
            }

            // Seksi 2: Manajemen Data & Cadangan
            SettingsSection(title = "Manajemen Data & Cadangan") {
                SettingsActionRow(
                    icon = Icons.Default.FileDownload,
                    title = "Cadangkan Data (Ekspor JSON)",
                    subtitle = "Simpan salinan seluruh entri watchlist ke format JSON",
                    onClick = { viewModel.exportWatchlist() }
                )

                HorizontalDivider(
                    color = OtakuTheme.colors.outline.copy(alpha = 0.15f),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(horizontal = Dimens.spacingMd)
                )

                SettingsActionRow(
                    icon = Icons.Default.FileUpload,
                    title = "Pulihkan Data (Impor JSON)",
                    subtitle = "Kembalikan entri watchlist dari salinan berkas cadangan JSON",
                    onClick = {
                        importInputText = ""
                        showImportDialog = true
                    }
                )

                HorizontalDivider(
                    color = OtakuTheme.colors.outline.copy(alpha = 0.15f),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(horizontal = Dimens.spacingMd)
                )

                SettingsActionRow(
                    icon = Icons.Default.DeleteSweep,
                    iconTint = Error,
                    title = "Kosongkan Watchlist",
                    titleColor = Error,
                    subtitle = "Hapus seluruh catatan tontonan tanpa mengubah arsip katalog master",
                    onClick = { showClearConfirmDialog = true }
                )
            }

            // Seksi 3: Spesifikasi Teknis & Integritas
            SettingsSection(title = "Informasi & Integritas Offline") {
                SettingsInfoRow(
                    icon = Icons.Default.Info,
                    title = "Versi Aplikasi",
                    value = "v${BuildConfig.VERSION_NAME}"
                )

                HorizontalDivider(
                    color = OtakuTheme.colors.outline.copy(alpha = 0.15f),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(horizontal = Dimens.spacingMd)
                )

                SettingsInfoRow(
                    icon = Icons.Default.Dataset,
                    title = "Basis Data Katalog",
                    value = if (isAdultContentEnabled) "3.360 Reguler + 1.637 Dewasa" else "3.360 Judul Reguler"
                )

                HorizontalDivider(
                    color = OtakuTheme.colors.outline.copy(alpha = 0.15f),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(horizontal = Dimens.spacingMd)
                )

                SettingsInfoRow(
                    icon = Icons.Default.Image,
                    title = "Aset Sampul Poster",
                    value = if (isAdultContentEnabled) "4.997 Berkas .webp (Lokal)" else "3.360 Berkas .webp (Lokal)"
                )

                HorizontalDivider(
                    color = OtakuTheme.colors.outline.copy(alpha = 0.15f),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(horizontal = Dimens.spacingMd)
                )

                SettingsInfoRow(
                    icon = Icons.Default.Policy,
                    title = "Privasi & Lisensi",
                    value = "100% Offline (Tanpa Pelacak)"
                )
            }

            // Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.spacingSm),
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
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "OtakuList - Offline Anime Tracker",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = OnSurfaceMuted
                    )
                }
                Text(
                    text = "Arsip pribadi yang beroperasi mandiri di penyimpanan lokal perangkat.",
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
            containerColor = OtakuTheme.colors.surfaceBase,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(text = "Cadangan Data Watchlist", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OtakuTheme.colors.onSurface)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
                    Text(
                        text = "Data watchlist berhasil diekspor ke format JSON. Salin ke clipboard atau bagikan ke aplikasi lain:",
                        style = Typography.bodySmall,
                        color = OtakuTheme.colors.onSurfaceMuted
                    )
                    OutlinedTextField(
                        value = json,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = OtakuTheme.colors.surfaceVariant,
                            unfocusedContainerColor = OtakuTheme.colors.surfaceVariant,
                            focusedTextColor = OtakuTheme.colors.onBackground,
                            unfocusedTextColor = OtakuTheme.colors.onBackground,
                            focusedBorderColor = OtakuTheme.colors.outline,
                            unfocusedBorderColor = OtakuTheme.colors.outline
                        ),
                        textStyle = Typography.bodySmall.copy(fontSize = 11.sp)
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("OtakuList Backup", json)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Cadangan disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                            viewModel.dismissExportDialog()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OtakuTheme.colors.primary, contentColor = Color.White),
                        shape = CompactButtonShape
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Salin", style = Typography.labelMedium)
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, json)
                                type = "application/json"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Bagikan Berkas Cadangan"))
                            viewModel.dismissExportDialog()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OtakuTheme.colors.surfaceContainerHigh, contentColor = OtakuTheme.colors.onSurface),
                        shape = CompactButtonShape
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Bagikan", style = Typography.labelMedium)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissExportDialog() }) {
                    Text(text = "Tutup", color = OtakuTheme.colors.onSurfaceMuted)
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = OtakuTheme.colors.surfaceBase,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(text = "Pulihkan Data Watchlist", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OtakuTheme.colors.onSurface)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
                    Text(
                        text = "Tempelkan kode JSON cadangan kamu di bawah ini untuk memulihkan entri tontonan:",
                        style = Typography.bodySmall,
                        color = OtakuTheme.colors.onSurfaceMuted
                    )
                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = { importInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        placeholder = { Text(text = "[{\"animeId\":1,...}]") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = OtakuTheme.colors.surfaceVariant,
                            unfocusedContainerColor = OtakuTheme.colors.surfaceVariant,
                            focusedTextColor = OtakuTheme.colors.onBackground,
                            unfocusedTextColor = OtakuTheme.colors.onBackground,
                            focusedBorderColor = OtakuTheme.colors.primary,
                            unfocusedBorderColor = OtakuTheme.colors.outline
                        ),
                        textStyle = Typography.bodySmall.copy(fontSize = 11.sp)
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
                    colors = ButtonDefaults.buttonColors(containerColor = OtakuTheme.colors.primary, contentColor = Color.White),
                    shape = CompactButtonShape
                ) {
                    Text(text = "Pulihkan Data", style = Typography.labelMedium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(text = "Batal", color = OtakuTheme.colors.onSurfaceMuted)
                }
            }
        )
    }

    // Clear Watchlist Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = OtakuTheme.colors.surfaceBase,
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(text = "Kosongkan Seluruh Watchlist?", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OtakuTheme.colors.onSurface)
            },
            text = {
                Text(
                    text = "Tindakan ini akan menghapus seluruh entri tontonan dari perangkat. Arsip katalog master tidak akan terpengaruh.",
                    style = Typography.bodySmall,
                    color = OtakuTheme.colors.onSurfaceMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllWatchlist()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error, contentColor = Color.White),
                    shape = CompactButtonShape
                ) {
                    Text(text = "Ya, Kosongkan", style = Typography.labelMedium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(text = "Batal", color = OtakuTheme.colors.onSurfaceMuted)
                }
            }
        )
    }

    // Import Result Message Dialog
    importMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissImportMessage() },
            containerColor = OtakuTheme.colors.surfaceBase,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(text = "Status Pemulihan", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OtakuTheme.colors.onSurface)
            },
            text = {
                Text(text = msg, style = Typography.bodyMedium, color = OtakuTheme.colors.onSurface)
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissImportMessage() },
                    colors = ButtonDefaults.buttonColors(containerColor = OtakuTheme.colors.primary, contentColor = Color.White),
                    shape = CompactButtonShape
                ) {
                    Text(text = "Selesai", style = Typography.labelMedium)
                }
            }
        )
    }

    // Custom Color HEX Input Dialog
    if (showCustomColorDialog) {
        var tempHex by remember { mutableStateOf(accentColorHex) }
        val parsedPreview = parseHexColor(tempHex, OtakuTheme.colors.primary)

        AlertDialog(
            onDismissRequest = { showCustomColorDialog = false },
            containerColor = OtakuTheme.colors.surfaceBase,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Pilih Warna Kustom",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OtakuTheme.colors.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)) {
                    Text(
                        text = "Masukkan kode HEX warna (contoh: #E01A6F atau #5B6EE8):",
                        style = Typography.bodySmall,
                        color = OtakuTheme.colors.onSurfaceMuted
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(parsedPreview)
                                .border(1.dp, OtakuTheme.colors.outline, CircleShape)
                        )

                        OutlinedTextField(
                            value = tempHex,
                            onValueChange = { input ->
                                if (input.length <= 9) tempHex = input
                            },
                            singleLine = true,
                            textStyle = Typography.bodyLarge.copy(color = OtakuTheme.colors.onSurface),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OtakuTheme.colors.primary,
                                unfocusedBorderColor = OtakuTheme.colors.outline,
                                focusedTextColor = OtakuTheme.colors.onSurface,
                                unfocusedTextColor = OtakuTheme.colors.onSurface
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = if (tempHex.startsWith("#")) tempHex else "#$tempHex"
                        viewModel.setAccentColorHex(clean)
                        showCustomColorDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OtakuTheme.colors.primary, contentColor = Color.White),
                    shape = CompactButtonShape
                ) {
                    Text("Terapkan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomColorDialog = false }) {
                    Text("Batal", color = OtakuTheme.colors.onSurfaceMuted)
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
            style = Typography.labelMedium.copy(
                letterSpacing = 0.6.sp,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            ),
            color = OtakuTheme.colors.onSurfaceMuted,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(OtakuTheme.colors.surfaceBase)
                .border(1.dp, OtakuTheme.colors.outline.copy(alpha = 0.25f), CardShape)
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
    onClick: () -> Unit,
    iconTint: Color = OtakuTheme.colors.onSurface,
    titleColor: Color = OtakuTheme.colors.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.spacingMd, vertical = 14.dp),
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
                    .clip(RoundedCornerShape(10.dp))
                    .background(OtakuTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.padding(end = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = Typography.titleMedium.copy(fontSize = 14.5.sp, fontWeight = FontWeight.Medium),
                    color = titleColor
                )
                Text(
                    text = subtitle,
                    style = Typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = OtakuTheme.colors.onSurfaceMuted
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = OtakuTheme.colors.onSurfaceMuted.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.spacingMd, vertical = 14.dp),
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
                    .clip(RoundedCornerShape(10.dp))
                    .background(OtakuTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OtakuTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = title,
                style = Typography.titleMedium.copy(fontSize = 14.5.sp, fontWeight = FontWeight.Medium),
                color = OtakuTheme.colors.onSurface
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(OtakuTheme.colors.surfaceContainer)
                .padding(horizontal = 9.dp, vertical = 5.dp)
        ) {
            Text(
                text = value,
                style = Typography.labelMedium.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                color = OtakuTheme.colors.primary
            )
        }
    }
}
