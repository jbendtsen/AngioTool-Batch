package Batch;

public class Outline
{
    public static class Scratch
    {
        
    }

    public void findOutline(byte[] image, int width, int height)
    {
        ArrayList polygons = new ArrayList();

      for (int y = 0; y <= height; y++) {
         for (int x = 0; x <= width; x++) {
            boolean aboveLeftFilled = y > 0 && y < height && x > 0 && x < width && image[x-1 + width * (y-1)] != 0;
            boolean aboveFilled     = y > 0 && y < height &&          x < width && image[x   + width * (y-1)] != 0;
            boolean leftFilled      = y < height && x > 0 && x < width && image[x-1 + width * y] != 0;
            boolean currentFilled   = y < height &&          x < width && image[x   + width * y] != 0;

            if (thisRow[x + 1]) {
               if (!prevRow[x + 1]) {
                  if (outline[x] == null) {
                     if (outline[x + 1] == null) {
                        outline[x + 1] = outline[x] = new ThresholdToSelection.Outline();
                        outline[x].push(x + 1, y);
                        outline[x].push(x, y);
                     } else {
                        outline[x] = outline[x + 1];
                        outline[x + 1] = null;
                        outline[x].push(x, y);
                     }
                  } else if (outline[x + 1] == null) {
                     outline[x + 1] = outline[x];
                     outline[x] = null;
                     outline[x + 1].shift(x + 1, y);
                  } else if (outline[x + 1] == outline[x]) {
                     polygons.add(outline[x].getPolygon());
                     outline[x] = outline[x + 1] = null;
                  } else {
                     outline[x].shift(outline[x + 1]);

                     for(int x1 = 0; x1 <= this.w; ++x1) {
                        if (x1 != x + 1 && outline[x1] == outline[x + 1]) {
                           outline[x1] = outline[x];
                           outline[x] = outline[x + 1] = null;
                           break;
                        }
                     }

                     if (outline[x] != null) {
                        throw new RuntimeException("assertion failed");
                     }
                  }
               }

               if (!thisRow[x]) {
                  if (outline[x] == null) {
                     throw new RuntimeException("assertion failed!");
                  }

                  outline[x].push(x, y + 1);
               }
            } else {
               if (prevRow[x + 1]) {
                  if (outline[x] == null) {
                     if (outline[x + 1] == null) {
                        outline[x] = outline[x + 1] = new ThresholdToSelection.Outline();
                        outline[x].push(x, y);
                        outline[x].push(x + 1, y);
                     } else {
                        outline[x] = outline[x + 1];
                        outline[x + 1] = null;
                        outline[x].shift(x, y);
                     }
                  } else if (outline[x + 1] == null) {
                     outline[x + 1] = outline[x];
                     outline[x] = null;
                     outline[x + 1].push(x + 1, y);
                  } else if (outline[x + 1] == outline[x]) {
                     polygons.add(outline[x].getPolygon());
                     outline[x] = outline[x + 1] = null;
                  } else {
                     outline[x].push(outline[x + 1]);

                     for(int x1 = 0; x1 <= this.w; ++x1) {
                        if (x1 != x + 1 && outline[x1] == outline[x + 1]) {
                           outline[x1] = outline[x];
                           outline[x] = outline[x + 1] = null;
                           break;
                        }
                     }

                     if (outline[x] != null) {
                        throw new RuntimeException("assertion failed");
                     }
                  }
               }

               if (thisRow[x]) {
                  if (outline[x] == null) {
                     throw new RuntimeException("assertion failed");
                  }

                  outline[x].shift(x, y + 1);
               }
            }
         }
      }

      GeneralPath path = new GeneralPath(0);

      for(int i = 0; i < polygons.size(); ++i) {
         path.append((Polygon)polygons.get(i), false);
      }

      ShapeRoi shape = new ShapeRoi(path);
      Roi roi = shape != null ? shape.shapeToRoi() : null;
      return (Roi)(roi != null ? roi : shape);
    }
}
