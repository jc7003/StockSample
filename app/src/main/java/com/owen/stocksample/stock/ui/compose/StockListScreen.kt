package com.owen.stocksample.stock.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.owen.stocksample.R
import com.owen.stocksample.core.extensions.toCompactText
import com.owen.stocksample.core.extensions.toDisplayText
import com.owen.stocksample.core.network.ApiException
import com.owen.stocksample.compose.SimpleCenterDialog
import com.owen.stocksample.compose.StockSampleTheme
import com.owen.stocksample.compose.stockColors
import com.owen.stocksample.stock.domain.Stock
import com.owen.stocksample.stock.ui.StockSort
import com.owen.stocksample.stock.ui.StockUiState
import com.owen.stocksample.stock.ui.StockViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal

/** 收集 [StockViewModel] 的狀態並交給 [StockListScreen]。 */
@Composable
fun StockListRoute(viewModel: StockViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sort by viewModel.sort.collectAsStateWithLifecycle()

    StockListScreen(
        uiState = uiState,
        sort = sort,
        onBack = onBack,
        onRefresh = viewModel::loadStocks,
        onSortSelected = viewModel::updateSort,
    )
}

/** 個股列表畫面，不持有 ViewModel，方便 Preview 與測試。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockListScreen(
    uiState: StockUiState,
    sort: StockSort,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onSortSelected: (StockSort) -> Unit,
) {
    // 重新整理時 uiState 會回到 Loading，保留上一次的列表避免畫面清空
    var stocks by remember { mutableStateOf((uiState as? StockUiState.Success)?.stocks.orEmpty()) }
    LaunchedEffect(uiState) {
        if (uiState is StockUiState.Success) {
            stocks = uiState.stocks
        }
    }

    // 排序後的新列表到達時才捲回頂端，旋轉螢幕不會誤觸
    val listState = rememberLazyListState()
    var scrolledSort by rememberSaveable { mutableStateOf(sort) }
    LaunchedEffect(stocks) {
        if (sort != scrolledSort) {
            scrolledSort = sort
            listState.scrollToItem(0)
        }
    }

    var showSortSheet by rememberSaveable { mutableStateOf(false) }
    // Stock 無法存入 Bundle，只記住代號，旋轉螢幕後 dialog 仍會顯示
    var selectedCode by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            StockListTopBar(onBack = onBack, onSortClick = { showSortSheet = true })
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState is StockUiState.Loading,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // 錯誤時只顯示錯誤畫面；listState 在外層保存，重試成功後回到原本的捲動位置
            if (uiState is StockUiState.Error) {
                ErrorContent(
                    throwable = uiState.throwable,
                    onRetry = onRefresh,
                    modifier = Modifier.align(Alignment.Center),
                )
                return@PullToRefreshBox
            }

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(items = stocks, key = { it.code }) { stock ->
                    StockItem(stock = stock, onClick = { selectedCode = stock.code })
                }
            }
        }
    }

    if (showSortSheet) {
        StockSortSheet(
            current = sort,
            onSortSelected = onSortSelected,
            onDismiss = { showSortSheet = false },
        )
    }

    stocks.find { it.code == selectedCode }?.let { stock ->
        StockInfoDialog(stock = stock, onDismiss = { selectedCode = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StockListTopBar(onBack: () -> Unit, onSortClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.stock_list_title_compose)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(painter = painterResource(R.drawable.outline_arrow_back_24), contentDescription = null)
            }
        },
        actions = {
            OutlinedIconButton(
                onClick = onSortClick,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(end = 8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_menu_24),
                    contentDescription = stringResource(R.string.stock_sort_title),
                )
            }
        },
    )
}

@Composable
private fun StockItem(stock: Stock, onClick: () -> Unit) {
    val diffFromAverage = stock.closingPrice?.let { closing ->
        stock.monthlyAveragePrice?.let { average -> closing - average }
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stock.code, style = MaterialTheme.typography.labelSmall)
            Text(
                text = stock.name,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // 第一列：開盤價 / 收盤價
            PriceRow(
                modifier = Modifier.padding(top = 12.dp),
                startLabel = stringResource(R.string.stock_label_opening_price),
                startValue = stock.openingPrice.toDisplayText(),
                endLabel = stringResource(R.string.stock_label_closing_price),
                endValue = stock.closingPrice.toDisplayText(),
                endColor = changeColor(diffFromAverage),
            )
            // 第二列：最高價 / 最低價
            PriceRow(
                modifier = Modifier.padding(top = 8.dp),
                startLabel = stringResource(R.string.stock_label_highest_price),
                startValue = stock.highestPrice.toDisplayText(),
                endLabel = stringResource(R.string.stock_label_lowest_price),
                endValue = stock.lowestPrice.toDisplayText(),
            )
            // 第三列：漲跌價差 / 月平均價
            PriceRow(
                modifier = Modifier.padding(top = 8.dp),
                startLabel = stringResource(R.string.stock_label_change),
                startValue = stock.change.toDisplayText(),
                startColor = changeColor(stock.change),
                endLabel = stringResource(R.string.stock_label_monthly_average_price),
                endValue = stock.monthlyAveragePrice.toDisplayText(),
            )
            // 第四列：成交筆數 / 成交股數 / 成交金額
            Row(modifier = Modifier.padding(top = 12.dp)) {
                TradeInfo(
                    label = stringResource(R.string.stock_label_transaction),
                    value = stock.transaction.toDisplayText(),
                    arrangement = Arrangement.Start,
                    modifier = Modifier.weight(1f),
                )
                TradeInfo(
                    label = stringResource(R.string.stock_label_trade_volume),
                    value = stock.tradeVolume.toCompactText(),
                    arrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f),
                )
                TradeInfo(
                    label = stringResource(R.string.stock_label_trade_value),
                    value = stock.tradeValue.toCompactText(),
                    arrangement = Arrangement.End,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** 依漲跌取得文字顏色：null 或 0 使用預設文字顏色，大於 0 為漲色，小於 0 為跌色。 */
