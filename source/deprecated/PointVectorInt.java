package Batch;

public class PointVectorInt {
    public int[] buf;
    public int size;

    public PointVectorInt(int initialCap) {
        buf = initialCap > 0 ? new int[initialCap * 2] : null;
        size = 0;
    }

    public PointVectorInt() {
        this(8);
    }

    public void add(int x, int y) {
        int pos = size * 2;
        resize(size + 1);
        buf[pos] = x;
        buf[pos+1] = y;
    }

    public void resize(int newSize) {
        int oldCap = buf != null ? (buf.length / 2) : 0;
        int newCap = Math.max(oldCap, 8);
        while (newSize > newCap)
            newCap = (int)((float)newCap * 1.7f) + 1;

        if (newCap > oldCap) {
            int[] newBuf = new int[newCap * 2];
            if (buf != null && oldCap > 0)
                System.arraycopy(buf, 0, newBuf, 0, oldCap * 2);
            buf = newBuf;
        }

        size = newSize;
    }
}
