plugins {
    kotlin("jvm") version "2.2.20"
}

group = "com.acmtd"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.madgag:animated-gif-lib:1.4")
    implementation("tools.aqua:z3-turnkey:4.12.2.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}