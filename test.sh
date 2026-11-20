#!/bin/bash
set -e

cd "$(dirname "$0")"

./framework/test.sh
./test-framework/test-integration.sh
