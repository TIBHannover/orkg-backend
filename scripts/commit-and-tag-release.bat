@echo off
git commit -m "Release version %1"
git tag "%1" -m "Release version %1"
