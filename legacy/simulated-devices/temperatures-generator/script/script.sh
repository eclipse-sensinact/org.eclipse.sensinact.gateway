#!/bin/bash

FILENAME=data.csv

count=0
previousLine="toto"
finalLine="titi"

cat $FILENAME | while read LINE

do

currentLine=`echo $LINE | cut -d , -f 1-3`
value=`echo $LINE | cut -d , -f 5`

if [ $currentLine != $previousLine ]
then
	echo $finalLine >> test.csv
	finalLine=$currentLine
	previousLine=$currentLine
fi

finalLine+=",${value}"

done
