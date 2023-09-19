plugins {
    id("fabric-loom")
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm").version(kotlinVersion)
    //id("com.github.johnrengelman.shadow") version "7.1.2"
}
base {
    val archivesBaseName: String by project
    archivesName.set(archivesBaseName)
}

val minecraftVersion: String by project
val modVersion: String by project
version = "$modVersion+$minecraftVersion"
val mavenGroup: String by project
group = mavenGroup
repositories {}
dependencies {
    minecraft("com.mojang", "minecraft", minecraftVersion)
    val yarnMappings: String by project
    mappings("net.fabricmc", "yarn", yarnMappings, null, "v2")
    val loaderVersion: String by project
    modImplementation("net.fabricmc", "fabric-loader", loaderVersion)
    val fabricVersion: String by project
    modImplementation("net.fabricmc.fabric-api", "fabric-api", fabricVersion)
    val fabricKotlinVersion: String by project
    modImplementation("net.fabricmc", "fabric-language-kotlin", fabricKotlinVersion)
}

tasks {
    val javaVersion = JavaVersion.VERSION_17
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions { jvmTarget = javaVersion.toString() }
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
    }

    jar {
        from("LICENSE")
    }

    /*shadowJar {
        destinationDirectory.set(buildDir.resolve("devlibs"))
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
    }*/

    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") { expand(mutableMapOf("version" to project.version)) }
    }

    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion.toString())) }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }
}
