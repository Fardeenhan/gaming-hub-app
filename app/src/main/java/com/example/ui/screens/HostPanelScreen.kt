package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRoomRepository
import com.example.model.GameRoom
import com.example.model.RoomSlot
import com.example.model.RoomStatus
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
import com.example.ui.theme.SlotPendingYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostPanelScreen(
  roomId: String,
  repository: GameRoomRepository,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val rooms by repository.rooms.collectAsState()
  val slotsMap by repository.slotsByRoom.collectAsState()
  val context = LocalContext.current

  val room = rooms.find { it.roomId == roomId }
  val slots = slotsMap[roomId] ?: emptyList()

  if (room == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("Room not found", color = TextSecondary)
    }
    return
  }

  var roomIdInput by remember(room.roomIdCode) { mutableStateOf(room.roomIdCode) }
  var roomPasswordInput by remember(room.roomPassword) { mutableStateOf(room.roomPassword) }
  var credentialsReleased by remember(room.credentialsReleased) { mutableStateOf(room.credentialsReleased) }

  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Credentials & Status", "Requests (${slots.count { it.status == SlotStatus.PENDING }})", "Manage Slots")

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Host Control Panel",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = room.title,
              style = MaterialTheme.typography.bodySmall,
              color = NeonPurple
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
      )
    },
    containerColor = DarkBackground
  ) { innerPadding ->
    Column(
      modifier = modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Room Quick Metric Pills
      RoomMetricsHeader(room = room, slots = slots)

      // Tabs
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
                fontSize = 12.sp,
                color = if (selectedTab == index) CyberCyan else TextSecondary
              )
            }
          )
        }
      }

      when (selectedTab) {
        0 -> CredentialsAndStatusTab(
          room = room,
          roomIdInput = roomIdInput,
          onRoomIdChange = { roomIdInput = it },
          roomPasswordInput = roomPasswordInput,
          onRoomPasswordChange = { roomPasswordInput = it },
          credentialsReleased = credentialsReleased,
          onToggleRelease = { release ->
            credentialsReleased = release
            repository.releaseCredentials(roomId, release)
            if (release) {
              Toast.makeText(context, "Credentials released to approved players! FCM notification simulated.", Toast.LENGTH_LONG).show()
            }
          },
          onSaveCredentials = {
            repository.updateCredentials(roomId, roomIdInput.trim(), roomPasswordInput.trim())
            Toast.makeText(context, "Room credentials updated!", Toast.LENGTH_SHORT).show()
          },
          onUpdateStatus = { status ->
            repository.updateRoomStatus(roomId, status)
            Toast.makeText(context, "Room status set to ${status.label}", Toast.LENGTH_SHORT).show()
          }
        )
        1 -> PendingRequestsTab(
          slots = slots.filter { it.status == SlotStatus.PENDING },
          onApprove = { slotNum ->
            repository.approveSlot(roomId, slotNum)
            Toast.makeText(context, "Approved slot #$slotNum!", Toast.LENGTH_SHORT).show()
          },
          onReject = { slotNum ->
            repository.rejectSlot(roomId, slotNum)
            Toast.makeText(context, "Rejected request for slot #$slotNum", Toast.LENGTH_SHORT).show()
          }
        )
        2 -> ManageSlotsTab(
          slots = slots,
          onKick = { slotNum ->
            repository.kickPlayer(roomId, slotNum)
            Toast.makeText(context, "Player kicked from slot #$slotNum. Notification pushed.", Toast.LENGTH_SHORT).show()
          },
          onToggleLock = { slotNum ->
            repository.toggleLockSlot(roomId, slotNum)
          }
        )
      }
    }
  }
}

