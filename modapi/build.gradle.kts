plugins {
    id("java")
    id("java-library")
}

group = "kr.syeoyung.dungeonsguide"
version = "unspecified"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}


repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.16")
    api("com.mojang:brigadier:1.0.18")

    testCompileOnly("org.projectlombok:lombok:1.18.20")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.20")
}

tasks.test {
    useJUnitPlatform()
}