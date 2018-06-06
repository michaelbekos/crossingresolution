#!/bin/sh
tmpfile=$(mktemp)
find ../ -name "*.java" > "$tmpfile"
javac -XDignore.symbol.file -cp ../yfiles-for-java.jar:../gson-2.8.2.jar:../:. @"$tmpfile"
rm "$tmpfile"
