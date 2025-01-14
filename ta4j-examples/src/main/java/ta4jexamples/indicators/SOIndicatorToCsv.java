/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014-2017 Marc de Verdelhan & respective authors (see AUTHORS)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ta4jexamples.indicators;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.StochasticOscillatorD2Indicator;
import eu.verdelhan.ta4j.indicators.StochasticOscillatorDIndicator;
import eu.verdelhan.ta4j.indicators.StochasticOscillatorKIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
import ta4jexamples.loaders.CsvTicksLoaderDownloaded;

/**
 * This class builds a CSV file containing values from indicators.
 */
public class SOIndicatorToCsv {

    public static void main(String[] args) {

        /**
         * Getting time series
         */
        TimeSeries series = CsvTicksLoaderDownloaded.loadASMLIncSeries("ASML", 3);

        /**
         * Creating indicators
         */
        // Close price
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//        // Typical price
//        TypicalPriceIndicator typicalPrice = new TypicalPriceIndicator(series);
//        // Price variation
//        PriceVariationIndicator priceVariation = new PriceVariationIndicator(series);
//        // Simple moving averages
//        SMAIndicator shortSma = new SMAIndicator(closePrice, 8);
//        SMAIndicator longSma = new SMAIndicator(closePrice, 20);
//        // Exponential moving averages
//        EMAIndicator shortEma = new EMAIndicator(closePrice, 8);
//        EMAIndicator longEma = new EMAIndicator(closePrice, 20);
//        // Percentage price oscillator
//        PPOIndicator ppo = new PPOIndicator(closePrice, 12, 26);
//        // Rate of change
//        ROCIndicator roc = new ROCIndicator(closePrice, 100);
//        // Relative strength index
//        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
//        // Williams %R
//        WilliamsRIndicator williamsR = new WilliamsRIndicator(series, 20);
//        // Average true range
//        AverageTrueRangeIndicator atr = new AverageTrueRangeIndicator(series, 20);
//        // Standard deviation
//        StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, 14);
//
        StochasticOscillatorKIndicator stochasticOscillatorKIndicator = new StochasticOscillatorKIndicator(series, 50);
        StochasticOscillatorDIndicator stochasticOscillatorDIndicator_1 =
                new StochasticOscillatorDIndicator(stochasticOscillatorKIndicator);
        StochasticOscillatorD2Indicator stochasticOscillatorDIndicator_2 =
                new StochasticOscillatorD2Indicator(stochasticOscillatorDIndicator_1);

        /**
         * Building header
         */
        StringBuilder sb = new StringBuilder("timestamp,close,sod,sod_1,sod_2\n");

        /**
         * Adding indicators values
         */
        final int nbTicks = series.getTickCount();
        for (int i = 0; i < nbTicks; i++) {
            sb.append(series.getTick(i)
                    .getEndTime())
                    .append(',')
                    .append(closePrice.getValue(i))
                    .append(',')
//                    .append(typicalPrice.getValue(i))
//                    .append(',')
//                    .append(priceVariation.getValue(i))
//                    .append(',')
//                    .append(shortSma.getValue(i))
//                    .append(',')
//                    .append(longSma.getValue(i))
//                    .append(',')
//                    .append(shortEma.getValue(i))
//                    .append(',')
//                    .append(longEma.getValue(i))
//                    .append(',')
//                    .append(ppo.getValue(i))
//                    .append(',')
//                    .append(roc.getValue(i))
//                    .append(',')
//                    .append(rsi.getValue(i))
//                    .append(',')
//                    .append(williamsR.getValue(i))
//                    .append(',')
//                    .append(atr.getValue(i))
//                    .append(',')
//                    .append(sd.getValue(i))
//                    .append(',')
                    .append(stochasticOscillatorKIndicator.getValue(i))
                    .append(',')
                    .append(stochasticOscillatorDIndicator_1.getValue(i))
                    .append(',')
                    .append(stochasticOscillatorDIndicator_2.getValue(i))
                    .append('\n');
        }

        /**
         * Writing CSV file
         */
        BufferedWriter writer = null;
        try {
            String path = "/Users/rivabu/aaa/other-java-projects/ta4j-origins/ta4j-examples/src/main/resources/";
            writer = new BufferedWriter(new FileWriter(path + "indicators.csv"));
            writer.write(sb.toString());
        } catch (IOException ioe) {
            Logger.getLogger(SOIndicatorToCsv.class.getName())
                    .log(Level.SEVERE, "Unable to write CSV file", ioe);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ioe) {
            }
        }

    }
}
