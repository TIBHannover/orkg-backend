FROM azul/zulu-openjdk-alpine:11-jre
LABEL maintainer="Manuel Prinz <manuel.prinz@tib.eu>"

EXPOSE 8080

COPY build/libs/orkg-*.jar /app/application.jar

CMD ["java", "-jar", "/app/application.jar"]
