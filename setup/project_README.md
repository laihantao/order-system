# Not longer in use due to already dockerized

# Setup

## 1. Create SpringBoot project

    1.1 Go to Spring Initializr - https://start.spring.io/

    1.2 Setting:

    Project: Maven
    Language: Java
    Spring Boot: Latest stable
    Group: com.example
    Artifact: order-system
    Packaging: Jar
    Java: 17

    1.3 Dependencies:

    Spring Web
    Spring Data JPA
    Lombok
    PostgreSQL

    1.4 Generate and download as ZIP.

    1.5 Extract the file and open with VS Code.

## 2. Install Java

    2.1 Java Version
    - Java 17 - https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
    
    2.2 Check Java version
    - java -version 

    2.3 Set the JAVA_HOME to your java-17 directory if you didn't set it yet.

## 3. VS Code Extension
1. Extension Pack for Java
2. Spring Boot Extension Pack
3. Maven for Java

## Some commands:

### Clean install 
./mvnw clean install -DskipTests

### Clean run
./mvnw clean spring-boot:run

### Clean package without test
./mvnw clean package -DskipTests



# Monitoring

## Prometheus
1. Create prometheus.yml
```
global:
  scrape_interval: 5s

scrape_configs:
  - job_name: "java-order-system"
    metrics_path: "/actuator/prometheus"
    static_configs:
      - targets: ["app:8080"] # In docker container level, point to app:8080. # becaue if localhost:8080 here means within prometheus container, so it is different.
```
2. application.properties
```
management.endpoints.web.exposure.include=*
management.endpoint.prometheus.enabled=true
management.endpoints.web.base-path=/actuator
```
3. pom.xml
```
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

4. Access to localhost:9090
5. Create datasource

## Grafana