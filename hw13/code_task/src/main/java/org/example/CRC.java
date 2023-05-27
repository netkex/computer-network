package org.example;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Random;

public class CRC {
    private final int r;
    private final BigInteger G;
    private final static Random random = new Random(239);

    public CRC(int r) {
        this.r = r;
        this.G = generateBase(r);
    }

    public byte[] calculateCheckValue(byte[] data) {
        var dataInt = bytesToBigInteger(data);
        return bigIntegerToBytes(divideByG(dataInt));
    }

    public boolean check(byte[] data, byte[] crcCheckValue) {
        final var dataInt = bytesToBigInteger(data);
        final var crcInt = bytesToBigInteger(crcCheckValue);
        return crcInt.compareTo(divideByG(dataInt)) == 0;
    }

    public BigInteger divideByG(@NotNull BigInteger value) {
        while (value.bitLength() > r) {
            value = value.xor(G.shiftLeft(value.bitLength() - (r + 1)));
        }
        return value;
    }

    private static BigInteger generateBase(int r) {
        var G = BigInteger.valueOf(1);
        for (int i = 0; i < r; i++) {
            G = G.multiply(BigInteger.valueOf(2)).add(BigInteger.valueOf(random.nextInt(0, 2)));
        }
        return G;
    }

    private static byte[] bigIntegerToBytes(@NotNull BigInteger value) {
        return value.toByteArray();
    }

    @NotNull
    private static BigInteger bytesToBigInteger(byte[] value) {
        var res = new BigInteger(value);
        if (res.compareTo(BigInteger.valueOf(0)) < 0)
            res = res.multiply(BigInteger.valueOf(-1));
        return res;
    }
}
