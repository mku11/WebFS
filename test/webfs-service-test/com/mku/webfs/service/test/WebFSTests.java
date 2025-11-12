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

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Basic tests for the Web Service
 */
public class WebFSTests {
    @BeforeAll
    public static void setup() throws Exception {
        WebFSTestHelper.startServer(WebFSTestHelper.TEST_OUTPUT_DIR);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        WebFSTestHelper.stopServer();
    }

    // use this test case for exploratory testing with CURL see RealFileController for examples
    @Test
    public void testStartServer() throws Exception {
//        Thread.sleep(6000000);
    }

    @Test
    public void testCreateDirAndFile() throws Exception {
        // create dir
        long time = System.currentTimeMillis();
        String dirPath = "/dir" + "_" + System.currentTimeMillis();
        JSONObject res = WebFSTestHelper.createDir(dirPath);
        File dir = new File(WebFSTestHelper.TEST_OUTPUT_DIR, dirPath);
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
        assertTrue(res.getBoolean("present"));
        assertFalse(res.getBoolean("file"));
        assertTrue(res.getBoolean("directory"));
        assertEquals(res.getString("name"), dir.getName());
        assertEquals(res.getString("path"), dirPath);
        assertEquals(res.getLong("length"), 0);
        assertTrue(Math.abs(res.getLong("lastModified") - time) < 5000);

        // get dir info
        res = WebFSTestHelper.getInfo(dirPath);
        assertTrue(res.getBoolean("present"));
        assertFalse(res.getBoolean("file"));
        assertTrue(res.getBoolean("directory"));
        assertEquals(res.getString("name"), dir.getName());
        assertEquals(res.getString("path"), dirPath);
        assertEquals(res.getLong("length"), 0);

        // create file
        time = System.currentTimeMillis();
        String filePath = dirPath + "/" + "test.dat";
        res = WebFSTestHelper.createFile(filePath);
        File file = new File(WebFSTestHelper.TEST_OUTPUT_DIR, filePath);
        assertTrue(file.exists());
        assertTrue(file.isFile());
        assertTrue(res.getBoolean("present"));
        assertTrue(res.getBoolean("file"));
        assertFalse(res.getBoolean("directory"));
        assertEquals(res.getString("name"), file.getName());
        assertEquals(res.getString("path"), filePath);
        assertTrue(Math.abs(res.getLong("lastModified") - time) < 5000);

        // get file info
        res = WebFSTestHelper.getInfo(filePath);
        assertTrue(res.getBoolean("present"));
        assertTrue(res.getBoolean("file"));
        assertFalse(res.getBoolean("directory"));
        assertEquals(res.getString("name"), file.getName());
        assertEquals(res.getString("path"), filePath);
        assertEquals(res.getLong("length"), 0);
        assertTrue(Math.abs(res.getLong("lastModified") - time) < 5000);
    }


    @Test
    public void testListFilesAndDirectories() throws Exception {
        String dirPath = "/dir" + "_" + System.currentTimeMillis();
        WebFSTestHelper.createDir(dirPath);
        File dir = new File(WebFSTestHelper.TEST_OUTPUT_DIR, dirPath);

        long time = System.currentTimeMillis();
        String filename = "test.dat";
        int filesNum = 10;
        for(int i =0; i < filesNum; i++) {
            WebFSTestHelper.createFile(dirPath + "/" + i + "_" + filename);
        }

        String dirname = "dir";
        int dirsNum = 5;
        for(int i =0; i < dirsNum; i++) {
            WebFSTestHelper.createDir(dirPath + "/" + i + "_" + dirname);
        }

        JSONArray jsonFiles = WebFSTestHelper.listFiles(dirPath);
        ArrayList<String> files = new ArrayList<>();
        ArrayList<String> filePaths = new ArrayList<>();
        ArrayList<String> dirs = new ArrayList<>();
        ArrayList<String> dirPaths = new ArrayList<>();
        for(int i =0; i<jsonFiles.length(); i++) {
            JSONObject file = (JSONObject) jsonFiles.get(i);
            if(file.getBoolean("file")) {
                files.add(file.getString("name"));
                filePaths.add(file.getString("path"));
            } else if (file.getBoolean("directory")) {
                dirs.add(file.getString("name"));
                dirPaths.add(file.getString("path"));
            }
            assertTrue(file.getBoolean("present"));
            assertTrue(Math.abs(file.getLong("lastModified") - time) < 30000);
        }
        files.sort(String::compareTo);
        filePaths.sort(String::compareTo);
        assertEquals(filesNum, files.size());
        for(int i =0; i<files.size(); i++) {
            assertEquals(files.get(i), i + "_" + filename);
            assertEquals(filePaths.get(i), dirPath + "/" + i + "_" + filename);

        }

        dirs.sort(String::compareTo);
        dirPaths.sort(String::compareTo);
        assertEquals(dirsNum, dirs.size());
        for(int i =0; i<dirs.size(); i++) {
            assertEquals(dirs.get(i), i + "_" + dirname);
            assertEquals(dirPaths.get(i), dirPath + "/" + i + "_" + dirname);
        }
    }

