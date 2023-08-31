plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.allopen") version "1.9.10"
    id("io.quarkus")
    id("com.github.ben-manes.versions") version "0.47.0"
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project


dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation(enforcedPlatform("io.quarkiverse.operatorsdk:quarkus-operator-sdk-bom:6.3.0"))
    implementation("io.quarkus:quarkus-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
//    implementation("io.kubernetes:client-java:18.0.1")
//    implementation("io.kubernetes:client-java-extended:18.0.0")
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-container-image-jib")

    implementation("io.quarkiverse.operatorsdk:quarkus-operator-sdk-bundle-generator:6.3.1-SNAPSHOT")
    implementation("io.quarkiverse.operatorsdk:quarkus-operator-sdk:6.3.1-SNAPSHOT")
    implementation("io.quarkiverse.operatorsdk:quarkus-operator-sdk-annotations:6.3.1-SNAPSHOT")

}



group = "com.example"
version = "1.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    kotlinOptions.javaParameters = true
}
