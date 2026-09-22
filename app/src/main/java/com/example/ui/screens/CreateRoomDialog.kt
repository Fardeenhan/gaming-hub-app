package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.GameRoomRepository
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateRoomDialog(
  repository: GameRoomRepository,
  onDismiss: () -> Unit,
  onRoomCreated: (String) -> Unit
) {
  val games = listOf("PUBG Mobile", "Free Fire", "Tekken 8", "COD Mobile", "BGMI")
  val teamTypes = listOf("Solo", "Duo", "Squad", "1v1")
  val playerLimits = listOf(16, 20, 48, 100)

  var selectedGame by remember { mutableStateOf(games.first()) }
  var title by remember { mutableStateOf("Championship Customs #1") }
  var mapOrMode by remember { mutableStateOf("Classic Erangel") }
  var selectedTeamType by remember { mutableStateOf("Squad") }
  var selectedMaxPlayers by remember { mutableIntStateOf(100) }
  var scheduledAt by remember { mutableStateOf("Tonight, 21:00 UTC") }
  var customRules by remember {
    mutableStateOf("Mobile devices only (no emulators/triggers). Level 3 helmet in air-drops only. Report hacks to host.")
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = DarkSurface,
    shape = RoundedCornerShape(16.dp),
    title = {
      Text(
        text = "Host Custom Game Room",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Game Selector
        Text("Select Game", style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontWeight = FontWeight.Bold)
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          games.forEach { game ->
            val isSelected = selectedGame == game
            ElevatedFilterChip(
              selected = isSelected,
              onClick = {
                selectedGame = game
                when (game) {
                  "PUBG Mobile" -> {
                    mapOrMode = "Erangel Classic"
                    selectedMaxPlayers = 100
                    selectedTeamType = "Squad"
                  }
                  "Free Fire" -> {
                    mapOrMode = "Bermuda Rush"
                    selectedMaxPlayers = 48
                    selectedTeamType = "Duo"
                  }
                  "Tekken 8" -> {
                    mapOrMode = "Arena Stage"
                    selectedMaxPlayers = 16
                    selectedTeamType = "1v1"
                  }
                  "COD Mobile" -> {
                    mapOrMode = "Isolated BR"
                    selectedMaxPlayers = 100
                    selectedTeamType = "Squad"
                  }
                }
              },
              label = { Text(game) },
              colors = FilterChipDefaults.elevatedFilterChipColors(
                selectedContainerColor = NeonPurple,
                selectedLabelColor = Color.White,
                containerColor = DarkSurfaceElevated,
                labelColor = TextSecondary
              )
            )
          }
        }

        // Room Title
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Room Title") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("create_room_title_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyberCyan,
            unfocusedBorderColor = DarkCardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )

        // Map / Mode
        OutlinedTextField(
          value = mapOrMode,
          onValueChange = { mapOrMode = it },
          label = { Text("Map / Mode") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyberCyan,
            unfocusedBorderColor = DarkCardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )

        // Team Type
        Text("Team Format", style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          teamTypes.forEach { type ->
            val isSelected = selectedTeamType == type
            ElevatedFilterChip(
              selected = isSelected,
              onClick = { selectedTeamType = type },
              label = { Text(type) },
              colors = FilterChipDefaults.elevatedFilterChipColors(
                selectedContainerColor = CyberCyan,
                selectedLabelColor = Color(0xFF0F172A),
                containerColor = DarkSurfaceElevated,
                labelColor = TextSecondary
              )
            )
          }
        }

        // Max Players / Slots
        Text("Max Slots (1 to N)", style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          playerLimits.forEach { limit ->
            val isSelected = selectedMaxPlayers == limit
            ElevatedFilterChip(
              selected = isSelected,
              onClick = { selectedMaxPlayers = limit },
              label = { Text("$limit Slots") },
              colors = FilterChipDefaults.elevatedFilterChipColors(
                selectedContainerColor = CyberCyan,
                selectedLabelColor = Color(0xFF0F172A),
                containerColor = DarkSurfaceElevated,
                labelColor = TextSecondary
              )
            )
          }
        }

        // Scheduled Time
        OutlinedTextField(
          value = scheduledAt,
          onValueChange = { scheduledAt = it },
          label = { Text("Scheduled Time") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyberCyan,
            unfocusedBorderColor = DarkCardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )

        // Custom Rules
        OutlinedTextField(
          value = customRules,
          onValueChange = { customRules = it },
          label = { Text("Custom Rules") },
          maxLines = 3,
          modifier = Modifier.fillMaxWidth(),
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
          if (title.isNotBlank()) {
            val newRoom = repository.createRoom(
              gameName = selectedGame,
              title = title.trim(),
              mapOrMode = mapOrMode.trim(),
              teamType = selectedTeamType,
              maxPlayers = selectedMaxPlayers,
              scheduledAt = scheduledAt.trim(),
              customRules = customRules.trim()
            )
            onRoomCreated(newRoom.roomId)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF0F172A)),
        modifier = Modifier.testTag("submit_create_room")
      ) {
        Text("Create & Host", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    }
  )
}
