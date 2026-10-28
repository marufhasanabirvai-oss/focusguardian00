package com.focusguardian.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusguardian.app.data.preferences.GuardianPreferences
import com.focusguardian.app.domain.matching.ProtectionLockManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MainDashboardScreen(
                    preferences = GuardianPreferences(this),
                    onShowLockError = { remainingDays, lockUntil ->
                        val msg = if (lockUntil > 0L) {
                            ProtectionLockManager.getLockErrorMessageWithRemaining(lockUntil, isBengali = false)
                        } else {
                            ProtectionLockManager.getLockErrorMessage(remainingDays, isBengali = false)
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }
}

data class BlockModuleItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isEnabled: Boolean,
    val activationTimestamp: Long,
    val lockDays: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    preferences: GuardianPreferences,
    onShowLockError: (remainingDays: Int, lockUntil: Long) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe active states and lock timestamps
    val accessibilityEnabled by preferences.accessibilityEnabled.collectAsState(initial = true)
    val websiteEnabled by preferences.websiteBlockingEnabled.collectAsState(initial = false)
    val wordEnabled by preferences.wordBlockingEnabled.collectAsState(initial = false)
    val imageEnabled by preferences.imageBlockingEnabled.collectAsState(initial = false)
    val adultEnabled by preferences.adultBlockingEnabled.collectAsState(initial = false)
    val vpnEnabled by preferences.vpnTorBlockingEnabled.collectAsState(initial = false)

    val accessibilityLockUntil by preferences.accessibilityLockUntil.collectAsState(initial = 0L)
    val websiteLockUntil by preferences.websiteLockUntil.collectAsState(initial = 0L)
    val wordLockUntil by preferences.wordLockUntil.collectAsState(initial = 0L)
    val imageLockUntil by preferences.imageLockUntil.collectAsState(initial = 0L)
    val adultLockUntil by preferences.adultLockUntil.collectAsState(initial = 0L)
    val vpnLockUntil by preferences.vpnLockUntil.collectAsState(initial = 0L)

    val isAccessibilityLocked = ProtectionLockManager.isLockActive(accessibilityLockUntil)
    val accessibilityRemaining = if (isAccessibilityLocked) ProtectionLockManager.getRemainingDays(accessibilityLockUntil) else 0

    val isWebsiteLocked = ProtectionLockManager.isLockActive(websiteLockUntil)
    val websiteRemaining = if (isWebsiteLocked) ProtectionLockManager.getRemainingDays(websiteLockUntil) else 0

    val isWordLocked = ProtectionLockManager.isLockActive(wordLockUntil)
    val wordRemaining = if (isWordLocked) ProtectionLockManager.getRemainingDays(wordLockUntil) else 0

    val isImageLocked = ProtectionLockManager.isLockActive(imageLockUntil)
    val imageRemaining = if (isImageLocked) ProtectionLockManager.getRemainingDays(imageLockUntil) else 0

    val isAdultLocked = ProtectionLockManager.isLockActive(adultLockUntil)
    val adultRemaining = if (isAdultLocked) ProtectionLockManager.getRemainingDays(adultLockUntil) else 0

    val isVpnLocked = ProtectionLockManager.isLockActive(vpnLockUntil)
    val vpnRemaining = if (isVpnLocked) ProtectionLockManager.getRemainingDays(vpnLockUntil) else 0

    // Mandatory Lock Dialog State
    var pendingModuleToActivate by remember { mutableStateOf<String?>(null) }
    var inspectLockedModule by remember { mutableStateOf<Pair<String, Long>?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Focus Guardian",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF020617)
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Protection Modules",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8)
                )
            }

            // 0. Accessibility Guardian Service (Tamper-Resistant Core Engine)
            item {
                ModuleCard(
                    title = "Accessibility Guardian Service",
                    description = "Core engine for on-screen scanning & tamper-resistant enforcement",
                    icon = Icons.Default.Security,
                    isEnabled = accessibilityEnabled,
                    isLocked = isAccessibilityLocked,
                    remainingDays = accessibilityRemaining,
                    onOpenLockDetails = {
                        inspectLockedModule = Pair("Accessibility Guardian Service", accessibilityLockUntil)
                    },
                    onToggleAttempt = { targetState ->
                        handleModuleToggle(
                            moduleId = "accessibility",
                            moduleTitle = "Accessibility Guardian Service",
                            targetState = targetState,
                            isCurrentlyActive = accessibilityEnabled,
                            lockUntilTimestamp = accessibilityLockUntil,
                            onRequestLockDialog = { pendingModuleToActivate = "accessibility" },
                            onDisableAllowed = {
                                coroutineScope.launch { preferences.setAccessibilityEnabled(false) }
                            },
                            onShowError = { _, lockUntil ->
                                val msg = ProtectionLockManager.getAccessibilityLockErrorMessage(lockUntil)
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                )
            }

            // 1. Website Blocker
            item {
                ModuleCard(
                    title = "Website Blocker",
                    description = "Sinkholes configured domains & subdomains locally",
                    icon = Icons.Default.Public,
                    isEnabled = websiteEnabled,
                    isLocked = isWebsiteLocked,
                    remainingDays = websiteRemaining,
                    onOpenLockDetails = {
                        inspectLockedModule = Pair("Website", websiteLockUntil)
                    },
                    onToggleAttempt = { targetState ->
                        handleModuleToggle(
                            moduleId = "website",
                            moduleTitle = "Website Blocker",
                            targetState = targetState,
                            isCurrentlyActive = websiteEnabled,
                            lockUntilTimestamp = websiteLockUntil,
                            onRequestLockDialog = { pendingModuleToActivate = "website" },
                            onDisableAllowed = {
                                coroutineScope.launch { preferences.setWebsiteBlocking(false) }
                            },
                            onShowError = onShowLockError
                        )
                    }
                )
            }

            // 2. Keyword Blocker
            item {
                ModuleCard(
                    title = "Keyword Blocker",
                    description = "Monitors on-screen text in English & Bangla",
                    icon = Icons.Default.TextFields,
                    isEnabled = wordEnabled,
                    isLocked = isWordLocked,
                    remainingDays = wordRemaining,
                    onOpenLockDetails = {
                        inspectLockedModule = Pair("Keyword", wordLockUntil)
                    },
                    onToggleAttempt = { targetState ->
                        handleModuleToggle(
                            moduleId = "word",
                            moduleTitle = "Keyword Blocker",
                            targetState = targetState,
                            isCurrentlyActive = wordEnabled,
                            lockUntilTimestamp = wordLockUntil,
                            onRequestLockDialog = { pendingModuleToActivate = "word" },
                            onDisableAllowed = {
                                coroutineScope.launch { preferences.setWordBlocking(false) }
                            },
                            onShowError = onShowLockError
                        )
                    }
                )
            }

            // 3. Screenshot Blocker
            item {
                ModuleCard(
                    title = "Screenshot / Image Blocker",
                    description = "Visual similarity dHash pattern matching",
                    icon = Icons.Default.Image,
                    isEnabled = imageEnabled,
                    isLocked = isImageLocked,
                    remainingDays = imageRemaining,
                    onOpenLockDetails = {
                        inspectLockedModule = Pair("Screenshot", imageLockUntil)
                    },
                    onToggleAttempt = { targetState ->
                        handleModuleToggle(
                            moduleId = "image",
                            moduleTitle = "Screenshot Blocker",
                            targetState = targetState,
                            isCurrentlyActive = imageEnabled,
                            lockUntilTimestamp = imageLockUntil,
                            onRequestLockDialog = { pendingModuleToActivate = "image" },
                            onDisableAllowed = {
                                coroutineScope.launch { preferences.setImageBlocking(false) }
                            },
                            onShowError = onShowLockError
                        )
                    }
                )
            }

            // 4. Adult Content Blocker
            item {
                ModuleCard(
                    title = "Adult Content Filter",
                    description = "Blocks 1,000+ adult domains & explicit search",
                    icon = Icons.Default.Explicit,
                    isEnabled = adultEnabled,
                    isLocked = isAdultLocked,
                    remainingDays = adultRemaining,
                    onOpenLockDetails = {
                        inspectLockedModule = Pair("Adult Content", adultLockUntil)
                    },
                    onToggleAttempt = { targetState ->
                        handleModuleToggle(
                            moduleId = "adult",
                            moduleTitle = "Adult Content Filter",
                            targetState = targetState,
                            isCurrentlyActive = adultEnabled,
                            lockUntilTimestamp = adultLockUntil,
                            onRequestLockDialog = { pendingModuleToActivate = "adult" },
                            onDisableAllowed = {
                                coroutineScope.launch { preferences.setAdultBlocking(false) }
                            },
                            onShowError = onShowLockError
                        )
                    }
                )
            }

            // 5. VPN / Tor Blocker
            item {
                ModuleCard(
                    title = "VPN & Tor Protection",
                    description = "Detects external VPN tunnels and Tor Browser",
                    icon = Icons.Default.VpnLock,
                    isEnabled = vpnEnabled,
                    isLocked = isVpnLocked,
                    remainingDays = vpnRemaining,
                    onOpenLockDetails = {
                        inspectLockedModule = Pair("VPN & Tor", vpnLockUntil)
                    },
                    onToggleAttempt = { targetState ->
                        handleModuleToggle(
                            moduleId = "vpn",
                            moduleTitle = "VPN & Tor Protection",
                            targetState = targetState,
                            isCurrentlyActive = vpnEnabled,
                            lockUntilTimestamp = vpnLockUntil,
                            onRequestLockDialog = { pendingModuleToActivate = "vpn" },
                            onDisableAllowed = {
                                coroutineScope.launch { preferences.setVpnTorBlocking(false) }
                            },
                            onShowError = onShowLockError
                        )
                    }
                )
            }
        }
    }

    // Show Mandatory Lock Selection Dialog on activation
    pendingModuleToActivate?.let { moduleKey ->
        LockSelectionDialog(
            moduleName = moduleKey.replaceFirstChar { it.uppercase() } + " Blocker",
            isAlreadyLocked = false,
            onConfirmLock = { duration, isMinuteBased ->
                coroutineScope.launch {
                    preferences.activateWithMandatoryLock(moduleKey, duration, isMinuteBased)
                    val unitStr = if (isMinuteBased) "$duration minutes" else "$duration days"
                    snackbarHostState.showSnackbar(
                        "$moduleKey Blocker activated and locked for $unitStr!"
                    )
                }
                pendingModuleToActivate = null
            },
            onDismiss = { pendingModuleToActivate = null }
        )
    }

    // Show Read-Only Unalterable Dialog when inspecting an active lock
    inspectLockedModule?.let { (moduleName, expiryTimestamp) ->
        val remaining = ProtectionLockManager.getRemainingDays(expiryTimestamp)
        LockSelectionDialog(
            moduleName = "$moduleName Blocker",
            isAlreadyLocked = true,
            remainingDays = remaining,
            currentExpiryTimestamp = expiryTimestamp,
            onConfirmLock = { _, _ -> /* Unalterable */ },
            onDismiss = { inspectLockedModule = null }
        )
    }
}

