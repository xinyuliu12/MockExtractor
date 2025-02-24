package org.davidespadini.mockextractor.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;

import org.davidespadini.mockextractor.dto.Variable;
import org.davidespadini.mockextractor.utils.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

public class Parser {
	String dir;
	String outputFile;
	private String[] classpath;
	
	
    public Parser(String dir, String outputFile, String[] deps) {
		this.dir = dir;
		this.outputFile = outputFile;
		this.classpath = deps;
	}
    
    public Parser(String dir) {
		this.dir = dir;
		this.outputFile = System.getProperty("java.io.tmpdir") + "/mockusages.csv";
	}
    
    @SuppressWarnings("rawtypes")
	private List<Variable> parse(String dir) {
    	
    	String[] srcDirs = FileUtils.getAllDirs(dir);
    	String[] javaFiles = FileUtils.getAllJavaFiles(dir);
		for(int i = 0; i< javaFiles.length; i++){

			System.out.println("F:" + javaFiles[i]);

		}
ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		
		Map options = JavaCore.getOptions();
	    JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
        parser.setCompilerOptions(options);
		
		Storage store = new Storage();
		
		parser.setEnvironment(classpath, srcDirs, null, true);
		parser.createASTs(javaFiles, null, new String[0], store, null);
 
		List<Variable> allVars = store.getVarsMocked();
		allVars.addAll(store.getVarsNotMocked());
			for(Variable v : allVars){

				System.out.println("v:" + v.getFilename());

			}			

		saveOnDisk(allVars);
		return allVars;
	}
   
  	public List<Variable> parse() throws IOException{
  		return parse(this.dir);
  	}
  	
  	private void saveOnDisk(List<Variable> vars) {
		
		try {
			 System.out.println(outputFile);
			PrintWriter ps = new PrintWriter(new FileWriter(outputFile));
			ps.println("filename;type;mocked");
			for(Variable var : vars) {
				ps.println(var.getFilename() + "," +
						   var.getType() + "," +
						   var.isMocked()
				);
			}
			ps.close();
		} catch (IOException e) {
			System.err.println("something failed: " + e.getMessage());
			System.out.println("Can not write on the output file!");
		}
	}
}
