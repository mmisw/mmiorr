watchdog
Luis Bermudez - MMI/SURA
Carlos Rueda - MMI/MBARI

Running the programs (using maven): 

# CF conversion:  (See SKOSCFCreator.java and pom.xml)
$ mvn exec:java 
This reads in src/main/resources/input/cf-standard-name-table.xml
and generates src/main/resources/output/cf.owl

Note: Only CF conversion is included. Other conversions to be included as
we work on them. 


TODOs: 
- make the programs more flexible in terms of parameters 
- automate the overall process (download inputs -> convert -> register at ORR)