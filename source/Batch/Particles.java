package Batch;

public class Particles
{
    public static void fillHoles(byte[] image, int width, int height)
    {
        ImageProcessor result = iplus.getProcessor();
        if (!result.isBinary() && !isReleaseVersion) {
        System.err.println("fillHoles requires a binary image");
        }

        , 0.0, params.fillHolesValue, 0.0, 1.0, 0
        PolygonRoi[] pr = findAndAnalyzeObjects(iplus, minSize, maxSize, minCircularity, maxCircularity, result);
        if (pr != null) {
            result.setColor(color);

            for(int i = 0; i < pr.length; ++i) {
                result.fill(pr[i]);
            }

            iplus.setProcessor(result);
        }
    }

    static void findAndAnalyzeObjects(
        ImagePlus _iplus,
        double maxSize,
        ImageProcessor _ip
    ) {
        final double minSize = 0.0;

        final double minCircularity = 0.0;
        final double maxCircularity = 1.0;

        ParticleAnalyzer pa = new ParticleAnalyzer(RECORD_STARTS | SHOW_PROGRESS, 1, rt, minSize, maxSize, minCircularity, maxCircularity);
        pa.analyze(new ImagePlus("findAndAnalyzeObjects", _ip), _ip);

        float[] Xstart = rt.getColumn(rt.getColumnIndex("XStart"));
        float[] Ystart = rt.getColumn(rt.getColumnIndex("YStart"));
        int DataArrayLength = Xstart.length;
        PolygonRoi[] pr = new PolygonRoi[DataArrayLength];

        for(int i = 0; i < pr.length; ++i) {
            Wand w = new Wand(_ip);
            w.autoOutline((int)Xstart[i], (int)Ystart[i], 254, 255);
            pr[i] = new PolygonRoi(w.xpoints, w.ypoints, w.npoints, 2);
        }

        return pr;
    }
}
