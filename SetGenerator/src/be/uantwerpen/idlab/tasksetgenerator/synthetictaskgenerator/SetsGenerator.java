package be.uantwerpen.idlab.tasksetgenerator.synthetictaskgenerator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


public class SetsGenerator {

	static final String NAME 			= "name";
	static final String LOCATION 		= "tasksetlocation";
	static final String UTIL_MIM 		= "globalUtilizationMin";
	static final String UTIL_MAX 		= "globalUtilizationMax";
	static final String UTIL_STEP 		= "utilizationStep";
	static final String NUM_TASKS 		= "numberOfTasks";
	static final String NUM_TASKSETS 	= "numberOfTaskSets";
	static final String PERIOD_MIN 		= "periodMin";
	static final String PERIOD_MAX 		= "periodMax";
	static final String PERIOD_STEP 	= "periodStep";
	static final String SEED 			= "seed";
	static final String CORES 			= "cores";
	
	private List<Task> taskset;
	private Map<String, List<String>> paramsTaskSet;
	private Map<String, List<String>> paramsTarget;

	
	public SetsGenerator (String fileName)
	{
		checkInput(fileName);
	}
	
	public int generateTaskset()
	{
		int setsperload = Integer.parseInt(paramsTaskSet.get(NUM_TASKSETS).get(0));
		String name = paramsTaskSet.get(NAME).get(0);
		String location = paramsTaskSet.get(LOCATION).get(0);
		String taskSetName = null;
		double minLoad =  Double.parseDouble(paramsTaskSet.get(UTIL_MIM).get(0));
		double maxLoad = Double.parseDouble(paramsTaskSet.get(UTIL_MAX).get(0));
		double stepLoad = Double.parseDouble(paramsTaskSet.get(UTIL_STEP).get(0));
		int minPeriod = Integer.parseInt(paramsTaskSet.get(PERIOD_MIN).get(0));
		int maxPeriod = Integer.parseInt(paramsTaskSet.get(PERIOD_MAX).get(0));
		int stepPeriod = Integer.parseInt(paramsTaskSet.get(PERIOD_STEP).get(0));
		int numberTasks = Integer.parseInt(paramsTaskSet.get(NUM_TASKS).get(0));
		int seed = Integer.parseInt(paramsTaskSet.get(SEED).get(0));
		int numberOfCores = Integer.parseInt(paramsTarget.get(CORES).get(0));
		int loads;
		double load = minLoad;
		BigDecimal bd = null;
		PrintWriter writer = null;

		if(minLoad > numberOfCores || maxLoad > numberOfCores)
		{
			System.err.println("The range of utilizations is bigger than the number of cores");
			return 0;
		}
		
		bd = new BigDecimal((maxLoad-minLoad)/stepLoad);
		bd = bd.setScale(4, RoundingMode.HALF_UP);
		loads = (int)(Math.floor(bd.doubleValue())+1.0);
	  
		try {
			writer = new PrintWriter(name+".xml");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.print("<taskSets>\n");
		for(int i=0;i<loads;i++)
		{
			for(int j=0;j<setsperload;j++)
			{
				taskSetName = "TaskSet_"+new Double(Math.round(100*load)).toString()+"_v"+j;
				createTasks(minPeriod,maxPeriod, stepPeriod ,load,numberTasks,seed+(j+1)*(int)Math.round(10*load));
				writer.print(XMLgenerator(taskSetName,Math.round(100*load)));
			}
			load+=stepLoad;
		}
		writer.print("</taskSets>\n");

		writer.close();
		
		return 0;
	}
	
	private List<Double> genTaskUtils(int numTask, double utilization, int seed, double periodmax)
	{
		boolean discard;
		List<Double> taskUtils = null;
		double nextSumU;
		double sumU;
		Random random = new Random(seed);
		do {
			sumU = utilization;
			taskUtils = new ArrayList<Double>();
			discard = false;
			for (int i = 0; i < numTask - 1; i++) {
				nextSumU = sumU * Math.pow(random.nextDouble(), (double) 1 / (numTask - (i + 1)));
				taskUtils.add(i,sumU - nextSumU);
				sumU = nextSumU;
				if (taskUtils.get(i) > 0.95) {
					discard = true;
				}
				if(taskUtils.get(i)*periodmax < 1000)
				{
					discard = true;
				}
			}
			taskUtils.add(numTask-1,sumU);
			if (taskUtils.get(numTask - 1) > 1 || taskUtils.get(numTask - 1)*periodmax < 1000) {
				discard = true;
			}
		} while (discard || !(getTasksetUtil(taskUtils)<=Math.ceil(utilization)));
		return taskUtils;
	}
	
	private void createTasks(double periodmin, double periodmax, double periodStep, double utilization, int numberTasks, int seed)
	{
		Random random = new Random(seed);
		double period=0,exec=0;
		List<Double> taskUtils;
		boolean discard;

		Task task = null;
		taskset = new ArrayList<Task>();
		taskUtils = genTaskUtils(numberTasks, utilization, seed*2,periodmax);
		
		for(Double u: taskUtils)
		{
			discard = true;
			while(discard)
			{
				discard = false;
				period = (int)(Math.round((random.nextDouble()*(periodmax/periodStep-periodmin/periodStep)+periodmin/periodStep))*periodStep);
				if(taskUtils.indexOf(u)==taskUtils.size()-1)
				{
					u=utilization-getTasksetU(taskset);
					exec = (int)Math.floor(u*period);
				}
				else
					exec = (int)Math.round(u*period);
				
				if(exec < 1000)
				{
					discard=true;
				}
				
			}
			task = new Task();
			task.setExe(exec);
			task.setPeriod(period);
			task.setDeadline(period);
			taskset.add(task);
			
		}
		Collections.sort(taskset);
		
		for(int i=0;i<taskset.size();i++)
		{
			if(i<9)
				taskset.get(i).setName("Task"+"0"+(i+1));
			else
				taskset.get(i).setName("Task"+(i+1));
		}
	}
	private double getTasksetU(List<Task> taskset)
	{
		double util=0;
		for(Task t:taskset)
			util+=t.getExe()/t.getPeriod();
		return util;
	}
	private double getTasksetUtil(List<Double> utils)
	{
		double util=0;
		for(Double t:utils)
			util+=t;
		return util;
	}
	private String XMLgenerator(String taskSetName, double load)
	{
		String resultString = "<taskSet name=\""+taskSetName+"\" load=\""+load+"\">\n";
		
		for(Task t: taskset)
		{
			resultString = resultString+"<task name=\""+t.getName()+"\" p=\""+t.getPeriod()+"\" d=\""+t.getDeadline()+"\" e=\""+t.getExe()+"\" />\n";
		}
		resultString +=  "</taskSet>\n";
		return resultString;
	}
	
	private void checkInput(String fileName)
	{
		XMLParser parser = new XMLParser(fileName);
		boolean check=true;
		parser.parseFile();
		paramsTaskSet = parser.getParameters("TaskSet");
		//check if all parameters are included
		if(!paramsTaskSet.containsKey(LOCATION))
			check = false;
		if(!paramsTaskSet.containsKey(UTIL_MIM))
			check = false;
		if(!paramsTaskSet.containsKey(UTIL_MAX))
			check = false;
		if(!paramsTaskSet.containsKey(UTIL_STEP))
			check = false;
		if(!paramsTaskSet.containsKey(NUM_TASKS))
			check = false;
		if(!paramsTaskSet.containsKey(NUM_TASKSETS))
			check = false;
		if(!paramsTaskSet.containsKey(PERIOD_MIN))
			check = false;
		if(!paramsTaskSet.containsKey(PERIOD_MAX))
			check = false;
		if(!paramsTaskSet.containsKey(PERIOD_STEP))
			check = false;
		if(!paramsTaskSet.containsKey(SEED))
			check = false;
		paramsTarget = parser.getParameters("TargetHardware");
		//check if all parameters are included
		if(!paramsTarget.containsKey(CORES))
			check = false;
		if(!check)
		{
			System.err.println("Some parameters are missing");
		}
	}
	
	public static void main(String[] args) 
	{	
		SetsGenerator gen;
		if(args.length==1)
			gen = new SetsGenerator(args[0]);
		else
			gen = new SetsGenerator("user.xml");
		gen.generateTaskset();
	}
}
