#!/bin/bash
set -e
set -o pipefail

archs='aarch64 amd64 armv7hf'
suite='jessie'
for arch in $archs; do
	baseImage='resin/'$arch'-debian'

	dockerfile=$arch.Dockerfile
	sed -e s~#{FROM}~$baseImage:$suite~g \
		Dockerfile.tpl > $dockerfile
done
