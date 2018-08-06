package io.github.oxmose.passlock.tools;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AESEncrypt {

    private static final int AES_KEY_LENGTH = 128;
    private static String AES_INSTANCE_TYPE = "AES/GCM/NoPadding";

    private static final int ITER_COUNT_MIN = 5000;
    private static final int ITER_COUNT_DERIVATION = 5000;

    private static final char stringDelimiter = ']';

    public static String encryptString(String stringToEncrypt, String password)
            throws NoSuchPaddingException,
                   NoSuchAlgorithmException,
                   InvalidAlgorithmParameterException,
                   InvalidKeyException,
                   InvalidKeySpecException,
                   UnsupportedEncodingException,
                   BadPaddingException,
                   IllegalBlockSizeException {
        
        /* Init secure random */
        SecureRandom secureRandom = new SecureRandom();

        /* Set parameters for the encryption */
        int iterCount = secureRandom.nextInt(ITER_COUNT_DERIVATION) + ITER_COUNT_MIN;
        int saltLength = AES_KEY_LENGTH / 8;

        /* Create the salt */
        byte[] salt = new byte[saltLength];
        secureRandom.nextBytes(salt);

        /* Create the key */
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt,
                                         iterCount, AES_KEY_LENGTH);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
        SecretKey key = new SecretKeySpec(keyBytes, "AES");

        /* Init cypher */
        Cipher cipher = Cipher.getInstance(AES_INSTANCE_TYPE);

        /* Init IV */
        byte[] iv = new byte[cipher.getBlockSize()];
        secureRandom.nextBytes(iv);

        /* Init GCM parameters */
        GCMParameterSpec parameterSpec = new GCMParameterSpec(AES_KEY_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        /* Cipher */
        byte[] cipherText = cipher.doFinal(stringToEncrypt.getBytes( "UTF-8"));

        /* Save to final string */
        return Integer.toString(iterCount) + stringDelimiter + /* Iter count */
                             Base64.encodeToString(iv, Base64.NO_WRAP) + stringDelimiter + /* IV */
                             Base64.encodeToString(salt, Base64.NO_WRAP) + stringDelimiter + /* Salt */
                             Base64.encodeToString(cipherText, Base64.NO_WRAP);
    }

    public static String decryptString(String stringToDecrypt, String password)
            throws InvalidAlgorithmParameterException,
            InvalidKeyException,
            NoSuchPaddingException,
            NoSuchAlgorithmException,
            BadPaddingException,
            IllegalBlockSizeException,
            UnsupportedEncodingException, InvalidKeySpecException {
        /* Split the string */
        String[] fields = stringToDecrypt.split("]");

        /* Get string components */
        int iterCount =  Integer.parseInt(fields[0]);
        byte[] iv = Base64.decode(fields[1], Base64.NO_WRAP);
        byte[] salt = Base64.decode(fields[2], Base64.NO_WRAP);
        byte[] cipherBytes = Base64.decode(fields[3], Base64.NO_WRAP);

        /* Get the key */
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt,
                iterCount, AES_KEY_LENGTH);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
        SecretKey key = new SecretKeySpec(keyBytes, "AES");

        /* Init cipher */
        Cipher cipher = Cipher.getInstance(AES_INSTANCE_TYPE);

        /* Init GCM parameters */
        GCMParameterSpec parameterSpec = new GCMParameterSpec(AES_KEY_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        byte[] plaintext = cipher.doFinal(cipherBytes);

        return new String(plaintext , "UTF-8");
    }
}
