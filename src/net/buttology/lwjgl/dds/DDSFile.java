/*
 * Copyright (C) 2020  Magnus Bull
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
import static org.lwjgl.opengl.EXTTextureCompressionS3TC.*;
import static org.lwjgl.opengl.EXTTextureCompressionRGTC.*;

/** 
 * Can load DirectDraw Surface (*.dds) texture files for use in LWJGL.
 * 
 * @author Magnus Bull
 * @version 2.0.0
 */
public class DDSFile {
	
	/**
	 * A 32-bit representation of the character sequence <code>"DDS "</code> which is the magic word for DDS files
	 */
	private static final int DDS_MAGIC = 0x20534444;
	
	/**
	 * Creates a new ByteBuffer and stores the data within it before returning it.
	 */
	private static ByteBuffer newByteBuffer(byte[] data) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(data.length).order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}

	//=======================================================================================
	
	/**
	 * Stores the magic word for the binary document read 
	 */
	private int					dwMagic;

	/**
	 * The header information for this DDS document 
	 */
	private DDSHeader			header;
	
	/**
	 * Arrays of bytes that contain the main surface image data 
	 */
	private List<ByteBuffer>	bdata;

	/** 
	 * Arrays of bytes that contain the secondary surface data, like mipmap levels
	 */
	private List<ByteBuffer>	bdata2;

	/** 
	 * The calculated size of the image */
	private int					imageSize;

	/** 
	 * The compression format for the current DDS document
	 */
	private int					format;

	/**
	 * Calculate the pitch or linear size of the document based on the given block size. Applies to block compression formats.
	 * @param blockSize
	 * @return
	 */
	private int calculatePitchBC(int width, int blockSize) {
		return Math.max(1, ((width + 3) / 4)) * blockSize;
	}
	
	public DDSFile() {}

	/**
	 * Loads a DDS file from the given file path.
	 * @param filePath
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public DDSFile(String filePath) throws IOException, FileNotFoundException {
		this(new File(filePath));
	}

	/**
	 * Loads a DDS file from the given file.
	 * @param file
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public DDSFile(File file) throws IOException, FileNotFoundException {
		if(!file.isFile()) {
			throw new FileNotFoundException("DDS: File not found " + file.getAbsolutePath());
		}
		FileInputStream fis = new FileInputStream(file);
		this.loadFile(fis);
	}
	
	/**
	 * Loads a DDS file from the given file path.
	 * @param file
	 * @throws IOException 
	 * @throws FileNotFoundException
	 */
	public void loadFile(String file) throws IOException, FileNotFoundException {
		this.loadFile(new File(file));
	}
	
	/**
	 * Loads a DDS file from the given file.
	 * @param file
	 * @throws IOException 
	 * @throws FileNotFoundException
	 */
	public void loadFile(File file) throws IOException, FileNotFoundException {
		FileInputStream fis = new FileInputStream(file);
		this.loadFile(fis);
	}
	
	/**
	 * Loads a DDS file.
	 * @param file
	 * @throws IOException
	 */
	public void loadFile(FileInputStream fis) throws IOException {
		if(fis.available() < 128) {
			fis.close();
			throw new IOException("Invalid file size. Must be at least 128 bytes.");
		}

		byte[] bMagic = new byte[4];
		fis.read(bMagic);
		dwMagic = newByteBuffer(bMagic).getInt();

		if(dwMagic != DDS_MAGIC) {
			fis.close();
			throw new IOException("Invalid DDS file. Magic number does not match.");
		}

		byte[] bHeader = new byte[124];
		fis.read(bHeader);
		header = new DDSHeader(newByteBuffer(bHeader));
		
		int blockSize = 16;
		
		switch(header.ddspf.sFourCC) {
		case "DXT1":
			format = GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
			blockSize = 8;
			break;
		case "DXT3":
			format = GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
			break;
		case "DXT5":
			format = GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
			break;
		case "ATI1":
			format = GL_COMPRESSED_RED_RGTC1_EXT;
			blockSize = 8;
			break;
		case "ATI2":
			format = GL_COMPRESSED_RED_GREEN_RGTC2_EXT;
			break;
		case "DX10":
			fis.close();
			throw new IOException("Unsupported format, uses DX10 extended header.");
		default:
			fis.close();
			throw new IOException("Surface format unknown or not supported: " + header.ddspf.sFourCC);
		}

		imageSize = calculatePitchBC(header.dwWidth, blockSize);

		int surfaceCount = header.hasCaps2CubeMap ? 6 : 1;
		int size = header.dwPitchOrLinearSize;
		
		bdata = new ArrayList<ByteBuffer>();
		bdata2 = new ArrayList<ByteBuffer>(); //TODO: Not properly implemented yet.

		for(int i = 0; i < surfaceCount; i++) {
			byte[] bytes = new byte[size];

			fis.read(bytes);
			bdata.add(newByteBuffer(bytes));

			if(header.hasFlagMipMapCount) {
				int size2 = Math.max(size / 4, blockSize);

				for(int j = 0; j < header.dwMipMapCount-1; j++) {
					byte[] bytes2 = new byte[size2];
					fis.read(bytes2);
					bdata2.add(newByteBuffer(bytes2));
					size2 = Math.max(size2 / 4, blockSize);
				}
			}
		}
		fis.close();
	}

	/**
	 * Get the width of this image document.
	 * @return width in pixels
	 */
	public int getWidth() {
		return header.dwWidth;
	}

	/**
	 * Get the height of this image document.
	 * @return height in pixels
	 */
	public int getHeight() {
		return header.dwHeight;
	}
	
	/**
	 * Gets the main surface data buffer.
	 * @return
	 */
	public ByteBuffer getBuffer() {
		return bdata.get(0);
	}

	/**
	 * Gets the main surface data buffer - usually the first full-sized image.
	 * @deprecated
	 * @return
	 */
	public ByteBuffer getMainBuffer() {
		return bdata.get(0);
	}

	/**
	 * Get the number of mipmap levels of this document.
	 * @return number of mipmaps
	 */
	public int getMipMapCount() {
		return this.header.dwMipMapCount;
	}

	/**
	 * Gets a specific level from the amount of mipmaps. <br>
	 * If specified outside the range of available mipmaps, the closest one is returned.
	 */
	public ByteBuffer getMipMapSurface(int level) {
		level = Math.min(Math.min(header.dwMipMapCount-2, level), Math.max(level, 0));
		return this.bdata2.get(level);
	}

	/**
	 * Get the surface buffer for the positive X direction of a cubemap. If this document is not a cubemap, null is returned.
	 * @return positive X buffer, or null if not cubemap.
	 */
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
		return this.bdata2.get((level*3)-1);
	}

	public ByteBuffer getCubeMapMipNYLevel(int level) {
		level = Math.min(Math.min(header.dwMipMapCount, level), Math.max(level, 0));
		return this.bdata2.get((level*4)-1);
	}

	public ByteBuffer getCubeMapMipPZLevel(int level) {
		level = Math.min(Math.min(header.dwMipMapCount, level), Math.max(level, 0));
		return this.bdata2.get((level*5)-1);
	}

	public ByteBuffer getCubeMapMipNZLevel(int level) {
		level = Math.min(Math.min(header.dwMipMapCount, level), Math.max(level, 0));
		return this.bdata2.get((level*6)-1);
	}

	/**
	 * Gets the compression format used for this DDS document.
	 * @return integer representing the format in LWJGL.
	 */
	public int getFormat() {
		return format;
	}

	public int getPitchOrLinearSize() {
		return imageSize;
	}

	/**
	 * Whether this DDS document is a cubemap or not.
	 * @return true if cubemap, else false
	 */
	public boolean isCubeMap() {
		return header.hasCaps2CubeMap;
	}
}
