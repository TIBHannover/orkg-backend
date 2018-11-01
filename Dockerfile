FROM azul/zulu-openjdk-alpine
LABEL maintainer="Manuel Prinz <manuel.prinz@tib.eu>"

ARG PROJECT_NAME
ARG VERSION

ADD "${PROJECT_NAME}-${VERSION}.war" /app/application.war

# Re-map the port to 8000, as both Blazegraph and Spring
# use port 8080 by default and clash when starting.
EXPOSE 8000

CMD ["java", "-jar", "/app/application.war", "--server.port=8000"]
