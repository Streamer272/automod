import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
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
            /**
             * In order to access Github PKG, you need to enter your username and access token with the read:packages grant
             * To create a personal access token, go to: https://github.com/settings/tokens
             */
            username = "Streamer272"
            password = "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"
        }
    }
}

dependencies {
    implementation("com.jessecorbett:diskord-bot:2.1.1")
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")

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
