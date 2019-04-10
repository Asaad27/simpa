#!/bin/bash

base_dir="$(cd "$(dirname "$0")"; pwd)"
bin="$base_dir/bin"
src="$base_dir/src"

classpath="$(find "$base_dir/lib" -iname '*.jar' -exec echo -n ':{}' \;)"
mkdir -p "$bin"

lastSource=$(find "$src" -name '*.java' -exec stat --format=%Y {} \; | sort -n | tail -n 1)
lastBin=$(find "$bin" -name '*.class' -exec stat --format=%Y {} \; | sort -n | tail -n 1)

if [ "$lastBin" == "" ] || [[ "$lastSource" -gt "$lastBin" ]]
then
echo "compiling"
pushd "$src" >/dev/null
java -jar "$base_dir/lib/antlr-4.5.3-complete.jar" -no-visitor -package tools.antlr4.DotMealy "$base_dir/src/tools/antlr4/DotMealy/DotMealy.g4"
popd > /dev/null
find "$src" -name '*.java' -exec javac -d "$bin" -Xlint:none -classpath "$classpath" {} +
echo "compiling done"
fi

java -classpath "$bin:$classpath" main.simpa.SIMPA "$@"
