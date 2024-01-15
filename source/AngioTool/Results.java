package AngioTool;

import Utils.Utils;
import java.io.File;
import java.util.ArrayList;

public class Results {
   public File image;
   public boolean isResized;
   public int resizingFactor;
   public int thresholdLow;
   public int thresholdHigh;
   public int removeSmallParticles;
   public int fillHoles;
   public double[] sigmas;
   public int totalNJunctions;
   public double allantoisPixelsArea;
   public double LinearScalingFactor;
   public double AreaScalingFactor;
   public double allantoisMMArea;
   public double JunctionsPerArea;
   public double JunctionsPerScaledArea;
   public double totalLength;
   public double averageBranchLength;
   public int totalNEndPoints;
   public long vesselPixelArea;
   public double vesselMMArea;
   public double vesselPercentageArea;
   public boolean computeThickness = false;
   public double averageVesselDiameter;
   public boolean computeLacunarity = false;
   public double ELacunarity;
   public double FLacuanrity;
   public double ELacunaritySlope;
   public double FLacunaritySlope;
   public double meanEl;
   public double meanFl;

   public Results() {
      this.sigmas = new double[0];
   }

   public void clear() {
      this.thresholdLow = Integer.MAX_VALUE;
      this.thresholdHigh = Integer.MIN_VALUE;
      this.sigmas = new double[0];
      this.totalNJunctions = Integer.MIN_VALUE;
      this.allantoisPixelsArea = -2.1474836E9F;
      this.LinearScalingFactor = Double.NaN;
      this.AreaScalingFactor = Double.NaN;
      this.allantoisMMArea = Double.NaN;
      this.JunctionsPerArea = Double.NaN;
      this.JunctionsPerScaledArea = Double.NaN;
      this.totalLength = Double.NaN;
      this.averageBranchLength = Double.NaN;
      this.totalNEndPoints = 0;
   }

   public String getSigmas() {
      return Utils.formatDoubleArray(sigmas);
   }

   public String getImageFilePath() {
      return this.image.getParentFile() + "/";
   }

   @Override
   public String toString() {
      String str = new String();
      return str + "  \n tresholdLow= " + this.thresholdLow + "\n tresholdHigh= " + this.thresholdHigh + "\n sigmas= " + this.sigmas.toString();
   }
}
