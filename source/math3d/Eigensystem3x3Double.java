package math3d;

public class Eigensystem3x3Double {
   double[][] m;
   double[] eigenVectors;
   double[] eigenValues;

   public Eigensystem3x3Double(double[][] symmetricMatrix) {
      this.m = symmetricMatrix;
      if (this.m[0][1] != this.m[1][0] || this.m[0][2] != this.m[2][0] || this.m[1][2] != this.m[2][1]) {
         throw new RuntimeException("Eigensystem3x3Double only works with symmetric matrices");
      }
   }

   public void getEvalues(double[] eigenValues) {
      eigenValues[0] = this.eigenValues[0];
      eigenValues[1] = this.eigenValues[1];
      eigenValues[2] = this.eigenValues[2];
   }

   public double[] getEvaluesCopy() {
      return (double[])this.eigenValues.clone();
   }

   public double[] getEvalues() {
      return this.eigenValues;
   }

   public boolean findEvalues() {
      this.eigenValues = new double[3];
      double A = this.m[0][0];
      double B = this.m[0][1];
      double C = this.m[0][2];
      double D = this.m[1][1];
      double E = this.m[1][2];
      double F = this.m[2][2];
      double a = -1.0;
      double b = A + D + F;
      double c = B * B + C * C + E * E - A * D - A * F - D * F;
      double d = A * D * F - A * E * E - B * B * F + 2.0 * B * C * E - C * C * D;
      double third = 0.3333333333333333;
      double q = (3.0 * a * c - b * b) / (9.0 * a * a);
      double r = (9.0 * a * b * c - 27.0 * a * a * d - 2.0 * b * b * b) / (54.0 * a * a * a);
      double discriminant = q * q * q + r * r;
      if (discriminant > 0.0) {
         return false;
      } else if (discriminant < 0.0) {
         double rootThree = 1.7320508075688772;
         double innerSize = Math.sqrt(r * r - discriminant);
         double innerAngle;
         if (r > 0.0) {
            innerAngle = Math.atan(Math.sqrt(-discriminant) / r);
         } else {
            innerAngle = Math.PI - Math.atan(Math.sqrt(-discriminant) / -r);
         }

         double stSize = Math.pow(innerSize, 0.3333333333333333);
         double sAngle = innerAngle / 3.0;
         double tAngle = -innerAngle / 3.0;
         double sPlusT = 2.0 * stSize * Math.cos(sAngle);
         this.eigenValues[0] = sPlusT - b / (3.0 * a);
         double firstPart = -(sPlusT / 2.0) - b / 3.0 * a;
         double lastPart = -rootThree * stSize * Math.sin(sAngle);
         this.eigenValues[1] = firstPart + lastPart;
         this.eigenValues[2] = firstPart - lastPart;
         return true;
      } else {
         double sPlusT;
         if (r >= 0.0) {
            sPlusT = 2.0 * Math.pow(r, 0.3333333333333333);
         } else {
            sPlusT = -2.0 * Math.pow(-r, 0.3333333333333333);
         }

         double bOver3A = b / (3.0 * a);
         this.eigenValues[0] = sPlusT - bOver3A;
         this.eigenValues[1] = -sPlusT / 2.0 - bOver3A;
         this.eigenValues[2] = this.eigenValues[1];
         return true;
      }
   }
}
