package be.uantwerpen.idlab.tasksetgenerator.taskCreator;

import java.util.ArrayList;
import java.util.List;

public class TaskSet {
	
	private List<Task> tasks;
	private String taskSetName;
	private double load;
	
	public TaskSet(String name,double load)
	{
		taskSetName = name;
		tasks = new ArrayList<Task>();
		this.load = load;
	}
	public void addTask(Task t)
	{
		tasks.add(t);
	}
	public List<Task> getTasks() {
		return tasks;
	}
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
	public String getTaskSetName() {
		return taskSetName;
	}
	public double getLoad() {
		return load;
	}
	public void setTaskSetName(String taskSetName) {
		this.taskSetName = taskSetName;
	}
	
}
