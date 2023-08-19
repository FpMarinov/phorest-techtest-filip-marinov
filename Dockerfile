FROM amazoncorretto:17
COPY target/backend-tech-test-1.0.0.jar backend-tech-test-1.0.0.jar
ENTRYPOINT ["java", "-jar", "/backend-tech-test-1.0.0.jar"]
