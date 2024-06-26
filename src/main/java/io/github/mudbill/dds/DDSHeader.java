package io.github.mudbill.dds;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Internal class storing the header information for the DDS texture file
 */
public class DDSHeader {

    /* Flags */
    static final int DDSD_CAPS        = 0x000001;
    static final int DDSD_HEIGHT      = 0x000002;
    static final int DDSD_WIDTH       = 0x000004;
    static final int DDSD_PITCH       = 0x000008;
    static final int DDSD_PIXELFORMAT = 0x001000;
    static final int DDSD_MIPMAPCOUNT = 0x020000;
    static final int DDSD_LINEARSIZE  = 0x080000;
    static final int DDSD_DEPTH       = 0x800000;
    
//  public static final int DDS_HEADER_FLAGS_TEXTURE = DDSD_CAPS | DDSD_HEIGHT | DDSD_WIDTH | DDSD_PIXELFORMAT;
//  public static final int DDS_HEADER_FLAGS_MIPMAP = DDSD_MIPMAPCOUNT;
//  public static final int DDS_HEADER_FLAGS_VOLUME = DDSD_DEPTH;
//  public static final int DDS_HEADER_FLAGS_PITCH = DDSD_PITCH;
//  public static final int DDS_HEADER_FLAGS_LINEARSIZE = DDSD_LINEARSIZE;
    
    static final int DDSCAPS_COMPLEX  = 0x000008;
    static final int DDSCAPS_MIPMAP   = 0x400000;
    static final int DDSCAPS_TEXTURE  = 0x001000;
    
//  public static final int DDS_SURFACE_FLAGS_MIPMAP = DDSCAPS_COMPLEX | DDSCAPS_MIPMAP;
//  public static final int DDS_SURFACE_FLAGS_TEXTURE = DDSCAPS_TEXTURE;
//  public static final int DDS_SURFACE_FLAGS_CUBEMAP = DDSCAPS_COMPLEX;
    
    static final int DDSCAPS2_CUBEMAP             = 0x000200;
    static final int DDSCAPS2_CUBEMAP_POSITIVEX   = 0x000400;
    static final int DDSCAPS2_CUBEMAP_NEGATIVEX   = 0x000800;
    static final int DDSCAPS2_CUBEMAP_POSITIVEY   = 0x001000;
    static final int DDSCAPS2_CUBEMAP_NEGATIVEY   = 0x002000;
    static final int DDSCAPS2_CUBEMAP_POSITIVEZ   = 0x004000;
    static final int DDSCAPS2_CUBEMAP_NEGATIVEZ   = 0x008000;
    static final int DDSCAPS2_VOLUME              = 0x200000;
    
//  public static final int DDS_CUBEMAP_POSITIVEX = DDSCAPS2_CUBEMAP | DDSCAPS2_CUBEMAP_POSITIVEX;
//  public static final int DDS_CUBEMAP_NEGATIVEX = DDSCAPS2_CUBEMAP | DDSCAPS2_CUBEMAP_NEGATIVEX;
//  public static final int DDS_CUBEMAP_POSITIVEY = DDSCAPS2_CUBEMAP | DDSCAPS2_CUBEMAP_POSITIVEY;
//  public static final int DDS_CUBEMAP_NEGATIVEY = DDSCAPS2_CUBEMAP | DDSCAPS2_CUBEMAP_NEGATIVEY;
//  public static final int DDS_CUBEMAP_POSITIVEZ = DDSCAPS2_CUBEMAP | DDSCAPS2_CUBEMAP_POSITIVEZ;
//  public static final int DDS_CUBEMAP_NEGATIVEZ = DDSCAPS2_CUBEMAP | DDSCAPS2_CUBEMAP_NEGATIVEZ;
//  public static final int DDS_CUBEMAP_ALLFACES = DDSCAPS2_CUBEMAP_POSITIVEX | DDSCAPS2_CUBEMAP_NEGATIVEX | DDSCAPS2_CUBEMAP_POSITIVEY | DDSCAPS2_CUBEMAP_NEGATIVEY | DDSCAPS2_CUBEMAP_POSITIVEZ | DDSCAPS2_CUBEMAP_NEGATIVEZ;
//  public static final int DDS_FLAGS_VOLUME = DDSCAPS2_VOLUME;
    
    /**
     * Size of header in bytes
     */
    int dwSize;
    
    /**
     * Flags to indicate which members contain valid data
     */
    int dwFlags;
    
    /**
     * Height in pixels of surface
     */
    int dwHeight;
    
    /**
     * Width in pixels of surface
     */
    int dwWidth;
    
    /**
     * The pitch or number of bytes per scan line in an uncompressed texture
     */
    int dwPitchOrLinearSize;
    
    /**
     * Depth of a volume texture in pixels, otherwise unused
     */
    int dwDepth;
    
    /**
     * Number of mipmap levels, otherwise unused
     */
    int dwMipMapCount;
    
    /**
     * Unused bytes
     */
    int[] dwReserved = new int[11];
    
    /**
     * The pixel format
     */
    DDSPixelFormat ddspf;
    
    /**
     * Specifies the complexity of the surfaces stored
     */
    int dwCaps;
    
