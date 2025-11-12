@echo off
SET CURRDIR=%~dp0
java -cp %CURRDIR%webfs-service.war org.springframework.boot.loader.WarLauncher