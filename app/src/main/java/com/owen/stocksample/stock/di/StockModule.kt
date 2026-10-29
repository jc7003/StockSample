package com.owen.stocksample.stock.di

import com.owen.stocksample.stock.data.StockRepositoryImpl
import com.owen.stocksample.stock.data.remote.StockApi
import com.owen.stocksample.stock.domain.StockRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StockModule {

    @Binds
    @Singleton
    abstract fun bindStockRepository(
        impl: StockRepositoryImpl
    ): StockRepository

    companion object {

        @Provides
        @Singleton
        fun provideStockApi(retrofit: Retrofit): StockApi {
            return retrofit.create(StockApi::class.java)
        }
    }
}
