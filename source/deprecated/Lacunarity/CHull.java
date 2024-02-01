package Lacunarity;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

class CHull {
   static int distance(Point p1, Point p2, Point p3) {
      int x1 = p1.x;
      int x2 = p2.x;
      int x3 = p3.x;
      int y1 = p1.y;
      int y2 = p2.y;
      int y3 = p3.y;
      return x1 * y2 + x3 * y1 + x2 * y3 - x3 * y2 - x2 * y1 - x1 * y3;
   }

   static Rectangle getBounds(ArrayList<Point> array) {
      return getPolygon(array).getBounds();
   }

   static Polygon getPolygon(ArrayList<Point> array) {
      Polygon p = new Polygon();
      ArrayList<Point> hull = cHull(array);
      Iterator<Point> itr = hull.iterator();
      Point curr = null;

      while(itr.hasNext()) {
         curr = itr.next();
         p.addPoint(curr.x, curr.y);
      }

      return p;
   }

   static ArrayList<Point> cHull(ArrayList<Point> array) {
      Collections.sort(array, new Comparator<Point>() {
         public int compare(Point pt1, Point pt2) {
            int r = pt1.x - pt2.x;
            return r != 0 ? r : pt1.y - pt2.y;
         }
      });
      int size = array.size();
      if (size < 2) {
         return null;
      } else {
         Point l = array.get(0);
         Point r = array.get(size - 1);
         ArrayList<Point> path = new ArrayList<>();
         path.add(l);
         cHull(array, l, r, path);
         path.add(r);
         cHull(array, r, l, path);
         return path;
      }
   }

   static void cHull(ArrayList<Point> points, Point l, Point r, ArrayList<Point> path) {
      if (points.size() >= 3) {
         int maxDist = 0;
         Point p = null;

         for(Point pt : points) {
            if (pt != l && pt != r) {
               int tmp = distance(l, r, pt);
               if (tmp > maxDist) {
                  maxDist = tmp;
                  p = pt;
               }
            }
         }

         ArrayList<Point> left = new ArrayList<>();
         ArrayList<Point> right = new ArrayList<>();
         left.add(l);
         right.add(p);

         for(Point pt : points) {
            if (distance(l, p, pt) > 0) {
               left.add(pt);
            } else if (distance(p, r, pt) > 0) {
               right.add(pt);
            }
         }

         left.add(p);
         right.add(r);
         cHull(left, l, p, path);
         path.add(p);
         cHull(right, p, r, path);
      }
   }
}
