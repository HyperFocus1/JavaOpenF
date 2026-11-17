# syntax=docker/dockerfile:1
FROM maven:3.9.16-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B package

FROM quay.io/wildfly/wildfly:42.0.0.Final-jdk21
WORKDIR /opt/jboss
COPY --chown=jboss:root --from=build /workspace/target/drivers/mysql-connector-j.jar /opt/jboss/wildfly/modules/com/mysql/main/mysql-connector-j.jar
COPY --chown=jboss:root server/mysql-module.xml /opt/jboss/wildfly/modules/com/mysql/main/module.xml
COPY --chown=jboss:root --from=build /workspace/target/jackson/jackson-core.jar /opt/jboss/wildfly/modules/com/fasterxml/jackson/core/jackson-core/main/jackson-core.jar
COPY --chown=jboss:root server/jackson-core-module.xml /opt/jboss/wildfly/modules/com/fasterxml/jackson/core/jackson-core/main/module.xml
COPY --chown=jboss:root --from=build /workspace/target/jackson/jackson-databind.jar /opt/jboss/wildfly/modules/com/fasterxml/jackson/core/jackson-databind/main/jackson-databind.jar
COPY --chown=jboss:root server/jackson-databind-module.xml /opt/jboss/wildfly/modules/com/fasterxml/jackson/core/jackson-databind/main/module.xml
COPY --chown=jboss:root server/configure.cli /opt/jboss/configure.cli
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/configure.cli \
    && mkdir -p /opt/jboss/wildfly/standalone/data/openf-secrets
COPY --chown=jboss:root --from=build /workspace/target/open_f.war /opt/jboss/wildfly/standalone/deployments/open_f.war
COPY --chown=jboss:root --chmod=755 server/docker-entrypoint.sh /opt/jboss/docker-entrypoint.sh
ENTRYPOINT ["/opt/jboss/docker-entrypoint.sh"]
CMD ["-c", "standalone-microprofile.xml", "-b", "0.0.0.0"]
