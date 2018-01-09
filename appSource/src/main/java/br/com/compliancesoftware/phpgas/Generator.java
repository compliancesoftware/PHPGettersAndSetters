package br.com.compliancesoftware.phpgas;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Generator {
	
	private String mode = "gas";
	private String filePath = "C:/DEV/personal/EclipseProjects/generateGettersAndSettersPHP/workspace/PHPGettersAndSetters/testFile/teste.php";
	
	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	private static String getterTemplate = "\t\tpublic function get{0}() {\n\t\t\treturn $this->{1};\n\t\t}";
	private static String setterTemplate = "\t\tpublic function set{0}(${1}) {\n\t\t\t$this->{1} = ${1};\n\t\t}";
	
	private static String removeSpecials(String variable) {
		String[] specials = {"_","-"};
		
		String var = variable;
		
		for(String special : specials) {
			if(variable.contains(special)) {
				String[] varSplit = variable.split(special);
				for(String item : varSplit) {
					var += upperFirst(item);
				}
				var = lowerFirst(var);
			}
		}
		return var;
	}
	
	private static String lowerFirst(String variable) {
		String var = variable.substring(1);
		String firstLetter = "" + variable.charAt(0);
		var = firstLetter.toLowerCase() + var;
		
		return var;
	}
	
	private static String upperFirst(String variable) {
		String var = variable.substring(1);
		String firstLetter = "" + variable.charAt(0);
		var = firstLetter.toUpperCase() + var;
		
		return var;
	}
	
	private static String generateSetter(String variable) {
		String varWithoutSpecials  = removeSpecials(variable);
		String var = upperFirst(varWithoutSpecials);
		String setter = setterTemplate.replace("{0}", var).replace("{1}", varWithoutSpecials);
		
		return setter;
	}
	
	private static String generateGetter(String variable) {
		String varWithoutSpecials  = removeSpecials(variable);
		String var = upperFirst(varWithoutSpecials);
		String getter = getterTemplate.replace("{0}", var).replace("{1}", varWithoutSpecials);
		
		return getter;
	}
	
	private static ArrayList<String> getFileLines(String filePath) {
		ArrayList<String> fileLines = new ArrayList<String>();
		
		try {
			FileReader reader = new FileReader(new File(filePath));
			BufferedReader br = new BufferedReader(reader);
			
			String line = br.readLine();
			while(line != null) {
				fileLines.add(line);
				line = br.readLine();
			}
			
			reader.close();
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found: "+filePath);
		} catch (IOException e) {
			System.out.println("Error trying to close file.");
		}
		
		return fileLines;
	}
	
	private static ArrayList<String> getAttributes(String filePath) {
		ArrayList<String> attributes = new ArrayList<String>();
		
		ArrayList<String> fileLines = getFileLines(filePath);
		for(String line : fileLines) {
			if((line.contains("private") || line.contains("public")) && line.contains("$")) {
				int startIndex = line.indexOf("$") + 1;
				int endIndex = line.indexOf("=") - 1;
				
				String var = line.substring(startIndex, endIndex);
				attributes.add(var);
			}
		}
		
		return attributes;
	}
	
	private static String getOutputPath(String filePath) {
		if(filePath.endsWith(".php")) {
			String fileOutPut = filePath.replace(".php", "-output.php");
			return fileOutPut;
		}
		else {
			System.out.println("This is not a php file: Please, check extension and structure.");
			return null;
		}
	}
	
	private static ArrayList<String> generateSetters(String filePath) {
		ArrayList<String> attrs = getAttributes(filePath);
		ArrayList<String> setters = new ArrayList<String>();
		
		for(String attr : attrs) {
			String setter = generateSetter(attr);
			setters.add(setter);
		}
		
		return setters;
	}
	
	private static ArrayList<String> generateGetters(String filePath) {
		ArrayList<String> attrs = getAttributes(filePath);
		ArrayList<String> getters = new ArrayList<String>();
		
		for(String attr : attrs) {
			String getter = generateGetter(attr);
			getters.add(getter);
		}
		
		return getters;
	}
	
	private static ArrayList<String> generate(String mode, String filePath) {
		ArrayList<String> generated = new ArrayList<String>();
		
		if(mode.equals("gas")) {
			ArrayList<String> getters = generateGetters(filePath);
			ArrayList<String> setters =  generateSetters(filePath);
			
			for(int i = 0;i < getters.size();i++) {
				generated.add(getters.get(i));
				generated.add(setters.get(i));
			}
		}
		else if(mode.equals("get")) {
			ArrayList<String> getters = generateGetters(filePath);
			
			for(int i = 0;i < getters.size();i++) {
				generated.add(getters.get(i));
			}
		}
		else if(mode.equals("set")) {
			ArrayList<String> setters =  generateSetters(filePath);
			
			for(int i = 0;i < setters.size();i++) {
				generated.add(setters.get(i));
			}
		}
		else {
			System.out.println("Define a mode: 'gas' | 'get' | 'set'");
		}
		return generated;
	}
	
	private static String[] splitFile(String filePath) {
		ArrayList<String> fileLines = getFileLines(filePath);
		int lineCount = 0;
		int lastLineIndex = 1;
		for(String line : fileLines) {
			lineCount++;
			if((line.contains("private") || line.contains("public")) && line.contains("$")) {
				lastLineIndex = lineCount;
			}
		}
		lastLineIndex++;
		
		String firstPart = "";
		String secondPart = "";
		
		lineCount = 0;
		for(String line : fileLines) {
			lineCount++;
			if(lineCount < lastLineIndex) {
				firstPart += line + "\n";
			}
			else {
				secondPart += line + "\n";
			}
		}
		
		String[] fileParts = {firstPart, secondPart};
		return fileParts;
	}
	
	private static void generateFile(String filepath, String content) {
		try {
			String outputPath = getOutputPath(filepath);
			FileWriter writer = new FileWriter(new File(outputPath));
			
			BufferedWriter bw = new BufferedWriter(writer);
			bw.write(content);
			
			bw.close();
			writer.close();
		} catch (IOException e) {
			System.out.println("Error trying to write file:\n");
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		try {
			System.out.println("Generating...");
			
			Generator gen = new Generator();
			
			for(String arg : args) {
				if(arg.contains("--mode")) {
					String mode = arg.split("=")[1];
					gen.setMode(mode);
				}
				else if(arg.contains("--file")) {
					String file = arg.split("=")[1];
					gen.setFilePath(file);
				}
			}
			
			ArrayList<String> generated = generate(gen.getMode(), gen.getFilePath());
			
			String linesToAppend = "\n";
			for(String function : generated) {
				linesToAppend += function + "\n";
			}
			
			String fileParts[] = splitFile(gen.getFilePath());
			String content = fileParts[0] + linesToAppend + fileParts[1];
			generateFile(gen.getFilePath(), content);
			
			System.out.println("File generated...");
		} catch(Exception e) {
			
			System.out.println("Error trying to generate getters and/or setters:\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
