#!/bin/bash
set -e
set -o pipefail

archs='aarch64 amd64 armv7hf'
suite='jessie'
buildDeps='build-essential libpcre3-dev libssl-dev'
for arch in $archs; do
	baseImage='resin/'$arch'-debian'
	case $arch in
		"aarch64")
			customQemu='RUN qemu-aarch64-static -version \&\& sha256sum -b /usr/bin/qemu-aarch64-static\
ADD https://github.com/resin-io-library/base-images/raw/80a6c74407a90beb5a2b517119a823e776a052c7/debian/aarch64/jessie/qemu-aarch64-static /tmp/qemu-aarch64-static\
RUN chmod +x /tmp/qemu-aarch64-static \&\& mv /tmp/qemu-aarch64-static /usr/bin/qemu-aarch64-static\
RUN qemu-aarch64-static -version \&\& sha256sum -b /usr/bin/qemu-aarch64-static'
			;;
		*)
			customQemu=''
	esac;

	dockerfile=$arch.Dockerfile
	sed -e s~#{FROM}~$baseImage:$suite~g \
		-e s~#{BUILD_DEPS}~"$buildDeps"~g \
		-e s~#{CUSTOM_QEMU}~"$customQemu"~g \
		-e '/./,/^$/!d' \
		Dockerfile.tpl > $dockerfile

done
