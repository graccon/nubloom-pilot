package com.sujin.nubloompilot.debug

import com.sujin.nubloompilot.models.InterventionActionType
import com.sujin.nubloompilot.models.InterventionType
import com.sujin.nubloompilot.models.MorningGloryType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class DemoIntervention(
    val id: String,
    val title: String,
    val description: String,
    val type: String,
    val actionType: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)

data class DemoHomeScenario(
    val yesterdayShift: String,
    val todayShift: String,
    val tomorrowShift: String,
    val dayAfterTomorrowShift: String?,
    val currentTime: LocalDateTime,
    val morningGloryType: MorningGloryType,
    val message: String,
    val interventions: List<DemoIntervention>
)

/**
 * Home 화면 데모용 시나리오 생성기.
 *
 * - Health Connect / Firestore / LocalStore를 건드리지 않음
 * - HomePage 표시용 데이터만 생성
 * - 근무 상황은 기준 시간표 결정
 * - 크로노타입은 개입 시간/종류 조정
 * - 피로 상태는 개입 강도 조정
 */
object DemoHomeScenarioFactory {

    fun create(state: DemoHomeControlState): DemoHomeScenario {
        val today = LocalDate.now()

        return when (state.workScenario) {
            DemoWorkScenario.FIRST_NIGHT -> createFirstNightScenario(today, state)
            DemoWorkScenario.CONSECUTIVE_DAY -> createConsecutiveDayScenario(today, state)
        }
    }

    private fun createFirstNightScenario(
        today: LocalDate,
        state: DemoHomeControlState
    ): DemoHomeScenario {
        return DemoHomeScenario(
            yesterdayShift = "OFF",
            todayShift = "N",
            tomorrowShift = "N",
            dayAfterTomorrowShift = "OFF",
            currentTime = LocalDateTime.of(today, LocalTime.of(14, 0)),
            morningGloryType = if (state.condition == DemoCondition.TIRED) {
                MorningGloryType.TYPE_4
            } else {
                MorningGloryType.TYPE_2
            },
            message = buildFirstNightMessage(state),
            interventions = buildFirstNightInterventions(today, state)
        )
    }

    private fun createConsecutiveDayScenario(
        today: LocalDate,
        state: DemoHomeControlState
    ): DemoHomeScenario {
        return DemoHomeScenario(
            yesterdayShift = "D",
            todayShift = "D",
            tomorrowShift = "D",
            dayAfterTomorrowShift = "OFF",
            currentTime = LocalDateTime.of(today, LocalTime.of(18, 0)),
            morningGloryType = if (state.condition == DemoCondition.TIRED) {
                MorningGloryType.TYPE_3
            } else {
                MorningGloryType.TYPE_1
            },
            message = buildConsecutiveDayMessage(state),
            interventions = buildConsecutiveDayInterventions(today, state)
        )
    }

    private fun buildFirstNightMessage(state: DemoHomeControlState): String {
        val chronotypeMessage = when (state.chronotype) {
            DemoChronotype.MORNING ->
                "아침형 리듬은 밤 시간대 졸림이 빨리 올 수 있어요."
            DemoChronotype.INTERMEDIATE ->
                "중간형 리듬은 낮잠과 빛 노출을 통해 밤 근무 쪽으로 조금 조정할 수 있어요."
            DemoChronotype.EVENING ->
                "저녁형 리듬은 야간근무 적응 여지가 있지만, 퇴근 후 빛 차단은 여전히 중요해요."
        }

        val conditionMessage = when (state.condition) {
            DemoCondition.TIRED ->
                "피로가 높은 상태라 첫 야간근무 전 회복과 각성 준비가 모두 필요해요."
            DemoCondition.FRESH ->
                "컨디션은 비교적 안정적이지만, 첫 야간근무 전에는 리듬을 늦추는 준비가 필요해요."
        }

        return "$chronotypeMessage $conditionMessage"
    }

