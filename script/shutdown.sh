#!/bin/bash

pid=$(cat "thingshub.pid")
if ps -p ${pid} > /dev/null; then
    echo "The Thingshub(${pid}) is running..."
else
    echo "WARNING: Thingshub(${pid}) not running!"
    exit -1;
fi

kill ${pid}

# 每隔1秒检查一次
while true; do
    if ! ps -p ${pid} > /dev/null; then
        echo "Thingshub stopped."
        break
    fi
    sleep 1
done
