#!/bin/bash
set -e
if [ -z "$JAVA_HOME" ]; then
  echo "JAVA_HOME must point to JDK 17 or higher"
  exit 1
fi
JAVA_VER=$("$JAVA_HOME/bin/javap" -verbose java.lang.String | grep "major version" | cut -d " " -f5)
if [ "$((JAVA_VER))" -lt 61 ]; then
  echo "Java version 17 or higher is required to build the cloudbeds integrator"
  exit 1
fi
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
cd "$SCRIPTPATH/.."
PROFILE="$1"

mvn package