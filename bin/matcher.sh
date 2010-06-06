#! /bin/sh
JAR_DIR="jars"
java -cp ${JAR_DIR}/drg.jar tops.engine.drg.Matcher "$1" $2
