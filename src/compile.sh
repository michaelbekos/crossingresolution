#!/bin/sh
tmpfile=$(mktemp)
find ./ -name "*.java" > "$tmpfile"
javac -cp ./yfiles-for-java.jar:. @"$tmpfile"
rm "$tmpfile"
