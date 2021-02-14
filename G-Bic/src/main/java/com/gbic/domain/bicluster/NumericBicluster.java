/**
 * Class that represents a numeric bicluster 
 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @author Rui Henriques - rmch@tecnico.ulisboa.pt
 */
package com.gbic.domain.bicluster;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gbic.domain.dataset.Dataset;
import com.gbic.domain.dataset.NumericDataset;
import com.gbic.types.PatternType;
import com.gbic.types.PlaidCoherency;
import com.gbic.types.TimeProfile;

public class NumericBicluster<T extends Number> extends Bicluster{

	private T numericSeed;
	
	//Matrix seed to use in the case of constant patterns
	private T[][] patternSeed;
	
	private T[] rowFactors;
	private T[] columnFactors;
	
	//Adaptar e testar isto
	/*
	private List<T> rowFactorsList;
	private List<T> columnFactorsList;
	private List<ArrayList<T>> patternSeedList;
	*/
	
	/**
	 * Constructs a Numeric Bicluster
	 * @param rows Set of bicluster's rows
	 * @param cols Set of bilcuster's columns
	 * @param rowPattern Bicluster's row pattern
	 * @param columnPattern Bicluster's column pattern
	 * @param seed Bicluster's seed
	 * @param rowFactors Bicluster's row factors
	 * @param columnFactors Bicluster's column factors
	 */
	public NumericBicluster(int id, SortedSet<Integer> rows, SortedSet<Integer>  cols, PatternType rowPattern, PatternType columnPattern,
			T seed, T[] rowFactors, T[] columnFactors, PlaidCoherency plaidPattern) {
		super(id, rows, cols, rowPattern, columnPattern, plaidPattern);
		this.numericSeed = seed;
		this.patternSeed = null;
		this.rowFactors = rowFactors;
		this.columnFactors = columnFactors;
	}
	
	public NumericBicluster(int id, SortedSet<Integer> rows, SortedSet<Integer>  cols, PatternType rowPattern, PatternType columnPattern,
			T seed, T[] rowFactors, T[] columnFactors, PlaidCoherency plaidPattern, TimeProfile timeProfile) {
		super(id, rows, cols, rowPattern, columnPattern, plaidPattern, timeProfile);
		this.numericSeed = seed;
		this.patternSeed = null;
		this.rowFactors = rowFactors;
		this.columnFactors = columnFactors;
	}
	
	//Adaptar e testar isto
	/*
	public NumericBicluster(SortedSet<Integer> rows, SortedSet<Integer>  cols, PatternType rowPattern, PatternType columnPattern,
			T[][] seed, T[] rowFactors, T[] columnFactors) {
		super(rows, cols, rowPattern, columnPattern);
		this.patternSeed = seed;
		this.numericSeed = null;
		this.rowFactors = rowFactors;
		this.columnFactors = columnFactors;
		
		this.rowFactorsList = new ArrayList<>();
		this.columnFactorsList = new ArrayList<>();
		this.patternSeedList = new ArrayList<ArrayList<T>>();
		
		for(int i = 0; i < seed.length; i++) {
			ArrayList<T> innerList = new ArrayList<>();
			for(int j = 0; j < seed[0].length; j++) {
				innerList.add(seed[i][j]);
			}
			this.patternSeedList.add(innerList);
		}
			
	}
	*/
	
	/**
	 * Constructs a Numeric Bicluster
	 * @param rows Set of bicluster's rows
	 * @param cols Set of bilcuster's columns
	 * @param rowPattern Bicluster's row pattern
	 * @param columnPattern Bicluster's column pattern
	 */
	public NumericBicluster(int id, SortedSet<Integer> rows, SortedSet<Integer>  cols, PatternType rowPattern, PatternType columnPattern, PlaidCoherency plaidPattern) {
		super(id, rows, cols, rowPattern, columnPattern, plaidPattern);
		this.patternSeed = null;
		this.numericSeed = null;
	}
	
