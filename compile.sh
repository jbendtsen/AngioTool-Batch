#!/usr/bin/sh
JAVAC="javac"
JAVA="java"
rm -rf build
mkdir build
cd source
find . | grep "\.java" > sources.txt
$JAVAC -source 8 -target 8 -d ../build -cp lib/AbsoluteLayout.jar:lib/ij.jar:lib/Jama-1.0.2.jar:lib/mail.jar:lib/markSlider.jar:lib/poi-3.7-20101029.jar:. @sources.txt
[[ $? -ne 0 ]] && exit
rm ../AngioTool-Batch.jar
find images icons META-INF doc | zip AngioTool-Batch.jar -@
cd ../build
find . | grep "\.class" | zip ../source/AngioTool-Batch.jar -@
cd ..
mv source/AngioTool-Batch.jar .
$JAVA -Dsun.java2d.uiScale=2 -jar AngioTool-Batch.jar
