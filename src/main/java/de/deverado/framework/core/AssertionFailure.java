package de.deverado.framework.core;

public class AssertionFailure extends RuntimeException {

	private static final long serialVersionUID = 8131413110698825781L;

	public AssertionFailure() {
		super();
	}

	public AssertionFailure(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AssertionFailure(String arg0) {
		super(arg0);
	}

	public AssertionFailure(Throwable arg0) {
		super(arg0);
	}

}
