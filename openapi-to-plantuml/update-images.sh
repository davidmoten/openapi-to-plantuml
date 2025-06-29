#!/bin/bash
set -e
cp target/outputs/*.svg src/docs/tests/
cp target/demos/*.svg src/docs/demos/
cp target/openapi-example.svg src/docs/
