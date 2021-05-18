package com.gbic.domain.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.json.JSONObject;

import com.gbic.domain.bicluster.Bicluster;
import com.gbic.domain.bicluster.MixedBicluster;
import com.gbic.domain.bicluster.NumericBicluster;
import com.gbic.domain.bicluster.SymbolicBicluster;
import com.gbic.types.Background;
import com.gbic.types.BackgroundType;
import com.gbic.utils.IOUtils;
import com.gbic.utils.RandomObject;

public class HeterogeneousDataset extends Dataset {

	private Random r = RandomObject.getInstance();
	private int numericCols;
	private int symbolicCols;
	private SortedSet<Integer> numericFeatures;
	private SortedSet<Integer> symbolicFeatures;

	//List<MixedBiclusters> plantedBiclusters;
	private Background numericBackground;
	private Background symbolicBackground;

	//The map that stores the elements
	private boolean realValued;
	private Map<String, Double> numericMatrix;
	private Double maxM;
	private Double minM;

	private String[] alphabet;
	//The map that stores the elements
	private Map<String, String> symbolicMatrix;

	private List<NumericBicluster<Double>> plantedNumericBics;
	private List<SymbolicBicluster> plantedSymbolicBics;
	private List<MixedBicluster> plantedMixedBics;

	public HeterogeneousDataset(int numRows, int numericCols, int symbolicCols, int numBics, Background numericBackground, 
			Background symbolicBackground, Double minM, Double maxM, boolean realValued, String[] alphabet, int alphabetL) {

		super(numRows, numericCols + symbolicCols, numBics);

		this.numericCols = numericCols;
		this.symbolicCols = symbolicCols;
		this.numericBackground = numericBackground;
		this.symbolicBackground = symbolicBackground;
		this.minM = minM;
		this.maxM = maxM;
		this.realValued = realValued;
		
		if(alphabet == null) {
			this.alphabet = new String[alphabetL];
			for (int i = 0; i < alphabetL; i++)
				this.alphabet[i] = Integer.toString(i);
		}
		else
			this.alphabet = alphabet;

		this.numericFeatures = new TreeSet<Integer>();
		this.symbolicFeatures = new TreeSet<Integer>();

		this.numericMatrix = new HashMap<>();
		this.symbolicMatrix = new HashMap<>();

		for(int c = 0; c < super.getNumCols(); c++) {
			if(c >= this.numericCols)
				this.symbolicFeatures.add(c);
			else
				this.numericFeatures.add(c);
		}

		this.plantedMixedBics = new ArrayList<>();
		this.plantedSymbolicBics = new ArrayList<>();
		this.plantedNumericBics = new ArrayList<>();
	}


	public int getTotalPlantedBics() {
		return this.plantedMixedBics.size() + this.plantedNumericBics.size() + this.plantedSymbolicBics.size();
	}

	public String getBicsInfo() {
		StringBuilder res = new StringBuilder("Total of planted biclusters: " + getTotalPlantedBics() + "\r\n");
		res.append("Number of planted numeric biclusters: " + plantedNumericBics.size()+"\r\n");
		res.append("Number of planted symbolic biclusters: " + plantedSymbolicBics.size()+"\r\n");
		res.append("Number of planted mixed biclusters: " + plantedMixedBics.size()+"\r\n");
		res.append("Bicluster coverage: " + ((double) (this.getSize() - this.getBackgroundSize())) / ((double) this.getSize()) * 100 + "%\n");
		res.append("Missing values on dataset: " + ((double) this.getNumberOfMissings()) / ((double) this.getSize()) * 100 + "%\n");
		res.append("Noise values on dataset: " + ((double) this.getNumberOfNoisy()) / ((double) this.getSize()) * 100 + "%\n");
		res.append("Errors on dataset: " + ((double) this.getNumberOfErrors()) / ((double) this.getSize()) * 100 + "%\n\n\n");

		if(plantedNumericBics.size() > 0)
			res.append("*** Numeric Biclusters *** \n\n");

		for(NumericBicluster<? extends Number> bic : plantedNumericBics) {
			res.append(bic.toString() + "\r\n\n");
			res.append(IOUtils.printNumericBicluster(this.numericMatrix, bic.getRows(), bic.getColumns()) + "\n");
		}

		if(plantedSymbolicBics.size() > 0)
			res.append("\n*** Symbolic Biclusters *** \n\n");

		for(SymbolicBicluster bic : plantedSymbolicBics) {
			res.append(bic.toString() + "\r\n\n");
			res.append(IOUtils.printSymbolicBicluster(this.symbolicMatrix, bic.getRows(), bic.getColumns()) + "\n");
		}

		if(plantedMixedBics.size() > 0)
			res.append("\n*** Mixed Biclusters *** \n\n");

		for(MixedBicluster bic : plantedMixedBics) {
			res.append(bic.toString() + "\r\n\n");
			res.append(IOUtils.printMixedBicluster(this.numericMatrix, this.symbolicMatrix, bic.getRows(), bic.getColumns()) + "\n");
		}

		return res.toString().replace(",]","]");
	}

