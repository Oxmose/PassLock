package io.github.oxmose.passlock;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Tools {

    public static String hashPassword(String passwordText) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
            byte[] digest = md.digest(passwordText.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digest.length; i++) {
                sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
