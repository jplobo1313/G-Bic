/**
 * Class that represents a Bicluster object

 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @author Rui Henriques - rmch@tecnico.ulisboa.pt
 */

package com.gbic.domain.bicluster;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONObject;

import com.gbic.domain.dataset.Dataset;
import com.gbic.types.PatternType;
import com.gbic.types.PlaidCoherency;
import com.gbic.types.TimeProfile;

public abstract class Bicluster {
	
	private int id;
	
	//row and column set of the bicluster
	private SortedSet<Integer> columns;
	private SortedSet<Integer> rows;
	
	private int numOfMissings;
	private int numOfNoisy;
	private int numOfErrors;

	/**
	 * Constructor
	 * @param rows The bicluster's set of rows
	 * @param cols The bicluster's set of columns
	 * @param rowPattern The bicluster's row pattern
	 * @param columnPattern The bicluster's column pattern
	 */
	public Bicluster(int id, SortedSet<Integer> rows, SortedSet<Integer> cols) {
		
		this.id = id;
		this.rows = rows;
		this.columns = cols;
	}
	
	public Bicluster(int id, SortedSet<Integer> rows, SortedSet<Integer> numericCols, SortedSet<Integer> symbolicCols) {
		
		this.id = id;
		this.rows = rows;
		
		this.columns = new TreeSet<>();
		this.columns.addAll((Collection<? extends Integer>) numericCols);
		this.columns.addAll((Collection<? extends Integer>) symbolicCols);
	}
	
	public int getSize() {
		return getNumRows() * getNumCols();
	}

	public int getNumRows() {
		return this.rows.size();
	}
	
	public int getNumCols() {
		return this.columns.size();
	}

	/**
	 * Get the triclster's ID
	 * @return The ID
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Set the bicluster's column set
	 * @param columns The column set
	 */
	public void setColumns(SortedSet<Integer> columns) {
		this.columns = columns;
	}

	/**
	 * Set the bicluster's row set
	 * @param columns The row set
	 */
	public void setRows(SortedSet<Integer> rows) {
		this.rows = rows;
	}

	/**
	 * get bic rows
	 * 
	 * @return
	 */
	public SortedSet<Integer> getRows() {
		return rows;
	}

	/**
	 * get bic columns
	 * 
	 * @return
	 */
	public SortedSet<Integer> getColumns() {
		return columns;
	}

	/**
	 * Bicluster's number of rows
	 * @return number of rows
	 */
	public int numRows() {
		return rows.size();
	}

	/**
	 * Bicluster's number of columns
	 * @return number of columns
	 */
	public int numColumns() {
		return columns.size();
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

	public abstract String toString();

	/**
	 * Textual representation of the bicluster's dimension
	 * @return A string in the format (|rows|, |columns|)
	 */
	public String toShortString() {
		return "(" + rows.size() + "," + columns.size() + ")";
	}
	
	/**
	 * Get tricluster's info in JSON
	 * @param generatedDataset The dataset generated
	 * @return JSONObject with tricluster representation
	 */
	public abstract JSONObject toStringJSON(Dataset generatedDataset, boolean heterogeneous);

}
