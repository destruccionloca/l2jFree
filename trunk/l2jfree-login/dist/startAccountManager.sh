#!/bin/sh
. ./setenv.sh
java -Djava.util.logging.config.file=console.cfg net.sf.l2j.accountmanager.AccountManager
