FROM eclipse-temurin:22-jdk-jammy AS build

WORKDIR /build

COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/

RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline -DskipTests

COPY ./src src/
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests && \
    mv target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)-$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout).jar target/app.jar

RUN java -Djarmode=layertools -jar target/app.jar extract --destination target/extracted

FROM eclipse-temurin:22-jre-jammy AS final

COPY --from=build build/target/extracted/dependencies/ ./
COPY --from=build build/target/extracted/spring-boot-loader/ ./
COPY --from=build build/target/extracted/snapshot-dependencies/ ./
COPY --from=build build/target/extracted/application/ ./

EXPOSE 8080

CMD [ "java", "--enable-preview", "org.springframework.boot.loader.launch.JarLauncher" ]

LABEL name="nexus_backend"