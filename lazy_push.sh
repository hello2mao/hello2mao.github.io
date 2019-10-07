#!/bin/sh
date=`date`

if [ $RANDOM -gt 10000 ];then 
echo $date >> ./git-history.txt
fi

git add .
git commit -m "Lazy push: $date"
git push
