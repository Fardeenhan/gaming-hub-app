package com.example.model

enum class RoomStatus(val label: String) {
  OPEN("Open"),
  FULL("Full"),
  LIVE("Live Match"),
  COMPLETED("Completed")
}

enum class SlotStatus(val label: String) {
  OPEN("Open"),
  PENDING("Pending Host Approval"),
  APPROVED("Approved / Occupied"),
  KICKED("Kicked"),
  LOCKED("Locked by Host")
}

enum class VerificationStatus(val label: String) {
  PENDING("Pending Verification"),
  APPROVED("Approved & Scored"),
  REJECTED("Rejected Proof")
}

data class GameRoom(
  val roomId: String,
  val hostId: String,
  val hostName: String,
  val gameName: String,
  val title: String,
  val mapOrMode: String,
  val teamType: String,
  val maxPlayers: Int,
  val scheduledAt: String,
  val status: RoomStatus,
  val roomIdCode: String = "",
  val roomPassword: String = "",
  val credentialsReleased: Boolean = false,
  val customRules: String = "Fair play only. No emulators or 3rd-party mods. Join discord for live comms.",
  val createdAt: Long = System.currentTimeMillis(),
)

data class RoomSlot(
  val slotId: String,
  val roomId: String,
  val slotNumber: Int,
  val userId: String? = null,
  val userIgn: String? = null,
  val status: SlotStatus = SlotStatus.OPEN,
  val joinedAt: Long = 0L,
)

data class MatchResult(
  val resultId: String,
  val roomId: String,
  val userId: String,
  val userIgn: String,
  val killsClaimed: Int,
  val screenshotTag: String = "Winner Winner Chicken Dinner #1",
  val status: VerificationStatus = VerificationStatus.PENDING,
  val createdAt: Long = System.currentTimeMillis(),
  val verifiedKills: Int? = null,
)

data class UserProfile(
  val uid: String,
  val name: String,
  val ign: String,
  val phone: String = "+1 (555) 839-2041",
  val avatarColorHex: Long = 0xFF00E5FF,
)
