package cis.xts.aes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class XTS {

    private static final int BYTE_SIZE = 8;
    private static final int BLOCK_SIZE = 128;
    private static final int ALPHA = 0x02;
    private static int[] plaintext;
    private static int[] ciphertext;
    private static int[] alpha = new int[16];
    private static String tweak = "0123456789abcdeffedcba9876543210";
    private static AES aes1;
    private static AES aes2;

    public XTS(File keyFile) throws MyException, FileNotFoundException, IOException {
        FileReader mykey = new FileReader(keyFile);
        BufferedReader in = new BufferedReader(mykey);
        String key = in.readLine();

        if (key.length() != 64) {
            throw new MyException("Key must have exactly 256 bits.");
        }

        String key1 = key.substring(0, 32);
        String key2 = key.substring(32);
        aes1 = new AES();
        aes2 = new AES();
        aes1.setRoundKey(Util.toInt(key1));
        aes2.setRoundKey(Util.toInt(key2));
        alpha[alpha.length - 1] = ALPHA;
    }

    public int[] encrypt(int[] plaintext) throws Exception {
        ciphertext = new int[plaintext.length];
        int m = plaintext.length / (BLOCK_SIZE / BYTE_SIZE);
        int[] plntxt = new int[16];
        int[] citext;

        for (int q = 0; q < m - 1; q++) {
            System.arraycopy(plaintext, q * 16, plntxt, 0, plntxt.length);
            citext = blockEnc(plntxt, q);
            System.arraycopy(citext, 0, ciphertext, q * 16, citext.length);
        }

        if (plaintext.length % (BLOCK_SIZE / BYTE_SIZE) == 0) {
            System.arraycopy(plaintext, (m - 1) * 16, plntxt, 0, plntxt.length);
            citext = blockEnc(plntxt, (m - 1));
            System.arraycopy(citext, 0, ciphertext, (m - 1) * 16, citext.length);
        } else {
            int lastLength = plaintext.length - (m * 16);
            int[] lastPlntxt = new int[16];
            System.arraycopy(plaintext, (m * 16), lastPlntxt, 0, lastLength);
            System.arraycopy(plaintext, (m - 1) * 16, plntxt, 0, plntxt.length);
            int[] CC = blockEnc(plntxt, m - 1);
            System.arraycopy(CC, 0, ciphertext, m * 16, lastLength);
            System.arraycopy(CC, lastLength, lastPlntxt, lastLength, lastPlntxt.length - lastLength);
            citext = blockEnc(lastPlntxt, m);
            System.arraycopy(citext, 0, ciphertext, (m - 1) * 16, citext.length);
        }

        return ciphertext;
    }

    public int[] decrypt(int[] ciphertext) throws Exception {
        plaintext = new int[ciphertext.length];
        int m = ciphertext.length / (BLOCK_SIZE / BYTE_SIZE);
        int[] citext = new int[16];
        int[] plntxt;

        for (int q = 0; q < m - 1; q++) {
            System.arraycopy(ciphertext, q * 16, citext, 0, citext.length);
            plntxt = blockDec(citext, q);
            System.arraycopy(plntxt, 0, plaintext, q * 16, plntxt.length);
        }

        if (ciphertext.length % (BLOCK_SIZE / BYTE_SIZE) == 0) {
            System.arraycopy(ciphertext, (m - 1) * 16, citext, 0, citext.length);
            plntxt = blockDec(citext, (m - 1));
            System.arraycopy(plntxt, 0, plaintext, (m - 1) * 16, plntxt.length);
        } else {
            int lastLength = ciphertext.length - (m * 16);
            int[] lastCitext = new int[16];
            System.arraycopy(ciphertext, (m * 16), lastCitext, 0, lastLength);
            System.arraycopy(ciphertext, (m - 1) * 16, citext, 0, citext.length);
            int[] PP = blockDec(citext, m);
            System.arraycopy(PP, 0, plaintext, m * 16, lastLength);
            System.arraycopy(PP, lastLength, lastCitext, lastLength, lastCitext.length - lastLength);
            plntxt = blockDec(lastCitext, m - 1);
            System.arraycopy(plntxt, 0, plaintext, (m - 1) * 16, plntxt.length);
        }

        return plaintext;
    }

    public int[] blockEnc(int[] plaintext, int blockNum) throws Exception {
        int[] resultBefore = aes2.encrypt(Util.toInt(tweak));
        int[] temp = alpha;

        for (int i = 0; i < blockNum - 1; i++) {
            temp = Util.multiplyGF2_128(temp, alpha);
        }

        int[] alphaPow = temp;
        int[] T = Util.multiplyGF2_128(resultBefore, alphaPow);
        int[] PP = new int[16];

        for (int i = 0; i < PP.length; i++) {
            PP[i] = plaintext[i] ^ T[i];
        }

        int[] CC = aes1.encrypt(PP);
        int[] result = new int[16];

        for (int i = 0; i < result.length; i++) {
            result[i] = CC[i] ^ T[i];
        }

        return result;
    }

    public int[] blockDec(int[] ciphertext, int blockNum) throws Exception {
        int[] resultBefore = aes2.encrypt(Util.toInt(tweak));
        int[] temp = alpha;

        for (int i = 0; i < blockNum - 1; i++) {
            temp = Util.multiplyGF2_128(temp, alpha);
        }

        int[] alphaPow = temp;
        int[] T = Util.multiplyGF2_128(resultBefore, alphaPow);
        int[] CC = new int[16];

        for (int i = 0; i < CC.length; i++) {
            CC[i] = ciphertext[i] ^ T[i];
        }

        int[] PP = aes1.decrypt(CC);
        int[] result = new int[16];

        for (int i = 0; i < result.length; i++) {
            result[i] = PP[i] ^ T[i];
        }

        return result;
    }
}
