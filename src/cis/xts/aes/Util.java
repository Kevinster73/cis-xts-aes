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
    private static final int GF_SIZE = 128,
                             BLOCK_SIZE = 128,
                             BYTE_SIZE = 8,
                             POLY_128 = 0x87;
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
    public static int[] convertByteToIntArray (String ciphertext){
        int[] result = new int[32];
        for (int i = 0; i < 32; i++){
            result[i] = Util.hexToInt(ciphertext.charAt(i));
        }
        return result;
    }
    public static int hexToInt (char hex){
        int temp = 0;
        switch (hex){
            case '0' : 
                temp = 0x00;
                break;
            case '1' : 
                temp = 0x01;
                break;
            case '2' : 
                temp = 0x02;
                break;
            case '3' : 
                temp = 0x03;
                break;
            case '4' : 
                temp = 0x04;
                break;
            case '5' : 
                temp = 0x05;
                break;
            case '6' : 
                temp = 0x06;
                break;
            case '7' : 
                temp = 0x07;
                break;
            case '8' : 
                temp = 0x08;
                break;
            case '9' : 
                temp = 0x09;
                break;
            case 'A' : 
                temp = 0x0A;
                break;
            case 'B' : 
                temp = 0x0B;
                break;
            case 'C' : 
                temp = 0x0C;
                break;
            case 'D' : 
                temp = 0x0D;
                break;
            case 'E' : 
                temp = 0x0E;
                break;
            case 'F' : 
                temp = 0x0F;
                break;
        }
        return temp;
    }
    public static char intToHex (int input){
        char temp = '0';
        switch (input){
            case 0 : 
                temp = '0';
                break;
            case 1 : 
                temp = '1';
                break;
            case 2 : 
                temp = '2';
                break;
            case 3 : 
                temp = '3';
                break;
            case 4 : 
                temp = '4';
                break;
            case 5 : 
                temp = '5';
                break;
            case 6 : 
                temp = '6';
                break;
            case 7 : 
                temp = '7';
                break;
            case 8 : 
                temp = '8';
                break;
            case 9 : 
                temp = '9';
                break;
            case 10 : 
                temp = 'A';
                break;
            case 11 : 
                temp = 'B';
                break;
            case 12 : 
                temp = 'C';
                break;
            case 13 : 
                temp = 'D';
                break;
            case 14 : 
                temp = 'E';
                break;
            case 15 : 
                temp = 'F';
                break;
        }
        return temp;
    }
                
    public static String convertToString(int[] data){
        String temp = "";
        for (int i = 0 ; i < data.length ; i++){
            temp += intToHex(data[i]);
        }
        return temp;
    }
    
    //multiplication in gf(2^128) with irreducible polynomial x^128 + x^7 + x^2 + x + 1
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
}
