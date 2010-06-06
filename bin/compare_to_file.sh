#! /bin/sh
java=/usr/java/j2sdk1.4.2/bin/java
$java -Xmx256m -jar jars/drg.jar -f "$1" -c "$2"
