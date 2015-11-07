#!/usr/bin/make

ONT_DIR        = org.mmisw.ont
ONT_DEFS_JAR   = org.mmisw.ont.defs.jar
ONT_CLIENT_JAR = org.mmisw.ont.client.jar
ONT_WAR        = ${ONT_DIR}/_generated/ont.war

ORR_DIR        = org.mmisw.orrportal
ORR_LIB_DIR    = ${ORR_DIR}/base_war/WEB-INF/lib
ORR_WAR        = ${ORR_DIR}/_generated/orr.war

TOMCAT         = ~/Software/apache-tomcat-7.0.57

###################################################

all: ont ont-defs ont-client orr

ont: ${ONT_WAR}

ont-defs: ${ONT_DIR}/${ONT_DEFS_JAR}

ont-client: ${ONT_DIR}/${ONT_CLIENT_JAR}

orr: ${ORR_WAR}

${ONT_WAR}:
	@echo "__________ ${ONT_DIR} ___________"
	cd ${ONT_DIR} && mkdir -p _generated && ant clean && ant

${ORR_WAR}: ont copy-ont-defs copy-ont-client
	@echo "__________ ${ORR_DIR} ___________"
	cd ${ORR_DIR} && mkdir -p _generated && ant clean && ant

copy-ont-defs: ont-defs
	cp ${ONT_DIR}/${ONT_DEFS_JAR} ${ORR_LIB_DIR}

${ONT_DIR}/${ONT_DEFS_JAR}:
	@echo "__________ ${ONT_DIR} ___________"
	cd ${ONT_DIR} && mkdir -p _generated &&  ant defs-lib

copy-ont-client: ont-client
	cp ${ONT_DIR}/${ONT_CLIENT_JAR} ${ORR_LIB_DIR}

${ONT_DIR}/${ONT_CLIENT_JAR}:
	@echo "__________ ${ONT_DIR} ___________"
	cd ${ONT_DIR} && mkdir -p _generated &&  ant client-lib

#######################################################

deploy-ont:
	cp ${ONT_WAR} ${TOMCAT}/webapps/

deploy-orr:
	cp ${ORR_WAR} ${TOMCAT}/webapps/

deploy-all: deploy-ont deploy-orr

#######################################################

clean-ont:
	rm -f ${ONT_WAR}
	rm -f ${ONT_DIR}/${ONT_DEFS_JAR}
	rm -f ${ONT_DIR}/${ONT_CLIENT_JAR}

clean-orr:
	rm -f ${ORR_WAR}
	rm -f ${ORR_LIB_DIR}/${ONT_DEFS_JAR}
	rm -f ${ORR_LIB_DIR}/${ONT_CLIENT_JAR}

clean-ontdefs:

clean-ontclient:

clean-all: clean-ont clean-orr
