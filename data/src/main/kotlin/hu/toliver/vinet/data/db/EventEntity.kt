package hu.toliver.vinet.data.db

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
data class EventEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val timestampUtcMs: Long,
    val lat: Double?,
    val lon: Double?,
    val accuracyM: Float?,
    val speedMps: Float?,
    val bearingDeg: Float?,
    val classId: Int,
    val className: String,
    val confidence: Float,
    val trackId: String,
    val boxLeft: Float,
    val boxTop: Float,
    val boxRight: Float,
    val boxBottom: Float,
    val imagePath: String?,
    val thumbPath: String?,
    val plotName: String?,
    val userFeedback: String,
    val packId: String,
    val modelVersion: String,
)
