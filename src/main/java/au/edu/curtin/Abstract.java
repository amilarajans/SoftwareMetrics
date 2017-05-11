package au.edu.curtin;

/**
 * @author M T A ALSTON
 *         IT12351114
 *         18296839
 */
public class Abstract {
	public Abstract() {

	}

	public void method1() {
		method2();
	}

	public void method2() {
		AbstractClass aClass = new AbstractClassImpl();
		aClass.abstractMethod1();
	}

}
