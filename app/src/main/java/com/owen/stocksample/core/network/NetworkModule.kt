package com.owen.stocksample.core.network

import com.owen.stocksample.core.config.Configure.STOCK_DOMAIN
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(STOCK_DOMAIN)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
