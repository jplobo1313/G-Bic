package com.gbic.domain.bicluster;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gbic.domain.dataset.Dataset;
import com.gbic.domain.dataset.HeterogeneousDataset;
import com.gbic.types.PatternType;

public class MixedBicluster extends Bicluster{

	private int id;
	private NumericBicluster<Double> numericComponent;
	private SymbolicBicluster symbolicComponent;
	
	/**
	 * @param numericComponent
	 * @param symbolicComponent
	 */
	public MixedBicluster(int id, NumericBicluster<Double> numericComponent, SymbolicBicluster symbolicComponent, SortedSet<Integer> rows) {

		super(id, rows, numericComponent.getColumns(), symbolicComponent.getColumns());
		this.id = id;
		this.numericComponent = numericComponent;
		this.symbolicComponent = symbolicComponent;
	}
	
	/**
	 * @return the numericComponent
	 */
	public Bicluster getNumericComponent() {
		return numericComponent;
	}

	/**
	 * @param numericComponent the numericComponent to set
	 */
	public void setNumericComponent(NumericBicluster<Double> numericComponent) {
		this.numericComponent = numericComponent;
	}

	/**
	 * @return the symbolicComponent
	 */
	public SymbolicBicluster getSymbolicComponent() {
		return symbolicComponent;
	}

	/**
	 * @param symbolicComponent the symbolicComponent to set
	 */
	public void setSymbolicComponent(SymbolicBicluster symbolicComponent) {
		this.symbolicComponent = symbolicComponent;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	public String toString() {
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);

		Set<Integer> rows = getRows();
		Set<Integer> columns = getColumns();
		Double numericSeed = this.numericComponent.getNumericSeed();

		StringBuilder res = new StringBuilder();
		res.append("Bicluster #" + this.getId());
		res.append(" (" + rows.size() + ", " + columns.size() + ")\nRows=[");
		for (int i : rows)
			res.append(i + ",");
		res.append("], Numeric Columns=[");
		for (int i : this.numericComponent.getColumns())
			res.append(i + ",");
		res.append("], Symbolic Columns=[");
		for (int i : this.symbolicComponent.getColumns())
			res.append(i + ",");
		res.append("]");
		
		res.append("\nNumeric Properties: ");
		res.append(" RowPattern=" + this.numericComponent.getRowPattern() + ",");
		res.append(" ColumnPattern=" + this.numericComponent.getColumnPattern() + ",");
		
		if (numericSeed != null) {
			
			res.append(" Seed=" + df.format(numericSeed) + ", ");

			if(this.numericComponent.getRowFactors().length > 0) {
				res.append(" RowFactors=[");
				for (Double i : this.numericComponent.getRowFactors())
					res.append(df.format(i) + ",");
				res.append("],");
			}
			if(this.numericComponent.getColumnFactors().length > 0) {
				res.append(" ColumnFactors=[");
				for (Double i : this.numericComponent.getColumnFactors())
					res.append(df.format(i) + ",");
				res.append("],");
			}
		}
		
		res.append(" PlaidCoherency=" + this.numericComponent.getPlaidCoherency().toString());
		
		if(this.numericComponent.getColumnPattern().equals(PatternType.ORDER_PRESERVING))
			res.append(" TimeProfile=" + this.numericComponent.getTimeProfile());
		
		res.append("\nSymbolic Properties: ");
		res.append(" RowPattern=" + this.symbolicComponent.getRowPattern() + ",");
		res.append(" ColumnPattern=" + this.symbolicComponent.getColumnPattern() + "\n");
		
		double missingsPerc = ((double) this.getNumberOfMissings()) / ((double) this.getSize()) * 100;
		double noisePerc = ((double) this.getNumberOfNoisy()) / ((double) this.getSize()) * 100;
		double errorsPerc = ((double) this.getNumberOfErrors()) / ((double) this.getSize()) * 100;
		
		res.append("%Missings=" + df.format(missingsPerc) + ",");
		res.append(" %Noise=" + df.format(noisePerc) + ",");
		res.append(" %Errors=" + df.format(errorsPerc) + ",");
		
		return res.toString().replace(",]", "]");
	}

	@Override
	public JSONObject toStringJSON(Dataset generatedDataset, boolean heterogeneous) {
		
		JSONObject bicluster = new JSONObject();
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);

