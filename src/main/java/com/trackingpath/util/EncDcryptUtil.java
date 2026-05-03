package com.trackingpath.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class EncDcryptUtil {
	private static String KEY_SALT="Tpxvtqertshysgoe";	
	private Cipher CRYPTO_CIPHER;
	

	public EncDcryptUtil() {
		try {
			CRYPTO_CIPHER = Cipher.getInstance("AES");
			CRYPTO_CIPHER.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY_SALT.getBytes(), "AES"));	
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}
	
	
	public String encrypt(String strClearText) {
		String strData="";
		
		try {
			SecretKeySpec skeyspec=new SecretKeySpec(KEY_SALT.getBytes(),"Blowfish");
			Cipher cipher=Cipher.getInstance("Blowfish");
			cipher.init(Cipher.ENCRYPT_MODE, skeyspec);
			byte[] encrypted=cipher.doFinal(strClearText.getBytes());
			strData=new String(encrypted);
			
		} catch (Exception e) {
			e.printStackTrace();		
		}
		return strData;

	}

	public String decrypt(String str) {
		String strData = "";
		try {
			SecretKeySpec skeyspec = new SecretKeySpec(KEY_SALT.getBytes(), "Blowfish");
			Cipher cipher = Cipher.getInstance("Blowfish");
			cipher.init(Cipher.DECRYPT_MODE, skeyspec);
			byte[] decrypted = cipher.doFinal(str.getBytes());
			strData = new String(decrypted);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return strData;
	}
	
	
	public static String generateHash(String password){
	     BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	     String hashedPassword = passwordEncoder.encode(password);
	     return hashedPassword;
	}
	public static boolean matchHash(String rawPassword,String encodedPassword){
	     BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	     return passwordEncoder.matches(rawPassword, encodedPassword);
	   
	}
	public static void main(String[] args) throws Exception {

		
		String data = "DYD,000000#";
        

        //SecretKey key = KeyGenerator.getInstance("AES").generateKey();
        SecretKey key = new SecretKeySpec(KEY_SALT.getBytes(), "AES");
        EncDcryptUtil encrypter = new EncDcryptUtil();

        String encrypted = encrypter.encrypt(data);

        String decrypted = encrypter.decrypt(encrypted);
		
	}
}