    @Test
    public void testUploadAndDownloadFile() throws Exception {
        // local dir
        String localDirPath = "/dir" + "_" + System.currentTimeMillis();
        File localDir = new File(WebFSTestHelper.TEST_OUTPUT_DIR, localDirPath);
        localDir.mkdir();
        File file = new File(localDir, "test.dat");
        byte[] data = WebFSTestHelper.getRandArray(1 * 1024 * 1024);
        WebFSTestHelper.createLocalFile(file, data);

        // remote dir
        String rDirPath = "/dir" + "_" + System.currentTimeMillis();
        WebFSTestHelper.createDir(rDirPath);
        File remoteDir = new File(WebFSTestHelper.TEST_OUTPUT_DIR, rDirPath);
        assertTrue(remoteDir.exists());
        assertTrue(remoteDir.isDirectory());

        // upload
        String remoteFilePath = rDirPath + "/" + file.getName();
        JSONObject res = WebFSTestHelper.uploadFile(file.getPath(), remoteFilePath, 0);
        long time = System.currentTimeMillis();
        File remoteFile = new File(remoteDir, file.getName());
        assertTrue(remoteFile.exists());
        assertEquals(remoteFile.length(), data.length);
        assertArrayEquals(WebFSTestHelper.getChecksumStream(new FileInputStream(remoteFile)),
                WebFSTestHelper.getChecksumStream(new FileInputStream(file)));
        assertTrue(res.getBoolean("present"));
        assertTrue(res.getBoolean("file"));
        assertFalse(res.getBoolean("directory"));
        assertEquals(res.getString("name"), file.getName());
        assertEquals(res.getString("path"), remoteFilePath);
        assertTrue(Math.abs(res.getLong("lastModified") - time) < 3000);

        // get the info after the upload
        res = WebFSTestHelper.getInfo(remoteFilePath);
        assertTrue(res.getBoolean("present"));
        assertTrue(res.getBoolean("file"));
        assertFalse(res.getBoolean("directory"));
        assertEquals(res.getString("name"), file.getName());
        assertEquals(res.getString("path"), remoteFilePath);
        assertEquals(res.getLong("length"), file.length());

        // local download dir
        String localDownloadDirPath = "/dir" + "_" + System.currentTimeMillis();
        File localDownloadDir = new File(WebFSTestHelper.TEST_OUTPUT_DIR, localDownloadDirPath);
        localDownloadDir.mkdir();

        // download
        File downloadFile = new File(localDownloadDir, file.getName());
        WebFSTestHelper.downloadFile(downloadFile.getPath(), remoteFilePath, 0);
        assertArrayEquals(WebFSTestHelper.getChecksumStream(new FileInputStream(file)),
                WebFSTestHelper.getChecksumStream(new FileInputStream(downloadFile)));
    }


