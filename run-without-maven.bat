@echo off
chcp 65001 >nul
setlocal
REM 未安装 Maven 时可用：需已配置 JAVA_HOME 或 java/javac 在 PATH 中
where javac >nul 2>&1
if errorlevel 1 (
  echo [错误] 找不到 javac。请先安装 JDK 并配置 PATH 或 JAVA_HOME。
  exit /b 1
)
set "SRC=src\main\java\com\javadesign\addressbook\AddressBookApp.java"
set "OUT=out"
if not exist "%SRC%" (
  echo [错误] 找不到源文件: %SRC%
  exit /b 1
)
if not exist "%OUT%" mkdir "%OUT%"
javac -encoding UTF-8 -d "%OUT%" "%SRC%"
if errorlevel 1 exit /b 1
java -cp "%OUT%" com.javadesign.addressbook.AddressBookApp
endlocal
