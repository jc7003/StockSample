# StockSample

使用 [臺灣證券交易所 OpenAPI](https://openapi.twse.com.tw/) 顯示上市個股每日成交資訊的 Android 範例專案。
同一份 `ViewModel` 分別以 **View System（XML）** 與 **Jetpack Compose** 實作兩套列表畫面，方便對照。

## Demo

[![StockSample Demo](https://img.youtube.com/vi/kLzqqpFkrsQ/hqdefault.jpg)](https://youtu.be/kLzqqpFkrsQ)

## 功能

- 上市個股列表：開盤 / 最高 / 最低 / 收盤價、漲跌、成交量、成交金額、成交筆數
- 合併月平均價、本益比、殖利率、股價淨值比
- 依股票代號升冪 / 降冪排序（Bottom Sheet）
- 下拉重新整理、錯誤畫面與重試
- 點擊個股顯示估值資訊對話框

## 核心技術

| 類別 | 技術 |
| --- | --- |
| 語言 | Kotlin 2.0、Coroutines、Flow |
| 架構 | MVVM + Clean Architecture（data / domain / ui 分層） |
| 依賴注入 | Hilt（KSP） |
| 網路 | Retrofit 3、OkHttp 4、Gson |
| UI（View） | ViewBinding、RecyclerView + `ListAdapter` / `DiffUtil`、SwipeRefreshLayout、Material Components |
| UI（Compose） | Jetpack Compose、Material 3、`PullToRefreshBox`、`ModalBottomSheet` |
| 生命週期 | Lifecycle `repeatOnLifecycle`、`collectAsStateWithLifecycle` |
| 建置 | Gradle Kotlin DSL、Version Catalog（`libs.versions.toml`）、AGP 9 |

### 架構分層

```
stock/
├── data/            # Repository 實作、DTO、DTO → Domain 轉換
│   └── remote/      # Retrofit API 介面與端點
├── domain/          # Stock 模型、StockRepository 介面
├── di/              # Hilt Module（綁定 Repository、提供 StockApi）
└── ui/              # StockViewModel、XML 版 Activity
    └── compose/     # Compose 版畫面
core/
├── network/         # NetworkModule、ErrorInterceptor、ApiException、safeApiCall
├── extensions/      # 數字格式化、Dialog 擴充
└── utils/           # 漲跌顏色等工具
```

資料流：`StockApi` → `StockRepositoryImpl` → `StockViewModel`（`StateFlow<StockUiState>`）→ Activity / Composable。

### 實作重點

- **並行請求與資料合併**：`StockRepositoryImpl` 以 `async` 同時呼叫三支 API（日成交資訊、月均價、本益比 / 殖利率 / 淨值比），再依股票代號合併成單一 `Stock` 模型。
- **UI 狀態**：`StockUiState`（`Loading` / `Success` / `Error`）以 sealed interface 表示；排序條件與載入結果透過 `combine` 合併，排序在 `Dispatchers.Default` 執行，並以 `stateIn(WhileSubscribed(5_000))` 在設定變更時保留狀態。
- **錯誤處理**：`ErrorInterceptor` 將連線失敗與非 2xx 回應轉成繼承 `IOException` 的 `ApiException`（`NoNetwork` / `Timeout` / `Server` / `Client`）；`safeApiCall` 包成 `Result`，且不吞掉 `CancellationException`。
- **HTTP 快取**：OkHttp 20MB 磁碟快取，搭配 `max-age=0` 每次以 ETag / Last-Modified 向伺服器確認，資料未更新時取得 304 回應。
- **TLS 設定**：限定 TLS 1.2，避開 API 37 以 TLS 1.3 連線時 TWSE CDN 回傳錯誤憑證的問題。
- **數值精度**：價格與比率使用 `BigDecimal`，避免浮點誤差。
- **Edge-to-edge**：處理 `WindowInsets`，支援 targetSdk 35+ 的強制全螢幕顯示。

## 使用的 API

Base URL：`https://openapi.twse.com.tw/v1/`

| 端點 | 說明 |
| --- | --- |
| `exchangeReport/STOCK_DAY_ALL` | 上市個股日成交資訊 |
| `exchangeReport/STOCK_DAY_AVG_ALL` | 上市個股日收盤價及月均價 |
| `exchangeReport/BWIBBU_ALL` | 上市個股日本益比、殖利率及股價淨值比 |

## 環境需求

- Android Studio（支援 AGP 9.2）
- JDK 11 以上
- minSdk 26 / targetSdk 36 / compileSdk 37

## 建置

```bash
./gradlew assembleDebug
```
