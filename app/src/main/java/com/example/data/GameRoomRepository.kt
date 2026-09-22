package com.example.data

import com.example.model.GameRoom
import com.example.model.MatchResult
import com.example.model.RoomSlot
import com.example.model.RoomStatus
import com.example.model.SlotStatus
import com.example.model.UserProfile
import com.example.model.VerificationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameRoomRepository {

  val currentUserPlayer = UserProfile(
    uid = "user_player_001",
    name = "Alex 'Ghost' Vance",
    ign = "GhostSniperX",
    phone = "+1 (555) 304-9811"
  )

  val currentUserHost = UserProfile(
    uid = "user_host_999",
    name = "Commander Rex",
    ign = "TourneyRex_Admin",
    phone = "+1 (555) 720-1940"
  )

  private val _activeUser = MutableStateFlow(currentUserPlayer)
  val activeUser: StateFlow<UserProfile> = _activeUser.asStateFlow()

  private val _isHostMode = MutableStateFlow(false)
  val isHostMode: StateFlow<Boolean> = _isHostMode.asStateFlow()

  private val _rooms = MutableStateFlow<List<GameRoom>>(emptyList())
  val rooms: StateFlow<List<GameRoom>> = _rooms.asStateFlow()

  private val _slotsByRoom = MutableStateFlow<Map<String, List<RoomSlot>>>(emptyMap())
  val slotsByRoom: StateFlow<Map<String, List<RoomSlot>>> = _slotsByRoom.asStateFlow()

  private val _matchResults = MutableStateFlow<List<MatchResult>>(emptyList())
  val matchResults: StateFlow<List<MatchResult>> = _matchResults.asStateFlow()

  private val _selectedRoomId = MutableStateFlow<String?>(null)
  val selectedRoomId: StateFlow<String?> = _selectedRoomId.asStateFlow()

  init {
    seedInitialData()
  }

  private fun seedInitialData() {
    val initialRooms = listOf(
      GameRoom(
        roomId = "room_pubg_01",
        hostId = "user_host_999",
        hostName = "TourneyRex_Admin",
        gameName = "PUBG Mobile",
        title = "Sunday Erangel Pro Scrims #42",
        mapOrMode = "Erangel Classic",
        teamType = "Squad",
        maxPlayers = 100,
        scheduledAt = "Today, 21:00 UTC",
        status = RoomStatus.OPEN,
        roomIdCode = "7294021",
        roomPassword = "CHICKEN_DINNER_24",
        credentialsReleased = true,
        customRules = "Level 3 Helmets on Air-Drops only. Mobile phones only (strictly no iPads/Emulators). Spectator mode enabled."
      ),
      GameRoom(
        roomId = "room_ff_02",
        hostId = "user_host_999",
        hostName = "TourneyRex_Admin",
        gameName = "Free Fire",
        title = "Bermuda Rush Hour Clash",
        mapOrMode = "Bermuda Remastered",
        teamType = "Duo",
        maxPlayers = 48,
        scheduledAt = "Tomorrow, 18:30 UTC",
        status = RoomStatus.OPEN,
        roomIdCode = "551903",
        roomPassword = "BOOYAH_PRO",
        credentialsReleased = false,
        customRules = "Gun attributes disabled. Character skills allowed. Grenades limit 2 per player."
      ),
      GameRoom(
        roomId = "room_tekken_03",
        hostId = "user_player_001",
        hostName = "GhostSniperX",
        gameName = "Tekken 8",
        title = "Colosseum King of the Hill 1v1",
        mapOrMode = "Arena (Underground)",
        teamType = "1v1",
        maxPlayers = 16,
        scheduledAt = "Friday, 20:00 UTC",
        status = RoomStatus.LIVE,
        roomIdCode = "TK8_LOBBY_99",
        roomPassword = "IRON_FIST",
        credentialsReleased = true,
        customRules = "Best of 3 rounds, double elimination bracket. Wired connection required."
      ),
      GameRoom(
        roomId = "room_codm_04",
        hostId = "user_host_999",
        hostName = "TourneyRex_Admin",
        gameName = "COD Mobile",
        title = "Isolated Battle Royale Blitz",
        mapOrMode = "Isolated BR",
        teamType = "Solo",
        maxPlayers = 100,
        scheduledAt = "Tonight, 22:30 UTC",
        status = RoomStatus.OPEN,
        roomIdCode = "COD_99812",
        roomPassword = "NUKE_ZONE",
        credentialsReleased = false,
        customRules = "Custom loadouts available after 1st safe zone collapse."
      )
    )

    _rooms.value = initialRooms
    _selectedRoomId.value = initialRooms.first().roomId

    // Seed slots for each room
    val initialSlotsMap = mutableMapOf<String, List<RoomSlot>>()

    // PUBG 100 slots with realistic sample occupancy
    val pubgSlots = mutableListOf<RoomSlot>()
    for (i in 1..100) {
      val slotStatus = when (i) {
        1 -> SlotStatus.APPROVED // Host slot
        4 -> SlotStatus.APPROVED // Player slot
        7, 8, 9 -> SlotStatus.APPROVED
        12, 13 -> SlotStatus.PENDING
        25, 26, 27, 28 -> SlotStatus.APPROVED
        45 -> SlotStatus.PENDING
        50 -> SlotStatus.LOCKED
        61, 62 -> SlotStatus.APPROVED
        else -> SlotStatus.OPEN
      }

      val (userId, userIgn) = when (i) {
        1 -> "user_host_999" to "TourneyRex_Admin"
        4 -> currentUserPlayer.uid to currentUserPlayer.ign
        7 -> "player_7" to "Viper_AK"
        8 -> "player_8" to "ShadowReaper"
        9 -> "player_9" to "StormBringer"
        12 -> "player_12" to "SniperWolf"
        13 -> "player_13" to "BulletKing"
        25 -> "player_25" to "ApexPredator"
        26 -> "player_26" to "FrostBite"
        27 -> "player_27" to "DeadShot"
        28 -> "player_28" to "NovaBlast"
        45 -> "player_45" to "LoneWolf9"
        61 -> "player_61" to "RedGhost"
        62 -> "player_62" to "CyberPunk"
        else -> null to null
      }

      pubgSlots.add(
        RoomSlot(
          slotId = "slot_pubg_$i",
          roomId = "room_pubg_01",
          slotNumber = i,
          userId = userId,
          userIgn = userIgn,
          status = slotStatus,
          joinedAt = if (userId != null) System.currentTimeMillis() - (1000L * 60 * i) else 0L
        )
      )
    }
    initialSlotsMap["room_pubg_01"] = pubgSlots

    // Free Fire 48 slots
    val ffSlots = (1..48).map { i ->
      val status = if (i <= 6) SlotStatus.APPROVED else if (i == 10) SlotStatus.PENDING else SlotStatus.OPEN
      val ign = if (i <= 6) "FF_Gladiator_$i" else if (i == 10) "RushMaster" else null
      RoomSlot(
        slotId = "slot_ff_$i",
        roomId = "room_ff_02",
        slotNumber = i,
        userId = if (ign != null) "uid_$i" else null,
        userIgn = ign,
        status = status
      )
    }
    initialSlotsMap["room_ff_02"] = ffSlots

    // Tekken 16 slots
    val tekkenSlots = (1..16).map { i ->
      val status = if (i <= 8) SlotStatus.APPROVED else SlotStatus.OPEN
      RoomSlot(
        slotId = "slot_tk_$i",
        roomId = "room_tekken_03",
        slotNumber = i,
        userId = if (i <= 8) "tk_player_$i" else null,
        userIgn = if (i == 1) currentUserPlayer.ign else if (i <= 8) "Mishima_${i}" else null,
        status = status
      )
    }
    initialSlotsMap["room_tekken_03"] = tekkenSlots

    // COD Mobile 100 slots
    val codSlots = (1..100).map { i ->
      val status = if (i in listOf(1, 2, 5, 14, 20)) SlotStatus.APPROVED else SlotStatus.OPEN
      RoomSlot(
        slotId = "slot_cod_$i",
        roomId = "room_codm_04",
        slotNumber = i,
        userId = if (status == SlotStatus.APPROVED) "cod_user_$i" else null,
        userIgn = if (status == SlotStatus.APPROVED) "GhostSpecOps_$i" else null,
        status = status
      )
    }
    initialSlotsMap["room_codm_04"] = codSlots

    _slotsByRoom.value = initialSlotsMap

    // Seed Match Results
    _matchResults.value = listOf(
      MatchResult(
        resultId = "res_001",
        roomId = "room_pubg_01",
        userId = "player_25",
        userIgn = "ApexPredator",
        killsClaimed = 8,
        screenshotTag = "ChickenDinner_Apex_Scoreboard.png",
        status = VerificationStatus.APPROVED,
        verifiedKills = 8
      ),
      MatchResult(
        resultId = "res_002",
        roomId = "room_pubg_01",
        userId = currentUserPlayer.uid,
        userIgn = currentUserPlayer.ign,
        killsClaimed = 5,
        screenshotTag = "Rank2_GhostSniperX_EndGame.jpg",
        status = VerificationStatus.PENDING,
        verifiedKills = null
      ),
      MatchResult(
        resultId = "res_003",
        roomId = "room_pubg_01",
        userId = "player_7",
        userIgn = "Viper_AK",
        killsClaimed = 14,
        screenshotTag = "ViperAK_SuspiciousResult.png",
        status = VerificationStatus.REJECTED,
        verifiedKills = 0
      )
    )
  }

  fun setHostMode(isHost: Boolean) {
    _isHostMode.value = isHost
    _activeUser.value = if (isHost) currentUserHost else currentUserPlayer
  }

  fun selectRoom(roomId: String) {
    _selectedRoomId.value = roomId
  }

  fun createRoom(
    gameName: String,
    title: String,
    mapOrMode: String,
    teamType: String,
    maxPlayers: Int,
    scheduledAt: String,
    customRules: String
  ): GameRoom {
    val newId = "room_${System.currentTimeMillis()}"
    val user = _activeUser.value
    val newRoom = GameRoom(
      roomId = newId,
      hostId = user.uid,
      hostName = user.ign,
      gameName = gameName,
      title = title,
      mapOrMode = mapOrMode,
      teamType = teamType,
      maxPlayers = maxPlayers,
      scheduledAt = scheduledAt,
      status = RoomStatus.OPEN,
      roomIdCode = "ROOM_${(1000..9999).random()}",
      roomPassword = "GAME_${(100..999).random()}",
      credentialsReleased = false,
      customRules = customRules
    )

    // Generate slots
    val slots = (1..maxPlayers).map { i ->
      if (i == 1) {
        RoomSlot(
          slotId = "slot_${newId}_1",
          roomId = newId,
          slotNumber = 1,
          userId = user.uid,
          userIgn = user.ign,
          status = SlotStatus.APPROVED,
          joinedAt = System.currentTimeMillis()
        )
      } else {
        RoomSlot(
          slotId = "slot_${newId}_$i",
          roomId = newId,
          slotNumber = i,
          status = SlotStatus.OPEN
        )
      }
    }

    _rooms.update { listOf(newRoom) + it }
    _slotsByRoom.update { it + (newId to slots) }
    _selectedRoomId.value = newId
    return newRoom
  }

  fun reserveSlot(roomId: String, slotNumber: Int, userIgn: String, requireApproval: Boolean = true): Boolean {
    val user = _activeUser.value
    val currentSlots = _slotsByRoom.value[roomId] ?: return false
    val targetSlot = currentSlots.find { it.slotNumber == slotNumber } ?: return false

    if (targetSlot.status != SlotStatus.OPEN) return false

    // Check if user already in a slot in this room
    val alreadyInSlot = currentSlots.any { it.userId == user.uid && it.status in listOf(SlotStatus.APPROVED, SlotStatus.PENDING) }
    if (alreadyInSlot) return false

    val updatedSlots = currentSlots.map { slot ->
      if (slot.slotNumber == slotNumber) {
        slot.copy(
          userId = user.uid,
          userIgn = userIgn,
          status = if (requireApproval) SlotStatus.PENDING else SlotStatus.APPROVED,
          joinedAt = System.currentTimeMillis()
        )
      } else slot
    }

    _slotsByRoom.update { it + (roomId to updatedSlots) }
    return true
  }

  fun leaveSlot(roomId: String, slotNumber: Int) {
    val user = _activeUser.value
    val currentSlots = _slotsByRoom.value[roomId] ?: return
    val updatedSlots = currentSlots.map { slot ->
      if (slot.slotNumber == slotNumber && slot.userId == user.uid) {
        slot.copy(userId = null, userIgn = null, status = SlotStatus.OPEN, joinedAt = 0L)
      } else slot
    }
    _slotsByRoom.update { it + (roomId to updatedSlots) }
  }

  fun kickPlayer(roomId: String, slotNumber: Int) {
    val currentSlots = _slotsByRoom.value[roomId] ?: return
    val updatedSlots = currentSlots.map { slot ->
      if (slot.slotNumber == slotNumber) {
        slot.copy(userId = null, userIgn = null, status = SlotStatus.OPEN, joinedAt = 0L)
      } else slot
    }
    _slotsByRoom.update { it + (roomId to updatedSlots) }
  }

  fun approveSlot(roomId: String, slotNumber: Int) {
    val currentSlots = _slotsByRoom.value[roomId] ?: return
    val updatedSlots = currentSlots.map { slot ->
      if (slot.slotNumber == slotNumber && slot.status == SlotStatus.PENDING) {
        slot.copy(status = SlotStatus.APPROVED)
      } else slot
    }
    _slotsByRoom.update { it + (roomId to updatedSlots) }
  }

  fun rejectSlot(roomId: String, slotNumber: Int) {
    kickPlayer(roomId, slotNumber)
  }

  fun toggleLockSlot(roomId: String, slotNumber: Int) {
    val currentSlots = _slotsByRoom.value[roomId] ?: return
    val updatedSlots = currentSlots.map { slot ->
      if (slot.slotNumber == slotNumber) {
        val newStatus = if (slot.status == SlotStatus.LOCKED) SlotStatus.OPEN else SlotStatus.LOCKED
        slot.copy(userId = null, userIgn = null, status = newStatus)
      } else slot
    }
    _slotsByRoom.update { it + (roomId to updatedSlots) }
  }

  fun updateCredentials(roomId: String, code: String, pass: String) {
    _rooms.update { roomList ->
      roomList.map { room ->
        if (room.roomId == roomId) {
          room.copy(roomIdCode = code, roomPassword = pass)
        } else room
      }
    }
  }

  fun releaseCredentials(roomId: String, release: Boolean) {
    _rooms.update { roomList ->
      roomList.map { room ->
        if (room.roomId == roomId) {
          room.copy(credentialsReleased = release)
        } else room
      }
    }
  }

  fun updateRoomStatus(roomId: String, status: RoomStatus) {
    _rooms.update { roomList ->
      roomList.map { room ->
        if (room.roomId == roomId) {
          room.copy(status = status)
        } else room
      }
    }
  }

  fun submitMatchResult(roomId: String, kills: Int, screenshotName: String) {
    val user = _activeUser.value
    val newResult = MatchResult(
      resultId = "res_${System.currentTimeMillis()}",
      roomId = roomId,
      userId = user.uid,
      userIgn = user.ign,
      killsClaimed = kills,
      screenshotTag = screenshotName,
      status = VerificationStatus.PENDING
    )
    _matchResults.update { listOf(newResult) + it }
  }

  fun verifyMatchResult(resultId: String, approve: Boolean, verifiedKills: Int?) {
    _matchResults.update { list ->
      list.map { res ->
        if (res.resultId == resultId) {
          res.copy(
            status = if (approve) VerificationStatus.APPROVED else VerificationStatus.REJECTED,
            verifiedKills = if (approve) (verifiedKills ?: res.killsClaimed) else 0
          )
        } else res
      }
    }
  }
}
