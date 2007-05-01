@echo off

REM ############################################
REM ## You can change here your own DB params ##
REM ############################################
REM MYSQL BIN PATH
set mysqlBinPath=C:\Program Files\MySQL\MySQL Server 4.1\bin
REM LOGINSERVER
set lsuser=root
set lspass=
set lsdb=l2jdb
set lshost=localhost

REM GAMESERVER
set gsuser=root
set gspass=
set gsdb=l2jdb
set gshost=localhost
REM ############################################

set mysqldumpPath="%mysqlBinPath%\mysqldump"
set mysqlPath="%mysqlBinPath%\mysql"


:fix_L2JFREE
echo.
echo.
echo WARNING: You may get some errors here cause of the old update files,its ok with this.
echo most of them should be duplicate line 1 errors.
:askL2JFREE
set expprompt=x
set /p expprompt=Install L2JFREE gameserver DB tables: (y) yes or (n) no or (q) quit? 
if /i %expprompt%==y goto L2JFREEinstall
if /i %expprompt%==n goto end
if /i %expprompt%==q goto end
goto askL2JFREE

:L2JFREEinstall

echo UPDATE

%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/updates/061120-[14].sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/updates/061124-[34].sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/updates/061126-[59].sql

:end
echo Script complete.Duplicte Errors on line 1 occure when u already have the lines at your db.
pause
