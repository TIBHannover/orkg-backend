FROM neo4j:3.5

ENV SEMANTICS_EXT_VERSION=3.5.0.2
ENV APOC_EXT_VERSION=3.5.0.4

RUN apt update && \
    apt install -y curl && \
    cd plugins && \
    curl --fail --show-error --location --remote-name \
      https://github.com/jbarrasa/neosemantics/releases/download/${SEMANTICS_EXT_VERSION}/neosemantics-${SEMANTICS_EXT_VERSION}.jar && \
    curl --fail --show-error --location --remote-name \
      https://github.com/neo4j-contrib/neo4j-apoc-procedures/releases/download/${APOC_EXT_VERSION}/apoc-${APOC_EXT_VERSION}-all.jar \
