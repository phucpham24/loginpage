plugins {
	java
	id("org.springframework.boot") version "3.4.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "vn.login"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	// implementation("org.springframework.boot:spring-boot-starter-web")
	implementation ("org.springframework.boot:spring-boot-starter-webflux")
	// implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
	implementation ("org.springframework.boot:spring-boot-starter-data-r2dbc") // Reactive MySQL DB support
    // implementation ("dev.miku:r2dbc-mysql:0.8.2.RELEASE") // R2DBC MySQL driver
	implementation ("io.asyncer:r2dbc-mysql:1.0.2") // or latest

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	// runtimeOnly ("io.r2dbc:r2dbc-mysql") // MySQL R2DBC driver
	// runtimeOnly("mysql:mysql-connector-java")
	// runtimeOnly("com.mysql:mysql-connector-j")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