	public JSONObject getBicsInfoJSON(Dataset generatedDataset) {
		JSONObject dataset = new JSONObject();

		dataset.put("#DatasetRows", this.getNumRows());
		dataset.put("#DatasetColumns", this.getNumCols());
		dataset.put("#DatasetNumericColumns", this.getNumericCols());
		dataset.put("#DatasetSymbolicColumns", this.getSymbolicCols());
		dataset.put("#DatasetMaxValue", this.getMaxM());
		dataset.put("#DatasetMinValue", this.getMinM());
		dataset.put("#DatasetAlphabet", this.getAlphabet());

		JSONObject numericBiclusters = new JSONObject();
		JSONObject symbolicBiclusters = new JSONObject();
		JSONObject mixedBiclusters = new JSONObject();

		for(NumericBicluster bic : plantedNumericBics) 
			numericBiclusters.putOpt(String.valueOf(bic.getId()), bic.toStringJSON(generatedDataset, true));

		for(SymbolicBicluster bic : plantedSymbolicBics) 
			symbolicBiclusters.putOpt(String.valueOf(bic.getId()), bic.toStringJSON(generatedDataset, true));

		for(MixedBicluster bic : plantedMixedBics) 
			mixedBiclusters.putOpt(String.valueOf(bic.getId()), bic.toStringJSON(generatedDataset, true));

		dataset.put("NumericBiclusters", numericBiclusters);
		dataset.put("SymbolicBiclusters", symbolicBiclusters);
		dataset.put("MixedBiclusters", mixedBiclusters);
		//System.out.println("\n\n" + dataset.toString());

		return dataset;
	}

	/**
	 * @return the numericCols
	 */
	public int getNumericCols() {
		return numericCols;
	}

	/**
	 * @param numericCols the numericCols to set
	 */
	public void setNumericCols(int numericCols) {
		this.numericCols = numericCols;
	}

	/**
	 * @return the symbolicCols
	 */
	public int getSymbolicCols() {
		return symbolicCols;
	}

	/**
	 * @param symbolicCols the symbolicCols to set
	 */
	public void setSymbolicCols(int symbolicCols) {
		this.symbolicCols = symbolicCols;
	}

	/**
	 * @return the numericFeatures
	 */
	public SortedSet<Integer> getNumericFeatures() {
		return numericFeatures;
	}

	/**
	 * @param numericFeatures the numericFeatures to set
	 */
	public void setNumericFeatures(SortedSet<Integer> numericFeatures) {
		this.numericFeatures = numericFeatures;
	}

	/**
	 * @return the symbolicFeatures
	 */
	public SortedSet<Integer> getSymbolicFeatures() {
		return symbolicFeatures;
	}

	/**
	 * @param symbolicFeatures the symbolicFeatures to set
	 */
	public void setSymbolicFeatures(SortedSet<Integer> symbolicFeatures) {
		this.symbolicFeatures = symbolicFeatures;
	}

	/**
	 * @return the plantedNumericBics
	 */
	public List<NumericBicluster<Double>> getPlantedNumericBics() {
		return plantedNumericBics;
	}

	/**
	 * @param plantedNumericBics the plantedNumericBics to set
	 */
	public void setPlantedNumericBics(List<NumericBicluster<Double>> plantedNumericBics) {
		this.plantedNumericBics = plantedNumericBics;
	}

	/**
	 * @return the plantedSymbolicBics
	 */
	public List<SymbolicBicluster> getPlantedSymbolicBics() {
		return plantedSymbolicBics;
	}

