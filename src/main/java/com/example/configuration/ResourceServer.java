package com.example.configuration;

import org.springframework.http.HttpMethod;


//@EnableWebSecurity
public class ResourceServer { //extends WebSecurityConfigurerAdapter {

    // URLs qui peuvent être accédés par les utilisateurs anonymes
    private static final String[] ANONYME_API_URLS = {
            "/v2/api-docs/**",
            "/stats/**"
    };

}