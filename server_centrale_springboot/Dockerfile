# Usa un'immagine base di OpenJDK
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copia il file JAR generato nella cartella di lavoro
COPY servercentrale-0.0.1-SNAPSHOT.jar app.jar  

# Espone la porta su cui gira l'applicazione
EXPOSE 8080

# Comando per avviare l'applicazione
CMD ["java", "-jar", "app.jar"]
