#!/usr/bin/env bash

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS=""

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "ERROR: $*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

# Attempt to realpath executable if it's a symlink
if [ -L "$0" ]; then
    # Use readlink to resolve the symlink
    EXECUTABLE_PATH_SYMLINK=`readlink ./"$0"`
    # If the path is relative, make it absolute
    if [ "${EXECUTABLE_PATH_SYMLINK:0:1}" != "/" ]; then
        EXECUTABLE_PATH_SYMLINK="`dirname ./"$0"`/$EXECUTABLE_PATH_SYMLINK"
    fi
else
    EXECUTABLE_PATH_SYMLINK=
fi

# Get the real path to the executable
if [ -n "$EXECUTABLE_PATH_SYMLINK" ]; then
    EXECUTABLE_PATH=`realpath "$EXECUTABLE_PATH_SYMLINK"`
else
    EXECUTABLE_PATH=`realpath ./"$0"`
fi

# Get the directory of the executable
EXECUTABLE_DIR=`dirname "$EXECUTABLE_PATH"`

# For Cygwin, switch paths to Windows format before running java
if $cygwin ; then
    [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --windows "$JAVA_HOME"`
    [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if ! $cygwin && ! $darwin && ! $nonstop; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            # use the system max
            MAX_FD="$MAX_FD_LIMIT"
        fi
        ulimit -n $MAX_FD
        if [ $? -ne 0 ] ; then
            warn "Could not set maximum file descriptor limit: $MAX_FD"
        fi
    else
        warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# Add the command-line arguments to the list of arguments
# ... (the rest of the script)

# Collect all arguments for the java command, stacking in reverse order:
#   * args passed to the gradle wrapper
#   * java command
#   * gradle opts
#   * java opts
#   * default jvm opts
#
# This is done differently on nonstop, where the java command can't be rebuilt.
if $nonstop ; then
  # nonstop doesn't support arguments with spaces, so we need to quote them
  for arg in "$@" ; do
    if [[ "$arg" == *" "* ]]; then
      if [[ "$arg" == *'"'* ]]; then
        # just hope for the best, there is no true way to quote this
        # for the nonstop shell
        arg="'$arg'"
      else
        arg=\"$arg\"
      fi
    fi
    CMD_LINE_ARGS="$CMD_LINE_ARGS $arg"
  done
  # a space is required before the first argument
  CMD_LINE_ARGS=" $CMD_LINE_ARGS"
else
  CMD_LINE_ARGS=()
  for arg in "$@" ; do
    CMD_LINE_ARGS+=("$arg")
  done
fi

# Split up the JVM options string into an array, following the shell quoting and substitution rules
function splitJvmOpts() {
  JVM_OPTS=()
  for opt in "$@" ; do
    # echo "Processing option: '$opt'"
    case "$opt" in
      # Separate the parameter from the value
      -D* | -X* | --*)
        # It's a single option, so we can just append it
        JVM_OPTS+=("$opt")
        ;;
      *)
        # It's a space-separated option, so we need to split it
        for word in $opt ; do
          JVM_OPTS+=("$word")
        done
        ;;
    esac
  done
}

# Collect the command-line options
splitJvmOpts $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS

# Get the project's root directory
PROJECT_DIR="$EXECUTABLE_DIR"

# Get the gradle wrapper jar
GRADLE_WRAPPER_JAR="$PROJECT_DIR/gradle/wrapper/gradle-wrapper.jar"

# Get the gradle wrapper properties
GRADLE_WRAPPER_PROPERTIES="$PROJECT_DIR/gradle/wrapper/gradle-wrapper.properties"

# Execute the command
if $nonstop ; then
    "$JAVACMD" "${JVM_OPTS[@]}" -Dorg.gradle.appname="$APP_BASE_NAME" -classpath "$GRADLE_WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain $CMD_LINE_ARGS
else
    exec "$JAVACMD" "${JVM_OPTS[@]}" -Dorg.gradle.appname="$APP_BASE_NAME" -classpath "$GRADLE_WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "${CMD_LINE_ARGS[@]}"
fi
