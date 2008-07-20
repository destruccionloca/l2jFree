#!/bin/bash


err=1
until [ $err == 0 ]; 
do
	. ./setenv.sh
	[ -f log/java0.log.0 ] && mv log/java0.log.0 "log/java/`date +%Y-%m-%d_%H-%M-%S`_java0.log.0"
	[ -f log/stdout.log ] && mv log/stdout.log "log/stdout/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
# For developers mostly (1. line gc logrotate, 2. line parameters for gc logging):
#	[ -f log/gc.log ] && mv log/gc.log "log/gc/`date +%Y-%m-%d_%H-%M-%S`_gc.log"
#	-verbose:gc -Xloggc:log/gc.log -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution
# Alternative startup by NB4L1
#	java -Dfile.encoding=UTF-8 -Xmn128m -Xms512m -Xmx1024m -server com.l2jfree.gameserver.GameServer > log/stdout.log 2>&1
	java -Dfile.encoding=UTF-8 -Xmx1024m com.l2jfree.gameserver.GameServer > log/stdout.log 2>&1
	err=$?
#	/etc/init.d/mysql restart
	sleep 10;
done

