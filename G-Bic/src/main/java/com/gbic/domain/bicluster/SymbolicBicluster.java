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
import com.gbic.domain.dataset.SymbolicDataset;
import com.gbic.types.PatternType;
import com.gbic.types.PlaidCoherency;
import com.gbic.types.TimeProfile;

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
	public SymbolicBicluster(int id, SortedSet<Integer> rows, SortedSet<Integer> cols, PatternType rowPattern, PatternType columnPattern, PlaidCoherency plaidPattern) {
		
		super(id, rows, cols, rowPattern, columnPattern, plaidPattern);
	}
	
	public SymbolicBicluster(int id, SortedSet<Integer> rows, SortedSet<Integer> cols, PatternType rowPattern, PatternType columnPattern, PlaidCoherency plaidPattern,
			TimeProfile timeProfile) {
		
		super(id, rows, cols, rowPattern, columnPattern, plaidPattern, timeProfile);
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
		String[][] seed = getSeed();
		
		StringBuilder res = new StringBuilder();
		res.append("(" + rows.size() + ", " + columns.size() + "), X=[");
		for (int i : rows)
			res.append(i + ",");
		res.append("], Y=[");
		for (int i : columns)
			res.append(i + ",");
		res.append("],");
		
		res.append(" RowPattern=" + getRowPattern() + ",");
		res.append(" ColumnPattern=" + getColumnPattern() + ",");
		
		double missingsPerc = ((double) this.getNumberOfMissings()) / ((double) this.getSize()) * 100;
		double noisePerc = ((double) this.getNumberOfNoisy()) / ((double) this.getSize()) * 100;
		double errorsPerc = ((double) this.getNumberOfErrors()) / ((double) this.getSize()) * 100;
		
		if(super.getColumnPattern().equals(PatternType.ORDER_PRESERVING))
			res.append(" TimeProfile=" + super.getTimeProfile() + ",");
		
		res.append(" %Missings=" + df.format(missingsPerc) + ",");
		res.append(" %Noise=" + df.format(noisePerc) + ",");
		res.append(" %Errors=" + df.format(errorsPerc));
		
		res.append(" PlaidCoherency=" + super.getPlaidCoherency().toString());
		
		return res.toString().replace(",]", "]");
	}	
		
	@Override
	public JSONObject toStringJSON(Dataset generatedDataset) {

		JSONObject tricluster = new JSONObject();
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);

		Set<Integer> rows = getRows();
		Set<Integer> columns = getColumns();
		
		tricluster.put("#rows", rows.size());
		tricluster.put("#columns", columns.size());
		
		tricluster.put("X", rows);
		tricluster.put("Y", columns);
		
		tricluster.put("PlaidCoherency", new String(super.getPlaidCoherency().toString()));
		
		tricluster.put("RowPattern", new String(getRowPattern().toString()));
		tricluster.put("ColumnPattern", new String(getColumnPattern().toString()));
		
		double missingsPerc = ((double) this.getNumberOfMissings()) / ((double) this.getSize()) * 100;
		double noisePerc = ((double) this.getNumberOfNoisy()) / ((double) this.getSize()) * 100;
		double errorsPerc = ((double) this.getNumberOfErrors()) / ((double) this.getSize()) * 100;

		if(super.getColumnPattern().equals(PatternType.ORDER_PRESERVING))
			tricluster.put("TimeProfile",new String(super.getTimeProfile().toString()));
		
		tricluster.put("%Missings", df.format(missingsPerc));
		tricluster.put("%Noise", df.format(noisePerc));
		tricluster.put("%Errors", df.format(errorsPerc));
		
		tricluster.put("PlaidCoherency", new String(super.getPlaidCoherency().toString()));
		
		Integer[] rowsArray = new Integer[rows.size()];
	    rows.toArray(rowsArray);
		
		Integer[] colsArray = new Integer[columns.size()];
	    columns.toArray(colsArray);
		
	    	
    	JSONArray bicData = new JSONArray();
    	
    	for(int row = 0; row < rowsArray.length; row++){
    		JSONArray rowData = new JSONArray();
			for(int col = 0; col < colsArray.length; col ++) 
				rowData.put(((SymbolicDataset)generatedDataset).getMatrixItem(rowsArray[row], colsArray[col]));
			bicData.put(rowData);
    	}
    	
        tricluster.put("Data", bicData);
	    
		return tricluster;
	}
	
}
