package com.example.hampouch.data.repository

import android.content.Context
import com.example.hampouch.domain.model.DailyRecordStatus
import com.example.hampouch.domain.model.EmotionStat
import com.example.hampouch.domain.model.PendingChallengeResult
import com.example.hampouch.domain.model.SpendingEmotion
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "hampouch_pending_challenge_result"
private const val KEY_USER_ID = "user_id"
private const val KEY_CHALLENGE_ID = "challenge_id"
private const val KEY_TITLE = "title"
private const val KEY_PERIOD_START = "period_start"
private const val KEY_PERIOD_END = "period_end"
private const val KEY_TOTAL_DAYS = "total_days"
private const val KEY_SUCCESS_DAYS = "success_days"
private const val KEY_STREAK_DAYS = "streak_days"
private const val KEY_AMOUNT_VALUE = "amount_value"
private const val KEY_GOAL_AMOUNT = "goal_amount"
private const val KEY_ACTUAL_AMOUNT = "actual_amount"
private const val KEY_DAILY_LIMIT = "daily_limit"
private const val KEY_EMOTION_STATS = "emotion_stats"
private const val KEY_DAILY_RECORDS = "daily_records"
private const val SEPARATOR = "|"

@Singleton
class PendingChallengeResultStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _pending = MutableStateFlow(readPending())
    val pending: StateFlow<PendingChallengeResult?> = _pending.asStateFlow()

    fun save(value: PendingChallengeResult) {
        preferences.edit()
            .putLong(KEY_USER_ID, value.userId)
            .putString(KEY_CHALLENGE_ID, value.challengeId)
            .putString(KEY_TITLE, value.title)
            .putLong(KEY_PERIOD_START, value.periodStart.toEpochDay())
            .putLong(KEY_PERIOD_END, value.periodEnd.toEpochDay())
            .putInt(KEY_TOTAL_DAYS, value.totalDays)
            .putInt(KEY_SUCCESS_DAYS, value.successDays)
            .putInt(KEY_STREAK_DAYS, value.streakDays)
            .putLong(KEY_AMOUNT_VALUE, value.amountValue)
            .putInt(KEY_GOAL_AMOUNT, value.goalAmount)
            .putLong(KEY_ACTUAL_AMOUNT, value.actualAmount)
            .putInt(KEY_DAILY_LIMIT, value.dailyLimit)
            .putStringSet(KEY_EMOTION_STATS, value.emotionStats.map(::encodeEmotionStat).toSet())
            .putStringSet(KEY_DAILY_RECORDS, value.dailyRecords.map(::encodeDailyRecord).toSet())
            .commit()
        _pending.value = value
    }

    fun clear() {
        preferences.edit().clear().commit()
        _pending.value = null
    }

    private fun readPending(): PendingChallengeResult? {
        val challengeId = preferences.getString(KEY_CHALLENGE_ID, null) ?: return null
        val title = preferences.getString(KEY_TITLE, null) ?: return null
        if (requiredKeys.any { !preferences.contains(it) }) return null
        val periodStart = LocalDate.ofEpochDay(preferences.getLong(KEY_PERIOD_START, 0L))
        val periodEnd = LocalDate.ofEpochDay(preferences.getLong(KEY_PERIOD_END, 0L))
        val storedTotalDays = preferences.getInt(KEY_TOTAL_DAYS, 0)
        val totalDays = storedTotalDays.takeIf { it > 0 }
            ?: (ChronoUnit.DAYS.between(periodStart, periodEnd).toInt() + 1).coerceAtLeast(1)
        return PendingChallengeResult(
            userId = preferences.getLong(KEY_USER_ID, 0L),
            challengeId = challengeId,
            title = if (storedTotalDays > 0) title else "${totalDays}일 챌린지",
            periodStart = periodStart,
            periodEnd = periodEnd,
            totalDays = totalDays,
            successDays = preferences.getInt(KEY_SUCCESS_DAYS, 0),
            streakDays = preferences.getInt(KEY_STREAK_DAYS, 0),
            amountValue = preferences.getLong(KEY_AMOUNT_VALUE, 0L),
            goalAmount = preferences.getInt(KEY_GOAL_AMOUNT, 0),
            actualAmount = preferences.getLong(KEY_ACTUAL_AMOUNT, 0L),
            dailyLimit = preferences.getInt(KEY_DAILY_LIMIT, 0),
            emotionStats = readEmotionStats(),
            dailyRecords = readDailyRecords()
        )
    }

    private fun readEmotionStats(): List<EmotionStat> {
        val encoded = preferences.getStringSet(KEY_EMOTION_STATS, emptySet()).orEmpty()
        val statsByEmotion = encoded.mapNotNull(::decodeEmotionStat).associateBy { it.emotion }
        return SpendingEmotion.entries.mapNotNull(statsByEmotion::get)
    }

    private fun readDailyRecords(): Map<LocalDate, DailyRecordStatus> =
        preferences.getStringSet(KEY_DAILY_RECORDS, emptySet())
            .orEmpty()
            .mapNotNull(::decodeDailyRecord)
            .toMap()

    private fun encodeEmotionStat(stat: EmotionStat): String =
        listOf(stat.emotion.name, stat.percent, stat.amount).joinToString(SEPARATOR)

    private fun decodeEmotionStat(encoded: String): EmotionStat? = runCatching {
        val (emotion, percent, amount) = encoded.split(SEPARATOR)
        EmotionStat(SpendingEmotion.valueOf(emotion), percent.toInt(), amount.toLong())
    }.getOrNull()

    private fun encodeDailyRecord(entry: Map.Entry<LocalDate, DailyRecordStatus>): String =
        listOf(entry.key.toEpochDay(), entry.value.name).joinToString(SEPARATOR)

    private fun decodeDailyRecord(encoded: String): Pair<LocalDate, DailyRecordStatus>? = runCatching {
        val (epochDay, status) = encoded.split(SEPARATOR)
        LocalDate.ofEpochDay(epochDay.toLong()) to DailyRecordStatus.valueOf(status)
    }.getOrNull()

    private companion object {
        val requiredKeys = listOf(
            KEY_USER_ID,
            KEY_PERIOD_START,
            KEY_PERIOD_END,
            KEY_TOTAL_DAYS,
            KEY_SUCCESS_DAYS,
            KEY_STREAK_DAYS,
            KEY_AMOUNT_VALUE,
            KEY_GOAL_AMOUNT,
            KEY_ACTUAL_AMOUNT,
            KEY_DAILY_LIMIT,
            KEY_EMOTION_STATS,
            KEY_DAILY_RECORDS
        )
    }
}
