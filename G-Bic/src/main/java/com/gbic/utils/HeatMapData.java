package com.gbic.utils;

import java.util.List;

public abstract class HeatMapData {

	private int bicID;
	private List<String> xData;
	private List<String> yData;
	private Number[][] chartData;
	private String[][] symbolicChartData;
	private String title;

	/**
	 * @param bicID
	 * @param xData
	 * @param yData
	 * @param cha
	 */
	public HeatMapData(int bicID, List<String> xData, List<String> yData, Number[][] chartData, String title) {
		this.bicID = bicID;
		this.xData = xData;
		this.yData = yData;
		this.chartData = chartData;
		this.title = title;
	}
	
	public HeatMapData(int bicID, List<String> xData, List<String> yData, String[][] chartData, String title) {
		this.bicID = bicID;
		this.xData = xData;
		this.yData = yData;
		this.symbolicChartData = chartData;
		this.title = title;
	}

	/**
	 * @return the bicID
	 */
	public int getBicID() {
		return bicID;
	}

	/**
	 * @param bicID the bicID to set
	 */
	public void setBicID(int bicID) {
		this.bicID = bicID;
	}

	/**
	 * @return the xData
	 */
	public List<String> getXData() {
		return xData;
	}

	/**
	 * @param xData the xData to set
	 */
	public void setXData(List<String> xData) {
		this.xData = xData;
	}

	/**
	 * @return the yData
	 */
	public List<String> getYData() {
		return yData;
	}

	/**
	 * @param yData the yData to set
	 */
	public void setYData(List<String> yData) {
		this.yData = yData;
	}

	/**
	 * @return the chartData
	 */
	public Number[][] getChartData() {
		return chartData;
	}

	public String[][] getSymbolicChartData(){
		return this.symbolicChartData;
	}
	
	/**
	 * @param chartData the chartData to set
	 */
	public void setChartData(Number[][] chartData) {
		this.chartData = chartData;
	}
	
	public void setSymbolicChartData(String[][] chartData) {
		this.symbolicChartData = chartData;
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
	
	public abstract void produceChart();
}