@Composable
@ReadOnlyComposable
private fun changeColor(change: BigDecimal?): Color = when (change?.signum() ?: 0) {
    0 -> Color.Unspecified
    1 -> MaterialTheme.stockColors.up
    else -> MaterialTheme.stockColors.down
}

@Composable
private fun PriceRow(
    startLabel: String,
    startValue: String,
    endLabel: String,
    endValue: String,
    modifier: Modifier = Modifier,
    startColor: Color = Color.Unspecified,
    endColor: Color = Color.Unspecified,
) {
    val labelStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
    val valueStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)

    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = startLabel,
            style = labelStyle,
            modifier = Modifier
                .weight(0.2f)
                .alignByBaseline(),
        )
        Text(
            text = startValue,
            style = valueStyle,
            color = startColor,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(0.3f)
                .padding(end = 8.dp)
                .alignByBaseline(),
        )
        Text(
            text = endLabel,
            style = labelStyle,
            modifier = Modifier
                .weight(0.2f)
                .alignByBaseline(),
        )
        Text(
            text = endValue,
            style = valueStyle,
            color = endColor,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(0.3f)
                .alignByBaseline(),
        )
    }
}

@Composable
private fun TradeInfo(
    label: String,
    value: String,
    arrangement: Arrangement.Horizontal,
    modifier: Modifier = Modifier,
) {
    val style = MaterialTheme.typography.bodySmall.copy(
        fontWeight = FontWeight.Bold,
        color = LocalContentColor.current,
    )

    Row(
        modifier = modifier,
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = style)
        // 對應 View 版的 autoSize：空間不足時字級由 12sp 縮至 10sp
        BasicText(
            text = value,
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 12.sp),
            modifier = Modifier
                .padding(start = 4.dp)
                .weight(1f, fill = false),
        )
    }
}

@Composable
private fun ErrorContent(throwable: Throwable, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = errorMessage(throwable),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.common_retry))
        }
    }
}

/** 對應 StockListActivity 的 Throwable.toMessage()。 */
@Composable
private fun errorMessage(throwable: Throwable): String = when (throwable) {
    is ApiException.NoNetwork -> stringResource(R.string.error_no_network)
    is ApiException.Timeout -> stringResource(R.string.error_timeout)
    is ApiException.Server -> stringResource(R.string.error_server, throwable.code)
    is ApiException.Client -> stringResource(R.string.error_client, throwable.code)
    else -> stringResource(R.string.error_unknown)
}

/** 對應 BottomDialogStockSort：預設全展開，點選後套用並收合。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StockSortSheet(
    current: StockSort,
    onSortSelected: (StockSort) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        StockSort.entries.forEach { sort ->
            SortOptionRow(
                label = stringResource(sort.labelRes),
                selected = sort == current,
                onClick = {
                    onSortSelected(sort)
                    // 等收合動畫結束再移除 sheet
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                },
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SortOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) primary else Color.Unspecified,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.outline_check_24),
                contentDescription = null,
                tint = primary,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(24.dp),
            )
        }
    }
}

/** 顯示本益比、殖利率、股價淨值比，無資料的項目整行不顯示。 */
@Composable
private fun StockInfoDialog(stock: Stock, onDismiss: () -> Unit) {
    val message = listOfNotNull(
        stock.peRatio?.let { stringResource(R.string.stock_alert_pe_ratio, it.toDisplayText()) },
        stock.dividendYield?.let { stringResource(R.string.stock_alert_dividend_yield, it.toDisplayText()) },
        stock.pbRatio?.let { stringResource(R.string.stock_alert_pb_ratio, it.toDisplayText()) },
    ).joinToString("\n")

    SimpleCenterDialog(
        title = stock.name,
        message = message,
        confirmText = stringResource(R.string.common_confirm),
        onDismissRequest = onDismiss,
    )
}

@Preview(showBackground = true)
@Composable
private fun StockListScreenPreview() {
    val stock = Stock(
        code = "2330",
        name = "台積電",
        tradeVolume = 32_345_678L,
        tradeValue = 33_961_234_567L,
        openingPrice = BigDecimal("1050.00"),
        highestPrice = BigDecimal("1065.00"),
        lowestPrice = BigDecimal("1045.00"),
        closingPrice = BigDecimal("1060.00"),
        change = BigDecimal("10.00"),
        monthlyAveragePrice = BigDecimal("1040.25"),
        transaction = 45_678L,
        peRatio = BigDecimal("25.3"),
        dividendYield = BigDecimal("1.5"),
        pbRatio = BigDecimal("6.8"),
    )
    StockSampleTheme {
        StockListScreen(
            uiState = StockUiState.Success(listOf(stock, stock.copy(code = "2317", name = "鴻海"))),
            sort = StockSort.CODE_DESCENDING,
            onBack = {},
            onRefresh = {},
            onSortSelected = {},
        )
    }
}
