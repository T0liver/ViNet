package hu.toliver.vinet.data.db

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [
        SessionEntity::class,
        EventEntity::class,
        SegmentEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class ViNetDatabase : RoomDatabase()
