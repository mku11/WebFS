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

### Config
There is a Spring Boot configuration file config/application.properties  
Make sure you create a p12 keystore named keystore.p12  
The default port is 8443 but you can change it  
For testing purposes you might want to disable the SSL configuration  
  
The primary user and password is provided in the config file but you can change it. 
If you want to add more users use AuthUsers.add()  

### Examples:
The following examples are provided with curl.  
If you're testing and don't use SSL then use http://localhost/8080/api...  
Also if you're testing and use a self-signed certificate then add curl option -k  

Get details about a file  
```
curl -X GET "https://localhost:8443/api/info?path=/dir1/file.dat" -u user:password
```

List files and directories under a directory  
```
curl -X GET "https://localhost:8443/api/list?path=/dir1/dir2"
```

Create a directory  
```
curl -X POST "https://localhost:8443/api/mkdir?path=/dir/newdir"
```

Create a file  
```
curl -X POST "https://localhost:8443/api/mkdir?path=/dir/file.dat"
```

Upload a file  
```
curl -X POST -F "file=@D:/tmp/testdata/data.dat" "https://localhost:8443/api/upload?path=/dir/file.dat&position=0"
```    
    
Get/Download a file  
```
curl -X GET "https://localhost:8443/api/get?path=/dir/file.dat"
```
        
Copy a file to the destination directory  
```
curl -X PUT "https://localhost:8443/api/copy?sourcePath=/dir1/file.dat&destDir=/dir2&filename=newfile.dat"
```
    
Move a file to the destination directory  
```
curl -X PUT "https://localhost:8443/api/move?sourcePath=/dir1/file.dat&destDir=/dir2&filename=newfile.dat"
```
   
Rename a file or directory    
```
curl -X PUT "https://localhost:8443/api/rename?path=/dir/file.dat&filename=newfile.dat"
```

Delete a file or directory  
```
curl -X DELETE "https://localhost:8443/api/delete?path=/dir/file.dat"
```
    
Set the file length  
```
curl -X PUT "https://localhost:8443/api/rename?path=/dir/file.dat&length=1204"
```


