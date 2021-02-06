/**
 * GTricService Class
 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @version 1.0
 */
package com.gbic.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.json.JSONObject;

import com.gbic.domain.dataset.Dataset;
import com.gbic.domain.dataset.NumericDataset;
import com.gbic.domain.dataset.SymbolicDataset;
import com.gbic.generator.NumericDatasetGenerator;
import com.gbic.generator.SymbolicDatasetGenerator;
import com.gbic.generator.TriclusterDatasetGenerator;
import com.gbic.tests.OutputWriterThread;
import com.gbic.types.Background;
import com.gbic.types.BackgroundType;
import com.gbic.types.Contiguity;
import com.gbic.types.Distribution;
import com.gbic.types.PatternType;
import com.gbic.types.PlaidCoherency;
import com.gbic.types.TimeProfile;
import com.gbic.utils.IOUtils;
import com.gbic.utils.OverlappingSettings;
import com.gbic.utils.QualitySettings;
import com.gbic.utils.TriclusterPattern;
import com.gbic.utils.TriclusterStructure;

public class GBicService extends Observable implements Observer {

	private String path = "data/";
	private String filename = "";
	private boolean singleFile;
	
	//Helper class to organize the tricluster's patterns
	public class TriclusterPatternWrapper{
		
		String rowPattern;
		String columnPattern;
		String contextPattern;
		String timeProfile;
		String imagePath;
		
		public TriclusterPatternWrapper(String rowPattern, String columnPattern, String contextPattern,
				String imagePath) {
			this.rowPattern = rowPattern;
			this.columnPattern = columnPattern;
			this.contextPattern = contextPattern;
			this.imagePath = imagePath;
		}
		
		public TriclusterPatternWrapper(String rowPattern, String columnPattern, String contextPattern,
				String timeProfile, String imagePath) {
			this.rowPattern = rowPattern;
			this.columnPattern = columnPattern;
			this.contextPattern = contextPattern;
			this.timeProfile = timeProfile;
			this.imagePath = imagePath;
		}

		public String getRowPattern() {
			return rowPattern;
		}

		public String getColumnPattern() {
			return columnPattern;
		}

		public String getContextPattern() {
			return contextPattern;
		}

		public String getImagePath() {
			return imagePath;
		}
		
		/**
		 * @return the timeProfile
		 */
		public String getTimeProfile() {
			return timeProfile;
		}

		/**
		 * @param timeProfile the timeProfile to set
		 */
		public void setTimeProfile(String timeProfile) {
			this.timeProfile = timeProfile;
		}

		public String toString() {
			return rowPattern + "|" + columnPattern + "|" + contextPattern;
		}
		
	}
	
	private JSONObject triclustersJSON;
	private Dataset generatedDataset;
	
	private BiConsumer<Integer, Integer> progressUpdate;
	private Consumer<String> messageUpdate;
	private int currentProgress;
	
	private String state;
	//Paths to files with symbolic and numerics patterns
	private static final String SYMBOLIC_PATTERNS_PATH = "src/main/java/com/gtric/app/service/symbolicPatterns.csv";
	private static final String NUMERIC_PATTERNS_PATH = "src/main/java/com/gtric/app/service/numericPatterns.csv";
	private List<String> numericDatasetDataTypes;
	private List<String> datasetBackground;
	private List<String> distributions;
	private List<String> contiguity;
	private List<String> plaidCoherency;
	private List<TriclusterPatternWrapper> symbolicPatterns;
	private List<TriclusterPatternWrapper> numericPatterns;
	private List<String> symbolType;
	
	//Dataset Properties
	//Numeric or Sumbolic
	private String datasetType;
	private int numRows;
	private int numCols;
	private int numCtxs;
	
	//Numeric dataset
	private boolean realValued;
	private double minM;
	private double maxM;
	
	//symbolic dataset
	private boolean defaultSymbols;
	private int numberOfSymbols;
	private List<String> listOfSymbols;
	
	private Background background;
	
	//TriclusterProperties
	private int numTrics;
	private TriclusterStructure tricStructure;
	
	//Tricluster's Patters
	List<TriclusterPattern> tricPatterns;
	
	//Overlapping
	private OverlappingSettings overlappingSettings;
	
	//Extras
	private QualitySettings qualitySettings;

	/**
	 * Constructor
	 */
	public GBicService() {
		
		this.currentProgress = 0;
		
		numericDatasetDataTypes = new ArrayList<>();
		fillNumericDatasetDataTypes();
		
		datasetBackground = new ArrayList<>();
		fillBackgound();
		
		distributions = new ArrayList<>();
		fillDistributions();
		
		contiguity = new ArrayList<>();
		fillContiguity();
		
		plaidCoherency = new ArrayList<>();
		fillPlaidCoherency();
		
		symbolicPatterns = new ArrayList<>();
		fillSymbolicPatterns();
		
		numericPatterns = new ArrayList<>();
		fillNumericPatterns();
		
		symbolType = new ArrayList<>();
		fillSymbolType();
	}
	
