package Pixels;

import java.awt.Color;

public class Rgb {
    public int value;

    public Rgb(String str) {
        value = 0;
        int len = str.length();
        int n = 0;
        for (int i = 0; i < len && n < 6; i++) {
            char c = str.charAt(i);
            if (c >= '0' && c <= '9')
                value = (value << 4) | (c - '0');
            else if (c >= 'A' && c <= 'F')
                value = (value << 4) | (c - 0x37);
            else if (c >= 'a' && c <= 'f')
                value = (value << 4) | (c - 0x57);
            else
                n--;
            n++;
        }
    }

    public Rgb(Color c) {
        value = (c.getRed() & 0xff) << 16 | (c.getGreen() & 0xff) << 8 | (c.getBlue() & 0xff);
    }

    public int getARGB() {
        return 0xff000000 | value;
    }

    public String toString() {
        byte[] buf = new byte[7];
        buf[0] = '#';
        for (int i = 0; i < 6; i++) {
            int d = ((value >> (5-i)*4) & 0xf);
            buf[i+1] = (byte)(d + (d < 10 ? '0' : 0x57));
        }
        return new String(buf, 0, buf.length);
    }

    public Color toColor() {
        return new Color((value >> 16) & 0xff, (value >> 8) & 0xff, value & 0xff);
    }
}
