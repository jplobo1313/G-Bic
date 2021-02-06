/**
 * Class that represents a symbolic tricluster
 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @author Rui Henriques - rmch@tecnico.ulisboa.pt
 */
package com.gbic.domain.bicluster;

import java.util.Set;
import java.util.SortedSet;

import com.gbic.types.PatternType;

public class SymbolicBicluster extends Bicluster{
	
	//Pattern seed
	private String[][] seed;
	
	/**
	 * Construtor
	 * @param rows Dataset's set of rows
	 * @param cols Dataset's set of cols
	 * @param rowPattern The row pattern
	 * @param columnPattern The column pattern
	 */
	public SymbolicBicluster(SortedSet<Integer> rows, SortedSet<Integer> cols, PatternType rowPattern, PatternType columnPattern) {
		
		super(rows, cols, rowPattern, columnPattern);
	}

	/**
	 * Set the biclusters seed
	 * @param seed The matrix
	 */
	public void setSeed(String[][] seed) {
		this.seed = seed;
	}
	
	/**
	 * Get biclusters seed
	 * @return
	 */
	public String[][] getSeed() {
		return this.seed;
	}

	@Override
	public String toString() {
		
		Set<Integer> rows = getRows();
		Set<Integer> columns = getColumns();
		
		StringBuilder res = new StringBuilder();
		res.append(" (" + rows.size() + "," + columns.size() + "), X=[");
		for (int i : rows)
			res.append(i + ",");
		res.append("], Y=[");
		for (int i : columns)
			res.append(i + ",");
		res.append("],");
		if (seed != null) {
			/*
			res.append(" Seed=[");
			for (String i : seed)
				res.append(i + ",");
			res.append("],");
			*/
			res.append(" Seed=" + seed + ",");
		}
		res.append(" RowPattern=" + getRowPattern() + ",");
		res.append(" ColumnPattern=" + getColumnPattern());
		return res.toString().replace(",]", "]");
	}
		
	
	
}
