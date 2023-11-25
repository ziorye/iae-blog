FROM openjdk:17
EXPOSE 8080
COPY target/iae-blog-0.0.1-SNAPSHOT.jar /app/iae-blog.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "iae-blog.jar"]