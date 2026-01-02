plugins {
    idea
    alias(libs.plugins.kotlin)
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

idea {
    module {
        excludeDirs.add(file(".server"))
        excludeDirs.addAll(allprojects.map { it.layout.buildDirectory.get().asFile })
        excludeDirs.addAll(allprojects.map { it.file(".gradle") })
    }
}
