/**
 * SymbolicTricluster Class
 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @version 1.0
 */

package com.gbic.domain.tricluster;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gbic.domain.bicluster.SymbolicBicluster;
import com.gbic.domain.dataset.Dataset;
import com.gbic.domain.dataset.SymbolicDataset;
import com.gbic.types.PatternType;
import com.gbic.types.PlaidCoherency;
import com.gbic.types.TimeProfile;

public class SymbolicTricluster extends Tricluster {

	private SymbolicBicluster template;
	private List<Slice<String>> contexts;
	
	/**
	 * Constructor
	 * @param id Trucluster ID
	 * @param template The tricluster's template bicluster
	 * @param contextPattern The context pattern
	 */
	public SymbolicTricluster(int id, SymbolicBicluster template, PatternType contextPattern) {
		super(contextPattern, PlaidCoherency.NONE, id);
		this.template = template;
		this.contexts = new ArrayList<>();
	}
	
	public SymbolicTricluster(int id, SymbolicBicluster template, PatternType contextPattern, TimeProfile timeProfile) {
		super(contextPattern, timeProfile, PlaidCoherency.NONE, id);
		this.template = template;
		this.contexts = new ArrayList<>();
	}
	
	/**
	 * Sets the template seed
	 * @param seed the 2D matrix that represents the template (bicluster) seed
	 */
	public void setSeed(String[][] seed) {
		this.template.setSeed(seed);
	}
	
	/**
	 * Add context to the tricluster
	 * @param seed the pattern of the context's bicluster
	 * @param context the context id
	 */
	public void addContext(String[][] seed, int context) {
		this.contexts.add(new Slice<>(context, seed));
	}
	
	/**
	 * Add context to the tricluster
	 * @param context the context id
	 */
	public void addContext(int context) {
		this.contexts.add(new Slice<>(context));
	}
	
	@Override
	public PatternType getRowPattern() {
		return this.template.getRowPattern();
	}
	
	@Override
	public PatternType getColumnPattern() {
		return this.template.getColumnPattern();
	}
	
	@Override
	public Set<Integer> getContexts(){
		SortedSet<Integer> ctxs = new TreeSet<>();

		for(Slice<String> s : this.contexts)
			ctxs.add(s.getContextID());

		return ctxs;
	}

	@Override
	public Set<Integer> getRows() {
		return this.template.getRows();
	}
	
	@Override
	public Set<Integer> getColumns() {
		return this.template.getColumns();
	}
	
	/**
	 * Tricluster number of rows
	 * @return the number of rows
	 */
	public int getNumRows() {
		return this.template.numRows();
	}
	
	/**
	 * Tricluster number of columns
	 * @return the number of columns
	 */
	public int getNumCols() {
		return this.template.numColumns();
	}
	
	/**
	 * Tricluster number of contexts
	 * @return the number of contexts
	 */
	public int getNumContexts() {
		return this.contexts.size();
	}
	
	@Override
	public String toString() {
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		
		Set<Integer> rows = template.getRows();
		Set<Integer> columns = template.getColumns();
		String[][] seed = template.getSeed();
		
		StringBuilder res = new StringBuilder();
		res.append("(" + rows.size() + ", " + columns.size() + ", " + contexts.size() + "), X=[");
		for (int i : rows)
			res.append(i + ",");
		res.append("], Y=[");
		for (int i : columns)
			res.append(i + ",");
		res.append("], Z=[");
		for (Slice<String> s : contexts)
			res.append(s.getContextID() + ",");
		res.append("],");
		
		/* to delete
		if (seed != null) {
			
			res.append(" Seed=[");
			for (String i : seed)
				res.append(i + ",");
			res.append("],");
			res.append(" Seed=" + matrixToString(seed) + ", ");
		}
		else if (template.getRowPattern() != PatternType.ORDER_PRESERVING && 
				template.getColumnPattern() != PatternType.ORDER_PRESERVING &&
				getContextPattern() != PatternType.ORDER_PRESERVING){
			
			res.append(" Seed={");
			for(Slice<String> c : this.contexts)
				res.append(c.getContextID() + ":" + matrixToString(c.getPatternSeed()) + ", ");
			res.delete(res.length() - 2, res.length());
			res.append("}, ");
		}
		*/
		
		res.append(" RowPattern=" + template.getRowPattern() + ",");
		res.append(" ColumnPattern=" + template.getColumnPattern() + ",");
		res.append(" ContextPattern=" + super.getContextPattern() + ",");
		
		double missingsPerc = ((double) this.getNumberOfMissings()) / ((double) this.getSize()) * 100;
		double noisePerc = ((double) this.getNumberOfNoisy()) / ((double) this.getSize()) * 100;
		double errorsPerc = ((double) this.getNumberOfErrors()) / ((double) this.getSize()) * 100;
		
		if(super.getContextPattern().equals(PatternType.ORDER_PRESERVING))
			res.append(" TimeProfile=" + super.getTimeProfile() + ",");
		
		res.append(" %Missings=" + df.format(missingsPerc) + ",");
		res.append(" %Noise=" + df.format(noisePerc) + ",");
		res.append(" %Errors=" + df.format(errorsPerc));
		
		res.append(" PlaidCoherency=" + super.getPlaidCoherency().toString());
		
		return res.toString().replace(",]", "]");
	}	
	
