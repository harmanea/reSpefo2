#!/bin/bash

declare -A build

build[gtk.linux.x86_64]=4.12.0
# build[gtk.linux.x86]=4.9.0
build[win32.win32.x86_64]=4.12.0
# build[win32.win32.x86]=4.9.0
build[cocoa.macosx.x86_64]=4.12.0

for b in "${!build[@]}"; do
    gradle -PswtPlatform="$b" -PswtRelease="${build[$b]}" fatJar
done
