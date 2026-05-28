package com.sujin.nubloompilot.pages

import com.sujin.nubloompilot.R
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sujin.nubloompilot.components.DutyScheduleSection
import com.sujin.nubloompilot.components.TopBanner
import com.sujin.nubloompilot.local.ParticipantLocalStore
import com.sujin.nubloompilot.models.BugReport
import com.sujin.nubloompilot.models.MctqBaselineProfile
import com.sujin.nubloompilot.models.MctqBehaviorProfile
import com.sujin.nubloompilot.repository.BugReportRepository
import com.sujin.nubloompilot.repository.ShiftScheduleRepository
import androidx.compose.ui.res.painterResource
import com.sujin.nubloompilot.ui.theme.DarkRed
import com.sujin.nubloompilot.ui.theme.Gray200
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray400
import com.sujin.nubloompilot.ui.theme.Gray500
import com.sujin.nubloompilot.ui.theme.Gray600
import com.sujin.nubloompilot.ui.theme.Gray700
import com.sujin.nubloompilot.ui.theme.Gray800
import com.sujin.nubloompilot.ui.theme.Gray900
import com.sujin.nubloompilot.ui.theme.HighlightsYellow
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
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

    val bugReportRepository = remember {
        BugReportRepository()
    }

    val scope = rememberCoroutineScope()

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

    var showBugReportBanner by remember {
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

    LaunchedEffect(showBugReportBanner) {
        if (showBugReportBanner) {
            delay(2000)
            showBugReportBanner = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
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

            BugReportSection(
                repository = bugReportRepository,
                scope = scope,
                participantId = participantLocalStore.getParticipantId() ?: "unknown",
                participantName = participantLocalStore.getParticipantName() ?: "간호사",
                onSuccess = {
                    showBugReportBanner = true
                }
            )

            Spacer(modifier = Modifier.height(220.dp))
        }

        TopBanner(
            visible = showSavedBanner,
            message = "듀티표가 저장되었습니다",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        TopBanner(
            visible = showBugReportBanner,
            message = "버그 리포트가 전송되었습니다",
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
private fun BugReportSection(
    repository: BugReportRepository,
    scope: CoroutineScope,
    participantId: String,
    participantName: String,
    onSuccess: () -> Unit
) {
    var bugDescription by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "버그 리포트",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Gray900
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Gray300),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(20.dp, vertical = 18.dp)) {
                Text(
                    text = "앱 사용 중 불편했던 점이나 오류가 있었나요?\n사용하면서 느꼈던 경험을 자유롭게 공유해주세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray800
                )

                Spacer(modifier = Modifier.height(12.dp))

                BulletItem("어떤 화면에서 발생했나요?")
                BulletItem("어떤 행동 이후 발생했나요?")
                BulletItem("어떤 문제가 있었나요?")

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    OutlinedTextField(
                        value = bugDescription,
                        onValueChange = { bugDescription = it },
                        placeholder = {
                            Text(
                                text = "예: 홈 화면에서 수면 체크인 버튼을 눌렀는데 다음 화면으로 넘어가지 않았어요.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray700
                            )
                        },
                        modifier = Modifier.matchParentSize(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White.copy(alpha = 0.6f),
                            focusedBorderColor = Gray900,
                            unfocusedBorderColor = Gray400
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    BugReportSendButton(
                        isEnabled = bugDescription.isNotBlank() && !isSubmitting,
                        isSubmitting = isSubmitting,
                        onClick = {
                            if (bugDescription.isBlank() || isSubmitting) return@BugReportSendButton

                            isSubmitting = true
                            scope.launch {
                                try {
                                    val report = BugReport(
                                        participantId = participantId,
                                        participantName = participantName,
                                        description = bugDescription,
                                        createdAt = Instant.now().toString()
                                    )
                                    repository.submitBugReport(report)
                                    bugDescription = ""
                                    onSuccess()
                                    Log.d("BugReport", "Bug report submitted successfully")
                                } catch (e: Exception) {
                                    Log.e("BugReport", "Failed to submit bug report", e)
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        }
    }
}

@Composable
private fun BulletItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            modifier = Modifier.padding(end = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = DarkRed
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkRed
        )
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

@Composable
private fun BugReportSendButton(
    isEnabled: Boolean,
    isSubmitting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(
                width = 64.dp,
                height = 40.dp
            )
            .padding(end = 2.dp, bottom = 2.dp)
            .clickable(
                enabled = isEnabled,
                onClick = onClick
            ),
        color = if (isEnabled) Gray900 else Gray500,
        shape = RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 0.dp,
            bottomStart = 0.dp,
            bottomEnd = 10.dp
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_send),
                    contentDescription = "버그 리포트 전송",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
