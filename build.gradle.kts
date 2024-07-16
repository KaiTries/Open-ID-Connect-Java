plugins {
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // json stuff
    implementation(libs.jackson)

    // rdf stuff
    implementation(libs.rdf4j)

    // test stuff
    testImplementation(platform(libs.junit.platform))
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}