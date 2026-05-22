package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.models.SignalState
import com.sujin.nubloompilot.models.SleepInterpretationResult

object MorningGloryClassifier {

    fun classify(
        interpretation: SleepInterpretationResult,
        fatigueLevel: Int
    ): MorningGloryType {
        val objectiveGood = interpretation.sleepDuration.state != SignalState.CAUTION &&
                interpretation.recovery.state != SignalState.CAUTION

        val subjectiveGood = fatigueLevel <= 2

        return when {
            objectiveGood && subjectiveGood -> MorningGloryType.TYPE_1
            !objectiveGood && subjectiveGood -> MorningGloryType.TYPE_2
            objectiveGood && !subjectiveGood -> MorningGloryType.TYPE_3
            else -> MorningGloryType.TYPE_4
        }
    }
}
