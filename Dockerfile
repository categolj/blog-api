FROM ubuntu:22.04 as builder
WORKDIR application
ENV JAVA_HOME /jdk-crac
ENV PATH $PATH:$JAVA_HOME/bin

ADD https://cdn.azul.com/zulu/bin/zulu21.30.19-ca-crac-jdk21.0.1-linux_x64.tar.gz $JAVA_HOME/openjdk.tar.gz
RUN tar --extract --file $JAVA_HOME/openjdk.tar.gz --directory "$JAVA_HOME" --strip-components 1; rm $JAVA_HOME/openjdk.tar.gz;

ADD ./.mvn .mvn/
ADD ./mvnw mvnw
ADD ./pom.xml pom.xml
ADD ./src src/
ADD ./.git .git/
RUN ./mvnw -V clean package -DskipTests --no-transfer-progress && \
    cp target/*.jar application.jar && \
    java -Djarmode=layertools -jar application.jar extract
ADD https://repo1.maven.org/maven2/org/springframework/cloud/spring-cloud-bindings/2.0.2/spring-cloud-bindings-2.0.2.jar dependencies/BOOT-INF/lib 
RUN rm -rf $JAVA_HOME/demo $JAVA_HOME/man

FROM ubuntu:22.04
WORKDIR application
ENV JAVA_HOME /jdk-crac
ENV PATH $PATH:$JAVA_HOME/bin

COPY --from=builder jdk-crac /jdk-crac
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
COPY entrypoint.sh ./

ENTRYPOINT ["/application/entrypoint.sh"]