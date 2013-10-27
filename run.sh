#!/bin/bash
java -cp "target/*:target/lib/slf4j-api-1.7.5.jar:target/lib/slf4j-simple-1.7.5.jar:target/lib/*" org.jboss.weld.environment.se.StartMain --pressgangServer http://pressgang-pnq.usersys.redhat.com:8080/pressgang-ccms/rest --topicGraphFile topics.lay --topicDatabaseFile topicDatabase.js
#java -cp "target/*:target/lib/slf4j-api-1.7.5.jar:target/lib/slf4j-simple-1.7.5.jar:target/lib/*" org.jboss.weld.environment.se.StartMain --pressgangServer http://topika.ecs.eng.bne.redhat.com:8080/pressgang-ccms/rest --topicGraphFile topics.lay --topicDatabaseFile topicDatabase.js
cp topics.lay /var/www/html/visualizations
cp topicDatabase.js /var/www/html/visualizations/javascript