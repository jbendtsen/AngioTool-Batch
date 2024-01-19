package math3d;

public class Eigensystem2x2Float {
   float[][] m;
   float[] eigenVectors;
   float[] eigenValues;

   public Eigensystem2x2Float(float[][] symmetricMatrix) {
      this.m = symmetricMatrix;
      if (this.m[0][1] != this.m[1][0]) {
         throw new RuntimeException("Eigensystem2x2Float only works with symmetric matrices");
      }
   }

   public void getEvalues(float[] eigenValues) {
      eigenValues[0] = this.eigenValues[0];
      eigenValues[1] = this.eigenValues[1];
   }

   public float[] getEvaluesCopy() {
      return (float[])this.eigenValues.clone();
   }

   public float[] getEvalues() {
      return this.eigenValues;
   }

   public boolean findEvalues() {
      this.eigenValues = new float[2];
      double A = (double)this.m[0][0];
      double B = (double)this.m[0][1];
      double C = (double)this.m[1][1];
      double a = 1.0;
      double b = -(A + C);
      double c = A * C - B * B;
      double discriminant = b * b - 4.0 * a * c;
      if (discriminant < 0.0) {
         return false;
      } else {
         this.eigenValues[0] = (float)((-b + Math.sqrt(discriminant)) / (2.0 * a));
         this.eigenValues[1] = (float)((-b - Math.sqrt(discriminant)) / (2.0 * a));
         return true;
      }
   }
}
