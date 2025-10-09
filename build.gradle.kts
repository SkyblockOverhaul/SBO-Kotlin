import dev.deftu.gradle.utils.version.MinecraftVersions

plugins {
    java
    kotlin("jvm")
    id("dev.deftu.gradle.multiversion")
    id("dev.deftu.gradle.tools")
    id("dev.deftu.gradle.tools.resources")
    id("dev.deftu.gradle.tools.bloom")
    id("dev.deftu.gradle.tools.shadow")
    id("dev.deftu.gradle.tools.minecraft.loom")
    id("dev.deftu.gradle.tools.minecraft.releases")
    id("com.google.devtools.ksp") version("2.2.10-2.0.2")
}

repositories {
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://repo.essential.gg/repository/maven-public")
    maven("https://maven.teamresourceful.com/repository/maven-public/")
    maven("https://maven.terraformersmc.com/")
    maven("https://maven.azureaaron.net/releases")
}

toolkitMultiversion {
    moveBuildsToRootProject.set(true)
}

dependencies {
    modImplementation("net.fabricmc.fabric-api:fabric-api:${mcData.dependencies.fabric.fabricApiVersion}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${mcData.dependencies.fabric.fabricLanguageKotlinVersion}")

    ksp(project(":event-processor"))
    ksp("dev.zacsweers.autoservice:auto-service-ksp:1.1.0")

    modImplementation(include("gg.essential:elementa:710")!!)
    modImplementation(include("gg.essential:universalcraft-${mcData}:430")!!)
    modImplementation(include("com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-fabric-1.21.5:3.5.13")!!)
    modImplementation(include("net.azureaaron:hm-api:1.0.1+1.21.2")!!)

    // modImplementation(include("xyz.meowing:vexel-${mcData}:100")!!)

    when (mcData.version) {
        MinecraftVersions.VERSION_1_21_7 -> {
            modImplementation("com.terraformersmc:modmenu:15.0.0-beta.3")
            modImplementation(include("com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-${mcData.version}:3.7.2")!!)
        }
        MinecraftVersions.VERSION_1_21_5 -> {
            modImplementation("com.terraformersmc:modmenu:14.0.0-rc.2")
            modImplementation(include("com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-${mcData.version}:3.5.13")!!)
        }
        else -> {}
    }

    implementation(project(":event-processor"))

    runtimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")
}

tasks.findByName("preprocessCode")?.dependsOn(":1.21.5-fabric:kspKotlin")
