FROM openjdk:17.0.2-jdk

WORKDIR /app
ARG JAR_FILE
ADD target/${JAR_FILE} /app/pet_money_tool.jar
EXPOSE 443
ENV JAVA_OPTIONS "-Xms1g -Xmx2g -Dfile.encoding=UTF-8 -Dspring.profiles.active=docker -Djava.awt.headless=true -Djava.awt.graphicsenv=sun.awt.CGraphicsEnvironment "
ENV OVERRIDE_PROP ""

ENTRYPOINT ["java", "-jar", "/app/pet_money_tool.jar"]