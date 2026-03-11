plugins {
    id("java-library")
}

group = "kr.syeyoung.dungeonsguide"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    api("org.json:json:20240303")
    api("org.apache.logging.log4j:log4j-core:2.25.3")
    implementation("io.nayuki:qrcodegen:1.4.0")
    implementation("org.bouncycastle:bcpg-jdk15on:1.70")

    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.16")

    testCompileOnly("org.projectlombok:lombok:1.18.20")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.20")
}

tasks.test {
    useJUnitPlatform()
}