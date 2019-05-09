#!/bin/bash

rm -r out
mkdir out
javac $(find src -name "*.java") -d out            
mkdir out/META-INF                     
echo "Manifest-Version: 1.0            
Implementation-Title: DrawingPad  
Implementation-Vendor: ByteHamster
Permissions: sandbox    
JavaFX-Version: 8.0 
Class-Path:                
Main-Class: com.bytehamster.drawingpad.Main
" > out/META-INF/MANIFEST.MF
cd out                                 
jar cmf META-INF/MANIFEST.MF drawingPad.jar *
mkdir addon
cp ../ankiAddon/* addon
cp drawingPad.jar addon/drawingPad.jar
cd addon
zip -q ../drawingPad.ankiaddon *

echo "Created out/drawingPad.ankiaddon"
