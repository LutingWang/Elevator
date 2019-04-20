set CURRENT_DATE_TIME=%date:~0,4%-%date:~5,2%-%date:~8,2% %time:~0,2%:%time:~3,2%:%time:~6,2%    
echo %CURRENT_DATE_TIME% >> log.txt
datacheck_hw1.exe -i datacheck_in.txt >> log.txt
echo. >> log.txt