#!/bin/bash

# 后台运行jar包，并将日志写到指定文件
APP_NAME=batch-0.0.1-SNAPSHOT.jar
LOG_FILE=app.log

tpid=`ps -ef|grep $APP_NAME|grep -v grep|grep -v kill|awk '{print $2}'`
if [ ${tpid} ]; then
  echo 'Stop Process...'
  kill -9 $tpid
fi

# 再次查看进程是否已结束
tpid=`ps -ef|grep $APP_NAME|grep -v grep|grep -v kill|awk '{print $2}'`
if [ ${tpid} ]; then
  echo 'Stop Process...'
  kill -9 $tpid
else
  echo 'Stop Procecss Successfully!'
  echo 'start Procecss...'
  # 启动程序，将标准输出和标准错误重定向到 /usr/train/out/
  nohup java -jar $APP_NAME > /usr/train/out/batch.out 2>&1 &
  # 或者将日志输出重定向到文件
  # nohup java -jar $APP_NAME > $LOG_FILE 2>&1 &

  echo 'Process started successfully!'
fi
