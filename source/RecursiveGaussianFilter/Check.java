package RecursiveGaussianFilter;

public class Check {
   private static byte _b;
   private static short _s;
   private static int _i;
   private static long _l;
   private static float _f;
   private static double _d;

   public static void argument(boolean condition, String message) {
      if (!condition) {
         throw new IllegalArgumentException("required condition: " + message);
      }
   }

   public static void state(boolean condition, String message) {
      if (!condition) {
         throw new IllegalStateException("required condition: " + message);
      }
   }

   public static void index(int n, int i) {
      if (i < 0) {
         throw new IndexOutOfBoundsException("index i=" + i + " < 0");
      } else if (n <= i) {
         throw new IndexOutOfBoundsException("index i=" + i + " >= n=" + n);
      }
   }

   public static void index(byte[] a, int i) {
      _b = a[i];
   }

   public static void index(short[] a, int i) {
      _s = a[i];
   }

   public static void index(int[] a, int i) {
      _i = a[i];
   }

   public static void index(long[] a, int i) {
      _l = a[i];
   }

   public static void index(float[] a, int i) {
      _f = a[i];
   }

   public static void index(double[] a, int i) {
      _d = a[i];
   }

   private Check() {
      System.out.println(_b);
      System.out.println(_s);
      System.out.println(_i);
      System.out.println(_l);
      System.out.println(_f);
      System.out.println(_d);
   }
}
