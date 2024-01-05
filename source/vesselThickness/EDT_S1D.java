package vesselThickness;

import Batch.ForkStep3;
import Utils.Utils;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class EDT_S1D implements PlugInFilter {
   private ImagePlus imp;
   public byte[][] data;
   public int w;
   public int h;
   public int d;
   public int thresh;
   public boolean inverse;
   ImagePlus impOut;

   final ThreadPoolExecutor threadPool;

   public EDT_S1D(ThreadPoolExecutor threadPool) {
      this.threadPool = threadPool;
   }

   public int setup(String arg, ImagePlus imp) {
      this.imp = imp;
      return 1;
   }

   public void run(ImageProcessor ip) {
      long start = System.currentTimeMillis();
      ImageStack stack = this.imp.getStack();
      this.w = stack.getWidth();
      this.h = stack.getHeight();
      this.d = this.imp.getStackSize();
      int nThreads = Runtime.getRuntime().availableProcessors();
      this.thresh = 200;
      this.data = new byte[this.d][];

      for(int k = 0; k < this.d; ++k) {
         this.data[k] = (byte[])stack.getPixels(k + 1);
      }

      ImageStack sStack = new ImageStack(this.w, this.h);
      float[][] s = new float[this.d][];

      for(int k = 0; k < this.d; ++k) {
         ImageProcessor ipk = new FloatProcessor(this.w, this.h);
         sStack.addSlice(null, ipk);
         s[k] = (float[])ipk.getPixels();
      }

      float[] sk = null;
      long s1 = System.currentTimeMillis();
      if (!Utils.isReleaseVersion) {
         System.out.println("EDT transformation 1/3");
      }

      EDT_S1D.Step1Thread[] s1t = new EDT_S1D.Step1Thread[nThreads];

      for(int thread = 0; thread < nThreads; ++thread)
         s1t[thread] = new EDT_S1D.Step1Thread(thread, nThreads, this.w, this.h, this.d, this.thresh, s, this.data);

      runAndJoinAll(threadPool, s1t, 1);

      long s2 = System.currentTimeMillis();
      if (!Utils.isReleaseVersion) {
         System.out.println("EDT transformation 2/3");
      }

      //ForkStep2 fs2 = new ForkStep2();
      //ArrayList<float[]> f = fs2.thin(s[0], this.w, this.h);
      //s[0] = f.get(1);

      ForkStep3.thin(threadPool, nThreads, s[0], this.w, this.h);

      long s3 = System.currentTimeMillis();
      if (!Utils.isReleaseVersion) {
         System.out.println("EDT transformation 3/3");
      }

      EDT_S1D.Step3Thread[] s3t = new EDT_S1D.Step3Thread[nThreads];

      for(int thread = 0; thread < nThreads; ++thread)
         s3t[thread] = new EDT_S1D.Step3Thread(thread, nThreads, this.w, this.h, this.d, s, this.data);

      runAndJoinAll(threadPool, s3t, 3);

      float distMax = 0.0F;
      int wh = this.w * this.h;

      for(int k = 0; k < this.d; ++k) {
         sk = s[k];

         for(int ind = 0; ind < wh; ++ind) {
            if ((this.data[k][ind] & 255) < this.thresh ^ this.inverse) {
               sk[ind] = 0.0F;
            } else {
               float dist = (float)Math.sqrt((double)sk[ind]);
               sk[ind] = dist;
               distMax = dist > distMax ? dist : distMax;
            }
         }
      }

      String title = this.stripExtension(this.imp.getTitle());
      this.impOut = new ImagePlus(title + "EDT", sStack);
      this.impOut.getProcessor().setMinAndMax(0.0, (double)distMax);
      long end = System.currentTimeMillis();
   }

   static void runAndJoinAll(ThreadPoolExecutor pool, Runnable[] tasks, int stepNumber) {
      Future[] futures = new Future[tasks.length];
      for (int i = 0; i < tasks.length; i++)
         futures[i] = pool.submit(tasks[i]);

      for (int i = 0; i < tasks.length; i++) {
         try {
            futures[i].get();
         }
         catch (InterruptedException ex) {
            System.err.println("A thread was interrupted in step " + stepNumber);
         }
         catch (ExecutionException ex) {
            System.err.println("A thread in step " + stepNumber + " threw " + ex.getCause().getClass().getSimpleName());
         }
      }
   }

   public ImagePlus getImageResult() {
      return this.impOut;
   }

   String stripExtension(String name) {
      if (name != null) {
         int dotIndex = name.lastIndexOf(".");
         if (dotIndex >= 0) {
            name = name.substring(0, dotIndex);
         }
      }

      return name;
   }

   boolean getScale() {
      this.thresh = (int)Prefs.get("edtS1.thresh", 128.0);
      this.inverse = Prefs.get("edtS1.inverse", false);
      GenericDialog gd = new GenericDialog("EDT...", IJ.getInstance());
      gd.addNumericField("Threshold (1 to 255; value < thresh is background)", (double)this.thresh, 0);
      gd.addCheckbox("Inverse case (background when value >= thresh)", this.inverse);
      gd.showDialog();
      if (gd.wasCanceled()) {
         return false;
      } else {
         this.thresh = (int)gd.getNextNumber();
         this.inverse = gd.getNextBoolean();
         Prefs.set("edtS1.thresh", this.thresh);
         Prefs.set("edtS1.inverse", this.inverse);
         return true;
      }
   }

   class Step1Thread implements Runnable {
      int thread;
      int nThreads;
      int w;
      int h;
      int d;
      int thresh;
      float[][] s;
      byte[][] data;

      public Step1Thread(int thread, int nThreads, int w, int h, int d, int thresh, float[][] s, byte[][] data) {
         this.thread = thread;
         this.nThreads = nThreads;
         this.w = w;
         this.h = h;
         this.d = d;
         this.thresh = thresh;
         this.data = data;
         this.s = s;
      }

      @Override
      public void run() {
         int n = this.w;
         if (this.h > n) {
            n = this.h;
         }

         if (this.d > n) {
            n = this.d;
         }

         int noResult = 3 * (n + 1) * (n + 1);
         boolean[] background = new boolean[n];

         for(int k = this.thread; k < this.d; k += this.nThreads) {
            IJ.showProgress((double)k / (1.0 * (double)this.d));
            float[] sk = this.s[k];
            byte[] dk = this.data[k];

            for(int j = 0; j < this.h; ++j) {
               for(int i = 0; i < this.w; ++i) {
                  background[i] = (dk[i + this.w * j] & 255) < this.thresh ^ EDT_S1D.this.inverse;
               }

               for(int i = 0; i < this.w; ++i) {
                  int min = noResult;

                  for(int x = i; x < this.w; ++x) {
                     if (background[x]) {
                        int test = i - x;
                        test *= test;
                        min = test;
                        break;
                     }
                  }

                  for(int x = i - 1; x >= 0; --x) {
                     if (background[x]) {
                        int test = i - x;
                        test *= test;
                        if (test < min) {
                           min = test;
                        }
                        break;
                     }
                  }

                  sk[i + this.w * j] = (float)min;
               }
            }
         }
      }
   }

   class Step3Thread implements Runnable {
      int thread;
      int nThreads;
      int w;
      int h;
      int d;
      float[][] s;
      byte[][] data;

      public Step3Thread(int thread, int nThreads, int w, int h, int d, float[][] s, byte[][] data) {
         this.thread = thread;
         this.nThreads = nThreads;
         this.w = w;
         this.h = h;
         this.d = d;
         this.s = s;
         this.data = data;
      }

      @Override
      public void run() {
         int n = this.w;
         if (this.h > n) {
            n = this.h;
         }

         if (this.d > n) {
            n = this.d;
         }

         int noResult = 3 * (n + 1) * (n + 1);
         int[] tempInt = new int[n];
         int[] tempS = new int[n];

         for(int j = this.thread; j < this.h; j += this.nThreads) {
            IJ.showProgress((double)j / (1.0 * (double)this.h));

            for(int i = 0; i < this.w; ++i) {
               boolean nonempty = false;

               for(int k = 0; k < this.d; ++k) {
                  tempS[k] = (int)this.s[k][i + this.w * j];
                  if (tempS[k] > 0) {
                     nonempty = true;
                  }
               }

               if (nonempty) {
                  int zStart = 0;

                  while(zStart < this.d - 1 && tempS[zStart] == 0) {
                     ++zStart;
                  }

                  if (zStart > 0) {
                     --zStart;
                  }

                  int zStop = this.d - 1;

                  while(zStop > 0 && tempS[zStop] == 0) {
                     --zStop;
                  }

                  if (zStop < this.d - 1) {
                     ++zStop;
                  }

                  for(int k = 0; k < this.d; ++k) {
                     if ((this.data[k][i + this.w * j] & 255) >= EDT_S1D.this.thresh ^ EDT_S1D.this.inverse) {
                        int min = noResult;
                        int zBegin = zStart;
                        int zEnd = zStop;
                        if (zStart > k) {
                           zBegin = k;
                        }

                        if (zStop < k) {
                           zEnd = k;
                        }

                        int delta = k - zBegin;

                        for(int z = zBegin; z <= zEnd; ++z) {
                           int test = tempS[z] + delta * delta--;
                           if (test < min) {
                              min = test;
                           }
                        }

                        tempInt[k] = min;
                     }
                  }

                  for(int k = 0; k < this.d; ++k) {
                     this.s[k][i + this.w * j] = (float)tempInt[k];
                  }
               }
            }
         }
      }
   }
}
