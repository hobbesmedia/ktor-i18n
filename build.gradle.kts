import groovy.lang.GroovyObject
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask

plugins {
    kotlin("jvm") version "1.3.70"
    maven
    `maven-publish`
    id("org.jetbrains.dokka") version "0.10.1"
    id("com.jfrog.artifactory") version "4.17.2"
}

val semVer: String by extra(project.findProperty("semVer") as? String ?: "1.1.0")

group = "io.github.atistrcsn"
version = semVer

println("Project version: $version")

repositories {
    jcenter()
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/ktor")
    maven("https://dl.bintray.com/kotlin/dokka")
    maven {
        setUrl("${System.getenv("ARTIFACTORY_CONTEXT_URL")}/maven-dev")
        credentials {
            username = System.getenv("ARTIFACTORY_USER")
            password = System.getenv("ARTIFACTORY_PASSWORD")
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
            artifact("$buildDir/libs/${project.name}.jar")
        }
    }
}

artifactory {
    setContextUrl("${System.getenv("ARTIFACTORY_CONTEXT_URL")}")
    publish(delegateClosureOf<PublisherConfig> {
        setPublishPom(true)
        repository(delegateClosureOf<GroovyObject> {
            setProperty("repoKey", "maven-dev")
            setProperty("username", "${System.getenv("ARTIFACTORY_USER")}")
            setProperty("password", "${System.getenv("ARTIFACTORY_PASSWORD")}")
        })
        defaults(delegateClosureOf<ArtifactoryTask> {
            publications("mavenJava")
            setPublishPom(true)
            setPublishArtifacts(true)
        })
    })
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("io.ktor:ktor-server-core:1.3.1")
    testImplementation("io.ktor:ktor-server-test-host:1.3.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    getting(DokkaTask::class) {
        outputFormat = "html"
        outputDirectory = "$buildDir/docs"
    }
}

