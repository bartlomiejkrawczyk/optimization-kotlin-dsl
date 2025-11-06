import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.time.Duration

plugins {
    kotlin("jvm") version "2.2.20"

    id("signing")
    id("maven-publish")
    id("com.coditory.manifest") version "1.1.0"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.18.1"
}

group = "io.github.bartlomiejkrawczyk"
description = "A concise Kotlin DSL wrapper for Google's OR-Tools Linear and Mixed Integer Programming Solver."

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
        apiVersion.set(KotlinVersion.KOTLIN_2_1)
        languageVersion.set(KotlinVersion.KOTLIN_2_1)
    }
    explicitApi()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/bartlomiejkrawczyk/optimization-kotlin-dsl")
        credentials {
            username = System.getenv("GITHUB_ACTOR") ?: project.extra.properties["GITHUB_ACTOR"].toString()
            password = System.getenv("GITHUB_TOKEN") ?: project.extra.properties["GITHUB_TOKEN"].toString()
        }
    }
}

tasks.withType<Test> {
    testLogging {
        events("passed", "failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
    useJUnitPlatform()
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    api("com.google.ortools:ortools-java:9.14.6206")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

manifest {
    buildAttributes = true
    implementationAttributes = true
    scmAttributes = true
}

publishing {
    publications {
        create<MavenPublication>("mavenCentral") {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://github.com/bartlomiejkrawczyk/optimization-kotlin-dsl")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("bartlomiejkrawczyk")
                        name.set("Bartłomiej Krawczyk")
                        url.set("https://github.com/bartlomiejkrawczyk")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/bartlomiejkrawczyk/optimization-kotlin-dsl.git")
                    developerConnection.set("scm:git:ssh://github.com/bartlomiejkrawczyk/optimization-kotlin-dsl.git")
                    url.set("https://github.com/bartlomiejkrawczyk/optimization-kotlin-dsl")
                }
            }
            suppressPomMetadataWarningsFor("runtimeElements")
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/bartlomiejkrawczyk/optimization-kotlin-dsl")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("GITHUB_ACTOR").toString()
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("GITHUB_TOKEN").toString()
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("GPG_PRIVATE_KEY") ?: project.findProperty("GPG_PRIVATE_KEY").toString(),
        System.getenv("GPG_PASSPHRASE") ?: project.findProperty("GPG_PASSPHRASE").toString(),
    )
    sign(publishing.publications["mavenCentral"])
}

nexusPublishing {
    transitionCheckOptions {
        maxRetries.set(5)
        delayBetween.set(Duration.ofSeconds(5))
    }
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username = System.getenv("OSSRH_USERNAME") ?: project.findProperty("OSSRH_USERNAME").toString()
            password = System.getenv("OSSRH_PASSWORD") ?: project.findProperty("OSSRH_PASSWORD").toString()
        }
    }
}
