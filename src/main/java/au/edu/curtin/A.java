package au.edu.curtin;

/**
 * @author M T A ALSTON
 *         IT12351114
 *         18296839
 */
public class A {
	public A() {
		this.method1();
		new B(10l);
	}

	public void method1() {
		this.method2(10);
		this.method2(10, 12.6);
	}

	public void method2(int x) {
		method3("s");
	}

	public void method2(int x, double y) {
		method3("S");
	}

	public void method3(String s) {

	}
}
