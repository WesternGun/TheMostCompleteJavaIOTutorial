#!/bin/sh
read -p "Which branch to pull? " branch
git pull origin $branch
