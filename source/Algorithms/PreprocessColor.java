package Algorithms;

import Utils.Misc;
import java.util.Arrays;

public class PreprocessColor
{
    static final float RED_WEIGHT   = 0.299f;
    static final float GREEN_WEIGHT = 0.587f;
    static final float BLUE_WEIGHT  = 0.114f;

    public static boolean computeBrightnessTable(float[] table, int[] lineSegments, int nPoints)
    {
        if (nPoints <= 0)
            return false;

        long[] longPoints = new long[nPoints];
        for (int i = 0; i < nPoints; i++)
            longPoints[i] = ((long)lineSegments[2*i] << 32L) | ((long)lineSegments[2*i+1] & 0xFFFFffffL);

        Arrays.sort(longPoints);

        boolean invalid = false;
        int highestX = (int)(longPoints[nPoints - 1] >> 32L);

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
            float yScale = 255f / (highestX * len);

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
        int voidColor,
        float saturationFactor,
        float[] brightnessTable
    ) {
        if (weightColor <= 0f) {
            computeImageBrightness(output, pixels, width, height, brightnessTable);
            return;
        }

        float colorFactor = weightColor / (weightColor + Math.max(weightBrightness, 0f));
        float brightnessFactor = 1f - colorFactor;

        int area = width * height;

        float targetHue = Misc.getHue(targetColor);
        float voidHue = Misc.getHue(voidColor);
        if (Float.isNaN(targetHue) || Float.isNaN(voidHue)) {
            for (int i = 0; i < area; i++)
                output[i] = 0f;
            return;
        }

        float targetVoidDistance = Math.abs(targetHue - voidHue);
        float narrowingFactor = 1f / Math.min(6f - targetVoidDistance, targetVoidDistance);

        final float hueOppositeToTarget = (targetHue + 3f) % 6f;

        float highestColorValue = 0f;
        float nonSaturation = (1f - saturationFactor) * 255f;

        for (int i = 0; i < area; i++) {
            int rgb = pixels[i];
            float r = (rgb >> 16) & 0xff;
            float g = (rgb >> 8) & 0xff;
            float b = rgb & 0xff;

            float hue = hueOppositeToTarget;

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
            float diff = narrowingFactor * Math.min(6f - dHue, dHue);
            float value = (saturationFactor * dMaxMin + nonSaturation) * Math.max(1f - diff, 0f);

            output[i] = value;
            highestColorValue = Math.max(highestColorValue, value);
        }

        float scaleFactor = highestColorValue > 0f ? 255f / highestColorValue : 255f;
        float tableSizeWithScaling = (float)brightnessTable.length / 255f;
        int highestIdx = brightnessTable.length - 1;

        for (int i = 0; i < area; i++) {
            float colorValue = Math.min(scaleFactor * output[i], 255f);

            int rgb = pixels[i];
            float r = RED_WEIGHT   * ((rgb >> 16) & 0xff);
            float g = GREEN_WEIGHT * ((rgb >> 8) & 0xff);
            float b = BLUE_WEIGHT  * (rgb & 0xff);

            int idx = (int)(tableSizeWithScaling * (r+g+b));
            float brightnessValue = brightnessTable[Math.min(idx, highestIdx)];

            output[i] = colorFactor * colorValue + brightnessFactor * brightnessValue;
        }
    }

    public static void computeImageBrightness(
        float[] output,
        int[] pixels,
        int width,
        int height,
        float[] brightnessTable
    ) {
        int area = width * height;
        final float tableSizeWithScaling = (float)brightnessTable.length / 255f;
        final int highestIdx = brightnessTable.length - 1;

        for (int i = 0; i < area; i++) {
            int rgb = pixels[i];
            float r = RED_WEIGHT   * ((rgb >> 16) & 0xff);
            float g = GREEN_WEIGHT * ((rgb >> 8) & 0xff);
            float b = BLUE_WEIGHT  * (rgb & 0xff);
            int idx = (int)(tableSizeWithScaling * (r+g+b));
            output[i] = brightnessTable[Math.min(idx, highestIdx)];
        }
    }
}
