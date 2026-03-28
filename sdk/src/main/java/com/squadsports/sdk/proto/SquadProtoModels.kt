// DO NOT EDIT — Generated from proto/withyoursquad/v2/*.proto
// These Kotlin data classes mirror the protobuf message definitions.
// When buf generate is set up, replace with actual protobuf-kotlin output.

package com.squadsports.sdk.proto

import java.util.Date

// ============================================================
// user.proto
// ============================================================

enum class UserLabel { UNSPECIFIED, EMCEE, TEST_USER, EMAIL_USER }

data class User(
    val id: String? = null,
    val phone: String = "",
    val displayName: String = "",
    val status: String = "",
    val firebaseId: String? = null,
    val birthday: String? = null,
    val zipCode: String? = null,
    val imageUrl: String? = null,
    val squadLevel: Int? = null,
    val community: String? = null,
    val invitedBy: String? = null,
    val labels: List<UserLabel> = emptyList(),
    val email: String? = null,
)

data class Users(val users: List<User> = emptyList())

// ============================================================
// auth.proto
// ============================================================

enum class CreateSessionStatus {
    UNSPECIFIED, ACTIVE, CODE_SENT, INVALID_PHONE, INVALID_CODE,
    SERVICE_UNREACHABLE, INVALID_REQUEST, INVALID_EMAIL,
    EMAIL_CODE_SENT, EMAIL_SERVICE_UNREACHABLE,
}

data class CreateSessionRequest(
    val phone: String? = null,
    val code: String? = null,
    val email: String? = null,
)

data class CreateSessionResponse(
    val status: CreateSessionStatus = CreateSessionStatus.UNSPECIFIED,
    val accessToken: String? = null,
)

// ============================================================
// connection.proto
// ============================================================

enum class ConnectionStatus {
    UNSPECIFIED, INVITED, ACCEPTED, REJECTED, RETRACTED, DELETED, BLOCKED,
}

data class Connection(
    val id: String? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val creator: User? = null,
    val recipient: User? = null,
    val status: ConnectionStatus = ConnectionStatus.UNSPECIFIED,
    val statusBy: String? = null,
    val unreadMessages: Int = 0,
    val lastMessageSentAt: Date? = null,
)

data class Squad(val connections: List<Connection> = emptyList())

// ============================================================
// message.proto
// ============================================================

data class MessageMetadata(
    val levels: List<Float> = emptyList(),
    val duration: Float = 0f,
)

data class MessageReaction(
    val id: String? = null,
    val createdAt: Date? = null,
    val creator: User? = null,
    val reactedTo: Boolean = false,
    val listenedTo: Boolean = false,
    val emojiLabel: String = "",
)

data class Message(
    val id: String? = null,
    val createdAt: Date? = null,
    val creator: User? = null,
    val recipient: User? = null,
    val contentUrl: String = "",
    val mimeType: String = "",
    val metadata: MessageMetadata? = null,
    val flagged: Boolean = false,
    val reactions: List<MessageReaction> = emptyList(),
)

data class Conversation(val messages: List<Message> = emptyList())

// ============================================================
// community.proto
// ============================================================

data class Community(
    val id: String? = null,
    val name: String = "",
    val imageUri: String = "",
    val active: Boolean = false,
    val color: String = "",
    val code: String = "",
    val secondaryColor: String = "",
    val labels: String = "",
)

data class Communities(val communities: List<Community> = emptyList())

// ============================================================
// feed.proto
// ============================================================

data class Freestyle(
    val id: String? = null,
    val createdAt: Date? = null,
    val creator: User? = null,
    val contentUrl: String = "",
    val duration: Float = 0f,
    val listenCount: Int = 0,
    val reactionCount: Int = 0,
    val promptText: String? = null,
    val reactions: List<FreestyleReaction> = emptyList(),
)

data class FreestyleReaction(
    val id: String? = null,
    val creator: User? = null,
    val emojiLabel: String = "",
)

data class Feed(val freestyles: List<Freestyle> = emptyList())

// ============================================================
// poll.proto
// ============================================================

data class Poll(
    val id: String? = null,
    val question: String = "",
    val options: List<PollOption> = emptyList(),
    val totalVotes: Int = 0,
)

data class PollOption(
    val id: String? = null,
    val text: String = "",
    val percentage: Double = 0.0,
)

data class PollResponse(
    val id: String? = null,
    val optionId: String = "",
    val creator: User? = null,
)

data class PollFeed(val polls: List<Poll> = emptyList())

// ============================================================
// wallet.proto + coupon.proto + brand.proto
// ============================================================

data class Wallet(
    val id: String? = null,
    val userId: String? = null,
    val balance: Int = 0,
)

data class Coupon(
    val id: String? = null,
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val status: String = "",
    val expiresAt: Date? = null,
)

data class Coupons(val coupons: List<Coupon> = emptyList())

data class Brand(
    val id: String? = null,
    val name: String = "",
    val imageUrl: String = "",
)

data class Brands(val brands: List<Brand> = emptyList())

// ============================================================
// events.proto
// ============================================================

data class Event(
    val id: String? = null,
    val title: String = "",
    val description: String = "",
    val startDate: Date? = null,
    val location: String = "",
    val attendeeCount: Int = 0,
)

data class Events(val events: List<Event> = emptyList())

// ============================================================
// misc
// ============================================================

data class InvitationCode(val code: String? = null)

data class Journey(
    val milestones: Map<String, Boolean> = emptyMap(),
)

data class SquaddieOfTheDay(
    val id: String? = null,
    val userId: String? = null,
)