    private fun buildConsecutiveDayMessage(state: DemoHomeControlState): String {
        val chronotypeMessage = when (state.chronotype) {
            DemoChronotype.MORNING ->
                "아침형 리듬은 데이 근무와 잘 맞아 수면 루틴을 안정적으로 유지하기 좋습니다."
            DemoChronotype.INTERMEDIATE ->
                "중간형 리듬은 저녁 빛과 카페인을 줄이면 내일 아침 기상이 더 수월해질 수 있어요."
            DemoChronotype.EVENING ->
                "저녁형 리듬은 이른 기상에 부담이 커서 저녁 빛과 카페인을 더 적극적으로 줄여야 해요."
        }

        val conditionMessage = when (state.condition) {
            DemoCondition.TIRED ->
                "연속 데이 근무로 회복이 부족할 수 있어 오늘 저녁 수면 준비를 앞당기는 것이 중요해요."
            DemoCondition.FRESH ->
                "컨디션은 안정적이지만, 내일도 데이 근무가 이어지므로 리듬을 유지하는 것이 좋아요."
        }

        return "$chronotypeMessage $conditionMessage"
    }

    private fun buildFirstNightInterventions(
        today: LocalDate,
        state: DemoHomeControlState
    ): List<DemoIntervention> {
        return buildList {
            when (state.chronotype) {
                DemoChronotype.MORNING -> {
                    addNap(
                        id = "first_night_morning_nap",
                        today = today,
                        start = LocalTime.of(15, 0),
                        end = LocalTime.of(16, 30),
                        title = "첫 나이트 전 90분 낮잠",
                        description = "아침형은 밤에 졸림이 빨리 올 수 있어요. 근무 전 낮잠으로 수면 압력을 낮춰주세요."
                    )

                    addLightDo(
                        id = "first_night_morning_light",
                        today = today,
                        start = LocalTime.of(19, 0),
                        end = LocalTime.of(21, 30),
                        title = "밝은 빛으로 리듬 늦추기",
                        description = "첫 나이트 전 밝은 빛은 각성도를 높이고 몸의 리듬을 밤 근무 쪽으로 늦추는 데 도움이 돼요. 출근 전에는 밝은 실내 조명 아래에서 준비하며 각성도를 유지해주세요."
                    )

                    addCaffeineDo(
                        id = "first_night_morning_caffeine",
                        startDate = today,
                        start = LocalTime.of(22, 30),
                        endDate = today.plusDays(1),
                        end = LocalTime.of(1, 30),
                        title = "근무 초반 카페인 사용",
                        description = "카페인은 밤 근무 초반에만 사용하세요. 새벽 후반 카페인은 퇴근 후 수면을 방해할 수 있어요."
                    )
                }

                DemoChronotype.INTERMEDIATE -> {
                    addNap(
                        id = "first_night_intermediate_nap",
                        today = today,
                        start = LocalTime.of(15, 30),
                        end = LocalTime.of(16, 30),
                        title = "짧은 회복 낮잠",
                        description = "첫 나이트 전에는 짧은 낮잠으로 밤 근무 중 졸림을 줄이는 것이 좋아요."
                    )

//                    addLightDo(
//                        id = "first_night_intermediate_light",
//                        today = today,
//                        start = LocalTime.of(19, 30),
//                        end = LocalTime.of(21, 30),
//                        title = "밝은 빛 노출",
//                        description = "야간근무 시작 전 밝은 빛을 쬐면 졸림을 줄이고 리듬을 조금 늦출 수 있어요."
//                    )

                    addCaffeineDo(
                        id = "first_night_intermediate_caffeine",
                        startDate = today,
                        start = LocalTime.of(23, 0),
                        endDate = today.plusDays(1),
                        end = LocalTime.of(1, 30),
                        title = "카페인은 근무 초반에만",
                        description = "각성이 필요한 시간대에만 카페인을 사용하고, 새벽 후반에는 피해주세요."
                    )
                }

                DemoChronotype.EVENING -> {
                    addLightDo(
                        id = "first_night_evening_light",
                        today = today,
                        start = LocalTime.of(20, 30),
                        end = LocalTime.of(22, 30),
                        title = "밤 근무 전 각성 유지",
                        description = "저녁형은 야간근무 적응 여지가 있지만, 출근 전 밝은 빛으로 각성 리듬을 안정적으로 유지하는 것이 좋아요."
                    )

                    addCaffeineDo(
                        id = "first_night_evening_caffeine",
                        startDate = today,
                        start = LocalTime.of(23, 30),
                        endDate = today.plusDays(1),
                        end = LocalTime.of(1, 30),
                        title = "각성이 필요할 때 카페인 사용",
                        description = "밤 각성이 비교적 유지될 수 있어요. 과한 카페인보다 필요한 시간대에만 사용하는 것이 좋습니다."
                    )
                }
            }

            if (state.condition == DemoCondition.TIRED) {
                addLightAvoid(
                    id = "first_night_tired_light_avoid",
                    startDate = today.plusDays(1),
                    start = LocalTime.of(7, 0),
                    endDate = today.plusDays(1),
                    end = LocalTime.of(9, 0),
                    title = "퇴근길 빛 피하기",
                    description = "피로가 높은 상태에서는 퇴근 후 빠르게 회복 수면으로 들어가는 것이 중요해요. 선글라스나 모자로 아침 빛을 줄여주세요."
                )
            }

            addMainSleep(
                id = "first_night_main_sleep",
                startDate = today.plusDays(1),
                start = LocalTime.of(9, 30),
                endDate = today.plusDays(1),
                end = LocalTime.of(15, 30),
                title = "야간근무 후 회복 수면",
                description = "퇴근 후에는 암막 환경에서 회복 수면을 확보해주세요. 첫 나이트 이후의 낮 수면은 다음 근무 적응에도 영향을 줍니다."
            )
        }
    }

