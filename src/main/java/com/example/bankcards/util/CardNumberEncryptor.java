package com.example.bankcards.util;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class CardNumberEncryptor {

    @Value("${encryption.secret}")
    private String secret;

    @Bean(name = "cardEncryptor")
    public StringEncryptor stringEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(secret);
        encryptor.setIvGenerator(new RandomIvGenerator());
        encryptor.setAlgorithm("PBEWithHMACSHA512AndAES_256");
        return encryptor;
    }
}