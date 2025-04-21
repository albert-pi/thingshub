#!/bin/bash

error_exit ()
{
    echo "ERROR: $1 !!"
    exit 1
}

BASE_DIR=`cd $(dirname $0)/..; pwd`

# -e 是否存在该目录或者文件
[ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=$HOME/bin/java
[ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=/usr/java
[ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=/opt/thingshub/java
[ ! -e "$JAVA_HOME/bin/java" ] && unset JAVA_HOME

THINGSHUB_VERSION="1.0.0"

# -z 字符串长度为0，变量为空
if [ -z "$JAVA_HOME" ]; then
	if [ -e "$BASE_DIR/jdk/bin/java" ]; then
		error_exit "Please set the JAVA_HOME variable in your environment or add your JDK to $BASE_DIR/jdk directory!"
	fi
	
	JAVA_HOME=$BASE_DIR/jdk
	JAVA_CMD="$JAVA_HOME/bin/java"
	JAVA_MAJOR_VERSION=$($JAVA_CMD -version 2>&1 | sed -E -n 's/.* version "([0-9]*).*$/\1/p')	
else
	JAVA_CMD="$JAVA_HOME/bin/java"
	JAVA_MAJOR_VERSION=$($JAVA_CMD -version 2>&1 | sed -E -n 's/.* version "([0-9]*).*$/\1/p')
fi

#===========================================================================================
# JVM Configuration
#===========================================================================================
JAVA_OPT="${JAVA_OPT} -server -Xms1g -Xmx1g -Xmn512m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=320m"
JAVA_OPT="${JAVA_OPT} -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${BASE_DIR}/logs/java_heapdump.hprof"
JAVA_OPT="${JAVA_OPT} -XX:-UseLargePages"
JAVA_OPT="${JAVA_OPT} -jar ${BASE_DIR}/bin/thingshub-${THINGSHUB_VERSION}.jar"

# if [ ! -d "${BASE_DIR}/logs" ]; then
#   mkdir ${BASE_DIR}/logs
# fi

echo "$JAVA_CMD ${JAVA_OPT}"

if [ ! -f "${BASE_DIR}/logs/thingshub.out" ]; then
  touch "${BASE_DIR}/logs/thingshub.out"
fi

# >> 将标准输出重定向到文件，并追加到该文件中。如果文件不存在，它将被创建
echo "$JAVA_CMD ${JAVA_OPT}" > ${BASE_DIR}/logs/thingshub.out 2>&1 &
nohup "$JAVA_CMD" ${JAVA_OPT} >> ${BASE_DIR}/logs/thingshub.out 2>&1 &

echo -n "Thingshub is starting..."

THINGSHUB_PROCESS="thingshub-${THINGSHUB_VERSION}.jar"
STARTUP_COMPLETE_MESSAGE="Thingshub started successfully"
OUT_FILE="${BASE_DIR}/logs/thingshub.out"

function is_thingshub_running {
    pgrep -f $THINGSHUB_PROCESS > /dev/null 2>&1
    return $?
}

function has_startup_completed {
    grep -q "$STARTUP_COMPLETE_MESSAGE" "$OUT_FILE"
    return $?
}

sleep 1
while true; do
    if is_thingshub_running; then
        if has_startup_completed; then
            echo "OK"
            break
        else
            echo -n ""
        fi
    else
        echo "failed"
        break
    fi
    sleep 1
done
