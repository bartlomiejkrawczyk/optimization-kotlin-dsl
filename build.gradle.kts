plugins {
    kotlin("jvm") version "2.0.20"

    id("maven-publish")
}

group = "io.github.bartlomiejkrawczyk"
description = "optimization-kotlin-dsl"

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
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
                    }
                }
                scm {
                    url.set("https://github.com/bartlomiejkrawczyk/optimization-kotlin-dsl")
                }
            }
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
