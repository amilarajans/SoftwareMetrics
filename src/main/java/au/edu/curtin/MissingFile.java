package au.edu.curtin;

/**
 * @author M T A ALSTON
 *         IT12351114
 *         18296839
 */
public class MissingFile {
	public MissingFile() {
		method1();
	}

	public void method1() {
		this.method2(10);
	}

	public void method2(int x) {
		method3("s");
	}

	public void method3(String s) {
		String val = String.valueOf(10);
	}
}
