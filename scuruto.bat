@ECHO OFF

cd /d %~dp0
CALL "scurutoEnv.bat"

CALL "skinny.bat" %*
