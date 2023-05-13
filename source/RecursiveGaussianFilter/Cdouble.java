package RecursiveGaussianFilter;

public class Cdouble {
   public static final Cdouble DBL_I = new Cdouble(0.0, 1.0);
   public double r;
   public double i;

   public Cdouble() {
      this(0.0, 0.0);
   }

   public Cdouble(double r) {
      this(r, 0.0);
   }

   public Cdouble(double r, double i) {
      this.r = r;
      this.i = i;
   }

   public Cdouble(Cdouble x) {
      this(x.r, x.i);
   }

   public Cdouble plus(Cdouble x) {
      return new Cdouble(this).plusEquals(x);
   }

   public Cdouble minus(Cdouble x) {
      return new Cdouble(this).minusEquals(x);
   }

   public Cdouble times(Cdouble x) {
      return new Cdouble(this).timesEquals(x);
   }

   public Cdouble over(Cdouble x) {
      return new Cdouble(this).overEquals(x);
   }

   public Cdouble plus(double x) {
      return new Cdouble(this).plusEquals(x);
   }

   public Cdouble minus(double x) {
      return new Cdouble(this).minusEquals(x);
   }

   public Cdouble times(double x) {
      return new Cdouble(this).timesEquals(x);
   }

   public Cdouble over(double x) {
      return new Cdouble(this).overEquals(x);
   }

   public Cdouble plusEquals(Cdouble x) {
      this.r += x.r;
      this.i += x.i;
      return this;
   }

   public Cdouble minusEquals(Cdouble x) {
      this.r -= x.r;
      this.i -= x.i;
      return this;
   }

   public Cdouble timesEquals(Cdouble x) {
      double tr = this.r;
      double ti = this.i;
      double xr = x.r;
      double xi = x.i;
      this.r = tr * xr - ti * xi;
      this.i = tr * xi + ti * xr;
      return this;
   }

   public Cdouble overEquals(Cdouble x) {
      double tr = this.r;
      double ti = this.i;
      double xr = x.r;
      double xi = x.i;
      double d = norm(x);
      this.r = (tr * xr + ti * xi) / d;
      this.i = (ti * xr - tr * xi) / d;
      return this;
   }

   public Cdouble plusEquals(double x) {
      this.r += x;
      return this;
   }

   public Cdouble minusEquals(double x) {
      this.r -= x;
      return this;
   }

   public Cdouble timesEquals(double x) {
      this.r *= x;
      this.i *= x;
      return this;
   }

   public Cdouble overEquals(double x) {
      this.r /= x;
      this.i /= x;
      return this;
   }

   public Cdouble conjEquals() {
      this.i = -this.i;
      return this;
   }

   public Cdouble invEquals() {
      this.r = -this.r;
      this.i = -this.i;
      return this;
   }

   public Cdouble negEquals() {
      double d = this.norm();
      this.r /= d;
      this.i = -this.i / d;
      return this;
   }

   public boolean isReal() {
      return this.i == 0.0;
   }

   public boolean isImag() {
      return this.r == 0.0;
   }

   public Cdouble conj() {
      return new Cdouble(this.r, -this.i);
   }

   public Cdouble inv() {
      double d = this.norm();
      return new Cdouble(this.r / d, -this.i / d);
   }

   public Cdouble neg() {
      return new Cdouble(-this.r, -this.i);
   }

   public double abs() {
      return abs(this);
   }

   public double arg() {
      return arg(this);
   }

   public double norm() {
      return norm(this);
   }

   public Cdouble sqrt() {
      return sqrt(this);
   }

   public Cdouble exp() {
      return exp(this);
   }

   public Cdouble log() {
      return log(this);
   }

   public Cdouble log10() {
      return log10(this);
   }

   public Cdouble pow(double y) {
      return pow(this, y);
   }

   public Cdouble pow(Cdouble y) {
      return pow(this, y);
   }

   public Cdouble sin() {
      return sin(this);
   }

   public Cdouble cos() {
      return cos(this);
   }

   public Cdouble tan() {
      return tan(this);
   }

   public Cdouble sinh() {
      return sinh(this);
   }

   public Cdouble cosh() {
      return cosh(this);
   }

   public Cdouble tanh() {
      return tanh(this);
   }

   public static boolean isReal(Cdouble x) {
      return x.i == 0.0;
   }

   public static boolean isImag(Cdouble x) {
      return x.r == 0.0;
   }

   public static Cdouble conj(Cdouble x) {
      return new Cdouble(x.r, -x.i);
   }

   public Cdouble inv(Cdouble x) {
      double d = x.norm();
      return new Cdouble(x.r / d, -x.i / d);
   }

   public static Cdouble neg(Cdouble x) {
      return new Cdouble(-x.r, -x.i);
   }

