FROM azul/zulu-openjdk-alpine
MAINTAINER Manuel Prinz <manuel.prinz@tib.eu>

ARG PROJECT_NAME
ARG VERSION

ADD "${PROJECT_NAME}-${VERSION}.jar" /app/application.jar

# Re-map the port to 8000, as both Blazegraph and Spring
# use port 8080 by default and clash when starting.
EXPOSE 8000

CMD ["java", "-jar", "/app/application.jar", "--server.port=8000"]
