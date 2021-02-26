/**
 * Class that represents a symbolic tricluster
 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @author Rui Henriques - rmch@tecnico.ulisboa.pt
 */
package com.gbic.domain.bicluster;

import java.text.DecimalFormat;
import java.util.Set;
import java.util.SortedSet;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gbic.domain.dataset.Dataset;
import com.gbic.domain.dataset.HeterogeneousDataset;
import com.gbic.domain.dataset.SymbolicDataset;
import com.gbic.types.PatternType;
import com.gbic.types.PlaidCoherency;
import com.gbic.types.TimeProfile;

public class SymbolicBicluster extends Bicluster{
	
	//Row and column patterns of the bicluster
	private PatternType rowPattern;
	private PatternType columnPattern;
	
	private TimeProfile timeProfile;
	private PlaidCoherency plaidPattern;
	
	//Pattern seed
	private String[][] seed;
	
	/**
	 * Construtor
	 * @param rows Dataset's set of rows
	 * @param cols Dataset's set of cols
	 * @param rowPattern The row pattern
	 * @param columnPattern The column pattern
	 */
	public SymbolicBicluster(int id, SortedSet<Integer> rows, SortedSet<Integer> cols, PatternType rowPattern, PatternType columnPattern, PlaidCoherency plaidPattern) {
		
		super(id, rows, cols);
		this.rowPattern = rowPattern;
		this.columnPattern = columnPattern;
		this.plaidPattern = plaidPattern;
	}
	
	public SymbolicBicluster(int id, SortedSet<Integer> rows, SortedSet<Integer> cols, PatternType rowPattern, PatternType columnPattern, PlaidCoherency plaidPattern,
			TimeProfile timeProfile) {
		
		super(id, rows, cols);
		this.rowPattern = rowPattern;
		this.columnPattern = columnPattern;
		this.plaidPattern = plaidPattern;
		this.timeProfile = timeProfile;
	}

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
	 * Get trilcuster's plaid coherency
	 * @return the plaid pattern
	 */
	public PlaidCoherency getPlaidCoherency() {
		return this.plaidPattern;
	}
	
	public void setPlaidCoherency(PlaidCoherency plaidPattern) {
		this.plaidPattern = plaidPattern;
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
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		
		Set<Integer> rows = getRows();
		Set<Integer> columns = getColumns();
		
		StringBuilder res = new StringBuilder();
		res.append("Bicluster #" + this.getId());
		res.append(" (" + rows.size() + ", " + columns.size() + ")\nRows=[");
		for (int i : rows)
			res.append(i + ",");
		res.append("], Columns=[");
		for (int i : columns)
			res.append(i + ",");
		res.append("],");
		
		res.append(" RowPattern=" + getRowPattern() + ",");
		res.append(" ColumnPattern=" + getColumnPattern() + ",");
		
		double missingsPerc = ((double) this.getNumberOfMissings()) / ((double) this.getSize()) * 100;
		double noisePerc = ((double) this.getNumberOfNoisy()) / ((double) this.getSize()) * 100;
		double errorsPerc = ((double) this.getNumberOfErrors()) / ((double) this.getSize()) * 100;
		
		if(getColumnPattern().equals(PatternType.ORDER_PRESERVING))
			res.append(" TimeProfile=" + getTimeProfile() + ",");
		
		res.append(" %Missings=" + df.format(missingsPerc) + ",");
		res.append(" %Noise=" + df.format(noisePerc) + ",");
		res.append(" %Errors=" + df.format(errorsPerc));
		
		res.append(" PlaidCoherency=" + getPlaidCoherency().toString());
		
		return res.toString().replace(",]", "]");
	}	
		
	@Override
	public JSONObject toStringJSON(Dataset generatedDataset, boolean heterogeneous) {

		JSONObject bicluster = new JSONObject();
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);

		Set<Integer> rows = getRows();
		Set<Integer> columns = getColumns();
		
		bicluster.put("Type", "Symbolic");
		
		bicluster.put("#rows", rows.size());
		bicluster.put("#columns", columns.size());
		
		bicluster.put("X", rows);
		bicluster.put("Y", columns);
		
		bicluster.put("PlaidCoherency", new String(getPlaidCoherency().toString()));
		
		bicluster.put("RowPattern", new String(getRowPattern().toString()));
		bicluster.put("ColumnPattern", new String(getColumnPattern().toString()));
		
		double missingsPerc = ((double) this.getNumberOfMissings()) / ((double) this.getSize()) * 100;
		double noisePerc = ((double) this.getNumberOfNoisy()) / ((double) this.getSize()) * 100;
		double errorsPerc = ((double) this.getNumberOfErrors()) / ((double) this.getSize()) * 100;

		if(getColumnPattern().equals(PatternType.ORDER_PRESERVING))
			bicluster.put("TimeProfile",new String(getTimeProfile().toString()));
		
		bicluster.put("%Missings", df.format(missingsPerc));
		bicluster.put("%Noise", df.format(noisePerc));
		bicluster.put("%Errors", df.format(errorsPerc));
		
		bicluster.put("PlaidCoherency", new String(getPlaidCoherency().toString()));
		
		Integer[] rowsArray = new Integer[rows.size()];
	    rows.toArray(rowsArray);
		
		Integer[] colsArray = new Integer[columns.size()];
	    columns.toArray(colsArray);
		
	    	
    	JSONArray bicData = new JSONArray();
    	
    	for(int row = 0; row < rowsArray.length; row++){
    		JSONArray rowData = new JSONArray();
			for(int col = 0; col < colsArray.length; col ++) {
				if(!heterogeneous)
					rowData.put(((SymbolicDataset)generatedDataset).getMatrixItem(rowsArray[row], colsArray[col]));
				else
					rowData.put(((HeterogeneousDataset)generatedDataset).getSymbolicElement(rowsArray[row], colsArray[col]));
			}
			bicData.put(rowData);
    	}
    	
        bicluster.put("Data", bicData);
	    
		return bicluster;
	}
	
}