@Composable
fun RoomMetricsHeader(room: GameRoom, slots: List<RoomSlot>) {
  val approvedCount = slots.count { it.status == SlotStatus.APPROVED }
  val pendingCount = slots.count { it.status == SlotStatus.PENDING }
  val openCount = slots.count { it.status == SlotStatus.OPEN }
  val lockedCount = slots.count { it.status == SlotStatus.LOCKED }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(DarkSurfaceElevated)
      .padding(horizontal = 14.dp, vertical = 10.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    MetricPill(label = "Total Slots", value = "${room.maxPlayers}", color = TextPrimary)
    MetricPill(label = "Approved", value = "$approvedCount", color = NeonEmerald)
    MetricPill(label = "Pending", value = "$pendingCount", color = SlotPendingYellow)
    MetricPill(label = "Open", value = "$openCount", color = CyberCyan)
    MetricPill(label = "Locked", value = "$lockedCount", color = SlotLockedGray)
  }
}

@Composable
fun MetricPill(label: String, value: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
    Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = TextMuted)
  }
}

@Composable
fun CredentialsAndStatusTab(
  room: GameRoom,
  roomIdInput: String,
  onRoomIdChange: (String) -> Unit,
  roomPasswordInput: String,
  onRoomPasswordChange: (String) -> Unit,
  credentialsReleased: Boolean,
  onToggleRelease: (Boolean) -> Unit,
  onSaveCredentials: () -> Unit,
  onUpdateStatus: (RoomStatus) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Credential Release Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(if (credentialsReleased) NeonEmerald else DarkCardBorder),
          width = 1.dp
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Key, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Room ID & Password", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            // Release Switch
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = if (credentialsReleased) "RELEASED" else "LOCKED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (credentialsReleased) NeonEmerald else SlotPendingYellow
              )
              Spacer(modifier = Modifier.width(6.dp))
              Switch(
                checked = credentialsReleased,
                onCheckedChange = onToggleRelease,
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = NeonEmerald,
                  uncheckedThumbColor = TextMuted,
                  uncheckedTrackColor = DarkSurfaceElevated
                ),
                modifier = Modifier.testTag("credentials_release_switch")
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = roomIdInput,
            onValueChange = onRoomIdChange,
            label = { Text("Game Room ID Code (e.g. 7294021)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("host_room_id_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyberCyan,
              unfocusedBorderColor = DarkCardBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = roomPasswordInput,
            onValueChange = onRoomPasswordChange,
            label = { Text("Game Room Password (e.g. WINNER_PASS)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("host_room_password_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonEmerald,
              unfocusedBorderColor = DarkCardBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (credentialsReleased) "✓ Visible to all approved players" else "🔒 Hidden until released",
              style = MaterialTheme.typography.bodySmall,
              color = if (credentialsReleased) NeonEmerald else SlotPendingYellow
            )

            Button(
              onClick = onSaveCredentials,
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF0F172A))
            ) {
              Text("Save Info", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // Match Status Card
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
          Text("Match Lifecycle Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            RoomStatus.values().forEach { status ->
              val isCurrent = room.status == status
              val btnColor = when (status) {
                RoomStatus.OPEN -> NeonEmerald
                RoomStatus.FULL -> SlotOccupiedRed
                RoomStatus.LIVE -> NeonPurple
                RoomStatus.COMPLETED -> TextMuted
              }
              OutlinedButton(
                onClick = { onUpdateStatus(status) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = if (isCurrent) btnColor.copy(alpha = 0.2f) else Color.Transparent,
                  contentColor = if (isCurrent) btnColor else TextSecondary
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                  brush = androidx.compose.ui.graphics.SolidColor(if (isCurrent) btnColor else DarkCardBorder)
                ),
                contentPadding = PaddingValues(vertical = 8.dp)
              ) {
                Text(text = status.label, fontSize = 11.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
              }
            }
          }
        }
      }
    }

    // Notification broadcast simulator
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text("Cloud Messaging (FCM)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(
              "Instant push notifications trigger automatically when you release credentials, kick a player, or start the live room.",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              lineHeight = 16.sp
            )
          }
        }
      }
    }
  }
}

