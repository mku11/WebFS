# WebFS
WebFS is a remote filesystem web service.   
Allows most common operations like create, list, upload, and download files with basic authentication and simple role permission.  
  
WebFS is released under MIT License.  

### Features
* Operations: info, create, list, get/download, upload, delete, rename, copy, move
* Currently supports only Basic Auth (password-based authentication)
* Roles: READ, WRITE, READ_WRITE
* Supports SSL certificates
* Spring Boot configuration file

### Examples:

Get details about a file  
```
curl -X GET "http://localhost:8080/api/info?path=/dir1/file.dat"
```

List files and directories under a directory  
```
curl -X GET "http://localhost:8080/api/list?path=/dir1/dir2"
```

Create a directory  
```
curl -X POST "http://localhost:8080/api/mkdir?path=/dir/newdir"
```

Create a file  
```
curl -X POST "http://localhost:8080/api/mkdir?path=/dir/file.dat"
```

Upload a file  
```
curl -X POST -F "file=@D:/tmp/testdata/data.dat" "http://localhost:8080/api/upload?path=/dir/file.dat&position=0"
```    
    
Get/Download a file  
```
curl -X GET "http://localhost:8080/api/get?path=/dir/file.dat"
```
        
Copy a file to the destination directory  
```
curl -X PUT "http://localhost:8080/api/copy?sourcePath=/dir1/file.dat&destDir=/dir2&filename=newfile.dat"
```
    
Move a file to the destination directory  
```
curl -X PUT "http://localhost:8080/api/move?sourcePath=/dir1/file.dat&destDir=/dir2&filename=newfile.dat"
```
   
Rename a file or directory    
```
curl -X PUT "http://localhost:8080/api/rename?path=/dir/file.dat&filename=newfile.dat"
```

Delete a file or directory  
```
curl -X DELETE "http://localhost:8080/api/delete?path=/dir/file.dat"
```
    
Set the file length  
```
curl -X PUT "http://localhost:8080/api/rename?path=/dir/file.dat&length=1204"
```


