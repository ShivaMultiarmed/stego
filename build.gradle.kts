import org.gradle.api.file.DuplicatesStrategy.EXCLUDE

plugins {
    kotlin("jvm") version "1.9.0" // Use the latest stable Kotlin version
    id("org.openjfx.javafxplugin") version "0.1.0" // Only if using JavaFX
    java
    id("org.jetbrains.compose") version "1.5.0" // Use the latest Compose Multiplatform version
}

group = "mikhail.shell.stego"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("org.openjfx:javafx:17") // Only if using JavaFX
    implementation("org.apache.tika:tika-core:2.9.1")
    implementation(compose.desktop.currentOs) // Required for Compose Multiplatform
}

javafx {
    version = "17.0.1"
    modules = listOf("javafx.controls") // Only if using JavaFX
}

compose.desktop {
    application {
        mainClass = "mikhail.shell.stego.task3.Task3Kt" // Replace with your main class

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "FirstKMProject"
            packageVersion = "1.0.0"
        }
    }
}

tasks.create<Jar>("stego1") {
    archiveFileName = "stego-1.jar"
    manifest {
        attributes(
            "Main-Class" to "mikhail.shell.stego.task1.Task1Kt"
        )
    }
    from(sourceSets.main.get().output) {
        include("mikhail/shell/stego/task1/**")
    }
    dependsOn(configurations.runtimeClasspath)
    duplicatesStrategy = EXCLUDE
    from({
        configurations.runtimeClasspath.get().filter { it.exists() }.map { zipTree(it) }
    })
}

tasks.create<Jar>("stego2") {
    archiveFileName = "stego-2.jar"
    manifest {
        attributes(
            "Main-Class" to "mikhail.shell.stego.task2.Task2Kt"
        )
    }
    from(sourceSets.main.get().output) {
        include("mikhail/shell/stego/task2/**")
    }
    dependsOn(configurations.runtimeClasspath)
    duplicatesStrategy = EXCLUDE
    from({
        configurations.runtimeClasspath.get().filter { it.exists() }.map { zipTree(it) }
    })
}

tasks.create<Jar>("stego3") {
    archiveFileName = "stego-3.jar"
    manifest {
        attributes(
            "Main-Class" to "mikhail.shell.stego.task3.Task3Kt"
        )
    }
    from(sourceSets.main.get().output) {
        include("mikhail/shell/stego/task3/**")
        include("mikhail/shell/stego/task5/**")
    }
    dependsOn(configurations.runtimeClasspath)
    duplicatesStrategy = EXCLUDE
    from(
        {
            configurations.runtimeClasspath.get().filter { it.exists() }.map { zipTree(it) }
        }
    )
}

tasks.create<Jar>("stego4") {
    archiveFileName = "stego-4.jar"
    manifest {
        attributes(
            "Main-Class" to "mikhail.shell.stego.task4.Task4Kt"
        )
    }
    from(sourceSets.main.get().output) {
        include("mikhail/shell/stego/task4/**")
    }
    dependsOn(configurations.runtimeClasspath)
    duplicatesStrategy = EXCLUDE
    from(
        {
            configurations.runtimeClasspath.get().filter { it.exists() }.map { zipTree(it) }
        }
    )
}

tasks.create<Jar>("stego5") {
    archiveFileName = "stego-5.jar"
    manifest {
        attributes(
            "Main-Class" to "mikhail.shell.stego.task5.MainKt"
        )
    }
    from(sourceSets.main.get().output) {
        include("mikhail/shell/stego/task4/**")
        include("mikhail/shell/stego/task5/**")
    }
    dependsOn(configurations.runtimeClasspath)
    duplicatesStrategy = EXCLUDE
    from(
        {
            configurations.runtimeClasspath.get().filter { it.exists() }.map { zipTree(it) }
        }
    )
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}