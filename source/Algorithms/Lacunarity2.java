package Algorithms;

import Utils.*;

public class Lacunarity2
{
    public static class Statistics
    {
        public IntVector boxTable = new IntVector();
        public IntVector el3 = new IntVector();
        public IntVector fl3 = new IntVector();
        public DoubleVector eLambda3 = new DoubleVector();
        public DoubleVector fLambda3 = new DoubleVector();
        public DoubleVector logBoxSizes = new DoubleVector();
        public DoubleVector logElambda3p1 = new DoubleVector();
        public DoubleVector logFlambda3p1 = new DoubleVector();

        public double elCurve;
        public double flCurve;
        public double elMedial;
        public double flMedial;
        public double elMean;
        public double flMean;
    }

    public static void computeLacunarity(Statistics stats, byte[] image, int width, int height, int numberOfBins, int minSize, int inc)
    {
        int minX = width;
        int minY = height;
        int maxX = 0;
        int maxY = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int isWhite = image[x + width * y] >> 31; // no AND with 0xff
                minX = Math.min(minX, (x & isWhite) | (width & ~isWhite));
                minY = Math.min(minY, (y & isWhite) | (height & ~isWhite));
                maxX = Math.max(maxX, x & isWhite);
                maxY = Math.max(maxY, y & isWhite);
            }
        }

        final int selWidth = maxX - minX + 1;
        final int selHeight = maxY - minY + 1;
        final int smallDimension = Math.min(selWidth, selHeight);
        final int factor = (smallDimension - minSize) / numberOfBins;

        int minMedialDiff = -1;
        int medialBox = 0;

        double eLacTotal = 0.0;
        double fLacTotal = 0.0;

        stats.boxTable.size = 0;
        stats.el3.size = 0;
        stats.fl3.size = 0;
        stats.eLambda3.size = 0;
        stats.fLambda3.size = 0;
        stats.logBoxSizes.size = 0;
        stats.logElambda3p1.size = 0;
        stats.logFlambda3p1.size = 0;

        for (int i = 0; i < numberOfBins; i++) {
            final int boxSize = (i < numberOfBins - 1) ? (minSize + i*factor) : smallDimension;

            int diff = Math.abs(boxSize - smallDimension / 2);
            if (minMedialDiff < 0 || diff < minMedialDiff) {
                minMedialDiff = diff;
                medialBox = i;
            }

            int numXBox = (int)Math.ceil((double)selWidth / (double)inc - (double)boxSize / (double)inc + 1.0);
            int numYBox = (int)Math.ceil((double)selHeight / (double)inc - (double)boxSize / (double)inc + 1.0);
            stats.boxTable.resize(numXBox * numYBox * 2);

            // 00 case
            int topLeftSlice  = countWhitePixels(image, width, height, minX, minY, inc, inc);
            int topMainSlice  = countWhitePixels(image, width, height, minX + inc, minY, boxSize - inc, inc);
            int mainLeftSlice = countWhitePixels(image, width, height, minX, minY + inc, inc, boxSize - inc);
            int mainMainSlice = countWhitePixels(image, width, height, minX + inc, minY + inc, boxSize - inc, boxSize - inc);

            stats.boxTable.buf[0] = topLeftSlice + topMainSlice + mainLeftSlice + mainMainSlice;
            stats.boxTable.buf[1] = topLeftSlice + topMainSlice;

            for (int col = 1; col < numXBox; col++) {
                // 0j case
                int x = minX + col * inc;
                int topRightSlice  = countWhitePixels(image, width, height, x + boxSize - inc, minY, inc, inc);
                int mainRightSlice = countWhitePixels(image, width, height, x + boxSize - inc, minY + inc, inc, boxSize - inc);

                stats.boxTable.buf[2*col]     = stats.boxTable.buf[2*(col-1)] + (topRightSlice + mainRightSlice) - (topLeftSlice + mainLeftSlice);
                stats.boxTable.buf[2*col + 1] = stats.boxTable.buf[2*(col-1) + 1] + topRightSlice - topLeftSlice;

                if (col < numXBox - 1) {
                    topLeftSlice  = countWhitePixels(image, width, height, x, minY, inc, inc);
                    mainLeftSlice = countWhitePixels(image, width, height, x, minY + inc, inc, boxSize - inc);
                }
            }

            for (int row = 1; row < numYBox; row++) {
                // i0 case
                int y = minY + row * inc;
                int bottomLeftSlice = countWhitePixels(image, width, height, minX, y + boxSize - inc, inc, inc);
                int bottomMainSlice = countWhitePixels(image, width, height, minX + inc, y + boxSize - inc, boxSize - inc, inc);

                topLeftSlice = countWhitePixels(image, width, height, minX, y, inc, inc);
                topMainSlice = countWhitePixels(image, width, height, minX + inc, y, boxSize - inc, inc);

                int bottomSlice = bottomMainSlice + bottomLeftSlice;
                int topSlice = topLeftSlice + topMainSlice;

                stats.boxTable.buf[2*row*numXBox] =
                    stats.boxTable.buf[2*(row-1)*numXBox] +
                    bottomSlice -
                    stats.boxTable.buf[2*(row-1)*numXBox + 1];

                stats.boxTable.buf[2*row*numXBox + 1] = topSlice;

                for (int col = 1; col < numXBox; col++) {
                    // ij case
                    int x = minX + col * inc;
                    int bottomRightSlice = countWhitePixels(image, width, height, x + boxSize - inc, y + boxSize - inc, inc, inc);
                    int topRightSlice = countWhitePixels(image, width, height, x + boxSize - inc, y, inc, inc);

                    bottomSlice += bottomRightSlice - bottomLeftSlice;
                    topSlice += topRightSlice - topLeftSlice;

                    int pos = row * numXBox + col;

                    stats.boxTable.buf[2*pos] =
                        stats.boxTable.buf[2*(pos-numXBox)] +
                        bottomSlice -
                        stats.boxTable.buf[2*(pos-numXBox) + 1];

                    stats.boxTable.buf[2*pos + 1] = topSlice;

                    if (col < numXBox - 1) {
                        bottomLeftSlice = countWhitePixels(image, width, height, x, y + boxSize - inc, inc, inc);
                        topLeftSlice  = countWhitePixels(image, width, height, x, y, inc, inc);
                    }
                }
            }

            stats.el3.size = 0;
            stats.fl3.size = 0;

            int totalBoxes = numXBox * numYBox;
            for (int j = 0; j < totalBoxes; j++) {
                int count = stats.boxTable.buf[j*2];
                stats.el3.add(count);
                if (count > 0)
                    stats.fl3.add(count);
            }

            double eLac = computeCoefficientOfVariationSquared(stats.el3.buf, stats.el3.size);
            double fLac = computeCoefficientOfVariationSquared(stats.fl3.buf, stats.fl3.size);
            eLacTotal += eLac;
            fLacTotal += fLac;
            stats.eLambda3.add(eLac);
            stats.fLambda3.add(fLac);

            stats.logBoxSizes.add(Math.log((double)boxSize));
            stats.logElambda3p1.add(Math.log(eLac + 1.0));
            stats.logFlambda3p1.add(Math.log(fLac + 1.0));
        }

        stats.elMean = stats.eLambda3.size > 0 ? eLacTotal / (double)stats.eLambda3.size : 0.0;
        stats.flMean = stats.fLambda3.size > 0 ? fLacTotal / (double)stats.fLambda3.size : 0.0;
        stats.elMedial = stats.eLambda3.buf[medialBox];
        stats.flMedial = stats.fLambda3.buf[medialBox];
        stats.elCurve = findLinearRegressionFactor(stats.logBoxSizes.buf, stats.logElambda3p1.buf, numberOfBins, null);
        stats.flCurve = findLinearRegressionFactor(stats.logBoxSizes.buf, stats.logFlambda3p1.buf, numberOfBins, null);
    }

    public static int countWhitePixels(byte[] image, int width, int height, int xStart, int yStart, int dx, int dy)
    {
        int count = 0;
        int xLast = Math.min(xStart + dx, width) - 1;
        int yLast = Math.min(yStart + dy, height) - 1;

        for (int y = yStart; y <= yLast; y++) {
            for (int x = xStart; x <= xLast; x++) {
                int pixel = image[x + width * y]; // no AND with 0xff
                count -= pixel >> 31; // -1 if the pixel is white, 0 if not
            }
        }

        return count;
    }

    public static double computeCoefficientOfVariationSquared(int[] buf, int n)
    {
        if (n <= 0)
            return 0.0;

        double total = 0;
        for (int i = 0; i < n; i++)
            total += buf[i];

        double avg = total / (double)n;
        double variation = 0.0;
        for (int i = 0; i < n; i++) {
            double dAvg = (double)buf[i] - avg;
            variation += dAvg * dAvg;
        }

        double stdDev = Math.sqrt(variation / (double)n);
        double cv = stdDev / avg;
        return cv * cv;
    }

    // Modified snippet, originally from ij/measure/CurveFitter.java:
    //   Determine sum of squared residuals with linear regression.
    //   The sum of squared residuals is written to the array element with index 'numParams',
    //   the offset and factor params (if any) are written to their proper positions in the
    //   params array
    public static double findLinearRegressionFactor(double[] xData, double[] yData, int numPoints, double[] params)
    {
        double sumX=0, sumX2=0, sumXY=0; // sums for regression; here 'x' are function values
        double sumY=0, sumY2=0;          // only calculated for 'slope', otherwise we use the values calculated already

        for (int i=0; i<numPoints; i++) {
            double x = xData[i];
            double y = yData[i];
            sumX += x;
            sumX2 += x*x;
            sumXY += x*y;
            sumY2 += y*y;
            sumY += y;
        }

        final double sumWeights = (double)numPoints;

        // full linear regression or offset only. Slope is named 'factor' here
        double factor = (sumXY-sumX*sumY/sumWeights)/(sumX2-sumX*sumX/sumWeights);
        if (Double.isNaN(factor) || Double.isInfinite(factor))
            factor = 0; // all 'x' values are equal, any factor (slope) will fit

        if (params != null) {
            double offset = (sumY-factor*sumX)/sumWeights;

            double factorSqrSumX2 = factor*factor*sumX2;
            double offsetSqrSumWeights = sumWeights*offset*offset;
            double sumResidualsSqr = factorSqrSumX2 + offsetSqrSumWeights + sumY2 + 2*factor*offset*sumX - 2*factor*sumXY - 2*offset*sumY;

            // check for accuracy problem: large difference of small numbers?
            // Don't report unrealistic or even negative values, otherwise minimization could lead
            // into parameters where we have a numeric problem
            sumResidualsSqr = Math.max(sumResidualsSqr, 2e-15*(factorSqrSumX2 + offsetSqrSumWeights + sumY2));

            params[0] = offset;
            params[1] = factor;
            params[2] = sumResidualsSqr;
        }

        return factor;
    }
}