	public String matrixToString(Object[][] matrix) {
		
		StringBuilder str = new StringBuilder("[");
		
		for(int row = 0; row < matrix.length; row++) {
			if(row == matrix.length - 1)
				str.append(Arrays.toString(matrix[row]) + "]");
			else
				str.append(Arrays.toString(matrix[row]) + ", ");
		}
		
		return str.toString();
	}



	@Override
	public JSONObject toStringJSON(Dataset generatedDataset) {

		JSONObject tricluster = new JSONObject();
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);

		Set<Integer> rows = template.getRows();
		Set<Integer> columns = template.getColumns();
		
		StringBuilder res = new StringBuilder();
		
		tricluster.put("#rows", rows.size());
		tricluster.put("#columns", columns.size());
		tricluster.put("#contexts", contexts.size());
		
		tricluster.put("X", rows);
		tricluster.put("Y", columns);
		tricluster.put("Z", this.getContexts());
		
		tricluster.put("PlaidCoherency", new String(super.getPlaidCoherency().toString()));
		
		tricluster.put("RowPattern", new String(template.getRowPattern().toString()));
		tricluster.put("ColumnPattern", new String(template.getColumnPattern().toString()));
		tricluster.put("ContextPattern", new String(this.getContextPattern().toString()));
		
		double missingsPerc = ((double) this.getNumberOfMissings()) / ((double) this.getSize()) * 100;
		double noisePerc = ((double) this.getNumberOfNoisy()) / ((double) this.getSize()) * 100;
		double errorsPerc = ((double) this.getNumberOfErrors()) / ((double) this.getSize()) * 100;

		if(super.getContextPattern().equals(PatternType.ORDER_PRESERVING))
			tricluster.put("TimeProfile",new String(super.getTimeProfile().toString()));
		
		tricluster.put("%Missings", df.format(missingsPerc));
		tricluster.put("%Noise", df.format(noisePerc));
		tricluster.put("%Errors", df.format(errorsPerc));
		
		tricluster.put("PlaidCoherency", new String(super.getPlaidCoherency().toString()));
		
		JSONObject data = new JSONObject();
		
		Integer[] rowsArray = new Integer[rows.size()];
	    rows.toArray(rowsArray);
		
		Integer[] colsArray = new Integer[columns.size()];
	    columns.toArray(colsArray);
		
	    for(int ctx : this.getContexts()) {
	    	
	    	JSONArray contextData = new JSONArray();
	    	
	    	for(int row = 0; row < rowsArray.length; row++){
	    		JSONArray rowData = new JSONArray();
				for(int col = 0; col < colsArray.length; col ++) 
					rowData.put(((SymbolicDataset)generatedDataset).getMatrixItem(ctx, rowsArray[row], colsArray[col]));
				contextData.put(rowData);
	    	}
	    	data.putOpt(String.valueOf(ctx), contextData);
	    }
	
	    tricluster.put("Data", data);
	    
		return tricluster;
	}

	
	@Override
	public int getSize() {
		return this.getNumRows() * this.getNumCols() * this.getNumContexts();
	}
}
