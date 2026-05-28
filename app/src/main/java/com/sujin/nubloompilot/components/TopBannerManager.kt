package com.sujin.nubloompilot.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

/**
 * 전역적으로 TopBanner의 상태를 관리하는 객체입니다.
 */
class TopBannerManager {
    var message by mutableStateOf<String?>(null)
        private set

    fun show(msg: String) {
        message = msg
    }

    fun dismiss() {
        message = null
    }
}

/**
 * TopBannerManager의 상태에 따라 실제 TopBanner를 화면에 그리는 호스트 컴포저블입니다.
 * 주로 AppNavGraph의 최상위 레이아웃에 배치됩니다.
 */
@Composable
fun TopBannerHost(
    manager: TopBannerManager,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(manager.message) {
        if (manager.message != null) {
            delay(2000L)
            manager.dismiss()
        }
    }

    TopBanner(
        visible = manager.message != null,
        message = manager.message ?: "",
        modifier = modifier
    )
}
