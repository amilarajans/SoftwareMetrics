package au.edu.curtin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author M T A ALSTON
 *         IT12351114
 *         18296839
 */
public class Program {

	//const
	private static String BASE_PATH;
	private static String METHOD_NAME;
	private static String FILE_NAME;
	private static String SUPER_CLASS;
	protected final static String BASE_PACKAGE = "au/edu/curtin/";
	protected final static String CLASS_FILE_EXTENSION = ".class";

	//JVM instructions
	protected final static String STATIC_METHODS = "invokestatic";
	protected final static String NON_STATIC_METHODS = "invokevirtual";
	protected final static String NON_STATIC_METHODS_INTERFACE = "invokeinterface";
	protected final static String CONSTRUCTORS_OR_SUPERCLASS_METHODS = "invokespecial";

	//regex patterns
	protected final static String PACKAGE_NAME_REGEX = "(.+Method )(([a-zA-Z]+|/)+)(\\..+)";
	protected final static String METHOD_NAME_REGEX = "(.+Method )(([a-zA-Z0-9]+|/)+)(.+)";
	private static String METHOD_REGEX;

	//variables
	protected static boolean isRunning = false;
	protected static boolean isConstructor = false;
	protected static boolean isMethod = false;
	protected static boolean isFirstMethod = false;
	protected static String previouslyPrinted = "";

	protected Program() {
		//protected constructor to limit object creation
	}

	public Program(String basePath, String fileName, String methodName, boolean isNewRun) {
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

		try {
			Process javap = new ProcessBuilder()
					.command("javap", "-c", basePath + fileName)
					.start();
			javap.waitFor();
			new BufferedReader(new InputStreamReader(javap.getInputStream()))
					.lines()
					.forEach(Program::processClassFile);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected static void processClassFile(String line) {
//		System.out.println(line);
		if (line.contains("pop")) {
			FILE_NAME = SUPER_CLASS + CLASS_FILE_EXTENSION;
		}
		String currentInstruction = FILE_NAME.replace(CLASS_FILE_EXTENSION, ".");
		if (line.contains(CONSTRUCTORS_OR_SUPERCLASS_METHODS)) {
			Pattern pattern = Pattern.compile(PACKAGE_NAME_REGEX);
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				// group 2 for fully qualified class name
				if (matcher.group(2).contains(BASE_PACKAGE)) {
					new Program(BASE_PATH, matcher.group(2).replace(BASE_PACKAGE, "") + CLASS_FILE_EXTENSION, "", false);
				}
				if (isConstructor && !isRunning) {
					isRunning = true;
				}
				currentInstruction += matcher.group(2);
			}
		} else if (line.contains(METHOD_NAME) && !line.contains(NON_STATIC_METHODS) && !line.contains(STATIC_METHODS) && !line.contains(NON_STATIC_METHODS_INTERFACE)) {
			Pattern pattern = Pattern.compile(METHOD_REGEX);
			Matcher matcher = pattern.matcher(line);
			String localMethodName = currentInstruction + METHOD_NAME + "()";
			if (matcher.matches()) {
				if (isMethod && localMethodName.equals(SUPER_CLASS + "." + matcher.group(2)) && !isRunning) {
					isRunning = true;
					isFirstMethod = true;
				}
				currentInstruction += matcher.group(2);
			}
		} else if (isFirstMethod && (line.contains(NON_STATIC_METHODS) || line.contains(STATIC_METHODS) || line.contains(NON_STATIC_METHODS_INTERFACE))) {
			Pattern pattern = Pattern.compile(METHOD_NAME_REGEX);
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				if (isMethod && METHOD_NAME.equals(matcher.group(2)) && !isRunning) {
					isRunning = true;
				}
				currentInstruction += matcher.group(2) + "()";
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
