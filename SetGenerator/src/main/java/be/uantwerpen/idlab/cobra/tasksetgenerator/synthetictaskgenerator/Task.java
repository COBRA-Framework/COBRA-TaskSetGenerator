package be.uantwerpen.idlab.cobra.tasksetgenerator.synthetictaskgenerator;


public class Task implements Comparable<Task>{
	private double period;
	private double exe;
	private double deadline;
	private String name;
	
	
	public Task(String name, String period, String deadline, String exe){
		
		this.name = name;
		this.period = Double.parseDouble(period);
		this.deadline = Double.parseDouble(deadline);
		this.exe = Double.parseDouble(exe);
		if(this.exe > this.deadline){
			System.err.println("task's execution > deadline");
		}
	}
	
	
	public Task() {
		
	}

	public String toString(){
		String str = "--task--";
		str += "name:" + this.name + ", period:" + this.period + ", exe:" + this.exe + 
				", deadline:" + this.deadline;
		return str;
	}
	
	public double getPeriod() {
		return period;
	}
	public void setPeriod(double period) {
		this.period = period;
	}
	public double getExe() {
		return exe;
	}
	public void setExe(double exe) {
		this.exe = exe;
	}
	public double getDeadline() {
		return deadline;
	}
	public void setDeadline(double deadline) {
		this.deadline = deadline;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public int compareTo(Task o) {
	    return deadline<o.getDeadline()?-1:deadline>o.getDeadline()?1:doSecodaryOrderSort(o);
	}
	//if two deadline are the same
	public int doSecodaryOrderSort(Task o) {
	    return exe<o.getExe()?-1:exe>o.getExe()?1:0;
	}

	
}
