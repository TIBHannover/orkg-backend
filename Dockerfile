FROM azul/zulu-openjdk-alpine:11-jre
LABEL maintainer="Manuel Prinz <manuel.prinz@tib.eu>"

COPY build/libs/orkg-*.jar /app/application.jar

# Re-map the port to 8000, as both Blazegraph and Spring
# use port 8080 by default and clash when starting.
EXPOSE 8000

CMD ["java", "-jar", "-Dspring.profiles.active=docker", "/app/application.jar", "--server.port=8000"]
