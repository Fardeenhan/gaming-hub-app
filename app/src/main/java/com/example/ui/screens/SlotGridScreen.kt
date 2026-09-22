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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRoomRepository
import com.example.model.GameRoom
import com.example.model.RoomSlot
import com.example.model.SlotStatus
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SlotLockedGray
import com.example.ui.theme.SlotOccupiedRed
import com.example.ui.theme.SlotOpenGreen
import com.example.ui.theme.SlotPendingYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotGridScreen(
  roomId: String,
  repository: GameRoomRepository,
  onNavigateBack: () -> Unit,
  onOpenHostPanel: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val rooms by repository.rooms.collectAsState()
  val slotsMap by repository.slotsByRoom.collectAsState()
  val activeUser by repository.activeUser.collectAsState()
  val isHostMode by repository.isHostMode.collectAsState()

  val room = rooms.find { it.roomId == roomId }
  val slots = slotsMap[roomId] ?: emptyList()

  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current

  var slotToReserve by remember { mutableStateOf<RoomSlot?>(null) }
  var playerIgnInput by remember { mutableStateOf(activeUser.ign) }
  var showRulesDialog by remember { mutableStateOf(false) }

  if (room == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("Room not found", color = TextSecondary)
    }
    return
  }

  val userSlot = slots.find { it.userId == activeUser.uid && it.status in listOf(SlotStatus.APPROVED, SlotStatus.PENDING) }
  val isApprovedUser = userSlot?.status == SlotStatus.APPROVED
  val isHost = room.hostId == activeUser.uid || isHostMode

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = room.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              color = TextPrimary
            )
            Text(
              text = "${room.gameName} • ${room.teamType} • ${room.mapOrMode}",
              style = MaterialTheme.typography.bodySmall,
              color = CyberCyan
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
          }
        },
        actions = {
          IconButton(onClick = { showRulesDialog = true }) {
            Icon(Icons.Default.Info, contentDescription = "Rules", tint = CyberCyan)
          }
          if (isHost) {
            IconButton(onClick = { onOpenHostPanel(roomId) }) {
              Icon(Icons.Default.Settings, contentDescription = "Host Panel", tint = NeonPurple)
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
      )
    },
    containerColor = DarkBackground
  ) { innerPadding ->
    LazyVerticalGrid(
      columns = GridCells.Adaptive(minSize = 68.dp),
      contentPadding = PaddingValues(
        top = innerPadding.calculateTopPadding() + 12.dp,
        bottom = 40.dp,
        start = 14.dp,
        end = 14.dp
      ),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = modifier.fillMaxSize()
    ) {
      // Room Credential Security Box (Full Width Span)
      item(span = { GridItemSpan(maxLineSpan) }) {
        SecurityCredentialCard(
          room = room,
          isApproved = isApprovedUser,
          isHost = isHost,
          onCopy = { text ->
            clipboardManager.setText(AnnotatedString(text))
            Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
          }
        )
      }

      // User Active Slot Status (if joined)
      if (userSlot != null) {
        item(span = { GridItemSpan(maxLineSpan) }) {
          UserJoinedBanner(
            slot = userSlot,
            onLeave = {
              repository.leaveSlot(roomId, userSlot.slotNumber)
              Toast.makeText(context, "Left slot #${userSlot.slotNumber}", Toast.LENGTH_SHORT).show()
            }
          )
        }
      }

      // Legend Row (Full Width Span)
      item(span = { GridItemSpan(maxLineSpan) }) {
        SlotLegendRow()
      }

      // Grid Section Header
      item(span = { GridItemSpan(maxLineSpan) }) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "SELECT YOUR SLOT (1 TO ${room.maxPlayers})",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = CyberCyan,
            letterSpacing = 1.sp
          )
          val openCount = slots.count { it.status == SlotStatus.OPEN }
          Text(
            text = "$openCount Available",
            style = MaterialTheme.typography.labelSmall,
            color = SlotOpenGreen,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // The Slot Cells
      items(slots, key = { it.slotId }) { slot ->
        val isMySlot = slot.userId == activeUser.uid
        SlotCellItem(
          slot = slot,
          isMySlot = isMySlot,
          onClick = {
            if (slot.status == SlotStatus.OPEN) {
              slotToReserve = slot
            } else if (isMySlot) {
              Toast.makeText(context, "You are in this slot (${slot.status.label})", Toast.LENGTH_SHORT).show()
            } else {
              val occupier = slot.userIgn ?: "Player"
              Toast.makeText(context, "Slot #${slot.slotNumber}: $occupier (${slot.status.label})", Toast.LENGTH_SHORT).show()
            }
          }
        )
      }
    }
  }

  // Reserve Slot Dialog
  if (slotToReserve != null) {
    AlertDialog(
      onDismissRequest = { slotToReserve = null },
      containerColor = DarkSurface,
      shape = RoundedCornerShape(16.dp),
      title = {
        Text(
          text = "Reserve Slot #${slotToReserve?.slotNumber}",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
      },
      text = {
        Column {
          Text(
            text = "Enter your in-game name (IGN) to reserve this slot. The host will review and approve your join request.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
          )
          Spacer(modifier = Modifier.height(14.dp))
          OutlinedTextField(
            value = playerIgnInput,
            onValueChange = { playerIgnInput = it },
            label = { Text("Game In-Game Name (IGN)") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("reserve_ign_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyberCyan,
              unfocusedBorderColor = DarkCardBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val target = slotToReserve
            if (target != null && playerIgnInput.isNotBlank()) {
              val success = repository.reserveSlot(
                roomId = roomId,
                slotNumber = target.slotNumber,
                userIgn = playerIgnInput.trim(),
                requireApproval = true
              )
              if (success) {
                Toast.makeText(context, "Slot #${target.slotNumber} reserved! Awaiting host approval.", Toast.LENGTH_LONG).show()
              } else {
                Toast.makeText(context, "Could not reserve slot. You may already be in a slot.", Toast.LENGTH_SHORT).show()
              }
              slotToReserve = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF0F172A)),
          modifier = Modifier.testTag("confirm_reserve_button")
        ) {
          Text("Request Slot", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { slotToReserve = null }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }

  // Rules Dialog
  if (showRulesDialog) {
    AlertDialog(
      onDismissRequest = { showRulesDialog = false },
      containerColor = DarkSurface,
      shape = RoundedCornerShape(16.dp),
      icon = { Icon(Icons.Default.Security, contentDescription = null, tint = CyberCyan) },
      title = { Text("Custom Room Rules", fontWeight = FontWeight.Bold, color = TextPrimary) },
      text = {
        Column {
          Text(text = room.customRules, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, lineHeight = 20.sp)
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Scheduled: ${room.scheduledAt}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = CyberCyan
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { showRulesDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = TextPrimary)
        ) {
          Text("Got It")
        }
      }
    )
  }
}

@Composable
fun SecurityCredentialCard(
  room: GameRoom,
  isApproved: Boolean,
  isHost: Boolean,
  onCopy: (String) -> Unit
) {
  val canViewCredentials = isHost || (room.credentialsReleased && isApproved)

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("credentials_security_card"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (canViewCredentials) Color(0xFF0C243B) else DarkSurface
    ),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.horizontalGradient(
        if (canViewCredentials) listOf(CyberCyan, NeonEmerald) else listOf(DarkCardBorder, DarkCardBorder)
      ),
      width = if (canViewCredentials) 1.5.dp else 1.dp
    )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (canViewCredentials) Icons.Default.Key else Icons.Default.Lock,
            contentDescription = null,
            tint = if (canViewCredentials) CyberCyan else SlotPendingYellow,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "ROOM CREDENTIALS",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (canViewCredentials) CyberCyan else TextPrimary
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
              if (room.credentialsReleased) NeonEmerald.copy(alpha = 0.2f)
              else SlotPendingYellow.copy(alpha = 0.2f)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = if (room.credentialsReleased) "RELEASED BY HOST" else "LOCKED BY HOST",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (room.credentialsReleased) NeonEmerald else SlotPendingYellow
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (canViewCredentials) {
        // Revealed Credentials
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkBackground.copy(alpha = 0.6f))
            .padding(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("ROOM ID", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              Text(
                text = room.roomIdCode.ifEmpty { "Pending host entry" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            }
            IconButton(onClick = { onCopy(room.roomIdCode) }) {
              Icon(Icons.Default.ContentCopy, contentDescription = "Copy Room ID", tint = CyberCyan, modifier = Modifier.size(18.dp))
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("PASSWORD", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              Text(
                text = room.roomPassword.ifEmpty { "Pending host entry" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeonEmerald
              )
            }
            IconButton(onClick = { onCopy(room.roomPassword) }) {
              Icon(Icons.Default.ContentCopy, contentDescription = "Copy Password", tint = NeonEmerald, modifier = Modifier.size(18.dp))
            }
          }
        }
      } else {
        // Locked / Security Explanations
        Text(
          text = if (!room.credentialsReleased) {
            "🔒 The host has not yet released the Room ID and Password. Once the host verifies the lobby and releases credentials, they will unlock here automatically for approved players."
          } else {
            "🔒 Security Policy: Credentials have been released, but are only visible to players with an APPROVED slot. Join and await host confirmation to view."
          },
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary,
          lineHeight = 18.sp
        )
      }
    }
  }
}

@Composable
fun UserJoinedBanner(
  slot: RoomSlot,
  onLeave: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.horizontalGradient(listOf(CyberCyan, NeonPurple)),
      width = 1.dp
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (slot.status == SlotStatus.APPROVED) NeonEmerald else SlotPendingYellow),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "#${slot.slotNumber}",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFF0F172A)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Your Slot: #${slot.slotNumber} (${slot.userIgn})",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Text(
            text = slot.status.label,
            style = MaterialTheme.typography.bodySmall,
            color = if (slot.status == SlotStatus.APPROVED) NeonEmerald else SlotPendingYellow
          )
        }
      }

      OutlinedButton(
        onClick = onLeave,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = SlotOccupiedRed),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
      ) {
        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Leave", fontSize = 12.sp)
      }
    }
  }
}

