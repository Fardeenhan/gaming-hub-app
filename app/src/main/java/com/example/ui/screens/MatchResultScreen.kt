package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRoomRepository
import com.example.model.MatchResult
import com.example.model.VerificationStatus
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SlotOccupiedRed
import com.example.ui.theme.SlotPendingYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MatchResultScreen(
  repository: GameRoomRepository,
  modifier: Modifier = Modifier,
) {
  val matchResults by repository.matchResults.collectAsState()
  val rooms by repository.rooms.collectAsState()
  val activeUser by repository.activeUser.collectAsState()
  val isHostMode by repository.isHostMode.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Submit Proof", "Host Verification (${matchResults.count { it.status == VerificationStatus.PENDING }})", "Leaderboard")

  Column(modifier = modifier.fillMaxSize().background(DarkBackground)) {
    // Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(DarkSurface)
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Text(
        text = "Match Results & Kills Hub",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
      Text(
        text = "Upload match end-screen proof to claim stats & verify kills",
        style = MaterialTheme.typography.bodySmall,
        color = CyberCyan
      )
    }

    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = DarkSurface,
      contentColor = CyberCyan,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
          color = CyberCyan
        )
      }
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = {
            Text(
              text = title,
              fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
              fontSize = 11.sp,
              color = if (selectedTab == index) CyberCyan else TextSecondary
            )
          }
        )
      }
    }

    when (selectedTab) {
      0 -> SubmitProofView(
        repository = repository,
        myResults = matchResults.filter { it.userId == activeUser.uid }
      )
      1 -> HostVerificationQueueView(
        results = matchResults,
        onApprove = { resId, kills ->
          repository.verifyMatchResult(resId, approve = true, verifiedKills = kills)
        },
        onReject = { resId ->
          repository.verifyMatchResult(resId, approve = false, verifiedKills = 0)
        }
      )
      2 -> MatchLeaderboardView(results = matchResults.filter { it.status == VerificationStatus.APPROVED })
    }
  }
}

