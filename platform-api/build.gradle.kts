plugins {
    id("java")
    id("java-library")
}

group = "kr.syeoyung.dungeonsguide"
version = "1.0"

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}


repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    api("com.mojang:brigadier:1.0.18")
    // we use adventure api for platform agnostic text components / nbt.
    api("net.kyori:adventure-api:4.23.0")
    api("net.kyori:adventure-nbt:4.23.0")
    api("net.kyori:adventure-text-serializer-gson:4.23.0")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    testCompileOnly("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")
}

tasks.test {
    useJUnitPlatform()
}