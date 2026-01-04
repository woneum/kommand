plugins {
    idea
    alias(libs.plugins.kotlin)
    alias(libs.plugins.dokka)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = rootProject.libs.plugins.kotlin.get().pluginId)

    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        compileOnly(rootProject.libs.paper)

        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))
    }
}

listOf("api", "core").forEach { projectName ->
    project(":${rootProject.name}-$projectName") {
        apply(plugin = "org.jetbrains.dokka")

        tasks {
            register<Jar>("sourcesJar") {
                archiveClassifier.set("sources")
                from(sourceSets["main"].allSource)
            }

            register<Jar>("dokkaJar") {
                archiveClassifier.set("javadoc")
                dependsOn("dokkaGenerateHtml")

                from(layout.buildDirectory.dir("dokka/html/")) {
                    include("**")
                }
            }
        }
    }
}

idea {
    module {
        excludeDirs.add(file(".server"))
        excludeDirs.addAll(allprojects.map { it.layout.buildDirectory.get().asFile })
        excludeDirs.addAll(allprojects.map { it.file(".gradle") })
    }
}
