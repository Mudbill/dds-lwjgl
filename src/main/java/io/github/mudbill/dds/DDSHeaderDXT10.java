package io.github.mudbill.dds;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.EXTTextureCompressionS3TC;
import org.lwjgl.opengl.EXTTextureCompressionRGTC;

class DDSHeaderDXT10 {
//    static final int DDS_DIMENSION_TEXTURE1D          = 0x2;
//    static final int DDS_DIMENSION_TEXTURE2D          = 0x3;
//    static final int DDS_DIMENSION_TEXTURE3D          = 0x4;

    static final int DDS_RESOURCE_MISC_TEXTURECUBE    = 0x4;

//    static final int DDS_ALPHA_MODE_UNKNOWN           = 0x0;
//    static final int DDS_ALPHA_MODE_STRAIGHT          = 0x1;
//    static final int DDS_ALPHA_MODE_PREMULTIPLIED     = 0x2;
//    static final int DDS_ALPHA_MODE_OPAQUE            = 0x3;
//    static final int DDS_ALPHA_MODE_CUSTOM            = 0x4;

    static final int DXGI_FORMAT_BC1_UNORM            = 71;
    static final int DXGI_FORMAT_BC2_UNORM            = 74;
    static final int DXGI_FORMAT_BC3_UNORM            = 77;
    static final int DXGI_FORMAT_BC4_UNORM            = 80;
    static final int DXGI_FORMAT_BC4_SNORM            = 81;
    static final int DXGI_FORMAT_BC5_UNORM            = 83;
    static final int DXGI_FORMAT_BC5_SNORM            = 84;
    static final int DXGI_FORMAT_BC6H_UF16            = 95;
    static final int DXGI_FORMAT_BC6H_SF16            = 96;
    static final int DXGI_FORMAT_BC7_UNORM            = 98;
    static final int DXGI_FORMAT_BC7_UNORM_SRGB       = 99;

    int dxgiFormat;
    int resourceDimension;
    int miscFlag;
    int arraySize;
    int miscFlags2;

    boolean isTextureCube;

    DDSHeaderDXT10(ByteBuffer header) throws IOException {
        if (header.capacity() != 20) {
            throw new IOException("DXT10 header needs 20 bytes");
        }

        dxgiFormat          = header.getInt();
        resourceDimension   = header.getInt();
        miscFlag            = header.getInt();
        arraySize           = header.getInt();
        miscFlags2          = header.getInt();

        isTextureCube = (miscFlag & DDS_RESOURCE_MISC_TEXTURECUBE) == DDS_RESOURCE_MISC_TEXTURECUBE;
    }

    public int getFormat() {
        switch (dxgiFormat) {
            case DXGI_FORMAT_BC1_UNORM:         return EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT1_EXT;
            case DXGI_FORMAT_BC2_UNORM:         return EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
            case DXGI_FORMAT_BC3_UNORM:         return EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
            case DXGI_FORMAT_BC4_UNORM:         return EXTTextureCompressionRGTC.GL_COMPRESSED_RED_RGTC1_EXT;
            case DXGI_FORMAT_BC4_SNORM:         return EXTTextureCompressionRGTC.GL_COMPRESSED_SIGNED_RED_RGTC1_EXT;
            case DXGI_FORMAT_BC5_UNORM:         return EXTTextureCompressionRGTC.GL_COMPRESSED_RED_GREEN_RGTC2_EXT;
            case DXGI_FORMAT_BC5_SNORM:         return EXTTextureCompressionRGTC.GL_COMPRESSED_SIGNED_RED_GREEN_RGTC2_EXT;
            case DXGI_FORMAT_BC6H_UF16:         return GL42.GL_COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT;
            case DXGI_FORMAT_BC6H_SF16:         return GL42.GL_COMPRESSED_RGB_BPTC_SIGNED_FLOAT;
            case DXGI_FORMAT_BC7_UNORM:         return GL42.GL_COMPRESSED_RGBA_BPTC_UNORM;
            case DXGI_FORMAT_BC7_UNORM_SRGB:    return GL42.GL_COMPRESSED_SRGB_ALPHA_BPTC_UNORM;
        }
        return GL11.GL_INVALID_VALUE;
    }

    public int getBlockSize() {
        switch (dxgiFormat) {
            case DXGI_FORMAT_BC1_UNORM:
            case DXGI_FORMAT_BC4_UNORM:
            case DXGI_FORMAT_BC4_SNORM:
                return 8;
        }
        return 16;
    }
}
