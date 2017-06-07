package com.dchealth.security;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.crypto.hash.SimpleHash;

/**
 * Created by heren on 2016/10/24.
 */
public class SystemPasswordService {

    private static String ALGORITHMNAME= Md5Hash.ALGORITHM_NAME;
    private static int HASHITERATIONS=1024 ;

    public static PasswordAndSalt enscriptPassword(String userName,String password ){
        String salt = new SecureRandomNumberGenerator().nextBytes().toHex();
        SimpleHash simpleHash = new SimpleHash(ALGORITHMNAME,password,userName+salt,HASHITERATIONS) ;
        PasswordAndSalt passwordAndSalt = new PasswordAndSalt(simpleHash.toHex(),salt) ;
        return passwordAndSalt ;
    }

    public static String enscriptPasswordWithSalt(String salt,String username,String password){
        SimpleHash simpleHash = new SimpleHash(ALGORITHMNAME,password,username+salt,HASHITERATIONS) ;
        return simpleHash.toHex();
    }


    public static void main(String[] args) {
        //cb869ff7955ab46704132e47a414bca9
        String passwordAndSalt = enscriptPasswordWithSalt("d0f483017280403ce294950b9bf75ea4","mt1","123");
        System.out.println(passwordAndSalt);
    }
}
