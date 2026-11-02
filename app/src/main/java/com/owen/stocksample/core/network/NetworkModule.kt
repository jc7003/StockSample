package com.owen.stocksample.core.network

import android.content.Context
import com.owen.stocksample.BuildConfig
import com.owen.stocksample.core.config.Configure.STOCK_DOMAIN
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val HTTP_CACHE_DIR = "http_cache"

    /** HTTP 快取上限，月均價資料未壓縮約 4MB */
    private const val HTTP_CACHE_SIZE = 20L * 1024 * 1024

    private val TLS_1_2_SPEC = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
        .tlsVersions(TlsVersion.TLS_1_2)
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        errorInterceptor: ErrorInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            // TWSE 回應帶 ETag / Last-Modified，資料未更新時伺服器回 304，不必重新下載
            .cache(Cache(File(context.cacheDir, HTTP_CACHE_DIR), HTTP_CACHE_SIZE))
            .addInterceptor { chain ->
                // 回應沒有 Cache-Control，OkHttp 會依 Last-Modified 推算有效期而直接使用快取；
                // max-age=0 強制每次向伺服器確認，下拉重新整理才拿得到最新資料
                val request = chain.request().newBuilder()
                    .cacheControl(CacheControl.Builder().maxAge(0, TimeUnit.SECONDS).build())
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(errorInterceptor)
            // Android API 37 以 TLS 1.3 連線時，TWSE 的 CDN 會回傳 www.twse.com.tw 的憑證而驗證失敗，
            // API 31 與 TLS 1.2 皆正常，故限定 TLS 1.2；cipher suite 沿用 MODERN_TLS，仍為前向保密的 AEAD 加密。
            // 之後可移除此設定，在 API 37 模擬器驗證是否已恢復正常
            .connectionSpecs(listOf(TLS_1_2_SPEC))
            .apply {
                if (BuildConfig.DEBUG) {
                    // 放在 ErrorInterceptor 之後，才能在錯誤被轉換前看到原始回應
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    })
                }
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(STOCK_DOMAIN)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}