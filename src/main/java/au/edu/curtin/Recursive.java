package au.edu.curtin;

/**
 * @author M T A ALSTON
 *         IT12351114
 *         18296839
 */
public class Recursive {
	public Recursive() {
		this.method1();
	}

	public void method1() {
		this.method2();
	}

	public void method2() {
		this.method3();
	}

	public void method3() {
		this.method4();
	}

	public void method4() {
		this.method2();
	}
}
