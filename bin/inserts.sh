#! /bin/sh
#USAGE : inserts.sh "pattern" string_file
#where "pattern" = eg "test N[0]E[0]E[1]C 1:2P"
#and string_file is the name of a file
JAR_DIR="/local/brc/users/maclean/tops/jars"
java -jar ${JAR_DIR}/inserts.jar "$1" $2 OFF
