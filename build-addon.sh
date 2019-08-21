#!/bin/bash

rm -r out
mkdir out
/usr/lib/jvm/java-8-openjdk/bin/javac $(find src -name "*.java") -d out            
mkdir out/META-INF                     
echo "Manifest-Version: 1.0            
Implementation-Title: DrawingPad  
Implementation-Vendor: ByteHamster
Class-Path:                
Main-Class: com.bytehamster.drawingpad.Main
" > out/META-INF/MANIFEST.MF
cd out                                 
/usr/lib/jvm/java-8-openjdk/bin/jar cmf META-INF/MANIFEST.MF drawingPad.jar *
mkdir addon
cp ../ankiAddon/* addon
cp drawingPad.jar addon/drawingPad.jar
cd addon
zip -q ../drawingPad.ankiaddon *

echo "Created out/drawingPad.ankiaddon"
