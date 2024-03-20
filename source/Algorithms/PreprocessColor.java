package Algorithms;

import Utils.Misc;
import java.util.Arrays;

public class PreprocessColor
{
    public static boolean computeBrightnessTable(float[] table, int[] lineSegments, int nPoints)
    {
        if (nPoints <= 0)
            return false;

        long[] longPoints = new long[nPoints];
        for (int i = 0; i < nPoints; i++)
            longPoints[i] = (lineSegments[2*i] << 32L) | (lineSegments[2*i+1] & 0xFFFFffffL);

        Arrays.sort(longPoints);

        boolean invalid = false;
        int highestX = -1;
        for (int i = 0; i < nPoints; i++) {
            int x = (int)(longPoints[i] >> 32L);
            if (x <= highestX) {
                invalid = true;
                break;
            }
            highestX = x;
        }

        if (highestX <= 0)
            invalid = true;

        if (table == null)
            return !invalid;

        int tableLen = table.length;
        float size = (float)tableLen;

        if (invalid) {
            for (int i = 0; i < tableLen; i++)
                table[i] = (float)i / size;
            return false;
        }

        Arrays.fill(table, 0, tableLen, 0f);

        float xScale = size / (float)highestX;

        int x1 = (int)(longPoints[0] >> 32L);
        int y1 = (int)(longPoints[0] & 0xFFFFffffL);

        for (int i = 1; i < nPoints; i++) {
            int x2 = (int)(longPoints[i] >> 32L);
            int y2 = (int)(longPoints[i] & 0xFFFFffffL);

            int start = (int)(x1 * xScale);
            int end   = (int)(x2 * xScale);
            int len = end - start;
            float yScale = 1f / (highestX * len);

            for (int j = start, pos = 0; j < end; j++, pos++)
                table[j] = (float)((len-pos) * y1 + pos * y2) * yScale;

            x1 = x2;
            y1 = y2;
        }

        return true;
    }

    public static void transformToMonoFloatArray(
        float[] output,
        int[] pixels,
        int width,
        int height,
        float weightColor,
        float weightBrightness,
        int targetColor,
        float narrowingFactor,
        boolean useTrueLuminance,
        float[] brightnessTable
    ) {
        float redWeight, greenWeight, blueWeight;
        if (useTrueLuminance) {
            redWeight = 0.299f;
            greenWeight = 0.587f;
            blueWeight = 0.114f;
        }
        else {
            redWeight = 0.328125f;
            greenWeight = 0.34375f;
            blueWeight = 0.328125f;
        }

        if (weightColor <= 0f) {
            computeImageBrightness(output, pixels, width, height, brightnessTable, redWeight, greenWeight, blueWeight);
            return;
        }

        float colorFactor = weightColor / (weightColor + Math.max(weightBrightness, 0f));
        float brightnessFactor = 1f - colorFactor;

        final float tableSizeWithScaling = (float)brightnessTable.length / 255f;
        final int highestIdx = brightnessTable.length - 1;

        int area = width * height;
        float nf = (narrowingFactor > 0f ? narrowingFactor : 1f) / 3f;

        float targetHue = Misc.getHue(targetColor);
        if (Float.isNaN(targetHue)) {
            for (int i = 0; i < area; i++)
                output[i] = 0f;
            return;
        }

        for (int i = 0; i < area; i++) {
            int rgb = pixels[i];
            int r = (rgb >> 16) & 0xff;
            int g = (rgb >> 8) & 0xff;
            int b = rgb & 0xff;

            float hue = targetHue;
            float max = Math.max(Math.max(r, g), b);
            float dMaxMin = max - Math.min(Math.min(r, g), b);
            if (dMaxMin != 0f) {
                if (max == (float)r) {
                    hue = (g - b) / dMaxMin;
                }
                else if (max == (float)g) {
                    hue = 2f + (b - r) / dMaxMin;
                }
                else {
                    hue = 4f + (r - g) / dMaxMin;
                }
            }

            float dHue = Math.abs(hue - targetHue);
            float diff = nf * Math.min(6f - dHue, dHue);
            float colorValue = Math.min(Math.max(1f - diff, 0f), 1f);

            int idx = (int)(tableSizeWithScaling * (r*redWeight + g*greenWeight + b*blueWeight));
            float brightnessValue = brightnessTable[Math.min(idx, highestIdx)];

            output[i] = colorFactor * colorValue + brightnessFactor * brightnessValue;
        }
    }

    public static void computeImageBrightness(
        float[] output,
        int[] pixels,
        int width,
        int height,
        float[] brightnessTable,
        float redWeight,
        float greenWeight,
        float blueWeight
    ) {
        int area = width * height;
        final float tableSizeWithScaling = (float)brightnessTable.length / 255f;
        final int highestIdx = brightnessTable.length - 1;

        for (int i = 0; i < area; i++) {
            int rgb = pixels[i];
            float r = redWeight   * ((rgb >> 16) & 0xff);
            float g = greenWeight * ((rgb >> 8) & 0xff);
            float b = blueWeight  * (rgb & 0xff);
            int idx = (int)(tableSizeWithScaling * (r+g+b));
            output[i] = brightnessTable[Math.min(idx, highestIdx)];
        }
    }
}
