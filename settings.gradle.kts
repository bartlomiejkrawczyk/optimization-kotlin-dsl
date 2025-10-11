rootProject.name = "optimization-kotlin-dsl"


pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/bartlomiejkrawczyk/optimization-kotlin-dsl")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: settings.extra.properties["GITHUB_ACTOR"].toString()
                password = System.getenv("GITHUB_TOKEN") ?: settings.extra.properties["GITHUB_TOKEN"].toString()
            }
        }
    }
}

buildscript {
    repositories.addAll(pluginManagement.repositories)
}
