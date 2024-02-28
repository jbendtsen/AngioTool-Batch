package Utils;

import java.util.Iterator;
import java.lang.reflect.Array;

public class RefVector<T> implements Iterable<T> {
    public class Iter<T> implements Iterator<T> {
        int idx = 0;
        public boolean hasNext() {
            return idx < size;
        }
        public T next() {
            return (T)buf[idx++];
        }
    }

    public final Class<T> type;
    public T[] buf;
    public int size;

    public RefVector(Class<T> type, int initialCap) {
        this.type = type;
        this.buf = (T[])Array.newInstance(type, initialCap);
        this.size = 0;
    }
    public RefVector(Class<T> type) {
        this(type, 8);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iter<>();
    }

    public void resize(int newSize) {
        int oldCap = buf != null ? buf.length : 0;
        int newCap = Math.max(oldCap, 8);
        while (newSize > newCap)
            newCap = (int)((float)newCap * 1.7f) + 1;

        if (newCap > oldCap) {
            T[] newBuf = (T[])Array.newInstance(type, newCap);
            if (buf != null && oldCap > 0)
                System.arraycopy(buf, 0, newBuf, 0, oldCap);
            buf = newBuf;
        }

        size = newSize;
    }

    public void add(T v) {
        int pos = size;
        resize(pos + 1);
        buf[pos] = v;
    }

    public void clear() {
        size = 0;
    }

    public T getOr(int idx, T defaultValue) {
        return idx >= 0 && idx < size ? (T)buf[idx] : defaultValue;
    }

    public T popOr(T defaultValue) {
        return size > 0 ? (T)buf[--size] : defaultValue;
    }

    public T lastOr(T defaultValue) {
        return size > 0 ? (T)buf[size-1] : defaultValue;
    }

    public T[] copy() {
        T[] array = (T[])Array.newInstance(type, size);
        for (int i = 0; i < size; i++)
            array[i] = (T)this.buf[i];
        return array;
    }

    public String makeJoinedString(String delim) {
        ByteVectorOutputStream sb = new ByteVectorOutputStream();
        for (int i = 0; i < size; i++) {
            if (i > 0)
                sb.add(delim);
            sb.add(buf[i] != null ? buf[i].toString() : "null");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        ByteVectorOutputStream sb = new ByteVectorOutputStream();
        sb.add("RefVector<");
        sb.add(type.getSimpleName());
        sb.add(">: [");
        sb.add("" + size);
        sb.add("] {");

        for (int i = 0; i < size; i++) {
            sb.add(i == 0 ? " " : ", ");
            sb.add(buf[i] != null ? buf[i].toString() : "null");
        }

        sb.add(" }");
        return sb.toString();
    }
}
