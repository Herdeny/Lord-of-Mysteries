package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CommissionCurrencyTest {

    @Test
    void formatsPoundsShillingsAndPence() {
        assertEquals("0£ 0s 0d", CommissionCurrency.format(-1));
        assertEquals("0£ 2s 0d", CommissionCurrency.format(24));
        assertEquals("1£ 1s 1d", CommissionCurrency.format(253));
    }
}
