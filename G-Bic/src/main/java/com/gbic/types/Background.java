package com.gbic.types;

public class Background {

	private BackgroundType type;
	private double param1;
	private double param2;
	private double[] param3;
	
	//When background is UNIFORM or MISSING
	public Background(BackgroundType type) {
		this.type = type;
	}
	
	//When background follows dist with one param
	public Background(BackgroundType type, double param1) {
		this.type = type;
		this.param1 = param1;
	}
	
	//When background follows dist with two params
	public Background(BackgroundType type, double param1, double param2) {
		this.type = type;
		this.param1 = param1;
		this.param2 = param2;
	}
	
	//When background has discrete probabilities
	public Background(BackgroundType type, double[] param3) {
		this.type = type;
		this.param3 = param3;
	}

	public BackgroundType getType() {
		return type;
	}

	public void setType(BackgroundType type) {
		this.type = type;
	}

	public double getParam1() {
		return param1;
	}

	public void setParam1(double param1) {
		this.param1 = param1;
	}

	public double getParam2() {
		return param2;
	}

	public void setParam2(double param2) {
		this.param2 = param2;
	}

	public double[] getParam3() {
		return param3;
	}

	public void setParam3(double[] param3) {
		this.param3 = param3;
	}
	
	
	
}
