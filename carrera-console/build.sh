#!/bin/bash

BASEDIR=$(dirname "$0")
cd ${BASEDIR}
WKDIR=`pwd`

cd ${BASEDIR}/carrera-console

mvn clean package -Pdev -Dmaven.test.skip=true

cd ..
OUTPATH=${WKDIR}/output
mkdir -p ${OUTPATH}
rm -rf ${OUTPATH}/carrera.war
cp control.sh ${OUTPATH}/
cp carrera-console/target/carrera.war ${OUTPATH}/