/**
 * Feature 8 Core Toggle Logic:
 * - When turning ON (targetState = true): Shows mandatory lock selection dialog.
 * - When turning OFF (targetState = false): Checks if current time < lockUntilTimestamp.
 *   If not expired, displays "This block is locked for another X days and cannot be disabled." and prevents OFF toggle.
 */
fun handleModuleToggle(
    moduleId: String,
    moduleTitle: String,
    targetState: Boolean,
    isCurrentlyActive: Boolean,
    lockUntilTimestamp: Long,
    onRequestLockDialog: () -> Unit,
    onDisableAllowed: () -> Unit,
    onShowError: (remainingDays: Int, lockUntil: Long) -> Unit
) {
    if (targetState) {
        // User wants to turn ON -> Must select mandatory lock period (1 to 30 days)
        onRequestLockDialog()
    } else {
        // User attempts to turn OFF -> Check expiration
        val isLocked = ProtectionLockManager.isLockActive(lockUntilTimestamp)
        if (isLocked) {
            val remainingDays = ProtectionLockManager.getRemainingDays(lockUntilTimestamp)
            onShowError(remainingDays, lockUntilTimestamp)
            // PREVENT SWITCH FROM TURNING OFF
        } else {
            // Lock has expired -> allow turning off successfully
            onDisableAllowed()
        }
    }
}

