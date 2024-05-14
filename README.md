# dds-lwjgl

A small library to add support for DirectDraw Surface textures in LWJGL 3. 

DDS textures are useful for improving performance, since they are decompressed on the GPU, decreasing CPU and memory usage.
Additionally they can include embedded mipmaps, further optimizing the appearance of textures without having to generate them on the fly.

## Features

* Supports loading standard headers and DXT10-extended headers
* Supports loading the compression formats: DXT1 (BC1), DXT3 (BC2), DXT5 (BC3), ATI1 (BC4), ATI2 (BC5), BC6H and BC7
* Supports loading 2D Textures with and without mipmaps
* Supports loading 3D Cubemap textures with and without mipmaps
* Does **not** support volume maps.
* Does **not** support legacy formats.
* Does **not** support uncompressed formats.

## Install with dependency manager

Check it out on Maven Central for your dependency manager: https://central.sonatype.com/artifact/io.github.mudbill/dds-lwjgl

Gradle users can add the following dependency to their build.gradle(.kts):

```kotlin
implementation("io.github.mudbill:dds-lwjgl:3.0.0")
```

Maven users can add the following dependency to their pom.xml:

```xml
<dependency>
    <groupId>io.github.mudbill</groupId>
    <artifactId>dds-lwjgl</artifactId>
    <version>3.0.0</version>
</dependency>
```

## Install manually

[Download the jar](https://github.com/Mudbill/dds-lwjgl/releases) from the releases page and include it in your project's build path.

## Usage

There's only one class you need to care about, that being `io.github.mudbill.dds.DDSFile`.

```java
DDSFile file = new DDSFile("path/to/file");
```

You can supply a string path, `File` instance, or `InputStream` to load the file.
You can also skip the argument and use the `.loadFile` method later.

To get the texture data and load it into OpenGL, you can do something like this:

For 2D textures:
```java
int textureID = GL11.glGenTextures();       // Generate a texture ID.
GL13.glActiveTexture(GL13.GL_TEXTURE0);     // Depends on your implementation
GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
for (int level = 0; level < file.getMipMapCount(); level++)
    GL13.glCompressedTexImage2D(
    	GL11.GL_TEXTURE_2D, 
    	level, 
    	file.getFormat(), 
    	file.getWidth(level), 
    	file.getHeight(level), 
    	0, 
    	file.getBuffer(level)
    );
GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, file.getMipMapCount() - 1);
```

For Cubemaps:
```java
int textureID = GL11.glGenTextures();                    // Generate a texture ID.
GL13.glActiveTexture(GL13.GL_TEXTURE0);
GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, textureID); // Remember this setting.
GL13.glCompressedTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, file.getFormat(), file.getWidth(), file.getHeight(), 0, file.getCubeMapPositiveX());
GL13.glCompressedTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, file.getFormat(), file.getWidth(), file.getHeight(), 0, file.getCubeMapNegativeX());
GL13.glCompressedTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, file.getFormat(), file.getWidth(), file.getHeight(), 0, file.getCubeMapPositiveY());
GL13.glCompressedTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, file.getFormat(), file.getWidth(), file.getHeight(), 0, file.getCubeMapNegativeY());
GL13.glCompressedTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, file.getFormat(), file.getWidth(), file.getHeight(), 0, file.getCubeMapPositiveZ());
GL13.glCompressedTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, file.getFormat(), file.getWidth(), file.getHeight(), 0, file.getCubeMapNegativeZ());
```

PS: You can check if the file is a cubemap using `isCubeMap()`.

## History

While working on some personal projects, I had the need to load DDS files for my 3D application. Not only is it very tempting to load a format that, from my basic testing, loads nearly 10x as fast as a JPEG texture, but the application I was making relied on external sources, some of which were DDS files.

After looking for such a library, I only came across one that I couldn't get to work, as well as a few (quite old) threads for writing a parser yourself. Since I couldn't get any of these to do what I wanted, I decided to just sit down and make my own. It took some fiddling with Microsoft's documentation, but it was a fun exercise.
