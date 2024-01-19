package math3d;

public class Eigensystem2x2Double {
   double[][] m;
   double[] eigenVectors;
   double[] eigenValues;

   public Eigensystem2x2Double(double[][] symmetricMatrix) {
      this.m = symmetricMatrix;
      if (this.m[0][1] != this.m[1][0]) {
         throw new RuntimeException("Eigensystem2x2Double only works with symmetric matrices");
      }
   }

   public void getEvalues(double[] eigenValues) {
      eigenValues[0] = this.eigenValues[0];
      eigenValues[1] = this.eigenValues[1];
   }

   public double[] getEvaluesCopy() {
      return (double[])this.eigenValues.clone();
   }

   public double[] getEvalues() {
      return this.eigenValues;
   }

   public boolean findEvalues() {
      this.eigenValues = new double[2];
      double A = this.m[0][0];
      double B = this.m[0][1];
      double C = this.m[1][1];
      double a = 1.0;
      double b = -(A + C);
      double c = A * C - B * B;
      double discriminant = b * b - 4.0 * a * c;
      if (discriminant < 0.0) {
         return false;
      } else {
         this.eigenValues[0] = (-b + Math.sqrt(discriminant)) / (2.0 * a);
         this.eigenValues[1] = (-b - Math.sqrt(discriminant)) / (2.0 * a);
         return true;
      }
   }
}
