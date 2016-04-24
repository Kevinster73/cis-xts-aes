/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cis.xts.aes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author prakash
 */
public class Util {

    private static final int GF_SIZE = 128;
    private static final int BLOCK_SIZE = 128;
    private static final int BYTE_SIZE = 8;
    private static final int POLY_128 = 0x87;
    public static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static int[] toIntArray(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int numData;
        byte[] data = new byte[16384];

        while ((numData = stream.read(data)) != -1) {
            buffer.write(data, 0, numData);
        }
        buffer.flush();

        byte[] byteResult = buffer.toByteArray();
        int[] intResult = new int[byteResult.length];
        for (int i = 0; i < byteResult.length; i++) {
            intResult[i] = ((int) byteResult[i]) + 128;
        }

        return intResult;
    }

    public static void writeToFile(int[] arr, File file) throws IOException {
        FileOutputStream stream = new FileOutputStream(file);

        byte[] data = new byte[arr.length];
        for (int i = 0; i < arr.length; i++) {
            data[i] = (byte) (arr[i] - 128);
        }

        stream.write(data);
        stream.close();
    }

    public static int[] multiplyGF2_128(int[] x, int[] y) {
        if (x.length != BLOCK_SIZE / BYTE_SIZE || y.length != BLOCK_SIZE / BYTE_SIZE) {
            return null;
        }

        int[][] arm = new int[GF_SIZE][y.length];
        System.arraycopy(y, 0, arm[0], 0, y.length);

        int[] temp = new int[y.length];
        System.arraycopy(y, 0, temp, 0, y.length);

        int n = 8;

        for (int i = 1; i < GF_SIZE; i++) {
            int m = (temp[0] & (1 << (n - 1))) >> (n - 1);
            temp = shiftLeft(temp);
            if (m == 1) {
                temp[temp.length - 1] ^= POLY_128;
            }
            arm[i] = temp;
        }

        int[] res = new int[y.length];
        for (int i = 0; i < GF_SIZE / BYTE_SIZE; i++) {
            for (int j = 0; j < BYTE_SIZE; j++) {
                if (((x[i] & (1 << j)) >> (j)) == 1) {
                    for (int k = 0; k < res.length; k++) {
                        res[k] = res[k] ^ arm[(GF_SIZE - (i + 1) * BYTE_SIZE) + j][k];
                    }
                }
            }
        }

        return res;
    }

    private static int[] shiftLeft(int[] x) {
        int msb = 0;
        int n = 8;
        int[] res = new int[x.length];

        for (int i = x.length - 1; i >= 0; i--) {
            int temp = (x[i] & (1 << (n - 1))) >> (n - 1);
            res[i] = x[i] << 1;
            res[i] &= ((1 << n) - 1);
            if (msb == 1) {
                res[i] |= 1;
            }
            msb = temp;
        }

        return res;
    }

    public static byte[] hex2byte(String string) {
        int n = string.length();
        byte[] arrby = new byte[(n + 1) / 2];
        int n2 = 0;
        int n3 = 0;
        if (n % 2 == 1) {
            arrby[n3++] = (byte) Util.hexDigit(string.charAt(n2++));
        }
        while (n2 < n) {
            arrby[n3++] = (byte) (Util.hexDigit(string.charAt(n2++)) << 4 | Util.hexDigit(string.charAt(n2++)));
        }
        return arrby;
    }

    public static int hexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 65 + 10;
        }
        if (c >= 'a' && c <= 'f') {
            return c - 97 + 10;
        }
        return 0;
    }

    public static int[] byte2int(byte[] arrby) {
        int n = arrby.length;
        int[] arrn = new int[n / 4];
        int n2 = 0;
        int n3 = 0;
        while (n3 < n / 4) {
            arrn[n3++] = (arrby[n2++] & 255) << 24 | (arrby[n2++] & 255) << 16 | (arrby[n2++] & 255) << 8 | arrby[n2++] & 255;
        }
        return arrn;
    }

    public static String toHEX(int[] ia) {
        int length = ia.length;
        char[] buf = new char[length * 10];
        for (int i = 0, j = 0, k; i < length;) {
            k = ia[i++];
            buf[j++] = HEX_DIGITS[(k >>> 28) & 0x0F];
            buf[j++] = HEX_DIGITS[(k >>> 24) & 0x0F];
            buf[j++] = HEX_DIGITS[(k >>> 20) & 0x0F];
            buf[j++] = HEX_DIGITS[(k >>> 16) & 0x0F];
            buf[j++] = HEX_DIGITS[(k >>> 12) & 0x0F];
            buf[j++] = HEX_DIGITS[(k >>> 8) & 0x0F];
            buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
            buf[j++] = HEX_DIGITS[k & 0x0F];
        }
        return new String(buf);
    }
}
