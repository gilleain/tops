#! /bin/sh
JAR_DIR="/Users/maclean/tops/jars"
java -cp ${JAR_DIR}/2D.jar:${JAR_DIR}/drg.jar tops.view.tops2D.app.TOPSViewer $*
