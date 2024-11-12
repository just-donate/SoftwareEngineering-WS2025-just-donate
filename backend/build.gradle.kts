plugins {
    id("java")
}

group = "com.just.donate"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("io.vavr:vavr:0.10.4")
}

tasks.test {
    useJUnitPlatform()
}