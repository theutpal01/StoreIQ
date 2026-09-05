package com.storiq.core.database.converters

import androidx.room.TypeConverter
import com.storiq.core.model.MediaCategory
import com.storiq.core.model.CleanupCategory
import com.storiq.core.model.CleanupRisk
import com.storiq.core.model.RecommendationPriority
import com.storiq.core.model.RecommendationType
import com.storiq.core.model.ScanStatus
import com.storiq.core.model.ScanType
import com.storiq.core.model.SwipeDecision
import com.storiq.core.model.SwipeMediaType
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun mediaCategoryToString(category: MediaCategory?): String? = category?.name

    @TypeConverter
    fun stringToMediaCategory(name: String?): MediaCategory? = name?.let { MediaCategory.valueOf(it) }

    @TypeConverter
    fun cleanupCategoryToString(category: CleanupCategory?): String? = category?.name

    @TypeConverter
    fun stringToCleanupCategory(name: String?): CleanupCategory? = name?.let { CleanupCategory.valueOf(it) }

    @TypeConverter
    fun cleanupRiskToString(risk: CleanupRisk?): String? = risk?.name

    @TypeConverter
    fun stringToCleanupRisk(name: String?): CleanupRisk? = name?.let { CleanupRisk.valueOf(it) }

    @TypeConverter
    fun recommendationPriorityToString(priority: RecommendationPriority?): String? = priority?.name

    @TypeConverter
    fun stringToRecommendationPriority(name: String?): RecommendationPriority? = name?.let { RecommendationPriority.valueOf(it) }

    @TypeConverter
    fun recommendationTypeToString(type: RecommendationType?): String? = type?.name

    @TypeConverter
    fun stringToRecommendationType(name: String?): RecommendationType? = name?.let { RecommendationType.valueOf(it) }

    @TypeConverter
    fun scanStatusToString(status: ScanStatus?): String? = status?.name

    @TypeConverter
    fun stringToScanStatus(name: String?): ScanStatus? = name?.let { ScanStatus.valueOf(it) }

    @TypeConverter
    fun scanTypeToString(type: ScanType?): String? = type?.name

    @TypeConverter
    fun stringToScanType(name: String?): ScanType? = name?.let { ScanType.valueOf(it) }

    @TypeConverter
    fun swipeDecisionToString(decision: SwipeDecision?): String? = decision?.name

    @TypeConverter
    fun stringToSwipeDecision(name: String?): SwipeDecision? = name?.let { SwipeDecision.valueOf(it) }

    @TypeConverter
    fun swipeMediaTypeToString(type: SwipeMediaType?): String? = type?.name

    @TypeConverter
    fun stringToSwipeMediaType(name: String?): SwipeMediaType? = name?.let { SwipeMediaType.valueOf(it) }

    @TypeConverter
    fun listToJson(list: List<String>?): String? = list?.let { json.encodeToString(it) }

    @TypeConverter
    fun jsonToList(jsonString: String?): List<String>? = jsonString?.let { json.decodeFromString(it) }
}