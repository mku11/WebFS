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

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.mku.webfs.service.security.Security;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequestMapping("/api")
/**
 * Provides endpoints for manipulating the filesystem remotely. The file system is expected to contain encrypted
 * files. The function of these endpoints is to simply facilitate most common fs operations, all encryption and
 * decryption will happen at the client side.
 */
public class FileController {

    /**
     * Get details about a file<br>
     * example:
     * curl -X GET "http://localhost:8080/api/info?path=/dir1/file.dat"
     *
     * @param path The file path
     * @return
     */
    @GetMapping("/info")
    public FileResponse info(HttpServletRequest request, String path) throws IOException {
        Security.checkRead(request);
		path = FileSystem.getInstance().validateFilePath(path);
        System.out.println("INFO, path: " + path);
        File file = FileSystem.getInstance().getFile(path);
        if (file == null)
            throw new IOException("Partial path does not exist: " + path);
        return new FileResponse(file);
    }

    /**
     * List files and directories under a directory<br>
     * example:
     * curl -X GET "http://localhost:8080/api/list?path=/dir1/dir2"
     *
     * @param path The directory path
     * @return
     */
    @GetMapping("/list")
    public List<FileResponse> list(HttpServletRequest request, String path) throws IOException {
        Security.checkRead(request);
		path = FileSystem.getInstance().validateFilePath(path);
        System.out.println("LIST, path: " + path);
        ArrayList<FileResponse> list = new ArrayList<>();
        File file = FileSystem.getInstance().getFile(path);
        if (file == null || !file.exists())
            throw new IOException("Directory does not exist");
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File rFile : files)
                list.add(new FileResponse(rFile));
        } else {
            throw new IOException("Resource is a file");
        }
        return list;
    }

    /**
     * Create a directory<br>
     * example:
     * curl -X POST "http://localhost:8080/api/mkdir?path=/dir/newdir"
     *
     * @param path The directory path
     * @return
     */
    @PostMapping("/mkdir")
    public FileResponse mkdir(HttpServletRequest request, String path) throws IOException {
        Security.checkWrite(request);
		path = FileSystem.getInstance().validateFilePath(path);
        System.out.println("MKDIR, path: " + path);
        File file = FileSystem.getInstance().getFile(path);
        File parent = new File(file.getParent());
        if (!parent.exists())
            throw new IOException("Parent does not exist");
        file = new File(parent, file.getName());
        file.mkdir();
        if (!file.exists() || !file.isDirectory())
            throw new IOException("Could not create dir");
        return new FileResponse(file);
    }


    /**
     * Create a file<br>
     * example:
     * curl -X POST "http://localhost:8080/api/mkdir?path=/dir/file.dat"
     *
     * @param path The file path
     * @return
     */
    @PostMapping("/create")
    public FileResponse create(HttpServletRequest request, String path) throws IOException {
        Security.checkWrite(request);
		path = FileSystem.getInstance().validateFilePath(path);
        System.out.println("CREATE, path: " + path);
        File file = FileSystem.getInstance().getFile(path);
        File parent = new File(file.getParent());
        if (!parent.exists() || !parent.isDirectory())
            throw new IOException("Parent does not exist");
        file = new File(parent, file.getName());
        file.createNewFile();
        if (!file.exists() || !file.isFile())
            throw new IOException("Could not create file");
        return new FileResponse(file);
    }

    /**
     * Upload a file<br>
     * example:
     * curl -X POST -F "file=@D:/tmp/testdata/data.dat" "http://localhost:8080/api/upload?path=/dir/file.dat&position=0"
     *
     * @param file The file data
     * @param path The path to the file
     * @param path The byte position of the file that writing will start
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public ResponseEntity<FileResponse> upload(HttpServletRequest request, @RequestParam("file") MultipartFile file, String path, Long position) throws IOException {
        Security.checkWrite(request);
		path = FileSystem.getInstance().validateFilePath(path);
        System.out.println("UPLOAD, path: " + path + ", position: " + position + ", size: " + file.getSize());
        File rFile = FileSystem.getInstance().write(path, file, position);
        return new ResponseEntity<>(new FileResponse(rFile), position > 0 ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
    }

    /**
     * Get a file<br>
     * example:
     * curl -X GET "http://localhost:8080/api/get?path=/dir/file.dat"
     *
     * @param path The path to the file
     * @param path The byte position of the file that reading will start from
     * @return
     * @throws IOException
     */
    @GetMapping(path = "/get")
    public ResponseEntity<Resource> get(HttpServletRequest request, String path, Long position) throws IOException {
        Security.checkRead(request);
		path = FileSystem.getInstance().validateFilePath(path);
        System.out.println("GET, path: " + path + ", position: " + position);
        File rFile = FileSystem.getInstance().getFile(path);
        if (rFile == null || !rFile.exists() || !rFile.isFile())
            throw new IOException("File does not exist");
        FileInputStream stream = new FileInputStream(rFile);
        stream.skip(position);
        InputStreamResource resource = new InputStreamResource(stream);
        return ResponseEntity.status(position > 0 ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK)
                .contentLength(rFile.length() - position)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * Copy a file to the destination directory<br>
     * example:
     * curl -X PUT "http://localhost:8080/api/copy?sourcePath=/dir1/file.dat&destDir=/dir2&filename=newfile.dat"
     *
     * @param path    The file to copy
     * @param destDir The destination directory
     * @return
     * @throws IOException
     */
    @PostMapping("/copy")
    public FileResponse copy(HttpServletRequest request, String path, String destDir, String filename) throws IOException {
        Security.checkRead(request);
        Security.checkWrite(request);
		path = FileSystem.getInstance().validateFilePath(path);
		destDir = FileSystem.getInstance().validateFilePath(destDir);
		filename = FileSystem.getInstance().validateFilePath(filename);
        System.out.println("COPY, path: " + path + ", filename: " + filename);
        File source = FileSystem.getInstance().getFile(path);
        if (source == null || !source.exists())
            throw new IOException("Path does not exist");
        File dest = FileSystem.getInstance().getFile(destDir);
        if (dest == null || !dest.exists() || !dest.isDirectory())
            throw new IOException("Destination directory does not exist");
        File nFile;
        if (source.isDirectory()) {
            throw new IOException("Cannot copy directories, use createDirectory and copy recursively");
        } else {
            filename = filename == null ? source.getName() : filename;
            nFile = new File(destDir, filename);
            FileInputStream stream = new FileInputStream(source);
            FileSystem.getInstance().write(nFile, stream, 0);
		}
        return new FileResponse(nFile);
    }


    /**
     * Move a file to the destination directory<br>
     * example:
     * curl -X PUT "http://localhost:8080/api/move?sourcePath=/dir1/file.dat&destDir=/dir2&filename=newfile.dat"
     *
     * @param path     The file to move
     * @param destDir  The destination directory
     * @param filename The new file name (optional)
     * @return
     * @throws IOException
     */
    @PutMapping("/move")
    public FileResponse move(HttpServletRequest request, String path, String destDir, String filename) throws IOException {
        Security.checkRead(request);
        Security.checkWrite(request);
		path = FileSystem.getInstance().validateFilePath(path);
		destDir = FileSystem.getInstance().validateFilePath(destDir);
		filename = FileSystem.getInstance().validateFilePath(filename);
        System.out.println("MOVE, path: " + path + ", destDir: " + destDir + ", filename: " + filename);
        File source = FileSystem.getInstance().getFile(path);
        if (source == null || !source.exists())
            throw new IOException("Path does not exist");
        File dest = FileSystem.getInstance().getFile(destDir);
        if (dest == null || !dest.exists() || !dest.isDirectory())
            throw new IOException("Destination directory does not exist");
        File nFile;
        if (source.isDirectory()) {
            throw new IOException("Cannot move directories, use createDirectory and move recursively");
        } else {
			filename = filename == null ? source.getName() : filename;
            nFile = new File(destDir, filename);
            source.renameTo(nFile);
		}
        return new FileResponse(nFile);
    }

    /**
     * Rename a file or directory<br>
     * example:
     * curl -X PUT "http://localhost:8080/api/rename?path=/dir/file.dat&filename=newfile.dat"
     *
     * @param path     The file or directory path
     * @param filename The new filename
     * @return
     * @throws IOException
     */
    @PutMapping("/rename")
    public FileResponse rename(HttpServletRequest request, String path, String filename) throws IOException {
        Security.checkWrite(request);
		path = FileSystem.getInstance().validateFilePath(path);
		filename = FileSystem.getInstance().validateFilePath(filename);
        System.out.println("RENAME, path: " + path + ", filename: " + filename);
        File file = FileSystem.getInstance().getFile(path);
        if (file == null || !file.exists())
            throw new IOException("Path does not exist");
        File nFile = new File(file.getParent(), filename);
        file.renameTo(nFile);
        return new FileResponse(nFile);
    }

    /**
     * Delete a file or directory<br>
     * example:
     * curl -X DELETE "http://localhost:8080/api/delete?path=/dir/file.dat"
     *
     * @param path The file or directory path
     * @return
     */
    @DeleteMapping("/delete")
    public FileResponse delete(HttpServletRequest request, String path) throws IOException {
        Security.checkWrite(request);
		path = FileSystem.getInstance().validateFilePath(path);
        System.out.println("DELETE, path: " + path);
        File file = FileSystem.getInstance().getFile(path);
        if (file == null || !file.exists())
            throw new IOException("Path does not exist");
        file.delete();
        return new FileResponse(file);
    }

    /**
     * Set the file length<br>
     * example:
     * curl -X PUT "http://localhost:8080/api/rename?path=/dir/file.dat&length=1204"
     *
     * @param path   The file
     * @param length The new size
     * @return
     * @throws IOException
     */
    @PutMapping("/setLength")
    public FileResponse setLength(HttpServletRequest request, String path, long length) throws IOException {
        Security.checkWrite(request);
		path = FileSystem.getInstance().validateFilePath(path);
        System.out.println("SETLENGTH, path: " + path + ", length: " + length);
        FileOutputStream stream = null;
        try {
            File file = FileSystem.getInstance().getFile(path);
            if (file == null || !file.exists())
                throw new IOException("Path does not exist");
            stream = new FileOutputStream(file);
            RandomAccessFile raf = new RandomAccessFile(file.getPath(), "rw");
            raf.setLength(length);
            return new FileResponse(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            stream.close();
        }
    }
}
