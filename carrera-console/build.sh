#!/bin/bash

BASEDIR=$(dirname "$0")
cd ${BASEDIR}
WKDIR=`pwd`

# cd ${BASEDIR}/carrera-console-fe
# sh build.sh
# cd ..
# rm -rf carrera-console/src/main/webapp/build carrera-console/src/main/webapp/static carrera-console/src/main/webapp/index.html
# cp -R carrera-console-fe/dist/* carrera-console/src/main/webapp


cd ${BASEDIR}/carrera-console

mvn clean package -Pdev -Dmaven.test.skip=true

cd ..
OUTPATH=${WKDIR}/output
mkdir -p ${OUTPATH}
cp control.sh ${OUTPATH}/
cp carrera-console/target/carrera.war ${OUTPATH}/
