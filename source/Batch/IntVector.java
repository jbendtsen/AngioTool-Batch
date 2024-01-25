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

    public void resize(int newSize) {
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
    }

    public void fillSizeWithZeroes() {
        final int s = size;
        for (int i = 0; i < s; i++)
            buf[i] = 0;
    }

    public void add(int v) {
        int pos = size;
        resize(pos + 1);
        buf[pos] = v;
    }

    public void add(int[] data) {
        add(data, 0, data.length);
    }

    public void add(int[] data, int off, int len) {
        if (off >= 0 && len > 0 && off+len <= data.length) {
            int pos = this.size;
            resize(pos + len);
            System.arraycopy(data, off, this.buf, pos, len);
        }
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
