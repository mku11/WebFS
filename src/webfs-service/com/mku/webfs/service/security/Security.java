package com.mku.webfs.service.security;
/*
MIT License

Copyright (c) 2021 Max Kas

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import com.mku.webfs.service.controller.FileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@EnableWebSecurity
public class Security {
    @Value("${app.username}")
    private String username;
    @Value("${app.password}")
    private String password;

    @Value("${app.role}")
    private String role;

    @Value("${app.path}")
    private String path;

    private AuthEntryPoint authenticationEntryPoint;

    public static void checkRead(HttpServletRequest request) {
        if(!request.isUserInRole("READ") && !request.isUserInRole("READ_WRITE"))
            throw new SecurityException("Read permission not allowed");
    }

    public static void checkWrite(HttpServletRequest request) {
        if(!request.isUserInRole("WRITE") && !request.isUserInRole("READ_WRITE"))
            throw new SecurityException("Write permission not allowed");
    }

    @Autowired
    private void BasicAuthSecurity(AuthEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();

        http.authorizeRequests()
                .antMatchers("/**")
                .authenticated()
                .and()
                .httpBasic()
                .authenticationEntryPoint(authenticationEntryPoint);
        return http.build();
    }

    @Autowired
    private void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        FileSystem.getInstance().setPath(path);

        // add the primary user if it hasn't been already supplied
        HashMap<String, AuthUsers.User> users = AuthUsers.getUsers();
        if(!users.containsKey(username)) {
            AuthUsers.addUser(username, password, role);
            users = AuthUsers.getUsers();
        }

        for(String name : users.keySet()) {
            AuthUsers.User user = users.get(name);
            auth.inMemoryAuthentication()
                    .passwordEncoder(new BCryptPasswordEncoder())
                    .withUser(user.getName())
                    .password(new BCryptPasswordEncoder().encode(user.getPassword()))
                    .roles(user.getRole());
        }
    }

    @EnableWebSecurity
    public class WebSecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.cors(Customizer.withDefaults()); // allow OPTIONS method for browser preflight
            return http.build();
        }
    }
}
