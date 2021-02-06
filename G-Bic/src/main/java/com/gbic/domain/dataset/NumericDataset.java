/**
 * NumericDataset Class
 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @version 1.0
 */

package com.gbic.domain.dataset;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.json.JSONObject;

import com.gbic.domain.tricluster.NumericTricluster;
import com.gbic.domain.tricluster.Tricluster;
import com.gbic.types.Background;
import com.gbic.types.BackgroundType;
import com.gbic.utils.IOUtils;

public class NumericDataset<T extends Number> extends Dataset {

	private Random r = new Random();
	//The map that stores the elements
	private Map<String, T> realMatrixMap;
	private T maxM;
	private T minM;

	private List<NumericTricluster<Double>> plantedTrics;

	/**
	 * Constructs a numeric dataset
	 * @param numRows The dataset's number of rows
	 * @param numCols The dataset's number of columns
	 * @param numContexts The dataset's number of contexts
	 * @param background The dataset's background
	 * @param minM The dataset's minimum alphabet value
	 * @param maxM The dataset's maximum alphabet value
	 */
	public NumericDataset(int numRows, int numCols, int numContexts, int numTrics, Background background, T minM, T maxM) {

		super(numRows, numCols, numContexts, numTrics, background);

		plantedTrics = new ArrayList<>();
		this.minM = minM;
		this.maxM = maxM;
		this.realMatrixMap = new HashMap<>();
	}

	/**
	 * Add a tricluster to this dataset
	 * @param tric The tricluster object
	 */
	public void addTricluster(NumericTricluster<Double> tric) {
		this.plantedTrics.add(tric);
	}

	/**
	 * Get the planted triclusters
	 * @return The list of planted triclusters
	 */
	public List<NumericTricluster<Double>> getPlantedTrics() {
		return plantedTrics;
	}
	
	@Override
	public Tricluster getTriclusterById(int id) {
		
		Tricluster t = null;
		
		for(int i = 0; i < this.plantedTrics.size() && t == null; i++) {
			if(this.plantedTrics.get(i).getId() == id)
				t = this.plantedTrics.get(i);
		}
		
		return t;
	}
	
	/**
	 * Set dataset's element value
	 * @param context The context ID
	 * @param row The row ID
	 * @param column The column ID
	 * @param newItem The element's value
	 */
	public void setMatrixItem(int context, int row, int column, T newItem) {
		this.realMatrixMap.put(context + ":" + row + ":" + column, newItem);
	}
	
	/**
	 * Get an element's value
	 * @param context The contexte ID
	 * @param row The row ID
	 * @param column The columns ID
	 * @return The element's value
	 */
	public T getMatrixItem(int context, int row, int column) {
		return this.realMatrixMap.get(context + ":" + row + ":" + column);
	}

	/**
	 * Check if a dataset's value already exists
	  * @param context The contexte ID
	 * @param row The row ID
	 * @param column The columns ID
	 * @return True if the elements exists, False otherwise
	 */
	public boolean existsMatrixItem(int context, int row, int column) {
		return this.realMatrixMap.containsKey(context + ":" + row + ":" + column);
	}
	
	/**
	 * Get the dataset's max alphabet value
	 * @return the maximum of the alphabet
	 */
	public T getMaxM() {
		return maxM;
	}

	/**
	 * Get the dataset's min alphabet value
	 * @return the minimum of the alphabet
	 */
	public T getMinM() {
		return minM;
	}

	/**
	 * Generated a value for the background
	 * @return a random generated value
	 */
	public T generateBackgroundValue() {
		
		T element = null;
		
		if(super.getBackground().getType().equals(BackgroundType.UNIFORM))
			element = generateBackgroundValue(null);
		else if (super.getBackground().getType().equals(BackgroundType.DISCRETE))
			element = generateBackgroundValue(super.getBackground().getParam3());
		else if (super.getBackground().getType().equals(BackgroundType.NORMAL))
			element = generateBackgroundValue(super.getBackground().getParam1(), super.getBackground().getParam2());
		else
			element = (T) new Integer(Integer.MIN_VALUE);
		
		return element;
	}
	
	private T generateBackgroundValue(double[] probs) {
		
		T backgroundValue = null;
		
		if(probs == null) {
			if (minM instanceof Integer) 
				backgroundValue = (T) new Integer(r.nextInt((maxM.intValue() - minM.intValue()) + 1) + minM.intValue());
			else 
				backgroundValue = (T) new Double(r.nextDouble() * (maxM.doubleValue() - minM.doubleValue()) + minM.doubleValue());
		}
		else {
			
			double p = r.nextDouble();
			double sum = 0.0;
			Integer i = 0;
			while(sum < p){
				sum += probs[i];
				i++;
			}
			backgroundValue = (T) i;
		}
		
		return backgroundValue;
	}
	
