plugins {
    alias(libs.plugins.quilt.loom)
}

base {
    val archivesBaseName: String by project
    archivesName.set(archivesBaseName)
}

version = "${project.version}+${libs.versions.minecraft.get()}"

loom {
    mods {
        register("allowlist") {
            sourceSet("main")
        }
    }
}

dependencies {
    minecraft(libs.minecraft)
    mappings(variantOf(libs.quilt.mappings) { classifier("intermediary-v2") })

    modImplementation(libs.quilted.fabric.api)
    modImplementation(libs.quilt.loader)
}

tasks {
    processResources {
        inputs.property("version", version)

        filesMatching("quilt.mod.json") {
            expand(mapOf("version" to version))
        }
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        // Minecraft 1.18 (1.18-pre2) upwards uses Java 17.
        options.release = 17
    }

    withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${base.archivesName}" }
        }
    }

    java {
        // Still required by IDEs such as Eclipse and Visual Studio Code
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        withSourcesJar()
    }
}