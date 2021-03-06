/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cis.xts.aes;

/**
 *
 * @author prakash
 */
public class XTS {

    private static final int KEY_SIZE = 256;
    private static final int SPLIT_KEY_SIZE = 128;
    private static final int BYTE_SIZE = 8;
    private static final int BLOCK_SIZE = 128;
    private static final int ALPHA = 0x02;
    private static int[] key = new int[KEY_SIZE / BYTE_SIZE];
    private static int[] tweak = {0x01, 0x23, 0x45, 0x67, 0x89, 0xab, 0xcd, 0xef, 0xfe, 0xdc, 0xba, 0x98, 0x76, 0x54, 0x32, 0x10};
    private static int[] plaintext;
    private static int[] ciphertext;
    private static int[] alpha = new int[16];
    private static AES aes1;
    private static AES aes2;

    public XTS(int[] key) throws MyException {
        if (key.length != 32) {
            throw new MyException("Key must have exactly 256 bits.");
        }
        this.key = key;
        this.tweak = tweak;
        int[] key1 = new int[SPLIT_KEY_SIZE / BYTE_SIZE];
        System.arraycopy(key, 0, key1, 0, key1.length);
        int[] key2 = new int[SPLIT_KEY_SIZE / BYTE_SIZE];
        System.arraycopy(key, SPLIT_KEY_SIZE / BYTE_SIZE, key2, 0, key2.length);
        aes1 = new AES();
        aes1.setRoundKey(key1);
        aes2 = new AES();
        aes2.setRoundKey(key2);

        alpha[alpha.length - 1] = ALPHA;
    }

    // enkripsi plaintext keseluruhan
    public int[] encrypt(int[] plaintext) {

        ciphertext = new int[plaintext.length];

        int m = plaintext.length / (BLOCK_SIZE / BYTE_SIZE);

        //plaintext per block
        int[] plntxt = new int[16];
        int[] citext;

        //enkripsi blok yang utuh 128 bit
        for (int q = 0; q < m - 1; q++) {
            System.arraycopy(plaintext, q * 16, plntxt, 0, plntxt.length);
            citext = blockEncrypt(plntxt, q);
            System.arraycopy(citext, 0, ciphertext, q * 16, citext.length);
        }
        if (plaintext.length % (BLOCK_SIZE / BYTE_SIZE) == 0) {
            System.arraycopy(plaintext, (m - 1) * 16, plntxt, 0, plntxt.length);
            citext = blockEncrypt(plntxt, (m - 1));
            System.arraycopy(citext, 0, ciphertext, (m - 1) * 16, citext.length);

            // untuk kasus yang ngga utuh blok terakhirnya
        } else {

            int lastLength = plaintext.length - (m * 16); // panjang block terakhir (<128)
            int[] lastPlntxt = new int[16]; // block sisa

            System.arraycopy(plaintext, (m * 16), lastPlntxt, 0, lastLength);

            //enkripsi 2 blok terakhir, bila blok terakhir tidak utuh 128 bit
            System.arraycopy(plaintext, (m - 1) * 16, plntxt, 0, plntxt.length);
            int[] cece = blockEncrypt(plntxt, m - 1);
            System.arraycopy(cece, 0, ciphertext, m * 16, lastLength);

            System.arraycopy(cece, lastLength, lastPlntxt, lastLength, lastPlntxt.length - lastLength);
            citext = blockEncrypt(lastPlntxt, m);

            System.arraycopy(citext, 0, ciphertext, (m - 1) * 16, citext.length);
        }
        return ciphertext;
    }

    // dekripsi ciphertext keseluruhan
    public int[] decrypt(int[] ciphertext) {

        plaintext = new int[ciphertext.length];

        int m = ciphertext.length / (BLOCK_SIZE / BYTE_SIZE);

        //ciphertext per block
        int[] citext = new int[16];
        int[] plntxt;

        //dekripsi blok yang utuh 128 bit
        for (int q = 0; q < m - 1; q++) {
            System.arraycopy(ciphertext, q * 16, citext, 0, citext.length);
            plntxt = blockDecrypt(citext, q);
            System.arraycopy(plntxt, 0, plaintext, q * 16, plntxt.length);
        }
        if (ciphertext.length % (BLOCK_SIZE / BYTE_SIZE) == 0) {
            System.arraycopy(ciphertext, (m - 1) * 16, citext, 0, citext.length);
            plntxt = blockDecrypt(citext, (m - 1));
            System.arraycopy(plntxt, 0, plaintext, (m - 1) * 16, plntxt.length);

            // untuk kasus yang ngga utuh blok terakhirnya
        } else {
            int lastLength = ciphertext.length - (m * 16); // panjang block terakhir (<128)
            int[] lastCitext = new int[16];
            System.arraycopy(ciphertext, (m * 16), lastCitext, 0, lastLength);

            //enkripsi 2 blok terakhir, bila blok terakhir tidak utuh 128 bit
            System.arraycopy(ciphertext, (m - 1) * 16, citext, 0, citext.length);
            int[] pepe = blockDecrypt(citext, m);

            System.arraycopy(pepe, 0, plaintext, m * 16, lastLength);

            System.arraycopy(pepe, lastLength, lastCitext, lastLength, lastCitext.length - lastLength);
            plntxt = blockDecrypt(lastCitext, m - 1);

            System.arraycopy(plntxt, 0, plaintext, (m - 1) * 16, plntxt.length);
        }
        return plaintext;
    }

    // enkripsi per block
    public int[] blockEncrypt(int[] plaintext, int blockKe) {

        int[] hasilEncryptSatu = aes2.encrypt(tweak);

        int[] temp = alpha;
        for (int i = 0; i < blockKe - 1; i++) {
            temp = CipherUtils.multiplyGF2_128(temp, alpha);
        }

        int[] alphaPangkat = temp;
        int[] te = CipherUtils.multiplyGF2_128(hasilEncryptSatu, alphaPangkat);

        int[] pepe = new int[16];
        for (int i = 0; i < pepe.length; i++) {
            pepe[i] = plaintext[i] ^ te[i];
        }

        int[] cece = aes1.encrypt(pepe);

        int[] hasil;
        hasil = new int[16];
        for (int i = 0; i < hasil.length; i++) {
            hasil[i] = cece[i] ^ te[i];
        }

        return hasil;
    }

    // dekripsi per block
    public int[] blockDecrypt(int[] ciphertext, int blockKe) {

        int[] hasilEncryptSatu = aes2.encrypt(tweak);

        int[] temp = alpha;
        for (int i = 0; i < blockKe - 1; i++) {
            temp = CipherUtils.multiplyGF2_128(temp, alpha);
        }

        int[] alphaPangkat = temp;
        int[] te = CipherUtils.multiplyGF2_128(hasilEncryptSatu, alphaPangkat);

        int[] cece = new int[16];
        for (int i = 0; i < cece.length; i++) {
            cece[i] = ciphertext[i] ^ te[i];
        }

        int[] pepe = aes1.decrypt(cece);

        int[] hasil;
        hasil = new int[16];
        for (int i = 0; i < hasil.length; i++) {
            hasil[i] = pepe[i] ^ te[i];
        }

        return hasil;
    }
}
