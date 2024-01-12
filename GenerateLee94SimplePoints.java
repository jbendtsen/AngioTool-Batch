import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;

public class GenerateLee94SimplePoints {
    static final int BLOCK_SIZE = 4096;
    static final int[] OCTANT_LUT = new int[] {
        1, 1, 2, 1, 1, 2, 3, 3, 4, 1, 1, 2, 1, 2, 3, 3, 4, 5, 5, 6, 5, 5, 6, 7, 7, 8
    };

    public static void main(String[] args) {
        final int n = 1 << 26;
        final int threadCount = Math.max(4, Runtime.getRuntime().availableProcessors() - 1);
        final long[] slices = makeSlices(n, threadCount);
        final byte[] map = new byte[n / 8];

        System.out.println("Generating lee94-simple-points.bin with " + threadCount + " threads...");
        long startTime = System.nanoTime();

        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            threadCount, /* corePoolSize */
            threadCount, /* maximumPoolSize */
            10, /* keepAliveTime */
            TimeUnit.SECONDS, /* unit */
            new LinkedBlockingQueue<>() /* workQueue */
        );
        Future[] futures = new Future[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            futures[i] = threadPool.submit(() -> {
                int start = (int)(slices[idx] >> 32L);
                int length = (int)(slices[idx] & ((1L << 32L) - 1L));

                for (int j = start; j < start+length; j++) {
                    boolean isSimple = isSimplePoint(j);
                    map[j >>> 3] |= (byte)(isSimple ? (1 << (7 - (j & 7))) : 0);
                }
            });
        }

        try {
            for (int i = 0; i < threadCount; i++) {
                futures[i].get();
            }
            threadPool.shutdownNow();
        }
        catch (ExecutionException | InterruptedException ex) {
            ex.printStackTrace();
            return;
        }

        long writeTime = System.nanoTime();
        System.out.println("Writing to disk...");

        try {
            Files.write(
                FileSystems.getDefault().getPath("", "lee94-simple-points.bin"),
                map,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
            );
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        long endTime = System.nanoTime();
        System.out.println(
            "Generate time: " + (int)((double)(writeTime - startTime) / 1E6) + "ms\n" +
            "   Total time: " + (int)((double)(endTime - startTime) / 1E6) + "ms"
        );
    }

    static long[] makeSlices(int n, int threadCount) {
        long[] slices = new long[threadCount];
        int sliceSize = n / threadCount;
        sliceSize += (BLOCK_SIZE - (sliceSize % BLOCK_SIZE)) % BLOCK_SIZE;

        int start = 0;
        for (int i = 0; i < threadCount; i++) {
            int length = Math.min(sliceSize, n - start);
            slices[i] = (long)start << 32L | (long)length;
            start += length;
        }

        return slices;
    }

   public static boolean isSimplePoint(int neighborBits) {
      int label = 2;

      for(int i = 0; i < 26; ++i) {
         if (((neighborBits >>> i) & 1) == 1) {
            neighborBits = octreeLabeling(OCTANT_LUT[i], label, neighborBits);

            if (++label - 2 >= 2) {
               return false;
            }
         }
      }

      return true;
   }

