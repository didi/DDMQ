#!/bin/bash

BASEDIR=$(dirname "$0")
cd ${BASEDIR}
WKDIR=`pwd`

cd ${BASEDIR}/carrera-console-fe
sh build.sh
cd ..
rm -rf carrera-console/src/main/webapp/build carrera-console/src/main/webapp/static carrera-console/src/main/webapp/index.html
cp -R carrera-console-fe/dist/* carrera-console/src/main/webapp
