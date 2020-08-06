package net.buttology.lwjgl.dds;

public class DDSException extends Exception {

	private static final long serialVersionUID = -8223917037948991116L;
	
	public DDSException(String reason) {
		super(reason);
	}
	
	public DDSException() {
		super();
	}

}
