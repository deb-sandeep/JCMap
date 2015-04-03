@echo off

set JCMAP_HOME=%~dp0

set CP=%JCMAP_HOME%\lib\*;%JCMAP_HOME%\config

start javaw -classpath "%CP%" com.sandy.jcmap.JCMap