    private fun buildConsecutiveDayInterventions(
        today: LocalDate,
        state: DemoHomeControlState
    ): List<DemoIntervention> {
        return buildList {
            when (state.chronotype) {
                DemoChronotype.MORNING -> {
                    val morningCaffeineEnd = if (state.condition == DemoCondition.TIRED) {
                        LocalTime.of(18, 0)
                    } else {
                        LocalTime.of(19, 0)
                    }

                    val morningPreparationStart = if (state.condition == DemoCondition.TIRED) {
                        LocalTime.of(20, 0)
                    } else {
                        LocalTime.of(21, 0)
                    }

                    val morningPreparationEnd = if (state.condition == DemoCondition.TIRED) {
                        LocalTime.of(20, 30)
                    } else {
                        LocalTime.of(21, 40)
                    }

                    val morningSleepStart = if (state.condition == DemoCondition.TIRED) {
                        LocalTime.of(21, 0)
                    } else {
                        LocalTime.of(22, 0)
                    }

                    addCaffeineAvoid(
                        id = "consecutive_day_morning_caffeine",
                        today = today,
                        start = LocalTime.of(13, 0),
                        end = morningCaffeineEnd,
                        title = if (state.condition == DemoCondition.TIRED) {
                            "카페인 더 일찍 중단하기"
                        } else {
                            "오후 카페인 중단"
                        },
                        description = if (state.condition == DemoCondition.TIRED) {
                            "피로가 누적된 상태에서는 카페인이 밤 수면을 더 방해할 수 있어요. 오늘은 카페인을 더 일찍 줄여 회복 수면을 준비해주세요."
                        } else {
                            "아침형은 이른 수면 준비가 비교적 가능하므로, 오후 카페인만 줄여도 주간근무 리듬을 안정적으로 유지할 수 있어요."
                        }
                    )

                    addPreparation(
                        id = "consecutive_day_morning_preparation",
                        today = today,
                        start = morningPreparationStart,
                        end = morningPreparationEnd,
                        title = if (state.condition == DemoCondition.TIRED) {
                            "회복을 위한 수면 준비 앞당기기"
                        } else {
                            "수면 준비 앞당기기"
                        },
                        description = if (state.condition == DemoCondition.TIRED) {
                            "피로가 높은 날에는 수면 준비를 더 앞당겨야 회복 시간이 충분해져요. 조명과 화면을 줄이고 몸을 쉬는 상태로 전환해주세요."
                        } else {
                            "연속 데이 근무에서는 기상 시간이 고정되므로, 평소보다 조금 이른 수면 준비로 회복 시간을 확보해주세요."
                        }
                    )

                    addMainSleep(
                        id = "consecutive_day_morning_sleep",
                        startDate = today,
                        start = morningSleepStart,
                        endDate = today.plusDays(1),
                        end = LocalTime.of(6, 0),
                        title = if (state.condition == DemoCondition.TIRED) {
                            "피로 회복을 위한 이른 목표 수면"
                        } else {
                            "주간근무 리듬 유지 수면"
                        },
                        description = if (state.condition == DemoCondition.TIRED) {
                            "아침형이라도 피로가 누적된 상태에서는 평소보다 더 일찍 잠자리에 드는 것이 좋아요. 내일 데이 근무를 위해 오늘은 21시 40분쯤 수면을 목표로 해보세요."
                        } else {
                            "아침형 리듬을 활용해 오늘은 22시 전후로 수면에 들어가는 것을 목표로 해보세요."
                        }
                    )
                }

                DemoChronotype.INTERMEDIATE -> {
                    addCaffeineAvoid(
                        id = "consecutive_day_intermediate_caffeine",
                        today = today,
                        start = LocalTime.of(13, 0),
                        end = LocalTime.of(20, 0),
                        title = "오후 카페인 줄이기",
                        description = "내일도 데이 근무가 이어지므로 오후 카페인을 줄여 수면 시작이 늦어지지 않게 해주세요."
                    )

                    addLightAvoid(
                        id = "consecutive_day_intermediate_light",
                        startDate = today,
                        start = LocalTime.of(19, 30),
                        endDate = today,
                        end = LocalTime.of(22, 0),
                        title = "저녁 조명 낮추기",
                        description = "저녁 빛을 줄이면 수면 준비 신호가 빨라져 내일 이른 기상 부담을 낮출 수 있어요."
                    )

                    addPreparation(
                        id = "consecutive_day_intermediate_preparation",
                        today = today,
                        start = LocalTime.of(21, 30),
                        end = LocalTime.of(22, 20),
                        title = "수면 전 루틴 시작",
                        description = "휴대폰과 밝은 조명을 줄이고, 내일 데이 근무를 위해 수면 준비를 시작해주세요."
                    )

                    addMainSleep(
                        id = "consecutive_day_intermediate_sleep",
                        startDate = today,
                        start = LocalTime.of(22, 30),
                        endDate = today.plusDays(1),
                        end = LocalTime.of(6, 0),
                        title = "내일 데이를 위한 목표 수면",
                        description = "내일 06:30 출근을 기준으로 충분한 회복 시간을 확보하는 수면 계획이에요."
                    )
                }

                DemoChronotype.EVENING -> {
//                    addLightAvoid(
//                        id = "consecutive_day_evening_light",
//                        startDate = today,
//                        start = LocalTime.of(19, 0),
//                        endDate = today,
//                        end = LocalTime.of(22, 0),
//                        title = "저녁 강한 빛 줄이기",
//                        description = "저녁형은 밤에 각성이 오래 유지되기 쉬워요. 내일 이른 출근을 위해 저녁 빛을 더 일찍 줄여 리듬을 앞당겨주세요."
//                    )

                    addCaffeineAvoid(
                        id = "consecutive_day_evening_caffeine",
                        today = today,
                        start = LocalTime.of(12, 0),
                        end = LocalTime.of(20, 0),
                        title = "카페인 더 일찍 제한하기",
                        description = "저녁형은 취침 시각이 늦어지기 쉬우므로, 카페인을 더 이른 시간부터 제한하는 것이 좋아요."
                    )

                    addPreparation(
                        id = "consecutive_day_evening_preparation",
                        today = today,
                        start = LocalTime.of(21, 20),
                        end = LocalTime.of(22, 20),
                        title = "수면 준비 강하게 시작하기",
                        description = "이른 데이 근무가 이어지는 날에는 저녁형 리듬을 앞당기는 것이 핵심이에요. 조명, 화면, 활동량을 함께 낮춰주세요."
                    )

                    val eveningSleepStart = if (state.condition == DemoCondition.TIRED) {
                        LocalTime.of(22, 0)
                    } else {
                        LocalTime.of(23, 30)
                    }

                    val eveningSleepTitle = if (state.condition == DemoCondition.TIRED) {
                        "피로 회복을 위한 이른 목표 수면"
                    } else {
                        "이른 출근 대비 목표 수면"
                    }

                    val eveningSleepDescription = if (state.condition == DemoCondition.TIRED) {
                        "피로가 누적된 상태에서는 저녁형이라도 수면 시작을 더 앞당기는 것이 중요해요. 내일 데이 근무를 위해 오늘은 22시쯤 잠자리에 드는 것을 목표로 해보세요."
                    } else {
                        "저녁형에게는 이른 취침이 어렵지만, 내일 데이 근무를 위해 오늘은 수면 시작을 조금 앞당기는 것이 필요해요."
                    }

                    addMainSleep(
                        id = "consecutive_day_evening_sleep",
                        startDate = today,
                        start = eveningSleepStart,
                        endDate = today.plusDays(1),
                        end = LocalTime.of(6, 0),
                        title = eveningSleepTitle,
                        description = eveningSleepDescription
                    )
                }
            }

            if (state.condition == DemoCondition.TIRED && state.chronotype != DemoChronotype.MORNING) {
                addLightAvoid(
                    id = "consecutive_day_tired_light_avoid",
                    startDate = today,
                    start = LocalTime.of(18, 30),
                    endDate = today,
                    end = LocalTime.of(20, 0),
                    title = "저녁 햇빛과 밝은 빛 줄이기",
                    description = "피로가 누적된 상태에서는 저녁 시간의 강한 빛이 수면 준비를 늦출 수 있어요. 창가의 강한 햇빛이나 밝은 조명을 줄이고, 몸이 쉴 준비를 할 수 있도록 도와주세요."
                )
            }
        }
    }