	/**
	 * Constructs a Numeric Biclusters
	 * @param rows Set of bicluster's rows
	 * @param cols Set of bilcuster's columns
	 * @param rowPattern Bicluster's row pattern
	 * @param columnPattern Bicluster's column pattern
	 * @param rowFactors Bicluster's row factors
	 * @param columnFactors Bicluster's column factors
	 */
	public NumericBicluster(int id, SortedSet<Integer> rows, SortedSet<Integer>  cols, PatternType rowPattern, PatternType columnPattern,
			T[] rowFactors, T[] columnFactors, PlaidCoherency plaidPattern) {
		super(id, rows, cols, rowPattern, columnPattern, plaidPattern);
		this.patternSeed = null;
		this.numericSeed = null;
		this.rowFactors = rowFactors;
		this.columnFactors = columnFactors;
	}
	
	public NumericBicluster(int id, SortedSet<Integer> rows, SortedSet<Integer>  cols, PatternType rowPattern, PatternType columnPattern,
			T[] rowFactors, T[] columnFactors, PlaidCoherency plaidPattern, TimeProfile timeProfile) {
		super(id, rows, cols, rowPattern, columnPattern, plaidPattern, timeProfile);
		this.patternSeed = null;
		this.numericSeed = null;
		this.rowFactors = rowFactors;
		this.columnFactors = columnFactors;
	}
	
	/**
	 * Set Bicluster's numeric seed
	 * @param seed Seed's value
	 */
	public void setSeed(T seed) {
		this.numericSeed = seed;
	}
	
	/**
	 * Set Bicluster's pattern seed
	 * @param seed Seed's matrix
	 */
	public void setSeed(T[][] seed) {
		this.patternSeed = seed;
	}
	
	/**
	 * Get bicluster's numeric seed
	 * @return the seed's value
	 */
	public T getNumericSeed () {
		return this.numericSeed;
	}
	
	/**
	 * Get bicluster's pattern seed
	 * @return the seed's matrix
	 */
	public T[][] getPatternSeed(){
		return this.patternSeed;
	}
	
	/**
	 * Set the bicluster's row factors
	 * @param rowFactors Array with row factors
	 */
	public void setRowFactors(T[] rowFactors) {
		this.rowFactors = rowFactors;
	}
	
	/**
	 * Set the bicluster's column factors
	 * @param rowFactors Array with column factors
	 */
	public void setColumnFactors(T[] columnFactors) {
		this.columnFactors = columnFactors;
	}
	
	/**
	 * Set a bicluster's specific row factor
	 * @param row Row which factor will be updated
	 * @param factor The factor's new value
	 */
	public void setRowFactor(int row, T factor) {
		this.rowFactors[row] = factor;
	}
	
	/**
	 * Set a bicluster's specific column factor
	 * @param col Column which factor will be updated
	 * @param factor The factor's new value
	 */
	public void setColumnFactor(int col, T factor) {
		this.columnFactors[col] = factor;
	}
	
	/**
	 * Get the bicluster's row factors
	 * @return Array with row factors
	 */
	public T[] getRowFactors() {
		return this.rowFactors;
	}
	
	/**
	 * Get the bicluster's column factors
	 * @return Array with column factors
	 */
	public T[] getColumnFactors() {
		return this.columnFactors;
	}
	
	/**
	 * Get bicluster's row factor for specific row
	 * @param row The row index
	 * @return The factor of the given row
	 */
	public T getRowFactor(int row) {
		return this.rowFactors[row];
	}
	
	/**
	 * Get bicluster's column factor for specific column
	 * @param col The column index
	 * @return The factor of the given column
	 */
	public T getColumnFactor(int col) {
		return this.columnFactors[col];
	}
	
	@Override
	public String toString() {

		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);

		Set<Integer> rows = getRows();
		Set<Integer> columns = getColumns();
		T numericSeed = getNumericSeed();
		T[][] patternSeed = getPatternSeed();

		StringBuilder res = new StringBuilder();
		res.append("Bicluster #" + this.getId() + "\n");
		res.append(" (" + rows.size() + ", " + columns.size() + "), X=[");
		for (int i : rows)
			res.append(i + ",");
		res.append("], Y=[");
		for (int i : columns)
			res.append(i + ",");
		res.append("],");
		
		res.append(" RowPattern=" + getRowPattern() + ",");
		res.append(" ColumnPattern=" + getColumnPattern() + ",");
		
