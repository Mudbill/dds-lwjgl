// I just hardcoded this for now.
// Change to appropriate natives for the platform.
// E.g. "natives-windows", "natives-linux-arm32"
// Might automate that later
val lwjglNatives = "natives-macos-arm64"

group = "io.github.mudbill"
version = "2.1.1"

plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
}

publishing {
    publications {
        create<MavenPublication>("java") {
            from(components["java"])

            pom {
                name = "DDS for LWJGL"
                description =
                    "dds-lwjgl is a tiny library for parsing DirectDraw Surface texture files for use in LWJGL's OpenGL in Java."
                url = "https://github.com/Mudbill/dds-lwjgl"
                inceptionYear = "2018"

                licenses {
                    license {
                        name = "GPL-3.0"
                        url = "https://www.gnu.org/licenses/gpl-3.0.en.html"
                    }
                }

                developers {
                    developer {
                        id = "mudbill"
                        name = "Magnus Bull"
                        email = "mudbill@buttology.net"
                    }
                }

                scm {
                    connection = "scm:git:git:github.com/Mudbill/dds-lwjgl.git"
                    developerConnection = "scm:git:ssh://github.com/Mudbill/dds-lwjgl.git"
                    url = "https://github.com/Mudbill/dds-lwjgl"
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["java"])
}

repositories {
    mavenCentral()
}

val lwjglVersion = "3.3.3"

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-opengl")
    testImplementation("org.lwjgl", "lwjgl-glfw")

    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
}

java {
    withSourcesJar()
    withJavadocJar()
}
