/**
 * SymbolicDataset Class
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
import org.json.JSONArray;
import org.json.JSONObject;

import com.gbic.domain.tricluster.SymbolicTricluster;
import com.gbic.domain.tricluster.Tricluster;
import com.gbic.types.Background;
import com.gbic.types.BackgroundType;
import com.gbic.utils.IOUtils;

public class SymbolicDataset extends Dataset {

	private Random r = new Random();

	private String[] alphabet;
	//The map that stores the elements
	private Map<String, String> symbolicMatrixMap;
	private boolean symmetries;

	private List<SymbolicTricluster> plantedTrics;

	/**
	 * Symbolic dataset constructor
	 * @param numRows The dataset's number of rows
	 * @param numCols The dataset's number of columns
	 * @param numCont The dataset's number of contexts
	 * @param background The dataset's background
	 * @param symmetries TO BE IMPLEMENTED, USE 'FALSE'
	 * @param alphabetL The alphabet's number of symbols
	 */
	public SymbolicDataset(int numRows, int numCols, int numCont, int numTrics, Background background, boolean symmetries,
			int alphabetL) {

		super(numRows, numCols, numCont, numTrics, background);

		this.plantedTrics = new ArrayList<>();
		this.symmetries = symmetries;

		this.alphabet = new String[alphabetL];
		int val = symmetries ? -(alphabetL / 2) : 0;
		for (int i = 0; i < alphabetL; i++, val++)
			alphabet[i] = Integer.toString(val);
		if (symmetries && alphabetL % 2 == 0)
			for (int i = alphabetL / 2; i < alphabetL; i++)
				alphabet[i] = Integer.toString(Integer.parseInt(alphabet[i]) + 1);

		this.symbolicMatrixMap = new HashMap<>();
	}

	/**
	 * 
	 * @param numRows The dataset's number of rows
	 * @param numCols The dataset's number of columns
	 * @param numCont The dataset's number of contexts
	 * @param background The dataset's background
	 * @param symmetries TO BE IMPLEMENTED, USE 'FALSE'
	 * @param alphabet Array with the alphabet symbols
	 */
	public SymbolicDataset(int numRows, int numCols, int numCont, int numTrics, Background background, boolean symmetries,
			String[] alphabet) {

		super(numRows, numCols, numCont, numTrics, background);

		this.plantedTrics = new ArrayList<>();
		this.symmetries = symmetries;
		this.alphabet = alphabet;

		this.symbolicMatrixMap = new HashMap<>();
	}

	/**
	 * Get symbol index in the alphabet
	 * @param s The symbol
	 * @return The symbol's index
	 */
	public int getSymbolIndex(String s) {

		int index = -1;

		for(int i = 0; i < this.alphabet.length && index == -1; i++) {
			if(s.equals(this.alphabet[i]))
				index = i;
		}

		return index;
	}

	/**
	 * Add a tricluster to this dataset
	 * @param tric The tricluster object
	 */
	public void addTricluster(SymbolicTricluster tric) {
		this.plantedTrics.add(tric);
	}

	/**
	 * Get the planted triclusters
	 * @return The list of planted triclusters
	 */
	public List<SymbolicTricluster> getPlantedTrics() {
		return plantedTrics;
	}

	/**
	 * Set the dataset's alphabet
	 * @param alphabet Array with the alphabet
	 */
	public void setAlphabet(String[] alphabet) {
		this.alphabet = alphabet;
	}

	/**
	 * Get the dataset's alphabet
	 * @return
	 */
	public String[] getAlphabet() {
		return this.alphabet;
	}

	public boolean hasSymmetries() {
		return symmetries;
	}

	/**
	 * Set dataset's element value
	 * @param context The context ID
	 * @param row The row ID
	 * @param column The column ID
	 * @param newItem The element's value
	 */
	public void setMatrixItem(int context, int row, int column, String newItem) {
		this.symbolicMatrixMap.put(context + ":" + row + ":" + column, newItem);
	}

	/**
	 * Get an element's value
	 * @param context The contexte ID
	 * @param row The row ID
	 * @param column The columns ID
	 * @return The element's value
	 */
	public String getMatrixItem(int context, int row, int column) {
		return this.symbolicMatrixMap.get(context + ":" + row + ":" + column);
	}

	/**
	 * Check if a dataset's value already exists
	  * @param context The contexte ID
	 * @param row The row ID
	 * @param column The columns ID
	 * @return True if the elements exists, False otherwise
	 */
	public boolean existsMatrixItem(int context, int row, int column) {
		
		return this.symbolicMatrixMap.containsKey(context + ":" + row + ":" + column);
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
	 * Generated a value for the background
	 * @return a random generated value
	 */
	public String generateBackgroundValue() {
		
		String element = null;
		
		if(super.getBackground().getType().equals(BackgroundType.UNIFORM))
			element = generateBackgroundValue(null);
		else if (super.getBackground().getType().equals(BackgroundType.DISCRETE))
			element = generateBackgroundValue(super.getBackground().getParam3());
		else if (super.getBackground().getType().equals(BackgroundType.NORMAL))
			element = generateBackgroundValue(super.getBackground().getParam1(), super.getBackground().getParam2());
		else
			element = "";
		
		return element;
	}

	
	private String generateBackgroundValue(double[] probs) {
		
		String element = null;
		
		if (probs == null)
			element = getAlphabet()[r.nextInt(getAlphabet().length)];
		else {
			double p = r.nextDouble();
			double sum = 0.0;
			int i = 0;
			while(sum < p){
				sum += probs[i];
				i++;
			}
			element = getAlphabet()[i - 1];
		}
		
		return element;
	}
	
	private String generateBackgroundValue(double mean, double sd) {
		
		String element = null;
		
		NormalDistribution n = new NormalDistribution(mean, sd);
		int vals = (int) n.sample(1)[0];
		
		if (vals < 0)
			vals = 0;
		else if (vals >= getAlphabet().length)
			vals = getAlphabet().length - 1;
		
		element = getAlphabet()[vals];
		
		return element;
		
	}

	@Override
	public String getTricsInfo() {
		StringBuilder res = new StringBuilder("Number of planted triclusters: " + plantedTrics.size()+"\r\n");
		res.append("Tricluster coverage: " + ((double) (this.getSize() - this.getBackgroundSize())) / ((double) this.getSize()) * 100 + "%\n");
		res.append("Missing values on dataset: " + ((double) this.getNumberOfMissings()) / ((double) this.getSize()) * 100 + "%\n");
		res.append("Noise values on dataset: " + ((double) this.getNumberOfNoisy()) / ((double) this.getSize()) * 100 + "%\n");
		res.append("Errors on dataset: " + ((double) this.getNumberOfErrors()) / ((double) this.getSize()) * 100 + "%\n\n\n");
		
		for(SymbolicTricluster tric : plantedTrics) {
			res.append(tric.toString() + "\r\n\n");
			for(int context : tric.getContexts()) {
				res.append("Context: " + context + "\n");
				res.append(IOUtils.printSymbolicTricluster(this.symbolicMatrixMap, context, tric.getRows(), tric.getColumns()) + "\n");
			}
		}
		return res.toString().replace(",]","]");
	}

	@Override
	public void plantMissingElements(double percBackground, double percTricluster) {

		int nrMissingsBackground = (int) (this.getBackgroundSize() * percBackground);
		//System.out.println("Total expected missings: " + nrMissingsBackground);
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
			
			//System.out.println("Missing on background " + e);
		}
		
		//System.out.println("Total Missings on back: " + this.getNumberOfMissings() + "(" + (double)this.getNumberOfMissings() / this.getBackgroundSize() +  "%)");

		if(Double.compare(percTricluster, 0.0) > 0) {
			for(SymbolicTricluster t : this.plantedTrics) {
	
				//System.out.println("Planting missings on tric " + t.getId());
				
				int nrMissingsTric = (int) (t.getSize() * percTricluster * rand.nextDouble());
	
				List<String> elems = this.getTriclusterElements(t.getId());
				String e;
				for(int k = t.getNumberOfMissings(); k < nrMissingsTric; k++) {
					do {
						e = elems.get(rand.nextInt(elems.size()));
					} while (this.isMissing(e) || !respectsOverlapConstraint(e, "Missings", percTricluster));
	
					this.addMissingElement(e);
	
					for(Integer i : this.getTricsByElem(e))
						this.getTriclusterById(i).addMissing();
	
					String[] coord = e.split(":");
	
					ctx = Integer.parseInt(coord[0]);
					row = Integer.parseInt(coord[1]);
					col = Integer.parseInt(coord[2]);
	
				}
			}
		}

		for(String e : this.getMissingElements()) {
			String[] coord = e.split(":");
			ctx = Integer.parseInt(coord[0]);
			row = Integer.parseInt(coord[1]);
			col = Integer.parseInt(coord[2]);

			this.setMatrixItem(ctx, row, col, "");
		}
	}

	/**
	 * Plant noisy elements on the dataset
	 * @param percBackground The percentage of noisy elements in the background (elements that do not belong to any tricluster)
	 * @param percTricluster The maximum percentage of noisy elements in the triclusters
	 * @param maxDeviation The noise deviation value
	 */
	public void plantNoisyElements(double percBackground, double percTricluster, int maxDeviation) {

		int nrNoiseBackground = (int) (this.getBackgroundSize() * percBackground);
		//System.out.println("Total expected noisy: " + nrNoiseBackground);
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
			
			//System.out.println("Noisy on background " + e);
		}

		//System.out.println("Total Noisy on back: " + this.getNumberOfNoisy() + "(" + (double)this.getNumberOfNoisy() / this.getBackgroundSize() +  "%)");
		
		if(Double.compare(percTricluster, 0.0) > 0) {
			for(SymbolicTricluster t : this.plantedTrics) {
	
				//System.out.println("Planting noise on tric " + t.getId());
				
				int nrNoisyTric = (int) (t.getSize() * percTricluster * rand.nextDouble());
	
				List<String> elems = this.getTriclusterElements(t.getId());
				String e;
				for(int k = t.getNumberOfNoisy(); k < nrNoisyTric; k++) {
					do {
						e = elems.get(rand.nextInt(elems.size()));
					} while (this.isMissing(e) || this.isNoisy(e) || !respectsOverlapConstraint(e, "Noisy", percTricluster));
	
					this.addNoisyElement(e);
	
					//System.out.println("Noisy on tric " + t.getId() + "on " + e);
					
					for(Integer i : this.getTricsByElem(e))
						this.getTriclusterById(i).addNoisy();
				}
			}
		}

		for(String e : this.getNoisyElements()) {
			
			String[] coord = e.split(":");
			ctx = Integer.parseInt(coord[0]);
			row = Integer.parseInt(coord[1]);
			col = Integer.parseInt(coord[2]);
			
			int symbolIndex = -1;
			
			if(this.existsMatrixItem(ctx, row, col))
				symbolIndex = this.getSymbolIndex(this.getMatrixItem(ctx, row, col));
			else {
				String newSymbol = this.generateBackgroundValue();
				this.setMatrixItem(ctx, row, col, newSymbol);
				symbolIndex = this.getSymbolIndex(newSymbol);
			}

			int deviation = 1 + rand.nextInt(maxDeviation);
			int newIndex = rand.nextBoolean() ? (symbolIndex + deviation) : (symbolIndex - deviation);
			String newValue;
			
			if(newIndex < 0)
				newValue = this.alphabet[0];
			else if(newIndex >= this.alphabet.length)
				newValue = this.alphabet[this.alphabet.length - 1];
			else
				newValue = this.alphabet[newIndex];
			 
			//System.out.println("(Noisy) OldValue: " + this.getMatrixItem(ctx, row, col) + " Deviation: " + deviation + " NewValue: " + newValue);

			this.setMatrixItem(ctx, row, col, newValue);
		}
	}


	/**
	 * Plant error elements on the dataset
	 * @param percMissing The percentage of error elements in the background (elements that do not belong to any tricluster)
	 * @param percTricluster The maximum percentage of error elements in the triclusters
	 * @param minDeviation The noise deviation value
	 */
	public void plantErrors(double percBackground, double percTricluster, int minDeviation) {

		int nrErrorsBackground = (int) (this.getBackgroundSize() * percBackground);
		//System.out.println("Total expected errors: " + nrErrorsBackground);
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
			
			//System.out.println("Error on background " + e);
			
			int newIndex = (rand.nextBoolean()) ? this.alphabet.length - 1 : 0;
			
			//System.out.println("(Error back) OldValue: " + this.getMatrixItem(ctx, row, col) + " NewValue: " + this.alphabet[newIndex]);
			
			this.setMatrixItem(ctx, row, col, this.alphabet[newIndex]);
		}

		//System.out.println("Total Errors on back: " + this.getNumberOfErrors() + "(" + (double)this.getNumberOfErrors() / this.getBackgroundSize() +  "%)");
		
		if(Double.compare(percTricluster, 0.0) > 0) {
			for(SymbolicTricluster t : this.plantedTrics) {
	
				//System.out.println("Planting errors on tric " + t.getId());
				
				int nrErrorsTric = (int) (t.getSize() * percTricluster * rand.nextDouble());
	
				List<String> elems = this.getTriclusterElements(t.getId());
				String e;
				for(int k = t.getNumberOfErrors(); k < nrErrorsTric; k++) {
					do {
						e = elems.get(rand.nextInt(elems.size()));
					} while (this.isMissing(e) || this.isNoisy(e) || this.isError(e) || !respectsOverlapConstraint(e, "Errors", percTricluster));
	
					this.addErrorElement(e);
	
					for(Integer i : this.getTricsByElem(e))
						this.getTriclusterById(i).addError();
					
					String[] coord = e.split(":");
					ctx = Integer.parseInt(coord[0]);
					row = Integer.parseInt(coord[1]);
					col = Integer.parseInt(coord[2]);
					
					int symbolIndex = -1;
					
					if(this.existsMatrixItem(ctx, row, col))
						symbolIndex = this.getSymbolIndex(this.getMatrixItem(ctx, row, col));
					else {
						String newSymbol = this.generateBackgroundValue();
						this.setMatrixItem(ctx, row, col, newSymbol);
						symbolIndex = this.getSymbolIndex(newSymbol);
					}
					
					int newIndex;
					
					do {
						newIndex = rand.nextInt(this.alphabet.length); 
					}while(Math.abs(symbolIndex - newIndex) <= minDeviation);
					
					//System.out.println("(Error tric) OldValue: " + this.getMatrixItem(ctx, row, col) +" NewValue: " + this.alphabet[newIndex]);
					
					this.setMatrixItem(ctx, row, col, this.alphabet[newIndex]);
				}
			}
		}
	}

	private boolean respectsOverlapConstraint(String elem, String type, double percTricluster) {

		boolean respects = true;

		List<Integer> trics = this.getTricsByElem(elem);

		for(int i = 0; i < trics.size() && respects; i++) {
			SymbolicTricluster t = (SymbolicTricluster) this.getTriclusterById(trics.get(i));
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

	public JSONObject getTricsInfoJSON(SymbolicDataset generatedDataset) {
		JSONObject dataset = new JSONObject();
		
		dataset.put("#DatasetRows", this.getNumRows());
		dataset.put("#DatasetColumns", this.getNumCols());
		dataset.put("#DatasetContexts", this.getNumContexts());
		JSONArray alphabet = new JSONArray();
		for(String s : this.alphabet)
			alphabet.put(s);
		dataset.put("#DatasetAlphabet", alphabet);
		
		JSONArray triclusterList = new JSONArray();
		JSONObject triclusters = new JSONObject();
		
		for(Tricluster tric : plantedTrics) 
			triclusters.put(String.valueOf(tric.getId()), tric.toStringJSON(generatedDataset));
		
		dataset.put("Triclusters", triclusters);
		
		//System.out.println("\n\n" + dataset.toString());
		
		return dataset;
	}
}
