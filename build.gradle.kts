import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val exposedVersion: String by project

plugins {
    kotlin("jvm") version "1.6.20"
    kotlin("plugin.serialization") version "1.6.10"
    application
}

group = "com.streamer272"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "Github PKG"
        url = uri("https://maven.pkg.github.com/jofairden/discord.kt")
        credentials {
            username = "Streamer272"
            password = "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"
        }
    }
}

dependencies {
    implementation("com.jessecorbett:diskord-bot:2.1.3")
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.postgresql:postgresql:42.3.4")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}
