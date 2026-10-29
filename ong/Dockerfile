# Imagen del backend para Render (o cualquier servicio que ejecute Docker).
# Funciona tanto si el contexto de construcción es la carpeta "ong" como si es la raíz del repositorio.
FROM maven:3.9-eclipse-temurin-17 AS build
COPY . /contexto
RUN set -e; \
    cd /contexto; \
    if [ ! -f pom.xml ]; then cd ong; fi; \
    mvn -q -B package -DskipTests; \
    mkdir /salida; \
    cp "$(ls target/*.jar | grep -v original | head -1)" /salida/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /salida/app.jar app.jar
# El plan gratuito de Render tiene 512 MB de memoria
ENV JAVA_TOOL_OPTIONS="-Xmx350m"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
