// I just hardcoded this for now.
// Change to appropriate natives for the platform.
// E.g. "natives-windows", "natives-linux-arm32"
// Might automate that later
val lwjglNatives = "natives-macos-arm64"

group = "net.buttology.lwjgl.dds"
version = "2.1.1"

plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:3.3.3"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-opengl")
    testImplementation("org.lwjgl", "lwjgl-glfw")

    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
}