		if (numericSeed != null) {
			
			res.append(" Seed=" + df.format(numericSeed) + ", ");

			//res.append(" RowPattern=" + template.getRowPattern() + ",");

			if(getRowFactors().length > 0) {
				res.append(" RowFactors=[");
				for (T i : getRowFactors())
					res.append(df.format(i) + ",");
				res.append("],");
			}
			if(getColumnFactors().length > 0) {
				res.append(" ColumnFactors=[");
				for (T i : getColumnFactors())
					res.append(df.format(i) + ",");
				res.append("],");
			}
		}
		
		double missingsPerc = ((double) this.getNumberOfMissings()) / ((double) this.getSize()) * 100;
		double noisePerc = ((double) this.getNumberOfNoisy()) / ((double) this.getSize()) * 100;
		double errorsPerc = ((double) this.getNumberOfErrors()) / ((double) this.getSize()) * 100;
		
		if(super.getColumnPattern().equals(PatternType.ORDER_PRESERVING))
			res.append(" TimeProfile=" + super.getTimeProfile() + ",");
		
		res.append(" %Missings=" + df.format(missingsPerc) + ",");
		res.append(" %Noise=" + df.format(noisePerc) + ",");
		res.append(" %Errors=" + df.format(errorsPerc) + ",");
		
		res.append(" PlaidCoherency=" + super.getPlaidCoherency().toString());

		return res.toString().replace(",]", "]");
	}
	
	@Override
	public JSONObject toStringJSON(Dataset generatedDataset) {

		JSONObject bicluster = new JSONObject();
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);

		Set<Integer> rows = getRows();
		Set<Integer> columns = getColumns();
		T numericSeed = getNumericSeed();
		
		bicluster.put("#rows", rows.size());
		bicluster.put("#columns", columns.size());
		
		bicluster.put("X", rows);
		bicluster.put("Y", columns);
		
		bicluster.put("RowPattern", new String(getRowPattern().toString()));
		bicluster.put("ColumnPattern", new String(getColumnPattern().toString()));
		
		if (numericSeed != null) {
	
			bicluster.put("Seed", df.format(numericSeed));

			if(getRowFactors().length > 0) {
				
				T[] rowFactors = getRowFactors();
				String[] s = new String[numRows()];
				
				for(int i = 0; i < numRows(); i++) 
					s[i] = df.format(rowFactors[i]);
				
				bicluster.put("RowFactors", Arrays.toString(s));
			}

			if(getColumnFactors().length > 0) {
				
				T[] colFactors = getColumnFactors();
				String[] s = new String[numColumns()];
				
				for(int i = 0; i < numColumns(); i++) 
					s[i] = df.format(colFactors[i]);
				
				bicluster.put("ColumnFactors", Arrays.toString(s));
			}
		}
		
		double missingsPerc = ((double) this.getNumberOfMissings()) / ((double) this.getSize()) * 100;
		double noisePerc = ((double) this.getNumberOfNoisy()) / ((double) this.getSize()) * 100;
		double errorsPerc = ((double) this.getNumberOfErrors()) / ((double) this.getSize()) * 100;

		if(super.getColumnPattern().equals(PatternType.ORDER_PRESERVING))
			bicluster.put("TimeProfile",new String(super.getTimeProfile().toString()));
		
		bicluster.put("%Missings", df.format(missingsPerc));
		bicluster.put("%Noise", df.format(noisePerc));
		bicluster.put("%Errors", df.format(errorsPerc));
		
		bicluster.put("PlaidCoherency", new String(super.getPlaidCoherency().toString()));
		
		JSONObject data = new JSONObject();
		
		Integer[] rowsArray = new Integer[rows.size()];
	    rows.toArray(rowsArray);
		
		Integer[] colsArray = new Integer[columns.size()];
	    columns.toArray(colsArray);
		
	    
	    	
    	JSONArray bicData = new JSONArray();
    	
    	for(int row = 0; row < rowsArray.length; row++){
    		JSONArray rowData = new JSONArray();
			for(int col = 0; col < colsArray.length; col ++) {
				double value = ((NumericDataset)generatedDataset).getMatrixItem(rowsArray[row], colsArray[col]).doubleValue();
				if(Double.compare(value, Integer.MIN_VALUE) == 0)
					rowData.put("");
				else
					rowData.put(df.format(value));
				
			}
			bicData.put(rowData);
    	}
	    
	
	    bicluster.put("Data", bicData);
	    
		return bicluster;
	}
}
