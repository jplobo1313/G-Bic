/**
 * GTricService Class
 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @version 1.0
 */
package com.gbic.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.math3.util.Pair;
import org.json.JSONObject;

import com.gbic.domain.dataset.Dataset;
import com.gbic.domain.dataset.HeterogeneousDataset;
import com.gbic.domain.dataset.NumericDataset;
import com.gbic.domain.dataset.SymbolicDataset;
import com.gbic.generator.BiclusterDatasetGenerator;
import com.gbic.generator.MixedDatasetGenerator;
import com.gbic.generator.NumericDatasetGenerator;
import com.gbic.generator.SymbolicDatasetGenerator;
import com.gbic.tests.OutputWriterThread;
import com.gbic.types.Background;
import com.gbic.types.BackgroundType;
import com.gbic.types.BiclusterType;
import com.gbic.types.Contiguity;
import com.gbic.types.Distribution;
import com.gbic.types.PatternType;
import com.gbic.types.PlaidCoherency;
import com.gbic.types.TimeProfile;
import com.gbic.utils.BiclusterPattern;
import com.gbic.utils.BiclusterStructure;
import com.gbic.utils.ComposedBiclusterPattern;
import com.gbic.utils.IOUtils;
import com.gbic.utils.OverlappingSettings;
import com.gbic.utils.QualitySettings;
import com.gbic.utils.RandomObject;
import com.gbic.utils.SingleBiclusterPattern;

public class GBicService extends Observable implements Observer {

	private String path = "data/";
	private String filename = "";
	private boolean singleFile;
	
	//Helper class to organize the tricluster's patterns
	public class BiclusterPatternWrapper{
		
		String biclusterType;
		String rowPattern;
		String columnPattern;
		String timeProfile;
		String imagePath;
		
		public BiclusterPatternWrapper(String biclusterType, String rowPattern, String columnPattern, String imagePath) {
			this.biclusterType = biclusterType;
			this.rowPattern = rowPattern;
			this.columnPattern = columnPattern;
			this.imagePath = imagePath;
		}
		
		public BiclusterPatternWrapper(String biclusterType, String rowPattern, String columnPattern, String timeProfile, String imagePath) {
			this.biclusterType = biclusterType;
			this.rowPattern = rowPattern;
			this.columnPattern = columnPattern;	
			this.timeProfile = timeProfile;
			this.imagePath = imagePath;
		}
		
		

		public String getRowPattern() {
			return rowPattern;
		}

		public String getColumnPattern() {
			return columnPattern;
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

		/**
		 * @return the biclusterType
		 */
		public String getBiclusterType() {
			return biclusterType;
		}

		/**
		 * @param biclusterType the biclusterType to set
		 */
		public void setBiclusterType(String biclusterType) {
			this.biclusterType = biclusterType;
		}

		public String toString() {
			return this.biclusterType + " -> " + rowPattern + "|" + columnPattern;
		}
		
	}
	
	private JSONObject biclustersJSON;
	private Dataset generatedDataset;
	
	private BiConsumer<Integer, Integer> progressUpdate;
	private Consumer<String> messageUpdate;
	private int currentProgress;
	
	private String state;
	//Paths to files with symbolic and numerics patterns
	private static final String SYMBOLIC_PATTERNS_PATH = "src/main/java/com/gbic/app/service/symbolicPatterns.csv";
	private static final String NUMERIC_PATTERNS_PATH = "src/main/java/com/gbic/app/service/numericPatterns.csv";
	private List<String> datasetTypes;
	private List<String> numericDatasetDataTypes;
	private List<String> datasetBackground;
	private List<String> distributions;
	private List<String> contiguity;
	private List<String> plaidCoherency;
	private List<BiclusterPatternWrapper> symbolicPatterns;
	private List<BiclusterPatternWrapper> numericPatterns;
	private List<BiclusterPatternWrapper> mixedPatterns;
	private List<String> symbolType;
	
	//Dataset Properties
	//Numeric or Sumbolic
	private String datasetType;
	private int numRows;
	private int numCols;
	private int numericCols;
	private int symbolicCols;
	
	//Numeric dataset
	private boolean realValued;
	private double minM;
	private double maxM;
	
