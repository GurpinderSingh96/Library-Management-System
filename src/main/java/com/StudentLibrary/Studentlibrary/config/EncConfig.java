package com.StudentLibrary.Studentlibrary.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncConfig {
    @Bean
    public Dotenv dotenv() {
        return Dotenv.configure().load();
    }
}
