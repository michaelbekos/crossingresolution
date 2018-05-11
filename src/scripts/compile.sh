#!/bin/sh
tmpfile=$(mktemp)
find ../ -name "*.java" > "$tmpfile"
javac -XDignore.symbol.file -cp ../yfiles-for-java.jar:../commons-cli-1.4.jar:../:. @"$tmpfile"
rm "$tmpfile"