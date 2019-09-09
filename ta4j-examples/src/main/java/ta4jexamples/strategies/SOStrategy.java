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
package ta4jexamples.strategies;

import java.util.List;

import eu.verdelhan.ta4j.BaseStrategy;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TimeSeriesManager;
import eu.verdelhan.ta4j.Trade;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.analysis.criteria.TotalProfitCriterion;
import eu.verdelhan.ta4j.indicators.StochasticOscillatorD2Indicator;
import eu.verdelhan.ta4j.indicators.StochasticOscillatorDIndicator;
import eu.verdelhan.ta4j.indicators.StochasticOscillatorKIndicator;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.UnderIndicatorRule;
import ta4jexamples.loaders.CsvTicksLoaderDownloaded;

/**
 * CCI Correction Strategy
 * <p>
 * @see http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:cci_correction
 */
public class SOStrategy {

    /**
     * @param series a time series
     * @return a CCI correction strategy
     */
    public static Strategy buildStrategy(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        //handelen op close = 32,3,4
        //handelen op open=28,3,2 (2.04,108)
        StochasticOscillatorKIndicator stochasticOscillatorKIndicator =
                new StochasticOscillatorKIndicator(series, 34);
        StochasticOscillatorDIndicator stochasticOscillatorDIndicator_1 =
                new StochasticOscillatorDIndicator(stochasticOscillatorKIndicator, 3);
        StochasticOscillatorD2Indicator stochasticOscillatorDIndicator_2 =
                new StochasticOscillatorD2Indicator(stochasticOscillatorDIndicator_1, 3);

        Rule entryRule =
                new OverIndicatorRule(stochasticOscillatorDIndicator_1,
                stochasticOscillatorDIndicator_2);

        Rule exitRule =
                new OverIndicatorRule(stochasticOscillatorKIndicator, Decimal.valueOf(67)).and(new UnderIndicatorRule(stochasticOscillatorDIndicator_1,
                stochasticOscillatorDIndicator_2));

        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(20);
        return strategy;
    }

    public static void main(String[] args) {

        // Getting the time series
        TimeSeries series = CsvTicksLoaderDownloaded.loadASMLIncSeries("ASML", 4);

        // Building the trading strategy
        Strategy strategy = buildStrategy(series);

        // Running the strategy
        TimeSeriesManager seriesManager = new TimeSeriesManager(series);
        Tick lastTick = series.getLastTick();
        TradingRecord tradingRecord = seriesManager.run(strategy);
        System.out.println("Number of trades for the strategy: " + tradingRecord.getTradeCount());
        //int index, Decimal price, Decimal amount, ZonedDateTime date
        tradingRecord.getCurrentTrade().operate(series.getTickCount()- 1, lastTick.getClosePrice(), Decimal.NaN,
                lastTick.getEndTime());
        tradingRecord.getTrades().add(tradingRecord.getCurrentTrade());

        // Analysis
        System.out.println("Total profit for the strategy: " + new TotalProfitCriterion().calculate(series, tradingRecord));

        String status = "not in stock";
        int amount = 0;
        int amountShortSell = 0;
        double cashReceivedByShortSell = 0;
        double value = 0;
        int index = 0;
        double cash = 100000;
        int aantalDagenNietGekocht = 0;
        double values[] = new double[series.getTickCount()];

        for (Tick tick : series.getTickData()) {
            if (status.equals("not in stock") && isBuySignal(index, tradingRecord.getTrades())) {
                status = "bought";
                amount = (int) Math.round(cash / tick.getClosePrice().toDouble());
                cash = cash + cashReceivedByShortSell - (amountShortSell * tick.getClosePrice().toDouble());;
                amountShortSell = 0;
                cashReceivedByShortSell = 0;
                cash = cash - (amount * tick.getClosePrice().toDouble());
                values[index] =
                        cash + (amount * tick.getClosePrice().toDouble());
                aantalDagenNietGekocht++; // close koers  = koop
            } else if (status.equals("bought") && isSellSignal(index, tradingRecord.getTrades())) {
                status = "not in stock";
                cash = cash + (amount * tick.getClosePrice().toDouble());
                amount = 0;
                amountShortSell = (int) Math.round(cash / tick.getClosePrice().toDouble());
                cashReceivedByShortSell = amountShortSell * tick.getClosePrice().toDouble();
                values[index] = cash;
            } else if (status.equals("bought")) {
                values[index] = cash + (amount * tick.getClosePrice().toDouble());
            } else if (status.equals("not in stock")) {
                aantalDagenNietGekocht++;
                values[index] = cash + cashReceivedByShortSell - (amountShortSell * tick.getClosePrice().toDouble());
            } else {
                //error
            }
            index++;
        }
        System.out.println("aantalDagenNietGekocht: " + aantalDagenNietGekocht);
        int i = 1;
        int aantalStijgers = 0;
        int aantalDalers = 0;
        double sumStijging = 0;
        double sumDaling = 0;
        double vorigeValue = -1;
        for (double myvalue: values) {
            System.out.println(i + " " + myvalue);
            if ((vorigeValue > 0) && myvalue >= vorigeValue) {
                aantalStijgers ++;
                sumStijging = sumStijging + ((myvalue - vorigeValue) / vorigeValue);
            }
            if ((vorigeValue > 0) && myvalue < vorigeValue) {
                aantalDalers ++;
                sumDaling = sumDaling + ((vorigeValue - myvalue) / myvalue);
            }
            vorigeValue = myvalue;
            i++;
        }
        System.out.println("aantalStijgers: " + aantalStijgers + " aantalDalers: " + aantalDalers);
        System.out.println("gemiddelde stijging: " + (sumStijging / aantalStijgers) * 100 + " gemiddelde daling: " + (sumDaling / aantalDalers) * 100);

        tradingRecord.getTrades().forEach(trade -> {
            System.out.println(((trade.getExit().getPrice().dividedBy(trade.getEntry().getPrice()).minus(Decimal.ONE)))
             .multipliedBy(Decimal.HUNDRED) + " " + trade);
        });

    }

    private static boolean isBuySignal(int index, List<Trade> trades) {
        return trades.stream().anyMatch(trade -> trade.getEntry().getIndex() == index);
    }

    private static boolean isSellSignal(int index, List<Trade> trades) {
        return trades.stream().anyMatch(trade -> trade.getExit() != null && trade.getExit().getIndex() == index);
    }

}
