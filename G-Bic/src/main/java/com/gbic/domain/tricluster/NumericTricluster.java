/**
 * NumericTricluster Class
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

import com.gbic.domain.bicluster.NumericBicluster;
import com.gbic.domain.dataset.Dataset;
import com.gbic.domain.dataset.NumericDataset;
import com.gbic.types.PatternType;
import com.gbic.types.PlaidCoherency;
import com.gbic.types.TimeProfile;

public class NumericTricluster<T extends Number> extends Tricluster {

	private NumericBicluster<T> template;
	private List<Slice<T>> contexts;

	/**
	 * Constructor
	 * @param id The tricluster id
	 * @param template the tricluster's bicluster template
	 * @param contextPattern the context pattern
	 * @param plaidPattern the plaid coherency
	 * @param tricContexts array with contexts ids of the tricluster
	 */
	public NumericTricluster(int id, NumericBicluster<T> template, PatternType contextPattern, PlaidCoherency plaidPattern, int[] tricContexts) {
		super(contextPattern, plaidPattern, id);
		this.template = template;
		this.contexts = new ArrayList<>();

		for(int c : tricContexts)
			contexts.add(new Slice<T>(c));

	}
	
	public NumericTricluster(int id, NumericBicluster<T> template, PatternType contextPattern, TimeProfile timeProfile,
			PlaidCoherency plaidPattern, int[] tricContexts) {
		super(contextPattern, timeProfile, plaidPattern, id);
		this.template = template;
		this.contexts = new ArrayList<>();

		for(int c : tricContexts)
			contexts.add(new Slice<T>(c));

	}

	/**
	 * Add context to tricluster
	 * @param contextID The context id
	 * @param factor the context factor
	 */
	public void addContext(int contextID, T factor) {
		this.contexts.add(new Slice<>(contextID, factor));
	}

	/**
	 * Add context to tricluster
	 * @param contextID The context id
	 */
	public void addContext(int contextID) {
		this.contexts.add(new Slice<>(contextID));
	}

	/**
	 * Add context to tricluster
	 * @param contextID The context id
	 * @param patternSeed the context pattern 
	 */
	public void addContext(int contextID, T[][] patternSeed) {
		this.contexts.add(new Slice<>(contextID, patternSeed));
	}

	// ** setters **

	/**
	 * Set the tricluster numeric seed
	 * @param seed the seed value
	 */
	public void setSeed(T seed) {
		this.template.setSeed(seed);
	}

	/**
	 * Set the tricluster pattern seed
	 * @param seed the matrix with the pattern seed
	 */
	public void setSeed(T[][] seed) {
		this.template.setSeed(seed);
	}

	/**
	 * Set the context factor
	 * @param context the context id
	 * @param factor the factor
	 */
	public void setContextFactor(int context, T factor) {
		this.contexts.get(context).setFactor(factor);
	}

	/**
	 * Set the context pattern
	 * @param context the context id
	 * @param patternSeed the matrix with the pattern
	 */
	public void setContextPattern(int context, T[][] patternSeed) {
		this.contexts.get(context).setPatternSeed(patternSeed);
	}

	/**
	 * Set the row factor
	 * @param row the row id
	 * @param factor the factor's value
	 */
	public void setRowFactor(int row, T factor) {
		this.template.setRowFactor(row, factor);
	}

	/**
	 * Set the rows' factors
	 * @param factors array of factors
	 */
	public void setRowFactors(T[] factors) {
		this.template.setRowFactors(factors);
	}

	/**
	 * Set the columns' factors
	 * @param factors array of factors
	 */
	public void setColumnFactors(T[] factors) {
		this.template.setColumnFactors(factors);
	}

	/**
	 * Set the column factor
	 * @param column the column id
	 * @param factor the factor's value
	 */
	public void setColumnFactor(int col, T factor) {
		this.template.setColumnFactor(col, factor);
	}

	// ****

	//** getters **

	/**
	 * Get the tricluster's number of factors
	 * @return the number of factors
	 */
	public int getNumContexts() {
		return this.contexts.size();
	}

	/**
	 * Get the tricluster's number of rows
	 * @return the number of rows
	 */
	public int getNumRows() {
		return this.template.numRows();
	}

	/**
	 * Get the tricluster's number of columns
	 * @return the number of columns
	 */
	public int getNumCols() {
		return this.template.numColumns();
	}

	/**
	 * Get context factor
	 * @param context the context id
	 * @return the context's factor
	 */
	public T getContextFactor(int context) {
		return this.contexts.get(context).getFactor();
	}

	/**
	 * Get row factor
	 * @param row the row id
	 * @return the row's factor
	 */
	public T getRowFactor(int row) {
		return this.template.getRowFactor(row);
	}

	/**
	 * Get column factor
	 * @param col the column id
	 * @return the column's factor
	 */
	public T getColumnFactor(int col) {
		return this.template.getColumnFactor(col);
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
	public Set<Integer> getRows(){
		return this.template.getRows();
	}

	@Override
	public Set<Integer> getColumns(){
		return this.template.getColumns();
	}

	@Override
	public Set<Integer> getContexts(){
		SortedSet<Integer> ctxs = new TreeSet<>();

		for(Slice<T> s : this.contexts)
			ctxs.add(s.getContextID());

		return ctxs;
	}

	// ****

	@Override
	public String toString() {

		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);

		Set<Integer> rows = template.getRows();
		Set<Integer> columns = template.getColumns();
		T numericSeed = template.getNumericSeed();
		T[][] patternSeed = template.getPatternSeed();

		StringBuilder res = new StringBuilder();
		res.append("Tricluster #" + this.getId() + "\n");
		res.append(" (" + rows.size() + ", " + columns.size() + ", " + contexts.size() + "), X=[");
		for (int i : rows)
			res.append(i + ",");
		res.append("], Y=[");
		for (int i : columns)
			res.append(i + ",");
		res.append("], Z=[");
		for (Slice<T> s : contexts)
			res.append(s.getContextID() + ",");
		res.append("],");
		
		res.append(" RowPattern=" + template.getRowPattern() + ",");
		res.append(" ColumnPattern=" + template.getColumnPattern() + ",");
		res.append(" ContextPattern=" + super.getContextPattern() + ",");
		
		if (numericSeed != null) {
			
			res.append(" Seed=" + df.format(numericSeed) + ", ");

			//res.append(" RowPattern=" + template.getRowPattern() + ",");

			if(template.getRowFactors().length > 0) {
				res.append(" RowFactors=[");
				for (T i : template.getRowFactors())
					res.append(df.format(i) + ",");
				res.append("],");
			}

			//res.append(" ColumnPattern=" + template.getColumnPattern() + ",");

			if(template.getColumnFactors().length > 0) {
				res.append(" ColumnFactors=[");
				for (T i : template.getColumnFactors())
					res.append(df.format(i) + ",");
				res.append("],");
			}

			//res.append(" ContextPattern=" + super.getContextPattern() + ",");

			if(this.contexts.size() > 0) {
				res.append(" ContextFactors=[");
				for (Slice<T> s : this.contexts)
					res.append(df.format(s.getFactor()) + ",");
				res.append("]");
			}
		}
		/*
		else {
			if(patternSeed != null) 
				res.append(" Seed=" + matrixToString(patternSeed) + ", ");
			else if(this.contexts.get(0).getPatternSeed() != null) {
				res.append(" Seed={");
				for(Slice<T> s : this.contexts)
					res.append(s.getContextID() + ":" + matrixToString(s.getPatternSeed()) + ", ");
				res.delete(res.length() - 2, res.length());
				res.append("}, ");
			}

			res.append(" RowPattern=" + template.getRowPattern() + ",");
			res.append(" ColumnPattern=" + template.getColumnPattern() + ",");
			res.append(" ContextPattern=" + super.getContextPattern() + ",");
		}
		*/
		double missingsPerc = ((double) this.getNumberOfMissings()) / ((double) this.getSize()) * 100;
		double noisePerc = ((double) this.getNumberOfNoisy()) / ((double) this.getSize()) * 100;
		double errorsPerc = ((double) this.getNumberOfErrors()) / ((double) this.getSize()) * 100;
		
		if(super.getContextPattern().equals(PatternType.ORDER_PRESERVING))
			res.append(" TimeProfile=" + super.getTimeProfile() + ",");
		
		res.append(" %Missings=" + df.format(missingsPerc) + ",");
		res.append(" %Noise=" + df.format(noisePerc) + ",");
		res.append(" %Errors=" + df.format(errorsPerc) + ",");
		
		res.append(" PlaidCoherency=" + super.getPlaidCoherency().toString());

		return res.toString().replace(",]", "]");
	}

	@Override
	public JSONObject toStringJSON(Dataset generatedDataset) {

		JSONObject tricluster = new JSONObject();
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);

		Set<Integer> rows = template.getRows();
		Set<Integer> columns = template.getColumns();
		T numericSeed = template.getNumericSeed();
		
		tricluster.put("#rows", rows.size());
		tricluster.put("#columns", columns.size());
		tricluster.put("#contexts", contexts.size());
		
		tricluster.put("X", rows);
		tricluster.put("Y", columns);
		tricluster.put("Z", this.getContexts());
		
		tricluster.put("RowPattern", new String(template.getRowPattern().toString()));
		tricluster.put("ColumnPattern", new String(template.getColumnPattern().toString()));
		tricluster.put("ContextPattern", new String(this.getContextPattern().toString()));
		
		if (numericSeed != null) {
	
			tricluster.put("Seed", df.format(numericSeed));

			if(template.getRowFactors().length > 0) {
				
				T[] rowFactors = template.getRowFactors();
				String[] s = new String[template.numRows()];
				
				for(int i = 0; i < template.numRows(); i++) 
					s[i] = df.format(rowFactors[i]);
				
				tricluster.put("RowFactors", Arrays.toString(s));
			}

			if(template.getColumnFactors().length > 0) {
				
				T[] colFactors = template.getColumnFactors();
				String[] s = new String[template.numColumns()];
				
				for(int i = 0; i < template.numColumns(); i++) 
					s[i] = df.format(colFactors[i]);
				
				tricluster.put("ColumnFactors", Arrays.toString(s));
			}

			if(this.contexts.size() > 0) {
				String[] s = new String[this.getNumContexts()];
				
				for(int i = 0; i < this.getNumContexts(); i++)
					s[i] = df.format(this.getContextFactor(i));
				
				tricluster.put("ContextFactors", Arrays.toString(s));
			}
		}
		
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
				for(int col = 0; col < colsArray.length; col ++) {
					double value = ((NumericDataset)generatedDataset).getMatrixItem(ctx, rowsArray[row], colsArray[col]).doubleValue();
					if(Double.compare(value, Integer.MIN_VALUE) == 0)
						rowData.put("");
					else
						rowData.put(df.format(value));
					
				}
				contextData.put(rowData);
	    	}
	    	data.putOpt(String.valueOf(ctx), contextData);
	    }
	
	    tricluster.put("Data", data);
	    
		return tricluster;
	}

	public String matrixToString(Object[][] matrix) {

		StringBuilder str = new StringBuilder("[");

		for(int row = 0; row < matrix.length; row++) {
			if(row == matrix.length - 1)
				str.append(formatResult(matrix[row]) + "]");
			else
				str.append(formatResult(matrix[row]) + ", ");
		}

		return str.toString();
	}

	private String formatResult(Object[] row) {

		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);

		StringBuilder sb = new StringBuilder("[");

		for(int i = 0; i < row.length; i++)
			sb.append(df.format(row[i]) + ", ");

		return sb.replace(sb.length() - 2, sb.length(), "]").toString();
	}

	@Override
	public int getSize() {
		
		return this.getNumRows() * this.getNumCols() * this.getNumContexts();
	}
}
