FROM neo4j:3.5

ENV LANG C.UTF-8

RUN \
    apt-get update && \
    apt-get install --assume-yes \
        curl \
    && \
    rm -rf /var/lib/apt/lists/*

# Check https://github.com/neo4j-labs/neosemantics/releases
ENV SEMANTICS_EXT_VERSION=3.5.0.4
# Check https://github.com/neo4j-contrib/neo4j-apoc-procedures#version-compatibility-matrix
ENV APOC_EXT_VERSION=3.5.0.9

# Activate semantics plug-in
ENV NEO4J_dbms_unmanagedExtensionClasses="semantics.extension=/rdf"

RUN \
    cd plugins && \
    curl --fail --show-error --location --remote-name \
      https://github.com/jbarrasa/neosemantics/releases/download/${SEMANTICS_EXT_VERSION}/neosemantics-${SEMANTICS_EXT_VERSION}.jar && \
    curl --fail --show-error --location --remote-name \
      https://github.com/neo4j-contrib/neo4j-apoc-procedures/releases/download/${APOC_EXT_VERSION}/apoc-${APOC_EXT_VERSION}-all.jar \
