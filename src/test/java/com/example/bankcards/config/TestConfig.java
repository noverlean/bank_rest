package com.example.bankcards.config;

import com.example.bankcards.util.CardNumberMasker;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class TestConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public StringEncryptor stringEncryptor() {
        return new StringEncryptor() {
            @Override
            public String encrypt(String input) {
                return "encrypted_" + input;
            }

            @Override
            public String decrypt(String encryptedInput) {
                return encryptedInput.replace("encrypted_", "");
            }
        };
    }

    @Bean
    public CardNumberMasker cardNumberMasker() {
        return new CardNumberMasker();
    }
}