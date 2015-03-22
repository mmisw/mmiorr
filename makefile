#!/usr/bin/make

ONT_DIR        = org.mmisw.ont
ONT_CLIENT_JAR = org.mmisw.ont.client.jar
ONT_WAR        = ${ONT_DIR}/_generated/ont.war

ORR_DIR        = org.mmisw.orrportal
ORR_LIB_DIR    = ${ORR_DIR}/base_war/WEB-INF/lib
ORR_WAR        = ${ORR_DIR}/_generated/orr.war

TOMCAT         = ~/Software/apache-tomcat-7.0.57

###################################################

ont: ${ONT_WAR}

ont-client: ${ONT_DIR}/${ONT_CLIENT_JAR}

orr: ${ORR_WAR}

${ONT_WAR}:
	@echo "__________ ${ONT_DIR} ___________"
	cd ${ONT_DIR} && ant clean && ant

${ORR_WAR}: copy-ont-client ont
	@echo "__________ ${ORR_DIR} ___________"
	cd ${ORR_DIR} && ant clean && ant

copy-ont-client: ont-client
	cp ${ONT_DIR}/${ONT_CLIENT_JAR} ${ORR_LIB_DIR}

${ONT_DIR}/${ONT_CLIENT_JAR}:
	@echo "__________ ${ONT_DIR} ___________"
	cd ${ONT_DIR} && ant client-lib

#######################################################

deploy-ont:
	cp ${ONT_WAR} ${TOMCAT}/webapps/

deploy-orr:
	cp ${ORR_WAR} ${TOMCAT}/webapps/

deploy-all: deploy-ont deploy-orr

#######################################################

clean-ont:
	rm -f ${ONT_WAR}

clean-orr:
	rm -f ${ORR_WAR}

clean-ontclient:
	rm -f ${ONT_DIR}/${ONT_CLIENT_JAR}
	rm -f ${ORR_LIB_DIR}/${ONT_CLIENT_JAR}

clean-all: clean-ont clean-ontclient clean-orr
