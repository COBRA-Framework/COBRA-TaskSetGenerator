package be.uantwerpen.idlab.cobra.tasksetgenerator.taskcreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class TaskCreator {
	
	static final String ARCHITECTURE 	= "architecture";
	static final String SPEED	 		= "CPUSpeedMHz";
	static final String CORES 			= "cores";
	static final double MAX_DEVIATION 	= 2.0;//percentage
	static final String LOCATION		= "location";
	static final String TASKSETLOCATION	= "tasksetlocation";
	static final String CACHEFLUSHTIME 	= "cacheFlushTime";
	
	private Map<String, List<String>> paramsTarget;
	private Map<String, List<String>> paramsTaskSet;
	private Map<String, List<String>> paramsBenchmark; //These parameters have no required parameters. The template is: name of parameter', {include or not include, when not defined include or no include}
	private String tasksetsFile;
	private List<WCET> usablePrograms;
	private List<TaskSet> tasksets;
	
	public TaskCreator(String parameterFile)
	{
		checkInput(parameterFile);
		this.usablePrograms = new ArrayList<WCET>();
	}
	
	private void checkInput(String fileName)
	{
		XMLParser parser = new XMLParser(fileName);
		boolean check=true;
		parser.parseFile();
		paramsTaskSet = parser.getParameters("taskSet");
		tasksetsFile = paramsTaskSet.get("name").get(0)+".xml";
		paramsBenchmark = parser.getParameters("benchMark");
		paramsTarget = parser.getParameters("TargetHardware");
		//check if all parameters are included
		if(!paramsTarget.containsKey(ARCHITECTURE))
			check = false;
		if(!paramsTarget.containsKey(SPEED))
			check = false;
		if(!paramsTarget.containsKey(CORES))
			check = false;
		if(!paramsBenchmark.containsKey(LOCATION))
			System.err.println("No benchmark specified");
		
		if(!check)
		{
			System.err.println("Some parameters are missing");
		}
	}
	//reads the description files and selects the programs based on the target platform and benchmark parameters
	private void readDescriptionFilesAndTaskSets()
	{
		File desFolder = new File(paramsBenchmark.get(LOCATION).get(0)+"/descriptionFiles");
		Map<String,List<String>> parameters = new HashMap<String, List<String>>();
		XMLParser parser;
		XMLParser parser2 = new XMLParser(this.tasksetsFile);
		WCET tupple = null;
		tasksets  = parser2.parseTaskset();
		
		if(!desFolder.isDirectory())
			System.err.println("No directory exist");
		
		FilenameFilter filter = new FilenameFilter() {
		        @Override
				public boolean accept(File directory, String fileName) {
		            return fileName.endsWith(".xml");
		        }
		        };
		
		for(File f:desFolder.listFiles(filter))
		{
			parser = new XMLParser(f);
			parameters = parser.parseDesriptionFile(f,paramsTarget.get(ARCHITECTURE).get(0));
			if(parameters == null)
				continue;
			tupple = new WCET(parameters.get("name").get(0),(Double.parseDouble(parameters.get("WCET").get(1)))/(Double.parseDouble(paramsTarget.get(SPEED).get(0))),parameters.get("path").get(0));
			tupple.setNumberOfExec(Integer.parseInt(parameters.get("min_n").get(0)));
			usablePrograms.add(tupple);
			
		}	
		//TODO: include the selection parameters for the benchmarks			
	}
	//Create a sequence of programs based on the input of the benchmark program available
	//input per benchmark: executime of single execution of the benchmark, and the minimum number of time a benchnmark should execute
	//Using a Integer Linear Program solver
	public void createProgramSequence()
	{
        LpSolver solver = new LpSolver(usablePrograms);
		List<WCET> programSequence = null;
        PrintWriter writer;
        String resultString = null;
        String tasksetsLocation = paramsTaskSet.get(TASKSETLOCATION).get(0)+"/"+tasksetsFile.substring(0, tasksetsFile.length()-4);
        double combinedWCET;
        File taskSetDir;
        long startTime = System.currentTimeMillis();
        long stopTime;
        try {
        	File dir = new File(tasksetsLocation);
        	dir.mkdir();
        	//TaskSet ts  = tasksets.get(20);
        	for(TaskSet ts:tasksets)
        	{
        		taskSetDir = new File(tasksetsLocation+"/"+ts.getTaskSetName());
        		taskSetDir.mkdir();
				writer = new PrintWriter(tasksetsLocation+"/"+ts.getTaskSetName()+"/taskset.xml");
				writer.print("<taskSet name=\""+ts.getTaskSetName()+"\" load=\""+ts.getLoad()+"\">\n");
				
		        for(Task t:ts.getTasks())
				{
		        	combinedWCET = 0;
        			programSequence = solver.findProgramCombinationI(t.getExe());
		        	for(WCET w:programSequence)
		        		combinedWCET+=w.getExecTime()*w.getNumberOfExec();
			        //	ILP++;
		        	//}
		        	resultString = "\t<task name=\""+t.getName()+"\" p=\""+t.getPeriod()+"\" d=\""+t.getDeadline()+"\" wanted_e=\""+t.getExe()+"\" real_e=\""+combinedWCET+"\" >\n";
		        	
		        	for(WCET w:programSequence)
		        	{
		        		if(w.getNumberOfExec()!=0)
		        			resultString = resultString+"\t\t<program name=\""+w.getProgramName()+"\" n=\""+w.getNumberOfExec()+"\" />\n";
		        	}
		        	resultString = resultString +"\t</task>\n";
		        	writer.print(resultString);
		        	copyPrograms(tasksetsLocation, programSequence, ts);	
				}  
		        writer.print("</taskSet>\n");
		        writer.close();
		        System.out.println("TASKSET "+ts.getTaskSetName()+" FINISHED");
        	}
        	stopTime=System.currentTimeMillis();
        	System.out.println("\n------REPORT------");
        	System.out.println("NUMBER OF TASK: "+tasksets.size()*tasksets.get(0).getTasks().size());
        	System.out.println("CALCULATION TIME: "+(double)(stopTime-startTime)/1000+" seconds");
        } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	public void copyPrograms(String taskSetLocation, List<WCET> programSequence, TaskSet ts)
	{
		//Copy all programs needed in the taskset into the folder
		File source,destination;
	
		for(WCET w:programSequence)
		{
			if(w.getNumberOfExec() > 0)
			{
				source = new File(paramsBenchmark.get(LOCATION).get(0)+"/"+w.getLocation());
				destination = new File(taskSetLocation+"/"+ts.getTaskSetName()+"/"+w.getProgramName()+"/");
				if (!destination.exists())
		        {
					try {
						FileUtils.copyDirectory(source, destination);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
			}
		}
	}
	
	public static void main(String[] args) 
	{		
		//input: user parameters and the generated tasksets
		TaskCreator gen;
		if(args.length==1)
			gen = new TaskCreator(args[0]);
		else
			gen = new TaskCreator("user.xml");
		
		gen.readDescriptionFilesAndTaskSets();
		gen.createProgramSequence();
	}

}
