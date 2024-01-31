package Batch;

import java.io.OutputStream;

public class ByteVectorOutputStream extends OutputStream {
    public byte[] buf;
    public int size;

    public ByteVectorOutputStream(int initialCap) {
        this.buf = new byte[initialCap];
        this.size = 0;
    }
    public ByteVectorOutputStream() {
        this(32);
    }

    public void resize(int newSize) {
        int oldCap = buf != null ? buf.length : 0;
        int newCap = Math.max(oldCap, 8);
        while (newSize > newCap)
            newCap = (int)((float)newCap * 1.7f) + 1;

        if (newCap > oldCap) {
            byte[] newBuf = new byte[newCap];
            if (buf != null && oldCap > 0)
                System.arraycopy(buf, 0, newBuf, 0, oldCap);
            buf = newBuf;
        }

        size = newSize;
    }

    @Override public void write(int b) { add((byte)(b & 0xff)); }
    @Override public void write(byte[] buf) { add(buf, 0, buf.length); }
    @Override public void write(byte[] buf, int off, int len) { add(buf, off, len); }
    @Override public void flush() {}
    @Override public void close() {}

    public void add(byte v) {
        int pos = size;
        resize(pos + 1);
        buf[pos] = v;
    }
    public void add(char v) {
        int pos = size;
        resize(pos + 1);
        buf[pos] = (byte)v;
    }

    public void add(String str) {
        byte[] bytes = str.getBytes();
        add(bytes, 0, bytes.length);
    }

    public void add(byte[] data) {
        add(data, 0, data.length);
    }

    public void add(byte[] data, int off, int len) {
        if (len > 0 && off >= 0 && off+len <= data.length) {
            int pos = this.size;
            resize(pos + len);
            System.arraycopy(data, off, this.buf, pos, len);
        }
    }

    public void addMany(byte v, int n) {
        if (n > 0) {
            int pos = this.size;
            resize(pos + n);
            for (int i = 0; i < n; i++)
                this.buf[pos+i] = v;
        }
    }
    public void addMany(char v, int n) {
        addMany((byte)v, n);
    }

    public byte popOr(byte defaultValue) {
        return size > 0 ? buf[--size] : defaultValue;
    }

    public byte lastOr(byte defaultValue) {
        return size > 0 ? buf[size-1] : defaultValue;
    }

    public byte[] copy() {
        byte[] array = new byte[size];
        if (this.size > 0)
            System.arraycopy(this.buf, 0, array, 0, this.size);
        return array;
    }

    @Override
    public String toString() {
        return new String(buf, 0, size);
    }
}
