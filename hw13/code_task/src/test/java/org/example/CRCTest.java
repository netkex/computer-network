package org.example;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CRCTest {
    private final Random random = new Random(128);
    private final Path documentPath = Paths.get("src/test/resources/holmes.txt");

    @Test
    void testCorrectShort() {
        final var crc = new CRC(3);

        final var data = "Very simple string".getBytes();
        final var crcCheckValue = crc.calculateCheckValue(data);
        assertTrue(crc.check(data, crcCheckValue));
    }

    @Test
    void testCorrectLong() {
        final var crc = new CRC(128);

        final var data = new byte[1000];
        random.nextBytes(data);
        final var crcCheckValue = crc.calculateCheckValue(data);

        assertTrue(crc.check(data, crcCheckValue));
    }

    @Test
    void testOneBitShort() {
        final var crc = new CRC(3);

        final var data = "Very simple string".getBytes();
        final var crcCheckValue = crc.calculateCheckValue(data);

        data[3] = (byte) (data[3] ^ (1 << 3));
        assertFalse(crc.check(data, crcCheckValue));
    }

    @Test
    void testOneBitLong() {
        final var crc = new CRC(128);

        final var data = new byte[1000];
        random.nextBytes(data);
        final var crcCheckValue = crc.calculateCheckValue(data);

        data[534] = (byte) (data[534] ^ (1 << 5));
        assertFalse(crc.check(data, crcCheckValue));
    }

    @Test
    void testOneBitCrc() {
        final var crc = new CRC(128);

        final var data = new byte[1000];
        random.nextBytes(data);
        final var crcCheckValue = crc.calculateCheckValue(data);

        crcCheckValue[10] = (byte) (crcCheckValue[10] ^ (1 << 5));
        assertFalse(crc.check(data, crcCheckValue));
    }

    @Test
    void testRBitsShort() {
        final var crc = new CRC(3);

        final var data = "Very simple string".getBytes();
        final var crcCheckValue = crc.calculateCheckValue(data);

        data[1] = (byte) (data[3] ^ (1 << 3) ^ (1 << 5));
        data[6] = (byte) (data[6] ^ (1 << 2));
        assertFalse(crc.check(data, crcCheckValue));
    }

    @Test
    void testTextWithRandomFailures() throws IOException {
        final var blockSize = 2048;
        final var r = 256;
        final var crc = new CRC(r);
        final var p = 0.7;

        final var document = readDocument();
        for (int i = 0; i < document.length; i += blockSize) {
            while (true) {
                final var documentBlock = Arrays.copyOfRange(document, i, min(i + blockSize, document.length));
                final var crcCheckValue = crc.calculateCheckValue(documentBlock);

                final var damaged = damage(documentBlock, crcCheckValue, r, p);
                if (!damaged) {
                    assertTrue(crc.check(documentBlock, crcCheckValue));
                    System.out.printf("Not damaged at %d: OK\n", i);
                    break;
                } else {
                    assertFalse(crc.check(documentBlock, crcCheckValue));
                    System.out.printf("Damaged at %d: OK\n", i);
                }
            }
        }
    }

    private boolean damage(byte[] data, byte[] checkValue, int r, double p) {
        if (random.nextDouble() > p)
            return false;

        while (r-- > 0) {
            final var rel = data.length * 1. / (data.length + checkValue.length);
            if (random.nextDouble() < rel) {
                final var damagedByte = random.nextInt(0, data.length);
                final var damagedBit = random.nextInt(0, 8);
                data[damagedByte] = (byte) (data[damagedByte] ^ (1 << damagedBit));
            } else {
                final var damagedByte = random.nextInt(0, checkValue.length);
                final var damagedBit = random.nextInt(0, 8);
                checkValue[damagedByte] = (byte) (checkValue[damagedByte] ^ (1 << damagedBit));
            }
        }

        return true;
    }

    private byte[] readDocument() throws IOException {
        return Files.readAllBytes(documentPath);
    }
}