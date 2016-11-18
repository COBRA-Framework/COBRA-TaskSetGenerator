package be.uantwerpen.idlab.tasksetgeneraotr.ProgramCombiner;

import java.util.*;
import java.util.Map.*;

public class Task{
	private String name;
	Map<String,Integer> programs;
	
	public Task(String name){
		this.name = name;
		programs = new HashMap<String, Integer>();
	}
	public Task(){
		programs = new HashMap<String, Integer>();
	}
	
	public void addProgram(String name, int numberOfExecution)
	{
		programs.put(name, numberOfExecution);
	}
	public String toString()
	{
		String result=name+"\n";
		for(Entry<String, Integer> s:programs.entrySet())
		{
			result+=s.getKey()+": "+s.getValue()+" times \n";
		}
		return result;
		
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, Integer> getPrograms() {
		return programs;
	}
	public void setPrograms(Map<String, Integer> programs) {
		this.programs = programs;
	}
}
