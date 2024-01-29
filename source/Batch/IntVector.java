package Batch;

public class IntVector {
    public int[] buf;
    public int size;

    public IntVector(int initialCap) {
        this.buf = new int[initialCap];
        this.size = 0;
    }
    public IntVector() {
        this(8);
    }

    public int resize(int newSize) {
        int oldSize = size;

        int oldCap = buf != null ? buf.length : 0;
        int newCap = Math.max(oldCap, 8);
        while (newSize > newCap)
            newCap = (int)((float)newCap * 1.7f) + 1;

        if (newCap > oldCap) {
            int[] newBuf = new int[newCap];
            if (buf != null && oldCap > 0)
                System.arraycopy(buf, 0, newBuf, 0, oldCap);
            buf = newBuf;
        }

        size = newSize;
        return oldSize;
    }

    public int add(int v) {
        int pos = size;
        resize(pos + 1);
        buf[pos] = v;
        return pos;
    }

    public int add(int[] data) {
        return add(data, 0, data.length);
    }

    public int add(int[] data, int off, int len) {
        int pos = this.size;
        if (off >= 0 && len > 0 && off+len <= data.length) {
            resize(pos + len);
            System.arraycopy(data, off, this.buf, pos, len);
        }
        return pos;
    }

    public int addTwo(int a, int b) {
        int pos = size;
        resize(pos + 2);
        buf[pos] = a;
        buf[pos+1] = b;
        return pos;
    }

    public int addThree(int a, int b, int c) {
        int pos = size;
        resize(pos + 3);
        buf[pos] = a;
        buf[pos+1] = b;
        buf[pos+2] = c;
        return pos;
    }

    public int popOr(int defaultValue) {
        return size > 0 ? buf[--size] : defaultValue;
    }

    public int lastOr(int defaultValue) {
        return size > 0 ? buf[size-1] : defaultValue;
    }

    public int[] copy() {
        int[] array = new int[size];
        if (size > 0)
            System.arraycopy(this.buf, 0, array, 0, size);
        return array;
    }
}
