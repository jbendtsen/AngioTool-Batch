package Batch;

import ij.process.ByteProcessor;
import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

public class Skeletonize2 {
    public static void skeletonize(ThreadPoolExecutor threadPool, int maxWorkers, ByteProcessor ip) {
        byte[] image = (byte[])ip.getPixels();
        int width = ip.getWidth();
        int height = ip.getHeight();
        Zha84.skeletonizeZha84(image, width, height);
    }
}
