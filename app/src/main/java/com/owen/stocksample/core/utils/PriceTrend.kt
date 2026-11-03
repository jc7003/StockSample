package com.owen.stocksample.core.utils

import java.math.BigDecimal

/** 價格漲跌方向，決定漲跌顏色。 */
enum class PriceTrend {
    UP,
    DOWN,
    FLAT,
    ;

    companion object {

        /** 依價差判斷漲跌方向，null 或 0 視為平盤。 */
        fun of(change: BigDecimal?): PriceTrend = when (change?.signum() ?: 0) {
            // 用 signum 判斷正負，BigDecimal 的 == 會比較小數位數，0.00 不等於 0
            0 -> FLAT
            1 -> UP
            else -> DOWN
        }
    }
}
