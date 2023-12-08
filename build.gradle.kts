
plugins {
    `java-library`
    kotlin("jvm")
    id("maven-publish")
}

group = "io.paoloconte"
version = "1.0"

repositories {
    mavenCentral()
}

val ktorVersion: String by project
val kotestVersion: String by project
val objectifyVersion: String by project

dependencies {

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("com.googlecode.objectify:objectify:$objectifyVersion")

    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("org.slf4j:slf4j-simple:+")

}


publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
        }
    }
    repositories {
        mavenLocal()
    }
}

java {
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}