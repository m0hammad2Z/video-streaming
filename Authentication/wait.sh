#!/bin/bash

set -e

host="$1"
shift
port="$1"
shift
cmd="$@"

until timeout 1 bash -c "echo > /dev/tcp/$host/$port"; do
  echo "Waiting for $host:$port - sleeping"
  sleep 1
done

echo "$host:$port is available - executing command"
exec $cmd