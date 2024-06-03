import com.xpdustry.toxopid.extension.anukeXpdustry
import com.xpdustry.toxopid.spec.ModMetadata
import com.xpdustry.toxopid.spec.ModPlatform

plugins {
    alias(libs.plugins.indra.common)
    alias(libs.plugins.shadow)
    alias(libs.plugins.toxopid)
}

val metadata = ModMetadata.fromJson(rootProject.file("plugin.json"))
group = "com.xpdustry"
version = metadata.version
description = metadata.description

toxopid {
    compileVersion = "v${metadata.minGameVersion}"
    platforms = setOf(ModPlatform.SERVER)
}

repositories {
    mavenCentral()
    anukeXpdustry()
}

dependencies {
    compileOnly(toxopid.dependencies.arcCore)
    compileOnly(toxopid.dependencies.mindustryCore)
}

indra {
    javaVersions {
        target(17)
        minimumToolchain(17)
    }

    mitLicense()

    if (metadata.repository.isNotBlank()) {
        val repo = metadata.repository.split("/")
        github(repo[0], repo[1]) {
            ci(true)
            issues(true)
            scm(true)
        }
    }

    configurePublications {
        pom {
            developers {
                developer {
                    id = metadata.author
                }
            }
        }
    }
}

val generateResources by tasks.registering {
    outputs.files(fileTree(temporaryDir))
    doLast {
        temporaryDir.resolve("plugin.json").writeText(ModMetadata.toJson(metadata))
    }
}

tasks.shadowJar {
    archiveFileName = "${metadata.name}.jar"
    archiveClassifier = "plugin"
    from(generateResources)
    from(rootProject.file("LICENSE.md")) { into("META-INF") }
    minimize()
}

tasks.register("release") {
    dependsOn(tasks.build)
    project.copy {
        from(tasks.shadowJar)
        into(temporaryDir)
    }
}