    public static int octreeLabeling(int octant, int label, int neighborBits) {
      if (octant == 1) {
         if ((neighborBits & 1) == 1) {
            neighborBits &= ~1;
         }

         if (((neighborBits >> 1) & 1) == 1) {
            neighborBits &= ~(1 << 1);
            neighborBits = octreeLabeling(2, label, neighborBits);
         }

         if (((neighborBits >> 3) & 1) == 1) {
            neighborBits &= ~(1 << 3);
            neighborBits = octreeLabeling(3, label, neighborBits);
         }

         if (((neighborBits >> 4) & 1) == 1) {
            neighborBits &= ~(1 << 4);
            neighborBits = octreeLabeling(2, label, neighborBits);
            neighborBits = octreeLabeling(3, label, neighborBits);
            neighborBits = octreeLabeling(4, label, neighborBits);
         }

         if (((neighborBits >> 9) & 1) == 1) {
            neighborBits &= ~(1 << 9);
            neighborBits = octreeLabeling(5, label, neighborBits);
         }

         if (((neighborBits >> 10) & 1) == 1) {
            neighborBits &= ~(1 << 10);
            neighborBits = octreeLabeling(2, label, neighborBits);
            neighborBits = octreeLabeling(5, label, neighborBits);
            neighborBits = octreeLabeling(6, label, neighborBits);
         }

         if (((neighborBits >> 12) & 1) == 1) {
            neighborBits &= ~(1 << 12);
            neighborBits = octreeLabeling(3, label, neighborBits);
            neighborBits = octreeLabeling(5, label, neighborBits);
            neighborBits = octreeLabeling(7, label, neighborBits);
         }
      }

      if (octant == 2) {
         if (((neighborBits >> 1) & 1) == 1) {
            neighborBits &= ~(1 << 1);
            neighborBits = octreeLabeling(1, label, neighborBits);
         }

         if (((neighborBits >> 4) & 1) == 1) {
            neighborBits &= ~(1 << 4);
            neighborBits = octreeLabeling(1, label, neighborBits);
            neighborBits = octreeLabeling(3, label, neighborBits);
            neighborBits = octreeLabeling(4, label, neighborBits);
         }

         if (((neighborBits >> 10) & 1) == 1) {
            neighborBits &= ~(1 << 10);
            neighborBits = octreeLabeling(1, label, neighborBits);
            neighborBits = octreeLabeling(5, label, neighborBits);
            neighborBits = octreeLabeling(6, label, neighborBits);
         }

         if (((neighborBits >> 2) & 1) == 1) {
            neighborBits &= ~(1 << 2);
         }

         if (((neighborBits >> 5) & 1) == 1) {
            neighborBits &= ~(1 << 5);
            neighborBits = octreeLabeling(4, label, neighborBits);
         }

         if (((neighborBits >> 11) & 1) == 1) {
            neighborBits &= ~(1 << 11);
            neighborBits = octreeLabeling(6, label, neighborBits);
         }

         if (((neighborBits >> 13) & 1) == 1) {
            neighborBits &= ~(1 << 13);
            neighborBits = octreeLabeling(4, label, neighborBits);
            neighborBits = octreeLabeling(6, label, neighborBits);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }
      }

      if (octant == 3) {
         if (((neighborBits >> 3) & 1) == 1) {
            neighborBits &= ~(1 << 3);
            neighborBits = octreeLabeling(1, label, neighborBits);
         }

         if (((neighborBits >> 4) & 1) == 1) {
            neighborBits &= ~(1 << 4);
            neighborBits = octreeLabeling(1, label, neighborBits);
            neighborBits = octreeLabeling(2, label, neighborBits);
            neighborBits = octreeLabeling(4, label, neighborBits);
         }

         if (((neighborBits >> 12) & 1) == 1) {
            neighborBits &= ~(1 << 12);
            neighborBits = octreeLabeling(1, label, neighborBits);
            neighborBits = octreeLabeling(5, label, neighborBits);
            neighborBits = octreeLabeling(7, label, neighborBits);
         }

         if (((neighborBits >> 6) & 1) == 1) {
            neighborBits &= ~(1 << 6);
         }

         if (((neighborBits >> 7) & 1) == 1) {
            neighborBits &= ~(1 << 7);
            neighborBits = octreeLabeling(4, label, neighborBits);
         }

         if (((neighborBits >> 14) & 1) == 1) {
            neighborBits &= ~(1 << 14);
            neighborBits = octreeLabeling(7, label, neighborBits);
         }

         if (((neighborBits >> 15) & 1) == 1) {
            neighborBits &= ~(1 << 15);
            neighborBits = octreeLabeling(4, label, neighborBits);
            neighborBits = octreeLabeling(7, label, neighborBits);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }
      }

      if (octant == 4) {
         if (((neighborBits >> 4) & 1) == 1) {
            neighborBits &= ~(1 << 4);
            neighborBits = octreeLabeling(1, label, neighborBits);
            neighborBits = octreeLabeling(2, label, neighborBits);
            neighborBits = octreeLabeling(3, label, neighborBits);
         }

         if (((neighborBits >> 5) & 1) == 1) {
            neighborBits &= ~(1 << 5);
            neighborBits = octreeLabeling(2, label, neighborBits);
         }

         if (((neighborBits >> 13) & 1) == 1) {
            neighborBits &= ~(1 << 13);
            neighborBits = octreeLabeling(2, label, neighborBits);
            neighborBits = octreeLabeling(6, label, neighborBits);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }

         if (((neighborBits >> 7) & 1) == 1) {
            neighborBits &= ~(1 << 7);
            neighborBits = octreeLabeling(3, label, neighborBits);
         }

         if (((neighborBits >> 15) & 1) == 1) {
            neighborBits &= ~(1 << 15);
            neighborBits = octreeLabeling(3, label, neighborBits);
            neighborBits = octreeLabeling(7, label, neighborBits);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }

         if (((neighborBits >> 8) & 1) == 1) {
            neighborBits &= ~(1 << 8);
         }

         if (((neighborBits >> 16) & 1) == 1) {
            neighborBits &= ~(1 << 16);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }
      }

      if (octant == 5) {
         if (((neighborBits >> 9) & 1) == 1) {
            neighborBits &= ~(1 << 9);
            neighborBits = octreeLabeling(1, label, neighborBits);
         }

         if (((neighborBits >> 10) & 1) == 1) {
            neighborBits &= ~(1 << 10);
            neighborBits = octreeLabeling(1, label, neighborBits);
            neighborBits = octreeLabeling(2, label, neighborBits);
            neighborBits = octreeLabeling(6, label, neighborBits);
         }

         if (((neighborBits >> 12) & 1) == 1) {
            neighborBits &= ~(1 << 12);
            neighborBits = octreeLabeling(1, label, neighborBits);
            neighborBits = octreeLabeling(3, label, neighborBits);
            neighborBits = octreeLabeling(7, label, neighborBits);
         }

         if (((neighborBits >> 17) & 1) == 1) {
            neighborBits &= ~(1 << 17);
         }

         if (((neighborBits >> 18) & 1) == 1) {
            neighborBits &= ~(1 << 18);
            neighborBits = octreeLabeling(6, label, neighborBits);
         }

         if (((neighborBits >> 20) & 1) == 1) {
            neighborBits &= ~(1 << 20);
            neighborBits = octreeLabeling(7, label, neighborBits);
         }

         if (((neighborBits >> 21) & 1) == 1) {
            neighborBits &= ~(1 << 21);
            neighborBits = octreeLabeling(6, label, neighborBits);
            neighborBits = octreeLabeling(7, label, neighborBits);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }
      }

      if (octant == 6) {
         if (((neighborBits >> 10) & 1) == 1) {
            neighborBits &= ~(1 << 10);
            neighborBits = octreeLabeling(1, label, neighborBits);
            neighborBits = octreeLabeling(2, label, neighborBits);
            neighborBits = octreeLabeling(5, label, neighborBits);
         }

         if (((neighborBits >> 11) & 1) == 1) {
            neighborBits &= ~(1 << 11);
            neighborBits = octreeLabeling(2, label, neighborBits);
         }

         if (((neighborBits >> 13) & 1) == 1) {
            neighborBits &= ~(1 << 13);
            neighborBits = octreeLabeling(2, label, neighborBits);
            neighborBits = octreeLabeling(4, label, neighborBits);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }

         if (((neighborBits >> 18) & 1) == 1) {
            neighborBits &= ~(1 << 18);
            neighborBits = octreeLabeling(5, label, neighborBits);
         }

         if (((neighborBits >> 21) & 1) == 1) {
            neighborBits &= ~(1 << 21);
            neighborBits = octreeLabeling(5, label, neighborBits);
            neighborBits = octreeLabeling(7, label, neighborBits);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }

         if (((neighborBits >> 19) & 1) == 1) {
            neighborBits &= ~(1 << 19);
         }

         if (((neighborBits >> 22) & 1) == 1) {
            neighborBits &= ~(1 << 22);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }
      }

      if (octant == 7) {
         if (((neighborBits >> 12) & 1) == 1) {
            neighborBits &= ~(1 << 12);
            neighborBits = octreeLabeling(1, label, neighborBits);
            neighborBits = octreeLabeling(3, label, neighborBits);
            neighborBits = octreeLabeling(5, label, neighborBits);
         }

         if (((neighborBits >> 14) & 1) == 1) {
            neighborBits &= ~(1 << 14);
            neighborBits = octreeLabeling(3, label, neighborBits);
         }

         if (((neighborBits >> 15) & 1) == 1) {
            neighborBits &= ~(1 << 15);
            neighborBits = octreeLabeling(3, label, neighborBits);
            neighborBits = octreeLabeling(4, label, neighborBits);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }

         if (((neighborBits >> 20) & 1) == 1) {
            neighborBits &= ~(1 << 20);
            neighborBits = octreeLabeling(5, label, neighborBits);
         }

         if (((neighborBits >> 21) & 1) == 1) {
            neighborBits &= ~(1 << 21);
            neighborBits = octreeLabeling(5, label, neighborBits);
            neighborBits = octreeLabeling(6, label, neighborBits);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }

         if (((neighborBits >> 23) & 1) == 1) {
            neighborBits &= ~(1 << 23);
         }

         if (((neighborBits >> 24) & 1) == 1) {
            neighborBits &= ~(1 << 24);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }
      }

      if (octant == 8) {
         if (((neighborBits >> 13) & 1) == 1) {
            neighborBits &= ~(1 << 13);
            neighborBits = octreeLabeling(2, label, neighborBits);
            neighborBits = octreeLabeling(4, label, neighborBits);
            neighborBits = octreeLabeling(6, label, neighborBits);
         }

         if (((neighborBits >> 15) & 1) == 1) {
            neighborBits &= ~(1 << 15);
            neighborBits = octreeLabeling(3, label, neighborBits);
            neighborBits = octreeLabeling(4, label, neighborBits);
            neighborBits = octreeLabeling(7, label, neighborBits);
         }

         if (((neighborBits >> 16) & 1) == 1) {
            neighborBits &= ~(1 << 16);
            neighborBits = octreeLabeling(4, label, neighborBits);
         }

         if (((neighborBits >> 21) & 1) == 1) {
            neighborBits &= ~(1 << 21);
            neighborBits = octreeLabeling(5, label, neighborBits);
            neighborBits = octreeLabeling(6, label, neighborBits);
            neighborBits = octreeLabeling(7, label, neighborBits);
         }

         if (((neighborBits >> 22) & 1) == 1) {
            neighborBits &= ~(1 << 22);
            neighborBits = octreeLabeling(6, label, neighborBits);
         }

         if (((neighborBits >> 24) & 1) == 1) {
            neighborBits &= ~(1 << 24);
            neighborBits = octreeLabeling(7, label, neighborBits);
         }

         if (((neighborBits >> 25) & 1) == 1) {
            neighborBits &= ~(1 << 25);
         }
      }
      
      return neighborBits;
   }
}
