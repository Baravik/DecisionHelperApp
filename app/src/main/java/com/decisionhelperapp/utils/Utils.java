package com.decisionhelperapp.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.util.Log;

public class Utils {
    private static final String TAG = "Utils";
    
    /**
     * Hashes a password using SHA-256 algorithm.
     * In a production app, you would want to use a more secure method with salt.
     *
     * @param password The plain text password to hash
     * @return The hashed password or null if hashing fails
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error hashing password", e);
            return null;
        }
    }
    
    /**
     * Checks if a string is null or empty
     *
     * @param str The string to check
     * @return True if the string is null or empty, false otherwise
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}