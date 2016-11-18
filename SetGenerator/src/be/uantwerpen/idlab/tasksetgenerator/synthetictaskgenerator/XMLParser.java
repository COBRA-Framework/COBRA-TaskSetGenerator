package be.uantwerpen.idlab.tasksetgenerator.synthetictaskgenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.print.attribute.Attribute;
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
			System.err.println("File does not excist.");
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
			System.err.println("File does not excist.");
		}
		
	}
	public boolean parseFile(){
		
		NodeList node = null;
		
		nodes = new ArrayList<Element>();
		
		doc.getDocumentElement().normalize();
		
		/*Node rootNode = doc.getDocumentElement();
		System.out.println(rootNode.getNodeName());
		NodeList listN = rootNode.getChildNodes();
		Node n = listN.item(2);
		System.out.println(n.getChildNodes().item(0).getNodeName());*/
		
		
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

	public static void main(String[] args) {

		Map<String, List<String>> param = new HashMap<String,  List<String>>();
		XMLParser parser = new XMLParser(new File("user.xml"));
		parser.parseFile();
		
		param = parser.getParameters("targetHardware");
		for(Entry<String, List<String>> e:param.entrySet())
			System.out.println(e.getKey() + ": "+e.getValue().get(0));
		System.out.println();
		
		param = parser.getParameters("taskSet");
		for(Entry<String, List<String>> e:param.entrySet())
			System.out.println(e.getKey() + ": "+e.getValue().get(0));
		System.out.println();
		
		param = parser.getParameters("benchmark");
		for(Entry<String, List<String>> e:param.entrySet())
			System.out.println(e.getKey() + ": "+e.getValue().get(0));
		System.out.println();
	}
	
	public String getInputFilename() {
		return inputFilename;
	}
	public void setInputFilename(String inputFilename) {
		this.inputFilename = inputFilename;
	}
}
