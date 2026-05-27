plugins {
	java
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
	war
	id("com.bmuschko.tomcat") version "2.7.0"
}

group = "eckofox"
version = "0.0.1-SNAPSHOT"

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
		url = uri("https://repository.aspose.com/repo/")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.bucket4j:bucket4j-core:8.10.1")
	implementation("de.mkammerer:argon2-jvm:2.12")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("com.auth0:java-jwt:4.4.0")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// file validation
	implementation("org.apache.commons:commons-imaging:1.0.0-alpha6")
	implementation("com.itextpdf:itextpdf:5.5.13.5")
	implementation("aspose.com:aspose-cells:8.7.0")
	implementation("aspose.com:aspose-words:16.1.0")
	implementation("aspose.com:aspose-slides:18.4.0")
	testImplementation("com.drewnoakes:metadata-extractor:2.20.0")
}

tasks.withType<Test> {
	useJUnitPlatform()
}