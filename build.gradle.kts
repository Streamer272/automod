import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "dev.svitan"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.jessecorbett:diskord-bot:3.0.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")
    implementation("org.slf4j:slf4j-simple:2.0.3")
    implementation("io.github.microutils:kotlin-logging:3.0.5")
    implementation("com.google.firebase:firebase-admin:9.1.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("MainKt")
}
