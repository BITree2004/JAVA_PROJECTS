#!/bin/bash
PATH_OF_SCRIPT=$(cd "$(dirname "${BASH_SOURCE[0]}")"&>/dev/null; pwd)
ROOT=$(cd "$PATH_OF_SCRIPT"; cd ../..; pwd)
PATH_OF_MY_MODULE="$ROOT"/java-advanced/java-solutions
PATH_OF_ARTIFACT="$ROOT"/java-advanced-2024/artifacts
IMPLEMENTOR=info.kgeorgiy.java.advanced.implementor.jar
PATH_OF_IMPLEMENTOR="$PATH_OF_ARTIFACT"/$IMPLEMENTOR

rm -rf "$PATH_OF_SCRIPT"/Implementor.jar
javac -cp "$PATH_OF_IMPLEMENTOR"\
  -d "$PATH_OF_SCRIPT" \
  "$PATH_OF_MY_MODULE"/info/kgeorgiy/ja/dmitriev/implementor/*.java \


(cd "$PATH_OF_SCRIPT";
jar --create \
  --file Implementor.jar \
  --main-class info.kgeorgiy.ja.dmitriev.implementor.Implementor \
  --manifest MANIFEST.MF \
  info
  )

rm -rf "$PATH_OF_SCRIPT"/info