@Composable
fun SubmitProofView(
  repository: GameRoomRepository,
  myResults: List<MatchResult>
) {
  val activeUser by repository.activeUser.collectAsState()
  val rooms by repository.rooms.collectAsState()
  val context = LocalContext.current

  var killsInput by remember { mutableStateOf("6") }
  var selectedRoomId by remember { mutableStateOf(rooms.firstOrNull()?.roomId ?: "") }
  var selectedScreenshotPreset by remember { mutableStateOf("Winner Winner Chicken Dinner #1 (1280x720.png)") }
  var isUploading by remember { mutableStateOf(false) }

  val screenshotPresets = listOf(
    "Winner Winner Chicken Dinner #1 (1280x720.png)",
    "Top 2 Runner-up Podium Stats (1920x1080.jpg)",
    "Squad Total 15 Kills Breakdown (1080x720.png)",
    "Tekken 8 Perfect KO Victory Screenshot.png"
  )

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder),
          width = 1.dp
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Upload Match End-Screen Proof",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Your image will be stored securely in Firebase Storage and cross-referenced with your claimed kills.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Screenshot selector box
          Text("SELECTED MATCH SCREENSHOT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CyberCyan)
          Spacer(modifier = Modifier.height(6.dp))

          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(DarkBackground)
              .border(1.dp, Brush.horizontalGradient(listOf(CyberCyan, NeonPurple)), RoundedCornerShape(10.dp))
              .padding(12.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Image, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(28.dp))
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = selectedScreenshotPreset,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                Text("Ready to upload to Firebase Cloud Storage", style = MaterialTheme.typography.labelSmall, color = NeonEmerald)
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Preset options
            Text("Switch Mock Captured Image:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            screenshotPresets.forEach { preset ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(6.dp))
                  .clickable { selectedScreenshotPreset = preset }
                  .padding(vertical = 4.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (selectedScreenshotPreset == preset) CyberCyan else DarkCardBorder)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = preset,
                  style = MaterialTheme.typography.bodySmall,
                  color = if (selectedScreenshotPreset == preset) CyberCyan else TextSecondary
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Claimed kills input
          OutlinedTextField(
            value = killsInput,
            onValueChange = { killsInput = it },
            label = { Text("Claimed Kills Count") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("claimed_kills_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyberCyan,
              unfocusedBorderColor = DarkCardBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              val kills = killsInput.toIntOrNull() ?: 0
              if (selectedRoomId.isNotBlank()) {
                repository.submitMatchResult(selectedRoomId, kills, selectedScreenshotPreset)
                Toast.makeText(context, "Proof uploaded! Pending host review.", Toast.LENGTH_LONG).show()
              }
            },
            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("submit_proof_button"),
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload to Storage & Submit Proof", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // My Submissions
    item {
      Text(
        text = "MY SUBMISSIONS (${myResults.size})",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = CyberCyan
      )
    }

    items(myResults, key = { it.resultId }) { result ->
      MatchResultCard(result = result)
    }
  }
}

@Composable
fun HostVerificationQueueView(
  results: List<MatchResult>,
  onApprove: (String, Int) -> Unit,
  onReject: (String) -> Unit,
) {
  val context = LocalContext.current

  if (results.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("No submissions in queue", color = TextMuted)
    }
    return
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    items(results, key = { it.resultId }) { res ->
      Card(
        modifier = Modifier.fillMaxWidth().testTag("host_verification_${res.resultId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(
            when (res.status) {
              VerificationStatus.PENDING -> SlotPendingYellow
              VerificationStatus.APPROVED -> NeonEmerald
              VerificationStatus.REJECTED -> SlotOccupiedRed
            }
          ),
          width = 1.dp
        )
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = res.userIgn,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )

            val statusColor = when (res.status) {
              VerificationStatus.PENDING -> SlotPendingYellow
              VerificationStatus.APPROVED -> NeonEmerald
              VerificationStatus.REJECTED -> SlotOccupiedRed
            }
            Text(
              text = res.status.label,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = statusColor
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(DarkBackground)
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(text = res.screenshotTag, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
              Text(
                text = "Claimed Kills: ${res.killsClaimed} kills",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = CyberCyan
              )
            }
          }

          if (res.status == VerificationStatus.PENDING) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedButton(
                onClick = {
                  onReject(res.resultId)
                  Toast.makeText(context, "Rejected proof for ${res.userIgn}", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SlotOccupiedRed),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Reject Proof")
              }

              Button(
                onClick = {
                  onApprove(res.resultId, res.killsClaimed)
                  Toast.makeText(context, "Approved & verified ${res.killsClaimed} kills!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Verify & Approve", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun MatchLeaderboardView(results: List<MatchResult>) {
  val rankedResults = results.sortedByDescending { it.verifiedKills ?: it.killsClaimed }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = SlotPendingYellow, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Official Verified Kill Standings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
      }
    }

    if (rankedResults.isEmpty()) {
      item {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
          Text("No verified match results yet.", color = TextMuted)
        }
      }
    } else {
      items(rankedResults.indices.toList()) { index ->
        val item = rankedResults[index]
        val kills = item.verifiedKills ?: item.killsClaimed
        val rank = index + 1

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
              if (rank == 1) SlotPendingYellow else if (rank <= 3) CyberCyan else DarkCardBorder
            ),
            width = if (rank <= 3) 1.5.dp else 1.dp
          )
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(
                    when (rank) {
                      1 -> SlotPendingYellow
                      2 -> Color(0xFFE2E8F0)
                      3 -> Color(0xFFCD7F32)
                      else -> DarkSurfaceElevated
                    }
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "#$rank",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = if (rank <= 3) Color(0xFF0F172A) else TextPrimary
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(text = item.userIgn, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = "Proof Verified by Host", style = MaterialTheme.typography.labelSmall, color = NeonEmerald)
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = SlotOccupiedRed, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$kills Kills",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CyberCyan
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun MatchResultCard(result: MatchResult) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder),
      width = 1.dp
    )
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(text = result.screenshotTag, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(text = "Claimed: ${result.killsClaimed} kills", style = MaterialTheme.typography.bodySmall, color = CyberCyan)
      }

      val statusColor = when (result.status) {
        VerificationStatus.PENDING -> SlotPendingYellow
        VerificationStatus.APPROVED -> NeonEmerald
        VerificationStatus.REJECTED -> SlotOccupiedRed
      }
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(statusColor.copy(alpha = 0.2f))
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Text(text = result.status.label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor)
      }
    }
  }
}
