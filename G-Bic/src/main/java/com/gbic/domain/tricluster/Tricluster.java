/**
 * Tricluster Class
 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @author Rui Henriques - rmch@tecnico.ulisboa.pt
 * @version 1.0
 */

package com.gbic.domain.tricluster;

import java.util.Set;

import org.json.JSONObject;

import com.gbic.domain.dataset.Dataset;
import com.gbic.types.PatternType;
import com.gbic.types.PlaidCoherency;
import com.gbic.types.TimeProfile;

public abstract class Tricluster {
	
	private int id;
	
	private PatternType contextPattern;
	private TimeProfile timeProfile;
	private PlaidCoherency plaidPattern;
	
	private int numOfMissings;
	private int numOfNoisy;
	private int numOfErrors;
	
	/**
	 * Constructor
	 * @param contextPattern Tricluster's context pattern
	 * @param plaidPattern Tricluster's plaid coherency pattern
	 * @param id The tricluster's id
	 */
	public Tricluster(PatternType contextPattern, PlaidCoherency plaidPattern, int id) {
		
		this.id = id;
		this.contextPattern = contextPattern;
		this.plaidPattern = plaidPattern;
		this.numOfMissings = 0;
		this.numOfNoisy = 0;
		this.numOfErrors = 0;
	}
	
	public Tricluster(PatternType contextPattern, TimeProfile timeProfile, PlaidCoherency plaidPattern, int id) {
		
		this.id = id;
		this.contextPattern = contextPattern;
		this.timeProfile = timeProfile;
		this.plaidPattern = plaidPattern;
		this.numOfMissings = 0;
		this.numOfNoisy = 0;
		this.numOfErrors = 0;
	}
	
	/**
	 * Get tricluster size
	 * @return Num Rows * Num Cols * Num Ctxs
	 */
	public abstract int getSize();
	
	/**
	 * Get tricluster's rows
	 * @return The set of rows
	 */
	public abstract Set<Integer> getRows();
	
	/**
	 * Get tricluster's columns
	 * @return The set of columns
	 */
	public abstract Set<Integer> getColumns();
	
	/**
	 * Get tricluster's contexts
	 * @return The set of contexts
	 */
	public abstract Set<Integer> getContexts();
	
	/**
	 * @return the timeProfile
	 */
	public TimeProfile getTimeProfile() {
		return timeProfile;
	}

	/**
	 * @param timeProfile the timeProfile to set
	 */
	public void setTimeProfile(TimeProfile timeProfile) {
		this.timeProfile = timeProfile;
	}

	/**
	 * Get the triclster's ID
	 * @return The ID
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Get the tricluster's row pattern
	 * @return the row pattern
	 */
	public abstract PatternType getRowPattern();
	
	/**
	 * Get the tricluster's column pattern
	 * @return the column pattern
	 */
	public abstract PatternType getColumnPattern();
	
	/**
	 * Get the tricluster's context pattern
	 * @return the context pattern
	 */
	public PatternType getContextPattern() {
		return this.contextPattern;
	}
	
	/**
	 * Get trilcuster's plaid coherency
	 * @return the plaid pattern
	 */
	public PlaidCoherency getPlaidCoherency() {
		return this.plaidPattern;
	}
	
	/**
	 * Get tricluster's number of missings
	 * @return The number of tricluster's elements that are missings
	 */
	public int getNumberOfMissings() {
		return this.numOfMissings;
	}
	
	/**
	 * Increse the number of missing elements
	 */
	public void addMissing(){
		this.numOfMissings++;
	}
	
	/**
	 * Get tricluster's number of noisy elements
	 * @return The number of tricluster's elements that are noisy
	 */
	public int getNumberOfNoisy() {
		return this.numOfNoisy;
	}
	
	/**
	 * Increse the number of noisy elements
	 */
	public void addNoisy(){
		this.numOfNoisy++;
	}
	
	/**
	 * Get tricluster's number of error elements
	 * @return The number of tricluster's elements that are errors
	 */
	public int getNumberOfErrors() {
		return this.numOfErrors;
	}
	
	/**
	 * Increse the number of error elements
	 */
	public void addError(){
		this.numOfErrors++;
	}
	
	public abstract String matrixToString(Object[][] matrix);
	
	public abstract String toString();
	
	/**
	 * Get tricluster's info in JSON
	 * @param generatedDataset The dataset generated
	 * @return JSONObject with tricluster representation
	 */
	public abstract JSONObject toStringJSON(Dataset generatedDataset);
}
