#!/bin/bash

base_dir="$(cd "$(dirname "$0")"; pwd)"
bin="$base_dir/bin"

classpath="$(find "$base_dir/lib" -iname '*.jar' -exec echo -n ':{}' \;)"
mkdir -p "$bin"
echo "compiling"
pushd "$base_dir/src" >/dev/null
java -jar "$base_dir/lib/antlr-4.5.3-complete.jar" -no-visitor -package tools.antlr4.DotMealy "$base_dir/src/tools/antlr4/DotMealy/DotMealy.g4"
popd > /dev/null
find "$base_dir/src" -name '*.java' -exec javac -d "$bin" -Xlint:none -classpath "$classpath" {} +
echo "compiling done"

java -classpath "$bin:$classpath" main.simpa.SIMPA "$@"