		Set<Integer> rows = getRows();
		Set<Integer> columns = getColumns();
		Double numericSeed = this.numericComponent.getNumericSeed();
		
		bicluster.put("Type", "Mixed");
		
		bicluster.put("#rows", rows.size());
		bicluster.put("#columns", columns.size());
		
		bicluster.put("Rows", rows);
		bicluster.put("NumericColumns", this.numericComponent.getColumns());
		bicluster.put("SymbolicColumns", this.symbolicComponent.getColumns());
		
		JSONObject numericProperties = new JSONObject();
		
		numericProperties.put("RowPattern", this.numericComponent.getRowPattern().toString());
		numericProperties.put("ColumnPattern", this.numericComponent.getColumnPattern().toString());
		
		if (numericSeed != null) {
	
			numericProperties.put("Seed", df.format(numericSeed));

			if(this.numericComponent.getRowFactors().length > 0) {
				
				Double[] rowFactors = this.numericComponent.getRowFactors();
				String[] s = new String[this.numericComponent.numRows()];
				
				for(int i = 0; i < this.numericComponent.numRows(); i++) 
					s[i] = df.format(rowFactors[i]);
				
				numericProperties.put("RowFactors", Arrays.toString(s));
			}

			if(this.numericComponent.getColumnFactors().length > 0) {
				
				Double[] colFactors = this.numericComponent.getColumnFactors();
				String[] s = new String[this.numericComponent.numColumns()];
				
				for(int i = 0; i < this.numericComponent.numColumns(); i++) 
					s[i] = df.format(colFactors[i]);
				
				numericProperties.put("ColumnFactors", Arrays.toString(s));
			}
		}
		
		if(this.numericComponent.getColumnPattern().equals(PatternType.ORDER_PRESERVING))
			numericProperties.put("TimeProfile",this.numericComponent.getTimeProfile().toString());
		
		numericProperties.put("PlaidCoherency", this.numericComponent.getPlaidCoherency().toString());
		
		bicluster.put("NumericProperties", numericProperties);
		
		JSONObject symbolicProperties = new JSONObject();
		
		symbolicProperties.put("PlaidCoherency", this.symbolicComponent.getPlaidCoherency().toString());
		
		symbolicProperties.put("RowPattern", this.symbolicComponent.getRowPattern().toString());
		symbolicProperties.put("ColumnPattern", this.symbolicComponent.getColumnPattern().toString());

		if(this.symbolicComponent.getColumnPattern().equals(PatternType.ORDER_PRESERVING))
			symbolicProperties.put("TimeProfile", this.symbolicComponent.getTimeProfile().toString());
		
		bicluster.put("SymbolicProperties", symbolicProperties);
		
		
		double missingsPerc = ((double) this.getNumberOfMissings()) / ((double) this.getSize()) * 100;
		double noisePerc = ((double) this.getNumberOfNoisy()) / ((double) this.getSize()) * 100;
		double errorsPerc = ((double) this.getNumberOfErrors()) / ((double) this.getSize()) * 100;

		
		bicluster.put("%Missings", df.format(missingsPerc));
		bicluster.put("%Noise", df.format(noisePerc));
		bicluster.put("%Errors", df.format(errorsPerc));
		
		
		Integer[] rowsArray = new Integer[rows.size()];
	    rows.toArray(rowsArray);
		
		Integer[] colsArray = new Integer[columns.size()];
	    columns.toArray(colsArray);
	    	
    	JSONArray bicData = new JSONArray();
    	
    	for(int row = 0; row < rowsArray.length; row++){
    		JSONArray rowData = new JSONArray();
			for(int col = 0; col < colsArray.length; col ++) {
				double value = 0;
				
				if(((HeterogeneousDataset)generatedDataset).isSymbolicFeature(colsArray[col])) 
					rowData.put(((HeterogeneousDataset)generatedDataset).getSymbolicElement(rowsArray[row], colsArray[col]));
				else {
					value = ((HeterogeneousDataset)generatedDataset).getNumericElement(rowsArray[row], colsArray[col]).doubleValue();
					
					if(Double.compare(value, Integer.MIN_VALUE) == 0)
						rowData.put("");
					else
						rowData.put(df.format(value));
				}

			}
			bicData.put(rowData);
    	}
	    
	    bicluster.put("Data", bicData);
	    
		return bicluster;
	}
}
