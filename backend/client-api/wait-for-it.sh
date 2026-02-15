#!/usr/bin/env bash
#   Use this script to test if a given TCP host/port are available

set -e

TIMEOUT=15
QUIET=0

echoerr() {
  if [[ "$QUIET" -ne 1 ]]; then echo "$@" 1>&2; fi
}

usage() {
  cat << USAGE >&2
Usage:
  $0 host:port [-t timeout] [-- command args]
  -q | --quiet                        Do not output any status messages
  -t TIMEOUT | --timeout=timeout      Timeout in seconds, zero for no timeout
  -- COMMAND ARGS                     Execute command with args after the test finishes
USAGE
  exit 1
}

wait_for() {
  if [[ "$TIMEOUT" -gt 0 ]]; then
    echoerr "Waiting for $HOST:$PORT (timeout: $TIMEOUT seconds)..."
  else
    echoerr "Waiting for $HOST:$PORT without a timeout..."
  fi

  for ((i=0;i<TIMEOUT;i++)); do
    if timeout 1 bash -c "cat < /dev/null > /dev/tcp/$HOST/$PORT"; then
      echoerr "$HOST:$PORT is available after $i second(s)."
      return 0
    fi
    sleep 1
  done

  echoerr "Timeout occurred after waiting $TIMEOUT seconds for $HOST:$PORT."
  exit 1
}

wait_for_wrapper() {
  if [[ "$QUIET" -eq 1 ]]; then
    "$@" > /dev/null 2>&1
  else
    "$@"
  fi
}

# Parse arguments
while [[ $# -gt 0 ]]
do
  case "$1" in
    *:* )
    HOSTPORT=(${1//:/ })
    HOST=${HOSTPORT[0]}
    PORT=${HOSTPORT[1]}
    shift 1
    ;;
    -q | --quiet)
    QUIET=1
    shift 1
    ;;
    -t)
    TIMEOUT="$2"
    shift 2
    ;;
    --timeout=*)
    TIMEOUT="${1#*=}"
    shift 1
    ;;
    --)
    shift
    CMD=("$@")
    break
    ;;
    -*)
    echoerr "Unknown option: $1"
    usage
    ;;
    *)
    usage
    ;;
  esac
done

if [[ -z "$HOST" || -z "$PORT" ]]; then
  echoerr "Error: You need to provide a host and port to test."
  usage
fi

wait_for

if [[ "$CMD" ]]; then
  exec "${CMD[@]}"
else
  exit 0
fi
