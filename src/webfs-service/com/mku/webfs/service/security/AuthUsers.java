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

import java.util.HashMap;

public class AuthUsers {
    private static HashMap<String,User> users = new HashMap<>();

    /**
     * User credentials and roles
     */
    public static class User {
        private String name;
        private String password;
        private String role;
        public String getName() {
            return name;
        }

        public String getPassword() {
            return password;
        }

        public String getRole() {
            return role;
        }

        public User(String name, String password, String role) {
            this.name = name;
            this.password = password;
            this.role = role;
        }
    }

    /**
     * Add user password and role
     * @param user The user name
     * @param password The password
     * @param role Valid roles: READ, WRITE, READ_WRITE
     * @throws Exception
     */
    public static void addUser(String user, String password, String role) throws Exception {
        if(users.containsKey(user))
            throw new Exception("User already exists, user removeUser() first");
        users.put(user, new User(user, password, role));
    }

    /**
     * Remove user
     * @param user The user name
     * @throws Exception
     */
    public static void removeUser(String user) throws Exception {
        if(users.containsKey(user))
            throw new Exception("User does not exist");
    }

    /**
     * Remove all users
     */
    public static void removeAllUsers() {
        users.clear();
    }

    /**
     * Get all users
     * @return The users
     */
    public static HashMap<String, User> getUsers() {
        return new HashMap<>(users);
    }
}
