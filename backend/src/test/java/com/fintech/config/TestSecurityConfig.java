package com.fintech.config;

import com.fintech.security.JwtService;
import com.fintech.client.AlphaVantageClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.mockito.Mockito;

@TestConfiguration
public class TestSecurityConfig {
    
    @Bean
    public JwtService jwtService() {
        return new JwtService();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests()
            .anyRequest().authenticated();
        return http.build();
    }

    @Bean
    public AlphaVantageClient alphaVantageClient() {
        return Mockito.mock(AlphaVantageClient.class);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 