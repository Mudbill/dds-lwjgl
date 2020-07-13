/*
 * Copyright (C) 2018  Magnus Bull
 *
 *  This file is part of dds-lwjgl.
 *
 *  dds-lwjgl is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  dds-lwjgl is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with dds-lwjgl.  If not, see <https://www.gnu.org/licenses/>. 
 */

package net.buttology.lwjgl.dds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/** 
 * A simple class for loading DirectDraw Surface (*.dds) files for LWJGL. DDS files have many variants and so this parser only supports the following:<br/>
 * <ul>
 * <li>Standard 124 byte headers (not extended D3D headers).</li>
 * <li>Compressed textures using DXT1, DXT3 and DXT5 formats.</li>
 * <li>Reads 2D textures with and without mipmaps (though they are discarded).</li>
 * <li>Reads Cubemap textures without mipmaps. Cubemaps with mipmaps appear offset.</li>
 * <li>Does not support volume maps.</li>
 * <li>Does not support legacy formats.</li>
 * </ul>
 * @author Magnus Bull
 * @version 2.0.0
 */
public class DDSFile {
	
	/** A 32-bit representation of the character sequence "DDS " which is the magic word for DDS files. */
	private static final int DDS_MAGIC = 0x20534444;
	
	private static final int GL_COMPRESSED_RGBA_S3TC_DXT1_EXT = 0x83f1;
	private static final int GL_COMPRESSED_RGBA_S3TC_DXT3_EXT = 0x83f2;
	private static final int GL_COMPRESSED_RGBA_S3TC_DXT5_EXT = 0x83f3;

	/** Creates a new ByteBuffer and stores the data within it before returning it. */
	public static ByteBuffer newByteBuffer(byte[] data) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(data.length).order(ByteOrder.nativeOrder());
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
	
	/**
	 * Print a debug message to the standard output. Only if printDebug is enabled.<br>
	 * This is a shorthand for System.out.printf.
	 * @param msg
	 * @param args
	 */
	private static void debug(String msg, Object... args) {
		if(printDebug) System.out.printf(msg, args);
	}
	
	public static boolean printDebug = false;

	/** Stores the magic word for the binary document read */
	private int 			dwMagic;

	/** The header information for this DDS document */
	private DDSHeader 		header;

	/** Arrays of bytes that contain the main surface image data */
	private List<ByteBuffer> bdata;

	/** Arrays of bytes that contain the secondary surface data, like mipmap levels */
	private List<ByteBuffer> bdata2;

	/** The calculated size of the image */
	private int 			imageSize;

	/** The compression format for the current DDS document */
	private int 			dxtFormat;
	
	/** Used for debugging to capture how long it takes to load a file */
	private long			startTime;

	/** Empty constructor */
	public DDSFile() {}

	/**
	 * Loads a DDS file from the given file path.
	 * @param filePath
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public DDSFile(String filePath) throws IOException {
		this.loadFile(new File(filePath));
	}

	/**
	 * Loads a DDS file from the given file.
	 * @param file
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public DDSFile(File file) throws IOException {
		this.loadFile(file);
	}
	
	/**
	 * Loads a DDS file from the given file path.
	 * @param file
	 * @throws IOException 
	 */
	public void loadFile(String file) throws IOException {
		this.loadFile(new File(file));
	}

	/**
	 * Loads a DDS file.
	 * @param file
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void loadFile(File file) throws IOException, FileNotFoundException {
		if(!file.isFile()) {
			throw new FileNotFoundException();
		}
		
		debug("Loading DDS file: '%s'\n", file.getAbsolutePath());
		if(printDebug) {
			this.startTime = System.currentTimeMillis(); 
		}
		
		bdata = new ArrayList<ByteBuffer>();
		bdata2 = new ArrayList<ByteBuffer>(); //TODO: Not properly implemented yet.

		FileInputStream fis = new FileInputStream(file);

		int totalByteCount = fis.available();
		debug("Total bytes: %d\n", totalByteCount);

		byte[] bMagic = new byte[4];
		fis.read(bMagic);
		dwMagic = newByteBuffer(bMagic).getInt();

		if(dwMagic != DDS_MAGIC) {
			fis.close();
			throw new IOException("Wrong magic word! This is not a valid DDS file.");
		}

		byte[] bHeader = new byte[124];
		fis.read(bHeader);
		header = new DDSHeader(newByteBuffer(bHeader));
		
		totalByteCount -= 128;

		int blockSize = 16;
		if(header.ddspf.sFourCC.equalsIgnoreCase("DXT1")) {
			dxtFormat = GL_COMPRESSED_RGBA_S3TC_DXT1_EXT;
			blockSize = 8;
		}
		else if(header.ddspf.sFourCC.equalsIgnoreCase("DXT3")) {
			dxtFormat = GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
		}
		else if(header.ddspf.sFourCC.equalsIgnoreCase("DXT5")) {
			dxtFormat = GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
		}
		else if(header.ddspf.sFourCC.equalsIgnoreCase("DX10")) {
			fis.close();
			throw new IOException("Uses DX10 extended header, which is not supported!");
		}
		else {
			fis.close();
			throw new IOException("Surface format unknown or not supported: "+header.ddspf.sFourCC);
		}

		imageSize = calculatePitch(blockSize);

		int surfaceCount = header.hasCaps2CubeMap ? 6 : 1;
		int size = header.dwPitchOrLinearSize;

		if(printDebug) {
			System.out.printf("Calculated pitch: %d\n", imageSize);
			System.out.printf("Included PitchOrLinearSize: %d\n", header.dwPitchOrLinearSize);
			System.out.printf("Mipmap count: %d\n", header.dwMipMapCount);
		}

		for(int i = 0; i < surfaceCount; i++) {
			byte[] bytes = new byte[size];

			debug("Getting main surface %d. Bytes: %d\n", i, bytes.length);

			fis.read(bytes);
			totalByteCount -= bytes.length;
			bdata.add(newByteBuffer(bytes));

			if(header.hasFlagMipMapCount) {
				int size2 = Math.max(size / 4, blockSize);

				for(int j = 0; j < header.dwMipMapCount-1; j++) {
					byte[] bytes2 = new byte[size2];

					debug("Getting secondary surface %d. Bytes: %d\n", j, bytes2.length);
					
					fis.read(bytes2);
					totalByteCount -= bytes2.length;
					bdata2.add(newByteBuffer(bytes2));
					size2 = Math.max(size2 / 4, blockSize);
				}
			}
		}

		debug("Remaining bytes: %d (%d)\n", fis.available(), totalByteCount);
		fis.close();
		debug("Time spent loading file: %dms", System.currentTimeMillis() - startTime);
	}

	public int getWidth() {
		return header.dwWidth;
	}

	public int getHeight() {
		return header.dwHeight;
	}

	/**
	 * Gets the main surface data buffer.
	 * @return
	 */
	public ByteBuffer getBuffer() {
		return getMainBuffer();
	}

