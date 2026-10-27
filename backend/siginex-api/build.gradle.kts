plugins {
    java
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.siginex"
version = "0.1.0-SNAPSHOT"

description = "SIGINEX backend skeleton"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-security")
    // Required by Argon2PasswordEncoder (argon2id) for password hashing.
    implementation("org.bouncycastle:bcprov-jdk18on:1.81")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // Lombok: used by all new code (getters, builders, @RequiredArgsConstructor
    // for constructor injection, @Slf4j, ...). Version managed by the Spring
    // Boot 4.0 BOM; compatible with Java 25.
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Spring Boot 4.0 split the MVC test slice into its own starter; it brings
    // @WebMvcTest / @AutoConfigureMockMvc (package
    // org.springframework.boot.webmvc.test.autoconfigure) for the controller
    // web-slice tests. Version managed by the Spring Boot 4.0 BOM.
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    // MockMvc security slice helpers (authentication()/csrf() request
    // post-processors) for the controller web-slice tests. Version managed by
    // the Spring Boot 4.0 BOM.
    testImplementation("org.springframework.security:spring-security-test")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    // Real-PostgreSQL integration tests (RLS tenant isolation). Versions are
    // pinned by the Spring Boot 4.0.0 BOM (Testcontainers 2.0.x), which renamed
    // the module artifacts with a `testcontainers-` prefix.
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

springBoot {
    mainClass.set("com.siginex.api.SiginexApiApplication")
}
