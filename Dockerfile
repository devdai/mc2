FROM tomcat:latest

ENV CATALINA_OPTS="-Xms1024m -Xmx4096m -XX:MetaspaceSize=512m -XX:MaxMetaspaceSize=512m -Xss512k"

COPY target/mc2.war /usr/local/tomcat/webapps/mc2.war

CMD ["catalina.sh", "run"]