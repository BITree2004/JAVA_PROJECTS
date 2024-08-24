#!/bin/bash
PATH_OF_SCRIPT=$(cd "$(dirname "${BASH_SOURCE[0]}")"&>/dev/null; pwd)
ROOT=$(cd "$PATH_OF_SCRIPT"; cd ../..; pwd)
PATH_OF_MY_MODULE="$ROOT"/java-advanced/java-solutions/
PATH_OF_MODULE="$ROOT"/java-advanced-2024/modules

MY_SUFFIX=info/kgeorgiy/ja/dmitriev
SUFFIX="$PATH_OF_MODULE"/info.kgeorgiy.java.advanced

IMPLEMENTOR="$SUFFIX".implementor
IMPLEMENTORS_CLASS="$IMPLEMENTOR"/info/kgeorgiy/java/advanced/implementor
ITERATIVE="$SUFFIX".iterative
ITERATIVE_CLASS="$ITERATIVE"/info/kgeorgiy/java/advanced/iterative

rm -rf "$ROOT"/java-advanced/javadoc

javadoc -d "$ROOT"/java-advanced/javadoc -private -author \
  "$PATH_OF_MY_MODULE""$MY_SUFFIX"/implementor/*.java\
  "$IMPLEMENTORS_CLASS"/Impler.java\
  "$IMPLEMENTORS_CLASS"/JarImpler.java\
  "$IMPLEMENTORS_CLASS"/ImplerException.java\
  "$IMPLEMENTORS_CLASS"/Impler.java\
  #"$PATH_OF_MY_MODULE""$MY_SUFFIX"/iterative/*.java\
  #"$ITERATIVE_CLASS"/ScalarIP.java\
  #"$ITERATIVE_CLASS"/NewScalarIP.java\
  #"$ITERATIVE_CLASS"/ListIP.java\
  #"$ITERATIVE_CLASS"/NewListIP.java\
