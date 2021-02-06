/**
 * Class that represents a Bicluster object

 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @author Rui Henriques - rmch@tecnico.ulisboa.pt
 */

package com.gbic.domain.bicluster;

import java.util.Set;
import java.util.SortedSet;

import com.gbic.types.PatternType;

public abstract class Bicluster {
	
	//row and column set of the bicluster
	private SortedSet<Integer> columns;
	private SortedSet<Integer> rows;
	
	//Row and column patterns of the bicluster
	private PatternType rowPattern;
	private PatternType columnPattern;

	/**
	 * Constructor
	 * @param rows The bicluster's set of rows
	 * @param cols The bicluster's set of columns
	 * @param rowPattern The bicluster's row pattern
	 * @param columnPattern The bicluster's column pattern
	 */
	public Bicluster(SortedSet<Integer> rows, SortedSet<Integer> cols, PatternType rowPattern, PatternType columnPattern) {
		
		this.rowPattern = rowPattern;
		this.columnPattern = columnPattern;
		this.rows = rows;
		this.columns = cols;
	}

	/**
	 * Get the bicluster's row pattern
	 * @return The row pattern
	 */
	public PatternType getRowPattern() {
		return rowPattern;
	}

	/**
	 * Set the bicluster's row pattern
	 * @param rowPattern the row pattern
	 */
	public void setRowPattern(PatternType rowPattern) {
		this.rowPattern = rowPattern;
	}

	/**
	 * Get the bicluster's column pattern
	 * @return The column pattern
	 */
	public PatternType getColumnPattern() {
		return columnPattern;
	}

	/**
	 * Set the bicluster's column pattern
	 * @param rowPattern the column pattern
	 */
	public void setColumnPattern(PatternType columnPattern) {
		this.columnPattern = columnPattern;
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
	public Set<Integer> getRows() {
		return rows;
	}

	/**
	 * get bic columns
	 * 
	 * @return
	 */
	public Set<Integer> getColumns() {
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

	public abstract String toString();

	/**
	 * Textual representation of the bicluster's dimension
	 * @return A string in the format (|rows|, |columns|)
	 */
	public String toShortString() {
		return "(" + rows.size() + "," + columns.size() + ")";
	}

}
