package be.uantwerpen.idlab.cobra.tasksetgenerator.taskcreator;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class XMLParser {
	
	private String inputFilename;
	private Document doc;
	private ArrayList<Element> nodes;
	
	public XMLParser(String inputFilename) {
		this.inputFilename = inputFilename;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(inputFilename));
		} catch (SAXException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.err.println("Could not parse file");
		} catch (IOException e){
			System.err.println("File does not exist.");
		}
	}
	public XMLParser(File inputFile) {
		this.inputFilename = inputFile.getName();
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputFile);
		} catch (SAXException | ParserConfigurationException e) {
			System.err.println("Could not parse file");
			//e.printStackTrace();
		} catch (IOException e){
			System.err.println("File does not exist.");
		}
		
	}
	public boolean parseFile()
	{		
		NodeList node = null;
		
		nodes = new ArrayList<Element>();
		
		doc.getDocumentElement().normalize();
		
		node = doc.getElementsByTagName("taskSet");
		if(node.getLength()==0 || node.getLength() > 1)
			System.err.println("The input must contain one TaskSetData element.");
		nodes.add((Element)node.item(0));
		
		node = doc.getElementsByTagName("targetHardware");
		if(node.getLength()==0 || node.getLength() > 1)
			System.err.println("The input must contain one targetHardware element.");
		nodes.add((Element)node.item(0));

		node = doc.getElementsByTagName("benchmark");
		if(node.getLength() > 1)
			System.err.println("The input should only contain one benchmarkParameters element.");
		nodes.add((Element) node.item(0));	
		return true;
	}
	
	public List<TaskSet> parseTaskset()
	{
		Node root = doc.getDocumentElement();
		NodeList nodelist = root.getChildNodes();
		Task task;
		Node taskline;
		TaskSet taskset = null;
		NodeList tasksetNodes;
		List<TaskSet> taskSetList = new ArrayList<TaskSet>();
		for(int j=0; j<nodelist.getLength();j++)
		{
			if(nodelist.item(j).getNodeName().equals("taskSet"))
			{
				tasksetNodes = nodelist.item(j).getChildNodes();	
				taskset = new TaskSet(nodelist.item(j).getAttributes().getNamedItem("name").getNodeValue(),Double.parseDouble(nodelist.item(j).getAttributes().getNamedItem("load").getNodeValue()));
				for(int i=0; i<tasksetNodes.getLength();i++)
				{
					taskline = tasksetNodes.item(i);
					if(taskline.getNodeName().equals("task"))
					{	
						task = new Task();
						task.setName(taskline.getAttributes().getNamedItem("name").getNodeValue());
						task.setPeriod(Double.parseDouble(taskline.getAttributes().getNamedItem("p").getNodeValue()));
						task.setDeadline(Double.parseDouble(taskline.getAttributes().getNamedItem("d").getNodeValue()));
						task.setExe(Double.parseDouble(taskline.getAttributes().getNamedItem("e").getNodeValue()));
						taskset.addTask(task);
					}
				}
				taskSetList.add(taskset);
			}
			
		}
		
		return taskSetList;
	}
	public Map<String,List<String>> getParameters(String name)
	{
		Map<String,List<String>> parameters = new HashMap<String, List<String>>();
		Element ele;
		List<String> values = null;
		for(Element e:nodes)
		{
			NodeList listn = null;
			
			if(e.getNodeName().equalsIgnoreCase(name))
			{
				listn = e.getElementsByTagName("parameter");
				for(int i=0;i<listn.getLength();i++)
				{
					ele = (Element) listn.item(i);
					NamedNodeMap map =  ele.getAttributes();
					values = new ArrayList<String>();
					
					for(int j=0;j<map.getLength();j++)
					{
						Node a = map.item(j);
						if(a.getNodeName()!="name")
							values.add(a.getNodeValue());
					}
					parameters.put(ele.getAttribute("name"), values);
				}
				return parameters;
			}
		}
		System.err.println("Parameters not found");
		return null;
	}
	public Map<String,List<String>> parseDesriptionFile(File file, String arch)
	{
		Map<String,List<String>> parameters = new HashMap<String, List<String>>();
		ArrayList<String> values;
		Element program = (Element)doc.getElementsByTagName("program").item(0);
		
		values = new ArrayList<String>();
		values.add(program.getAttribute("name"));
		parameters.put("name", values);
		values = new ArrayList<String>();
		values.add(program.getAttribute("path"));
		parameters.put("path", values);
		
		NodeList params = program.getElementsByTagName("parameter");
	
		Element ele;
		String atrName ="";
		NamedNodeMap map;
		for(int i=0;i<params.getLength();i++)
		{
			ele = (Element) params.item(i);
			map =  ele.getAttributes();
			values = new ArrayList<String>();
			if(ele.getAttribute("name").equals("WCET") && !ele.getAttribute("var0").equals(arch))
				continue;
			for(int j=0;j<map.getLength();j++)
			{
				Node a = map.item(j);
				if(a.getNodeName()!="name")
					values.add(a.getNodeValue());
				else
					atrName = a.getNodeValue();
			}
			parameters.put(atrName, values);
		}
		if(!parameters.containsKey("WCET"))
			return null;
		return parameters;
	}
	
	
	public String getInputFilename() {
		return inputFilename;
	}
	public void setInputFilename(String inputFilename) {
		this.inputFilename = inputFilename;
	}
}
