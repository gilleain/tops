#!/bin/sh
#path to the Connector/J JDBC Drivers
PATH_TO_MYSQL=/Users/maclean/languages/java/jakarta-tomcat-5.5.9/common/lib/mysql-connector-java-3.1.8-bin.jar

#path to the jar file with database query code
PATH_TO_DBJAR=~/tops/jars/db.jar

#david westhead's code (the 'protein' package)
PATH_TO_DWJAR=~/tops/dw/dw_tops.jar

JAVA=/System/Library/Frameworks/JavaVM.framework/Versions/1.5/Home/bin/java

CP=$PATH_TO_MYSQL:$PATH_TO_DBJAR:$PATH_TO_DWJAR
"$JAVA" -cp "$CP" tops.db.ProteinFactory $@
