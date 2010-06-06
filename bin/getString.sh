#!/bin/sh
#path to the Connector/J JDBC Drivers
PATH_TO_MYSQL=/Users/maclean/languages/java/jakarta-tomcat-5.5.9/common/lib/mysql-connector-java-3.1.8-bin.jar

#path to the jar file with database query code
PATH_TO_DBJAR=jars/db.jar

#path to the jar file with beans code
PATH_TO_BEANJAR=jars/tops-beans.jar

#path to the jar file with engine code
PATH_TO_ENGINE=jars/engine.jar

JAVA=/System/Library/Frameworks/JavaVM.framework/Versions/1.5/Home/bin/java

CP=$PATH_TO_MYSQL:$PATH_TO_DBJAR:$PATH_TO_BEANJAR:$PATH_TO_ENGINE
"$JAVA" -cp "$CP" tops.db.generation.StringFactory $@
