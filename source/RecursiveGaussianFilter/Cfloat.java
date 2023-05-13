package RecursiveGaussianFilter;

public class Cfloat {
   public static final Cfloat FLT_I = new Cfloat(0.0F, 1.0F);
   public float r;
   public float i;

   public Cfloat() {
      this(0.0F, 0.0F);
   }

   public Cfloat(float r) {
      this(r, 0.0F);
   }

   public Cfloat(float r, float i) {
      this.r = r;
      this.i = i;
   }

   public Cfloat(Cfloat x) {
      this(x.r, x.i);
   }

   public Cfloat plus(Cfloat x) {
      return new Cfloat(this).plusEquals(x);
   }

   public Cfloat minus(Cfloat x) {
      return new Cfloat(this).minusEquals(x);
   }

   public Cfloat times(Cfloat x) {
      return new Cfloat(this).timesEquals(x);
   }

   public Cfloat over(Cfloat x) {
      return new Cfloat(this).overEquals(x);
   }

   public Cfloat plus(float x) {
      return new Cfloat(this).plusEquals(x);
   }

   public Cfloat minus(float x) {
      return new Cfloat(this).minusEquals(x);
   }

   public Cfloat times(float x) {
      return new Cfloat(this).timesEquals(x);
   }

   public Cfloat over(float x) {
      return new Cfloat(this).overEquals(x);
   }

   public Cfloat plusEquals(Cfloat x) {
      this.r += x.r;
      this.i += x.i;
      return this;
   }

   public Cfloat minusEquals(Cfloat x) {
      this.r -= x.r;
      this.i -= x.i;
      return this;
   }

   public Cfloat timesEquals(Cfloat x) {
      float tr = this.r;
      float ti = this.i;
      float xr = x.r;
      float xi = x.i;
      this.r = tr * xr - ti * xi;
      this.i = tr * xi + ti * xr;
      return this;
   }

   public Cfloat overEquals(Cfloat x) {
      float tr = this.r;
      float ti = this.i;
      float xr = x.r;
      float xi = x.i;
      float d = norm(x);
      this.r = (tr * xr + ti * xi) / d;
      this.i = (ti * xr - tr * xi) / d;
      return this;
   }

   public Cfloat plusEquals(float x) {
      this.r += x;
      return this;
   }

   public Cfloat minusEquals(float x) {
      this.r -= x;
      return this;
   }

   public Cfloat timesEquals(float x) {
      this.r *= x;
      this.i *= x;
      return this;
   }

   public Cfloat overEquals(float x) {
      this.r /= x;
      this.i /= x;
      return this;
   }

   public Cfloat conjEquals() {
      this.i = -this.i;
      return this;
   }

   public Cfloat invEquals() {
      this.r = -this.r;
      this.i = -this.i;
      return this;
   }

   public Cfloat negEquals() {
      float d = this.norm();
      this.r /= d;
      this.i = -this.i / d;
      return this;
   }

   public boolean isReal() {
      return this.i == 0.0F;
   }

   public boolean isImag() {
      return this.r == 0.0F;
   }

   public Cfloat conj() {
      return new Cfloat(this.r, -this.i);
   }

   public Cfloat inv() {
      float d = this.norm();
      return new Cfloat(this.r / d, -this.i / d);
   }

   public Cfloat neg() {
      return new Cfloat(-this.r, -this.i);
   }

   public float abs() {
      return abs(this);
   }

   public float arg() {
      return arg(this);
   }

   public float norm() {
      return norm(this);
   }

   public Cfloat sqrt() {
      return sqrt(this);
   }

   public Cfloat exp() {
      return exp(this);
   }

   public Cfloat log() {
      return log(this);
   }

   public Cfloat log10() {
      return log10(this);
   }

   public Cfloat pow(float y) {
      return pow(this, y);
   }

   public Cfloat pow(Cfloat y) {
      return pow(this, y);
   }

   public Cfloat sin() {
      return sin(this);
   }

   public Cfloat cos() {
      return cos(this);
   }

   public Cfloat tan() {
      return tan(this);
   }

   public Cfloat sinh() {
      return sinh(this);
   }

   public Cfloat cosh() {
      return cosh(this);
   }

   public Cfloat tanh() {
      return tanh(this);
   }

   public static boolean isReal(Cfloat x) {
      return x.i == 0.0F;
   }

   public static boolean isImag(Cfloat x) {
      return x.r == 0.0F;
   }

   public static Cfloat conj(Cfloat x) {
      return new Cfloat(x.r, -x.i);
   }

   public Cfloat inv(Cfloat x) {
      float d = x.norm();
      return new Cfloat(x.r / d, -x.i / d);
   }

   public static Cfloat neg(Cfloat x) {
      return new Cfloat(-x.r, -x.i);
   }

