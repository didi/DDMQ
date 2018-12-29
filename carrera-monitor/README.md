## DDMQ Monitor ##

Monitor provide important monitor features to DDMQ. 
 

### Features ###

* Broker Monitor: monitor broker and namesvr
* Inspection Monitor: monitor cproxy and pproxy 
* Lag Monitor: monitor consume lag and consume delay time


### Deploy ###
* create inspection topic and cg_inspection group in User Console
* modify monitor.yaml
* run ```build.sh``` to build package
* start monitor with ```control.sh start```