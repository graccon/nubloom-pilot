package com.sujin.nubloompilot.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sujin.nubloompilot.components.DutyScheduleSection
import com.sujin.nubloompilot.components.TopBanner
import com.sujin.nubloompilot.local.ParticipantLocalStore
import com.sujin.nubloompilot.models.MctqBaselineProfile
import com.sujin.nubloompilot.models.MctqBehaviorProfile
import com.sujin.nubloompilot.repository.ShiftScheduleRepository
import com.sujin.nubloompilot.ui.theme.Gray200
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray400
import com.sujin.nubloompilot.ui.theme.Gray500
import com.sujin.nubloompilot.ui.theme.Gray600
import com.sujin.nubloompilot.ui.theme.Gray700
import com.sujin.nubloompilot.ui.theme.Gray800
import com.sujin.nubloompilot.ui.theme.Gray900
import com.sujin.nubloompilot.ui.theme.HighlightsYellow
import kotlinx.coroutines.delay
import java.time.YearMonth

@Composable
fun MyInfoPage(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheduleRepository = remember {
        ShiftScheduleRepository(context)
    }
    val participantLocalStore = remember {
        ParticipantLocalStore(context)
    }

    val baselineProfile = remember {
        participantLocalStore.getBaselineProfile()
    }

    val mctqBehaviorProfile = remember {
        participantLocalStore.getMctqBehaviorProfile()
    }

    var currentYearMonth by remember {
        mutableStateOf(YearMonth.now())
    }

    var shifts by remember(currentYearMonth) {
        mutableStateOf(
            scheduleRepository.getLocalSchedule(
                year = currentYearMonth.year,
                month = currentYearMonth.monthValue
            )
        )
    }

    var originalShifts by remember(currentYearMonth) {
        mutableStateOf(shifts)
    }

    var showSavedBanner by remember {
        mutableStateOf(false)
    }

    var isEditMode by remember {
        mutableStateOf(false)
    }

    var selectedDay by remember(currentYearMonth) {
        mutableStateOf(1)
    }

    val daysInMonth = currentYearMonth.lengthOfMonth()

    LaunchedEffect(showSavedBanner) {
        if (showSavedBanner) {
            delay(2000)
            showSavedBanner = false
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            DutyScheduleSection(
                currentYearMonth = currentYearMonth,
                shifts = shifts,
                isEditMode = isEditMode,
                selectedDay = selectedDay,
                onPrevMonth = { currentYearMonth = currentYearMonth.minusMonths(1) },
                onNextMonth = { currentYearMonth = currentYearMonth.plusMonths(1) },
                onEditStart = {
                    originalShifts = shifts
                    selectedDay = 1
                    isEditMode = true
                },
                onCancelEdit = {
                    shifts = originalShifts
                    isEditMode = false
                },
                onSave = {
                    scheduleRepository.saveSchedule(
                        year = currentYearMonth.year,
                        month = currentYearMonth.monthValue,
                        shifts = shifts,
                        onSuccess = {
                            originalShifts = shifts
                            isEditMode = false
                            showSavedBanner = true
                        },
                        onFailure = {
                            // TODO: 실패 배너 처리
                        }
                    )
                },
                onDayClick = { day ->
                    selectedDay = day
                },
                onShiftSelected = { shift ->
                    shifts = shifts.toMutableMap().apply {
                        this[selectedDay] = shift
                    }
                    selectedDay = if (selectedDay < daysInMonth) {
                        selectedDay + 1
                    } else {
                        1
                    }
                }
            )

            MctqBaselineSection(baselineProfile)

            MctqBehaviorSection(mctqBehaviorProfile)

            Spacer(modifier = Modifier.height(120.dp))
        }

        TopBanner(
            visible = showSavedBanner,
            message = "듀티표가 저장되었습니다",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )
    }
}

@Composable
private fun MctqBaselineSection(profile: MctqBaselineProfile?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(26.dp))
        Text(
            text = "수면 베이스라인 기초 정보",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Gray900
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (profile == null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Gray800.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "아직 수면 baseline 정보가 없습니다.",
                    modifier = Modifier.padding(16.dp),
                    color = Gray200
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Gray300),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(20.dp, vertical = 14.dp)) {
                    InfoRow("크로노타입 (MSFEsc)", profile.chronotypeMsfEsc)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Gray400)
                    
                    InfoRow("평균 근무일 수면", formatMinutes(profile.averageWorkSleepDurationMinutes))
                    InfoRow("평균 휴일 수면", formatMinutes(profile.averageFreeSleepDurationMinutes))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Gray400)
                    
                    Text(
                        text = "사회적 시차 (Social Jetlag)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Gray700,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    InfoRow("  - Day", formatMinutes(profile.socialJetlagDayMinutes))
                    InfoRow("  - Evening", formatMinutes(profile.socialJetlagEveningMinutes))
                    InfoRow("  - Night", formatMinutes(profile.socialJetlagNightMinutes))
                }
            }
        }
    }
}

@Composable
private fun MctqBehaviorSection(profile: MctqBehaviorProfile?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "MCTQ 수면 행동 패턴",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Gray900
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (profile == null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Gray800.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "아직 수면 행동 분석 정보가 없습니다.",
                    modifier = Modifier.padding(16.dp),
                    color = Gray200
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Gray300),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(20.dp, vertical = 14.dp)) {
                    InfoRow("평균 수면 진입 시간", "${profile.averageSleepLatencyMinutes}분")
                    InfoRow("평균 침대-수면 간격", "${profile.averageBedGapMinutes}분")
                    InfoRow("취약 근무", profile.vulnerableShift?.label ?: "-")
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Gray400)

                    BehaviorTagRow("잠들기 오래 걸리는 편", profile.hasSleepLatencyRisk)
                    BehaviorTagRow("침대에 오래 머무는 편", profile.hasBedInefficiency)
                    BehaviorTagRow("낮잠 습관 있음", profile.hasNapHabit)
                    BehaviorTagRow("늦은 낮잠 위험", profile.hasLateNapRisk)
                }
            }
        }
    }
}

@Composable
private fun BehaviorTagRow(label: String, isActive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = if (isActive) Gray900 else Gray500
        )
        Text(
            text = if (isActive) "YES" else "NO",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (isActive) HighlightsYellow else Gray400,
            modifier = Modifier
                .background(
                    if (isActive) Gray900 else Gray200,
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge,fontWeight = FontWeight.SemiBold, color = Gray900)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                letterSpacing = 0.8.sp
            ),
            fontWeight = FontWeight.Bold,
            color = Gray700
        )
    }
}

private fun formatMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
