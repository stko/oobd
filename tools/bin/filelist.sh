#!/bin/bash
cd $1
shift
while [ $# -gt 1 ]
  do
  echo -n $1 " "
  shopt -s nullglob
  for i in *.$2
    do echo -n $i " "
  done
  echo
  shift 2
done