	private T generateBackgroundValue(double mean, double sd) {
		
		T backgroundValue = null;
		
		NormalDistribution n = new NormalDistribution(mean, sd);

		Double vals = n.sample(1)[0];
		
		if (Double.compare(vals, minM.doubleValue()) < 0)
			vals = minM.doubleValue();
		else if (Double.compare(vals, maxM.doubleValue()) > 0)
			vals = maxM.doubleValue();
		
		if (minM instanceof Integer)
			backgroundValue = (T) new Integer(vals.intValue());
		else
			backgroundValue = (T) vals;	
		
		return backgroundValue;
	}
	
	/**
	 * Get tricluster
	 * @param id The tricluster ID
	 * @return the tricluster with the specified ID
	 */
	public NumericTricluster<? extends Number> getTricluster(int id) {

		NumericTricluster<?> res = null;

		for(int i = 0; i < this.plantedTrics.size() && res == null; i++) {
			NumericTricluster<?> t = this.plantedTrics.get(i);
			if(t.getId() == id) 
				res = t;
		}

		return res;
	}
	
	@Override
	public String getTricsInfo() {
		StringBuilder res = new StringBuilder("Number of planted triclusters: " + plantedTrics.size()+"\r\n");
		res.append("Tricluster coverage: " + ((double) (this.getSize() - this.getBackgroundSize())) / ((double) this.getSize()) * 100 + "%\n");
		res.append("Missing values on dataset: " + ((double) this.getNumberOfMissings()) / ((double) this.getSize()) * 100 + "%\n");
		res.append("Noise values on dataset: " + ((double) this.getNumberOfNoisy()) / ((double) this.getSize()) * 100 + "%\n");
		res.append("Errors on dataset: " + ((double) this.getNumberOfErrors()) / ((double) this.getSize()) * 100 + "%\n\n\n");
		
		for(NumericTricluster<? extends Number> tric : plantedTrics) {
			res.append(tric.toString() + "\r\n\n");
			for(Integer context : tric.getContexts()) {
				res.append("Context: " + context + "\n");
				res.append(IOUtils.printNumericTricluster(this.realMatrixMap, context, tric.getRows(), tric.getColumns()) + "\n");
			}
		}
		
		return res.toString().replace(",]","]");
	}
	
	public JSONObject getTricsInfoJSON(Dataset generatedDataset) {
		JSONObject dataset = new JSONObject();
		
		dataset.put("#DatasetRows", this.getNumRows());
		dataset.put("#DatasetColumns", this.getNumCols());
		dataset.put("#DatasetContexts", this.getNumContexts());
		dataset.put("#DatasetMaxValue", this.getMaxM());
		dataset.put("#DatasetMinValue", this.getMinM());
		
		JSONObject triclusters = new JSONObject();
		
		for(Tricluster tric : plantedTrics) 
			triclusters.putOpt(String.valueOf(tric.getId()), tric.toStringJSON(generatedDataset));
		
		dataset.put("Triclusters", triclusters);
		//System.out.println("\n\n" + dataset.toString());
		
		return dataset;
	}

	@Override
	public void plantMissingElements(double percBackground, double percTricluster) {

		int nrMissingsBackground = (int) (this.getBackgroundSize() * percBackground);
		Random rand = new Random();

		int row = -1;
		int col = -1;
		int ctx = -1;

		for (int k = 0; k < nrMissingsBackground; k++) {
			String e;
			do {
				row = rand.nextInt(getNumRows());
				col = rand.nextInt(getNumCols());
				ctx = rand.nextInt(getNumContexts());
				e = ctx + ":" + row + ":" + col;
			} while (this.isMissing(e) || this.isPlanted(e));

			this.addMissingElement(e);
		}
		if(Double.compare(percTricluster, 0.0) > 0) {
			for(NumericTricluster<? extends Number> t : this.plantedTrics) {
	
				double random = rand.nextDouble();
				int nrMissingsTric = (int) (t.getSize() * percTricluster * random);
	
				List<String> elems = this.getTriclusterElements(t.getId());
				String e;
				for(int k = t.getNumberOfMissings(); k < nrMissingsTric; k++) {
					do {
						e = elems.get(rand.nextInt(elems.size()));
					} while (this.isMissing(e) || !respectsOverlapConstraint(e, "Missings", percTricluster));
	
					this.addMissingElement(e);
					
					for(Integer i : this.getTricsByElem(e))
						this.getTricluster(i).addMissing();
				}
			}
		}
		
		for(String e : this.getMissingElements()) {
			
			String[] coord = e.split(":");
			ctx = Integer.parseInt(coord[0]);
			row = Integer.parseInt(coord[1]);
			col = Integer.parseInt(coord[2]);
	
			if(this.maxM instanceof Double)
				setMatrixItem(ctx, row, col, (T) new Double(Integer.MIN_VALUE));
			else
				setMatrixItem(ctx, row, col, (T) new Integer(Integer.MIN_VALUE));
		}
		
	}

