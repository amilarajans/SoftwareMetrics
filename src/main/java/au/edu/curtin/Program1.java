package au.edu.curtin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author M T A ALSTON
 *         IT12351114
 *         18296839
 */
public class Program1 {

	//const
	private static String BASE_PATH;
	private static String METHOD_NAME;
	private static String FILE_NAME;
	private static String SUPER_CLASS;
	protected final static String BASE_PACKAGE = "au/edu/curtin/";
	protected final static String CLASS_FILE_EXTENSION = ".class";
	protected final static String METHOD_BRACKETS = "()";
	protected final static String METHOD_VOID_RETURN = "[:V]";

	//JVM instructions
	protected final static String STATIC_METHODS = "invokestatic";
	protected final static String NON_STATIC_METHODS = "invokevirtual";
	protected final static String NON_STATIC_METHODS_INTERFACE = "invokeinterface";
	protected final static String CONSTRUCTORS_OR_SUPERCLASS_METHODS = "invokespecial";
	protected final static String RETURN_TO_PARENT = "pop";

	//regex patterns
	protected final static String PACKAGE_NAME_REGEX = "(.+Method )((\\w+|/)+)(\\..+)";
	protected final static String METHOD_NAME_REGEX = "(.+Method )((\\w+|/)+)(.+)";
	//	protected final static String METHOD_NAME_REGEX = "(.+) ((\\w+\\(\\))|(\\w+\\(.+\\)));";
	private static String METHOD_REGEX;

	//variables
	protected static boolean isRunning = false;
	protected static boolean isConstructor = false;
	protected static boolean isMethod = false;
	protected static String previouslyPrinted = "";
	protected static HashMap<String, String> map;

	protected Program1() {
		//protected constructor to limit object creation
	}

	public Program1(String basePath, String fileName, String methodName, boolean isNewRun) {
		BASE_PATH = basePath;
		if (isNewRun) {
			METHOD_NAME = methodName;
			METHOD_REGEX = String.format("(.+)((%s\\(\\))|(%s\\(.+\\)));", METHOD_NAME, METHOD_NAME);
			SUPER_CLASS = fileName.replace(CLASS_FILE_EXTENSION, "");
		}
		FILE_NAME = fileName;
		if ((METHOD_NAME == null || !METHOD_NAME.isEmpty())) {
			isMethod = true;
		}
		if (METHOD_NAME == null || METHOD_NAME.isEmpty()) {
			isConstructor = true;
		}

		initializeByteCodeInstructions();

		try {
			Process javap = new ProcessBuilder()
					.command("javap", "-c", basePath + fileName)
					.start();
			javap.waitFor();
			new BufferedReader(new InputStreamReader(javap.getInputStream()))
					.lines()
					.forEach(Program1::processClassFile);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void initializeByteCodeInstructions() {
		map = new HashMap<>();
		map.put("B", "byte");
		map.put("C", "char");
		map.put("D", "double");
		map.put("F", "float");
		map.put("I", "int");
		map.put("J", "long");
		map.put("S", "short");
		map.put("V", "void");
		map.put("Z", "boolean");
	}

	protected static void processClassFile(String line) {
		//change the file name to parent class if it contains pop
		if (line.contains(RETURN_TO_PARENT)) {
			FILE_NAME = SUPER_CLASS + CLASS_FILE_EXTENSION;
		}

		//get the class name
		String currentInstruction = FILE_NAME.replace(CLASS_FILE_EXTENSION, ".");

		//filter the method names
//		Pattern methodPattern = Pattern.compile(METHOD_NAME_REGEX);
//		Matcher methodMatcher = methodPattern.matcher(line);
//		if (methodMatcher.matches()) {
//			System.out.println(line);
//			methods.add(currentInstruction + methodMatcher.group(2));
//		}

		if (line.contains(CONSTRUCTORS_OR_SUPERCLASS_METHODS)) {
			Pattern pattern = Pattern.compile(PACKAGE_NAME_REGEX);
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				// group 2 for fully qualified class name
				if (matcher.group(2).contains(BASE_PACKAGE)) {
					new Program1(BASE_PATH, matcher.group(2).replace(BASE_PACKAGE, "") + CLASS_FILE_EXTENSION, "", false);
				}
				if (isConstructor && !isRunning) {
					isRunning = true;
				}
				currentInstruction += matcher.group(2);
			}
		} else if (line.contains(NON_STATIC_METHODS) || line.contains(STATIC_METHODS) || line.contains(NON_STATIC_METHODS_INTERFACE)) {
			Pattern pattern = Pattern.compile(METHOD_NAME_REGEX);
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				if (isMethod && METHOD_NAME.equals(matcher.group(3)) && !isRunning) {
					isRunning = true;
				}
				currentInstruction += matcher.group(3) + matcher.group(4).replaceAll(METHOD_VOID_RETURN, "");
			}
		} else {
			currentInstruction = "";
		}
		if (isRunning) {
			if (!currentInstruction.isEmpty() && !previouslyPrinted.equals(currentInstruction)) {
				previouslyPrinted = currentInstruction;
				System.out.println(currentInstruction);
			}
		}

	}
}
