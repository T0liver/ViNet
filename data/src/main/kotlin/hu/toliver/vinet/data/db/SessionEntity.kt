package hu.toliver.vinet.data.db

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val startUtcMs: Long,
    val endUtcMs: Long?,
    val deviceInfo: String,
    val packId: String,
    val modelVersion: String,
    val notes: String,
    val plotName: String?,
    val maxTrackAge: Int,
    val minTrackFrames: Int,
    @ColumnInfo(name = "perClassMinScore")
    val perClassMinScore: String,
    val mergeDistanceM: Float,
    val mergeWindowS: Float,
    val minSpeedMps: Float,
    val segmentLengthM: Float,
    val mountSide: String,
    val mountOffsetM: Float,
    val maxAccuracyM: Float,
    val weakAccuracyM: Float,
    val bestFrameConfidence: Float,
    val bestFrameBoxArea: Float,
    val bestFrameFullyInside: Float,
    val bestFrameSharpness: Float,
)
