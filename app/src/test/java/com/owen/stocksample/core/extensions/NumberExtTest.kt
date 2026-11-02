package com.owen.stocksample.core.extensions

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class NumberExtTest {

    @Test
    fun toDisplayText_null_returnsPlaceholder() {
        assertEquals(PLACEHOLDER_TEXT, (null as Number?).toDisplayText())
    }

    @Test
    fun toDisplayText_zero() {
        assertEquals("0", 0.toDisplayText())
        assertEquals("0", 0.0.toDisplayText())
    }

    @Test
    fun toDisplayText_belowThousand_noSeparator() {
        assertEquals("999", 999.toDisplayText())
    }

    @Test
    fun toDisplayText_long_addsThousandsSeparator() {
        assertEquals("1,000", 1_000L.toDisplayText())
        assertEquals("1,123,123", 1_123_123L.toDisplayText())
        assertEquals("15,000,000,000", 15_000_000_000L.toDisplayText())
    }

    @Test
    fun toDisplayText_double_keepsOriginalFraction() {
        assertEquals("1,050.1234", 1050.1234.toDisplayText())
        assertEquals("12.5", 12.5.toDisplayText())
    }

    @Test
    fun toDisplayText_wholeDouble_dropsTrailingZero() {
        assertEquals("12", 12.0.toDisplayText())
    }

    @Test
    fun toDisplayText_smallDouble_notScientificNotation() {
        assertEquals("0.0000001", 0.0000001.toDisplayText())
    }

    @Test
    fun toDisplayText_negative() {
        assertEquals("-1,234.5", (-1234.5).toDisplayText())
        assertEquals("-9,999", (-9_999L).toDisplayText())
    }

    @Test
    fun toDisplayText_bigDecimal() {
        assertEquals("1,234.56", BigDecimal("1234.56").toDisplayText())
        // 小數位數不影響顯示，結尾的 0 不顯示
        assertEquals("1,050", BigDecimal("1050.00").toDisplayText())
        assertEquals("37.6", BigDecimal("37.60").toDisplayText())
        assertEquals("-0.4", BigDecimal("-0.4000").toDisplayText())
    }

    @Test
    fun toDisplayText_ignoresDefaultLocale() {
        // 德文的千分位為「.」、小數點為「,」，結果仍應使用 Locale.US 的符號
        withDefaultLocale(Locale.GERMANY) {
            assertEquals("1,050.1234", 1050.1234.toDisplayText())
        }
    }


    @Test
    fun toCompactText_null_returnsPlaceholder() {
        assertEquals(PLACEHOLDER_TEXT, (null as Long?).toCompactText())
    }

    @Test
    fun toCompactText_belowTenThousand_sameAsDisplayText() {
        assertEquals("0", 0L.toCompactText())
        assertEquals("9,949", 9_949L.toCompactText())
        assertEquals("9,999", 9_999L.toCompactText())
    }

    @Test
    fun toCompactText_exactUnit() {
        assertEquals("1.0萬", 1_0000L.toCompactText())
        assertEquals("1.0億", 1_0000_0000L.toCompactText())
        assertEquals("1.0兆", 1_0000_0000_0000L.toCompactText())
    }

    @Test
    fun toCompactText_wan() {
        assertEquals("1.2萬", 12_345L.toCompactText())
        assertEquals("3,234.6萬", 32_345_678L.toCompactText())
    }

    @Test
    fun toCompactText_yi() {
        assertEquals("339.6億", 33_961_234_567L.toCompactText())
    }

    @Test
    fun toCompactText_zhao() {
        assertEquals("1.2兆", 1_234_567_890_123L.toCompactText())
    }

    @Test
    fun toCompactText_roundsHalfUp() {
        assertEquals("1.3萬", 12_500L.toCompactText())
        assertEquals("1.2萬", 12_499L.toCompactText())
    }

    @Test
    fun toCompactText_justBelowNextUnit_staysInCurrentUnit() {
        assertEquals("9,999.5萬", 99_994_999L.toCompactText())
    }

    @Test
    fun toCompactText_roundsUpToTenThousand_promotesToNextUnit() {
        assertEquals("1.0億", 99_999_999L.toCompactText())
        assertEquals("1.0億", 99_999_500L.toCompactText())
        assertEquals("1.0兆", 999_999_999_950L.toCompactText())
    }

    @Test
    fun toCompactText_largestUnit_doesNotPromote() {
        assertEquals("10,000.0兆", 1_0000_0000_0000_0000L.toCompactText())
        assertEquals("9,223,372.0兆", Long.MAX_VALUE.toCompactText())
    }

    @Test
    fun toCompactText_negative() {
        assertEquals("-5.7萬", (-56_789L).toCompactText())
        assertEquals("-1.3萬", (-12_500L).toCompactText())
        assertEquals("-1.0億", (-99_999_999L).toCompactText())
        assertEquals("-9,999", (-9_999L).toCompactText())
    }

    @Test
    fun toCompactText_minValue_fallsBackToDisplayText() {
        assertEquals("-9,223,372,036,854,775,808", Long.MIN_VALUE.toCompactText())
    }

    @Test
    fun toCompactText_ignoresDefaultLocale() {
        withDefaultLocale(Locale.GERMANY) {
            assertEquals("3,234.6萬", 32_345_678L.toCompactText())
        }
    }


    private fun withDefaultLocale(locale: Locale, block: () -> Unit) {
        val original = Locale.getDefault()
        Locale.setDefault(locale)
        try {
            block()
        } finally {
            Locale.setDefault(original)
        }
    }
}
