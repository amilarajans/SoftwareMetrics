package au.edu.curtin;

/**
 * @author M T A ALSTON
 *         IT12351114
 *         18296839
 */
public class Assignment2 {
	public static void main(String[] args) {
		args = new String[3];
		args[0] = "D:\\dev\\Projects\\SoftwareMetrics\\src\\main\\java\\au\\edu\\curtin\\";
		args[1] = "A.class";
		args[2] = "method1()";

		if (args.length >= 2) {
			new Program(args[0], args[1], args[2], true);
		} else {
			System.out.println("Usage:");
			System.out.println("\tAssignment2 <Base Path> <Class Name> [Method Name]");
			System.out.println("Example:");
			System.out.println("\tAssignment2 /home/user/desktop/ A.class method1()");
			System.out.println("Or");
			System.out.println("\tAssignment2 /home/user/desktop/ A.class ");
		}
	}

}
