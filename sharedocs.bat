@ECHO OFF

cd /d %~dp0
CALL "sharedocsEnv.bat"

CALL "skinny.bat" %*