	/**
	 * Plant noisy elements on the dataset
	 * @param percBackground The percentage of noisy elements in the background (elements that do not belong to any tricluster)
	 * @param percTricluster The maximum percentage of noisy elements in the triclusters
	 * @param maxDeviation The noise deviation value
	 */
	public void plantNoisyElements(double percBackground, double percTricluster, double maxDeviation) {

		int nrNoiseBackground = (int) (this.getBackgroundSize() * percBackground);
		Random rand = new Random();

		int row = -1;
		int col = -1;
		int ctx = -1;

		for (int k = 0; k < nrNoiseBackground; k++) {

			boolean stop = false;
			String e;

			do {
				row = rand.nextInt(getNumRows());
				col = rand.nextInt(getNumCols());
				ctx = rand.nextInt(getNumContexts());
				e = ctx + ":" + row + ":" + col;
				if(!this.isNoisy(e) && !this.isPlanted(e) && !this.isMissing(e))
					stop = true;

			} while (!stop);

			this.addNoisyElement(e);
		}

		if(Double.compare(percTricluster, 0.0) > 0) {
			for(NumericTricluster<? extends Number> t : this.plantedTrics) {
	
				double random = rand.nextDouble();
				int nrNoisyTric = (int) (t.getSize() * percTricluster * random);
	
				/*
				System.out.println("Tric size: " + t.getSize());
				System.out.println("Tric max perc: " + percTricluster);
				System.out.println("Tric random: " + random);
				System.out.println("Tric " + t.getId() + " - Number of noise: " + nrNoisyTric + "(" + ratio + ")\n");
				*/
				
				List<String> elems = this.getTriclusterElements(t.getId());
				String e;
				for(int k = t.getNumberOfNoisy(); k < nrNoisyTric; k++) {
					do {
						e = elems.get(rand.nextInt(elems.size()));
					} while (this.isMissing(e) || this.isNoisy(e) || !respectsOverlapConstraint(e, "Noisy", percTricluster));
	
					this.addNoisyElement(e);
					
					for(Integer i : this.getTricsByElem(e))
						this.getTricluster(i).addNoisy();
	
				}
			}
		}
		
		for(String e : this.getNoisyElements()) {
			
			String[] coord = e.split(":");

			ctx = Integer.parseInt(coord[0]);
			row = Integer.parseInt(coord[1]);
			col = Integer.parseInt(coord[2]);

			T symbolIndex = null;
			
			if(this.existsMatrixItem(ctx, row, col))
				symbolIndex = this.getMatrixItem(ctx, row, col);
			else {
				symbolIndex = this.generateBackgroundValue();
				this.setMatrixItem(ctx, row, col, symbolIndex);
			}

			double deviation;
			T newElem;
			
			if(this.maxM instanceof Double) {
				deviation = rand.nextDouble() * maxDeviation;
				deviation = rand.nextBoolean() ? deviation : -deviation;
				double newItem = symbolIndex.doubleValue() + deviation;
				
				if(Double.compare(newItem, minM.doubleValue()) < 0)
					newElem = (T) new Double(minM.doubleValue());
				else if(Double.compare(newItem, maxM.doubleValue()) > 0)
					newElem = (T) new Double(maxM.doubleValue());
				else
					newElem = (T) new Double(newItem);
				}
			else {
				deviation = 1.0 + rand.nextInt((int)maxDeviation);
				deviation = rand.nextBoolean() ? deviation : -deviation;
				//System.out.println(symbolIndex);
				int newItem = symbolIndex.intValue() + (int)deviation;
				
				if(newItem < minM.intValue())
					newElem = (T) new Integer(minM.intValue());
				else if(newItem > maxM.intValue())
					newElem = (T) new Integer(maxM.intValue());
				else
					newElem = (T) new Integer(newItem);
			}
			
			setMatrixItem(ctx, row, col, newElem);
		}

	}