   public static Cdouble polar(double r, double a) {
      return new Cdouble(r * cos(a), r * sin(a));
   }

   public static Cdouble add(Cdouble x, Cdouble y) {
      return x.plus(y);
   }

   public static Cdouble sub(Cdouble x, Cdouble y) {
      return x.minus(y);
   }

   public static Cdouble mul(Cdouble x, Cdouble y) {
      return x.times(y);
   }

   public static Cdouble div(Cdouble x, Cdouble y) {
      return x.over(y);
   }

   public static double abs(Cdouble x) {
      double ar = abs(x.r);
      double ai = abs(x.i);
      double s = max(abs(ar), abs(ai));
      if (s == 0.0) {
         return 0.0;
      } else {
         ar /= s;
         ai /= s;
         return s * sqrt(ar * ar + ai * ai);
      }
   }

   public static double arg(Cdouble x) {
      return atan2(x.i, x.r);
   }

   public static double norm(Cdouble x) {
      return x.r * x.r + x.i * x.i;
   }

   public static Cdouble sqrt(Cdouble x) {
      if (x.r == 0.0) {
         double t = sqrt(0.5 * abs(x.i));
         return new Cdouble(t, x.i < 0.0 ? -t : t);
      } else {
         double t = sqrt(2.0 * (abs(x) + abs(x.r)));
         double u = 0.5 * t;
         return x.r > 0.0 ? new Cdouble(u, x.i / t) : new Cdouble(abs(x.i) / t, x.i < 0.0 ? -u : u);
      }
   }

   public static Cdouble exp(Cdouble x) {
      return polar(exp(x.r), x.i);
   }

   public static Cdouble log(Cdouble x) {
      return new Cdouble(log(abs(x)), arg(x));
   }

   public static Cdouble log10(Cdouble x) {
      return log(x).overEquals(log(10.0));
   }

   public static Cdouble pow(Cdouble x, double y) {
      if (x.i == 0.0) {
         return new Cdouble(pow(x.r, y));
      } else {
         Cdouble t = log(x);
         return polar(exp(y * t.r), y * t.i);
      }
   }

   public static Cdouble pow(double x, Cdouble y) {
      return x == 0.0 ? new Cdouble() : polar(pow(x, y.r), y.i * log(x));
   }

   public static Cdouble pow(Cdouble x, Cdouble y) {
      return x.r == 0.0 && x.i == 0.0 ? new Cdouble() : exp(y.times(log(x)));
   }

   public static Cdouble sin(Cdouble x) {
      return new Cdouble(sin(x.r) * cosh(x.i), cos(x.r) * sinh(x.i));
   }

   public static Cdouble cos(Cdouble x) {
      return new Cdouble(cos(x.r) * cosh(x.i), -sin(x.r) * sinh(x.i));
   }

   public static Cdouble tan(Cdouble x) {
      return sin(x).overEquals(cos(x));
   }

   public static Cdouble sinh(Cdouble x) {
      return new Cdouble(sinh(x.r) * cos(x.i), cosh(x.r) * sin(x.i));
   }

   public static Cdouble cosh(Cdouble x) {
      return new Cdouble(cosh(x.r) * cos(x.i), sinh(x.r) * sin(x.i));
   }

   public static Cdouble tanh(Cdouble x) {
      return sinh(x).overEquals(cosh(x));
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         Cdouble that = (Cdouble)obj;
         return this.r == that.r && this.i == that.i;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      long rbits = Double.doubleToLongBits(this.r);
      long ibits = Double.doubleToLongBits(this.i);
      return (int)(rbits ^ rbits >>> 32 ^ ibits ^ ibits >>> 32);
   }

   @Override
   public String toString() {
      if (this.i == 0.0) {
         return "(" + this.r + "+0.0i)";
      } else {
         return this.i > 0.0 ? "(" + this.r + "+" + this.i + "i)" : "(" + this.r + "-" + -this.i + "i)";
      }
   }

   private static double max(double x, double y) {
      return x >= y ? x : y;
   }

   private static double abs(double x) {
      return x >= 0.0 ? x : -x;
   }

   private static double sqrt(double x) {
      return Math.sqrt(x);
   }

   private static double sin(double x) {
      return Math.sin(x);
   }

   private static double cos(double x) {
      return Math.cos(x);
   }

   private static double sinh(double x) {
      return Math.sinh(x);
   }

   private static double cosh(double x) {
      return Math.cosh(x);
   }

   private static double exp(double x) {
      return Math.exp(x);
   }

   private static double log(double x) {
      return Math.log(x);
   }

   private static double pow(double x, double y) {
      return Math.pow(x, y);
   }

   private static double atan2(double y, double x) {
      return Math.atan2(y, x);
   }
}