	private void fillSymbolType() {
		
		symbolType.add("Default");
		symbolType.add("Custom");
	}

	/**
	 * @return the symbol types available
	 */
	public List<String> getSymbolType(){
		return this.symbolType;
	}
	
	/**
	 * Set whether the symbolic dataset will use the dafault symbols or not
	 * @param b true is dataset is composed by the default symbols, false otherwise
	 */
	public void setDefaultSymbolBoolean(boolean b) {
		this.defaultSymbols = b;
	}
	
	/**
	 * Set whether the output should be written in a single or multiple files
	 * @param b true if single file, false otherwise
	 */
	public void setSingleFileOutput(boolean b) {
		this.singleFile = b;
	}
	
	/**
	 * Check if the output will be written in a single file
	 * @return true if output is written in a single file, false otherwise
	 */
	public boolean isSingleFileOutput() {
		return this.singleFile;
	}
	
	private void fillNumericPatterns() {
		
		BufferedReader patternReader = null;
		patternReader = new BufferedReader(new InputStreamReader(GBicService.class.getResourceAsStream("numericPatterns.csv")));
		
		String row;
		
		try {
			while ((row = patternReader.readLine()) != null) {
			    String[] data = row.split(",");
			    String img = "";
			    if(data.length == 4)
			    	img = data[3];
			    numericPatterns.add(new TriclusterPatternWrapper(data[0], data[1], data[2], img));
			    //System.out.println("Added: (" + data[0] + ", " + data[1] + ", " + data[2] + ")");
			    
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			patternReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void fillSymbolicPatterns() {
		
		BufferedReader patternReader = null;
		//System.out.println(GTricService.class.getResource("symbolicPatterns.csv").getPath());
		patternReader = new BufferedReader(new InputStreamReader(GBicService.class.getResourceAsStream("symbolicPatterns.csv")));
		
		String row;
		
		try {
			while ((row = patternReader.readLine()) != null) {
			    String[] data = row.split(",");
			    String img = "";
			    if(data.length == 4)
			    	img = data[3];
			    symbolicPatterns.add(new TriclusterPatternWrapper(data[0], data[1], data[2], img));
			    //System.out.println("Added: (" + data[0] + ", " + data[1] + ", " + data[2] + ")");
			    
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			patternReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void fillPlaidCoherency() {
		
		plaidCoherency.add("Additive");
		plaidCoherency.add("Multiplicative");
		plaidCoherency.add("Interpoled");
		plaidCoherency.add("None");
		plaidCoherency.add("No Overlapping");
	}

	private void fillContiguity() {
		
		contiguity.add("None");
		contiguity.add("Columns");
		contiguity.add("Contexts");
	}

	private void fillDistributions() {
		
		distributions.add("Uniform");
		distributions.add("Normal");
	}

	private void fillBackgound() {
		
		datasetBackground.add("Uniform");
		datasetBackground.add("Normal");
		datasetBackground.add("Discrete");
		datasetBackground.add("Missing");
	}

	/**
	 * Set the dataset's data type
	 * @param type the data tpe
	 */
	public void setDatasetType(String type) {
		this.datasetType = type;
	}
	
	private void fillNumericDatasetDataTypes() {
		
		numericDatasetDataTypes.add("Integer");
		numericDatasetDataTypes.add("Real Valued");
	}

	/**
	 * @return the list of available data types
	 */
	public List<String> getDataTypes(){
		return this.numericDatasetDataTypes;
	}
	
	/**
	 * @return the list of available background's distributions
	 */
	public List<String> getDatasetBackground(){
		return this.datasetBackground;
	}
	
	/**
	 * @return the list of available distributions
	 */
	public List<String> getDistributions(){
		return this.distributions;
	}
	
	/**
	 * Get the available contiguities
	 * @return the list with the contiguity values
	 */
	public List<String> getContiguity(){
		return this.contiguity;
	}
	
	/**
	 * Get the available plaid coherencies
	 * @return list with plaid coherencies
	 */
	public List<String> getPlaidCoherency(){
		return this.plaidCoherency;
	}
	
	/**
	 * Get tricluster's symbolic patterns
	 * @return list with the patterns
	 */
	public List<TriclusterPatternWrapper> getSymbolicPatterns(){
		return this.symbolicPatterns;
	}
	
	/**
	 * Get tricluster's numeric patterns
	 * @return list with numeric patterns
	 */
	public List<TriclusterPatternWrapper> getNumericPatterns(){
		return this.numericPatterns;
	}
	
	/**
	 * Set numeric dataset's properties
	* @param numRows Dataset's number of rows
	 * @param numCols Dataset's number of columns
	 * @param numCtxs Dataset's number of contexts
	 * @param realValued boolean that indicates if the dataset is real valued
	 * @param minM The alphabet min
	 * @param maxM The alphabet max
	  * @param background The dataset's background (NORMAL, UNIFORM, DISCRETE or MISSING)
	 * @param backgroundParam1 The background's first parameter (mean or min)
	 * @param backgroundParam2 The background's second parameter (std or max)
	 * @param backgroundParam3 The background's third parameter (probabilities array)
	 */
	public void setDatasetProperties(int numRows, int numCols, int numCtxs, boolean realValued, double minM, double maxM, String background,
			double backgroundParam1, double backgroundParam2, double[] backgroundParam3) {
		
		this.numRows = numRows;
		this.numCols = numCols;
		this.numCtxs = numCtxs;
		this.realValued = realValued;
		this.minM = minM;
		this.maxM = maxM;
		
		BackgroundType backgroundType = null;
		
		if(background.equals("Normal"))
			backgroundType = BackgroundType.NORMAL;

		if(background.equals("Uniform"))
			backgroundType = BackgroundType.UNIFORM;

		if(background.equals("Discrete"))
			backgroundType = BackgroundType.DISCRETE;
		
		if(background.equals("Missing"))
			backgroundType = BackgroundType.MISSING;
		
		
		this.background = new Background(backgroundType);
		this.background.setParam1(backgroundParam1);
		this.background.setParam2(backgroundParam2);
		this.background.setParam3(backgroundParam3);
	}
	
	/**
	 * Set symbolic dataset's properties
	 * @param numRows Dataset's number of rows
	 * @param numCols Dataset's number of columns
	 * @param numCtxs Dataset's number of contexts
	 * @param defaultSymbols Boolean that indicates if the user used the dafult symbols or the custom ones
	 * @param alphabetLength The size of the alphabet
	 * @param symbols The array with the alphabet's symbols
	 * @param background The dataset's background (NORMAL, UNIFORM, DISCRETE or MISSING)
	 * @param backgroundParam1 The background's first parameter (mean or min)
	 * @param backgroundParam2 The background's second parameter (std or max)
	 * @param backgroundParam3 The background's third parameter (probabilities array)
	 */
	public void setDatasetProperties(int numRows, int numCols, int numCtxs, boolean defaultSymbols, int alphabetLength, String[] symbols, String background,
			double backgroundParam1, double backgroundParam2, double[] backgroundParam3) {
		
		this.numRows = numRows;
		this.numCols = numCols;
		this.numCtxs = numCtxs;
		this.defaultSymbols = defaultSymbols;
		
		if(symbols == null)
			this.numberOfSymbols = alphabetLength;
		else
			this.listOfSymbols = Arrays.asList(symbols);
		
		BackgroundType backgroundType = null;
		
		if(background.equals("Normal"))
			backgroundType = BackgroundType.NORMAL;
		else if(background.equals("Uniform"))
			backgroundType = BackgroundType.UNIFORM;
		else if(background.equals("Discrete")) 
			backgroundType = BackgroundType.DISCRETE;
		else
			backgroundType = BackgroundType.MISSING;
		
		
		this.background = new Background(backgroundType);
		this.background.setParam1(backgroundParam1);
		this.background.setParam2(backgroundParam2);
		this.background.setParam3(backgroundParam3);
	}
	
	/**
	 * Set tricluster's structure properties
	 * @param numTrics The number of triclusters to plant
	 * @param rowDist The row distribution (normal or uniform)
	 * @param rowsParam1 The first parameter of the row distribution (mean or min)
	 * @param rowsParam2 The second parameter of the row distribution (std or max)
	 * @param colDist The column distribution (normal or uniform)
	 * @param columnsParam1 The first parameter of the column distribution (mean or min)
	 * @param columnsParam2 The second parameter of the column distribution (std or max)
	 * @param ctxDist The context distribution (normal or uniform)
	 * @param contextsParam1 The first parameter of the context distribution (mean or min)
	 * @param contextsParam2 The second parameter of the context distribution (std or max)
	 * @param contiguity Which dimension is contiguous (COLUMNS or CONTEXTS) or NONE
	 */
	public void setTriclustersProperties(int numTrics, String rowDist, double rowDistParam1, double rowDistParam2, String colDist, 
			double colDistParam1, double colDistParam2, String ctxDist, double ctxDistParam1, double ctxDistParam2, String contiguity) {
		
		this.numTrics = numTrics;
		this.tricStructure = new TriclusterStructure();
		
		if(rowDist.equals("Normal"))
			this.tricStructure.setRowsDistribution(Distribution.NORMAL);
		if(rowDist.equals("Uniform"))
			this.tricStructure.setRowsDistribution(Distribution.UNIFORM);
		
		if(colDist.equals("Normal"))
			this.tricStructure.setColumnsDistribution(Distribution.NORMAL);
		if(colDist.equals("Uniform"))
			this.tricStructure.setColumnsDistribution(Distribution.UNIFORM);
		
		if(ctxDist.equals("Normal"))
			this.tricStructure.setContextsDistribution(Distribution.NORMAL);
		if(ctxDist.equals("Uniform"))
			this.tricStructure.setContextsDistribution(Distribution.UNIFORM);
		
		
		this.tricStructure.setRowsParam1(rowDistParam1);
		this.tricStructure.setRowsParam2(rowDistParam2);
		
		this.tricStructure.setColumnsParam1(colDistParam1);
		this.tricStructure.setColumnsParam2(colDistParam2);
		
		this.tricStructure.setContextsParam1(ctxDistParam1);
		this.tricStructure.setContextsParam2(ctxDistParam2);
		
		if(contiguity.equals("Columns"))
			this.tricStructure.setContiguity(Contiguity.COLUMNS);
		else if(contiguity.equals("Contexts"))
			this.tricStructure.setContiguity(Contiguity.CONTEXTS);
		else
			this.tricStructure.setContiguity(Contiguity.NONE);
	}
	
	
	public void setTriclusterPatterns(List<TriclusterPatternWrapper> patterns) {
		
		this.tricPatterns = new ArrayList<>();
		
		for(TriclusterPatternWrapper p : patterns) {
			TriclusterPattern tp = new TriclusterPattern(getPatternType(p.rowPattern), getPatternType(p.columnPattern),
					getPatternType(p.contextPattern));
			if(tp.getContextsPattern().equals(PatternType.ORDER_PRESERVING))
				tp.setTimeProfile(getTimeProfile(p.getTimeProfile()));
			System.out.println(getPatternType(p.rowPattern));
			this.tricPatterns.add(tp);
		}
	}
	
	private PatternType getPatternType(String type) {
		
		PatternType res = null;
		System.out.println(type);
		if(type.contains("Constant"))
			res = PatternType.CONSTANT;
		else if(type.contains("Additive"))
			res = PatternType.ADDITIVE;
		else if(type.contains("Multiplicative"))
			res = PatternType.MULTIPLICATIVE;
		else if(type.contains("Order Preserving"))
			res = PatternType.ORDER_PRESERVING;
		else
			res = PatternType.NONE;
		
		return res;
	}
	
	private TimeProfile getTimeProfile(String type) {
		
		TimeProfile res = null;
		System.out.println(type);
		if(type.contains("Random"))
			res = TimeProfile.RANDOM;
		else if(type.contains("Up-Regulated"))
			res = TimeProfile.MONONICALLY_INCREASING;
		else
			res = TimeProfile.MONONICALLY_DECREASING;
		
		return res;
	}
	
	/**
	 * 
	 * @param plaidCoherency The plaid coherency
	 * @param percOverlappingTrics The percentage of dataset's triclusters that can overlap
	 * @param maxOverlappingTrics The maximum amount of trics that can overlap together
	 * @param percOverlappingElements The maximum percentage of elements tha can be share between 
	 * triclusters (relative to the smallest tric)
	 * @param percOverlappingRows The maximum percentage of overlapping in the row dimension
	 * @param percOverlappingColumns The maximum percentage of overlapping in the column dimension
	 * @param percOverlappingContexts The maximum percentage of overlapping in the context dimension
	 */
	public void setOverlappingSettings(String plaidCoherency, double percOverlappingTrics, int maxOverlappingTrics, double percOverlappingElements,
			double percOverlappingRows, double percOverlappingColumns, double percOverlappingContexts) {
		
		this.overlappingSettings = new OverlappingSettings();
		
		if(plaidCoherency.equals("Additive"))
			this.overlappingSettings.setPlaidCoherency(PlaidCoherency.ADDITIVE);
		else if (plaidCoherency.equals("Multiplicative"))
			this.overlappingSettings.setPlaidCoherency(PlaidCoherency.MULTIPLICATIVE);
		else if (plaidCoherency.equals("Interpoled"))
			this.overlappingSettings.setPlaidCoherency(PlaidCoherency.INTERPOLED);
		else if (plaidCoherency.equals("NONE"))
			this.overlappingSettings.setPlaidCoherency(PlaidCoherency.NONE);
		else
			this.overlappingSettings.setPlaidCoherency(PlaidCoherency.NO_OVERLAPPING);
		
		
		this.overlappingSettings.setPercOfOverlappingTrics(percOverlappingTrics);
		this.overlappingSettings.setMaxTricsPerOverlappedArea(maxOverlappingTrics);
		this.overlappingSettings.setMaxPercOfOverlappingElements(percOverlappingElements);
		this.overlappingSettings.setPercOfOverlappingRows(percOverlappingRows);
		this.overlappingSettings.setPercOfOverlappingColumns(percOverlappingColumns);
		this.overlappingSettings.setPercOfOverlappingContexts(percOverlappingContexts);
	}
	
	/**
	 * Set the Quality properties 
	 * @param percMissingsOnBackground The percentage of missings on dataset's background
	 * @param percMissingsOnTrics The maximum percentage of missings on planted triclusters
	 * @param percNoiseOnBackground  The percentage of noise on dataset's background
	 * @param percNoiseOnTrics The maximum percentage of noise on planted triclusters
	 * @param noiseDeviation The noise deviation value
	 * @param percErrorsOnBackground The percentage of errors on dataset's background
	 * @param percErrorsOnTrics The maximum percentage of errors on planted triclusters
	 */
	public void setQualitySettings(double percMissingsOnBackground, double percMissingsOnTrics, double percNoiseOnBackground, double percNoiseOnTrics,
			double noiseDeviation, double percErrorsOnBackground, double percErrorsOnTrics) {
		
		this.qualitySettings = new QualitySettings();
		
		this.qualitySettings.setPercMissingsOnBackground(percMissingsOnBackground);
		this.qualitySettings.setPercMissingsOnTrics(percMissingsOnTrics);
		this.qualitySettings.setPercNoiseOnBackground(percNoiseOnBackground);
		this.qualitySettings.setPercNoiseOnTrics(percNoiseOnTrics);
		this.qualitySettings.setNoiseDeviation(noiseDeviation); 
		this.qualitySettings.setPercErrorsOnBackground(percErrorsOnBackground);
		this.qualitySettings.setPercErrorsOnTrics(percErrorsOnTrics);
	}
	
	/**
	 * Get the generated dataset
	 * @return the generated dataset
	 */
	public Dataset getGeneratedDataset() {
		return this.generatedDataset;
	}
	
	/**
	 * Generated a numeric dataset
	 * @throws Exception
	 */
	public void generateNumericDataset() throws Exception {
		
		long startTimeGen;
		long stopTimeGen;
		long startTimeBics;
		long stopTimeBics;
		NumericDatasetGenerator generator;
		
		printDatasetSettings();
		
		this.progressUpdate.accept(5, 100);
		this.messageUpdate.accept("Generating Background...");
		
		String tricDataFileName;
		String datasetFileName;
		
		if(this.filename.isEmpty()) {
			if(tricPatterns.size() == 1) {
			tricDataFileName = "tric_" + tricPatterns.get(0).getRowsPattern().name().charAt(0) 
					+ tricPatterns.get(0).getColumnsPattern().name().charAt(0) 
					+ tricPatterns.get(0).getContextsPattern().name().charAt(0) 
					+ "_" + numRows + "x" + numCols + "x" + numCtxs;

			datasetFileName = "data_" + tricPatterns.get(0).getRowsPattern().name().charAt(0) 
					+ tricPatterns.get(0).getColumnsPattern().name().charAt(0) 
					+ tricPatterns.get(0).getContextsPattern().name().charAt(0) 
					+ "_" + numRows + "x" + numCols + "x" + numCtxs;
			}
			else {
				tricDataFileName = "tric_multiple" + "_" + numRows + "x" + numCols + "x" + numCtxs;
				datasetFileName = "data_multiple" + "_" + numRows + "x" + numCols + "x" + numCtxs;
			}
		}
		else {
			tricDataFileName = this.filename + "_trics";
			datasetFileName = this.filename + "_data";
		}
		
		startTimeGen = System.currentTimeMillis();
		generator = new NumericDatasetGenerator(realValued, numRows, numCols, numCtxs, numTrics, background, minM, maxM);
		stopTimeGen = System.currentTimeMillis();
		
		generator.addObserver(this);
		
		System.out.println("(TricDatasetGenerator) Execution Time: " + ((double)(stopTimeGen - startTimeGen)) / 1000);
		
		updateProgressStatusAndMessage(20, "Generating Triclusters...");
		
		startTimeBics = System.currentTimeMillis();
		NumericDataset generatedDataset = (NumericDataset) generator.generate(tricPatterns, tricStructure, overlappingSettings);
		stopTimeBics = System.currentTimeMillis();

		System.out.println("(GeneratePlaidRealTrics) Execution Time: " + ((double) (stopTimeBics - startTimeBics)) / 1000);
		
		updateProgressStatusAndMessage(80, "Generating Missings...");
		System.out.println("Generating Missings...");
		generatedDataset.plantMissingElements(this.qualitySettings.getPercMissingsOnBackground(), this.qualitySettings.getPercMissingsOnTrics());
		
		updateProgressStatusAndMessage(85, "Generating Noise...");
		System.out.println("Generating Noise...");
		generatedDataset.plantNoisyElements(this.qualitySettings.getPercNoiseOnBackground(), this.qualitySettings.getPercNoiseOnTrics(), this.qualitySettings.getNoiseDeviation());
		
		updateProgressStatusAndMessage(90, "Generating Errors...");
		System.out.println("Generating Errors...");
		generatedDataset.plantErrors(this.qualitySettings.getPercErrorsOnBackground(), this.qualitySettings.getPercErrorsOnTrics(), this.qualitySettings.getNoiseDeviation());
		
		updateProgressStatusAndMessage(95, "Writing output...");
		
		this.generatedDataset = generatedDataset;
		this.generatedDataset.destroyElementsMap();
		
		System.gc();
		
		int mb = 1024*1024;

		//Getting the runtime reference from system
		Runtime runtime = Runtime.getRuntime();
		
		System.out.println("##### Heap utilization statistics [MB] #####");

		//Print used memory
		System.out.println("Used Memory:"
			+ (runtime.totalMemory() - runtime.freeMemory()) / mb);

		//Print free memory
		System.out.println("Free Memory:"
			+ runtime.freeMemory() / mb);

		//Print total available memory
		System.out.println("Total Memory:" + runtime.totalMemory() / mb);

		//Print Maximum available memory
		System.out.println("Max Memory:" + runtime.maxMemory() / mb);
		
		saveResult(generatedDataset, tricDataFileName, datasetFileName);
		
		//updateProgressStatusAndMessage(100, "Completed!");
	}
	
	/**
	 * Get the JSON with the triclusters info
	 * @return the JSON Object
	 */
	public JSONObject getTriclustersJSON() {
		return this.triclustersJSON;
	}
	
	/**
	 * Generated a symbolic dataset
	 * @throws Exception
	 */
	public void generateSymbolicDataset() throws Exception {
	
		long startTimeGen;
		long stopTimeGen;
		long startTimeBics;
		long stopTimeBics;

		printDatasetSettings();
		
		TriclusterDatasetGenerator generator = null;
		
		this.progressUpdate.accept(5, 100);
		this.messageUpdate.accept("Generating Background...");
		
		startTimeGen = System.currentTimeMillis();

		if(!this.defaultSymbols) {
			String[] symbols = this.listOfSymbols.toArray(new String[0]);
			generator = new SymbolicDatasetGenerator(numRows,numCols, numCtxs, numTrics, background, symbols,
					false);
		}
		else
			generator = new SymbolicDatasetGenerator(numRows,numCols, numCtxs, numTrics, background, numberOfSymbols, false);
		stopTimeGen = System.currentTimeMillis();

		generator.addObserver(this);
		
		System.out.println("(BicMatrixGenerator) Execution Time: " + ((double)(stopTimeGen - startTimeGen))/1000 + " secs");

		updateProgressStatusAndMessage(20, "Generating Triclusters...");
		
		startTimeBics = System.currentTimeMillis();
		SymbolicDataset generatedDataset = (SymbolicDataset) generator.generate(this.tricPatterns, tricStructure, this.overlappingSettings);
		stopTimeBics = System.currentTimeMillis();
		
		updateProgressStatusAndMessage(80, "Generating Missings...");
		System.out.println("Generating Missings...");
		generatedDataset.plantMissingElements(this.qualitySettings.getPercMissingsOnBackground(), this.qualitySettings.getPercMissingsOnTrics());
		
		updateProgressStatusAndMessage(85, "Generating Noise...");
		System.out.println("Generating Noise...");
		generatedDataset.plantNoisyElements(this.qualitySettings.getPercNoiseOnBackground(), this.qualitySettings.getPercNoiseOnTrics(), 
				(int)this.qualitySettings.getNoiseDeviation());
		
		updateProgressStatusAndMessage(90, "Generating Errors...");
		System.out.println("Generating Errors...");
		generatedDataset.plantErrors(this.qualitySettings.getPercErrorsOnBackground(), this.qualitySettings.getPercErrorsOnTrics(), 
				(int)this.qualitySettings.getNoiseDeviation());
		
		String tricDataFileName;
		String datasetFileName;
		
		updateProgressStatusAndMessage(95, "Writing output...");
		
		if(filename.isEmpty()) {
			if(tricPatterns.size() == 1) {
			tricDataFileName = "tric_" + tricPatterns.get(0).getRowsPattern().name().charAt(0) 
					+ tricPatterns.get(0).getColumnsPattern().name().charAt(0) 
					+ tricPatterns.get(0).getContextsPattern().name().charAt(0) 
					+ "_" + numRows + "x" + numCols + "x" + numCtxs;

			datasetFileName = "data_" + tricPatterns.get(0).getRowsPattern().name().charAt(0) 
					+ tricPatterns.get(0).getColumnsPattern().name().charAt(0) 
					+ tricPatterns.get(0).getContextsPattern().name().charAt(0) 
					+ "_" + numRows + "x" + numCols + "x" + numCtxs;
			}
			else {
				tricDataFileName = "tric_multiple" + "_" + numRows + "x" + numCols + "x" + numCtxs;
				datasetFileName = "data_multiple" + "_" + numRows + "x" + numCols + "x" + numCtxs;
			}
		}
		else {
			tricDataFileName = filename + "_trics";
			datasetFileName = filename + "_data";
		}
		
		this.generatedDataset = generatedDataset;
		this.generatedDataset.destroyElementsMap();
		
		System.gc();
		
		int mb = 1024*1024;

		//Getting the runtime reference from system
		Runtime runtime = Runtime.getRuntime();
		
		System.out.println("##### Heap utilization statistics [MB] #####");

		//Print used memory
		System.out.println("Used Memory:"
			+ (runtime.totalMemory() - runtime.freeMemory()) / mb);

		//Print free memory
		System.out.println("Free Memory:"
			+ runtime.freeMemory() / mb);

		//Print total available memory
		System.out.println("Total Memory:" + runtime.totalMemory() / mb);

		//Print Maximum available memory
		System.out.println("Max Memory:" + runtime.maxMemory() / mb);
		
		saveResult(generatedDataset, tricDataFileName, datasetFileName);
		
		//updateProgressStatusAndMessage(100, "Completed!");
	}
	
	/**
	 * Save the generated datset's output
	 * @param generatedDataset The dataset
	 * @param tricDataFileName The name of the tricluster's files
	 * @param datasetFileName The name of the dataset file
	 * @throws Exception
	 */
	public void saveResult(NumericDataset generatedDataset, String tricDataFileName, String datasetFileName) throws Exception {

		System.out.println("Writting output...");
		
		IOUtils.writeFile(path, tricDataFileName + ".txt",generatedDataset.getTricsInfo(), false);
		System.out.println("Triclusters txt file written!");
		
		this.triclustersJSON = generatedDataset.getTricsInfoJSON(generatedDataset);
		IOUtils.writeFile(path, tricDataFileName + ".json", this.triclustersJSON.toString(), false);
		System.out.println("Triclusters JSON file written!");
		
		
		this.triclustersJSON = this.triclustersJSON.getJSONObject("Triclusters");
		
		int threshold = generatedDataset.getNumRows() / 10;
		
		if (threshold == 0)
			threshold++;
		
		int step = generatedDataset.getNumRows() / threshold;

		ExecutorService es = null;
		
		if(!this.isSingleFileOutput())
			es = Executors.newCachedThreadPool();
		
		for(int s = 0; s < step; s++)
			if(this.isSingleFileOutput())
				IOUtils.writeFile(path, datasetFileName + ".tsv", IOUtils.matrixToStringColOriented(generatedDataset, threshold, s, s==0), s!=0);	
			else {
				Thread t = new Thread(new OutputWriterThread(path, datasetFileName, s, threshold, generatedDataset));
				es.execute(t);
			}
		

		if(!this.isSingleFileOutput()) {
			es.shutdown();
			es.awaitTermination(5, TimeUnit.MINUTES);
		}
		
		System.out.println("Dataset tsv file written!");

	}
	
	/**
	 * Save the generated datset's output
	 * @param generatedDataset The dataset
	 * @param tricDataFileName The name of the tricluster's files
	 * @param datasetFileName The name of the dataset file
	 * @throws Exception
	 */
	public void saveResult(SymbolicDataset generatedDataset, String tricDataFileName, String datasetFileName) throws Exception {

		System.out.println("Writting output...");
		
		IOUtils.writeFile(path, tricDataFileName + ".txt",generatedDataset.getTricsInfo(), false);
		System.out.println("Triclusters txt file written!");
		
		this.triclustersJSON = generatedDataset.getTricsInfoJSON(generatedDataset);
		
		IOUtils.writeFile(path, tricDataFileName + ".json", this.triclustersJSON.toString(), false);
		System.out.println("Triclusters JSON file written");
		
		this.triclustersJSON = this.triclustersJSON.getJSONObject("Triclusters");

		int threshold = generatedDataset.getNumRows() / 10;
		
		if (threshold == 0)
			threshold++;
		
		int step = generatedDataset.getNumRows() / threshold;

		ExecutorService es = null;
		
		if(!this.isSingleFileOutput())
			es = Executors.newCachedThreadPool();
		
		for(int s = 0; s <= step; s++)
			if(this.isSingleFileOutput())
				IOUtils.writeFile(path, datasetFileName + ".tsv", IOUtils.matrixToStringColOriented(generatedDataset, threshold, s, s==0), s!=0);
			else {
				Thread t = new Thread(new OutputWriterThread(path, datasetFileName, s, threshold, generatedDataset));
				es.execute(t);
			}

		if(!this.isSingleFileOutput()) {
			es.shutdown();
			es.awaitTermination(5, TimeUnit.MINUTES);
		}
		
		System.out.println("Dataset tsv file written!");

	}

	public void setProgressUpdate(BiConsumer<Integer, Integer> progressUpdate) {
        this.progressUpdate = progressUpdate ;
    }
	
	public void setMessageUpdate(Consumer<String> messageUpdate) {
        this.messageUpdate = messageUpdate ;
    }
	
	/**
	 * Set the output's path
	 * @param path the path
	 */
	public void setPath(String path) {
		this.path = path;
	}
	
	/**
	 * Set output's file name
	 * @param filename the file name
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		
		String[] tokens = ((String) arg).split(",");
		String[] msg = tokens[1].split(":");
		String[] tricInfo = msg[1].split(" ");
		int currentTric = Integer.parseInt(tricInfo[1]) + 1;
		
		double progress = (60 / ((double) this.numTrics));
		updateProgressStatusAndMessage(this.currentProgress + (int)progress, "Generating Triclusters (" + currentTric + "/" + this.numTrics + ")...");
	}
	
	private void updateProgressStatusAndMessage(int prog, String msg) {
		
		this.currentProgress = prog;
		this.progressUpdate.accept(prog, 100);
		this.messageUpdate.accept(msg);
	}
	
	private void printDatasetSettings() {
		
		System.out.println("*** Dataset Properties ***");
		System.out.println("NumRows: " + this.numRows);
		System.out.println("NumCols: " + this.numCols);
		System.out.println("NumCtxs: " + this.numCtxs);
		
		if(this.datasetType.equals("Numeric")) {
			System.out.println("RealValued: " + this.realValued );
			System.out.println("Min: " + this.minM);
			System.out.println("Max: " + this.maxM);
		}
		else {
			System.out.println("Default symbols: " + this.defaultSymbols);
			if(this.defaultSymbols)
				System.out.println("Number of Symbols: " + this.numberOfSymbols);
			else
				System.out.println("List of Symbols: " + Arrays.toString(this.listOfSymbols.toArray()));
		}
		
		System.out.println("Background: " + this.background.getType().toString());
		if(this.background.getType().equals(BackgroundType.NORMAL)){
			System.out.println("Background Mean: " + background.getParam1());
			System.out.println("Background Std: " + background.getParam2());
		}
		else if(this.background.getType().equals(BackgroundType.DISCRETE))
			System.out.println("Probabilities: " + Arrays.toString(this.background.getParam3()));
		
		System.out.println("\n*** Triclusters Properties ***");
		System.out.println("NumTrics: " + this.numTrics);
		System.out.println("RowDist: " + this.tricStructure.getRowsDistribution());
		System.out.println("Row Param1: " + this.tricStructure.getRowsParam1());
		System.out.println("Row Param2: " + this.tricStructure.getRowsParam2());
		System.out.println("ColDist: " + this.tricStructure.getColumnsDistribution());
		System.out.println("Col Param1: " + this.tricStructure.getColumnsParam1());
		System.out.println("Col Param2: " + this.tricStructure.getColumnsParam2());
		System.out.println("CtxDist: " + this.tricStructure.getContextsDistribution());
		System.out.println("Ctx Param1: " + this.tricStructure.getContextsParam1());
		System.out.println("Ctx Param2: " + this.tricStructure.getContextsParam2());
		System.out.println("Contiguity: " + this.tricStructure.getContiguity().toString());
		
		
		System.out.println("\n*** Overlapping Settings ***");
		System.out.println("Plaid Coherency: " + this.overlappingSettings.getPlaidCoherency().toString());
		System.out.println("% of overlapping trics: " + this.overlappingSettings.getPercOfOverlappingTrics());
		System.out.println("Max trics per overlapped area: " + this.overlappingSettings.getMaxTricsPerOverlappedArea());
		System.out.println("Max % of overlapping elements per tric: " + this.overlappingSettings.getMaxPercOfOverlappingElements());
		System.out.println("Max % of overlapping rows: " + this.overlappingSettings.getPercOfOverlappingRows());
		System.out.println("Max % of overlapping cols: " + this.overlappingSettings.getPercOfOverlappingColumns());
		System.out.println("Max % of overlapping ctxs: " + this.overlappingSettings.getPercOfOverlappingContexts());
		
		System.out.println("\n*** Patterns ***");
		for(int p = 0; p < this.tricPatterns.size(); p++) {
			System.out.println("Pattern " + p + ": (" + this.tricPatterns.get(p).getRowsPattern().toString() +
					", " + this.tricPatterns.get(p).getColumnsPattern().toString() +
					", " + this.tricPatterns.get(p).getContextsPattern().toString() + ")");
		}
		
		System.out.println("\n*** Missing/Noise/Error Settings ***");
		System.out.println("% of missings on background: " + this.qualitySettings.getPercMissingsOnBackground());
		System.out.println("Max % of missings on trics: " + this.qualitySettings.getPercMissingsOnTrics());
		System.out.println("% of noise on background: " + this.qualitySettings.getPercNoiseOnBackground());
		System.out.println("Max % of noise on trics: " + this.qualitySettings.getPercNoiseOnTrics());
		System.out.println("% of errors on background: " + this.qualitySettings.getPercErrorsOnBackground());
		System.out.println("Max % of errors on trics: " + this.qualitySettings.getPercErrorsOnTrics());
	}
}
