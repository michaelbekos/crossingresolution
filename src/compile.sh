#!/bin/sh
tmpfile=$(mktemp)
find ./ -name "*.java" > "$tmpfile"
javac -XDignore.symbol.file -cp ./yfiles-for-java.jar:. @"$tmpfile"
rm "$tmpfile"
