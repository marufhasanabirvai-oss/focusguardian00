package com.focusguardian.app.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class PenaltyLockActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val reason = intent.getStringExtra("EXTRA_REASON") ?: "Restricted content triggered"
        val durationSeconds = intent.getIntExtra("EXTRA_DURATION_SECONDS", 30)

        // Block Back Button navigation during active penalty
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Restricted during penalty! Do nothing to enforce self-control
            }
        })

        setContent {
            MaterialTheme {
                PenaltyScreenContent(
                    reason = reason,
                    totalSeconds = durationSeconds,
                    onFinished = { finish() }
                )
            }
        }
    }
}

@Composable
fun PenaltyScreenContent(
    reason: String,
    totalSeconds: Int,
    onFinished: () -> Unit
) {
    var remainingSeconds by remember { mutableStateOf(totalSeconds) }

    // Countdown timer
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
        onFinished()
    }

    // Breathing pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Breathing Circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale)
                    .background(Color(0xFF6366F1).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(Color(0xFF6366F1).copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = "Reflect",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Take a Deep Breath",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Breathe in... Breathe out... Regain control.",
                fontSize = 15.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Triggered Rule:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = reason,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF43F5E),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Timer display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$remainingSeconds s",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF38BDF8)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Back button is restricted during cooldown.",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