	/**
	 * Gets the main surface data buffer - usually the first full-sized image.
	 * @return
	 */
	public ByteBuffer getMainBuffer() {
		return bdata.get(0);
	}

	public int getMipMapCount() {
		return this.header.dwMipMapCount;
	}

	/**
	 * Gets a specific level from the amount of mipmaps. If specified outside the range of available mipmaps, the closest one is returned.
	 */
	public ByteBuffer getMipMapLevel(int level) {
		level = Math.min(Math.min(header.dwMipMapCount-1, level), Math.max(level, 0));
		return this.bdata2.get(level);
	}

	public ByteBuffer getCubeMapPositiveX() {
		if(!header.hasCaps2CubeMap) return null;
		return bdata.get(0);
	}

	public ByteBuffer getCubeMapNegativeX() {
		if(!header.hasCaps2CubeMap) return null;
		return bdata.get(1);
	}

	public ByteBuffer getCubeMapPositiveY() {
		if(!header.hasCaps2CubeMap) return null;
		return bdata.get(2);
	}

	public ByteBuffer getCubeMapNegativeY() {
		if(!header.hasCaps2CubeMap) return null;
		return bdata.get(3);
	}

	public ByteBuffer getCubeMapPositiveZ() {
		if(!header.hasCaps2CubeMap) return null;
		return bdata.get(4);
	}

	public ByteBuffer getCubeMapNegativeZ() {
		if(!header.hasCaps2CubeMap) return null;
		return bdata.get(5);
	}

	public ByteBuffer getCubeMapMipPXLevel(int level) {
		level = Math.min(Math.min(header.dwMipMapCount, level), Math.max(level, 0));
		return this.bdata2.get((level*1)-1);
	}

	public ByteBuffer getCubeMapMipNXLevel(int level) {
		level = Math.min(Math.min(header.dwMipMapCount, level), Math.max(level, 0));
		return this.bdata2.get((level*2)-1);
	}

	public ByteBuffer getCubeMapMipPYLevel(int level) {
		level = Math.min(Math.min(header.dwMipMapCount, level), Math.max(level, 0));
		if(printDebug) System.out.println((level*3)-1);
		return this.bdata2.get((level*3)-1);
	}

	public ByteBuffer getCubeMapMipNYLevel(int level) {
		level = Math.min(Math.min(header.dwMipMapCount, level), Math.max(level, 0));
		if(printDebug) System.out.println((level*4)-1);
		return this.bdata2.get((level*4)-1);
	}

	public ByteBuffer getCubeMapMipPZLevel(int level) {
		level = Math.min(Math.min(header.dwMipMapCount, level), Math.max(level, 0));
		if(printDebug) System.out.println((level*5)-1);
		return this.bdata2.get((level*5)-1);
	}

	public ByteBuffer getCubeMapMipNZLevel(int level) {
		level = Math.min(Math.min(header.dwMipMapCount, level), Math.max(level, 0));
		if(printDebug) System.out.println((level*6)-1);
		return this.bdata2.get((level*6)-1);
	}

	public int getDXTFormat() {
		return dxtFormat;
	}

	public int getPitchOrLinearSize() {
		return imageSize;
	}

	public boolean isCubeMap() {
		return header.hasCaps2CubeMap;
	}

	private int calculatePitch(int blockSize) {
		return Math.max(1, ((header.dwWidth + 3) / 4)) * blockSize;
	}
}
