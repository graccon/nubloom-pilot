package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.models.InterpretationBlock
import com.sujin.nubloompilot.models.SignalState
import com.sujin.nubloompilot.models.SleepInterpretationInput
import com.sujin.nubloompilot.models.SleepInterpretationResult

object SleepInterpretationEngine {

    fun interpret(
        input: SleepInterpretationInput
    ): SleepInterpretationResult {
        return SleepInterpretationResult(
            sleepDuration = interpretSleepDuration(input),
            recovery = interpretRecovery(input)
        )
    }

    private fun interpretSleepDuration(
        input: SleepInterpretationInput
    ): InterpretationBlock {
        val duration = input.sleepDurationMinutes

        if (duration == null) {
            return InterpretationBlock(
                state = SignalState.UNKNOWN,
                title = "수면 시간을 확인하지 못했어요.",
                description = "웨어러블에서 수면 데이터가 충분히 기록되지 않았어요."
            )
        }

        val durationText = formatMinutes(duration)
        val baseline = input.baselineSleepDurationMinutes

        return when {
            baseline != null && duration >= baseline + 20 -> {
                InterpretationBlock(
                    state = SignalState.GOOD,
                    title = "평소보다 조금 더 오래 주무셨어요.",
                    description =
                        "웨어러블 기준 $durationText 주무셨네요.\n" +
                                "평소보다 수면 시간이 조금 긴 편이에요."
                )
            }

            baseline != null && duration <= baseline - 20 -> {
                InterpretationBlock(
                    state = SignalState.CAUTION,
                    title = "평소보다 수면 시간이 짧았어요.",
                    description =
                        "웨어러블 기준 $durationText 주무셨네요.\n" +
                                "오늘은 수면 시간이 부족했을 수 있어요."
                )
            }

            duration >= 420 -> {
                InterpretationBlock(
                    state = SignalState.GOOD,
                    title = "수면 시간이 충분한 편이에요.",
                    description =
                        "웨어러블 기준 $durationText 주무셨어요.\n" +
                                "오늘은 비교적 충분한 수면을 확보했어요."
                )
            }

            duration < 360 -> {
                InterpretationBlock(
                    state = SignalState.CAUTION,
                    title = "수면 시간이 부족한 편이에요.",
                    description =
                        "웨어러블 기준 $durationText 주무셨어요.\n" +
                                "몸이 충분히 회복되지 않았을 수 있어요."
                )
            }

            else -> {
                InterpretationBlock(
                    state = SignalState.NEUTRAL,
                    title = "평소와 비슷한 수면이에요.",
                    description =
                        "웨어러블 기준 $durationText 주무셨어요.\n" +
                                "오늘 몸 상태와 함께 확인해보면 좋아요."
                )
            }
        }
    }

    private fun interpretRecovery(
        input: SleepInterpretationInput
    ): InterpretationBlock {
        val hr = input.wakeHeartRate

        if (hr == null) {
            return InterpretationBlock(
                state = SignalState.UNKNOWN,
                title = "기상 심박수를 확인하지 못했어요.",
                description = "웨어러블에서 기상 직후 심박수 데이터가 없어요."
            )
        }

        val baseline = input.baselineWakeHeartRate

        if (baseline == null) {
            return InterpretationBlock(
                state = SignalState.NEUTRAL,
                title = "기상 직후 심박수는 ${hr}bpm이에요.",
                description =
                    "평소 데이터가 쌓이면 회복 상태를 더 정확하게 비교할 수 있어요."
            )
        }

        return when {
            hr >= baseline + 10 -> {
                InterpretationBlock(
                    state = SignalState.CAUTION,
                    title = "기상 심박수가 평소보다 높아요.",
                    description =
                        "기상 직후 심박수는 ${hr}bpm이에요.\n" +
                                "몸에 부담이 남아 있거나 아직 회복 중일 수 있어요."
                )
            }

            hr <= baseline - 5 -> {
                InterpretationBlock(
                    state = SignalState.GOOD,
                    title = "기상 심박수가 안정적인 편이에요.",
                    description =
                        "기상 직후 심박수는 ${hr}bpm이에요.\n" +
                                "몸이 비교적 안정된 상태일 수 있어요."
                )
            }

            else -> {
                InterpretationBlock(
                    state = SignalState.NEUTRAL,
                    title = "기상 심박수가 평소와 비슷해요.",
                    description =
                        "기상 직후 심박수는 ${hr}bpm이에요.\n" +
                                "오늘 몸 상태와 함께 참고해보세요."
                )
            }
        }
    }

    private fun formatMinutes(
        minutes: Long
    ): String {
        val hours = minutes / 60
        val remain = minutes % 60
        return "${hours}시간 ${remain}분"
    }
}