    /**
     * Additional details about the surfaces stored
     */
    int dwCaps2;
    
    /**
     * Unused bytes
     */
    int dwCaps3;
    
    /**
     * Unused bytes
     */
    int dwCaps4;
    
    /**
     * Unused bytes
     */
    int dwReserved2;
    
    boolean hasFlagMipMapCount;
    boolean hasFlagCaps;
    boolean hasFlagHeight;
    boolean hasFlagWidth;
    boolean hasFlagPitch;
    boolean hasFlagPixelFormat;
    boolean hasFlagLinearSize;
    boolean hasFlagDepth;
    
    boolean hasCapsComplex;
    boolean hasCapsMipMap;
    boolean hasCapsTexture;
    
    boolean hasCaps2CubeMap;
    boolean hasCaps2CubeMapPX;
    boolean hasCaps2CubeMapNX;
    boolean hasCaps2CubeMapPY;
    boolean hasCaps2CubeMapNY;
    boolean hasCaps2CubeMapPZ;
    boolean hasCaps2CubeMapNZ;
    boolean hasCaps2Volume;

    DDSHeader(ByteBuffer header) throws IOException {
        if (header.capacity() != 124) {
            throw new IOException("Invalid header size. Should be 124 but is " + header.capacity());
        }
        
        dwSize              = header.getInt();
        dwFlags             = header.getInt();
        dwHeight            = header.getInt();
        dwWidth             = header.getInt();
        dwPitchOrLinearSize = header.getInt();
        dwDepth             = header.getInt();
        dwMipMapCount       = header.getInt();
        
        // Unused bytes
        for (int i = 0; i < dwReserved.length; i++) {
            dwReserved[i]   = header.getInt();
        }
        
        ddspf               = new DDSPixelFormat(header);
        
        dwCaps              = header.getInt();
        dwCaps2             = header.getInt();
        
        // Unused bytes
        dwCaps3             = header.getInt();
        dwCaps4             = header.getInt();
        dwReserved2         = header.getInt();
        
        /* Flags */
        hasFlagCaps         = (dwFlags & DDSD_CAPS)         == DDSD_CAPS;
        hasFlagHeight       = (dwFlags & DDSD_HEIGHT)       == DDSD_HEIGHT;
        hasFlagWidth        = (dwFlags & DDSD_WIDTH)        == DDSD_WIDTH;
        hasFlagPitch        = (dwFlags & DDSD_PITCH)        == DDSD_PITCH;
        hasFlagPixelFormat  = (dwFlags & DDSD_PIXELFORMAT)  == DDSD_PIXELFORMAT;
        hasFlagMipMapCount  = (dwFlags & DDSD_MIPMAPCOUNT)  == DDSD_MIPMAPCOUNT;
        hasFlagLinearSize   = (dwFlags & DDSD_LINEARSIZE)   == DDSD_LINEARSIZE;
        hasFlagDepth        = (dwFlags & DDSD_DEPTH)        == DDSD_DEPTH;
        
        /* Caps */
        hasCapsComplex      = (dwCaps & DDSCAPS_COMPLEX)    == DDSCAPS_COMPLEX;
        hasCapsMipMap       = (dwCaps & DDSCAPS_MIPMAP)     == DDSCAPS_MIPMAP;
        hasCapsTexture      = (dwCaps & DDSCAPS_TEXTURE)    == DDSCAPS_TEXTURE;
        
        /* Caps2 */
        hasCaps2CubeMap     = (dwCaps2 & DDSCAPS2_CUBEMAP)              == DDSCAPS2_CUBEMAP;
        hasCaps2CubeMapPX   = (dwCaps2 & DDSCAPS2_CUBEMAP_POSITIVEX)    == DDSCAPS2_CUBEMAP_POSITIVEX;
        hasCaps2CubeMapNX   = (dwCaps2 & DDSCAPS2_CUBEMAP_NEGATIVEX)    == DDSCAPS2_CUBEMAP_NEGATIVEX;
        hasCaps2CubeMapPY   = (dwCaps2 & DDSCAPS2_CUBEMAP_POSITIVEY)    == DDSCAPS2_CUBEMAP_POSITIVEY;
        hasCaps2CubeMapNY   = (dwCaps2 & DDSCAPS2_CUBEMAP_NEGATIVEY)    == DDSCAPS2_CUBEMAP_NEGATIVEY;
        hasCaps2CubeMapPZ   = (dwCaps2 & DDSCAPS2_CUBEMAP_POSITIVEZ)    == DDSCAPS2_CUBEMAP_POSITIVEZ;
        hasCaps2CubeMapNZ   = (dwCaps2 & DDSCAPS2_CUBEMAP_NEGATIVEZ)    == DDSCAPS2_CUBEMAP_NEGATIVEZ;
        hasCaps2Volume      = (dwCaps2 & DDSCAPS2_VOLUME)               == DDSCAPS2_VOLUME;
        
        // Do some error checking.
        
        if (!hasFlagCaps || !hasFlagHeight || !hasFlagWidth || !hasFlagPixelFormat) {
            throw new IOException("Required DDS internal flags missing.");
        }
        
        if (!hasCapsTexture) {
            throw new IOException("Required DDS internal caps missing.");
        }
    }
}
