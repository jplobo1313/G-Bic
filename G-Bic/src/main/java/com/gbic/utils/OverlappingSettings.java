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
	private double percOfOverlappingTrics;
	private int maxTricsPerOverlappedArea;
	private double percOfOverlappingRows;
	private double percOfOverlappingColumns;
	private double percOfOverlappingContexts;
	private double maxPercOfOverlappingElements;
	
	/**
	 * Constructor
	 * @param plaid The plaid coherency
	 * @param percOfOverlappingTrics The percentage of dataset's triclusters that can overlap
	 * @param maxTricsPerOverlappedArea The maximum amount of trics that can overlap together
	 * @param percOfOverlappingRows The maximum percentage of overlapping in the row dimension
	 * @param percOfOverlappingColumns The maximum percentage of overlapping in the column dimension
	 * @param percOfOverlappingContexts The maximum percentage of overlapping in the context dimension
	 * @param maxPercOfOverlappingElements The maximum percentage of elements tha can be share between 
	 * triclusters (relative to the smallest tric)
	 */
	public OverlappingSettings(PlaidCoherency plaid, double percOfOverlappingTrics, int maxTricsPerOverlappedArea,
			double percOfOverlappingRows, double percOfOverlappingColumns, double percOfOverlappingContexts,
			double maxPercOfOverlappingElements) {
		
		this.plaid = plaid;
		this.percOfOverlappingTrics = percOfOverlappingTrics;
		this.maxTricsPerOverlappedArea = maxTricsPerOverlappedArea;
		this.percOfOverlappingRows = percOfOverlappingRows;
		this.percOfOverlappingColumns = percOfOverlappingColumns;
		this.percOfOverlappingContexts = percOfOverlappingContexts;
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
	 * Get the percentage of overlapping trics
	 * @return the percentage of overlapping trics
	 */
	public double getPercOfOverlappingTrics() {
		return percOfOverlappingTrics;
	}

	/**
	 * Set the percentage of overlapping trics
	 * @param percOfOverlappingTrics the percentage of overlapping trics
	 */
	public void setPercOfOverlappingTrics(double percOfOverlappingTrics) {
		this.percOfOverlappingTrics = percOfOverlappingTrics;
	}

	/**
	 * Get maximum number of triclusters that can overlap together
	 * @return the number of triclusters
	 */
	public int getMaxTricsPerOverlappedArea() {
		return maxTricsPerOverlappedArea;
	}

	/**
	 * Set maximum number of triclusters that can overlap together
	 * @param maxTricsPerOverlappedArea the number of triclusters
	 */
	public void setMaxTricsPerOverlappedArea(int maxTricsPerOverlappedArea) {
		this.maxTricsPerOverlappedArea = maxTricsPerOverlappedArea;
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
	 * Get the maximum percentage of overlapping on the context dimension
	 * @return the allowed percentage of overlapping across contexts
	 */
	public double getPercOfOverlappingContexts() {
		return percOfOverlappingContexts;
	}

	/**
	 * Set the maximum percentage of overlapping on the context dimension
	 * @param percOfOverlappingContexts the allowed percentage of overlapping across contexts
	 */
	public void setPercOfOverlappingContexts(double percOfOverlappingContexts) {
		this.percOfOverlappingContexts = percOfOverlappingContexts;
	}
	
	/**
	 * Set the maximum percentage of elements that can be share between triclusters, relative to the smallest one
	 * @param maxPercOfOverlappingElements the percentage
	 */
	public void setMaxPercOfOverlappingElements(double maxPercOfOverlappingElements) {
		this.maxPercOfOverlappingElements = maxPercOfOverlappingElements;
	}
	
	/**
	 * Set the maximum percentage of elements that can be share between triclusters, relative to the smallest one
	 * @return the percentage
	 */
	public double getMaxPercOfOverlappingElements() {
		return this.maxPercOfOverlappingElements;
	}
}
