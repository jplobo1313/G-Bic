package com.gbic.utils;

import java.util.List;

import javafx.scene.control.Button;

public abstract class HeatMapTableView {

	protected int contextID;
	protected List<String> xTicks;
	protected List<String> yTicks;
	protected Button show;
	
	public HeatMapTableView(int contextID, List<String> xTicks, List<String> yTicks) {
		this.contextID = contextID;
		this.xTicks = xTicks;
		this.yTicks = yTicks;
		this.show = new Button("Show");
	}
	
	public int getContextID() {
		return this.contextID;
	}
	
	public void setContextID(int contextID) {
		this.contextID = contextID;
	}
	
	public Button getShow() {
		return this.show;
	}
	
	public void setShow(Button show) {
		this.show = show;
	}
}
