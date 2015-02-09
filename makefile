#!/usr/bin/make

ONTDIR=org.mmisw.ont
ONTJAR=${ONTDIR}/org.mmisw.ont.client.jar
ONTWAR=${ONTDIR}/_generated/ont.war

CLNDIR=org.mmisw.orrclient
CLNLIB=${CLNDIR}/base_war/WEB-INF/lib
CLNJAR=${CLNDIR}/_generated/org.mmisw.orrclient.jar

PTLDIR=org.mmisw.orrportal
PTLLIB=${PTLDIR}/base_war/WEB-INF/lib
PTLWAR=${PTLDIR}/_generated/orr.war

TOMCAT=~/Software/apache-tomcat-7.0.57


clean:
	rm -f out/ontclient-built
	
ont: ${ONTJAR}
	
${ONTJAR}: out/ontclient-built

out/ontclient-built:
	mkdir -p out
	@echo "__________ ${ONTDIR} ___________" 
	cd ${ONTDIR} && ant clean && ant && ant client-lib
	touch out/ontclient-built

	
orrclient: ${CLNJAR}

${CLNJAR}: ${ONTJAR}
	@echo "__________ ${CLNDIR} ___________"
	cp ${ONTJAR} ${CLNLIB}
	cd ${CLNDIR} && ant clean && ant jar-no-tests
	touch out/orrclient-lib-built
	
	
orrportal: ${PTLWAR}

${PTLWAR}: ${ONTJAR} ${CLNJAR}
	@echo "__________ ${PTLDIR} ___________"
	cp ${ONTJAR} ${PTLLIB}	
	cp ${CLNJAR} ${PTLLIB}
	cd ${PTLDIR} && ant clean && ant
	touch orr-war-built
	
	
deploy-all: deploy-ont deploy-orr

deploy-ont:
	cp ${ONTWAR} ${TOMCAT}/webapps/
	
deploy-orr:
	cp ${PTLWAR} ${TOMCAT}/webapps/

