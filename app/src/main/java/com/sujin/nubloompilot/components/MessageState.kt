package com.sujin.nubloompilot.components

import androidx.compose.runtime.*
import kotlin.random.Random

private val homeMessages = listOf(
    "오늘의 근무 리듬을 확인해보세요",
    "지금 컨디션을 점검해보세요",
    "오늘의 회복 상태를 살펴보세요",
    "오늘도 수고 많으셨어요",
    "오늘 하루는 어떠셨나요?",
    "다음 근무를 위한 상태를 확인해보세요",
    "현재 리듬과 다음 스케줄을 살펴보세요",

    "오늘의 에너지는 어느 정도인가요?",
    "지금 몸이 보내는 신호를 확인해보세요",
    "오늘의 컨디션을 가볍게 점검해볼까요?",
    "회복이 필요한 순간일 수도 있어요",
    "다음 근무까지의 리듬을 살펴보세요",

    "충분히 쉬고 계신가요?",
    "지금 필요한 건 회복일지도 몰라요",
    "오늘의 피로도를 체크해보세요",
    "몸의 리듬을 잠시 들여다볼까요?",
    "지금 상태를 천천히 살펴보세요",

    "바쁜 하루 속에서도 나를 확인하는 시간이에요",
    "오늘의 몸 상태는 어떤가요?",
    "현재 컨디션을 기록해보세요",
    "오늘의 회복 밸런스를 확인해보세요",

    "다음 근무를 위한 준비가 잘 되고 있나요?",
    "근무 사이 회복 시간을 잘 보내고 계신가요?",
    "오늘의 스케줄 흐름을 확인해보세요",
    "다가오는 근무를 함께 준비해볼까요?",

    "오늘의 수면 리듬은 어떠셨나요?",
    "지금 필요한 휴식이 무엇인지 확인해보세요",
    "지금 내 몸이 어떤 상태인지 살펴보세요",
    "잠시 숨 고르고 현재를 확인해보세요",

    "오늘도 충분히 애쓰고 계십니다",
    "바쁜 하루에도 나를 챙겨보세요",
    "조금만 더 나은 컨디션을 만들어볼까요?",
    "오늘의 작은 회복이 내일을 바꿀 수 있어요",

    "지금 이 순간의 리듬을 확인해보세요",
    "몸과 일정의 흐름을 함께 살펴보세요",
    "현재 상태를 기반으로 다음을 준비해보세요",
    "오늘의 근무 패턴을 한눈에 확인해보세요"
)

@Composable
fun rememberRotatingMessage(): String {
    var messageQueue by remember {
        mutableStateOf(homeMessages.shuffled(Random(System.currentTimeMillis())))
    }

    var currentMessage by remember {
        mutableStateOf(messageQueue.first())
    }

    LaunchedEffect(Unit) {
        if (messageQueue.isNotEmpty()) {
            messageQueue = messageQueue.drop(1)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (messageQueue.isEmpty()) {
                messageQueue =
                    homeMessages.shuffled(Random(System.currentTimeMillis()))
            }
        }
    }

    return currentMessage
}
