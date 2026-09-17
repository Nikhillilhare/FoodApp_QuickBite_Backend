package com.aurainfo.foodapp;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class GeneratePasswordHashTest {

    @Test
    void generateHash() {

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        String hash =
                encoder.encode("Admin@12345");

        System.out.println("BCrypt Hash:");
        System.out.println(hash);
    }
}