	/**
	 * @param plantedSymbolicBics the plantedSymbolicBics to set
	 */
	public void setPlantedSymbolicBics(List<SymbolicBicluster> plantedSymbolicBics) {
		this.plantedSymbolicBics = plantedSymbolicBics;
	}

	/**
	 * @return the plantedMixedBics
	 */
	public List<MixedBicluster> getPlantedMixedBics() {
		return plantedMixedBics;
	}

	/**
	 * @param plantedMixedBics the plantedMixedBics to set
	 */
	public void setPlantedMixedBics(List<MixedBicluster> plantedMixedBics) {
		this.plantedMixedBics = plantedMixedBics;
	}

	public void addPlantedMixedBic(MixedBicluster bic) {
		this.plantedMixedBics.add(bic);
	}

	public void addPlantedNumericBic(NumericBicluster bic) {
		this.plantedNumericBics.add(bic);
	}

	public void addPlantedSymbolicBic(SymbolicBicluster bic) {
		this.plantedSymbolicBics.add(bic);
	}

	/**
	 * @return the numericBackground
	 */
	public Background getNumericBackground() {
		return numericBackground;
	}

	/**
	 * @param numericBackground the numericBackground to set
	 */
	public void setNumericBackground(Background numericBackground) {
		this.numericBackground = numericBackground;
	}

	/**
	 * @return the symbolicBackground
	 */
	public Background getSymbolicBackground() {
		return symbolicBackground;
	}

	/**
	 * @param symbolicBackground the symbolicBackground to set
	 */
	public void setSymbolicBackground(Background symbolicBackground) {
		this.symbolicBackground = symbolicBackground;
	}

	/**
	 * @return the realValued
	 */
	public boolean isRealValued() {
		return realValued;
	}

	/**
	 * @param realValued the realValued to set
	 */
	public void setRealValued(boolean realValued) {
		this.realValued = realValued;
	}

	/**
	 * @return the numericMatrix
	 */
	public Map<String, Double> getNumericMatrix() {
		return numericMatrix;
	}

	/**
	 * @param numericMatrix the numericMatrix to set
	 */
	public void setNumericMatrix(Map<String, Double> numericMatrix) {
		this.numericMatrix = numericMatrix;
	}

	public void setNumericElement(int row, int col, Double value) {
		this.numericMatrix.put(row + ":" + col, value);
	}

	public Double getNumericElement(int row, int col) {
		return this.numericMatrix.get(row + ":" + col);
	}

	/**
	 * @return the maxM
	 */
	public Double getMaxM() {
		return maxM;
	}

	/**
	 * @param maxM the maxM to set
	 */
	public void setMaxM(Double maxM) {
		this.maxM = maxM;
	}

	/**
	 * @return the minM
	 */
	public Double getMinM() {
		return minM;
	}

	/**
	 * @param minM the minM to set
	 */
	public void setMinM(Double minM) {
		this.minM = minM;
	}

	/**
	 * @return the alphabet
	 */
	public String[] getAlphabet() {
		return alphabet;
	}

	/**
	 * @param alphabet the alphabet to set
	 */
	public void setAlphabet(String[] alphabet) {
		this.alphabet = alphabet;
	}

	/**
	 * @return the symbolicMatrix
	 */
	public Map<String, String> getSymbolicMatrix() {
		return symbolicMatrix;
	}

	/**
	 * @param symbolicMatrix the symbolicMatrix to set
	 */
	public void setSymbolicMatrix(Map<String, String> symbolicMatrix) {
		this.symbolicMatrix = symbolicMatrix;
	}

	public void setSymbolicElement(int row, int col, String value) {
		this.symbolicMatrix.put(row + ":" + col, value);
	}

	public String getSymbolicElement(int row, int col) {
		return this.symbolicMatrix.get(row + ":" + col);
	}

	public boolean existsSymbolicElement(int row, int col) {
		return this.symbolicMatrix.containsKey(row + ":" + col);
	}

	public boolean existsNumericElement(int row, int col) {
		return this.numericMatrix.containsKey(row + ":" + col);
	}

	public boolean isSymbolicFeature(int col) {
		return this.symbolicFeatures.contains(col);
	}

