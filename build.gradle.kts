plugins {
    application
    java
    alias(libs.plugins.shadowJar)
    checkstyle
    alias(libs.plugins.spotbugs)
    jacoco
    id("jacoco-report-aggregation")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation(libs.rdf4j.model)
    implementation(libs.rdf4j.rio.turtle)
    implementation(libs.rdf4j.rio.jsonld)

    implementation(libs.gson)

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}