@Composable
fun ModuleCard(
    title: String,
    description: String,
    icon: ImageVector,
    isEnabled: Boolean,
    isLocked: Boolean = false,
    remainingDays: Int = 0,
    onOpenLockDetails: (() -> Unit)? = null,
    onToggleAttempt: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isEnabled) (if (isLocked) Color(0xFFF59E0B) else Color(0xFF818CF8)) else Color(0xFF64748B),
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = isLocked) {
                        onOpenLockDetails?.invoke()
                    }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (isLocked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE11D48).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFFE11D48).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🔒 $remainingDays d locked",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDA4AF)
                            )
                        }
                    }
                }
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Switch is strictly disabled and grayed out when isLocked == true.
            // Any click attempts are intercepted by the container to trigger the warning.
            Box(
                modifier = Modifier.clickable(
                    enabled = isLocked,
                    onClick = {
                        onToggleAttempt(false)
                    }
                )
            ) {
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { onToggleAttempt(it) },
                    enabled = !isLocked, // Grayed out & disabled
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF6366F1),
                        disabledCheckedThumbColor = Color(0xFFFDA4AF),
                        disabledCheckedTrackColor = Color(0xFF881337).copy(alpha = 0.7f),
                        uncheckedThumbColor = Color(0xFF94A3B8),
                        uncheckedTrackColor = Color(0xFF1E293B)
                    )
                )
            }
        }
    }
}
