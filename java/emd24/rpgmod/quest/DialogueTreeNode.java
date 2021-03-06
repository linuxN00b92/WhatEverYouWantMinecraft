/**
 * A class that is used to represent a dialogue between a player and an NPC
 * 
 * @author Wesley Reardan
 */
package emd24.rpgmod.quest;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;

public class DialogueTreeNode {
	public List<DialogueTreeNode> children;	//List of children dialogue nodes
	public DialogueTreeNode parent;	//Parent node
	public String dialogueText;	//Text display to user, or choices if isReply
	public String itemNeeded;		//The item ID needed to access this dialogue item
	public int itemQuantity;		//The quantity of items needed to access this dialogue item
	public boolean isReply;		//if the node is a reply to a statement made by the character
	public String action;	//name of server-side script to execute
	
	public String reward;	//Item to give after Dialogue item is clicked
	public int rewardQuantity;	//Quantity of reward item
	
	
	protected void Init()
	{
		children = new ArrayList<DialogueTreeNode>();
		dialogueText = "";
		itemNeeded = "";
		itemQuantity = 0;
		isReply = false;
		action = "";
		parent = null;
		
		reward = "";
		rewardQuantity = 0;
	}
	
	public DialogueTreeNode()
	{
		Init();
	}
	
	public DialogueTreeNode(DialogueTreeNode parent)
	{
		Init();
		this.parent = parent;
	}
	
	/*
	 * Function to add a new child
	 */
	public DialogueTreeNode addChild() {
		if(this.isReply && this.children.size() != 0)
			return null;
		DialogueTreeNode child = new DialogueTreeNode(this);
		child.isReply = !isReply;
		if(child.isReply) {
			child.addChild();
		}
		children.add(child);
		return child;
	}
	
	/*
	 * Function to store the node to a string
	 */
	public String store() {
		String result = "";
		
		result += dialogueText + "`";
		result += itemNeeded + "`";
		result += itemQuantity + "`";
		result += isReply + "`";
		result += action + "`";
		
		result += children.size() + "`";
		
		result += reward + "`";
		result += rewardQuantity + "`";
		
		result += "\n";
		
		for(DialogueTreeNode child : children) {
			result += child.store();
		}
		
		return result;
	}
	
	/*
	 * Function to retrieve a tree from a string
	 */
	public void load(String value) {
		children.clear();
		String[] lines = value.split("\n");
		load(lines, 0, "");
	}
	
	/**
	 * Recursive load method for dialogue tree
	 * 
	 * @return resulting line number
	 */
	protected int load(String[] lines, int lineNumber, String spaces) {
		String line = lines[lineNumber].trim();
		String[] values = line.split("`");
		if(values.length < 6)
			return 0;
		
		int i = 0;
		dialogueText = values[i++];
		itemNeeded = values[i++];
		itemQuantity = Integer.parseInt(values[i++]);
		isReply = Boolean.parseBoolean(values[i++]);
		action = values[i++];
		int childrenSize = Integer.parseInt(values[i++]);
		
		if(values.length > 7)
		{
			reward = values[i++];
			rewardQuantity = Integer.parseInt(values[i++]);
		}
		
		lineNumber++;
		
		spaces += " ";
		for(int j = 0; j < childrenSize; j++) {
			DialogueTreeNode child = new DialogueTreeNode();
			lineNumber = child.load(lines, lineNumber, spaces);
			children.add(child);
		}
		
		return lineNumber;
	}
	
	/*
	 * Get a list of Nodes in tree for displaying in list
	 */
	public void getList(List<String> strings, String spaces) {
		String isReplyText = isReply ? "-" : "+";
		strings.add(spaces + isReplyText + dialogueText);
		for(DialogueTreeNode child : children) {
			child.getList(strings, spaces + " ");
		}
	}
	
	/*
	 * Get a list of Nodes in tree for displaying in list
	 */
	public void getList(List<DialogueTreeNode> nodes) {
		nodes.add(this);
		for(DialogueTreeNode child : children) {
			//nodes.add(child);
			child.getList(nodes);
		}
	}

	public void remove() {
		if(this.isReply && this.parent != null) {
			if(parent.children.size() > 1) {
				parent.children.remove(this);
				this.parent = null;	
			}
		}
	}
}