	/**
	 * Plant error elements on the dataset
	 * @param percMissing The percentage of error elements in the background (elements that do not belong to any tricluster)
	 * @param percTricluster The maximum percentage of error elements in the triclusters
	 * @param minDeviation The noise deviation value
	 */
	public void plantErrors(double percBackground, double percTricluster, double minDeviation) {

		int nrErrorsBackground = (int) (this.getBackgroundSize() * percBackground);
		Random rand = new Random();

		int row = -1;
		int col = -1;
		int ctx = -1;

		for (int k = 0; k < nrErrorsBackground; k++) {

			boolean stop = false;
			String e;

			do {
				row = rand.nextInt(getNumRows());
				col = rand.nextInt(getNumCols());
				ctx = rand.nextInt(getNumContexts());
				e = ctx + ":" + row + ":" + col;

				if(!this.isError(e) && !this.isPlanted(e) && !this.isMissing(e) && !this.isNoisy(e))
					stop = true;

			} while (!stop);

			this.addErrorElement(e);

			T newElem = (rand.nextBoolean()) ? maxM : minM;

			this.setMatrixItem(ctx, row, col, newElem);
		}
		
		if(Double.compare(percTricluster, 0.0) > 0) {
			for(NumericTricluster<? extends Number> t : this.plantedTrics) {
	
				int nrErrorsTric = (int) (t.getSize() * percTricluster * rand.nextDouble());
				//System.out.println("Tric " + t.getId() + " - Number of errors: " + nrErrorsTric + "(" + ratio + ")\n");
	
				List<String> elems = this.getTriclusterElements(t.getId());
				String e;
				for(int k = t.getNumberOfErrors(); k < nrErrorsTric; k++) {
					do {
						e = elems.get(rand.nextInt(elems.size()));
					} while (this.isMissing(e) || this.isNoisy(e) || this.isError(e) || !respectsOverlapConstraint(e, "Errors", percTricluster));
	
					this.addErrorElement(e);
					
					for(Integer i : this.getTricsByElem(e))
						this.getTricluster(i).addError();
	
					String[] coord = e.split(":");
	
					ctx = Integer.parseInt(coord[0]);
					row = Integer.parseInt(coord[1]);
					col = Integer.parseInt(coord[2]);
	
					double currentElement = 0;
					
					if(this.existsMatrixItem(ctx, row, col))
						currentElement = this.getMatrixItem(ctx, row, col).doubleValue();
					else {
						currentElement = this.generateBackgroundValue().doubleValue();
						this.setMatrixItem(ctx, row, col, (T) new Double(currentElement));
					}
					
					double candidate = 0;
					T newElem;
					
					if(this.maxM instanceof Double) {
						
						do {
							candidate = minM.doubleValue() + (maxM.doubleValue() - minM.doubleValue()) *  rand.nextDouble();
						}while(Math.abs(currentElement - candidate) <= minDeviation);
						
						if(Double.compare(candidate, minM.doubleValue()) < 0)
							newElem = (T) new Double(minM.doubleValue());
						else if(Double.compare(candidate, maxM.doubleValue()) > 0)
							newElem = (T) new Double(maxM.doubleValue());
						else
							newElem = (T) new Double(candidate);
						}
					else {
						
						do {
							candidate = minM.doubleValue() + (maxM.doubleValue() - minM.doubleValue()) *  rand.nextDouble();
						}while(Math.abs(currentElement - candidate) <= minDeviation);
						
						candidate = Math.round(candidate);
						
						if(Double.compare(candidate, minM.doubleValue()) < 0)
							newElem = (T) new Integer(minM.intValue());
						else if(Double.compare(candidate, maxM.doubleValue()) > 0)
							newElem = (T) new Integer(maxM.intValue());
						else
							newElem = (T) new Integer((int)candidate);
					}
					
					setMatrixItem(ctx, row, col, newElem);
					
				}
			}
		}

	}

	private boolean respectsOverlapConstraint(String elem, String type, double percTricluster) {

		boolean respects = true;

		List<Integer> trics = this.getTricsByElem(elem);

		for(int i = 0; i < trics.size() && respects; i++) {
			NumericTricluster<? extends Number> t = this.getTricluster(trics.get(i));
			int maxAllowed = (int) (t.getSize() * percTricluster);
			
			if(type.equals("Missings") && t.getNumberOfMissings() + 1 > maxAllowed)
				respects = false;
			
			if(type.equals("Noisy") && t.getNumberOfNoisy() + 1 > maxAllowed)
				respects = false;
			
			if(type.equals("Errors") && t.getNumberOfErrors() + 1 > maxAllowed)
				respects = false;
		}

		return respects;
	}

}
