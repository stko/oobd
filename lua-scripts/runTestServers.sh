#!/bin/sh
php -S localhost:8080 &
( cd testWSServer ; python oobdWsSimulator.py & ) 
