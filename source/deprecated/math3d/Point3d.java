package math3d;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class Point3d {
   public double x;
   public double y;
   public double z;

   public Point3d() {
   }

   public Point3d(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public double[] toArray() {
      return new double[]{this.x, this.y, this.z};
   }

   public float[] toArrayFloat() {
      return new float[]{(float)this.x, (float)this.y, (float)this.z};
   }

   public double[] toArrayDouble() {
      return new double[]{this.x, this.y, this.z};
   }

   public Point3d minus(Point3d other) {
      return new Point3d(this.x - other.x, this.y - other.y, this.z - other.z);
   }

   public Point3d plus(Point3d other) {
      return new Point3d(this.x + other.x, this.y + other.y, this.z + other.z);
   }

   public double scalar(Point3d other) {
      return this.x * other.x + this.y * other.y + this.z * other.z;
   }

   public Point3d times(double factor) {
      return new Point3d(this.x * factor, this.y * factor, this.z * factor);
   }

   public Point3d vector(Point3d other) {
      return new Point3d(this.y * other.z - this.z * other.y, this.z * other.x - this.x * other.z, this.x * other.y - this.y * other.x);
   }

   public double length() {
      return Math.sqrt(this.scalar(this));
   }

   public double distance2(Point3d other) {
      double x1 = this.x - other.x;
      double y1 = this.y - other.y;
      double z1 = this.z - other.z;
      return x1 * x1 + y1 * y1 + z1 * z1;
   }

   public double distanceTo(Point3d other) {
      return Math.sqrt(this.distance2(other));
   }

   public static Point3d average(Point3d[] list) {
      Point3d result = new Point3d();

      for(int i = 0; i < list.length; ++i) {
         result = result.plus(list[i]);
      }

      return result.times(1.0 / (double)list.length);
   }

   static Point3d random() {
      return new Point3d(Math.random() * 400.0 + 50.0, Math.random() * 400.0 + 50.0, Math.random() * 400.0 + 50.0);
   }

   @Override
   public String toString() {
      return this.x + " " + this.y + " " + this.z;
   }

   public static Point3d parsePoint(String s) {
      StringTokenizer st = new StringTokenizer(s, " ");
      Point3d p = new Point3d();
      p.x = Double.parseDouble(st.nextToken());
      p.y = Double.parseDouble(st.nextToken());
      p.z = Double.parseDouble(st.nextToken());
      return p;
   }

   public static Point3d[] parsePoints(String s) {
      ArrayList list = new ArrayList();
      StringTokenizer st = new StringTokenizer(s, ",");

      while(st.hasMoreTokens()) {
         list.add(parsePoint(st.nextToken().trim()));
      }

      Point3d[] result = new Point3d[list.size()];

      for(int i = 0; i < result.length; ++i) {
         result[i] = (Point3d)list.get(i);
      }

      return result;
   }

   public static void print(Point3d[] points) {
      for(int i = 0; i < points.length; ++i) {
         System.out.println((i > 0 ? "," : "") + points[i]);
      }
   }

   public static void main(String[] args) {
      String s = "127.46979200950274 127.5047385083133 28.033169558193062,153.0 123.5 0.0";
      Point3d[] p = parsePoints(s);
      print(p);
   }
}
