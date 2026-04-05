plugins {
    id("java-library")
}

group = "kr.syeyoung.dungeonsguide"
version = "1.0.0"

java {
//    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}

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

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    testCompileOnly("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")
}


tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}


tasks.test {
    useJUnitPlatform()
}