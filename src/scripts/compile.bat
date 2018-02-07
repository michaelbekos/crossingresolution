@echo off
SETLOCAL enableextensions
:uniqLoop
set "tmpfile=%tmp%\bat~%RANDOM%.tmp"
if exist "%uniqueFileName%" goto :uniqLoop
dir /s /B *.java > "%tmpfile%"
cat "%tmpfile%"
javac -encoding utf8 -Xlint:unchecked -XDignore.symbol.file -cp ".;yfiles-for-java.jar;commons-cli-1.4.jar" @"%tmpfile%"
del "%tmpfile%"