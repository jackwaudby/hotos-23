#!/bin/bash

# shellcheck disable=SC2034
for i in {1..1000} ; do
  for c in 10 15 20 25 30
  do
      java -cp ../target/sim-lock-escalation-1.0-SNAPSHOT.jar Main --keys 100000 --transactionSize 10 --communities $c --ranges $c
  done
done
