package de.deverado.framework.core;


public class H {
	private H() {}

	public static void assertIt(boolean b) {
		if (!b) throw new AssertionFailure();
	}
}
