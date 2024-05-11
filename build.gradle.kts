// Calculate the appropriate natives for the current OS (just for running tests)
val lwjglNatives = Pair(
    System.getProperty("os.name")!!,
    System.getProperty("os.arch")!!
).let { (name, arch) ->
    when {
        arrayOf("Linux", "SunOS", "Unit").any { name.startsWith(it) } ->
            if (arrayOf("arm", "aarch64").any { arch.startsWith(it) })
                "natives-linux${if (arch.contains("64") || arch.startsWith("armv8")) "-arm64" else "-arm32"}"
            else if (arch.startsWith("ppc"))
                "natives-linux-ppc64le"
            else if (arch.startsWith("riscv"))
                "natives-linux-riscv64"
            else
                "natives-linux"
        arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) }     ->
            "natives-macos${if (arch.startsWith("aarch64")) "-arm64" else ""}"
        arrayOf("Windows").any { name.startsWith(it) }                ->
            if (arch.contains("64"))
                "natives-windows${if (arch.startsWith("aarch64")) "-arm64" else ""}"
            else
                "natives-windows-x86"
        else                                                                            ->
            throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
    }
}

group = "io.github.mudbill"
version = "3.0.0"

plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
}

java {
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    // Support all version of 3, but not 4 (because at this time there is no 4 so it should be tested first)
    implementation(platform("org.lwjgl:lwjgl-bom:[3, 4["))
    implementation("org.lwjgl", "lwjgl-opengl")

    testRuntimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    testRuntimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    testRuntimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    testImplementation("org.lwjgl", "lwjgl-glfw")
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