	/**
	 * Generated a value for the background
	 * @return a random generated value
	 */
	public Double generateNumericBackgroundValue() {

		Double element = null;

		if(getNumericBackground().getType().equals(BackgroundType.UNIFORM))
			element = generateNumericBackgroundValue(null);
		else if (getNumericBackground().getType().equals(BackgroundType.DISCRETE))
			element = generateNumericBackgroundValue(getNumericBackground().getParam3());
		else if (getNumericBackground().getType().equals(BackgroundType.NORMAL))
			element = generateNumericBackgroundValue(getNumericBackground().getParam1(), getNumericBackground().getParam2());
		else
			element = new Double(Integer.MIN_VALUE);

		return element;
	}

	private Double generateNumericBackgroundValue(double[] probs) {

		Double backgroundValue = null;

		if(probs == null) {
			if (!realValued) 
				backgroundValue = new Double(r.nextInt((maxM.intValue() - minM.intValue()) + 1) + minM.intValue());
			else 
				backgroundValue = new Double(r.nextDouble() * (maxM.doubleValue() - minM.doubleValue()) + minM.doubleValue());
		}
		else {

			double p = r.nextDouble();
			double sum = 0.0;
			Integer i = 0;
			while(sum < p){
				sum += probs[i];
				i++;
			}
			backgroundValue = new Double(i);
		}

		return backgroundValue;
	}

	private Double generateNumericBackgroundValue(double mean, double sd) {

		Double backgroundValue = null;

		NormalDistribution n = new NormalDistribution(mean, sd);

		Double vals = n.sample(1)[0];

		if (Double.compare(vals, minM.doubleValue()) < 0)
			vals = minM;
		else if (Double.compare(vals, maxM.doubleValue()) > 0)
			vals = maxM;

		if (!realValued)
			backgroundValue = new Double(vals.intValue());
		else
			backgroundValue = vals;	

		return backgroundValue;
	}

	/**
	 * Get Numeric Bicluster
	 * @param id The Bicluster ID
	 * @return the Bicluster with the specified ID
	 */
	public NumericBicluster<? extends Number> getNumericBicluster(int id) {

		NumericBicluster<?> res = null;

		for(int i = 0; i < this.plantedNumericBics.size() && res == null; i++) {
			NumericBicluster<?> t = this.plantedNumericBics.get(i);
			if(t.getId() == id) 
				res = t;
		}

		return res;
	}

	/**
	 * Get Symbolic Bicluster
	 * @param id The Bicluster ID
	 * @return the Bicluster with the specified ID
	 */
	public SymbolicBicluster getSymbolicBicluster(int id) {

		SymbolicBicluster res = null;

		for(int i = 0; i < this.plantedSymbolicBics.size() && res == null; i++) {
			SymbolicBicluster t = this.plantedSymbolicBics.get(i);
			if(t.getId() == id) 
				res = t;
		}

		return res;
	}

	/**
	 * Get Symbolic Bicluster
	 * @param id The Bicluster ID
	 * @return the Bicluster with the specified ID
	 */
	public MixedBicluster getMixedBicluster(int id) {

		MixedBicluster res = null;

		for(int i = 0; i < this.plantedMixedBics.size() && res == null; i++) {
			MixedBicluster t = this.plantedMixedBics.get(i);
			if(t.getId() == id) 
				res = t;
		}

		return res;
	}

	@Override
	public Bicluster getBiclusterById(int id) {

		Bicluster b = null;

		for(NumericBicluster<Double> n : this.plantedNumericBics) {
			if(n.getId() == id)
				b = n;

			if(b != null)
				break;
		}

		if(b == null) {
			for(SymbolicBicluster s : this.plantedSymbolicBics) {
				if(s.getId() == id)
					b = s;

				if(b != null)
					break;
			}
		}

		if(b == null) {
			for(MixedBicluster m : this.plantedMixedBics) {
				if(m.getId() == id)
					b = m;

				if(b != null)
					break;
			}
		}

		return b;
	}

	/**
	 * Generated a value for the background
	 * @return a random generated value
	 */
	public String generateSymbolicBackgroundValue() {

		String element = null;

		if(getSymbolicBackground().getType().equals(BackgroundType.UNIFORM))
			element = generateSymbolicBackgroundValue(null);
		else if (getSymbolicBackground().getType().equals(BackgroundType.DISCRETE))
			element = generateSymbolicBackgroundValue(getSymbolicBackground().getParam3());
		else if (getSymbolicBackground().getType().equals(BackgroundType.NORMAL))
			element = generateSymbolicBackgroundValue(getSymbolicBackground().getParam1(), getSymbolicBackground().getParam2());
		else
			element = "";

		return element;
	}


