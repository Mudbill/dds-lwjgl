dds-lwjgl
======

dds-lwjgl is a small and simple DirectDraw Surface (DDS) file parser made for the Light-Weight Java Game Library.
It allows you to load DDS texture files for LWJGL, which can be useful as DDS files have the added advantage of being decompressed on the GPU, making for faster load times.

Features
------------

* Supports loading standard headers and DXT10 headers
* Supports loading the compression formats: DXT1 (BC1), DXT3 (BC2), DXT5 (BC3), ATI1 (BC4), ATI2 (BC5), BC6H and BC7
* Supports loading 2D Textures with and without mipmaps
* Supports loading 3D Cubemap textures with and without mipmaps
* Does not support volume maps.
* Does not support legacy formats.
* Does not support uncompressed formats.

This list may change with updates.

Usage
-----

[Download the jar](https://github.com/Mudbill/dds-lwjgl/releases) from the releases page and include it in your project's build path, and you'll be able to access the `DDSFile` class. Using it is simple:

```java
DDSFile file = new DDSFile("path/to/file");
```

This will immediately load the file at the given path. You can also supply a `File` or a `FileInputStream`. Optionally use the `loadFile` method after initializing the object if you prefer.

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

History
-------

While working on some personal projects, I had the need to load DDS files for my 3D application. Not only is it very tempting to load a format that, from my basic testing, loads nearly 10x as fast as a JPEG texture, but the application I was making relied on external sources, some of which were DDS files.

After looking for such a library, I only came across 1, as well as a few (quite old) threads for writing a parser yourself. Since I couldn't get any of these to do what I wanted, I decided to just sit down and make my own.
