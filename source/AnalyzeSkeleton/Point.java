package AnalyzeSkeleton;

public class Point {
   public int x = 0;
   public int y = 0;
   public int z = 0;

   public Point(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   @Override
   public String toString() {
      return new String("(" + this.x + ", " + this.y + ", " + this.z + ")");
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Point p = (Point)o;
         return p.x == this.x && p.y == this.y && p.z == this.z;
      } else {
         return false;
      }
   }
}
