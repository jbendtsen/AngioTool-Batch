package RecursiveGaussianFilter;

public class MathPlus {
   public static final double E = Math.E;
   public static final float FLT_E = (float) Math.E;
   public static final double DBL_E = Math.E;
   public static final double PI = Math.PI;
   public static final float FLT_PI = (float) Math.PI;
   public static final double DBL_PI = Math.PI;
   public static final float FLT_MAX = Float.MAX_VALUE;
   public static final float FLT_MIN = Float.MIN_VALUE;
   public static final float FLT_EPSILON = 1.1920929E-7F;
   public static final double DBL_MAX = Double.MAX_VALUE;
   public static final double DBL_MIN = Double.MIN_VALUE;
   public static final double DBL_EPSILON = 2.220446E-16F;

   public static float sin(float x) {
      return (float)Math.sin((double)x);
   }

   public static double sin(double x) {
      return Math.sin(x);
   }

   public static float cos(float x) {
      return (float)Math.cos((double)x);
   }

   public static double cos(double x) {
      return Math.cos(x);
   }

   public static float tan(float x) {
      return (float)Math.tan((double)x);
   }

   public static double tan(double x) {
      return Math.tan(x);
   }

   public static float asin(float x) {
      return (float)Math.asin((double)x);
   }

   public static double asin(double x) {
      return Math.asin(x);
   }

   public static float acos(float x) {
      return (float)Math.acos((double)x);
   }

   public static double acos(double x) {
      return Math.acos(x);
   }

   public static float atan(float x) {
      return (float)Math.atan((double)x);
   }

   public static double atan(double x) {
      return Math.atan(x);
   }

   public static float atan2(float y, float x) {
      return (float)Math.atan2((double)y, (double)x);
   }

   public static double atan2(double y, double x) {
      return Math.atan2(y, x);
   }

   public static float toRadians(float angdeg) {
      return (float)Math.toRadians((double)angdeg);
   }

   public static double toRadians(double angdeg) {
      return Math.toRadians(angdeg);
   }

   public static float toDegrees(float angrad) {
      return (float)Math.toDegrees((double)angrad);
   }

   public static double toDegrees(double angrad) {
      return Math.toDegrees(angrad);
   }

   public static float exp(float x) {
      return (float)Math.exp((double)x);
   }

   public static double exp(double x) {
      return Math.exp(x);
   }

   public static float log(float x) {
      return (float)Math.log((double)x);
   }

   public static double log(double x) {
      return Math.log(x);
   }

   public static float log10(float x) {
      return (float)Math.log10((double)x);
   }

   public static double log10(double x) {
      return Math.log10(x);
   }

   public static float sqrt(float x) {
      return (float)Math.sqrt((double)x);
   }

   public static double sqrt(double x) {
      return Math.sqrt(x);
   }

   public static float pow(float x, float y) {
      return (float)Math.pow((double)x, (double)y);
   }

   public static double pow(double x, double y) {
      return Math.pow(x, y);
   }

   public static float sinh(float x) {
      return (float)Math.sinh((double)x);
   }

   public static double sinh(double x) {
      return 0.5 * (Math.exp(x) - Math.exp(-x));
   }

   public static float cosh(float x) {
      return (float)Math.cosh((double)x);
   }

   public static double cosh(double x) {
      return 0.5 * (Math.exp(x) + Math.exp(-x));
   }

   public static float tanh(float x) {
      return (float)tanh((double)x);
   }

   public static double tanh(double x) {
      double ep = Math.exp(x);
      double em = Math.exp(-x);
      return (ep - em) / (ep + em);
   }

   public static float ceil(float x) {
      return (float)Math.ceil((double)x);
   }

   public static double ceil(double x) {
      return Math.ceil(x);
   }

   public static float floor(float x) {
      return (float)Math.floor((double)x);
   }

   public static double floor(double x) {
      return Math.floor(x);
   }

   public static float rint(float x) {
      return (float)Math.rint((double)x);
   }

   public static double rint(double x) {
      return Math.rint(x);
   }

   public static int round(float x) {
      return Math.round(x);
   }

   public static long round(double x) {
      return Math.round(x);
   }

   public static float signum(float x) {
      return x > 0.0F ? 1.0F : (x < 0.0F ? -1.0F : 0.0F);
   }

   public static double signum(double x) {
      return x > 0.0 ? 1.0 : (x < 0.0 ? -1.0 : 0.0);
   }

   public static int abs(int x) {
      return x > 0 ? x : -x;
   }

   public static long abs(long x) {
      return x > 0L ? x : -x;
   }

   public static float abs(float x) {
      return x > 0.0F ? x : -x;
   }

   public static double abs(double x) {
      return x >= 0.0 ? x : -x;
   }

   public static int max(int a, int b) {
      return a >= b ? a : b;
   }

   public static int max(int a, int b, int c) {
      return max(a, max(b, c));
   }

   public static int max(int a, int b, int c, int d) {
      return max(a, max(b, max(c, d)));
   }

   public static long max(long a, long b) {
      return a >= b ? a : b;
   }

   public static long max(long a, long b, long c) {
      return max(a, max(b, c));
   }

   public static long max(long a, long b, long c, long d) {
      return max(a, max(b, max(c, d)));
   }

   public static float max(float a, float b) {
      return a >= b ? a : b;
   }

   public static float max(float a, float b, float c) {
      return max(a, max(b, c));
   }

   public static float max(float a, float b, float c, float d) {
      return max(a, max(b, max(c, d)));
   }

   public static double max(double a, double b) {
      return a >= b ? a : b;
   }

   public static double max(double a, double b, double c) {
      return max(a, max(b, c));
   }

   public static double max(double a, double b, double c, double d) {
      return max(a, max(b, max(c, d)));
   }

   public static int min(int a, int b) {
      return a <= b ? a : b;
   }

   public static int min(int a, int b, int c) {
      return min(a, min(b, c));
   }

   public static int min(int a, int b, int c, int d) {
      return min(a, min(b, min(c, d)));
   }

   public static long min(long a, long b) {
      return a <= b ? a : b;
   }

   public static long min(long a, long b, long c) {
      return min(a, min(b, c));
   }

   public static long min(long a, long b, long c, long d) {
      return min(a, min(b, min(c, d)));
   }

   public static float min(float a, float b) {
      return a <= b ? a : b;
   }

   public static float min(float a, float b, float c) {
      return min(a, min(b, c));
   }

   public static float min(float a, float b, float c, float d) {
      return min(a, min(b, min(c, d)));
   }

   public static double min(double a, double b) {
      return a <= b ? a : b;
   }

   public static double min(double a, double b, double c) {
      return min(a, min(b, c));
   }

   public static double min(double a, double b, double c, double d) {
      return min(a, min(b, min(c, d)));
   }

   private MathPlus() {
   }
}
