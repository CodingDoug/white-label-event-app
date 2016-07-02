#!/bin/bash
JAVA_HOME=`/usr/libexec/java_home -version 1.7` ./gradlew :backend:appengineExplodeApp $*
