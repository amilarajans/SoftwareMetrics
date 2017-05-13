package au.edu.curtin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
	protected final static String OBJECT_PACKAGE = ".java/lang/Object";
	protected final static String CLASS_FILE_EXTENSION = ".class";
	protected final static String METHOD_PARAMS = "[.:V()]";
	protected final static String CONSTRUCTOR_PARAMS = "[.<>int:V\"()]";

	//JVM instructions
	protected final static String STATIC_METHODS = "invokestatic";
	protected final static String NON_STATIC_METHODS = "invokevirtual";
	protected final static String NON_STATIC_METHODS_INTERFACE = "invokeinterface";
	protected final static String CONSTRUCTORS_OR_SUPERCLASS_METHODS = "invokespecial";
	protected final static String RETURN_TO_PARENT = "pop";

	//regex patterns
	protected final static String PACKAGE_NAME_REGEX = "(.+Method )((\\w+|/)+)(\\..+)";
	protected final static String METHOD_NAME_REGEX = "(.+Method )((\\w+|/)+)(.+)";
	protected final static String FIRST_METHOD_NAME_REGEX = "(.+) ((\\w+\\(\\))|(\\w+\\(.+\\)));";

	//variables
	protected static boolean isRunning = false;
	protected static boolean isConstructor = false;
	protected static boolean isMethod = false;
	protected static boolean isFirstMethod = false;
	protected static boolean isAbstractMethod = false;
	protected static boolean isNextClass = false;
	protected static String previouslyPrinted = "";
	protected static String nextClass = "";
	protected static HashMap<Character, String> map;
	protected static List<String> methodStack;
	protected static Stack<String> executionMethodStack;

	protected Program() {
		//protected constructor to limit object creation
	}

	public Program(String basePath, String fileName, String methodName, boolean isNewRun) {
		BASE_PATH = basePath;
		if (isNewRun) {
			METHOD_NAME = methodName;
			SUPER_CLASS = fileName.replace(CLASS_FILE_EXTENSION, "");
		}
		FILE_NAME = fileName;
		if ((METHOD_NAME == null || !METHOD_NAME.isEmpty())) {
			isMethod = true;
		}
		if (METHOD_NAME == null || METHOD_NAME.isEmpty()) {
			isConstructor = true;
		}

		methodStack = new ArrayList<>();
		executionMethodStack = new Stack<>();
		initializeByteCodeInstructions();

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

		executionMethodStack.stream().forEach(System.out::println);
	}

	private void initializeByteCodeInstructions() {
		map = new HashMap<>();
		map.put('B', "byte");
		map.put('C', "char");
		map.put('D', "double");
		map.put('F', "float");
		map.put('I', "int");
		map.put('J', "long");
		map.put('S', "short");
		//no need to keep void
		map.put('V', "void");
		map.put('Z', "boolean");
	}

	protected static void processClassFile(String line) {

		if (isNextClass) {
			isNextClass = false;
			new Program(BASE_PATH, nextClass, "", false);
		}

		//change the file name to parent class if it contains pop
		if (line.contains(RETURN_TO_PARENT)) {
			FILE_NAME = SUPER_CLASS + CLASS_FILE_EXTENSION;
		}

		//get the class name
		String currentInstruction = FILE_NAME.replace(CLASS_FILE_EXTENSION, ".");

		if (isMethod) {
			if (line.contains(CONSTRUCTORS_OR_SUPERCLASS_METHODS)) {
				currentInstruction = getConstructorInstructions(line, currentInstruction);
			} else if (line.contains(METHOD_NAME)) {
				Pattern pattern = Pattern.compile(FIRST_METHOD_NAME_REGEX);
				Matcher matcher = pattern.matcher(line);
				String localMethodName = currentInstruction + METHOD_NAME;
				if (matcher.matches()) {
					String method = matcher.group(2);
					if (isMethod && localMethodName.equals(SUPER_CLASS + "." + method) && !isRunning) {
						isRunning = true;
						isFirstMethod = true;
					}
					currentInstruction += method;
				}
			} else if (isFirstMethod && (line.contains(NON_STATIC_METHODS) || line.contains(STATIC_METHODS) || line.contains(NON_STATIC_METHODS_INTERFACE))) {
				currentInstruction = getMethodInstructions(line, currentInstruction);
			} else {
				currentInstruction = "";
			}
		}
		if (isConstructor) {
			if (line.contains(CONSTRUCTORS_OR_SUPERCLASS_METHODS)) {
				currentInstruction = getConstructorInstructions(line, currentInstruction);
			} else if (line.contains(NON_STATIC_METHODS) || line.contains(STATIC_METHODS) || line.contains(NON_STATIC_METHODS_INTERFACE)) {
				currentInstruction = getMethodInstructions(line, currentInstruction);
			} else {
				currentInstruction = "";
			}
		}

		if (isRunning) {
			if (!currentInstruction.isEmpty() && !previouslyPrinted.equals(currentInstruction)) {
				previouslyPrinted = currentInstruction;
				if (methodStack.contains(currentInstruction)) {
					System.out.println(currentInstruction + "[recursive]");
				} else if (isAbstractMethod) {
					System.out.println(currentInstruction + "[abstract]");
				} else {
					if (currentInstruction.contains(BASE_PACKAGE)) {
						System.out.println(getConstructorName(currentInstruction));
						System.out.println("Object constructor()");
					} else {
						if (!currentInstruction.contains(OBJECT_PACKAGE)) {
							System.out.println(currentInstruction);
						}
					}
				}
				methodStack.add(previouslyPrinted);
			}
		}

	}

	private static String getConstructorName(String currentInstruction) {
		String[] constructorSplit = currentInstruction.split("\\.");
		String constructorParams = constructorSplit[1].substring(constructorSplit[1].indexOf("("), constructorSplit[1].length());
		String constructorClass = constructorSplit[1].replace(BASE_PACKAGE, "").replace(constructorParams, "");
		return constructorClass + " constructor" + constructorParams;
	}

	private static String getMethodInstructions(String line, String currentInstruction) {
		Pattern pattern = Pattern.compile(METHOD_NAME_REGEX);
		Matcher matcher = pattern.matcher(line);
		if (matcher.matches()) {
			if (isMethod && METHOD_NAME.equals(matcher.group(3)) && !isRunning) {
				isRunning = true;
			}
			String method;
			String group = matcher.group(4);
			if (group.startsWith(".")) {
				String[] methodSplit = group.replace(".", "").split(":");
				method = methodSplit[0] + getReadableSignature(methodSplit[1].replaceAll(METHOD_PARAMS, ""));
				isAbstractMethod = true;
			} else {
				method = matcher.group(3) + getReadableSignature(group.replaceAll(METHOD_PARAMS, ""));
				isAbstractMethod = false;
			}
			currentInstruction += method;
		}
		return currentInstruction;
	}

	private static String getReadableSignature(String signature) {
		if (signature.startsWith("L")) {
			String[] split = signature.split("/");
			return "(" + split[split.length - 1].replace(";", "") + ")";
		} else {
			return "(" + signature.chars()
					.mapToObj(x -> (char) x)
					.map(map::get).collect(Collectors.joining(", ")) + ")";
		}
	}

	private static String getConstructorInstructions(String line, String currentInstruction) {
		Pattern pattern = Pattern.compile(PACKAGE_NAME_REGEX);
		Matcher matcher = pattern.matcher(line);
		if (matcher.matches()) {
			currentInstruction += matcher.group(2) + getReadableSignature(matcher.group(4).replaceAll(CONSTRUCTOR_PARAMS, ""));
			// group 2 for fully qualified class name
			if (matcher.group(2).contains(BASE_PACKAGE)) {
				nextClass = matcher.group(3) + CLASS_FILE_EXTENSION;
				isNextClass = true;
			}
			if (isConstructor && !isRunning) {
				isRunning = true;
			}
		}
		return currentInstruction;
	}
}
