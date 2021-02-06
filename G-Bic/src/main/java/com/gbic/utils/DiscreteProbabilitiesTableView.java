package com.gbic.utils;

public class DiscreteProbabilitiesTableView {

	private String symbol;
	private String prob;
	/**
	 * @param symbol
	 * @param prob
	 */
	public DiscreteProbabilitiesTableView(String symbol, String prob) {
		this.symbol = symbol;
		this.prob = prob;
	}
	/**
	 * @return the symbol
	 */
	public String getSymbol() {
		return symbol;
	}
	/**
	 * @param symbol the symbol to set
	 */
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	/**
	 * @return the prob
	 */
	public String getProb() {
		return prob;
	}
	/**
	 * @param prob the prob to set
	 */
	public void setProb(String prob) {
		this.prob = prob;
	}
	
	
}
