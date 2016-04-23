/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cis.xts.aes;

import java.util.Scanner;

/**
 *
 * @author Kevin
 */
public class CisXtsAes {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        Scanner in = new Scanner(System.in);
        String plaintext = in.nextLine();
        String key = in.nextLine();
        AES aes = new AES(plaintext, key);
        System.out.println(aes.AESEncrypt());
    }
    
}