   public static Cfloat polar(float r, float a) {
      return new Cfloat(r * cos(a), r * sin(a));
   }

   public static Cfloat add(Cfloat x, Cfloat y) {
      return x.plus(y);
   }

   public static Cfloat sub(Cfloat x, Cfloat y) {
      return x.minus(y);
   }

   public static Cfloat mul(Cfloat x, Cfloat y) {
      return x.times(y);
   }

   public static Cfloat div(Cfloat x, Cfloat y) {
      return x.over(y);
   }

   public static float abs(Cfloat x) {
      float ar = abs(x.r);
      float ai = abs(x.i);
      float s = max(abs(ar), abs(ai));
      if (s == 0.0F) {
         return 0.0F;
      } else {
         ar /= s;
         ai /= s;
         return s * sqrt(ar * ar + ai * ai);
      }
   }

   public static float arg(Cfloat x) {
      return atan2(x.i, x.r);
   }

   public static float norm(Cfloat x) {
      return x.r * x.r + x.i * x.i;
   }

   public static Cfloat sqrt(Cfloat x) {
      if (x.r == 0.0F) {
         float t = sqrt(0.5F * abs(x.i));
         return new Cfloat(t, x.i < 0.0F ? -t : t);
      } else {
         float t = sqrt(2.0F * (abs(x) + abs(x.r)));
         float u = 0.5F * t;
         return x.r > 0.0F ? new Cfloat(u, x.i / t) : new Cfloat(abs(x.i) / t, x.i < 0.0F ? -u : u);
      }
   }

   public static Cfloat exp(Cfloat x) {
      return polar(exp(x.r), x.i);
   }

   public static Cfloat log(Cfloat x) {
      return new Cfloat(log(abs(x)), arg(x));
   }

   public static Cfloat log10(Cfloat x) {
      return log(x).overEquals(log(10.0F));
   }

   public static Cfloat pow(Cfloat x, float y) {
      if (x.i == 0.0F) {
         return new Cfloat(pow(x.r, y));
      } else {
         Cfloat t = log(x);
         return polar(exp(y * t.r), y * t.i);
      }
   }

   public static Cfloat pow(float x, Cfloat y) {
      return x == 0.0F ? new Cfloat() : polar(pow(x, y.r), y.i * log(x));
   }

   public static Cfloat pow(Cfloat x, Cfloat y) {
      return x.r == 0.0F && x.i == 0.0F ? new Cfloat() : exp(y.times(log(x)));
   }

   public static Cfloat sin(Cfloat x) {
      return new Cfloat(sin(x.r) * cosh(x.i), cos(x.r) * sinh(x.i));
   }

   public static Cfloat cos(Cfloat x) {
      return new Cfloat(cos(x.r) * cosh(x.i), -sin(x.r) * sinh(x.i));
   }

   public static Cfloat tan(Cfloat x) {
      return sin(x).overEquals(cos(x));
   }

   public static Cfloat sinh(Cfloat x) {
      return new Cfloat(sinh(x.r) * cos(x.i), cosh(x.r) * sin(x.i));
   }

   public static Cfloat cosh(Cfloat x) {
      return new Cfloat(cosh(x.r) * cos(x.i), sinh(x.r) * sin(x.i));
   }

   public static Cfloat tanh(Cfloat x) {
      return sinh(x).overEquals(cosh(x));
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         Cfloat that = (Cfloat)obj;
         return this.r == that.r && this.i == that.i;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Float.floatToIntBits(this.r) ^ Float.floatToIntBits(this.i);
   }

   @Override
   public String toString() {
      if (this.i == 0.0F) {
         return "(" + this.r + "+0.0i)";
      } else {
         return this.i > 0.0F ? "(" + this.r + "+" + this.i + "i)" : "(" + this.r + "-" + -this.i + "i)";
      }
   }

   private static float max(float x, float y) {
      return x >= y ? x : y;
   }

   private static float abs(float x) {
      return x >= 0.0F ? x : -x;
   }

   private static float sqrt(float x) {
      return (float)Math.sqrt((double)x);
   }

   private static float sin(float x) {
      return (float)Math.sin((double)x);
   }

   private static float cos(float x) {
      return (float)Math.cos((double)x);
   }

   private static float sinh(float x) {
      return (float)Math.sinh((double)x);
   }

   private static float cosh(float x) {
      return (float)Math.cosh((double)x);
   }

   private static float exp(float x) {
      return (float)Math.exp((double)x);
   }

   private static float log(float x) {
      return (float)Math.log((double)x);
   }

   private static float pow(float x, float y) {
      return (float)Math.pow((double)x, (double)y);
   }

   private static float atan2(float y, float x) {
      return (float)Math.atan2((double)y, (double)x);
   }
}
