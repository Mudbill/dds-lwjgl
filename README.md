dds-lwjgl
======

dds-lwjgl is a simple DirectDraw Surface (DDS) file parser for the Light-Weight Java Game Library (with possibly other uses).
It is a small library aimed at loading DDS files for LWJGL, as DDS files have the added advantage of being decompressed on the GPU, making for faster load times.
It is by no means an extensive library, and has several limitations (please read below).

Restrictions
------------

dds-lwjgl currently has a selection of restrictions, as I originally developed this for a very specific purpose. Please consider these.

* Supports loading standard 124 byte headers (not extended D3D headers)
* Supports loading the compression formats: DXT1, DXT3, DXT5
* Supports reading 2D Textures with and without included mipmaps (though the mipmaps are currently discarded)
* Supports loading 3D Cubemap textures *without* mipmaps. Cubemaps with mipmaps appear offset.
* Does not support volume maps.
* Does not support legacy formats.

This list may change with updates.

Usage
-----

[Download the jar](https://github.com/Mudbill/dds-lwjgl/releases) from the releases page and include it in your project's build path, and you'll be able to access the DDSFile class. Using it is simple:

```java
DDSFile file = new DDSFile("path/to/file");
```

This will immediately load the file at the given path. You can also supply a `File`.

If you need debug information (I use this a lot), separate them and enable the `printDebug` flag.

```java
DDSFile file = new DDSFile();
file.printDebug = true;
file.loadFile("path/to/file");
```

To get the texture data and load it to OpenGL, you can do something like this:

For 2D textures:
```java
int textureID = GL11.glGenTextures();       // Generate a texture ID.
GL13.glActiveTexture(GL13.GL_TEXTURE0);     // Depends on your implementation
GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
GL13.glCompressedTexImage2D(GL11.GL_TEXTURE_2D, 0, file.getDXTFormat(), file.getWidth(), file.getHeight(), 0, file.getBuffer());
GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);  // Optional, since I don't yet support embedded mipmaps.
```

For Cubemaps:
```java
int textureID = GL11.glGenTextures();                    // Generate a texture ID.
GL13.glActiveTexture(GL13.GL_TEXTURE0);
GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, textureID); // Remember this setting.
GL13.glCompressedTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, file.getDXTFormat(), file.getWidth(), file.getHeight(), 0, file.getCubeMapPositiveX());
GL13.glCompressedTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, file.getDXTFormat(), file.getWidth(), file.getHeight(), 0, file.getCubeMapNegativeX());
GL13.glCompressedTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, file.getDXTFormat(), file.getWidth(), file.getHeight(), 0, file.getCubeMapPositiveY());
GL13.glCompressedTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, file.getDXTFormat(), file.getWidth(), file.getHeight(), 0, file.getCubeMapNegativeY());
GL13.glCompressedTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, file.getDXTFormat(), file.getWidth(), file.getHeight(), 0, file.getCubeMapPositiveZ());
GL13.glCompressedTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, file.getDXTFormat(), file.getWidth(), file.getHeight(), 0, file.getCubeMapNegativeZ());
```

PS: You can check if the file is a cubemap using `isCubeMap()`.

History
-------

While working on some personal projects, I had the need to load DDS files for my 3D application. Not only is it very tempting to load a format that (from my testing) loads nearly 10x as fast as a JPEG texture, but the application I was making relied on external sources, some of which were DDS files.

After looking for such a library, I only came across 1, as well as a few (quite old) threads around for writing a parser yourself. Since I couldn't get any of these to do what I wanted, I decided to just sit down and make my own.

This small library contains only the core features for what I originally needed, so it is by no means reliable for just any DDS file. As of today, I still need to edit it to fix some bugs, but I felt it usable enough to publish.

I'm also a fairly self-taught programmer, so any feedback is greatly appreciated.

(I'm also still not too familiar with GitHub, so please bear with me)

License
-------

This library is free to use as long as it complies with the GNU GPL license. Please see https://www.gnu.org/licenses/ for more information.
