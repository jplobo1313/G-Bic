/**
 * OverlappingSettings Class
 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @version 1.0
 */

package com.gbic.utils;

import com.gbic.types.PlaidCoherency;

public class OverlappingSettings {

	private PlaidCoherency plaid;
	private double percOfOverlappingBics;
	private int maxBicsPerOverlappedArea;
	private double percOfOverlappingRows;
	private double percOfOverlappingColumns;
	private double maxPercOfOverlappingElements;
	
	/**
	 * Constructor
	 * @param plaid The plaid coherency
	 * @param percOfOverlappingBics The percentage of dataset's biclusters that can overlap
	 * @param maxBicsPerOverlappedArea The maximum amount of bics that can overlap together
	 * @param percOfOverlappingRows The maximum percentage of overlapping in the row dimension
	 * @param percOfOverlappingColumns The maximum percentage of overlapping in the column dimension
	 * @param percOfOverlappingContexts The maximum percentage of overlapping in the context dimension
	 * @param maxPercOfOverlappingElements The maximum percentage of elements tha can be share between 
	 * biclusters (relative to the smallest bic)
	 */
	public OverlappingSettings(PlaidCoherency plaid, double percOfOverlappingBics, int maxBicsPerOverlappedArea,
			double percOfOverlappingRows, double percOfOverlappingColumns,
			double maxPercOfOverlappingElements) {
		
		this.plaid = plaid;
		this.percOfOverlappingBics = percOfOverlappingBics;
		this.maxBicsPerOverlappedArea = maxBicsPerOverlappedArea;
		this.percOfOverlappingRows = percOfOverlappingRows;
		this.percOfOverlappingColumns = percOfOverlappingColumns;
		this.maxPercOfOverlappingElements = maxPercOfOverlappingElements;
	}

	/**
	 * Empty constructor
	 */
	public OverlappingSettings() {};
	
	/**
	 * Get plaid coherency
	 * @return plaid coherency
	 */
	public PlaidCoherency getPlaidCoherency() {
		return plaid;
	}

	/**
	 * Set plaid coherency
	 * @param plaid the plaid coherency (NONE, ADDITIVE, MULTIPLICATIVE, INTERPOLED or NO_OVERLAPPING)
	 */
	public void setPlaidCoherency(PlaidCoherency plaid) {
		this.plaid = plaid;
	}

	/**
	 * Get the percentage of overlapping bics
	 * @return the percentage of overlapping bics
	 */
	public double getPercOfOverlappingBics() {
		return percOfOverlappingBics;
	}

	/**
	 * Set the percentage of overlapping bics
	 * @param percOfOverlappingBics the percentage of overlapping bics
	 */
	public void setPercOfOverlappingBics(double percOfOverlappingBics) {
		this.percOfOverlappingBics = percOfOverlappingBics;
	}

	/**
	 * Get maximum number of biclusters that can overlap together
	 * @return the number of biclusters
	 */
	public int getMaxBicsPerOverlappedArea() {
		return maxBicsPerOverlappedArea;
	}

	/**
	 * Set maximum number of biclusters that can overlap together
	 * @param maxBicsPerOverlappedArea the number of biclusters
	 */
	public void setMaxBicsPerOverlappedArea(int maxBicsPerOverlappedArea) {
		this.maxBicsPerOverlappedArea = maxBicsPerOverlappedArea;
	}

	/**
	 * Get the maximum percentage of overlapping on the row dimension
	 * @return the allowed percentage of overlapping across rows
	 */
	public double getPercOfOverlappingRows() {
		return percOfOverlappingRows;
	}

	/**
	 * Set the maximum percentage of overlapping on the row dimension
	 * @param percOfOverlappingRows the allowed percentage of overlapping across rows
	 */
	public void setPercOfOverlappingRows(double percOfOverlappingRows) {
		this.percOfOverlappingRows = percOfOverlappingRows;
	}

	/**
	 * Get the maximum percentage of overlapping on the column dimension
	 * @return the allowed percentage of overlapping across columns
	 */
	public double getPercOfOverlappingColumns() {
		return percOfOverlappingColumns;
	}

	/**
	 * Set the maximum percentage of overlapping on the column dimension
	 * @param percOfOverlappingColumns the allowed percentage of overlapping across columns
	 */
	public void setPercOfOverlappingColumns(double percOfOverlappingColumns) {
		this.percOfOverlappingColumns = percOfOverlappingColumns;
	}

	/**
	 * Set the maximum percentage of elements that can be share between biclusters, relative to the smallest one
	 * @param maxPercOfOverlappingElements the percentage
	 */
	public void setMaxPercOfOverlappingElements(double maxPercOfOverlappingElements) {
		this.maxPercOfOverlappingElements = maxPercOfOverlappingElements;
	}
	
	/**
	 * Set the maximum percentage of elements that can be share between biclusters, relative to the smallest one
	 * @return the percentage
	 */
	public double getMaxPercOfOverlappingElements() {
		return this.maxPercOfOverlappingElements;
	}
}
