package com.owen.stocksample.stock.data

import com.owen.stocksample.stock.data.remote.model.StockBwiBbuDto
import com.owen.stocksample.stock.data.remote.model.StockDayDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class StockMapperTest {

    private val dayDto = StockDayDto(
        date = "1141103",
        code = "2330",
        name = "台積電",
        tradeVolume = "32,345,678",
        tradeValue = "33,961,234,567",
        openingPrice = "1,050.00",
        highestPrice = "1,065.00",
        lowestPrice = "1,045.00",
        closingPrice = "1,060.00",
        change = "-10.5000",
        transaction = "45,678",
    )

    private val bwiBbuDto = StockBwiBbuDto(
        code = "2330",
        name = "台積電",
        peRatio = "25.30",
        dividendYield = "1.50",
        pbRatio = "6.80",
    )

    @Test
    fun toNumber_null_returnsNull() {
        assertNull((null as String?).toNumber())
    }

    @Test
    fun toNumber_blank_returnsNull() {
        assertNull("".toNumber())
        assertNull("   ".toNumber())
    }

    @Test
    fun toNumber_removesThousandsSeparator() {
        assertEquals("1234567", "1,234,567".toNumber())
        assertEquals("1050.25", "1,050.25".toNumber())
    }

    @Test
    fun toDomain_mapsAllFields() {
        val stock = dayDto.toDomain(BigDecimal("1040.25"), bwiBbuDto)!!

        assertEquals("2330", stock.code)
        assertEquals("台積電", stock.name)
        assertEquals(32_345_678L, stock.tradeVolume)
        assertEquals(33_961_234_567L, stock.tradeValue)
        assertEquals(45_678L, stock.transaction)
        assertEquals(BigDecimal("1050.00"), stock.openingPrice)
        assertEquals(BigDecimal("1065.00"), stock.highestPrice)
        assertEquals(BigDecimal("1045.00"), stock.lowestPrice)
        assertEquals(BigDecimal("1060.00"), stock.closingPrice)
        assertEquals(BigDecimal("-10.5000"), stock.change)
        assertEquals(BigDecimal("1040.25"), stock.monthlyAveragePrice)
        assertEquals(BigDecimal("25.30"), stock.peRatio)
        assertEquals(BigDecimal("1.50"), stock.dividendYield)
        assertEquals(BigDecimal("6.80"), stock.pbRatio)
    }

    @Test
    fun toDomain_noTrade_emptyFieldsBecomeNull() {
        // 無成交時 API 回傳空字串
        val noTradeDto = dayDto.copy(
            tradeVolume = "0",
            tradeValue = "0",
            openingPrice = "",
            highestPrice = "",
            lowestPrice = "",
            closingPrice = "",
            change = "",
            transaction = "0",
        )

        val stock = noTradeDto.toDomain(null, null)!!

        assertEquals(0L, stock.tradeVolume)
        assertNull(stock.openingPrice)
        assertNull(stock.highestPrice)
        assertNull(stock.lowestPrice)
        assertNull(stock.closingPrice)
        assertNull(stock.change)
    }

    @Test
    fun toDomain_invalidNumber_becomesNull() {
        val stock = dayDto.copy(closingPrice = "--", tradeVolume = "N/A").toDomain(null, null)!!

        assertNull(stock.closingPrice)
        assertNull(stock.tradeVolume)
    }

    @Test
    fun toDomain_withoutBwiBbu_valuationIsNull() {
        val stock = dayDto.toDomain(null, null)!!

        assertNull(stock.monthlyAveragePrice)
        assertNull(stock.peRatio)
        assertNull(stock.dividendYield)
        assertNull(stock.pbRatio)
    }

    @Test
    fun toDomain_emptyValuation_becomesNull() {
        // 虧損公司沒有本益比，API 回傳空字串
        val stock = dayDto.toDomain(null, bwiBbuDto.copy(peRatio = ""))!!

        assertNull(stock.peRatio)
        assertEquals(BigDecimal("1.50"), stock.dividendYield)
    }

    @Test
    fun toDomain_missingCode_returnsNull() {
        assertNull(dayDto.copy(code = null).toDomain(null, null))
        assertNull(dayDto.copy(code = " ").toDomain(null, null))
    }

    @Test
    fun toDomain_missingName_returnsNull() {
        assertNull(dayDto.copy(name = null).toDomain(null, null))
        assertNull(dayDto.copy(name = "").toDomain(null, null))
    }
}
