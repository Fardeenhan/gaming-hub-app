package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRoomRepository
import com.example.ui.screens.CreateRoomDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HostPanelScreen
import com.example.ui.screens.MatchResultScreen
import com.example.ui.screens.SlotGridScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

  private val repository = GameRoomRepository()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        GameRoomApp(repository = repository)
      }
    }
  }
}

sealed class NavDestination(val route: String, val title: String, val icon: ImageVector) {
  object Rooms : NavDestination("rooms", "Rooms", Icons.Default.SportsEsports)
  object Slots : NavDestination("slots", "Slot Grid", Icons.Default.GridOn)
  object Host : NavDestination("host", "Host Panel", Icons.Default.AdminPanelSettings)
  object Proof : NavDestination("proof", "Match Proof", Icons.Default.Leaderboard)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameRoomApp(repository: GameRoomRepository) {
  var currentTabIndex by remember { mutableIntStateOf(0) }
  var showCreateDialog by remember { mutableStateOf(false) }

  val selectedRoomId by repository.selectedRoomId.collectAsState()
  val isHostMode by repository.isHostMode.collectAsState()
  val activeUser by repository.activeUser.collectAsState()
  val rooms by repository.rooms.collectAsState()

  val effectiveRoomId = selectedRoomId ?: rooms.firstOrNull()?.roomId ?: "room_pubg_01"

  val navItems = listOf(
    NavDestination.Rooms,
    NavDestination.Slots,
    NavDestination.Host,
    NavDestination.Proof
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isHostMode) NeonPurple else CyberCyan),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.SportsEsports,
                contentDescription = null,
                tint = Color(0xFF0F172A),
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "GameRoom Sync",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = "Match Hub • ${if (isHostMode) "Host Mode" else "Player Mode"}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isHostMode) NeonPurple else CyberCyan
              )
            }
          }
        },
        actions = {
          // Switch between Player Role and Host Role to test both perspectives easily!
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(DarkSurfaceElevated)
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(2.dp)
            ) {
              Text(
                text = activeUser.ign,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(6.dp))
              IconButton(
                onClick = { repository.setHostMode(!isHostMode) },
                modifier = Modifier.size(28.dp).testTag("switch_role_button")
              ) {
                Icon(
                  imageVector = Icons.Default.SwitchAccount,
                  contentDescription = "Switch Host/Player Role",
                  tint = if (isHostMode) NeonPurple else CyberCyan,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = DarkSurface,
        contentColor = CyberCyan
      ) {
        navItems.forEachIndexed { index, dest ->
          NavigationBarItem(
            selected = currentTabIndex == index,
            onClick = { currentTabIndex = index },
            icon = { Icon(dest.icon, contentDescription = dest.title) },
            label = {
              Text(
                dest.title,
                fontSize = 11.sp,
                fontWeight = if (currentTabIndex == index) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = CyberCyan,
              selectedTextColor = CyberCyan,
              unselectedIconColor = TextSecondary,
              unselectedTextColor = TextSecondary,
              indicatorColor = CyberCyan.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_tab_${dest.route}")
          )
        }
      }
    },
    containerColor = DarkBackground
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentTabIndex) {
        0 -> HomeScreen(
          repository = repository,
          onOpenRoom = { rId ->
            repository.selectRoom(rId)
            currentTabIndex = 1
          },
          onOpenHostPanel = { rId ->
            repository.selectRoom(rId)
            currentTabIndex = 2
          },
          onCreateRoomClick = { showCreateDialog = true }
        )
        1 -> SlotGridScreen(
          roomId = effectiveRoomId,
          repository = repository,
          onNavigateBack = { currentTabIndex = 0 },
          onOpenHostPanel = { rId ->
            repository.selectRoom(rId)
            currentTabIndex = 2
          }
        )
        2 -> HostPanelScreen(
          roomId = effectiveRoomId,
          repository = repository,
          onNavigateBack = { currentTabIndex = 0 }
        )
        3 -> MatchResultScreen(repository = repository)
      }
    }

    if (showCreateDialog) {
      CreateRoomDialog(
        repository = repository,
        onDismiss = { showCreateDialog = false },
        onRoomCreated = { newRoomId ->
          showCreateDialog = false
          repository.selectRoom(newRoomId)
          currentTabIndex = 1
        }
      )
    }
  }
}