@Composable
fun PendingRequestsTab(
  slots: List<RoomSlot>,
  onApprove: (Int) -> Unit,
  onReject: (Int) -> Unit,
) {
  if (slots.isEmpty()) {
    Box(
      modifier = Modifier.fillMaxSize().padding(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Check, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text("No Pending Requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("All player requests have been reviewed!", style = MaterialTheme.typography.bodySmall, color = TextMuted)
      }
    }
  } else {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(slots, key = { it.slotId }) { slot ->
        Card(
          modifier = Modifier.fillMaxWidth().testTag("pending_request_slot_${slot.slotNumber}"),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(SlotPendingYellow),
            width = 1.dp
          )
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(SlotPendingYellow),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "#${slot.slotNumber}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Color(0xFF0F172A)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = slot.userIgn ?: "Unknown Player",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                Text(
                  text = "Awaiting approval for Slot #${slot.slotNumber}",
                  style = MaterialTheme.typography.bodySmall,
                  color = SlotPendingYellow
                )
              }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              IconButton(
                onClick = { onReject(slot.slotNumber) },
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(SlotOccupiedRed.copy(alpha = 0.2f))
                  .testTag("reject_slot_${slot.slotNumber}")
              ) {
                Icon(Icons.Default.Close, contentDescription = "Reject", tint = SlotOccupiedRed, modifier = Modifier.size(18.dp))
              }

              IconButton(
                onClick = { onApprove(slot.slotNumber) },
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(NeonEmerald.copy(alpha = 0.2f))
                  .testTag("approve_slot_${slot.slotNumber}")
              ) {
                Icon(Icons.Default.Check, contentDescription = "Approve", tint = NeonEmerald, modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ManageSlotsTab(
  slots: List<RoomSlot>,
  onKick: (Int) -> Unit,
  onToggleLock: (Int) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(slots, key = { it.slotId }) { slot ->
      val isOccupied = slot.status == SlotStatus.APPROVED
      val isLocked = slot.status == SlotStatus.LOCKED
      val isPending = slot.status == SlotStatus.PENDING

      Card(
        modifier = Modifier.fillMaxWidth().testTag("manage_slot_${slot.slotNumber}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(
            if (isOccupied) SlotOccupiedRed.copy(alpha = 0.5f) else DarkCardBorder
          ),
          width = 1.dp
        )
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                  when (slot.status) {
                    SlotStatus.APPROVED -> SlotOccupiedRed.copy(alpha = 0.2f)
                    SlotStatus.PENDING -> SlotPendingYellow.copy(alpha = 0.2f)
                    SlotStatus.LOCKED -> SlotLockedGray.copy(alpha = 0.2f)
                    else -> CyberCyan.copy(alpha = 0.1f)
                  }
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "${slot.slotNumber}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = when (slot.status) {
                  SlotStatus.APPROVED -> SlotOccupiedRed
                  SlotStatus.PENDING -> SlotPendingYellow
                  SlotStatus.LOCKED -> SlotLockedGray
                  else -> CyberCyan
                }
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = slot.userIgn ?: if (isLocked) "Locked Slot" else "Empty / Open",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (slot.userIgn != null) FontWeight.Bold else FontWeight.Normal,
                color = if (slot.userIgn != null) TextPrimary else TextMuted
              )
              Text(
                text = slot.status.label,
                style = MaterialTheme.typography.labelSmall,
                color = when (slot.status) {
                  SlotStatus.APPROVED -> NeonEmerald
                  SlotStatus.PENDING -> SlotPendingYellow
                  SlotStatus.LOCKED -> SlotLockedGray
                  else -> TextMuted
                }
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Lock toggle
            IconButton(
              onClick = { onToggleLock(slot.slotNumber) },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = "Lock",
                tint = if (isLocked) SlotPendingYellow else TextMuted,
                modifier = Modifier.size(16.dp)
              )
            }

            // Kick action (only if occupied or pending)
            if (isOccupied || isPending) {
              IconButton(
                onClick = { onKick(slot.slotNumber) },
                modifier = Modifier.size(32.dp).testTag("kick_player_slot_${slot.slotNumber}")
              ) {
                Icon(
                  Icons.Default.PersonRemove,
                  contentDescription = "Kick Player",
                  tint = SlotOccupiedRed,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