@Composable
fun SlotLegendRow() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(DarkSurface)
      .padding(horizontal = 12.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    LegendItem(color = SlotOpenGreen, label = "Open")
    LegendItem(color = SlotPendingYellow, label = "Pending")
    LegendItem(color = SlotOccupiedRed, label = "Occupied")
    LegendItem(color = SlotLockedGray, label = "Locked")
  }
}

@Composable
fun LegendItem(color: Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(10.dp)
        .clip(RoundedCornerShape(2.dp))
        .background(color)
    )
    Spacer(modifier = Modifier.width(5.dp))
    Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 11.sp)
  }
}

@Composable
fun SlotCellItem(
  slot: RoomSlot,
  isMySlot: Boolean,
  onClick: () -> Unit
) {
  val backgroundColor = when (slot.status) {
    SlotStatus.OPEN -> SlotOpenGreen.copy(alpha = 0.15f)
    SlotStatus.PENDING -> SlotPendingYellow.copy(alpha = 0.2f)
    SlotStatus.APPROVED -> SlotOccupiedRed.copy(alpha = 0.2f)
    SlotStatus.LOCKED -> SlotLockedGray.copy(alpha = 0.2f)
    SlotStatus.KICKED -> DarkSurfaceElevated
  }

  val borderColor = when {
    isMySlot -> CyberCyan
    slot.status == SlotStatus.OPEN -> SlotOpenGreen
    slot.status == SlotStatus.PENDING -> SlotPendingYellow
    slot.status == SlotStatus.APPROVED -> SlotOccupiedRed
    else -> SlotLockedGray
  }

  val statusIcon = when (slot.status) {
    SlotStatus.LOCKED -> Icons.Default.Lock
    SlotStatus.OPEN -> null
    else -> Icons.Default.Person
  }

  Box(
    modifier = Modifier
      .aspectRatio(1f)
      .clip(RoundedCornerShape(8.dp))
      .background(backgroundColor)
      .border(
        width = if (isMySlot) 2.dp else 1.dp,
        color = borderColor,
        shape = RoundedCornerShape(8.dp)
      )
      .clickable { onClick() }
      .padding(4.dp)
      .testTag("slot_cell_${slot.slotNumber}"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = "#${slot.slotNumber}",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = if (isMySlot) CyberCyan else TextPrimary,
        fontSize = 12.sp
      )

      if (slot.userIgn != null) {
        Text(
          text = slot.userIgn,
          style = MaterialTheme.typography.labelSmall,
          fontSize = 9.sp,
          color = if (isMySlot) CyberCyan else TextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          textAlign = TextAlign.Center
        )
      } else if (slot.status == SlotStatus.OPEN) {
        Text(
          text = "JOIN",
          style = MaterialTheme.typography.labelSmall,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          color = SlotOpenGreen
        )
      } else if (statusIcon != null) {
        Icon(
          imageVector = statusIcon,
          contentDescription = null,
          tint = if (slot.status == SlotStatus.LOCKED) SlotLockedGray else SlotOccupiedRed,
          modifier = Modifier.size(12.dp)
        )
      }
    }
  }
}
