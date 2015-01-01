package com.tritera.xml;
/*/
XmlDoc

* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or (at
* your option) any later version.
*
* This program is distributed in the hope that it will be useful, but
* WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*
* Copyright (C) Tritera Incorporated. 2000 - 2014 

John Shackelford
jshack@tritera.com

TODO - update this base class for the latest xerces parser, look at using generics and collections to improve.
	
/*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



/*
 * A simple utility class for parsing XML documents and getting info out of them. Based on previous work.
 * 
 * @author jshack
 */

public class XmlDoc
{
	protected Document doc;
	protected String errorMessage;
	protected String treeIndent;
	protected boolean LOG_MESSAGE = false;
	
	/**
	XmlDoc - Default, no parameter constructor.
	Use this constructor when creating an XmlDoc from scratch - that is
	trying to create a doc by not parsing a bunch of xml.
**/
	public XmlDoc()
	{
		doc = new DocumentImpl();
		treeIndent = "";
	}
	
	/**
		Use this constructor when trying to create an XmlDoc object
		with a given set of XML. You can use this to tell you if the supplied
		"xml" is well formed.
		@param a string of possible xml
		@returns XmlDoc is the input string was parsed successfully 
	**/
	public XmlDoc(String xml) throws Exception
	{
		try
		{
			doc = new DocumentImpl();
			treeIndent = "";
			parseDoc(xml);
		}
		catch(SAXException sax)
		{
			throw new Exception(sax.getMessage());
		}
		catch(IOException ioe)
		{
			throw new Exception(ioe.getMessage());
		}
	}
	
	public XmlDoc(InputStream is) throws Exception
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        //System.out.println(out.toString());   //Prints the string content read from input stream
        reader.close();
        
        doc = new DocumentImpl();
		treeIndent = "";
		parseDoc(out.toString());
	}
	
	
	public Document getDocument()
	{
		return doc;
	}
	
	/**
		This method create a node element for an xml document
		and names it "name".
	**/
	public Node makeNode(String name)
	{
		Node node = doc.createElement(name);
		return node;
	}
	
	public Node makeNode(String name, String value)
	{
		//Node node;
		//node = makeNode(name);
		//addNodeElement(name,value);
		//return node;
		
		Element item = doc.createElement(name);
		item.appendChild(doc.createTextNode(value));
		//node.appendChild(item);
		
		return item;
	}
	
	public Node getRootNode()
	{
		Node node;
		node = doc.getFirstChild();
		return node;
	}
	
	public String getRootNodeName()
	{
		if(doc == null) return null;
		return getDocument().getDocumentElement().getNodeName();
	}
	
	/**
		This nethod creates a node called "name", sets its value to "value"
		and adds it to the first child of the root node.
	**/
	public void addNode(String name, String value)
	{
		//Node node;
		//node = makeNode(name);
		addNodeElement(name,value);
	}
	
	public void addCDataNode(String name, String value)
	{
		if(value != null && value.length() > 3)
		{
			CDATASection cdata = doc.createCDATASection(value);
			
			Element item = doc.createElement(name);
			item.appendChild(cdata);
			getRootNode().appendChild(item);
		}
		else
		{
			addNode(name, value);
		}
	}
	
	/**
		This method appends the node to the document.
		This can only happen once - one root node for a document
	**/
	public void addNode(Node node)
	{
		doc.appendChild(node);
	}
	
	/**
		Creates a node named "name", appends it to the document and returns
		the node.
	**/
	public Node addRoot(String name)
	{
		Node root = doc.createElement(name);
		doc.appendChild(root);
		return root;
	}
	
	/**
		Creates a node called "name", sets it value to "value" and
		appends it to the first child node of the document.
	**/	
	public void addNodeElement(String name, String value)
	{
		Node node;
		node = doc.getFirstChild();
		addNodeElement(node, name, value);
	}
	
	/**
		Creates a node, names it "name", sets its value to "value"
		and appends the new node to the input "node";
	**/
	public void addNodeElement(Node node, String name, String value)
	{
		Element item = doc.createElement(name);
		item.appendChild(doc.createTextNode(value));
		node.appendChild(item);
	}
