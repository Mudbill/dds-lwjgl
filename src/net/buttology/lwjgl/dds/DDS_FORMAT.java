package net.buttology.lwjgl.dds;

public enum DDS_FORMAT {
	
	DXGI_FORMAT_UNKNOWN(0),
	DXGI_FORMAT_R32G32B32A32_TYPELESS(123);

	private int dwFourCC;
	
	private DDS_FORMAT(int dwFourCC) {
		this.dwFourCC = dwFourCC;
	}
	
	public int getFourCC() {
		return this.dwFourCC;
	}
}
