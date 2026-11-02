package com.owen.stocksample.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** 左右兩側與螢幕邊緣的最小距離 */
private val HorizontalMargin = 32.dp

/** 最高高度佔螢幕高度的比例 */
private const val MAX_HEIGHT_RATIO = 0.5f

/**
 * 置中 dialog，對應 View 版的 BaseCenterDialog。
 * 寬度依內容而定，最寬為螢幕寬度扣掉左右各 [HorizontalMargin]；高度依內容而定，最高為螢幕高度的 [MAX_HEIGHT_RATIO]。
 */
@Composable
fun CenterDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val maxWidth = configuration.screenWidthDp.dp - HorizontalMargin * 2
    val maxHeight = configuration.screenHeightDp.dp * MAX_HEIGHT_RATIO

    Dialog(
        onDismissRequest = onDismissRequest,
        // 關閉平台預設寬度，改由 widthIn 限制最寬寬度
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // 用 widthIn 而非 padding 留邊：點擊判斷以內容邊界為準，padding 區域會被當成 dialog 內部而無法點擊關閉
        Surface(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .heightIn(max = maxHeight),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            content = content,
        )
    }
}

/**
 * 顯示標題、訊息與確定按鈕的置中 dialog，對應 View 版的 SimpleCenterDialog。
 * 訊息超過最高高度時可捲動，標題與按鈕維持顯示。
 */
@Composable
fun SimpleCenterDialog(
    message: String,
    confirmText: String,
    onDismissRequest: () -> Unit,
    title: String? = null,
) {
    CenterDialog(onDismissRequest = onDismissRequest) {
        Column(modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 16.dp)) {
            if (title != null) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
            }
            // fill = false：內容短時依內容高度，超過最高高度時才壓縮並捲動
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text(text = confirmText)
                }
            }
        }
    }
}
