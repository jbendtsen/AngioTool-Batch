package Algorithms;

import Utils.*;

public class ConvexHull
{
    public static double findConvexHull(IntVector points, byte[] image, int width, int height)
    {
        points.size = 0;

        boolean hasEqualLowestHeight = false;
        int firstY = height;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (image[x + width * y] == -1) {
                    hasEqualLowestHeight = hasEqualLowestHeight || y == firstY;
                    firstY = Math.min(y, firstY);
                    points.addTwo(x, y);
                }
            }
        }

        final int n = points.size;

        if (n >= 2)
            points.addTwo(points.buf[0], points.buf[1]);

        int[] xy = points.buf;

        int min = 0;
        int m = -2;
        double minAngle = hasEqualLowestHeight ? -1.0 : 0.0;

        while (min != n) {
            m += 2;
            int temp = xy[m];
            xy[m] = xy[min];
            xy[min] = temp;
            temp = xy[m+1];
            xy[m+1] = xy[min+1];
            xy[min+1] = temp;

            min = n;
            double v = minAngle;
            minAngle = 4.0;
            int h2 = 0;

            for (int i = m + 2; i < n + 2; i += 2) {
                int dx = xy[i] - xy[m];
                int ax = Math.abs(dx);
                int dy = xy[i+1] - xy[m+1];
                int ay = Math.abs(dy);
                double t;
                if (dx == 0 && dy == 0) {
                    t = 0.0;
                }
                else {
                    t = (double)dy / (double)(ax + ay);
                }

                if (dx < 0) {
                    t = 2.0 - t;
                }
                else if (dy < 0) {
                    t += 4.0;
                }

                if (t > v) {
                    if (t < minAngle) {
                        min = i;
                        minAngle = t;
                        h2 = dx * dx + dy * dy;
                    }
                    else if (t == minAngle) {
                        int h = dx * dx + dy * dy;
                        if (h > h2) {
                            min = i;
                            h2 = h;
                        }
                    }
                }
            }
        }

        points.resize(m + 2);

        double signedArea = 0.0;
        for (int i = 0; i < points.size; i += 2) {
            int next = (i+2) % points.size;
            signedArea += (double)(points.buf[i] * points.buf[next+1]) - (double)(points.buf[i+1] * points.buf[next]);
        }

        double area = Math.abs(0.5 * signedArea);
        return area;
    }
}
