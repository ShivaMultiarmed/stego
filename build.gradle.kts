plugins {
    kotlin("jvm") version "2.0.0"
    id ("org.openjfx.javafxplugin") version "0.1.0"
}

group = "mikhail.shell.web.application"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.openjfx:javafx:17")
}

javafx {
    version = "21.0.1"
    modules = listOf("javafx.controls")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}