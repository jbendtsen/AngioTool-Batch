package Utils;

public class DoubleVector {
    public double[] buf;
    public int size;
    public int sp;

    public DoubleVector(int initialCap) {
        this.buf = new double[initialCap];
        this.size = 0;
    }
    public DoubleVector() {
        this(8);
    }

    public int resize(int newSize) {
        int oldSize = size;

        int oldCap = buf != null ? buf.length : 0;
        int newCap = Math.max(oldCap, 8);
        while (newSize > newCap)
            newCap = (int)((float)newCap * 1.7f) + 1;

        if (newCap > oldCap) {
            double[] newBuf = new double[newCap-sp];
            if (buf != null && oldCap > 0)
                System.arraycopy(buf, sp, newBuf, 0, oldCap-sp);
            buf = newBuf;
            newSize -= sp;
            sp = 0;
        }

        size = newSize;
        return oldSize;
    }

    public int add(double v) {
        resize(size + 1);
        buf[size-1] = v;
        return size-1;
    }

    public int add(double[] data) {
        return add(data, 0, data.length);
    }

    public int add(double[] data, int off, int len) {
        if (off >= 0 && len > 0 && off+len <= data.length) {
            resize(this.size + len);
            System.arraycopy(data, off, this.buf, this.size-len, len);
        }
        return this.size - len;
    }

    public int addTwo(double a, double b) {
        resize(size + 2);
        buf[size-2] = a;
        buf[size-1] = b;
        return size-2;
    }

    public int addThree(double a, double b, double c) {
        resize(size + 3);
        buf[size-3] = a;
        buf[size-2] = b;
        buf[size-1] = c;
        return size-3;
    }

    public int addFour(double a, double b, double c, double d) {
        resize(size + 4);
        buf[size-4] = a;
        buf[size-3] = b;
        buf[size-2] = c;
        buf[size-1] = d;
        return size-4;
    }

    public double popOr(int defaultValue) {
        return size > 0 ? buf[--size] : defaultValue;
    }

    public double lastOr(int defaultValue) {
        return size > 0 ? buf[size-1] : defaultValue;
    }

    public double[] copy() {
        double[] array = new double[size-sp];
        if (size > 0)
            System.arraycopy(this.buf, sp, array, 0, size-sp);
        return array;
    }
}
