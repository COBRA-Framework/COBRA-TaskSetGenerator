package be.uantwerpen.idlab.tasksetgenerator.ExecutableTaskGenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CreateTaskCombination {
	
	static final String NAME 	= "name";
	static final String LOCATION 	= "tasksetlocation";
	static final String COMPILER 	= "cCompiler";
	static final String CFLAGS_TASKS 	= "cflagsTasks";
	static final String CFLAGS_BENCHMARKPROGRAMS 	= "cflagsBenchmarkPrograms";
	static final String PREFIX 	= "executablePrefix";

	
	private Map<String, List<String>> paramsTarget;
	private Map<String, List<String>> paramsTaskSet;
	private Map<String, List<String>> paramsBenchmark;
	private String header, footer, body;
	
	public CreateTaskCombination(String inputParameters)
	{
		checkInput(inputParameters);
		header="";
		footer="";
		body="";
	}
	private void checkInput(String fileName)
	{
		XMLParser parser = new XMLParser(fileName);
		boolean check=true;
		parser.parseFile();
		paramsTaskSet = parser.getParameters("taskSet");
		paramsBenchmark = parser.getParameters("benchMark");
		paramsTarget = parser.getParameters("TargetHardware");
		//check if all parameters are included
		if(!paramsTaskSet.containsKey(NAME))
			check = false;
		if(!paramsTarget.containsKey(COMPILER))
			check = false;
		if(!check)
		{
			System.err.println("Some parameters are missing");
		}
	}
	public void readTemplate()
	{
		File f = new File("template");
		FileInputStream in;
		BufferedReader reader;
		String line;
		try {
			in = new FileInputStream(f);
			reader = new BufferedReader(new InputStreamReader(in));
			
			while ((line = reader.readLine()) != null) {
				if(line.contains("HEADER"))
					header = readTemplateSection(reader,line);
				else if(line.contains("FOOTER"))
					footer = readTemplateSection(reader,line);
				else if(line.contains("BODY"))
					body = readTemplateSection(reader,line);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private String readTemplateSection(BufferedReader reader, String keyWord)
	{
		String line="";
		String temp="";
		
		try {
			temp = reader.readLine();
			while (!temp.contains(keyWord)) {
				line += temp; 
				line +="\n";
				temp = reader.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return line;
	}
	public void scanTaskSet()
	{
		File f = new File(paramsTaskSet.get(LOCATION).get(0)+"/"+paramsTaskSet.get(NAME).get(0));
		String genMakefile = "all:\n";
		System.out.println(f.getName());
		FileFilter filter = new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if(pathname.isDirectory()&&pathname.getName().contains("TaskSet"))
	        		return true;
	        	return false;
			}
		};
		
		for(File t:f.listFiles(filter))
		{
			cFilePreparation(t);
			createProgramCombination(t);
			genMakefile += createMakefile(t);
		}
		createGeneralMakefile(genMakefile);
		
	}
	private void createGeneralMakefile(String rules)
	{
		BufferedWriter writer =null;
		try{
			File file = new File(paramsTaskSet.get(LOCATION).get(0)+"/"+paramsTaskSet.get(NAME).get(0)+"/Makefile");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(rules);
			writer.close();
			

		}catch (IOException excep)  {
			// TODO Auto-generated catch block
			excep.printStackTrace();
		} 
	}
	private void cFilePreparation(File taskSetFolder)
	{
		BufferedReader reader;
		InputStream in;
		StringBuilder out;
		BufferedWriter writer;
		String name;
		
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if(pathname.isDirectory())
	        		return true;
	        	return false;
			}
		};
		
		for(File folder:taskSetFolder.listFiles(filter))
		{
			try {
				name = folder.getName();
				in = new FileInputStream(new File(taskSetFolder.getAbsolutePath()+"/"+name+"/"+name+".c"));
				reader = new BufferedReader(new InputStreamReader(in));
		        out = new StringBuilder();
		        String line;
		        boolean remove = false;
		        while ((line = reader.readLine()) != null) {
		        	if(line.contains("int main")&&line.contains(";"))
		        		continue;
		        	if(line.contains("int main"))
		        	{
		        		remove = true;
		        	}
		        	if(!remove)
		        		out.append(line+"\n");
		        	if(line.contains("}")&&remove)
		        		remove=false; 		
		        }
		        reader.close();			       
		        File file = new File(taskSetFolder.getAbsolutePath()+"/"+name+"/"+name+".c");
		        writer = new BufferedWriter(new FileWriter(file));
		        writer.write(out.toString());
		        writer.close();
			}catch (IOException excep)  {
				// TODO Auto-generated catch block
				excep.printStackTrace();
			} 
		}
	
	}
	private void createProgramCombination(File taskSetFolder)
	{		
		XMLParser parser = new XMLParser(new File(taskSetFolder.getAbsolutePath()+"/taskset.xml"));
		TaskSet taskset = parser.parseTasks();
		BufferedWriter writer;
	
		File taskCFile;
		try {
			for(Task t:taskset.getTasks())
			{
				taskCFile = new File(taskSetFolder.getAbsolutePath()+"/"+t.getName()+".c");
				writer = new BufferedWriter(new FileWriter(taskCFile));
				writer.write(header);
				writer.write("\tint i;\n");
				for(Entry<String,Integer> e:t.getPrograms().entrySet())
				{
					if(e.getValue()>1)
					{
						writer.write("\tfor(i = 0; i <"+e.getValue()+"; i++)\n\t{\n");
						writer.write("\t\t"+e.getKey()+"_init();\n");
						writer.write("\t\t"+e.getKey()+"_main();\n\t}\n");
					}
					else
					{
						writer.write("\t"+e.getKey()+"_init();\n");
						writer.write("\t"+e.getKey()+"_main();\n");
					}
					writer.write(body);
				}
				writer.write(footer);
				writer.close();
		       
			}
			
		 } catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	}
	private String createMakefile(File taskSetFolder)
	{
		BufferedWriter writer;
		String vpath="";
		String deps="";
		String obj="";
		String name="";
		String all="all:";
		String rules="";
		String generalMakefile = "";
		String taskName = "";
		
		FilenameFilter headerFilter = new FilenameFilter() {

			@Override
			public boolean accept(File file, String name) {
				if(name.contains(".h"))
					return true;
				return false;
			}
			
		};
		FilenameFilter cfileFilter = new FilenameFilter() {

			@Override
			public boolean accept(File file, String name) {
				if(name.contains(".c"))
					return true;
				return false;
			}
			
		};
		FilenameFilter taskFilter = new FilenameFilter() {

			@Override
			public boolean accept(File file, String name) {
				if(name.contains("Task")&&name.contains(".c"))
					return true;
				return false;
			}
			
		};
		FileFilter isFolder = new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if(pathname.isDirectory())
					return true;
				return false;
			}
		};
		
		generalMakefile="\t cd "+taskSetFolder.getName()+" && $(MAKE) all\n";
		
		for(File folder:taskSetFolder.listFiles(isFolder))
		{
		   vpath+=folder.getName()+":";
		   for(File hFile:folder.listFiles(headerFilter))
		   {
			   deps+=folder.getName()+"/"+hFile.getName()+" ";  
		   }
		   for(File cFile:folder.listFiles(cfileFilter))
		   {
			   name = cFile.getName().replace(".c", ".o");
			   obj+=name+" ";
		   }
		}
		vpath=vpath.substring(0, vpath.length()-1);
		deps=deps.substring(0, deps.length()-1);
		obj=obj.substring(0, obj.length()-1);
		
		for(File task:taskSetFolder.listFiles(taskFilter))
		{
			taskName = (task.getName().split("\\."))[0];
			all += " "+taskName;
			rules += taskName+": $(OBJ)\n\t$(CC) $(CFLAGS) -o "+paramsTarget.get(PREFIX).get(0)+taskName.substring(4, taskName.length())+" $^ "+task.getName()+"\n";
		}
	 
		try{
			File file = new File(taskSetFolder.getAbsolutePath()+"/Makefile");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("CC="+paramsTarget.get(COMPILER).get(0)+"\n");
			//writer.write("CFLAGS= -Wall -shared -fPIC -Wl,-soname,libtask.so.1 -I.\n");
			writer.write("CFLAGS= "+paramsTarget.get(CFLAGS_TASKS).get(0)+"\n");
			writer.write("ODIR=obj\n");
			writer.write("VPATH = "+vpath+"\n\n");
			writer.write("_DEPS = "+deps+"\n");
			writer.write("DEPS = $(patsubst %,$/%,$(_DEPS))\n\n");
			writer.write("_OBJ = "+obj+"\n");
			writer.write("OBJ = $(patsubst %,$(ODIR)/%,$(_OBJ))\n\n");
			//writer.write("$(ODIR)/%.o: %.c $(DEPS)\n\t$(CC) -fPIC -c -o $@ $< \n\n");
			writer.write("$(ODIR)/%.o: %.c $(DEPS)\n\t$(CC) "+paramsTarget.get(CFLAGS_BENCHMARKPROGRAMS).get(0)+" -c -o $@ $< \n\n");
			writer.write(all+"\n\n");
			writer.write(rules+"\n");
			writer.write(".PHONY: clean\n\nclean:\n\trm -f $(ODIR)/*.o\n\n");
			writer.write("-include $(shell mkdir obj 2>NUL) $(wildcard obj/*)");
			writer.close();
			

		}catch (IOException excep)  {
			excep.printStackTrace();
		} 
		return generalMakefile;
	}
	public static void main(String[] args)
	{
		CreateTaskCombination reader;
		if(args.length==1)
			reader = new CreateTaskCombination(args[0]);
		else
			reader = new CreateTaskCombination("user.xml");
		reader.readTemplate();
		reader.scanTaskSet();
	}

}
