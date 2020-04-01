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
ENV SEMANTICS_EXT_SHA1=c541e958e13f8e89070b3650e596dd7c1322cfa1
# Check https://github.com/neo4j-contrib/neo4j-apoc-procedures#version-compatibility-matrix
ENV APOC_EXT_VERSION=3.5.0.9
ENV APOC_EXT_SHA1=8e93f820534f46d83782e1cfcc50626d3dbb3de2

# Activate semantics plug-in
ENV NEO4J_dbms_unmanagedExtensionClasses="semantics.extension=/rdf"

RUN \
    cd plugins && \
    curl --fail --show-error --location --remote-name \
      https://github.com/jbarrasa/neosemantics/releases/download/${SEMANTICS_EXT_VERSION}/neosemantics-${SEMANTICS_EXT_VERSION}.jar && \
      echo "${SEMANTICS_EXT_SHA1}  neosemantics-${SEMANTICS_EXT_VERSION}.jar" | sha1sum --strict --quiet --status --check - && \
    curl --fail --show-error --location --remote-name \
      https://github.com/neo4j-contrib/neo4j-apoc-procedures/releases/download/${APOC_EXT_VERSION}/apoc-${APOC_EXT_VERSION}-all.jar && \
      echo "${APOC_EXT_SHA1}  apoc-${APOC_EXT_VERSION}-all.jar" | sha1sum --strict --quiet --status --check - \
