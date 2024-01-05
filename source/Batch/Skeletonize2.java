package Batch;

import ij.process.ByteProcessor;
import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

public class Skeletonize2 {
    public static ByteProcessor skeletonize(ThreadPoolExecutor threadPool, int maxWorkers, ByteProcessor ip) {
        return ip;
        /*
        byte[] layeredImage = prepareData(ip);
        computeThinImage(layeredImage);

        for(int i = 1; i <= this.inputImage.getSize(); ++i) {
            this.inputImage.getProcessor(i).multiply(255.0);
        }

        this.inputImage.update(ip);
        return this.inputImage.getProcessor(1);
        */
    }
/*
    private void prepareData(ImageStack outputImage) {
        for(int z = 0; z < this.depth; ++z) {
            for(int x = 0; x < this.width; ++x) {
                for(int y = 0; y < this.height; ++y) {
                    if (((byte[])((byte[])this.inputImage.getPixels(z + 1)))[x + y * this.width] != 0) {
                        ((byte[])outputImage.getPixels(z + 1))[x + y * this.width] = 1;
                    }
                }
            }
        }
    }

    public void computeThinImage(ImageStack outputImage) {
        int[] eulerLUT = new int[256];
        SUM.fillEulerLUT(eulerLUT);

        while (true) {
            int unchangedBorders = 0;

            for(int currentBorder = 1; currentBorder <= 6; ++currentBorder) {
                ForkJoinSkeletonize2 fs2 = new ForkJoinSkeletonize2();
                ArrayList<int[]> simpleBorderPoints = fs2.thin(outputImage, currentBorder, eulerLUT);
                boolean noChange = true;
                int[] index = null;

                for(int i = 0; i < simpleBorderPoints.size(); ++i) {
                    index = (int[])simpleBorderPoints.get(i);
                    SUM.setPixel(outputImage, index[0], index[1], index[2], (byte)0);
                    if (!SUM.isSimplePoint(SUM.getNeighborhood(outputImage, index[0], index[1], index[2]))) {
                        SUM.setPixel(outputImage, index[0], index[1], index[2], (byte)1);
                    } else {
                        noChange = false;
                    }
                }

                if (noChange) {
                    ++unchangedBorders;
                }

                simpleBorderPoints.clear();
            }

            if (unchangedBorders >= 6)
                break;
        }
    }
*/
}
