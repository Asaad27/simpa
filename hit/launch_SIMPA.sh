#!/bin/bash

hit="$(cd "$(dirname "$0")"; pwd)"
bin="$hit/bin"

classpath="$(find "$hit/lib" -iname '*.jar' -exec echo -n ':{}' \;)"
mkdir -p "$bin"
echo "compiling"
pushd "$hit/src" >/dev/null
java -jar "$hit/lib/antlr-4.5.3-complete.jar" -no-visitor -package tools.antlr4.DotMealy "$hit/src/tools/antlr4/DotMealy/DotMealy.g4"
popd > /dev/null
find "$hit/src" -name '*.java' -exec javac -d "$bin" -Xlint:none -classpath "$classpath" {} +
echo "compiling done"

java -classpath "$bin:$classpath" main.simpa.SIMPA "$@"
