package Batch;

public class DoubleVector {
    public double[] buf;
    public int size;

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
            double[] newBuf = new double[newCap];
            if (buf != null && oldCap > 0)
                System.arraycopy(buf, 0, newBuf, 0, oldCap);
            buf = newBuf;
        }

        size = newSize;
        return oldSize;
    }

    public int add(double v) {
        int pos = size;
        resize(pos + 1);
        buf[pos] = v;
        return pos;
    }

    public int add(double[] data) {
        return add(data, 0, data.length);
    }

    public int add(double[] data, int off, int len) {
        int pos = this.size;
        if (off >= 0 && len > 0 && off+len <= data.length) {
            resize(pos + len);
            System.arraycopy(data, off, this.buf, pos, len);
        }
        return pos;
    }

    public int addTwo(double a, double b) {
        int pos = size;
        resize(pos + 2);
        buf[pos] = a;
        buf[pos+1] = b;
        return pos;
    }

    public int addThree(double a, double b, double c) {
        int pos = size;
        resize(pos + 3);
        buf[pos] = a;
        buf[pos+1] = b;
        buf[pos+2] = c;
        return pos;
    }

    public double popOr(double defaultValue) {
        return size > 0 ? buf[--size] : defaultValue;
    }

    public double lastOr(double defaultValue) {
        return size > 0 ? buf[size-1] : defaultValue;
    }

    public double[] copy() {
        double[] array = new double[size];
        if (size > 0)
            System.arraycopy(this.buf, 0, array, 0, size);
        return array;
    }
}
