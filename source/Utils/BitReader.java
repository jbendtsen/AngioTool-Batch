package Utils;

public class BitReader
{
    public byte[] data;
    public int bitPos;

    public BitReader(byte[] data) {
        this.data = data;
    }

    public int getBits(int nBits) {
        if (nBits <= 0 || nBits > 32)
            throw new IllegalArgumentException();
        if (bitPos + nBits > data.length * 8)
            return -1;

        int bits = 0;
        while (nBits > 0) {
            int bytePos = (bitPos & 7);
            if (bytePos != 0) {
                int leftover = 8 - bytePos;
                int n = this.data[bitPos >>> 3] & ((1 << leftover) - 1);
                if (nBits < leftover) {
                    bits <<= nBits;
                    bits |= n >>> (leftover - nBits);
                    this.bitPos += nBits;
                    nBits = 0;
                }
                else {
                    bits <<= leftover;
                    bits |= n;
                    this.bitPos += leftover;
                    nBits -= leftover;
                }
            }
            else {
                if (nBits < 8) {
                    bits <<= nBits;
                    bits |= (this.data[bitPos >>> 3] & 0xff) >>> (8 - nBits);
                    this.bitPos += nBits;
                    nBits = 0;
                }
                else {
                    bits <<= 8;
                    bits |= this.data[bitPos >>> 3] & 0xff;
                    this.bitPos += 8;
                    nBits -= 8;
                }
            }
        }

        return bits;
    }
}
