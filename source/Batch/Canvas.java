package Batch;

public class Canvas
{
    public static void drawLines(
        int[] image,
        int width,
        int height,
        int[] index, // optional
        int[] points,
        int arraySize,
        int pointSize,
        int rgbColor,
        double strokeWidth
    ) {
        pointSize = Math.max(pointSize, 2);

        double alpha = ((rgbColor >> 24) & 0xff) / 255.0;
        double r1 = ((rgbColor >> 16) & 0xff) / 255.0;
        double g1 = ((rgbColor >> 8) & 0xff) / 255.0;
        double b1 = (rgbColor & 0xff) / 255.0;

        double strokeDistance = 0.5 + strokeWidth * 0.5;

        int idxJump = index != null ? 1 : pointSize;

        for (int i = 0; i < arraySize; i += idxJump) {
            int next = (i+idxJump) % arraySize;
            int idx, idxNext;
            if (index != null) {
                idx = index[i];
                idxNext = index[next];
            }
            else {
                idx = i;
                idxNext = next;
            }

            int x1 = points[i];
            int y1 = points[i+1];
            int x2 = points[next];
            int y2 = points[next+1];

            int firstX, lastX;
            if (x1 < x2) {
                firstX = x1;
                lastX = x2;
            }
            else {
                firstX = x2;
                lastX = x1;
            }

            int firstY, lastY;
            if (y1 < y2) {
                firstY = y1;
                lastY = y2;
            }
            else {
                firstY = y2;
                lastY = y1;
            }

            int startX = Math.min(Math.max(firstX - (int)strokeWidth, 0), width - 1);
            int startY = Math.min(Math.max(firstY - (int)strokeWidth, 0), height - 1);
            int endX   = Math.min(Math.max(lastX + (int)strokeWidth,  0), width - 1);
            int endY   = Math.min(Math.max(lastY + (int)strokeWidth,  0), height - 1);

            int dx = x2 - x1;
            int dy = y2 - y1;
            double maxDistance = (dx*dx + dy*dy) * strokeDistance * strokeDistance;

            for (int y = startY; y <= endY; y++) {
                for (int x = startX; x <= endX; x++) {
                    double distance = 0;
                    if (dx == 0) {
                        if (y < firstY || y > lastY)
                            distance = maxDistance;
                        else
                            distance = dy * dy * (x1 - x) * (x1 - x);
                    }
                    else {
                        // using <= and >= for y covers the horizontal line case
                        if ((x < firstX || x > lastX) && (y <= firstY || y >= lastY)) {
                            distance = maxDistance;
                        }
                        else {
                            distance = dx * (double)(y2 - y) - dy * (double)(x2 - x);
                            distance *= distance;
                        }
                    }

                    if (distance >= maxDistance)
                        continue;

                    double a1 = alpha * (1.0 - (distance / maxDistance));

                    int p = image[x + width * y];
                    double a2 = ((p >> 24) & 0xff) / 255.0;
                    double r2 = ((p >> 16) & 0xff) / 255.0;
                    double g2 = ((p >> 8) & 0xff) / 255.0;
                    double b2 = (p & 0xff) / 255.0;

                    a2 = 1.0 - ((1.0 - a1) * (1.0 - a2));
                    r2 = (1.0 - a1) * r2 + a1 * r1;
                    g2 = (1.0 - a1) * g2 + a1 * g1;
                    b2 = (1.0 - a1) * b2 + a1 * b1;

                    image[x + width * y] =
                        ((int)(a2 * 255.0) << 24 & 0xff000000) |
                        ((int)(r2 * 255.0) << 16 & 0xff0000) |
                        ((int)(g2 * 255.0) << 8 & 0xff00) |
                        ((int)(b2 * 255.0) & 0xff);
                }
            }
        }
    }

    public static void drawCircles(
        int[] image,
        int width,
        int height,
        int[] index, // optional
        int[] points,
        int arraySize,
        int pointSize,
        int rgbColor,
        double radius
    ) {
        pointSize = Math.max(pointSize, 2);

        double alpha = ((rgbColor >> 24) & 0xff) / 255.0;
        rgbColor &= 0xffFFff;

        double r1 = ((rgbColor >> 16) & 0xff) / 255.0;
        double g1 = ((rgbColor >> 8) & 0xff) / 255.0;
        double b1 = (rgbColor & 0xff) / 255.0;

        int pixDiameter = (int)Math.ceil(radius * 2);
        pixDiameter += 1 - (pixDiameter % 2);

        int startOff = -pixDiameter / 2;

        int idxJump = index != null ? 1 : pointSize;
        radius += 0.5;

        for (int i = 0; i < arraySize; i += idxJump) {
            int idx = index != null ? index[i] : i;
            int x = points[idx];
            int y = points[idx+1];

            for (int j = 0; j < pixDiameter; j++) {
                int yy = y + j + startOff;
                if (yy < 0 || yy >= height)
                    continue;

                for (int k = 0; k < pixDiameter; k++) {
                    int xx = x + k + startOff;
                    if (xx < 0 || xx >= width)
                        continue;

                    double dx = xx - x;
                    double dy = yy - y;
                    double distance = Math.sqrt(dx*dx + dy*dy);
                    double weight = Math.min(1.0, Math.max(0.0, 3.0 * (1.0 - (distance / radius))));
                    double a1 = weight * alpha;

                    int p = image[xx + width * yy];
                    double a2 = ((p >> 24) & 0xff) / 255.0;
                    double r2 = ((p >> 16) & 0xff) / 255.0;
                    double g2 = ((p >> 8) & 0xff) / 255.0;
                    double b2 = (p & 0xff) / 255.0;

                    a2 = 1.0 - ((1.0 - a1) * (1.0 - a2));
                    r2 = (1.0 - a1) * r2 + a1 * r1;
                    g2 = (1.0 - a1) * g2 + a1 * g1;
                    b2 = (1.0 - a1) * b2 + a1 * b1;

                    image[xx + width * yy] =
                        ((int)(a2 * 255.0) << 24 & 0xff000000) |
                        ((int)(r2 * 255.0) << 16 & 0xff0000) |
                        ((int)(g2 * 255.0) << 8 & 0xff00) |
                        ((int)(b2 * 255.0) & 0xff);
                }
            }
        }
    }
}
