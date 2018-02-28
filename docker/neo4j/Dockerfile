FROM neo4j:3.3

ENV SEMANTICS_EXT_VERSION=3.3.0.2

RUN \
  apk update && apk add curl && \
  ( \
    cd plugins && \
    curl --fail --show-error --location --remote-name \
      https://github.com/jbarrasa/neosemantics/releases/download/${SEMANTICS_EXT_VERSION}/neosemantics-${SEMANTICS_EXT_VERSION}.jar \
  ) && \
  apk del curl
