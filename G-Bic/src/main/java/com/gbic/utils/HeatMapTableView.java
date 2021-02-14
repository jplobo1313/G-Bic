package com.gbic.utils;

import java.util.List;

import javafx.scene.control.Button;

public abstract class HeatMapTableView {

	protected int bicID;
	protected List<String> xTicks;
	protected List<String> yTicks;
	protected Button show;
	private String title;
	
	public HeatMapTableView(int bicID, List<String> xTicks, List<String> yTicks, String title) {
		this.bicID = bicID;
		this.xTicks = xTicks;
		this.yTicks = yTicks;
		this.show = new Button("Show");
		this.title = title;
	}
	
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}


	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}


	public int getBicID() {
		return this.bicID;
	}
	
	public void setBicID(int bicID) {
		this.bicID = bicID;
	}
	
	public Button getShow() {
		return this.show;
	}
	
	public void setShow(Button show) {
		this.show = show;
	}

	/**
	 * @return the xTicks
	 */
	public List<String> getxTicks() {
		return xTicks;
	}

	/**
	 * @param xTicks the xTicks to set
	 */
	public void setxTicks(List<String> xTicks) {
		this.xTicks = xTicks;
	}

	/**
	 * @return the yTicks
	 */
	public List<String> getyTicks() {
		return yTicks;
	}

	/**
	 * @param yTicks the yTicks to set
	 */
	public void setyTicks(List<String> yTicks) {
		this.yTicks = yTicks;
	}
	
	public abstract void produceChart();
	
}