	//symbolic dataset
	private boolean defaultSymbols;
	private int numberOfSymbols;
	private List<String> listOfSymbols;
	
	private Background singleBackground;
	private Background composedBackground;
	
	//BiclusterProperties
	private int numbics;
	private BiclusterStructure bicStructure;
	
	//Bicluster's Patters
	List<BiclusterPattern> bicPatterns;
	
	//Overlapping
	private OverlappingSettings overlappingSettings;
	
	//Extras
	private QualitySettings qualitySettings;

	/**
	 * Constructor
	 */
	public GBicService() {
		
		this.currentProgress = 0;
		
		this.datasetTypes = new ArrayList<>();
		fillDatasetTypes();
		
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
			    numericPatterns.add(new BiclusterPatternWrapper("Numeric", data[0], data[1], img));
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
			    symbolicPatterns.add(new BiclusterPatternWrapper("Symbolic", data[0], data[1], img));
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
	
	private void fillDatasetTypes() {
		datasetTypes.add("Symbolic");
		datasetTypes.add("Numeric");
		datasetTypes.add("Heterogeneous");
	}

	/**
	 * @return the list of available data types
	 */
	public List<String> getDataTypes(){
		return this.numericDatasetDataTypes;
	}
	
	/**
	 * @return the list of available data types
	 */
	public List<String> getDatasetTypes(){
		return this.datasetTypes;
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
	public List<BiclusterPatternWrapper> getSymbolicPatterns(){
		return this.symbolicPatterns;
	}
	
	/**
	 * Get tricluster's numeric patterns
	 * @return list with numeric patterns
	 */
	public List<BiclusterPatternWrapper> getNumericPatterns(){
		return this.numericPatterns;
	}
	
	public List<BiclusterPatternWrapper> getMixedPatterns(){
		return this.mixedPatterns;
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
	public void setDatasetProperties(int numRows, int numCols, boolean realValued, double minM, double maxM, String background,
			double backgroundParam1, double backgroundParam2, double[] backgroundParam3) {
		
		this.numRows = numRows;
		this.numCols = numCols;
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
		
		
		this.singleBackground = new Background(backgroundType);
		this.singleBackground.setParam1(backgroundParam1);
		this.singleBackground.setParam2(backgroundParam2);
		this.singleBackground.setParam3(backgroundParam3);
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
	public void setDatasetProperties(int numRows, int numCols, boolean defaultSymbols, int alphabetLength, String[] symbols, String background,
			double backgroundParam1, double backgroundParam2, double[] backgroundParam3) {
		
		this.numRows = numRows;
		this.numCols = numCols;
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
		
		
		this.singleBackground = new Background(backgroundType);
		this.singleBackground.setParam1(backgroundParam1);
		this.singleBackground.setParam2(backgroundParam2);
		this.singleBackground.setParam3(backgroundParam3);
	}
	
	public void setDatasetProperties(int numRows, int numCols, double colsRatio, boolean realValued, double minM, double maxM, 
			int alphabetLength, String[] symbols, 
			String singleBackground, double singleBackgroundParam1, double singleBackgroundParam2, double[] singleBackgroundParam3,
			String composedBackground, double composedBackgroundParam1, double composedBackgroundParam2, double[] composedBackgroundParam3) {
		
		this.numRows = numRows;
		this.numCols = numCols;
		this.numericCols = (int) Math.round(((double) this.numCols) * colsRatio);
		this.symbolicCols = this.numCols - this.numericCols;
		this.realValued = realValued;
		this.minM = minM;
		this.maxM = maxM;
		
		this.defaultSymbols = symbols == null;
		
		if(symbols == null)
			this.numberOfSymbols = alphabetLength;
		else
			this.listOfSymbols = Arrays.asList(symbols);
		
		BackgroundType singleBackgroundType = null;
		BackgroundType composedBackgroundType = null;
		
		switch(singleBackground) {
		case "Normal":
			singleBackgroundType = BackgroundType.NORMAL;
			break;
		case "Uniform":
			singleBackgroundType = BackgroundType.UNIFORM;
			break;
		case "Discrete":
			singleBackgroundType = BackgroundType.DISCRETE;
			break;
		default:
			singleBackgroundType = BackgroundType.MISSING;
			break;
		}
		
		switch(composedBackground) {
		case "Normal":
			composedBackgroundType = BackgroundType.NORMAL;
			break;
		case "Uniform":
			composedBackgroundType = BackgroundType.UNIFORM;
			break;
		case "Discrete":
			composedBackgroundType = BackgroundType.DISCRETE;
			break;
		default:
			composedBackgroundType = BackgroundType.MISSING;
			break;
		}
		
		
		this.singleBackground = new Background(singleBackgroundType);
		this.singleBackground.setParam1(singleBackgroundParam1);
		this.singleBackground.setParam2(singleBackgroundParam2);
		this.singleBackground.setParam3(singleBackgroundParam3);
		
		this.composedBackground = new Background(composedBackgroundType);
		this.composedBackground.setParam1(composedBackgroundParam1);
		this.composedBackground.setParam2(composedBackgroundParam2);
		this.composedBackground.setParam3(composedBackgroundParam3);
	}
	
	/**
	 * Set tricluster's structure properties
	 * @param numBics The number of biclusters to plant
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
	public void setBiclustersProperties(int numBics, String rowDist, double rowDistParam1, double rowDistParam2, String colDist, 
			double colDistParam1, double colDistParam2, String contiguity) {
		
		this.numbics = numBics;
		this.bicStructure = new BiclusterStructure();
		
		if(rowDist.equals("Normal"))
			this.bicStructure.setRowsDistribution(Distribution.NORMAL);
		if(rowDist.equals("Uniform"))
			this.bicStructure.setRowsDistribution(Distribution.UNIFORM);
		
		if(colDist.equals("Normal"))
			this.bicStructure.setColumnsDistribution(Distribution.NORMAL);
		if(colDist.equals("Uniform"))
			this.bicStructure.setColumnsDistribution(Distribution.UNIFORM);
		
		this.bicStructure.setRowsParam1(rowDistParam1);
		this.bicStructure.setRowsParam2(rowDistParam2);
		
		this.bicStructure.setColumnsParam1(colDistParam1);
		this.bicStructure.setColumnsParam2(colDistParam2);
		
		if(contiguity.equals("Columns"))
			this.bicStructure.setContiguity(Contiguity.COLUMNS);
		else
			this.bicStructure.setContiguity(Contiguity.NONE);
	}
	
	
	public void setBiclusterPatterns(List<BiclusterPatternWrapper> patterns, String datasetType) {
		
		Random r = RandomObject.getInstance();
		BiclusterType type = null;
		
		this.bicPatterns = new ArrayList<>();
		List<BiclusterPatternWrapper> numericPatterns = new ArrayList<>();
		List<BiclusterPatternWrapper> symbolicPatterns = new ArrayList<>();
		
		for(BiclusterPatternWrapper p : patterns) {
			if(p.getBiclusterType().equals("Numeric"))
				numericPatterns.add(p);
			else
				symbolicPatterns.add(p);
		}
		
		if(datasetType.equals("Heterogeneous")) {
			int numMixedBics = 0;
			int numNumericBics = 0;
			int numSymbolicBics = 0;
			
			int totalBics = this.numbics;
			
			if(!numericPatterns.isEmpty() && !symbolicPatterns.isEmpty()) {
				numMixedBics = r.nextInt(totalBics - 2) + 1;
				totalBics -= numMixedBics;
			}
			
			if(!numericPatterns.isEmpty() && symbolicPatterns.isEmpty())
				numNumericBics = totalBics;
			else if(numericPatterns.isEmpty() && !symbolicPatterns.isEmpty())
				numSymbolicBics = totalBics;
			else {
				numNumericBics = r.nextInt(totalBics - 1) + 1;
				totalBics -= numNumericBics;
				numSymbolicBics = totalBics;
			}
			
			
			//gerar padroes p/cada tipo
			for(int s = 0; s < numSymbolicBics; s++) {
				BiclusterPatternWrapper p = symbolicPatterns.get(r.nextInt(symbolicPatterns.size()));
				BiclusterPattern tp = new SingleBiclusterPattern(BiclusterType.SYMBOLIC, getPatternType(p.rowPattern), 
						getPatternType(p.columnPattern), getTimeProfile(p.timeProfile));
				this.bicPatterns.add(tp);
			}
			
			for(int n = 0; n < numNumericBics; n++) {
				BiclusterPatternWrapper p = numericPatterns.get(r.nextInt(numericPatterns.size()));
				BiclusterPattern tp = new SingleBiclusterPattern(BiclusterType.NUMERIC, getPatternType(p.rowPattern), 
						getPatternType(p.columnPattern), getTimeProfile(p.timeProfile));
				this.bicPatterns.add(tp);
			}
			
			for(int m = 0; m < numMixedBics; m++) {
				BiclusterPatternWrapper numericP = numericPatterns.get(r.nextInt(numericPatterns.size()));
				BiclusterPatternWrapper symbolicP = symbolicPatterns.get(r.nextInt(symbolicPatterns.size()));
				
				Pair<PatternType, PatternType> numericComponent = new Pair<>(getPatternType(numericP.rowPattern), 
						getPatternType(numericP.columnPattern));
				
				Pair<PatternType, PatternType> symbolicComponent = new Pair<>(getPatternType(symbolicP.rowPattern), 
						getPatternType(symbolicP.columnPattern));
				
				BiclusterPattern tp = new ComposedBiclusterPattern(BiclusterType.MIXED, numericComponent, getTimeProfile(numericP.timeProfile),
						symbolicComponent, getTimeProfile(symbolicP.timeProfile));
				
				this.bicPatterns.add(tp);
			}
		}
		else {
			
			if(datasetType.equals("Numeric"))
				type = BiclusterType.NUMERIC;
			else
				type = BiclusterType.SYMBOLIC;
			
			for(BiclusterPatternWrapper p : patterns) {
				BiclusterPattern tp = new SingleBiclusterPattern(type, getPatternType(p.rowPattern), getPatternType(p.columnPattern), null);
				if(((SingleBiclusterPattern) tp).getColumnsPattern().equals(PatternType.ORDER_PRESERVING))
					((SingleBiclusterPattern) tp).setTimeProfile(getTimeProfile(p.getTimeProfile()));
				System.out.println(getPatternType(p.rowPattern));
				this.bicPatterns.add(tp);
			}
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
		if(type != null) {
			if(type.contains("Random"))
				res = TimeProfile.RANDOM;
			else if(type.contains("Up-Regulated"))
				res = TimeProfile.MONONICALLY_INCREASING;
			else
				res = TimeProfile.MONONICALLY_DECREASING;
		}
		
		return res;
	}
	
	/**
	 * 
	 * @param plaidCoherency The plaid coherency
	 * @param percOverlappingBics The percentage of dataset's biclusters that can overlap
	 * @param maxOverlappingBics The maximum amount of bics that can overlap together
	 * @param percOverlappingElements The maximum percentage of elements tha can be share between 
	 * biclusters (relative to the smallest tric)
	 * @param percOverlappingRows The maximum percentage of overlapping in the row dimension
	 * @param percOverlappingColumns The maximum percentage of overlapping in the column dimension
	 * @param percOverlappingContexts The maximum percentage of overlapping in the context dimension
	 */
	public void setOverlappingSettings(String plaidCoherency, double percOverlappingBics, int maxOverlappingBics, double percOverlappingElements,
			double percOverlappingRows, double percOverlappingColumns) {
		
		this.overlappingSettings = new OverlappingSettings();
		
		if(plaidCoherency.equals("Additive"))
			this.overlappingSettings.setPlaidCoherency(PlaidCoherency.ADDITIVE);
		else if (plaidCoherency.equals("Multiplicative"))
			this.overlappingSettings.setPlaidCoherency(PlaidCoherency.MULTIPLICATIVE);
		else if (plaidCoherency.equals("Interpoled"))
			this.overlappingSettings.setPlaidCoherency(PlaidCoherency.INTERPOLED);
		else if (plaidCoherency.equals("None"))
			this.overlappingSettings.setPlaidCoherency(PlaidCoherency.NONE);
		else
			this.overlappingSettings.setPlaidCoherency(PlaidCoherency.NO_OVERLAPPING);
		
		
		this.overlappingSettings.setPercOfOverlappingBics(percOverlappingBics);
		this.overlappingSettings.setMaxBicsPerOverlappedArea(maxOverlappingBics);
		this.overlappingSettings.setMaxPercOfOverlappingElements(percOverlappingElements);
		this.overlappingSettings.setPercOfOverlappingRows(percOverlappingRows);
		this.overlappingSettings.setPercOfOverlappingColumns(percOverlappingColumns);
	}
	
	/**
	 * Set the Quality properties 
	 * @param percMissingsOnBackground The percentage of missings on dataset's background
	 * @param percMissingsOnBics The maximum percentage of missings on planted biclusters
	 * @param percNoiseOnBackground  The percentage of noise on dataset's background
	 * @param percNoiseOnBics The maximum percentage of noise on planted biclusters
	 * @param noiseDeviation The noise deviation value
	 * @param percErrorsOnBackground The percentage of errors on dataset's background
	 * @param percErrorsOnBics The maximum percentage of errors on planted biclusters
	 */
	public void setQualitySettings(double percMissingsOnBackground, double percMissingsOnBics, double percNoiseOnBackground, double percNoiseOnBics,
			double noiseDeviation, double percErrorsOnBackground, double percErrorsOnBics) {
		
		this.qualitySettings = new QualitySettings();
		
		this.qualitySettings.setPercMissingsOnBackground(percMissingsOnBackground);
		this.qualitySettings.setPercMissingsOnBics(percMissingsOnBics);
		this.qualitySettings.setPercNoiseOnBackground(percNoiseOnBackground);
		this.qualitySettings.setPercNoiseOnBics(percNoiseOnBics);
		this.qualitySettings.setNoiseDeviation(noiseDeviation); 
		this.qualitySettings.setPercErrorsOnBackground(percErrorsOnBackground);
		this.qualitySettings.setPercErrorsOnBics(percErrorsOnBics);
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
			tricDataFileName = "dataset" + "_" + numRows + "x" + numCols;
			datasetFileName = "dataset" + "_" + numRows + "x" + numCols;
		}
		else {
			tricDataFileName = this.filename + "_bics";
			datasetFileName = this.filename + "_data";
		}
		
		startTimeGen = System.currentTimeMillis();
		generator = new NumericDatasetGenerator(realValued, numRows, numCols, numbics, singleBackground, minM, maxM);
		stopTimeGen = System.currentTimeMillis();
		
		generator.addObserver(this);
		
		System.out.println("(TricDatasetGenerator) Execution Time: " + ((double)(stopTimeGen - startTimeGen)) / 1000);
		
		updateProgressStatusAndMessage(20, "Generating Biclusters...");
		
		startTimeBics = System.currentTimeMillis();
		NumericDataset generatedDataset = (NumericDataset) generator.generate(bicPatterns, bicStructure, overlappingSettings);
		stopTimeBics = System.currentTimeMillis();

		System.out.println("(GeneratePlaidRealBics) Execution Time: " + ((double) (stopTimeBics - startTimeBics)) / 1000);
		
		updateProgressStatusAndMessage(80, "Generating Missings...");
		System.out.println("Generating Missings...");
		generatedDataset.plantMissingElements(this.qualitySettings.getPercMissingsOnBackground(), this.qualitySettings.getPercMissingsOnBics());
		
		updateProgressStatusAndMessage(85, "Generating Noise...");
		System.out.println("Generating Noise...");
		generatedDataset.plantNoisyElements(this.qualitySettings.getPercNoiseOnBackground(), this.qualitySettings.getPercNoiseOnBics(), this.qualitySettings.getNoiseDeviation());
		
		updateProgressStatusAndMessage(90, "Generating Errors...");
		System.out.println("Generating Errors...");
		generatedDataset.plantErrors(this.qualitySettings.getPercErrorsOnBackground(), this.qualitySettings.getPercErrorsOnBics(), this.qualitySettings.getNoiseDeviation());
		
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
		
		saveNumericResult(generatedDataset, tricDataFileName, datasetFileName);
		
		//updateProgressStatusAndMessage(100, "Completed!");
	}
	
	public void generateHeterogeneousDataset() throws Exception {
		
		long startTimeGen;
		long stopTimeGen;
		long startTimeBics;
		long stopTimeBics;
		MixedDatasetGenerator generator;
		
		printDatasetSettings();
		
		this.progressUpdate.accept(5, 100);
		this.messageUpdate.accept("Generating Background...");
		
		String tricDataFileName;
		String datasetFileName;
		
		if(this.filename.isEmpty()) {	
			tricDataFileName = "dataset" + "_" + numRows + "x" + numCols;
			datasetFileName = "dataset" + "_" + numRows + "x" + numCols;
		}
		else {
			tricDataFileName = this.filename + "_bics";
			datasetFileName = this.filename + "_data";
		}
		
		startTimeGen = System.currentTimeMillis();
		
		String[] symbols = null;
		
		if(!(this.listOfSymbols == null))
			symbols = this.listOfSymbols.toArray(new String[0]);
		
		generator = new MixedDatasetGenerator(realValued, numRows, numericCols, symbolicCols, numbics, this.composedBackground,
				this.singleBackground, this.minM, this.maxM, symbols, this.numberOfSymbols);
		stopTimeGen = System.currentTimeMillis();
		
		generator.addObserver(this);
		
		System.out.println("(TricDatasetGenerator) Execution Time: " + ((double)(stopTimeGen - startTimeGen)) / 1000);
		
		updateProgressStatusAndMessage(20, "Generating Biclusters...");
		
		startTimeBics = System.currentTimeMillis();
		HeterogeneousDataset generatedDataset = (HeterogeneousDataset) generator.generate(bicPatterns, bicStructure, overlappingSettings);
		stopTimeBics = System.currentTimeMillis();

		System.out.println("(GeneratePlaidRealBics) Execution Time: " + ((double) (stopTimeBics - startTimeBics)) / 1000);
		
		updateProgressStatusAndMessage(80, "Generating Missings...");
		System.out.println("Generating Missings...");
		generatedDataset.plantMissingElements(this.qualitySettings.getPercMissingsOnBackground(), this.qualitySettings.getPercMissingsOnBics());
		
		updateProgressStatusAndMessage(85, "Generating Noise...");
		System.out.println("Generating Noise...");
		generatedDataset.plantNoisyElements(this.qualitySettings.getPercNoiseOnBackground(), this.qualitySettings.getPercNoiseOnBics(), this.qualitySettings.getNoiseDeviation());
		
		updateProgressStatusAndMessage(90, "Generating Errors...");
		System.out.println("Generating Errors...");
		generatedDataset.plantErrors(this.qualitySettings.getPercErrorsOnBackground(), this.qualitySettings.getPercErrorsOnBics(), this.qualitySettings.getNoiseDeviation());
		
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
		
		saveHeterogeneousResult(generatedDataset, tricDataFileName, datasetFileName);
		
		//updateProgressStatusAndMessage(100, "Completed!");
	}
	
	/**
	 * Get the JSON with the biclusters info
	 * @return the JSON Object
	 */
	public JSONObject getBiclustersJSON() {
		return this.biclustersJSON;
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
		
		BiclusterDatasetGenerator generator = null;
		
		this.progressUpdate.accept(5, 100);
		this.messageUpdate.accept("Generating Background...");
		
		startTimeGen = System.currentTimeMillis();

		if(!this.defaultSymbols) {
			String[] symbols = this.listOfSymbols.toArray(new String[0]);
			generator = new SymbolicDatasetGenerator(numRows,numCols, numbics, singleBackground, symbols,
					false);
		}
		else
			generator = new SymbolicDatasetGenerator(numRows,numCols, numbics, singleBackground, numberOfSymbols, false);
		stopTimeGen = System.currentTimeMillis();

		generator.addObserver(this);
		
		System.out.println("(BicMatrixGenerator) Execution Time: " + ((double)(stopTimeGen - startTimeGen))/1000 + " secs");

		updateProgressStatusAndMessage(20, "Generating Biclusters...");
		
		startTimeBics = System.currentTimeMillis();
		SymbolicDataset generatedDataset = (SymbolicDataset) generator.generate(this.bicPatterns, bicStructure, this.overlappingSettings);
		stopTimeBics = System.currentTimeMillis();
		
		updateProgressStatusAndMessage(80, "Generating Missings...");
		System.out.println("Generating Missings...");
		generatedDataset.plantMissingElements(this.qualitySettings.getPercMissingsOnBackground(), this.qualitySettings.getPercMissingsOnBics());
		
		updateProgressStatusAndMessage(85, "Generating Noise...");
		System.out.println("Generating Noise...");
		generatedDataset.plantNoisyElements(this.qualitySettings.getPercNoiseOnBackground(), this.qualitySettings.getPercNoiseOnBics(), 
				(int)this.qualitySettings.getNoiseDeviation());
		
		updateProgressStatusAndMessage(90, "Generating Errors...");
		System.out.println("Generating Errors...");
		generatedDataset.plantErrors(this.qualitySettings.getPercErrorsOnBackground(), this.qualitySettings.getPercErrorsOnBics(), 
				(int)this.qualitySettings.getNoiseDeviation());
		
		String tricDataFileName;
		String datasetFileName;
		
		updateProgressStatusAndMessage(95, "Writing output...");
		
		if(filename.isEmpty()) {
			tricDataFileName = "dataset" + "_" + numRows + "x" + numCols;
			datasetFileName = "dataset" + "_" + numRows + "x" + numCols;
		}
		else {
			tricDataFileName = filename + "_bics";
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
		
		saveSymbolicResult(generatedDataset, tricDataFileName, datasetFileName);
		
		//updateProgressStatusAndMessage(100, "Completed!");
	}
	
	/**
	 * Save the generated datset's output
	 * @param generatedDataset The dataset
	 * @param tricDataFileName The name of the tricluster's files
	 * @param datasetFileName The name of the dataset file
	 * @throws Exception
	 */
	public void saveNumericResult(NumericDataset<? extends Number> generatedDataset, String tricDataFileName, String datasetFileName) throws Exception {

		System.out.println("numWritting output...");
		
		IOUtils.writeFile(path, tricDataFileName + ".txt",generatedDataset.getBicsInfo(), false);
		System.out.println("Biclusters txt file written!");
		
		this.biclustersJSON = generatedDataset.getBicsInfoJSON(generatedDataset, false);
		IOUtils.writeFile(path, tricDataFileName + ".json", this.biclustersJSON.toString(), false);
		System.out.println("Biclusters JSON file written!");
		
		
		this.biclustersJSON = this.biclustersJSON.getJSONObject("biclusters");
		
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
	public void saveSymbolicResult(SymbolicDataset generatedDataset, String tricDataFileName, String datasetFileName) throws Exception {

		System.out.println("symbWritting output...");
		
		IOUtils.writeFile(path, tricDataFileName + ".txt",generatedDataset.getBicsInfo(), false);
		System.out.println("Biclusters txt file written!");
		
		this.biclustersJSON = generatedDataset.getBicsInfoJSON(generatedDataset, false);
		
		IOUtils.writeFile(path, tricDataFileName + ".json", this.biclustersJSON.toString(), false);
		System.out.println("Biclusters JSON file written");
		
		this.biclustersJSON = this.biclustersJSON.getJSONObject("biclusters");

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

	/**
	 * Save the generated datset's output
	 * @param generatedDataset The dataset
	 * @param tricDataFileName The name of the tricluster's files
	 * @param datasetFileName The name of the dataset file
	 * @throws Exception
	 */
	public void saveHeterogeneousResult(HeterogeneousDataset generatedDataset, String tricDataFileName, String datasetFileName) throws Exception {

		System.out.println("Writting output...");
		
		IOUtils.writeFile(path, tricDataFileName + ".txt",generatedDataset.getBicsInfo(), false);
		System.out.println("Biclusters txt file written!");
		
		this.biclustersJSON = generatedDataset.getBicsInfoJSON(generatedDataset);
		IOUtils.writeFile(path, tricDataFileName + ".json", this.biclustersJSON.toString(), false);
		System.out.println("Biclusters JSON file written!");
		
		for(String k : this.biclustersJSON.getJSONObject("NumericBiclusters").keySet()) {
			this.biclustersJSON.put(k, this.biclustersJSON.getJSONObject("NumericBiclusters").get(k));
		}
		
		for(String k : this.biclustersJSON.getJSONObject("SymbolicBiclusters").keySet()) {
			this.biclustersJSON.put(k, this.biclustersJSON.getJSONObject("SymbolicBiclusters").get(k));
		}
		
		for(String k : this.biclustersJSON.getJSONObject("MixedBiclusters").keySet()) {
			this.biclustersJSON.put(k, this.biclustersJSON.getJSONObject("MixedBiclusters").get(k));
		}
		
		//this.biclustersJSON = this.biclustersJSON.getJSONObject("biclusters");
		
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
		
		double progress = (60 / ((double) this.numbics));
		updateProgressStatusAndMessage(this.currentProgress + (int)progress, "Generating Biclusters (" + currentTric + "/" + this.numbics + ")...");
	}
	
	private void updateProgressStatusAndMessage(int prog, String msg) {
		
		this.currentProgress = prog;
		this.progressUpdate.accept(prog, 100);
		this.messageUpdate.accept(msg);
	}
	
	private void printDatasetSettings() {
		
		System.out.println("*** Dataset Properties ***");
		System.out.println("Dataset type: " + this.datasetType);
		System.out.println("NumRows: " + this.numRows);
		System.out.println("NumCols: " + this.numCols);
		
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
		
		System.out.println("Background: " + this.singleBackground.getType().toString());
		if(this.singleBackground.getType().equals(BackgroundType.NORMAL)){
			System.out.println("Background Mean: " + singleBackground.getParam1());
			System.out.println("Background Std: " + singleBackground.getParam2());
		}
		else if(this.singleBackground.getType().equals(BackgroundType.DISCRETE))
			System.out.println("Probabilities: " + Arrays.toString(this.singleBackground.getParam3()));
		
		System.out.println("\n*** Biclusters Properties ***");
		System.out.println("NumBics: " + this.numbics);
		System.out.println("RowDist: " + this.bicStructure.getRowsDistribution());
		System.out.println("Row Param1: " + this.bicStructure.getRowsParam1());
		System.out.println("Row Param2: " + this.bicStructure.getRowsParam2());
		System.out.println("ColDist: " + this.bicStructure.getColumnsDistribution());
		System.out.println("Col Param1: " + this.bicStructure.getColumnsParam1());
		System.out.println("Col Param2: " + this.bicStructure.getColumnsParam2());
		System.out.println("Contiguity: " + this.bicStructure.getContiguity().toString());
		
		
		System.out.println("\n*** Overlapping Settings ***");
		System.out.println("Plaid Coherency: " + this.overlappingSettings.getPlaidCoherency().toString());
		System.out.println("% of overlapping bics: " + this.overlappingSettings.getPercOfOverlappingBics());
		System.out.println("Max bics per overlapped area: " + this.overlappingSettings.getMaxBicsPerOverlappedArea());
		System.out.println("Max % of overlapping elements per tric: " + this.overlappingSettings.getMaxPercOfOverlappingElements());
		System.out.println("Max % of overlapping rows: " + this.overlappingSettings.getPercOfOverlappingRows());
		System.out.println("Max % of overlapping cols: " + this.overlappingSettings.getPercOfOverlappingColumns());
		
		System.out.println("\n*** Patterns ***");
		//TODO
		
		for(int p = 0; p < this.bicPatterns.size(); p++) {
			System.out.println(this.bicPatterns.get(p).toString());
		}
		
		
		System.out.println("\n*** Missing/Noise/Error Settings ***");
		System.out.println("% of missings on background: " + this.qualitySettings.getPercMissingsOnBackground());
		System.out.println("Max % of missings on bics: " + this.qualitySettings.getPercMissingsOnBics());
		System.out.println("% of noise on background: " + this.qualitySettings.getPercNoiseOnBackground());
		System.out.println("Max % of noise on bics: " + this.qualitySettings.getPercNoiseOnBics());
		System.out.println("% of errors on background: " + this.qualitySettings.getPercErrorsOnBackground());
		System.out.println("Max % of errors on bics: " + this.qualitySettings.getPercErrorsOnBics());
	}

	public void initializeRandom(int seed) {
		RandomObject.initialization(seed);
	}
}
