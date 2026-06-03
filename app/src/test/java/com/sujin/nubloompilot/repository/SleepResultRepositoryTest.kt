package com.sujin.nubloompilot.repository

import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.models.SleepResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class SleepResultRepositoryTest {

    private lateinit var repository: SleepResultRepository
    private lateinit var fakeLocalStore: FakeSleepSurveyLocalStore

    @Before
    fun setUp() {
        fakeLocalStore = FakeSleepSurveyLocalStore()
        // Firestore is null to avoid real network calls or crashes in unit tests
        repository = SleepResultRepository(
            participantId = "test_user",
            localStore = fakeLocalStore,
            firestore = null
        )
    }

    @Test
    fun `기존 결과가 없으면 새 결과가 그대로 저장됨`() = runBlocking {
        // given
        val newResult = createSleepResult(
            endTime = "2024-06-01T07:00:00Z",
            type = MorningGloryType.TYPE_1
        )

        // when
        repository.saveSleepResult(newResult)

        // then
        val saved = fakeLocalStore.getLatestSavedResult()
        assertEquals(MorningGloryType.TYPE_1, saved?.morningGloryType)
    }

    @Test
    fun `같은 날짜 기존 결과가 있으면 기존 타입 유지`() = runBlocking {
        // given: 오늘 오전 Type 1 저장됨 (UTC 기준 02:00 -> KST 11:00)
        val existingResult = createSleepResult(
            endTime = "2024-06-01T02:00:00Z",
            type = MorningGloryType.TYPE_1
        )
        fakeLocalStore.saveFullSleepResult(existingResult)

        // when: 같은 날 낮잠 Type 2 저장 시도 (UTC 기준 05:00 -> KST 14:00)
        val newResult = createSleepResult(
            endTime = "2024-06-01T05:00:00Z", 
            type = MorningGloryType.TYPE_2
        )
        repository.saveSleepResult(newResult)

        // then: 타입은 기존 Type 1 유지됨
        val saved = fakeLocalStore.getLatestSavedResult()
        assertEquals(MorningGloryType.TYPE_1, saved?.morningGloryType)
    }

    @Test
    fun `같은 날짜 새 타입이 더 좋아도 기존 타입 유지`() = runBlocking {
        // given: 오늘 오전 Type 3 저장됨
        val existingResult = createSleepResult(
            endTime = "2024-06-01T02:00:00Z",
            type = MorningGloryType.TYPE_3
        )
        fakeLocalStore.saveFullSleepResult(existingResult)

        // when: 같은 날 더 좋은 Type 1 저장 시도
        val newResult = createSleepResult(
            endTime = "2024-06-01T05:00:00Z",
            type = MorningGloryType.TYPE_1
        )
        repository.saveSleepResult(newResult)

        // then: 여전히 기존 Type 3 유지 (정책 확인)
        val saved = fakeLocalStore.getLatestSavedResult()
        assertEquals(MorningGloryType.TYPE_3, saved?.morningGloryType)
    }

    @Test
    fun `날짜가 다르면 새 타입 저장`() = runBlocking {
        // given: 어제 결과 Type 2 저장됨
        val yesterdayResult = createSleepResult(
            endTime = "2024-05-31T02:00:00Z",
            type = MorningGloryType.TYPE_2
        )
        fakeLocalStore.saveFullSleepResult(yesterdayResult)

        // when: 오늘 결과 Type 1 저장
        val todayResult = createSleepResult(
            endTime = "2024-06-01T02:00:00Z",
            type = MorningGloryType.TYPE_1
        )
        repository.saveSleepResult(todayResult)

        // then: 날짜가 다르므로 새 Type 1 저장됨
        val saved = fakeLocalStore.getLatestSavedResult()
        assertEquals(MorningGloryType.TYPE_1, saved?.morningGloryType)
    }

    @Test
    fun `같은 날짜 판단은 sleepEndTime 기준`() = runBlocking {
        // 1. 같은 LocalDate인 경우 (기존 타입 유지)
        val baseTime = "2024-06-01T02:00:00Z"
        val existing = createSleepResult(endTime = baseTime, type = MorningGloryType.TYPE_1)
        fakeLocalStore.saveFullSleepResult(existing)

        val sameDay = createSleepResult(
            endTime = "2024-06-01T06:00:00Z",
            type = MorningGloryType.TYPE_2
        )
        repository.saveSleepResult(sameDay)
        assertEquals(MorningGloryType.TYPE_1, fakeLocalStore.getLatestSavedResult()?.morningGloryType)

        // 2. 다른 LocalDate인 경우 (새 타입 저장)
        val differentDay = createSleepResult(
            endTime = "2024-06-02T02:00:00Z",
            type = MorningGloryType.TYPE_4
        )
        repository.saveSleepResult(differentDay)
        assertEquals(MorningGloryType.TYPE_4, fakeLocalStore.getLatestSavedResult()?.morningGloryType)
    }

    private fun createSleepResult(
        endTime: String,
        type: MorningGloryType
    ): SleepResult {
        return SleepResult(
            participantId = "test_user",
            participantName = "Tester",
            sleepEndTime = Instant.parse(endTime),
            sleepDurationMinutes = 480L,
            wakeHeartRate = 70L,
            fatigueLevel = 3,
            morningGloryType = type
        )
    }
}
