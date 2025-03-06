import org.gradle.api.file.DuplicatesStrategy.EXCLUDE

plugins {
    kotlin("jvm") version "2.0.0"
    id ("org.openjfx.javafxplugin") version "0.1.0"
    java
}

group = "mikhail.shell.stego.task1"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.openjfx:javafx:17")
}

javafx {
    version = "17.0.1"
    modules = listOf("javafx.controls")
}

//tasks.jar {
//    archiveFileName = "stego-1-$version.jar"
//    manifest {
//        attributes(
//            "Main-Class" to "mikhail.shell.stego.task1.MainKt"
//        )
//    }
//    from (sourceSets.main.get().output)
//    dependsOn(configurations.runtimeClasspath)
//    duplicatesStrategy = EXCLUDE
//    from ({
//        configurations.runtimeClasspath.get().filter { it.exists() }.map { zipTree(it) }
//    })
//}

tasks.create<Jar>("stego1"){
    archiveFileName = "stego-1.jar"
    manifest {
        attributes(
            "Main-Class" to "mikhail.shell.stego.task1.Task1Kt"
        )
    }
    from (sourceSets.main.get().output) {
        include("mikhail/shell/stego/task1/**")
    }
    dependsOn(configurations.runtimeClasspath)
    duplicatesStrategy = EXCLUDE
    from ({
        configurations.runtimeClasspath.get().filter { it.exists() }.map { zipTree(it) }
    })
}
tasks.create<Jar>("stego2"){
    archiveFileName = "stego-2.jar"
    manifest {
        attributes(
            "Main-Class" to "mikhail.shell.stego.task2.Task2Kt"
        )
    }
    from (sourceSets.main.get().output) {
        include("mikhail/shell/stego/task2/**")
    }
    dependsOn(configurations.runtimeClasspath)
    duplicatesStrategy = EXCLUDE
    from ({
        configurations.runtimeClasspath.get().filter { it.exists() }.map { zipTree(it) }
    })
}
tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}