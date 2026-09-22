package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
  repository: GameRoomRepository,
  onOpenRoom: (String) -> Unit,
  onOpenHostPanel: (String) -> Unit,
  onCreateRoomClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val rooms by repository.rooms.collectAsState()
  val slotsByRoom by repository.slotsByRoom.collectAsState()
  val isHostMode by repository.isHostMode.collectAsState()
  val activeUser by repository.activeUser.collectAsState()

  var selectedGameFilter by remember { mutableStateOf("All") }
  val gameCategories = listOf("All", "PUBG Mobile", "Free Fire", "Tekken 8", "COD Mobile")

  val filteredRooms = rooms.filter {
    selectedGameFilter == "All" || it.gameName.equals(selectedGameFilter, ignoreCase = true)
  }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = 96.dp, top = 12.dp, start = 16.dp, end = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Hero Banner
      item {
        HeroBanner(
          activeIgn = activeUser.ign,
          isHost = isHostMode,
          totalRooms = rooms.size
        )
      }

      // Game Filter Chips
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "CHOOSE GAME / TOURNAMENT",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = CyberCyan,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            gameCategories.forEach { game ->
              val isSelected = selectedGameFilter == game
              ElevatedFilterChip(
                selected = isSelected,
                onClick = { selectedGameFilter = game },
                label = { Text(game, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.elevatedFilterChipColors(
                  selectedContainerColor = NeonPurple,
                  selectedLabelColor = Color.White,
                  containerColor = DarkSurfaceElevated,
                  labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                  enabled = true,
                  selected = isSelected,
                  borderColor = if (isSelected) CyberCyan else DarkCardBorder
                )
              )
            }
          }
        }
      }

      // Section Title
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "ACTIVE CUSTOM ROOMS (${filteredRooms.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        }
      }

      // Room Cards
      items(filteredRooms, key = { it.roomId }) { room ->
        val slots = slotsByRoom[room.roomId] ?: emptyList()
        val occupiedCount = slots.count { it.status == SlotStatus.APPROVED }
        val isUserInRoom = slots.any { it.userId == activeUser.uid && it.status in listOf(SlotStatus.APPROVED, SlotStatus.PENDING) }
        val isHostOfRoom = room.hostId == activeUser.uid || isHostMode

        GameRoomCard(
          room = room,
          slots = slots,
          occupiedCount = occupiedCount,
          isUserInRoom = isUserInRoom,
          isHost = isHostOfRoom,
          onViewSlots = { onOpenRoom(room.roomId) },
          onManageHost = { onOpenHostPanel(room.roomId) }
        )
      }
    }

    // Host Room FAB
    FloatingActionButton(
      onClick = onCreateRoomClick,
      containerColor = CyberCyan,
      contentColor = Color(0xFF0F172A),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(20.dp)
        .testTag("create_room_fab")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Add, contentDescription = "Host Room")
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "Host Room", fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun HeroBanner(
  activeIgn: String,
  isHost: Boolean,
  totalRooms: Int
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(
        Brush.horizontalGradient(
          listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF1A103C))
        )
      )
      .border(1.dp, Brush.horizontalGradient(listOf(CyberCyan, NeonPurple)), RoundedCornerShape(16.dp))
      .padding(18.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(if (isHost) NeonPurple else CyberCyan),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isHost) Icons.Default.Star else Icons.Default.SportsEsports,
              contentDescription = null,
              tint = Color(0xFF0F172A),
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = activeIgn,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = if (isHost) "Tournament Host Role" else "Player / Competitor",
              style = MaterialTheme.typography.bodySmall,
              color = if (isHost) NeonPurple else CyberCyan
            )
          }
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceElevated)
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Text(
            text = "$totalRooms Live Hubs",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = NeonEmerald
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "Select your slot, lock in with your squad, and get instant credentials access when released!",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary,
        lineHeight = 18.sp
      )
    }
  }
}

@Composable
fun GameRoomCard(
  room: GameRoom,
  slots: List<RoomSlot>,
  occupiedCount: Int,
  isUserInRoom: Boolean,
  isHost: Boolean,
  onViewSlots: () -> Unit,
  onManageHost: () -> Unit,
) {
  val fillProgress = if (room.maxPlayers > 0) occupiedCount.toFloat() / room.maxPlayers else 0f

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("room_card_${room.roomId}")
      .clickable { onViewSlots() },
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.verticalGradient(
        listOf(
          if (isUserInRoom) CyberCyan else DarkCardBorder,
          DarkCardBorder
        )
      ),
      width = if (isUserInRoom) 1.5.dp else 1.dp
    )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header: Game Tag & Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(NeonPurple.copy(alpha = 0.2f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = room.gameName.uppercase(),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = NeonPurple
            )
          }

          Spacer(modifier = Modifier.width(8.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(DarkSurfaceElevated)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = room.teamType,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Medium,
              color = CyberCyan
            )
          }
        }

        // Room Status Badge
        val statusColor = when (room.status) {
          RoomStatus.OPEN -> NeonEmerald
          RoomStatus.FULL -> SlotOccupiedRed
          RoomStatus.LIVE -> NeonPurple
          RoomStatus.COMPLETED -> TextMuted
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(statusColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(statusColor)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = room.status.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = statusColor
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Room Title
      Text(
        text = room.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Map & Scheduled Info
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Gamepad, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = room.mapOrMode, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Schedule, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = room.scheduledAt, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Slot Occupancy Bar
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Group, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Slots: $occupiedCount / ${room.maxPlayers}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
          }

          // Credentials Indicator
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (room.credentialsReleased) Icons.Default.LockOpen else Icons.Default.Lock,
              contentDescription = null,
              tint = if (room.credentialsReleased) NeonEmerald else SlotPendingYellow,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = if (room.credentialsReleased) "ID & Pass Released" else "Credentials Locked",
              style = MaterialTheme.typography.labelSmall,
              color = if (room.credentialsReleased) NeonEmerald else SlotPendingYellow
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
          progress = { fillProgress },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = if (fillProgress > 0.9f) SlotOccupiedRed else CyberCyan,
          trackColor = DarkSurfaceElevated,
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Bottom Action Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Host: ${room.hostName}",
          style = MaterialTheme.typography.bodySmall,
          color = TextMuted
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          if (isHost) {
            OutlinedButton(
              onClick = onManageHost,
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPurple)
            ) {
              Text(text = "Host Panel", style = MaterialTheme.typography.labelMedium)
            }
          }

          Button(
            onClick = onViewSlots,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isUserInRoom) NeonEmerald else CyberCyan,
              contentColor = Color(0xFF0F172A)
            )
          ) {
            Text(
              text = if (isUserInRoom) "My Slot Details" else "Select Slot",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
