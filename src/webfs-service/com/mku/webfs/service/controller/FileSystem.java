package com.mku.webfs.service.controller;
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

import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility for file system operations
 */
public class FileSystem {
    private static final int BUFF_LENGTH = 32768;
	private static final Pattern pattern = Pattern.compile("^.+[\\:\\*\\?\\<\\>\\|]+$");
    private String path;

    private static FileSystem instance;

    public static FileSystem getInstance() {
        if(instance == null)
            instance = new FileSystem();
        return instance;
    }


    public void setPath(String path) {
        this.path = path;
    }

    public File getRoot() {
        File realRoot = new File(path);
        return realRoot;
    }

    public File getFile(String path) {
		path = validateFilePath(path);
        String[] parts = path.split("/");
        File file = getRoot();
        for (String part : parts) {
            if (part.length() == 0)
                continue;
            if (part.equals(".."))
                throw new RuntimeException("Backwards traversing (..) is not supported");
            file = new File(file, part);
			if(file == null)
				return null;
        }
		validateFile(file);
        return file;
    }

    public String getRelativePath(File file) {
        return new File(file.getPath()).getPath().replace(
                new File(path).getPath(), "").replace("\\", "/");
    }

    public File write(String path, MultipartFile file, long position) throws IOException {
        File rFile = getFile(path);
        if (!rFile.exists()) {
            rFile.createNewFile();
        }
        InputStream inputStream = file.getInputStream();
        return write(rFile, inputStream, position);
    }

    public File write(File rFile, InputStream inputStream, long position) throws IOException {
        FileOutputStream outputStream = null;
        try {
            inputStream.skip(position);
            outputStream = new FileOutputStream(rFile);

            byte[] buff = new byte[BUFF_LENGTH];
            int bytesRead;
            while ((bytesRead = inputStream.read(buff, 0, buff.length)) > 0) {
                outputStream.write(buff, 0, bytesRead);
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
        }
        return rFile;
    }
	
	public String validateFilePath(String path) {
		path = StringEscapeUtils.escapeHtml4(path);
		Matcher matcher = pattern.matcher(path);
		if(matcher.matches()){
			throw new RuntimeException("Invalid characters in file path found");
		}
		return path;
	}
	
	public void validateFile(File file) {
		try {
			java.io.File f = new java.io.File(file.getPath());
			java.io.File r = new java.io.File(getRoot().getPath());
			if(!f.getCanonicalPath().startsWith(r.getCanonicalPath()))
				throw new RuntimeException("Could not validate file path");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
