@echo off
SETLOCAL enableextensions
:uniqLoop
set "tmpfile=%tmp%\bat~%RANDOM%.tmp"
if exist "%uniqueFileName%" goto :uniqLoop
dir /s /B *.java > "%tmpfile%"
javac -encoding utf8 -Xlint:unchecked -cp ".;yfiles-for-java.jar" @"%tmpfile%"
del "%tmpfile%"