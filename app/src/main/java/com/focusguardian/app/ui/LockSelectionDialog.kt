package com.focusguardian.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LockSelectionDialog(
    moduleName: String,
    isAlreadyLocked: Boolean = false,
    remainingDays: Int = 0,
    currentExpiryTimestamp: Long = 0L,
    onConfirmLock: (duration: Int, isMinuteBased: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    // Mode: Basic (Minutes: 1..100) vs Advanced (Days: 1..30)
    var isMinuteMode by remember { mutableStateOf(false) }
    
    val presetMinutes = listOf(5, 10, 25, 45, 60, 100)
    val presetDays = listOf(1, 3, 7, 14, 21, 30)

    var selectedMinutes by remember { mutableStateOf(25) }
    var selectedDays by remember { mutableStateOf(if (isAlreadyLocked && remainingDays > 0) remainingDays else 7) }
    var showLockedWarning by remember { mutableStateOf(false) }

    val unlockTimestamp = if (isAlreadyLocked && currentExpiryTimestamp > 0L) {
        currentExpiryTimestamp
    } else if (isMinuteMode) {
        System.currentTimeMillis() + (selectedMinutes * 60_000L)
    } else {
        System.currentTimeMillis() + (selectedDays * 86_400_000L)
    }

    val unlockDateStr = remember(unlockTimestamp, isMinuteMode) {
        val pattern = if (isMinuteMode) "hh:mm a, MMM dd" else "MMM dd, yyyy"
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        sdf.format(Date(unlockTimestamp))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header with Lock Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isAlreadyLocked) Color(0xFFE11D48).copy(alpha = 0.2f)
                                else if (isMinuteMode) Color(0xFF06B6D4).copy(alpha = 0.2f)
                                else Color(0xFF6366F1).copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = if (isAlreadyLocked) Color(0xFFFDA4AF) else if (isMinuteMode) Color(0xFF22D3EE) else Color(0xFF818CF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAlreadyLocked) "Strict Irreversible Lock" else "Dual Time Lock System",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = moduleName,
                            fontSize = 12.sp,
                            color = Color(0xFFFBBF24),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dual Mode Switcher Tabs: Basic (Minutes) vs Advanced (Days)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF020617))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Basic Mode Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isMinuteMode) Color(0xFF0891B2) else Color.Transparent)
                            .clickable(enabled = !isAlreadyLocked) { isMinuteMode = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Basic (Minutes)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMinuteMode) Color.White else Color(0xFF94A3B8)
                            )
                            Text(
                                text = "1 - 100 Mins",
                                fontSize = 8.sp,
                                color = if (isMinuteMode) Color(0xFFCFFAFE) else Color(0xFF64748B)
                            )
                        }
                    }

                    // Advanced Mode Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (!isMinuteMode) Color(0xFF4F46E5) else Color.Transparent)
                            .clickable(enabled = !isAlreadyLocked) { isMinuteMode = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Advanced (Days)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isMinuteMode) Color.White else Color(0xFF94A3B8)
                            )
                            Text(
                                text = "1 - 30 Days Strict",
                                fontSize = 8.sp,
                                color = if (!isMinuteMode) Color(0xFFE0E7FF) else Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Explanatory Requirement Text
                if (isAlreadyLocked) {
                    val remainingFormatted = com.focusguardian.app.domain.matching.ProtectionLockManager.formatRemaining(currentExpiryTimestamp)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4C0519).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFBE123C).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFFB7185),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Active Lock: ${remainingFormatted}. Both the toggle switch and duration selector are completely disabled. Cannot be changed, extended, or reduced until expiration.",
                                fontSize = 11.sp,
                                color = Color(0xFFFECDD3),
                                lineHeight = 15.sp
                            )
                        }
                    }
                } else if (isMinuteMode) {
                    Text(
                        text = "Basic Mode: Short-term focus sessions (1-100 mins) for quick work bursts and study resets.",
                        fontSize = 11.sp,
                        color = Color(0xFF67E8F9),
                        lineHeight = 15.sp
                    )
                } else {
                    Text(
                        text = "Advanced Mode: Strict commitment lock (1-30 days). Once activated, cannot be disabled until expiration.",
                        fontSize = 11.sp,
                        color = Color(0xFFA5B4FC),
                        lineHeight = 15.sp
                    )
                }

                if (showLockedWarning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF78350F).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Warning: This lock is active and cannot be modified until the countdown finishes.",
                            fontSize = 11.sp,
                            color = Color(0xFFFDE68A),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Duration Selection Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isMinuteMode) "Minutes Selection:" else "Days Selection:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = if (isMinuteMode) "${selectedMinutes} Mins" else "${selectedDays} Days",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMinuteMode) Color(0xFF22D3EE) else Color(0xFF818CF8)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Presets Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .height(100.dp)
                        .clickable(enabled = isAlreadyLocked) { showLockedWarning = true }
                ) {
                    if (isMinuteMode) {
                        items(presetMinutes) { mins ->
                            val isSelected = selectedMinutes == mins
                            val buttonColor = when {
                                isAlreadyLocked -> Color(0xFF1E293B).copy(alpha = 0.35f)
                                isSelected -> Color(0xFF0891B2)
                                else -> Color(0xFF1E293B)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(buttonColor)
                                    .border(width = 1.dp, color = if (isSelected) Color(0xFF67E8F9) else Color(0xFF334155), shape = RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isAlreadyLocked) { selectedMinutes = mins }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${mins}m",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                )
                            }
                        }
                    } else {
                        items(presetDays) { days ->
                            val isSelected = selectedDays == days
                            val buttonColor = when {
                                isAlreadyLocked -> Color(0xFF1E293B).copy(alpha = 0.35f)
                                isSelected -> Color(0xFF6366F1)
                                else -> Color(0xFF1E293B)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(buttonColor)
                                    .border(width = 1.dp, color = if (isSelected) Color(0xFFA5B4FC) else Color(0xFF334155), shape = RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isAlreadyLocked) { selectedDays = days }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${days}d",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom Slider
                if (isMinuteMode) {
                    Slider(
                        value = selectedMinutes.toFloat(),
                        onValueChange = { if (!isAlreadyLocked) selectedMinutes = it.toInt() else showLockedWarning = true },
                        valueRange = 1f..100f,
                        enabled = !isAlreadyLocked,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF22D3EE), activeTrackColor = Color(0xFF0891B2))
                    )
                } else {
                    Slider(
                        value = selectedDays.toFloat(),
                        onValueChange = { if (!isAlreadyLocked) selectedDays = it.toInt() else showLockedWarning = true },
                        valueRange = 1f..30f,
                        enabled = !isAlreadyLocked,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF818CF8), activeTrackColor = Color(0xFF6366F1))
                    )
                }

                // Unlock Date Preview & Warning
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAlreadyLocked) Color(0xFF31101E) else Color(0xFF1E1B4B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Unlocks on ${unlockDateStr}. Cannot be bypassed until expired.",
                            fontSize = 11.sp,
                            color = Color(0xFFC7D2FE),
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                if (isAlreadyLocked) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                    ) {
                        Text("Understood (Close)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8))
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (isMinuteMode) {
                                    onConfirmLock(selectedMinutes, true)
                                } else {
                                    onConfirmLock(selectedDays, false)
                                }
                            },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMinuteMode) Color(0xFF0891B2) else Color(0xFF6366F1)
                            )
                        ) {
                            Text(
                                text = if (isMinuteMode) "Lock ${selectedMinutes}m" else "Lock ${selectedDays}d",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
