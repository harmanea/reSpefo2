#!/bin/bash

declare -A build

build[gtk.linux.x86_64]=4.17.0
build[win32.win32.x86_64]=4.17.0
build[cocoa.macosx.x86_64]=4.17.0
build[cocoa.macosx.aarch64]=4.22.0

# We no longer use legacy 32-bit versions
# build[gtk.linux.x86]=4.9.0
# build[win32.win32.x86]=4.9.0

for b in "${!build[@]}"; do
    gradle -PswtPlatform="$b" -PswtRelease="${build[$b]}" fatJar
done