/*	
	private void addNodeElement(String name, CDATASection cdata)
	{
		Element item = doc.createElement(name);
		item.appendChild(cdata);
	}
	*/
	
	/**
		Import node "import_node" appending it to "base_node".
		The imported node is not modified - rather a copy of the
		node is made and inserted as a child of base_node.
		
	**/
	public Node importNode(Node base_node, Node import_node) throws DOMException
	{
		Node insert_node = null;
		try
		{
			insert_node = getDocument().importNode(import_node, true);
			base_node.appendChild(insert_node);
		}
		catch(DOMException domex)
		{
			//Log something here?
			throw domex;
		}

		return insert_node;

	}
	
	
	
	/**
	returns the "value" of the node.
	this method may endup evaluating children nodes.
	**/
	public String getNodeValue(Node node)
	{
		String value;
		if(node == null) return null;
		
		//value = node.getNodeValue();
		try
		{
			value = getDOMTreeSansDeclaration(node, false);
			return value;
		}
		catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | ClassCastException
				| TransformerException | IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
		Gets a child node of Node and returns its value.
	**/
	public String getNodeValue(Node node, String name)
	{
		Node child_node;
		String value;
		child_node = getNodeByName(node,name);
		if(child_node == null) return null;
		value = getNodeValue(child_node);
		return value;
	}
	
	/**
		Returns the value of the first node with name
	**/
	public String getNodeValue(String name)
	{
		Node node;
		String val;
		node = getNodeByName(name);
		if(node == null) return null;
		val = getNodeValue(node);
		return val;
	}
	
	/**
		Adds an attribute to a node.
	**/
	public void addAttribute(Node node, String name, String value)
	{
		Element elm;
		elm = (Element)node;
		elm.setAttribute(name,value);
	}
	
	/*/
		Pass in the name of a node and the attribute name,
		this method will find the first node with name
		node_name, and return the attribute value.
	/*/
	public String getNodeAttribute(String node_name, String attr_name)
	{
		String value = null;
		Element node = (Element)getNodeByName(node_name);
		if(node != null)
		{
			value =	node.getAttribute(attr_name);
		}
		return value;
	}
	
	/*/
		Pass in a node and an attribute name, and this method
		will return the value of that attribute.
	/*/
	public String getNodeAttribute(Node node, String attr_name)
	{
		String value = null;
		Element enode = (Element)node;
		if(node != null)
		{
			value =	enode.getAttribute(attr_name);
		}
		return value;
	}
	
	/**
		Sets the value of the node "name" to "value"
	**/
	public boolean setNodeValue(String name, String value)
	{
		Node node;
		Node first_child;
		//String val;
		node = getNodeByName(name);
		if(node == null)
		{
			// Would it be better to throw an exception here?
			writeLogMessage("XmlDoc.setNodeValue - No Node named: " + name);
			return false;
		}
		first_child = node.getFirstChild();
		if(first_child == null) return false;  //guard!
		
		first_child.setNodeValue(value);
		//val = getNodeValue(name);
		return true;
	}
	
	/**
		Sets the value of the node "name" to "value"
	**/
	public boolean setNodeValue(Node start_node,String name, String value)
	{
		Node node;
		Node first_child;
		//String val;
		node = getNodeByName(start_node,name);
		if(node == null)
		{
			// Would it be better to throw an exception here?
			writeLogMessage("XmlDoc.setNodeValue - No Node named: " + name);
			return false;
		}
		first_child = node.getFirstChild();
		if(first_child == null) return false;  //guard!
		
		first_child.setNodeValue(value);
		//val = getNodeValue(name);
		return true;
	}
	
	/**
		Given a name, this method the first node with that
		name.
	**/
	public Node getNodeByName(String name)
	{
		Node[] nodes = getNodesByName(getRootNode(), name);
		return nodes[0];
	}

	/**
		Given a name, this method the first node with that
		name, starting with start_node.
	**/
	public Node getNodeByName(Node start_node, String name)
	{
		Node[] nodes = getNodesByName(start_node, name);
		return nodes[0];
	}
	
	/**
		Given a name, this method returns an array of nodes
		with that name.
	**/
	public Node[] getNodesByName(String name)
	{
		return getNodesByName(getRootNode(), name);
	}
	
	/**
		Given a name, this method returns an array of nodes
		with that name, starting with 
	**/
	public Node[] getNodesByName(Node start_node, String name)
	{
		int i,len;
		Vector<Node> vector = new Vector<Node>();
		Node nodes[];
		searchDoc(start_node,name,vector);
		len = vector.size();
		if(len > 0)
		{
			nodes = new Node[len];
			for(i = 0; i < len; i++)
			{
				nodes[i] = vector.elementAt(i);
			}
			return nodes;
		}
		else
		{
			return null;
		}
	}
	
	/**
	
	**/
	public Node getNodeByAttributeValue(String node_name, String attr_name, String attr_value)
	{
		Node[] nodes;
		Node node = null;
		String temp;
		int len, i;
		nodes = getNodesByName(node_name);
		len = nodes.length;
		if(len < 1) return null;
		for(i = 0; i < len; i++)
		{
			node = nodes[i];
			temp = getNodeAttribute(node,attr_name);
			if(temp != null)
			{
				if(temp.equals(attr_value))
				{
					return node;
				}
				node = null;
			}
			node = null;
		}
		return node;
	}
	
	/**
		
	**/
	public Node getNodeByAttributeValue(Node start_node, String node_name, String attr_name, String attr_value)
	{
		Node[] nodes;
		Node node = null;
		String temp;
		int len, i;
		nodes = getNodesByName(start_node, node_name);
		len = nodes.length;
		if(len < 1) return null;
		for(i = 0; i < len; i++)
		{
			node = nodes[i];
			temp = getNodeAttribute(node,attr_name);
			if(temp != null)
			{
				if(temp.equals(attr_value))
				{
					return node;
				}
				node = null;
			}
			node = null;
		}
		return node;
	}
	
	/**
			Search the document for all nodes with a sepcific attribute name and value.
			Returns a lit of matching nodes.
			
	**/
	public Node[] getNodesByAttributeValue(String node_name, String attr_name, String attr_value)
	{
		Node[] nodes, ret_nodes;
		Vector<Node> vector = new Vector<Node>();
		Node node = null;
		String temp;
		int len, i;
		
		// list of potential nodes
		nodes = getNodesByName(node_name);
		len = nodes.length;
		if(len < 1) return null;
		
		for(i = 0; i < len; i++)
		{
			node = nodes[i];
			temp = getNodeAttribute(node,attr_name);
			if(temp != null)
			{
				if(temp.equals(attr_value))
				{
					vector.add(node);
				}
				node = null;
			}
			node = null;
		}
		
		len = vector.size();
		if(len > 0)
		{
			ret_nodes = new Node[len];
			for(i = 0; i < len; i++)
			{
				ret_nodes[i] = vector.elementAt(i);
			}
			return ret_nodes;
		}
		else
		{
			return null;
		}
	}
	
	public void setNodeAttributeValue(Node node, String attr_name, String attr_value)
	{
		String temp;
		if(node != null)
		{
			Element elm;
			elm = (Element)node;
			temp = elm.getAttribute(attr_name);
			if(temp != null)
			{
				elm.setAttribute(attr_name, attr_value);
			}
		}
	}
	
	/**
		Adds a comment tag to node.
	**/
	public void addComment(Node node, String data)
	{
		Comment comment = doc.createComment(data);
		node.appendChild(comment);
	}
	
	/**
		Adds a comment to the document at root.
	**/
	public void addDocComment(String data)
	{
		Comment comment = doc.createComment(data);
		doc.appendChild(comment);
	}
	
	/**
		This method attempts to parse the input "text" into an xml document.
		If this method fails, the "text" is not well formed xml. This method
		relies on using a DOMParser. If it succeeds, the value of this XmlDoc
		will now reflect the xml represented by the input "text".
	**/
	public void parseDoc(String text) throws SAXException, IOException
	{
		DOMParser parser = new DOMParser();
		
		StringReader sr = new StringReader(text);
		InputSource src = new InputSource(sr);
		try
		{
			parser.parse(src);
			doc = parser.getDocument();
			writeLogMessage("XmlDoc.parseDoc Succeeded");
			//return true;
		}
		catch(SAXException se)
		{
			errorMessage = "SAXException: " + se.toString();
			writeLogMessage("XmlDoc.parseDoc: " + errorMessage);
			//return false;
			throw(se);
		}
		catch(IOException ie)
		{
			errorMessage = "IOException: " + ie.toString();
			writeLogMessage("XmlDoc.parseDoc: " + errorMessage);
			//return false;
			throw(ie);
		}
	}
	

	/**
		Removes the node from the document
	**/
	public void removeNode(Node node)
	{
		while(node.hasChildNodes())
		{
			node.removeChild(node.getFirstChild());
		}
	}
	
	/**
	Removes all nodes named "name" and returns them in a vector.
	**/
	public Vector<Node> removeNodes(String name)
	{
		int len;
		Vector<Node> vector = new Vector<Node>();
		Node node = null;
		Node root;
		searchDoc(this.doc,name,vector);
		root = doc.getFirstChild();
		len = vector.size();
		writeLogMessage("XmlDoc.removeNode: Found " + len + " Nodes named " + name);
		for(int i = 0; i < len; i++)
		{
			node = vector.elementAt(i);
			root = node.getParentNode();
			root.removeChild(node);
		}
		return vector;
	}
	
	/**
		Public method for searching for a tag or node
		by name. Search starts with the root node/doc.
	**/
	public void searchDoc(String name, Vector<Node> output)
	{
		writeLogMessage("XmlDoc.searchDoc: Searching for " + name);
		searchDoc(this.doc,name,output);
	}
	
	/**
		Use this method for searching for a tag starting at
		the input node. 
	**/
	protected void searchDoc(Node node, String name, Vector<Node> output)
	{
		writeLogMessage("XmlDoc.searchDoc: Searching for " + name);
		recurseDoc(node,name,output);
	}
	
	/**
		Recurses through the document tree and for any node
		with which has a name "name" then it is added to the vector;
	**/ 
	protected void recurseDoc(Node node, String name, Vector<Node> output)  
	{ 
		if(node == null) return;
		if(output == null) return;
		
		int type = node.getNodeType(); 
		switch (type) 
		{ 
	  		case Node.DOCUMENT_NODE:  
	  		{ 
	    		recurseDoc(((Document)node).getDocumentElement(), name, output); 
	    		break; 
	  		} 
	
			case Node.ELEMENT_NODE:  
			{ 
				String nname;
				nname = node.getNodeName();
				writeLogMessage("XmlDoc.recurseDoc:ELEMENT_NODE: " + nname);
				if(nname.equals(name))
				{
					output.addElement(node);
					writeLogMessage("XmlDoc.recurseDoc:FOUND ONE: " + node.getNodeName());
				}
				
				NodeList children = node.getChildNodes(); 
				if (children != null) 
				{ 
					int len = children.getLength(); 
					for (int i = 0; i < len; i++)
					{ 
						recurseDoc(children.item(i), name, output);
					}
				} 
				break; 
			} 
	
			case Node.ENTITY_REFERENCE_NODE:  
			{  
				break; 
			} 
			
			case Node.CDATA_SECTION_NODE:  
			{  
				break; 
			} 
			
			case Node.TEXT_NODE:  
			{  
				String nname;
				nname = node.getNodeName();
				writeLogMessage("XmlDoc.recurseDoc:TEXT_NODE: " + nname);
				if(nname.equals(name))
				{
					output.addElement(node);
					writeLogMessage("XmlDoc.recurseDoc:FOUND ONE\n" + node.getNodeName());
				}
				break; 
			} 
			
			case Node.PROCESSING_INSTRUCTION_NODE:  
			{ 
				break; 
			} 
		} 
	}
	
	/**
		Use this method to get a list of tag names - that is
		for each node in the document, this method gets the name of the
		node.
	**/	
	public String[] getTagNames()
	{
		int i,ii, len;
		String dont_include = "#text";
		String strings[], ret_strings[];
		String name;
		Node node;
		Vector<Node> vector = getNodeList();
		len = vector.size();
		strings = new String[len];
		ii = 0;
		for(i = 0; i < len; i++)
		{
			node = (Node)vector.elementAt(i);
			name = node.getNodeName();
			strings[i] = name;
			if(name.equals(dont_include))
			{
				ii++;
			}
		}
		
		ret_strings = new String[len - ii];
		ii = 0;
		for(i = 0; i < len; i++)
		{
			name = strings[i];
			if(name.equals(dont_include))
			{
			
			}
			else
			{
				ret_strings[ii] = name;
				ii++;
			}
		}
		return ret_strings;
	}
	
	/**
		Returns all the nodes in this document
		in a vector.
	**/
	public Vector<Node> getNodeList()
	{
		Vector<Node> vector = new Vector<Node>();

		if(doc != null)
		{
			getNodeList(doc,vector);
			return vector;
		}
		else
		{
			return null;
		}
	}
	
	/**
		Returns the nodes starting with the input node.
		Supply a vector to capture them all.
	**/
	public void getNodeList(Node node,Vector<Node> vector)
	{
		NodeList list;
		Node anode;
		int len, i;
		list = node.getChildNodes();
		len = list.getLength();
		for(i = 0; i < len; i++)
		{
			anode = list.item(i);
			vector.add(anode);
			getNodeList(anode,vector);
		}
	}
	
	/**
		This method will try to copy data out of the src
		document into this document. Note that it does this
		by tag name, so to get a value out of the src document
		into this document, the name of tag has to be the same.
	**/
	public void copyDoc(XmlDoc src)
	{
		String[]  src_tags;
		String value;
		int len, i;
		//my_tags = getTagNames();
		src_tags = src.getTagNames();
		len = src_tags.length;
		for(i = 0; i < len; i++)
		{
			value = src.getNodeValue(src_tags[i]);
			setNodeValue(src_tags[i], value);
		}
	}
	

	
	/*/
	Simple logging
	/*/
	public void writeLogMessage(String mess)
	{
		if(LOG_MESSAGE)
		{
			System.out.println("XmlDoc: " + mess);
		}
	}
	/**
	Returns the document xml as a nice printable formatted string.
	Replacing the old approach, this seems to produce better more
	consistent output.
	**/
	public String getDOMTree()
	{
		Node node = doc.getDocumentElement();
		String xml = getDOMTree(node);
		return xml;		
	}
	
	public String getDOMTree(Node node)
	{		
		try
		{			
			return getDOMTree(node, true, true);
	
		}
		catch(IOException e)
		{
			e.printStackTrace();
			writeLogMessage("XmlDoc.getDOMTree: " + e.toString());
			
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			writeLogMessage("XmlDoc.getDOMTree: " + e.toString());
		} catch (InstantiationException e)
		{
			e.printStackTrace();
			writeLogMessage("XmlDoc.getDOMTree: " + e.toString());
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
			writeLogMessage("XmlDoc.getDOMTree: " + e.toString());
		} catch (ClassCastException e)
		{
			e.printStackTrace();
			writeLogMessage("XmlDoc.getDOMTree: " + e.toString());
		} catch (TransformerException e)
		{
			e.printStackTrace();
			writeLogMessage("XmlDoc.getDOMTree: " + e.toString());
		}
		return null;
	}
	
	
	
	
	
	/**
		Method uses getDOMTree to print
		the dom tree in a "pretty" fashion to std out.
	**/
	public void printDOMTree()
	{
		String xml = getDOMTree();
		System.out.println(xml);
	}
	
	public String getDOMTreeSansDeclaration(boolean isPretty) throws TransformerException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException
	{
		return getDOMTreeSansDeclaration(getRootNode(), isPretty);
        
		//Alternate ways to get this done - keep for future reference
		/*
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(getRootNode());		
		transformer.transform(source, result);		
		return result.getWriter().toString();
		*/
        /*
		StringWriter out = new StringWriter();
		OutputFormat format = new OutputFormat(doc);
		format.setOmitXMLDeclaration(true);
		XMLSerializer serializer = new XMLSerializer(out, format);
		serializer.serialize(doc);
		return out.toString();
         */
	}
	
	public String getDOMTreeSansDeclaration(Node node, boolean isPretty) throws TransformerException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException
	{
		return getDOMTree(node, isPretty, false);
	}
	
	public String getDOMTree(Node node, boolean isPretty, boolean showXmlDeclaration) throws TransformerException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException
	{
		String xml = null;
		DOMImplementationRegistry registry =  DOMImplementationRegistry.newInstance();
        DOMImplementationLS impls =  (DOMImplementationLS)registry.getDOMImplementation("LS");
		StringWriter stringWriter=new StringWriter();
        LSOutput domOutput = impls.createLSOutput();
        domOutput.setEncoding(java.nio.charset.Charset.defaultCharset().name());            
        domOutput.setCharacterStream(stringWriter);

        LSSerializer domWriter = impls.createLSSerializer();            
        DOMConfiguration domConfig = domWriter.getDomConfig();
        
        if(isPretty)
        {
            domWriter.setNewLine("\r\n"); 
            domConfig.setParameter("format-pretty-print", true);
            domConfig.setParameter("element-content-whitespace", true);
        }
        if(showXmlDeclaration == false)
        {
        	domConfig.setParameter("xml-declaration", false);
        }
        domConfig.setParameter("cdata-sections", Boolean.TRUE);
               
        domWriter.write(node, domOutput);
        xml = domOutput.getCharacterStream().toString();
        
        //DOMStringList dsl=domConfig.getParameterNames();

        return xml;
	}

}