    @Test
    public void testRenameFile() throws Exception {
        // local dir
        String localDirPath = "/dir" + "_" + System.currentTimeMillis();
        File localDir = new File(WebFSTestHelper.TEST_OUTPUT_DIR, localDirPath);
        localDir.mkdir();
        File file = new File(localDir, "test.dat");
        byte[] data = WebFSTestHelper.getRandArray(1 * 1024 * 1024);
        WebFSTestHelper.createLocalFile(file, data);

        // remote dir
        String rDirPath = "/dir" + "_" + System.currentTimeMillis();
        WebFSTestHelper.createDir(rDirPath);
        File remoteDir = new File(WebFSTestHelper.TEST_OUTPUT_DIR, rDirPath);

        // upload
        String remoteFilePath = rDirPath + "/" + file.getName();
        WebFSTestHelper.uploadFile(file.getPath(), remoteFilePath, 0);
        long time = System.currentTimeMillis();
        File remoteFile = new File(remoteDir, file.getName());
        assertTrue(remoteFile.exists());
        assertEquals(remoteFile.length(), data.length);
        assertArrayEquals(WebFSTestHelper.getChecksumStream(new FileInputStream(remoteFile)),
                WebFSTestHelper.getChecksumStream(new FileInputStream(file)));

        // rename
        time = System.currentTimeMillis();
        String newfilename = "newfile.dat";
        String newRemoteFilePath = rDirPath + "/" + newfilename;
        JSONObject res = WebFSTestHelper.rename(remoteFilePath, newfilename);
        assertTrue(res.getBoolean("present"));
        assertTrue(res.getBoolean("file"));
        assertFalse(res.getBoolean("directory"));
        assertEquals(res.getString("name"), newfilename);
        assertEquals(res.getString("path"), newRemoteFilePath);
        assertEquals(res.getLong("length"), file.length());
        assertTrue(Math.abs(res.getLong("lastModified") - time) < 1000);

        File newRemoteFile = new File(remoteDir, newfilename);
        assertTrue(newRemoteFile.exists());
        assertEquals(newRemoteFile.length(), data.length);
        assertArrayEquals(WebFSTestHelper.getChecksumStream(new FileInputStream(newRemoteFile)),
                WebFSTestHelper.getChecksumStream(new FileInputStream(file)));

        // get the info of the new path after the rename
        res = WebFSTestHelper.getInfo(newRemoteFilePath);
        assertTrue(res.getBoolean("present"));
        assertTrue(res.getBoolean("file"));
        assertFalse(res.getBoolean("directory"));
        assertEquals(res.getString("name"), newfilename);
        assertEquals(res.getString("path"), newRemoteFilePath);
        assertEquals(res.getLong("length"), file.length());
        assertTrue(Math.abs(res.getLong("lastModified") - time) < 1000);

        // get the info of the old path after the rename
        res = WebFSTestHelper.getInfo(remoteFilePath);
        assertFalse(res.getBoolean("present"));
        assertFalse(res.getBoolean("file"));
        assertFalse(res.getBoolean("directory"));
        assertEquals(res.getString("name"), file.getName());
        assertEquals(res.getString("path"), remoteFilePath);
        assertEquals(res.getLong("length"), 0);
        assertEquals(res.getLong("lastModified"), 0);
    }

    @Test
    public void testRenameDir() throws Exception {
        // remote dir
        String rDirName = "dir" + "_" + System.currentTimeMillis();
        String rDirPath = "/" + rDirName;
        WebFSTestHelper.createDir(rDirPath);
        File remoteDir = new File(WebFSTestHelper.TEST_OUTPUT_DIR, rDirPath);

        // rename
        long time = System.currentTimeMillis();
        String newDirname = "newdir" + "_" + System.currentTimeMillis();
        String newRemoteDirPath = "/" + newDirname;
        JSONObject res = WebFSTestHelper.rename(rDirPath, newDirname);
        assertTrue(res.getBoolean("present"));
        assertFalse(res.getBoolean("file"));
        assertTrue(res.getBoolean("directory"));
        assertEquals(res.getString("name"), newDirname);
        assertEquals(res.getString("path"), newRemoteDirPath);
        assertEquals(res.getLong("length"), 0);
        assertTrue(Math.abs(res.getLong("lastModified") - time) < 1000);

        File newRemoteDir = new File(WebFSTestHelper.TEST_OUTPUT_DIR, newDirname);
        assertTrue(newRemoteDir.exists());
        assertEquals(newRemoteDir.length(), 0);

        // get the info of the new path after the rename
        res = WebFSTestHelper.getInfo(newRemoteDirPath);
        assertTrue(res.getBoolean("present"));
        assertFalse(res.getBoolean("file"));
        assertTrue(res.getBoolean("directory"));
        assertEquals(res.getString("name"), newDirname);
        assertEquals(res.getString("path"), newRemoteDirPath);
        assertEquals(res.getLong("length"), 0);
        assertTrue(Math.abs(res.getLong("lastModified") - time) < 1000);

        // get the info of the old path after the rename
        res = WebFSTestHelper.getInfo(rDirPath);
        assertFalse(res.getBoolean("present"));
        assertFalse(res.getBoolean("file"));
        assertFalse(res.getBoolean("directory"));
        assertEquals(res.getString("name"), rDirName);
        assertEquals(res.getString("path"), rDirPath);
        assertEquals(res.getLong("length"), 0);
        assertEquals(res.getLong("lastModified"), 0);
    }

