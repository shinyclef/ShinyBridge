package com.hotmail.shinyclef.shinybridge;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

/**
 * User: Shinyclef
 * Date: 10/08/13
 * Time: 11:13 PM
 */

public class AccountPassword
{
    private static final int PASSWORD_MIN = 5;
    private static final int PASSWORD_MAX = 25;
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int ITERATIONS = 1;
    private static final int SALT_BYTE_SIZE = 8;
    private static final int PW_HASH_BYTE_SIZE = 16;

    /* Checks if a password is of correct length. returns 0 if it is, or the difference if not. */
    public static int isCorrectLength(String password)
    {
        int length = password.length();

        if (length < PASSWORD_MIN || length > PASSWORD_MAX)
        {
            return length - PASSWORD_MIN;
        }
        else
        {
            return 0;
        }
    }

    /* Validates a password using a hash.
    * @param   password        the password to check
    * @param   correctHash     the hash of the valid password
    * @return                  true if the password is correct, false if not */
    public static boolean validatePassword(char[] password, String correctHash)
    {
        //decode the hash into its parameters
        String[] params = correctHash.split(":");
        int iterations = Integer.parseInt(params[0]);
        byte[] salt = fromHex(params[1]);
        byte[] hash = fromHex(params[2]);
        byte[] testHash = null;

        try
        {
            //compute the hash of the provided password, using the same salt, iteration count, and hash length
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, hash.length * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            testHash = skf.generateSecret(spec).getEncoded();
        }
        catch (NoSuchAlgorithmException e){}
        catch (InvalidKeySpecException e){}

        //compare the hashes and return. true = match
        return Arrays.equals(hash, testHash);
    }

    /* String override of method above. */
    public static boolean validatePassword(String password, String correctHash)
    {
        char[] charPassword = password.toCharArray();
        return validatePassword(charPassword, correctHash);
    }


    /* Creates a hash string used which contains ITERATIONS, salt and hash for storage in database. */
    public static String generateHash(String password)
    {
        String encodedString = "";

        try
        {
            //generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_BYTE_SIZE];
            random.nextBytes(salt);

            //hash the password
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, PW_HASH_BYTE_SIZE * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] pwHash = skf.generateSecret(spec).getEncoded();

            // format ITERATIONS:salt:hash, then return
            encodedString = ITERATIONS + ":" + toHex(salt) + ":" +  toHex(pwHash);
        }
        catch (NoSuchAlgorithmException e){}
        catch (InvalidKeySpecException e){}

        return encodedString;
    }

     /* Converts a byte array into a hexadecimal string.
     * returns a length*2 character string encoding the byte array */
    private static String toHex(byte[] array)
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        }
        else
        {
            return hex;
        }
    }

     /* Converts a string of hexadecimal characters into a byte array.
     * the hex string decoded into a byte array */
    private static byte[] fromHex(String hex)
    {
        byte[] binary = new byte[hex.length() / 2];
        for(int i = 0; i < binary.length; i++)
        {
            binary[i] = (byte)Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
        }
        return binary;
    }
}
