:: Copyright (c) 2020 Kentyou.
:: All rights reserved. This program and the accompanying materials
:: are made available under the terms of the Eclipse Public License v1.0
:: which accompanies this distribution, and is available at
:: http://www.eclipse.org/legal/epl-v10.html
::
:: Contributors:
::     Kentyou - initial API and implementation
@echo off
setlocal enabledelayedexpansion

REM Test Java is installed
where java /Q
if %ERRORLEVEL% NEQ 0 (
	@echo Java not found
	pause
	exit
)

echo Welcome to sensiNact. Please choose one of the following options.
echo 	1. Change current profiles
echo 	2. Launch sensiNact with the current profiles
set /P CHOICE="Enter your choice: "

cls

if %CHOICE%==1 (
	echo Choose the profiles to activate ^([X] active profile, [ ] inactive profile^):
	set VALUE=0
	
	for /r profile-available/ %%g in (*.cfg) do (
		set STATE= 
		set FILE=%%~nxg
		set TITLE=!FILE:org.apache.felix.fileinstall-=!
		set TITLE=!TITLE:.cfg=!
		
		if exist %CD%\profile-enabled\!FILE! (
			set STATE=X
		)
		
		echo 	[!STATE!] !VALUE!. !TITLE!
		set TABPROFILES[!VALUE!]=!FILE!
		set /A VALUE+=1
	)
	
	set /A VALUE-=1
	
	set /P PROFILES="Enter your choices (separate your choices with space): "
	
	for %%a in (!PROFILES!) do (
		if %%a gtr !VALUE! (
			echo Profile %%a does not exist. Exiting.
			pause
			exit
		)
	)
	
	del %CD%\profile-enabled\* /F /Q 
	
	for %%a in (!PROFILES!) do (
		copy /Y %CD%\profile-available\!TABPROFILES[%%a]! %CD%\profile-enabled\
	)
	
	pause
	exit
) else if %CHOICE% == 2 (
	java -Djava.security.policy=conf\all.policy -cp "bin\sensinact-condperm-1.1-SNAPSHOT.jar;bin\org.apache.felix.main-4.0.3.jar" org.apache.felix.main.Main
	exit
) else (
	@echo This option does not exist. Exiting.
	pause
	exit
)