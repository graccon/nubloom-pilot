package com.sujin.nubloompilot.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sujin.nubloompilot.components.BottomBar
import com.sujin.nubloompilot.local.ParticipantLocalStore
import com.sujin.nubloompilot.local.SleepSurveyLocalStore
import com.sujin.nubloompilot.models.BaselineAssessment
import com.sujin.nubloompilot.models.SleepResult
import com.sujin.nubloompilot.pages.HomePage
import com.sujin.nubloompilot.pages.NotificationPermissionGuidePage
import com.sujin.nubloompilot.pages.MorningGloryResultPage
import com.sujin.nubloompilot.pages.MorningGloryTypeInfoPage
import com.sujin.nubloompilot.pages.CheckInNotificationHelpPage
import com.sujin.nubloompilot.pages.MyInfoPage
import com.sujin.nubloompilot.pages.OnboardingPage
import com.sujin.nubloompilot.pages.HealthConnectGuidePage
import com.sujin.nubloompilot.pages.OnboardingProcessingPage
import com.sujin.nubloompilot.pages.SleepCheckInPage
import com.sujin.nubloompilot.pages.SleepProcessingPage
import com.sujin.nubloompilot.pages.SleepPage
import com.sujin.nubloompilot.repository.ParticipantRepository
import com.sujin.nubloompilot.repository.ShiftScheduleRepository
import com.sujin.nubloompilot.repository.SleepResultRepository
import com.sujin.nubloompilot.repository.SleepStatusRepository
import com.sujin.nubloompilot.repository.HealthConnectRepository
import com.sujin.nubloompilot.repository.HealthSummaryRepository
import com.sujin.nubloompilot.repository.SleepInterventionRepository
import com.sujin.nubloompilot.local.SleepInterventionLocalStore
import com.sujin.nubloompilot.local.ShiftTimingLocalStore
import com.sujin.nubloompilot.models.SleepIntervention
import com.sujin.nubloompilot.utils.SleepInterventionMapper
import com.sujin.nubloompilot.utils.SleepInterventionContextBuilder
import com.sujin.nubloompilot.utils.MctqProcessor
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.components.TopBannerHost
import com.sujin.nubloompilot.components.TopBannerManager
import com.sujin.nubloompilot.utils.MctqBehaviorAnalyzer
import java.time.Instant
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@Composable
fun AppNavGraph() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val localStore = remember {
        ParticipantLocalStore(context)
    }

    val participantRepository = remember {
        ParticipantRepository(context)
    }

    val healthConnectRepository = remember { HealthConnectRepository(context) }
    val healthSummaryRepository = remember { HealthSummaryRepository(healthConnectRepository) }

    val shiftScheduleRepository = remember {
        ShiftScheduleRepository(context)
    }


    val sleepStatusRepository = remember {
        SleepStatusRepository(
            healthConnectRepository,
            SleepSurveyLocalStore(context)
        )
    }

    var participantId by remember {
        mutableStateOf(localStore.getParticipantId() ?: "unknown")
    }

    var participantName by remember {
        mutableStateOf(localStore.getParticipantName() ?: "간호사")
    }

    var hasParticipant by remember {
        mutableStateOf(localStore.getParticipantId() != null)
    }

    val sleepResultRepository = remember(participantId) {
        SleepResultRepository(
            participantId = participantId,
            localStore = SleepSurveyLocalStore(context)
        )
    }

    val topBannerManager = remember {
        TopBannerManager()
    }

    val sleepInterventionRepository = remember {
        SleepInterventionRepository(SleepInterventionLocalStore(context))
    }

    // Pending states for onboarding (Reset after completion)
    // TODO: Consider rememberSaveable or temporary local store for better process death handling
    var pendingDemographics by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var pendingAssessment by remember { mutableStateOf<BaselineAssessment?>(null) }

    val startDestination = if (hasParticipant) {
        Routes.HOME
    } else {
        Routes.OnboardingPage
    }

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    val shouldShowBottomBar = currentRoute != null &&
            currentRoute != Routes.OnboardingPage &&
            currentRoute != Routes.HEALTH_CONNECT_GUIDE &&
            currentRoute != Routes.NOTIFICATION_PERMISSION_GUIDE &&
            currentRoute != Routes.ONBOARDING_PROCESSING &&
            !currentRoute.startsWith("sleep_check_in") &&
            !currentRoute.startsWith("sleep_processing") &&
            !currentRoute.startsWith("morning_glory_result") &&
            currentRoute != Routes.MORNING_GLORY_TYPE_INFO &&
            currentRoute != Routes.CHECK_IN_NOTIFICATION_HELP

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(Routes.OnboardingPage) {
                OnboardingPage(
                    onSubmit = { name, birthYear, assessment ->
                        pendingDemographics = name to birthYear
                        pendingAssessment = assessment
                        navController.navigate(Routes.HEALTH_CONNECT_GUIDE)
                    }
                )
            }

            composable(Routes.HEALTH_CONNECT_GUIDE) {
                HealthConnectGuidePage(
                    onNext = {
                        navController.navigate(Routes.NOTIFICATION_PERMISSION_GUIDE)
                    }
                )
            }

            composable(Routes.NOTIFICATION_PERMISSION_GUIDE) {
                NotificationPermissionGuidePage(
                    onPermissionGranted = {
                        navController.navigate(Routes.ONBOARDING_PROCESSING)
                    },
                    onPermissionSkipped = {
                        navController.navigate(Routes.ONBOARDING_PROCESSING)
                    }
                )
            }

            composable(Routes.ONBOARDING_PROCESSING) {
                OnboardingProcessingPage(
                    onAction = {
                        val (name, birthYear) = pendingDemographics ?: ("간호사" to 1990)
                        val assessment = pendingAssessment
                        
                        // MCTQ Processing
                        val baselineProfile = assessment?.mctqResponses?.let { responses ->
                            MctqProcessor.process(responses)
                        }

                        val mctqBehaviorProfile = baselineProfile?.let { bp ->
                            assessment?.mctqResponses?.let { responses ->
                                MctqBehaviorAnalyzer.analyze(
                                    responses = responses,
                                    baseline = bp
                                )
                            }
                        }
                        
                        // Perform actual registration and save
                        kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
                            participantRepository.registerParticipant(
                                name = name,
                                birthYear = birthYear,
                                assessment = assessment,
                                baselineProfile = baselineProfile,
                                mctqBehaviorProfile = mctqBehaviorProfile,
                                onSuccess = {
                                    continuation.resume(Unit)
                                },
                                onFailure = { exception ->
                                    continuation.resumeWithException(exception)
                                }
                            )
                        }
                    },
                    onComplete = {
                        participantId = localStore.getParticipantId() ?: "unknown"
                        participantName = localStore.getParticipantName() ?: "간호사"
                        hasParticipant = true

                        pendingDemographics = null
                        pendingAssessment = null
                        
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.OnboardingPage) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(Routes.HOME) { backStackEntry ->
                val shiftsAroundToday = remember(backStackEntry) {
                    shiftScheduleRepository.getShiftsAroundToday()
                }

                val latestIntervention = remember(backStackEntry) {
                    sleepInterventionRepository.getLatestLocal()
                }

                HomePage(
                    participantName = participantName,
                    yesterdayShift = shiftsAroundToday.yesterdayShift,
                    todayShift = shiftsAroundToday.todayShift,
                    tomorrowShift = shiftsAroundToday.tomorrowShift,
                    dayAfterTomorrowShift = shiftsAroundToday.dayAfterTomorrowShift,
                    latestInterventionBundle = latestIntervention,
                    onNavigateToSleepCheckIn = { endTime, duration, heartRate, baselineDuration, baselineHeartRate ->
                        navController.navigate(
                            Routes.sleepCheckInRoute(
                                endTime = endTime,
                                duration = duration,
                                heartRate = heartRate,
                                baselineDuration = baselineDuration,
                                baselineHeartRate = baselineHeartRate
                            )
                        )
                    },
                    topBannerManager = topBannerManager,
                    onNavigateToResult = { type ->
                        navController.navigate(Routes.morningGloryReviewRoute(type))
                    }
                )
            }

            composable(
                route = Routes.SLEEP_CHECK_IN
            ) { backStackEntry ->
                val endTime = backStackEntry.arguments?.getString("endTime") ?: ""
                val duration =
                    backStackEntry.arguments
                        ?.getString("duration")
                        ?.toLongOrNull()
                        ?: 0L

                val heartRate =
                    backStackEntry.arguments
                        ?.getString("heartRate")
                        ?.toLongOrNull()
                        ?: -1L

                val baselineDuration =
                    backStackEntry.arguments
                        ?.getString("baselineDuration")
                        ?.toLongOrNull()
                        ?: -1L

                val baselineHeartRate =
                    backStackEntry.arguments
                        ?.getString("baselineHeartRate")
                        ?.toLongOrNull()
                        ?: -1L

                SleepCheckInPage(
                    participantName = participantName,
                    sleepEndTime = endTime,
                    sleepDurationMinutes = duration,
                    wakeHeartRate = heartRate.takeIf { it != -1L },
                    baselineSleepDurationMinutes = baselineDuration.takeIf { it != -1L },
                    baselineWakeHeartRate = baselineHeartRate.takeIf { it != -1L },
                    onSubmitClick = { type, time, fatigue ->
                        navController.navigate(
                            Routes.sleepProcessingRoute(
                                type = type,
                                endTime = time,
                                duration = duration,
                                heartRate = heartRate.takeIf { it != -1L },
                                fatigueLevel = fatigue
                            )
                        )
                    }
                )
            }

            composable(
                route = Routes.SLEEP_PROCESSING
            ) { backStackEntry ->
                val args = MorningGloryResultArgs.from(backStackEntry)
                val shiftsAroundToday = remember {
                    shiftScheduleRepository.getShiftsAroundToday()
                }

                val baselineProfile = remember { localStore.getBaselineProfile() }
                val behaviorProfile = remember { localStore.getMctqBehaviorProfile() }
                val shiftTimingConfig = remember { ShiftTimingLocalStore(context).getConfig() }

                val interventionContext = SleepInterventionContextBuilder.build(
                    type = args.type,
                    fatigueLevel = args.fatigueLevel,
                    endTime = args.endTime,
                    shiftsAroundToday = shiftsAroundToday,
                    mctqBaselineProfile = baselineProfile,
                    mctqBehaviorProfile = behaviorProfile,
                    shiftTimingConfig = shiftTimingConfig
                )

                SleepProcessingPage(
                    participantId = participantId,
                    participantName = participantName,
                    type = args.type,
                    endTime = args.endTime,
                    duration = args.duration,
                    heartRate = if (args.heartRate == -1L) null else args.heartRate,
                    fatigueLevel = args.fatigueLevel,
                    interventionContext = interventionContext,
                    healthSummaryRepository = healthSummaryRepository,
                    onSaveResult = { result ->
                        sleepResultRepository.saveSleepResult(result)
                    },
                    onSaveInterventions = { interventions, context ->
                        val bundle = SleepInterventionMapper.toBundle(
                            participantId = participantId,
                            morningGloryType = args.type,
                            context = context,
                            interventions = interventions
                        )
                        sleepInterventionRepository.save(bundle)
                    },
                    onProcessingComplete = {
                        navController.navigate(
                            Routes.morningGloryResultRoute(
                                type = args.type,
                                endTime = args.endTime,
                                duration = args.duration,
                                heartRate = args.heartRate.takeIf { it != -1L },
                                fatigueLevel = args.fatigueLevel
                            )
                        ) {
                            // pop sleep_check_in and sleep_processing from backstack
                            popUpTo(Routes.SLEEP_CHECK_IN) {
                                inclusive = true
                            }
                        }
                    }
                )
            }


            composable(Routes.MYINFO) {
                MyInfoPage(
                    topBannerManager = topBannerManager
                )
            }

            composable(Routes.SLEEP) {
                SleepPage()
            }

            composable(
                route = Routes.MORNING_GLORY_RESULT
            ) { backStackEntry ->
                val args = MorningGloryResultArgs.from(backStackEntry)

                var recoveredResult by remember { mutableStateOf<SleepResult?>(null) }

                LaunchedEffect(args.endTime) {
                    if (args.endTime == "NONE") {
                        recoveredResult = sleepStatusRepository.getLatestSavedSleepResult()
                    }
                }

                val finalType = recoveredResult?.morningGloryType ?: args.type

                val latestInterventionBundle = remember {
                    sleepInterventionRepository.getLatestLocal()
                }
                val interventions = latestInterventionBundle?.interventions ?: emptyList()

                MorningGloryResultPage(
                    participantName = participantName,
                    type = finalType,
                    interventions = interventions,
                    isReviewMode = args.endTime == "NONE",
                    onOpenTypeInfo = {
                        navController.navigate(Routes.MORNING_GLORY_TYPE_INFO)
                    },
                    onOpenNotificationHelp = {
                        navController.navigate(Routes.CHECK_IN_NOTIFICATION_HELP)
                    },
                    onBackHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(Routes.MORNING_GLORY_TYPE_INFO) {
                MorningGloryTypeInfoPage(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.CHECK_IN_NOTIFICATION_HELP) {
                CheckInNotificationHelpPage(
                    onBack = { navController.popBackStack() }
                )
            }
        }

        TopBannerHost(
            manager = topBannerManager,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
        )

        if (shouldShowBottomBar) {
            BottomBar(
                navController = navController,
                currentRoute = currentRoute,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
