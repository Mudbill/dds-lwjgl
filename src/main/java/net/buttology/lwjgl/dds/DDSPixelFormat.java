package net.buttology.lwjgl.dds;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Internal class to store the pixel format information from the DDS file header
 */
public class DDSPixelFormat {

    static final int DDPF_ALPHAPIXELS     = 0x00001;
    static final int DDPF_ALPHA           = 0x00002;
    static final int DDPF_FOURCC          = 0x00004;
    static final int DDPF_RGB             = 0x00040;
    static final int DDPF_YUV             = 0x00200;
    static final int DDPF_LUMINANCE       = 0x20000;
    
    /**
     * Structure size in bytes
     */
    int dwSize;
    
    /**
     * Values which indicate what type of data is in the surface
     */
    int dwFlags;
    
    /**
     * Four-character code for specifying compressed or custom format
     */
    int dwFourCC;
    
    /**
     * Number of bits in an RGB (possibly including alpha) format
     */
    int dwRGBBitCount;
    
    /**
     * Red (or luminance or Y) mask for reading color data
     */
    int dwRBitMask;
    
    /**
     * Green (or U) mask for reading color data
     */
    int dwGBitMask;
    
    /**
     * Blue (or V) mask for reading color data
     */
    int dwBBitMask;
    
    /**
     * Alpha mask for reading alpha data
     */
    int dwABitMask;
    
    /**
     * Four-character code's String representation
     */
    String sFourCC;
    
    boolean isCompressed;
    boolean hasFlagAlphaPixels;
    boolean hasFlagAlpha;
    boolean hasFlagFourCC;
    boolean hasFlagRgb;
    boolean hasFlagYuv;
    boolean hasFlagLuminance;
    
    /**
     * Constructs the four-character code's String representation from the integer value.
     * @param fourCC bytes to test
     * @return a string of the given bytes
     */
    private String createFourCCString(int fourCC) {
        byte[] fourCCString = new byte[DDPF_FOURCC];

        for (int i = 0; i < fourCCString.length; i++) {
            fourCCString[i] = (byte) (fourCC >> (i*8));
        }

        return new String(fourCCString);
    }

    DDSPixelFormat(ByteBuffer header) throws IOException {
        dwSize          = header.getInt();
        dwFlags         = header.getInt();
        dwFourCC        = header.getInt();
        dwRGBBitCount   = header.getInt();
        dwRBitMask      = header.getInt();
        dwGBitMask      = header.getInt();
        dwBBitMask      = header.getInt();
        dwABitMask      = header.getInt();
        
        sFourCC = createFourCCString(dwFourCC);
                
        hasFlagAlphaPixels  = (dwFlags & DDPF_ALPHAPIXELS)  == DDPF_ALPHAPIXELS;
        hasFlagAlpha        = (dwFlags & DDPF_ALPHA)        == DDPF_ALPHA;
        hasFlagFourCC       = (dwFlags & DDPF_FOURCC)       == DDPF_FOURCC;
        hasFlagRgb          = (dwFlags & DDPF_RGB)          == DDPF_RGB;
        hasFlagYuv          = (dwFlags & DDPF_YUV)          == DDPF_YUV;
        hasFlagLuminance    = (dwFlags & DDPF_LUMINANCE)    == DDPF_LUMINANCE;
        
        isCompressed = hasFlagFourCC;

        // File should not specify both RGB and compression at the same time
        if (!isCompressed && !hasFlagRgb) {
            throw new IOException("Invalid compression values.");
        }
    }    
}
