/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cis.xts.aes;

import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Kevin
 */
public class AES {

    final String key;

    public AES(String key) {
        this.key = key;
    }

    public int[] AESEncrypt(String plaintext) throws Exception {
        SecretKey AESkey = new SecretKeySpec(DatatypeConverter.parseHexBinary(key), "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, AESkey);

        byte[] result = cipher.doFinal(DatatypeConverter.parseHexBinary(plaintext));
        String ciphertext = (DatatypeConverter.printHexBinary(result));
        return Util.convertByteToIntArray(ciphertext);

    }
    
    
    
    
}
