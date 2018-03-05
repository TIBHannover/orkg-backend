FROM lyrasis/blazegraph:2.1.4

USER root

RUN apk update && apk add bash

RUN \
  mkdir /tmp/bigdata && \
  cp /var/lib/jetty/webapps/bigdata.war /tmp/bigdata && \
  ( \
    cd /tmp/bigdata/ && \
    unzip bigdata.war && rm bigdata.war && \
    sed -i 's,^</web-app>,,' WEB-INF/web.xml && \
    echo "\
  <filter>\
    <filter-name>cross-origin</filter-name>\
    <filter-class>org.eclipse.jetty.servlets.CrossOriginFilter</filter-class>\
    <init-param>\
      <param-name>allowedOrigins</param-name>\
      <param-value></param-value>\
    </init-param>\
    <init-param>\
      <param-name>allowedMethods</param-name>\
      <param-value>GET,POST,OPTIONS,DELETE,PUT,HEAD</param-value>\
    </init-param>\
    <init-param>\
      <param-name>allowedHeaders</param-name>\
      <param-value>origin, content-type, accept, authorization</param-value>\
  </init-param>\
  </filter>\
  <filter-mapping>\
    <filter-name>cross-origin</filter-name>\
    <url-pattern>/*</url-pattern>\
  </filter-mapping>\
  " >> WEB-INF/web.xml && \
    echo '</web-app>' >> WEB-INF/web.xml && \
    apk add curl && \
    ( \
      cd WEB-INF/lib && \
      curl --fail --show-error --location --remote-name \
        http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-servlets/9.2.3.v20140905/jetty-servlets-9.2.3.v20140905.jar \
    ) && \
    apk del curl && \
    apk add zip && \
    zip -r bigdata.war * && \
    apk del zip && \
    cp bigdata.war /var/lib/jetty/webapps \
  ) && \
  rm -rf /tmp/bigdata

# Listen on all adresses, so that containers can be linked /
# found on the same network.
ENV JETTY_HOST '0.0.0.0'

USER jetty
