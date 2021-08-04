package com.company;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Controller {

    public static String formatBigNumber(BigDecimal bigDec, int scale) {
        NumberFormat formatter = new DecimalFormat("0.0E0");
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        formatter.setMinimumFractionDigits(scale);
        return formatter.format(bigDec);
    }

    public static String formatInRangeNumber(BigDecimal bigDec){
        NumberFormat formatter = new DecimalFormat("###,###.###");
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        formatter.setMaximumFractionDigits(16);
        return formatter.format(bigDec);
    }

        public static BigDecimal bigDecimalSQRT(BigDecimal A, final int SCALE) {
            BigDecimal x0 = new BigDecimal("0");
            BigDecimal x1 = new BigDecimal(Math.sqrt(A.doubleValue()));
            while (!x0.equals(x1)) {
                x0 = x1;
                x1 = A.divide(x0, SCALE, BigDecimal.ROUND_HALF_UP);
                x1 = x1.add(x0);
                x1 = x1.divide(BigDecimal.valueOf(2d), SCALE, BigDecimal.ROUND_HALF_UP);
            }
            return x1;
        }

}
