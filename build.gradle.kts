plugins {
	java
	id("org.springframework.boot") version "3.4.6"
	id("io.spring.dependency-management") version "1.1.7"
	war
	id("com.github.ben-manes.versions") version "0.52.0"
}

group = "eckofox"
version = "0.0.1-SNAPSHOT"

extra["spring-security.version"] = "6.4.10"
extra["jackson.version"] = "2.18.6"
extra["tomcat.version"] = "10.1.55"
extra["logback.version"] = "1.5.25"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(23)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}



repositories {
	mavenCentral()

	maven {
		url = uri("https://releases.aspose.com/java/repo/")
	}
}

dependencies {

	// Spring Boot
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework:spring-test")

	// Security / Auth
	implementation("de.mkammerer:argon2-jvm:2.12")
	implementation("com.auth0:java-jwt:4.4.0")


	// Rate limiting
	implementation("com.bucket4j:bucket4j-core:8.10.1")

	// Database
	runtimeOnly("org.postgresql:postgresql:42.7.11")


	// Apache Commons
	implementation("org.apache.commons:commons-lang3:3.18.0")

	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// File validation / processing
	implementation("org.apache.commons:commons-imaging:1.0.0-alpha6")

	implementation("com.itextpdf:itextpdf:5.5.13.5")

	implementation("com.aspose:aspose-words:22.11:jdk17")
	implementation("com.aspose:aspose-cells:26.5")
	implementation("com.aspose:aspose-slides:26.5:jdk16")

	// BouncyCastle
	implementation("org.bouncycastle:bcprov-jdk18on:1.84")
	implementation("org.bouncycastle:bcpkix-jdk18on:1.84")

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("com.drewnoakes:metadata-extractor:2.20.0")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

dependencyLocking {
     lockAllConfigurations()
}
