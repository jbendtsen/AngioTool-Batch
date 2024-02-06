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

        int lanes = (int)Math.ceil(strokeWidth);
        double halfLaneCount = Math.floor(lanes * 0.5);

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

            double dx = x2 - x1;
            double dy = y2 - y1;
            double distance = Math.sqrt(dx*dx + dy*dy);

            double xDir = dx / distance;
            double yDir = dy / distance;

            double x = x1 + 0.5;
            double y = y1 + 0.5;
            int plots = (int)Math.ceil(distance);

            for (int j = 0; j < plots; j++) {
                for (int k = 0; k < lanes; k++) {
                    double xx = x + (k - halfLaneCount) * yDir;
                    double yy = y - (k - halfLaneCount) * xDir;

                    double xSub = xx - Math.floor(xx) - 0.5;
                    double ySub = yy - Math.floor(yy) - 0.5;
                    int xAdj = (int)xSub * 2 + 1;
                    int yAdj = (int)ySub * 2 + 1;

                    for (int l = 0; l < 4; l++) {
                        int px = (int)xx + xAdj * (l % 2);
                        int py = (int)yy + yAdj * (l / 2);

                        if (px >= 0 && py >= 0 && px < width && py < height) {
                            int p = image[px + width * py];
                            double a2 = ((p >> 24) & 0xff) / 255.0;
                            double r2 = ((p >> 16) & 0xff) / 255.0;
                            double g2 = ((p >> 8) & 0xff) / 255.0;
                            double b2 = (p & 0xff) / 255.0;

                            double a1 = alpha * (
                                (0.5 - Math.abs(xSub)) +
                                (0.5 - Math.abs(ySub))
                            );
                            a2 = 1.0 - ((1.0 - a1) * (1.0 - a2));
                            r2 = (1.0 - a1) * r2 + a1 * r1;
                            g2 = (1.0 - a1) * g2 + a1 * g1;
                            b2 = (1.0 - a1) * b2 + a1 * b1;

                            image[px + width * py] =
                                ((int)(a2 * 255.0) << 24 & 0xff000000) |
                                ((int)(r2 * 255.0) << 16 & 0xff0000) |
                                ((int)(g2 * 255.0) << 8 & 0xff00) |
                                ((int)(b2 * 255.0) & 0xff);
                        }
                    }
                }

                x += xDir;
                y += yDir;
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
                    double weight = Math.min(1.0, Math.max(0.0, 20.0 * (1.0 - (distance / radius))));
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
