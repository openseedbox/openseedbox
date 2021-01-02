#!/bin/bash
set -e
set -o pipefail

archs='aarch64 amd64 armv7hf'
suite='buster'
buildDeps='build-essential libpcre3-dev libssl-dev zlib1g-dev'
for arch in $archs; do
	baseImage='balenalib/'$arch'-debian'
	case "$arch" in
		aarch64)
			# Not needed on Travis CI (but https://github.com/openseedbox/openseedbox/issues/70 exists)
			#balenaCrossBuildBegin='RUN [ "cross-build-start" ]'
			#balenaCrossBuildEnd='RUN [ "cross-build-end" ]'
			;;
		armv7hf)
			# It's still needed, as armv7hf is still bogous on Travis CI
			balenaCrossBuildBegin='RUN [ "cross-build-start" ]'
			balenaCrossBuildEnd='RUN [ "cross-build-end" ]'
			;;
		*)
			balenaCrossBuildBegin=''
			balenaCrossBuildEnd=''
	esac;

	dockerfile=$arch.Dockerfile
	sed -e s~#{FROM}~$baseImage:$suite~g \
		-e s~#{BUILD_DEPS}~"$buildDeps"~g \
		-e s~#{BALENA_CROSSBUILD_BEGIN}~"$balenaCrossBuildBegin"~g \
		-e s~#{BALENA_CROSSBUILD_END}~"$balenaCrossBuildEnd"~g \
		-e '/./,/^$/!d' \
		Dockerfile.tpl > $dockerfile

done
