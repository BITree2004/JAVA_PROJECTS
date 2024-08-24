#!/bin/bash
PATH_OF_SCRIPT=$(cd "$(dirname "${BASH_SOURCE[0]}")"&>/dev/null; pwd)
ROOT=$(cd "$PATH_OF_SCRIPT"; cd ..; pwd)
PATH_OF_TESTS="$ROOT"/java-solutions/info/kgeorgiy/ja/dmitriev/bank
PATH_OF_LIB="$ROOT"/lib

rm -rf "$PATH_OF_SCRIPT"/Implementor.jar
javac -cp "$PATH_OF_LIB/*:$ROOT/java-solutions:$PATH_OF_TESTS/*"\
  -d "$PATH_OF_SCRIPT" \
  "$PATH_OF_TESTS"/BankTests.java \

java -cp ".:$PATH_OF_LIB/*" info.kgeorgiy.ja.dmitriev.bank.BankTests
RETURN_CODE=$?
rm -r info
exit $RETURN_CODE
