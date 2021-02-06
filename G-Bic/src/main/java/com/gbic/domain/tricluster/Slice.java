/**
 * Slice Class that represents a Context
 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @version 1.0
 */
package com.gbic.domain.tricluster;

public class Slice<T> {

	private int contextID;
	private T factor;
	private T[][] patternSeed;
	
	/**
	 * Constructor
	 * @param contextID The context ID
	 */
	public Slice(int contextID) {
		this.contextID = contextID;
		this.factor = null;
		this.patternSeed = null;
	}
	
	/**
	 * Constructor
	 * @param contextID The context ID
	 * @param factor the context numeric seed
	 */
	public Slice(int contextID, T factor) {
		this(contextID);
		this.factor = factor;
	}
	
	/**
	 * Constructor
	 * @param contextID the context ID
	 * @param patternSeed the context pattern seed
	 */
	public Slice(int contextID, T[][] patternSeed) {
		this(contextID);
		this.patternSeed = patternSeed;
	}

	/**
	 * Get the context ID
	 * @return the context ID
	 */
	public int getContextID() {
		return contextID;
	}

	/**
	 * Set the context's ID
	 * @param contextID the context id
	 */
	public void setContextID(int contextID) {
		this.contextID = contextID;
	}

	/**
	 * Get the context factor
	 * @return the factor
	 */
	public T getFactor() {
		return factor;
	}

	/**
	 * Set the context's factor
	 * @param factor the factor
	 */
	public void setFactor(T factor) {
		this.factor = factor;
	}
	
	/**
	 * Get the context pattern seed
	 * @return the 2D matrix that represents the pattern seed
	 */
	public T[][] getPatternSeed() {
		return patternSeed;
	}

	/**
	 * Set the context's pattern seed
	 * @param patternSeed the 2D matrix that represents the pattern seed
	 */
	public void setPatternSeed(T[][] patternSeed) {
		this.patternSeed = patternSeed;
	}
	
	
}