    private fun MutableList<DemoIntervention>.addNap(
        id: String,
        today: LocalDate,
        start: LocalTime,
        end: LocalTime,
        title: String,
        description: String
    ) {
        add(
            DemoIntervention(
                id = id,
                title = title,
                description = description,
                type = InterventionType.NAP.name,
                actionType = InterventionActionType.DO.name,
                startTime = LocalDateTime.of(today, start),
                endTime = LocalDateTime.of(today, end)
            )
        )
    }

    private fun MutableList<DemoIntervention>.addLightDo(
        id: String,
        today: LocalDate,
        start: LocalTime,
        end: LocalTime,
        title: String,
        description: String
    ) {
        add(
            DemoIntervention(
                id = id,
                title = title,
                description = description,
                type = InterventionType.LIGHT.name,
                actionType = InterventionActionType.DO.name,
                startTime = LocalDateTime.of(today, start),
                endTime = LocalDateTime.of(today, end)
            )
        )
    }

    private fun MutableList<DemoIntervention>.addLightAvoid(
        id: String,
        startDate: LocalDate,
        start: LocalTime,
        endDate: LocalDate,
        end: LocalTime,
        title: String,
        description: String
    ) {
        add(
            DemoIntervention(
                id = id,
                title = title,
                description = description,
                type = InterventionType.LIGHT.name,
                actionType = InterventionActionType.AVOID.name,
                startTime = LocalDateTime.of(startDate, start),
                endTime = LocalDateTime.of(endDate, end)
            )
        )
    }

