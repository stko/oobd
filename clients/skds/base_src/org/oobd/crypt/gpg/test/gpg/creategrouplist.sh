#!/bin/sh
ls -1 -F --color=none $1 | grep --color=never "/" | sed -e 's/\///g' - 