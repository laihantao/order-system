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
