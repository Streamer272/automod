import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val exposedVersion: String by project

plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "com.streamer272"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.jessecorbett:diskord-bot:3.0.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")
    implementation("io.github.microutils:kotlin-logging:2.1.21")
    implementation("com.google.cloud:google-cloud-firestore:3.8.0")
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
