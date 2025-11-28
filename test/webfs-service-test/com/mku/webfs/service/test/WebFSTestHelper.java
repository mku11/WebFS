package com.mku.webfs.service.test;
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

import com.mku.webfs.service.WebFSApplication;
import com.mku.webfs.service.controller.FileSystem;
import com.mku.webfs.service.security.AuthUsers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class WebFSTestHelper {
    //    public static String HOST = "http://localhost:8080";
    public static String HOST = "https://localhost:8443";
    public static String TEST_OUTPUT_DIR = "D:\\tmp\\webfs\\test\\output";
    private static String user = "user";
    private static String password = "password";
    public static String role = "READ_WRITE";
    private static boolean serverStarted;
    private static final Random random = new Random(System.currentTimeMillis());
    private static Runtime rt = Runtime.getRuntime();


    public static void startServer(String dir) throws Exception {
        if (serverStarted)
            throw new Exception("Another instance is running, use stopServer to stop");
        AuthUsers.addUser(user, password, role);
        serverStarted = true;
        try {
            WebFSApplication.start(new String[0]);
            FileSystem.getInstance().setPath(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopServer() {
        WebFSApplication.stop();
        serverStarted = false;
    }


    public static void createLocalFile(File file, String contents) throws Exception {
        FileOutputStream stream = new FileOutputStream(file);
        byte[] data = contents.getBytes();
        createLocalFile(file, data);
    }

    public static void createLocalFile(File file, byte[] data) throws Exception {
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(data, 0, data.length);
        stream.flush();
        stream.close();
    }

    public static byte[] getRandArray(int size) {
        byte[] data = new byte[size];
        random.nextBytes(data);
        return data;
    }

    public static JSONObject createDir(String remotePath) throws IOException, JSONException {
        remotePath = remotePath.replaceAll("\\\\", "/");
        String cmd = "curl -X POST " +
                "\"" + HOST
                + "/api/mkdir"
                + "?path=" + remotePath + "\""
                + " -u " + user + ":" + password
                + " -k ";
        byte[] data = send(cmd);
        String response = new String(data);
        System.out.println(response);
        JSONObject obj = new JSONObject(response);
        return obj;
    }

    public static JSONObject createFile(String remotePath) throws IOException, JSONException {
        remotePath = remotePath.replaceAll("\\\\", "/");
        String cmd = "curl -X POST " +
                "\"" + HOST
                + "/api/create"
                + "?path=" + remotePath + "\""
                + " -u " + user + ":" + password
                + " -k ";
        byte[] data = send(cmd);
        String response = new String(data);
        System.out.println(response);
        JSONObject obj = new JSONObject(response);
        return obj;
    }

    public static byte[] send(String cmd) throws IOException {
        Process pr = rt.exec(cmd);
        InputStream stream = null;
        try {
            pr.waitFor();
            stream = pr.getInputStream();
            byte[] data = stream.readAllBytes();
            return data;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (stream != null)
                stream.close();
        }
    }

    public static JSONObject uploadFile(String localPath, String remotePath, long position) throws IOException, JSONException {
        remotePath = remotePath.replaceAll("\\\\", "/");
        String cmd = "curl -X POST "
                + " -F \"file=@" + localPath + "\" "
                + "\"" + HOST
                + "/api/upload"
                + "?path=" + remotePath
                + "&position=" + position + "\""
                + " -u " + user + ":" + password
                + " -k ";
        byte[] data = send(cmd);
        String response = new String(data);
        System.out.println(response);
        JSONObject obj = new JSONObject(response);
        return obj;
    }

    public static byte[] getChecksumStream(InputStream stream) throws NoSuchAlgorithmException, IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[256 * 1024];
            int bytesRead;
            while ((bytesRead = stream.read(buffer, 0, buffer.length)) > 0) {
                md.update(buffer, 0, bytesRead);
            }
            byte[] digest = md.digest();
            return digest;
        } finally {
            if (stream != null)
                stream.close();
        }
    }

    public static void downloadFile(String localPath, String remotePath, int position) throws IOException, JSONException {
        remotePath = remotePath.replaceAll("\\\\", "/");
        String cmd = "curl -X GET "
                + "\"" + HOST
                + "/api/get"
                + "?path=" + remotePath
                + "&position=" + position + "\""
                + " --output " + localPath
                + " -u " + user + ":" + password
                + " -k ";
        send(cmd);
    }

    public static JSONArray listFiles(String remotePath) throws IOException, JSONException {
        remotePath = remotePath.replaceAll("\\\\", "/");
        String cmd = "curl -X GET " +
                "\"" + HOST
                + "/api/list"
                + "?path=" + remotePath + "\""
                + " -u " + user + ":" + password
                + " -k ";
        byte[] data = send(cmd);
        String response = new String(data);
        System.out.println(response);
        JSONArray obj = new JSONArray(response);
        return obj;
    }

    public static JSONObject getInfo(String remotePath) throws IOException, JSONException {
        remotePath = remotePath.replaceAll("\\\\", "/");
        String cmd = "curl -X GET " +
                "\"" + HOST
                + "/api/info"
                + "?path=" + remotePath + "\""
                + " -u " + user + ":" + password
                + " -k ";
        byte[] data = send(cmd);
        String response = new String(data);
        System.out.println(response);
        JSONObject obj = new JSONObject(response);
        return obj;
    }

    public static JSONObject rename(String remotePath, String newFilename) throws IOException, JSONException {
        remotePath = remotePath.replaceAll("\\\\", "/");
        String cmd = "curl -X PUT " +
                "\"" + HOST
                + "/api/rename"
                + "?path=" + remotePath
                + "&filename=" + newFilename + "\""
                + " -u " + user + ":" + password
                + " -k ";
        byte[] data = send(cmd);
        String response = new String(data);
        System.out.println(response);
        JSONObject obj = new JSONObject(response);
        return obj;
    }

    public static JSONObject delete(String remotePath) throws IOException, JSONException {
        remotePath = remotePath.replaceAll("\\\\", "/");
        String cmd = "curl -X DELETE " +
                "\"" + HOST
                + "/api/delete"
                + "?path=" + remotePath + "\""
                + " -u " + user + ":" + password
                + " -k ";
        byte[] data = send(cmd);
        String response = new String(data);
        System.out.println(response);
        JSONObject obj = new JSONObject(response);
        return obj;
    }
}
