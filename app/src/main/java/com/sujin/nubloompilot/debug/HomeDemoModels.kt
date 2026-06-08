package com.sujin.nubloompilot.debug

enum class DemoChronotype {
    MORNING,
    INTERMEDIATE,
    EVENING
}

enum class DemoCondition {
    TIRED,
    FRESH
}

enum class DemoWorkScenario {
    FIRST_NIGHT,
    CONSECUTIVE_DAY
}

data class DemoHomeControlState(
    val enabled: Boolean = false,
    val chronotype: DemoChronotype = DemoChronotype.INTERMEDIATE,
    val condition: DemoCondition = DemoCondition.TIRED,
    val workScenario: DemoWorkScenario = DemoWorkScenario.FIRST_NIGHT
)
