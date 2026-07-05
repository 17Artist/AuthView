plugins {
    kotlin("jvm") version "1.8.22"
    id("priv.seventeen.artist.blink") version "1.3.12"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "priv.seventeen.artist.arcartx.authview"
version = "2.0.0"

repositories {
    mavenLocal()
    maven("https://repo.arcartx.com/repository/maven-public/")   // Blink + ArcartX
    maven("https://repo.codemc.io/repository/maven-public/")     // AuthMe
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

blink {
    name.set("AuthView")
    version.set(project.version.toString())
    description.set("基于 Blink + ArcartX 的 AuthMe 登录 / 注册 / 改密界面")
    authors.set(listOf("17Artist"))
    apiVersion.set("1.18")
    packageName.set("priv.seventeen.artist.arcartx.authview")
    depend.set(listOf("ArcartX", "AuthMe"))
    logPrefix.set("§6♦ §bArc§3art§1X§7-§eAuthView")
    foliaSupported.set(false)
    obfuscate.set(false)
}

dependencies {
    implementation("priv.seventeen.artist.blink:blink-common:1.3.12")

    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("priv.seventeen.artist.arcartx:ArcartX:2.2.0.5")
    compileOnly("fr.xephi:authme:5.7.0")
}

kotlin {
    jvmToolchain(17)
}

tasks.shadowJar {
    archiveClassifier.set("")   // 产物无 -all 后缀
}

tasks.named("build") {
    dependsOn("shadowJar")
}
