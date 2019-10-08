#!/bin/sh
date=`date`

git add .
git commit -m "Lazy push: $date"
git push
