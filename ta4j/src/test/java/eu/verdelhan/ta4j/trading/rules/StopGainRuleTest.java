/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Marc de Verdelhan & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package eu.verdelhan.ta4j.trading.rules;

import eu.verdelhan.ta4j.BaseTradingRecord;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
import eu.verdelhan.ta4j.mocks.MockTimeSeries;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StopGainRuleTest {

    private TradingRecord tradingRecord;
    private ClosePriceIndicator closePrice;
    private StopGainRule rule;
    
    @Before
    public void setUp() {
        tradingRecord = new BaseTradingRecord();
        closePrice = new ClosePriceIndicator(new MockTimeSeries(
                100, 105, 110, 120, 150, 120, 160, 180
        ));
    }
    
    @Test
    public void isSatisfied() {
        final Decimal tradedAmount = Decimal.ONE;
        
        // 30% stop-gain
        rule = new StopGainRule(closePrice, Decimal.valueOf("30"));
        
        assertFalse(rule.isSatisfied(0, null));
        assertFalse(rule.isSatisfied(1, tradingRecord));
        
        // Enter at 108
        tradingRecord.enter(2, Decimal.valueOf("108"), tradedAmount, null);
        assertFalse(rule.isSatisfied(2, tradingRecord));
        assertFalse(rule.isSatisfied(3, tradingRecord));
        assertTrue(rule.isSatisfied(4, tradingRecord));
        // Exit
        tradingRecord.exit(5);
        
        // Enter at 118
        tradingRecord.enter(5, Decimal.valueOf("118"), tradedAmount, null);
        assertFalse(rule.isSatisfied(5, tradingRecord));
        assertTrue(rule.isSatisfied(6, tradingRecord));
        assertTrue(rule.isSatisfied(7, tradingRecord));
    }
}
        