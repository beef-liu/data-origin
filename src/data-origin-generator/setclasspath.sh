#!/bin/sh
#JAVA_HOME=/usr/java/jdk1.6.0_43
JAVA_HOME=/Library/Java/Home
CLASSPATH=$JAVA_HOME/lib/tools.jar
for filename in /usr/share/scala/lib/*.jar;
do
  CLASSPATH=$CLASSPATH:$filename
done
_RUNJAVA="$JAVA_HOME/bin/java"
JAVA_ENDORSED_DIRS=
