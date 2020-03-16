package gr.dcu.utils;

import org.apache.commons.codec.digest.DigestUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Vangelis Nomikos
 */
public class MySQLUtils {
    
    public static String toMySQLPassword(String plainText) throws UnsupportedEncodingException {
        byte[] utf8 = plainText.getBytes("UTF-8");
        
        return "*" + DigestUtils.shaHex(DigestUtils.sha(utf8)).toUpperCase();
    }
    
    /**
     * Create MD5 hash like MySQL MD5 function
     * @param plainText
     * @return
     * @throws NoSuchAlgorithmException 
     */
    public static String toMd5(String plainText) throws NoSuchAlgorithmException {
        
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(plainText.getBytes());
        byte[] digest = md.digest();
        String myHash = DatatypeConverter
          .printHexBinary(digest).toLowerCase();
          // .toUpperCase();
    
        return myHash;
    }
    
    /**
     * Create MD5 hash like MySQL MD5 function
     * @param input
     * @return
     * @throws NoSuchAlgorithmException 
     */
    public static String md5(String input) throws NoSuchAlgorithmException {
        String result = input;
        if(input != null) {
            MessageDigest md = MessageDigest.getInstance("MD5"); //or "SHA-1"
            md.update(input.getBytes());
            BigInteger hash = new BigInteger(1, md.digest());
            result = hash.toString(16);
            while(result.length() < 32) { //40 for SHA-1
                result = "0" + result;
            }
        }
        return result;
    }
}
