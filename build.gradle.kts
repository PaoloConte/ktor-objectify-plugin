import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java-library")
    kotlin("jvm")
    id("com.vanniktech.maven.publish") version "0.28.0"
}

group = "io.paoloconte"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion: String by project
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")

    val objectifyVersion: String by project
    implementation("com.googlecode.objectify:objectify:$objectifyVersion")

    val kotestVersion: String by project
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("org.slf4j:slf4j-simple:+")
}

mavenPublishing {
    configure(JavaLibrary(
        javadocJar = JavadocJar.Empty(),
        sourcesJar = true,
    ))

    coordinates(project.group.toString(), rootProject.name, project.version.toString())

    pom {
        name.set("Ktor Objectify plugin")
        description.set("Library that allows to use Objectify with Ktor in coroutines context.")
        inceptionYear.set("2023")
        url.set("https://github.com/PaoloConte/ktor-objectify-plugin")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://github.com/PaoloConte/ktor-objectify-plugin/blob/main/LICENSE")
                distribution.set("https://github.com/PaoloConte/ktor-objectify-plugin/blob/main/LICENSE")
            }
        }
        developers {
            developer {
                id.set("PaoloConte")
                name.set("Paolo Conte")
                url.set("https://github.com/PaoloConte/")
            }
        }
        scm {
            url.set("https://github.com/PaoloConte/ktor-objectify-plugin")
            connection.set("scm:git:git@github.com:PaoloConte/ktor-objectify-plugin.git")
            developerConnection.set("scm:git:ssh://git@github.com/PaoloConte/ktor-objectify-plugin.git")
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}