    private fun MutableList<DemoIntervention>.addCaffeineDo(
        id: String,
        startDate: LocalDate,
        start: LocalTime,
        endDate: LocalDate,
        end: LocalTime,
        title: String,
        description: String
    ) {
        add(
            DemoIntervention(
                id = id,
                title = title,
                description = description,
                type = InterventionType.CAFFEINE.name,
                actionType = InterventionActionType.DO.name,
                startTime = LocalDateTime.of(startDate, start),
                endTime = LocalDateTime.of(endDate, end)
            )
        )
    }

    private fun MutableList<DemoIntervention>.addCaffeineAvoid(
        id: String,
        today: LocalDate,
        start: LocalTime,
        end: LocalTime,
        title: String,
        description: String
    ) {
        add(
            DemoIntervention(
                id = id,
                title = title,
                description = description,
                type = InterventionType.CAFFEINE.name,
                actionType = InterventionActionType.AVOID.name,
                startTime = LocalDateTime.of(today, start),
                endTime = LocalDateTime.of(today, end)
            )
        )
    }

    private fun MutableList<DemoIntervention>.addPreparation(
        id: String,
        today: LocalDate,
        start: LocalTime,
        end: LocalTime,
        title: String,
        description: String
    ) {
        add(
            DemoIntervention(
                id = id,
                title = title,
                description = description,
                type = InterventionType.SLEEP_PREPARATION.name,
                actionType = InterventionActionType.DO.name,
                startTime = LocalDateTime.of(today, start),
                endTime = LocalDateTime.of(today, end)
            )
        )
    }

    private fun MutableList<DemoIntervention>.addMainSleep(
        id: String,
        startDate: LocalDate,
        start: LocalTime,
        endDate: LocalDate,
        end: LocalTime,
        title: String,
        description: String
    ) {
        add(
            DemoIntervention(
                id = id,
                title = title,
                description = description,
                type = InterventionType.MAIN_SLEEP.name,
                actionType = InterventionActionType.DO.name,
                startTime = LocalDateTime.of(startDate, start),
                endTime = LocalDateTime.of(endDate, end)
            )
        )
    }
}