	private String generateSymbolicBackgroundValue(double[] probs) {

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

	private String generateSymbolicBackgroundValue(double mean, double sd) {

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
	public void plantMissingElements(double percBackground, double percBicluster) {

		int nrMissingsBackground = (int) (this.getBackgroundSize() * percBackground);
		Random rand = RandomObject.getInstance();

		int row = -1;
		int col = -1;

		for (int k = 0; k < nrMissingsBackground; k++) {
			String e;
			do {
				row = rand.nextInt(getNumRows());
				col = rand.nextInt(getNumCols());
				e = row + ":" + col;
			} while (this.isMissing(e) || this.isPlanted(e));

			this.addMissingElement(e);
		}
		if(Double.compare(percBicluster, 0.0) > 0) {
			
			List<Bicluster> bicsList = new ArrayList<Bicluster>();
			bicsList.addAll(this.plantedNumericBics);
			bicsList.addAll(this.plantedSymbolicBics);
			bicsList.addAll(this.plantedMixedBics);
			
			for(Bicluster t : bicsList) {

				double random = rand.nextDouble();
				int nrMissingsBic = (int) (t.getSize() * percBicluster * random);

				List<String> elems = this.getBiclusterElements(t.getId());
				String e;
				for(int k = t.getNumberOfMissings(); k < nrMissingsBic; k++) {
					do {
						e = elems.get(rand.nextInt(elems.size()));
					} while (this.isMissing(e) || !respectsOverlapConstraint(e, "Missings", percBicluster));

					this.addMissingElement(e);

					for(Integer i : this.getBicsByElem(e))
						this.getBiclusterById(i).addMissing();
				}
			}
		}


		for(String e : this.getMissingElements()) {

			String[] coord = e.split(":");
			row = Integer.parseInt(coord[0]);
			col = Integer.parseInt(coord[1]);

			if(this.isSymbolicFeature(col)) {
				this.setSymbolicElement(row, col, "");
			}
			else {
				this.setNumericElement(row, col, (double) Integer.MIN_VALUE);
			}
		}

	}

	/**
	 * Plant noisy elements on the dataset
	 * @param percBackground The percentage of noisy elements in the background (elements that do not belong to any Bicluster)
	 * @param percBicluster The maximum percentage of noisy elements in the biclusters
	 * @param maxDeviation The noise deviation value
	 */
	public void plantNoisyElements(double percBackground, double percBicluster, double maxDeviation) {

		int nrNoiseBackground = (int) (this.getBackgroundSize() * percBackground);
		Random rand = RandomObject.getInstance();

		int row = -1;
		int col = -1;

		for (int k = 0; k < nrNoiseBackground; k++) {

			boolean stop = false;
			String e;

			do {
				row = rand.nextInt(getNumRows());
				col = rand.nextInt(getNumCols());
				e = row + ":" + col;
				if(!this.isNoisy(e) && !this.isPlanted(e) && !this.isMissing(e))
					stop = true;

			} while (!stop);

			this.addNoisyElement(e);
		}

		if(Double.compare(percBicluster, 0.0) > 0) {

			List<Bicluster> bicsList = new ArrayList<Bicluster>();
			bicsList.addAll(this.plantedNumericBics);
			bicsList.addAll(this.plantedSymbolicBics);
			bicsList.addAll(this.plantedMixedBics);

			for(Bicluster t : bicsList) {

				double random = rand.nextDouble();
				int nrNoisyBic = (int) (t.getSize() * percBicluster * random);

				/*
				System.out.println("Tric size: " + t.getSize());
				System.out.println("Tric max perc: " + percTricluster);
				System.out.println("Tric random: " + random);
				System.out.println("Tric " + t.getId() + " - Number of noise: " + nrNoisyTric + "(" + ratio + ")\n");
				 */

				List<String> elems = this.getBiclusterElements(t.getId());
				String e;
				for(int k = t.getNumberOfNoisy(); k < nrNoisyBic; k++) {
					do {
						e = elems.get(rand.nextInt(elems.size()));
					} while (this.isMissing(e) || this.isNoisy(e) || !respectsOverlapConstraint(e, "Noisy", percBicluster));

					this.addNoisyElement(e);

					for(Integer i : this.getBicsByElem(e))
						this.getBiclusterById(i).addNoisy();

				}
			}	
		}

		for(String e : this.getNoisyElements()) {

			String[] coord = e.split(":");

			row = Integer.parseInt(coord[0]);
			col = Integer.parseInt(coord[1]);

			if(this.isSymbolicFeature(col)) {
				int symbolIndex = -1;

				if(this.existsSymbolicElement(row, col))
					symbolIndex = this.getSymbolIndex(this.getSymbolicElement(row, col));
				else {
					String newSymbol = this.generateSymbolicBackgroundValue();
					this.setSymbolicElement(row, col, newSymbol);
					symbolIndex = this.getSymbolIndex(newSymbol);
				}

				int deviation = 1 + rand.nextInt((int)maxDeviation);
				int newIndex = rand.nextBoolean() ? (symbolIndex + deviation) : (symbolIndex - deviation);
				String newValue;

				if(newIndex < 0)
					newValue = this.alphabet[0];
				else if(newIndex >= this.alphabet.length)
					newValue = this.alphabet[this.alphabet.length - 1];
				else
					newValue = this.alphabet[newIndex];

				//System.out.println("(Noisy) OldValue: " + this.getMatrixItem(ctx, row, col) + " Deviation: " + deviation + " NewValue: " + newValue);

				this.setSymbolicElement(row, col, newValue);
			}
			else {
				Double symbolIndex = null;

				if(this.existsNumericElement(row, col))
					symbolIndex = this.getNumericElement(row, col);
				else {
					symbolIndex = this.generateNumericBackgroundValue();
					this.setNumericElement(row, col, symbolIndex);
				}

				double deviation;
				Double newElem;

				if(this.realValued) {
					deviation = rand.nextDouble() * maxDeviation;
					deviation = rand.nextBoolean() ? deviation : -deviation;
					double newItem = symbolIndex.doubleValue() + deviation;

					if(Double.compare(newItem, minM.doubleValue()) < 0)
						newElem = new Double(minM.doubleValue());
					else if(Double.compare(newItem, maxM.doubleValue()) > 0)
						newElem = new Double(maxM.doubleValue());
					else
						newElem = new Double(newItem);
				}
				else {
					deviation = 1.0 + rand.nextInt((int)maxDeviation);
					deviation = rand.nextBoolean() ? deviation : -deviation;
					int newItem = symbolIndex.intValue() + (int)deviation;

					if(newItem < minM.intValue())
						newElem = new Double(minM.intValue());
					else if(newItem > maxM.intValue())
						newElem = new Double(maxM.intValue());
					else
						newElem = new Double(newItem);
				}

				this.setNumericElement(row, col, newElem);
			}
		}	
	}

	/**
	 * Plant error elements on the dataset
	 * @param percMissing The percentage of error elements in the background (elements that do not belong to any Bicluster)
	 * @param percBicluster The maximum percentage of error elements in the biclusters
	 * @param minDeviation The noise deviation value
	 */
	public void plantErrors(double percBackground, double percBicluster, double minDeviation) {

		int nrErrorsBackground = (int) (this.getBackgroundSize() * percBackground);
		Random rand = RandomObject.getInstance();

		int row = -1;
		int col = -1;

		for (int k = 0; k < nrErrorsBackground; k++) {

			boolean stop = false;
			String e;

			do {
				row = rand.nextInt(getNumRows());
				col = rand.nextInt(getNumCols());
				e = row + ":" + col;

				if(!this.isError(e) && !this.isPlanted(e) && !this.isMissing(e) && !this.isNoisy(e))
					stop = true;

			} while (!stop);

			this.addErrorElement(e);

			if(this.isSymbolicFeature(col)) {
				int newIndex = (rand.nextBoolean()) ? this.alphabet.length - 1 : 0;
				this.setSymbolicElement(row, col, this.alphabet[newIndex]);
			}
			else {
				Double newElem = (rand.nextBoolean()) ? maxM : minM;
				if(this.realValued)
					newElem = new Double(newElem.intValue());
				this.setNumericElement(row, col, newElem);
			}
		}

		if(Double.compare(percBicluster, 0.0) > 0) {
			
			List<Bicluster> bicsList = new ArrayList<Bicluster>();
			bicsList.addAll(this.plantedNumericBics);
			bicsList.addAll(this.plantedSymbolicBics);
			bicsList.addAll(this.plantedMixedBics);
			
			for(Bicluster t : bicsList) {

				int nrErrorsBic = (int) (t.getSize() * percBicluster * rand.nextDouble());
				//System.out.println("Tric " + t.getId() + " - Number of errors: " + nrErrorsTric + "(" + ratio + ")\n");

				List<String> elems = this.getBiclusterElements(t.getId());
				String e;
				for(int k = t.getNumberOfErrors(); k < nrErrorsBic; k++) {
					do {
						e = elems.get(rand.nextInt(elems.size()));
					} while (this.isMissing(e) || this.isNoisy(e) || this.isError(e) || !respectsOverlapConstraint(e, "Errors", percBicluster));

					this.addErrorElement(e);

					for(Integer i : this.getBicsByElem(e))
						this.getBiclusterById(i).addError();

					String[] coord = e.split(":");

					row = Integer.parseInt(coord[0]);
					col = Integer.parseInt(coord[1]);

					if(this.isSymbolicFeature(col)) {
						int symbolIndex = -1;
						
						if(this.existsSymbolicElement(row, col))
							symbolIndex = this.getSymbolIndex(this.getSymbolicElement(row, col));
						else {
							String newSymbol = this.generateSymbolicBackgroundValue();
							this.setSymbolicElement(row, col, newSymbol);
							symbolIndex = this.getSymbolIndex(newSymbol);
						}
						
						int newIndex;
						
						do {
							newIndex = rand.nextInt(this.alphabet.length); 
						}while(Math.abs(symbolIndex - newIndex) <= minDeviation);
						
						//System.out.println("(Error tric) OldValue: " + this.getMatrixItem(ctx, row, col) +" NewValue: " + this.alphabet[newIndex]);
						
						this.setSymbolicElement(row, col, this.alphabet[newIndex]);
					}
					else {
						double currentElement = 0;

						if(this.existsNumericElement(row, col))
							currentElement = this.getNumericElement(row, col).doubleValue();
						else {
							currentElement = this.generateNumericBackgroundValue().doubleValue();
							this.setNumericElement(row, col, new Double(currentElement));
						}

						double candidate = 0;
						Double newElem;

						if(this.realValued) {

							do {
								candidate = minM.doubleValue() + (maxM.doubleValue() - minM.doubleValue()) *  rand.nextDouble();
							}while(Math.abs(currentElement - candidate) <= minDeviation);

							if(Double.compare(candidate, minM.doubleValue()) < 0)
								newElem = new Double(minM.doubleValue());
							else if(Double.compare(candidate, maxM.doubleValue()) > 0)
								newElem = new Double(maxM.doubleValue());
							else
								newElem = new Double(candidate);
						}
						else {

							do {
								candidate = minM.doubleValue() + (maxM.doubleValue() - minM.doubleValue()) *  rand.nextDouble();
							}while(Math.abs(currentElement - candidate) <= minDeviation);

							candidate = Math.round(candidate);

							if(Double.compare(candidate, minM.doubleValue()) < 0)
								newElem = new Double(minM.intValue());
							else if(Double.compare(candidate, maxM.doubleValue()) > 0)
								newElem = new Double(maxM.intValue());
							else
								newElem = new Double((int)candidate);
						}
						setNumericElement(row, col, newElem);
					}
				}
			}
		}
	}

	private boolean respectsOverlapConstraint(String elem, String type, double percBicluster) {

		boolean respects = true;

		List<Integer> bics = this.getBicsByElem(elem);

		//Mudar isto depois pq esta bastante feio
		for(int i = 0; i < bics.size() && respects; i++) {
			Bicluster t = getBiclusterById(bics.get(i));

			int maxAllowed = (int) (t.getSize() * percBicluster);

			if(type.equals("Missings") && t.getNumberOfMissings() + 1 > maxAllowed)
				respects = false;

			if(type.equals("Noisy") && t.getNumberOfNoisy() + 1 > maxAllowed)
				respects = false;

			if(type.equals("Errors") && t.getNumberOfErrors() + 1 > maxAllowed)
				respects = false;
		}

		return respects;
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
}