    @Test
    public void testDeleteFile() throws Exception {
        // local dir
        String localDirPath = "/dir" + "_" + System.currentTimeMillis();
        File localDir = new File(WebFSTestHelper.TEST_OUTPUT_DIR, localDirPath);
        localDir.mkdir();
        File file = new File(localDir, "test.dat");
        byte[] data = WebFSTestHelper.getRandArray(1 * 1024 * 1024);
        WebFSTestHelper.createLocalFile(file, data);

        // remote dir
        String rDirPath = "/dir" + "_" + System.currentTimeMillis();
        WebFSTestHelper.createDir(rDirPath);
        File remoteDir = new File(WebFSTestHelper.TEST_OUTPUT_DIR, rDirPath);

        // upload
        String remoteFilePath = rDirPath + "/" + file.getName();
        WebFSTestHelper.uploadFile(file.getPath(), remoteFilePath, 0);
        long time = System.currentTimeMillis();
        File remoteFile = new File(remoteDir, file.getName());
        assertTrue(remoteFile.exists());
        assertEquals(remoteFile.length(), data.length);
        assertArrayEquals(WebFSTestHelper.getChecksumStream(new FileInputStream(remoteFile)),
                WebFSTestHelper.getChecksumStream(new FileInputStream(file)));

        // delete
        time = System.currentTimeMillis();
        JSONObject res = WebFSTestHelper.delete(remoteFilePath);
        assertFalse(res.getBoolean("present"));
        assertFalse(res.getBoolean("file"));
        assertFalse(res.getBoolean("directory"));
        assertEquals(res.getString("name"), remoteFile.getName());
        assertEquals(res.getString("path"), remoteFilePath);
        assertEquals(res.getLong("length"), 0);
        assertEquals(res.getLong("lastModified"), 0);

        File newRemoteFile = new File(remoteDir, remoteFile.getName());
        assertFalse(newRemoteFile.exists());
        assertEquals(newRemoteFile.length(), 0);

        // get the info of the new path after the delete
        res = WebFSTestHelper.getInfo(remoteFilePath);
        assertFalse(res.getBoolean("present"));
        assertFalse(res.getBoolean("file"));
        assertFalse(res.getBoolean("directory"));
        assertEquals(res.getString("name"), remoteFile.getName());
        assertEquals(res.getString("path"), remoteFilePath);
        assertEquals(res.getLong("length"), 0);
        assertEquals(res.getLong("lastModified"), 0);
    }


    @Test
    public void testDeleteDir() throws Exception {
        // remote dir
        String dirName = "dir" + "_" + System.currentTimeMillis();
        String rDirPath = "/" + dirName;
        WebFSTestHelper.createDir(rDirPath);
        File remoteDir = new File(WebFSTestHelper.TEST_OUTPUT_DIR, rDirPath);

        // delete
        JSONObject res = WebFSTestHelper.delete(rDirPath);
        assertFalse(res.getBoolean("present"));
        assertFalse(res.getBoolean("file"));
        assertFalse(res.getBoolean("directory"));
        assertEquals(res.getString("name"), dirName);
        assertEquals(res.getString("path"), rDirPath);
        assertEquals(res.getLong("length"), 0);
        assertEquals(res.getLong("lastModified"), 0);

        assertFalse(remoteDir.exists());
        assertEquals(remoteDir.length(), 0);

        // get the info of the new path after the delete
        res = WebFSTestHelper.getInfo(rDirPath);
        assertFalse(res.getBoolean("present"));
        assertFalse(res.getBoolean("file"));
        assertFalse(res.getBoolean("directory"));
        assertEquals(res.getString("name"), dirName);
        assertEquals(res.getString("path"), rDirPath);
        assertEquals(res.getLong("length"), 0);
        assertEquals(res.getLong("lastModified"), 0);
    }
}
