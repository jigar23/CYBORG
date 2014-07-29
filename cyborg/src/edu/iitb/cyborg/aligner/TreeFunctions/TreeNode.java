/*
 * 
 * Copyright 2013 Digital Audio Processing Lab, Indian Institute of Technology.  
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 *
 */

/**
 *
 * @author  : Nicool
 * @contact : nicool@iitb.ac.in
 */

package edu.iitb.cyborg.aligner.TreeFunctions;

import java.util.ArrayList;

import edu.iitb.cyborg.fst.Monophone;
import edu.iitb.cyborg.fst.NewTriphone;


public class TreeNode {
	//Reference to parent TreeNode
	TreeNode parent;
	
	/* stateInfo[0] : State ID index
	 * stateInfo[1] : Observation id
	 * stateInfo[2] : State tmat value 
	 */
	int stateInfo[];
	
	int branchId;
	
	//Corresponding triphone
	String triPhone;
	
	//Stores likelihood of each frame
	double costOfNode;
	
	//Stores likelihood score of each path
	double costOfPath;
	
	boolean active;
	
	//Stores list of children in left to right order.
	ArrayList<TreeNode> children = new ArrayList<TreeNode>();
	
	//Stores branchIds in case
	public ArrayList<Integer> siblingsBranchIds = new ArrayList<>();
	
	NewTriphone newTriphoneObj;
	
	Monophone monophoneObj;
	
	public Monophone getMonophoneObj() {
		return monophoneObj;
	}


	public void setMonophoneObj(Monophone monophoneObj) {
		this.monophoneObj = monophoneObj;
	}


	public NewTriphone getNewTriphoneObj() {
		return newTriphoneObj;
	}


	public void setNewTriphoneObj(NewTriphone newTriphoneObj) {
		this.newTriphoneObj = newTriphoneObj;
	}


	public TreeNode(){
		stateInfo = new int[3];
		active = true;
	}
	

	public ArrayList<Integer> getSiblingsBranchIds() {
		return siblingsBranchIds;
	}

	public void setSiblingsBranchIds(ArrayList<Integer> siblingsBranchIds) {
		this.siblingsBranchIds = siblingsBranchIds;
	}

	
	public int getBranchId() {
		return branchId;
	}

	public void setBranchId(int branchId) {
		this.branchId = branchId;
	}


	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	

	public TreeNode getParent() {
		return parent;
	}
	
	public void setParent(TreeNode parent) {
		this.parent = parent;
	}
	
	
	public String getTriPhone() {
		return triPhone;
	}

	public void setTriPhone(String triPhone) {
		this.triPhone = triPhone;
	}

	
	public int[] getStateInfo() {
		return stateInfo;
	}

	public void setStateInfo(int[] stateInfo) {
		this.stateInfo = stateInfo;
	}

	
	
	public ArrayList<TreeNode> getChildren() {
		return children;
	}
	public void setChildren(ArrayList<TreeNode> children) {
		this.children = children;
	}		
	
	
	public double getCostOfNode() {
		return costOfNode;
	}


	public void setCostOfNode(double costOfNode) {
		this.costOfNode = costOfNode;
	}


	public double getCostOfPath() {
		return costOfPath;
	}


	public void setCostOfPath(double costOfPath) {
		this.costOfPath = costOfPath;
	}


	public void setChild(TreeNode child) {
		this.children.add(child);
	}	
	
}
