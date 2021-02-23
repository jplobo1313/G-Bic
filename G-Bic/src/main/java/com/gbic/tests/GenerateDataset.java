package com.gbic.tests;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import com.gbic.domain.dataset.HeterogeneousDataset;
import com.gbic.domain.dataset.NumericDataset;
import com.gbic.domain.dataset.SymbolicDataset;
import com.gbic.generator.NumericDatasetGenerator;
import com.gbic.generator.SymbolicDatasetGenerator;
import com.gbic.generator.BiclusterDatasetGenerator;
import com.gbic.generator.MixedDatasetGenerator;
import com.gbic.service.GBicService;
import com.gbic.types.Background;
import com.gbic.types.BackgroundType;
import com.gbic.types.BiclusterType;
import com.gbic.types.Contiguity;
import com.gbic.types.Distribution;
import com.gbic.types.PatternType;
import com.gbic.types.PlaidCoherency;
import com.gbic.types.TimeProfile;
import com.gbic.utils.IOUtils;
import com.gbic.utils.InputValidation;
import com.gbic.utils.OverlappingSettings;
import com.gbic.utils.SingleBiclusterPattern;
import com.gbic.utils.BiclusterPattern;
import com.gbic.utils.BiclusterPatternWrapper;
import com.gbic.utils.BiclusterStructure;
import com.gbic.utils.ComposedBiclusterPattern;
public class GenerateDataset{

	public static String path;
	public static String outputFolder = "TriGenData";
	public static int bics;
	public static double percMissings;
	
	public static void main (String[] args) throws Exception {
		
		
		for(int i = 0; i < 1; i++) {
			System.out.println("Run " + i);
			generateHeterogeneous();
		}
	}

	//*** USA O REAL ***
	public static void generateSymbolic() throws Exception {

		long startTimeGen;
		long stopTimeGen;
		long startTimeBics;
		long stopTimeBics;
		long startWriting;
		long stopWriting;

		//** 1 - Define dataset properties **//
		//num de linhas do dataset
		int numRows = 100;
		//num de colunas do dataset
		int numCols = 100;
		//num de bics a plantar
		int numBics = 10;

		//tamanho do alfabeto ou simbolos do alfabeto (escolher um)
		int alphabetL = 10;
		//String[] alphabet = {"1","2","3","4","5"};

		//simetrias nos valores do dataset
		boolean symmetries = false;

		Background background = null;
		BiclusterDatasetGenerator generator = null;

		/* Background Normal(2.5, 1)
    	background = new Background(BackgroundType.NORMAL, 2.5, 1);
		 */
		 //Background Uniform
        background = new Background(BackgroundType.UNIFORM);
		 
		/* Background Missing
        background = new Background(BackgroundType.MISSING);
		 */
		// Background Weighted probabilities
		//double[] probs = {0.05, 0.1, 0.3, 0.35, 0.2};
		//background = new Background(BackgroundType.DISCRETE, probs);
		// **************** //
		
		InputValidation.validateDatasetSettings(numRows, numCols, numBics, alphabetL);
		
		startTimeGen = System.currentTimeMillis();

		generator = new SymbolicDatasetGenerator(numRows,numCols, numBics, background, alphabetL, symmetries);
		stopTimeGen = System.currentTimeMillis();

		System.out.println("(BicMatrixGenerator) Execution Time: " + ((double)(stopTimeGen - startTimeGen))/1000 + " secs");

		//** 2 - Set bicluster's patterns **//
		List<BiclusterPattern> patterns = new ArrayList<>();
		patterns.add(new SingleBiclusterPattern(BiclusterType.SYMBOLIC, PatternType.CONSTANT, PatternType.CONSTANT, null));
		patterns.add(new SingleBiclusterPattern(BiclusterType.SYMBOLIC, PatternType.CONSTANT, PatternType.NONE, null));
		patterns.add(new SingleBiclusterPattern(BiclusterType.SYMBOLIC, PatternType.NONE, PatternType.CONSTANT, null));
		//patterns.add(new BiclusterPattern(PatternType.CONSTANT, PatternType.CONSTANT, PatternType.NONE));
		//patterns.add(new BiclusterPattern(PatternType.NONE, PatternType.CONSTANT, PatternType.CONSTANT));
		//patterns.add(new BiclusterPattern(PatternType.CONSTANT, PatternType.NONE, PatternType.CONSTANT));
		//patterns.add(new BiclusterPattern(PatternType.CONSTANT, PatternType.NONE, PatternType.NONE));
		//patterns.add(new BiclusterPattern(PatternType.NONE, PatternType.CONSTANT, PatternType.NONE));
		//patterns.add(new BiclusterPattern(PatternType.NONE, PatternType.NONE, PatternType.CONSTANT));
		// *************** //
		
		InputValidation.validatePatterns(patterns);
		
		//** 3 - Define bicluster's structure **//
		//Object that encapsulates the configurations of the bicluster's structure
		BiclusterStructure bicStructure = new BiclusterStructure();
		
		//Distribution used to calculate the number of rows/cols/ctxs for a bic (NORMAL or UNIFORM)
		//Dist args: if dist=UNIFORM, then param1 and param2 represents the min and max, respectively
		//			 if dist=NORMAL, then param1 and param2 represents the mean and stdDev, respectively
		bicStructure.setRowsSettings(Distribution.UNIFORM, 4, 4);
		bicStructure.setColumnsSettings(Distribution.UNIFORM, 4, 4);
		
		//Contiguity can occour on COLUMNS or CONTEXTS. To avoid contiguity use NONE
		bicStructure.setContiguity(Contiguity.NONE);
		// ************* /
		
		//** 4- Define overlapping settings ** //
		//Object to encapsulate overlapping parameters
		OverlappingSettings overlapping = new OverlappingSettings();
		
		//Plaid Coherency (ADDITIVE, MULTIPLICATIVE, INTERPOLED, NONE or NO_OVERLAPPING)
		overlapping.setPlaidCoherency(PlaidCoherency.ADDITIVE);
		
		//Percentage of overlapping bics defines how many bics are allowed to overlap:
		//if 0.5 only half of the dataset biclusters will overlap
		overlapping.setPercOfOverlappingBics(0.5);
		
		//Maximum number of biclusters that can overlap together. if equal to 3, there will be, at max, 3 biclusters
		//that intersect each other(T1 ^ T2 ^ T3)
		overlapping.setMaxBicsPerOverlappedArea(2);
		
		//Maximum percentage of elements shared by overlapped biclusters. If 0.5, T1 ^ T2 will have, at max, 50% of the elements
		//of the smallest bic
		overlapping.setMaxPercOfOverlappingElements(0.5);
		
		//Percentage of allowed amount of overlaping across biclusters rows, columns and contexts. if rows=0.5, then 
		//T2 will intersect, at max, with half(50%) the rows of T1.
		//if you dont want any resbiction on the number of rows/cols/ctxs that can overlapp, use 1.0
		overlapping.setPercOfOverlappingRows(1);
		overlapping.setPercOfOverlappingColumns(1);
		// ************* //
		
		startTimeBics = System.currentTimeMillis();
		SymbolicDataset generatedDataset = (SymbolicDataset) generator.generate(patterns, bicStructure, overlapping);
		stopTimeBics = System.currentTimeMillis();

		//Percentage of missing values on the background, that is, values that do not belong to planted bics (Range = [0,1])
		double missingPercOnBackground = 0.0;
		//Maximum percentage of missing values on each bicluster. Range [0,1]. 
		//Ex: 0.1 significa que cada bic tem no maximo 10% de missings. Pode ter menos
		double missingPercOnPlantedBics = 0.0;

		//Same as above but for noise
		double noisePercOnBackground = 0.0;
		double noisePercOnPlantedBics = 0.0;
		//Level of symbol deviation, that is, the maximum difference between the current symbol on the matrix and the one that
		//will replaced it to be considered noise.
		//Ex: Let Alphabet = [1,2,3,4,5] and CurrentSymbol = 3, if the noiseDeviation is '1', then CurrentSymbol will be, randomly,
		//replaced by either '2' or '4'. If noiseDeviation is '2', CurrentSymbol can be replaced by either '1','2','4' or '5'.
		int noiseDeviation = 1;

		//Same as above but for errors
		//Similar as noise, a new value is considered an error if the difference between it and the current value in the matrix is
		//greater than noiseDeviation.
		//Ex: Alphabet = [1,2,3,4,5], If currentValue = 2, and errorDeviation = 2, to turn currentValue an error, it's value must be
		//replaced by '5', that is the only possible value that respects abs(currentValue - newValue) > noiseDeviation
		double errorPercOnBackground = 0.0;
		double errorPercOnPlantedBics = 0.0;
		
		generatedDataset.plantMissingElements(missingPercOnBackground, missingPercOnPlantedBics);
		//generatedDataset.plantNoisyElements(noisePercOnBackground, noisePercOnPlantedBics, noiseDeviation);
		//generatedDataset.plantErrors(errorPercOnBackground, errorPercOnPlantedBics, noiseDeviation);
		
		System.out.println("(GeneratePlaidSymbolicBics) Execution Time: " + ((double)(stopTimeBics - startTimeBics))/1000 + " secs");

		String bicDataFileName;
		String datasetFileName;
		
		
		bicDataFileName = "bic_test" + "_" + numRows + "x" + numCols;
		datasetFileName = "data_test" + "_" + numRows + "x" + numCols;
		

		GBicService serv = new GBicService();
		serv.setPath("/Users/atticus/git/G-Bic/G-Bic/temp/");
		serv.setSingleFileOutput(true);
		serv.saveResult(generatedDataset, bicDataFileName, datasetFileName);

		//Tests.testMaxBicsOnOverlappedArea(generatedDataset, overlapping, numBics);
		//Tests.testPercOfOverlappingBics(generatedDataset, overlapping, numBics);
		//Tests.testContiguity(generatedDataset.getPlantedBics(), bicStructure.getContiguity());
		//Tests.testMissingNoiseError(generatedDataset);
		
		//generateHeatMap(bicDataFileName, datasetFileName);
	}

	
	public static NumericDataset generateReal() throws Exception {

		long startTimeGen;
		long stopTimeGen;
		long startTimeBics;
		long stopTimeBics;

		//num de linhas do dataset
		int numRows = 100;
		//num de colunas do dataset
		int numCols = 100;
		//num de bics a plantar
		int numBics = 6;

		bics = numBics;
		
		//TODO: limites dos valores do dataset (usar em caso de dataset real)
		double min = 0;
		double max = 100;

		//use real valued or integer alphabet
		boolean realValued = true;

		Background background = null;
		BiclusterDatasetGenerator generator = null;

		/* Background Normal(2.5, 1)
    	*/
    	background = new Background(BackgroundType.UNIFORM);
		 

		/* Background Uniform
        background = new Background(BackgroundType.UNIFORM);
        generator = new BicMatrixGenerator(numRows,numCols,numBics, background, alphabetL, symmetries);
		 */

		/* Background Missing
        background = new Background(BackgroundType.MISSING);
		generator = new BicMatrixGenerator(numRows,numCols,numBics, background, alphabetL, symmetries);
		 */

		/*
		double[] probs = {0.05, 0.1, 0.3, 0.35, 0.2};
		background = new Background(BackgroundType.DISCRETE, probs);
		*/
		//background = new Background(BackgroundType.MISSING);
		
		//background = new Background(BackgroundType.UNIFORM);

		startTimeGen = System.currentTimeMillis();
		generator = new NumericDatasetGenerator(realValued, numRows, numCols, numBics, background, min, max);
		stopTimeGen = System.currentTimeMillis();
		
		System.out.println("(BackgroundGenerator) Execution Time: " + ((double)(stopTimeGen - startTimeGen)) / 1000);

		//Padrao
		List<BiclusterPattern> patterns = new ArrayList<>();
		//patterns.add(new BiclusterPattern(PatternType.ORDER_PRESERVING, PatternType.NONE, PatternType.NONE));
		//patterns.add(new BiclusterPattern(PatternType.NONE, PatternType.ORDER_PRESERVING, PatternType.NONE));
		//patterns.add(new BiclusterPattern(PatternType.NONE, PatternType.ORDER_PRESERVING, TimeProfile.RANDOM));
		//patterns.add(new BiclusterPattern(PatternType.NONE, PatternType.ORDER_PRESERVING, TimeProfile.MONONICALLY_DECREASING));
		//patterns.add(new BiclusterPattern(PatternType.NONE, PatternType.ORDER_PRESERVING, TimeProfile.MONONICALLY_INCREASING));
		
		//patterns.add(new BiclusterPattern(PatternType.CONSTANT, PatternType.NONE, PatternType.NONE));
		//patterns.add(new BiclusterPattern(PatternType.NONE, PatternType.CONSTANT, PatternType.NONE));
		//patterns.add(new BiclusterPattern(PatternType.NONE, PatternType.NONE, PatternType.CONSTANT));
		//patterns.add(new BiclusterPattern(PatternType.CONSTANT, PatternType.CONSTANT, PatternType.CONSTANT));
		//patterns.add(new BiclusterPattern(PatternType.CONSTANT, PatternType.NONE, PatternType.CONSTANT));
		//patterns.add(new BiclusterPattern(PatternType.CONSTANT, PatternType.CONSTANT, PatternType.NONE));
		//patterns.add(new BiclusterPattern(PatternType.NONE, PatternType.CONSTANT, PatternType.CONSTANT));
		
		patterns.add(new SingleBiclusterPattern(BiclusterType.NUMERIC, PatternType.MULTIPLICATIVE, PatternType.MULTIPLICATIVE, null));
		patterns.add(new SingleBiclusterPattern(BiclusterType.NUMERIC, PatternType.CONSTANT, PatternType.MULTIPLICATIVE, null));
		patterns.add(new SingleBiclusterPattern(BiclusterType.NUMERIC, PatternType.MULTIPLICATIVE, PatternType.CONSTANT, null));
		//patterns.add(new BiclusterPattern(PatternType.ADDITIVE, PatternType.ADDITIVE, PatternType.ADDITIVE));
		//patterns.add(new BiclusterPattern(PatternType.CONSTANT, PatternType.ADDITIVE, PatternType.ADDITIVE));
		//patterns.add(new BiclusterPattern(PatternType.ADDITIVE, PatternType.CONSTANT, PatternType.ADDITIVE));
		//patterns.add(new BiclusterPattern(PatternType.ADDITIVE, PatternType.ADDITIVE, PatternType.CONSTANT));
		
		//patterns.add(new BiclusterPattern(PatternType.MULTIPLICATIVE, PatternType.CONSTANT, PatternType.CONSTANT));
		//patterns.add(new BiclusterPattern(PatternType.CONSTANT, PatternType.MULTIPLICATIVE, PatternType.CONSTANT));
		//patterns.add(new BiclusterPattern(PatternType.CONSTANT, PatternType.CONSTANT, PatternType.MULTIPLICATIVE));
		//patterns.add(new BiclusterPattern(PatternType.MULTIPLICATIVE, PatternType.MULTIPLICATIVE, PatternType.MULTIPLICATIVE));
		//patterns.add(new BiclusterPattern(PatternType.CONSTANT, PatternType.MULTIPLICATIVE, PatternType.MULTIPLICATIVE));
		//patterns.add(new BiclusterPattern(PatternType.MULTIPLICATIVE, PatternType.CONSTANT, PatternType.MULTIPLICATIVE));
		//patterns.add(new BiclusterPattern(PatternType.MULTIPLICATIVE, PatternType.MULTIPLICATIVE, PatternType.CONSTANT));
		
		

		//** 3 - Define bicluster's structure **//
		//Object that encapsulates the configurations of the bicluster's structure
		BiclusterStructure bicStructure = new BiclusterStructure();
		
		//Distribution used to calculate the number of rows/cols/ctxs for a bic (NORMAL or UNIFORM)
		//Dist args: if dist=UNIFORM, then param1 and param2 represents the min and max, respectively
		//			 if dist=NORMAL, then param1 and param2 represents the mean and stdDev, respectively
		bicStructure.setRowsSettings(Distribution.UNIFORM, 3, 3);
		bicStructure.setColumnsSettings(Distribution.UNIFORM, 3, 3);
		
		//Contiguity can occour on COLUMNS or CONTEXTS. To avoid contiguity use NONE
		bicStructure.setContiguity(Contiguity.NONE);
		// ************* /

		//** Define overlapping settings ** //
		
		//Object to encapsulate overlapping parameters
		OverlappingSettings overlapping = new OverlappingSettings();
		
		//Plaid Coherency (ADDITIVE, MULTIPLICATIVE, INTERPOLED, NONE or NO_OVERLAPPING
		overlapping.setPlaidCoherency(PlaidCoherency.NO_OVERLAPPING);
		
		//Percentage of overlapping bics defines how many bics are allowed to overlap:
		//if 0.5 only half of the dataset biclusters will overlap
		overlapping.setPercOfOverlappingBics(0.8);
		
		//Maximum number of biclusters that can overlap together. if equal to 3, there will be, at max, 3 biclusters
		//that intersect each other(T1 ^ T2 ^ T3)
		overlapping.setMaxBicsPerOverlappedArea(8);
		
		//Maximum percentage of elements shared by overlapped biclusters. If 0.5, T1 ^ T2 will have, at max, 50% of the elements
		//of the smallest bic
		overlapping.setMaxPercOfOverlappingElements(0.6);
		
		//Percentage of allowed amount of overlaping across biclusters rows, columns and contexts. if rows=0.5, then 
		//T2 will intersect, at max, with half(50%) the rows of T1.
		//if you dont want any resbiction on the number of rows/cols/ctxs that can overlapp, use 1.0
		overlapping.setPercOfOverlappingRows(1.0);
		overlapping.setPercOfOverlappingColumns(1.0);	
		//** end of overlapping settings ** //
		
		startTimeBics = System.currentTimeMillis();
		NumericDataset generatedDataset = (NumericDataset) generator.generate(patterns, bicStructure, overlapping);
		stopTimeBics = System.currentTimeMillis();
		
		System.out.println("(GenerateBics) Execution Time: " + ((double) (stopTimeBics - startTimeBics)) / 1000);
		//System.out.println("Number of planted bics = " + generatedDataset.getBiclusters().size());
		
		/*
		for(int id = 0; id < numBics; id++) {
			NumericBicluster<?> t = generatedDataset.getBicluster(id);
			System.out.println("ID = " + id + "(" + t.getNumContexts() + "x" + t.getNumRows() + "x" + t.getNumCols() + ")");
			List<String> elements = generatedDataset.getBiclusterElements(id);
			Map<Integer, Integer> overlappedBicsCounter = new HashMap<>();
			for(String elem : elements) {
				List<Integer> overlappedBics = generatedDataset.getBicsByElem(elem);
				for(Integer bic : overlappedBics) {
					if(bic != id) {
						if(overlappedBicsCounter.containsKey(bic))
							overlappedBicsCounter.put(bic, overlappedBicsCounter.get(bic) + 1);
						else
							overlappedBicsCounter.put(bic, 1);
					}	
				}
			}
			for(Integer k : overlappedBicsCounter.keySet()) {
				int total = overlappedBicsCounter.get(k);
				double perc = ((double) total) / ((double) t.getSize());
				System.out.println("Overlaps with bic " + k + " in " + total + " (" + perc + ") positions");
				
				if(Double.compare(perc, overlapping.getMaxPercOfOverlappingElements()) > 0)
					throw new OutputErrorException("excedeu o max_overlap_elements (3)");
			}
		}
		*/
		
		
		//Percentage of missing values on the background, that is, values that do not belong to planted bics (Range = [0,1])
		//Maximum percentage of missing values on each bicluster. Range [0,1]. 
		//Ex: 0.1 significa que cada bic tem no maximo 10% de missings. Pode ter menos
		double missingPercOnBackground = 0.0;
		double missingPercOnPlantedBics = 0.0;
		
		//Same as above but for noise
		double noisePercOnBackground = 0.0;
		double noisePercOnPlantedBics = 0.0;
		//Level of symbol deviation, that is, the maximum difference between the current symbol on the matrix and the one that
		//will replaced it to be considered noise.
		//Ex: Let Alphabet = [1,2,3,4,5] and CurrentSymbol = 3, if the noiseDeviation is '1', then CurrentSymbol will be, randomly,
		//replaced by either '2' or '4'. If noiseDeviation is '2', CurrentSymbol can be replaced by either '1','2','4' or '5'.
		int noiseDeviation = 1;

		//Same as above but for errors
		//Similar as noise, a new value is considered an error if the difference between it and the current value in the matrix is
		//greater than noiseDeviation.
		//Ex: Alphabet = [1,2,3,4,5], If currentValue = 2, and errorDeviation = 2, to turn currentValue an error, it's value must be
		//replaced by '5', that is the only possible value that respects abs(currentValue - newValue) > noiseDeviation
		double errorPercOnBackground = 0.0;
		double errorPercOnPlantedBics = 0.0;
		
		generatedDataset.plantMissingElements(missingPercOnBackground, missingPercOnPlantedBics);
		generatedDataset.plantNoisyElements(noisePercOnBackground, noisePercOnPlantedBics, noiseDeviation);
		generatedDataset.plantErrors(errorPercOnBackground, errorPercOnPlantedBics, noiseDeviation);
		
		
		String bicDataFileName;
		String datasetFileName;
		
		bicDataFileName = "dataset_test_op_up_reg_bics";
		datasetFileName = "dataset_test_op_up_reg_data";
		 
		//Tests.testMissingNoiseError(generatedDataset);
		//System.out.println("Missings: " + Arrays.toString(generatedDataset.getMissingElements().toArray()));
		//System.out.println("Noise: " + Arrays.toString(generatedDataset.getNoisyElements().toArray()));
		//System.out.println("Errors: " + Arrays.toString(generatedDataset.getErrorElements().toArray()));
		
		GBicService serv = new GBicService();
		serv.setPath("/Users/atticus/git/G-Bic/G-Bic/temp/");
		serv.setSingleFileOutput(true);
		serv.saveResult(generatedDataset, bicDataFileName, datasetFileName);

		//IOUtils.generateHeatMap(bicDataFileName, datasetFileName);
		
		return generatedDataset;
	}

	public static HeterogeneousDataset generateHeterogeneous() throws Exception {

		long startTimeGen;
		long stopTimeGen;
		long startTimeBics;
		long stopTimeBics;

		//num de linhas do dataset
		int numRows = 100;
		//num de colunas do dataset
		int numCols = 50;
		int numericCols = 35;
		int symbolicCols = 15;
		//num de bics a plantar
		int numBics = 6;

		bics = numBics;
		
		//TODO: limites dos valores do dataset (usar em caso de dataset real)
		double min = 0;
		double max = 100;
		String[] alphabet = {"a","b","c","d","e","f"};

		//use real valued or integer alphabet
		boolean realValued = true;

		Background numericBackground = null;
		Background symbolicBackground = null;
		BiclusterDatasetGenerator generator = null;

		/* Background Normal(2.5, 1)
    	*/
    	numericBackground = new Background(BackgroundType.UNIFORM);
    	symbolicBackground = new Background(BackgroundType.UNIFORM);
		 

		/* Background Uniform
        background = new Background(BackgroundType.UNIFORM);
        generator = new BicMatrixGenerator(numRows,numCols,numBics, background, alphabetL, symmetries);
		 */

		/* Background Missing
        background = new Background(BackgroundType.MISSING);
		generator = new BicMatrixGenerator(numRows,numCols,numBics, background, alphabetL, symmetries);
		 */

		/*
		double[] probs = {0.05, 0.1, 0.3, 0.35, 0.2};
		background = new Background(BackgroundType.DISCRETE, probs);
		*/
		//background = new Background(BackgroundType.MISSING);
		
		//background = new Background(BackgroundType.UNIFORM);

		startTimeGen = System.currentTimeMillis();
		generator = new MixedDatasetGenerator(realValued, numRows, numericCols, symbolicCols, numBics, numericBackground,
				symbolicBackground, min, max, alphabet);
		stopTimeGen = System.currentTimeMillis();
		
		System.out.println("(BackgroundGenerator) Execution Time: " + ((double)(stopTimeGen - startTimeGen)) / 1000);

		//Padrao
		List<BiclusterPattern> patterns = new ArrayList<>();
		//patterns.add(new SingleBiclusterPattern(BiclusterType.NUMERIC, PatternType.ADDITIVE, PatternType.ADDITIVE, null));
		//patterns.add(new SingleBiclusterPattern(BiclusterType.SYMBOLIC, PatternType.CONSTANT, PatternType.CONSTANT, null));
		//patterns.add(new SingleBiclusterPattern(BiclusterType.SYMBOLIC, PatternType.ORDER_PRESERVING, PatternType.NONE, null));
		
		patterns.add(new SingleBiclusterPattern(BiclusterType.NUMERIC, PatternType.ADDITIVE, PatternType.CONSTANT, null));
		
		patterns.add(new SingleBiclusterPattern(BiclusterType.SYMBOLIC, PatternType.CONSTANT, PatternType.CONSTANT, null));
		
		patterns.add(new ComposedBiclusterPattern(BiclusterType.MIXED, new Pair<>(PatternType.MULTIPLICATIVE, PatternType.CONSTANT), null,
				new Pair<>(PatternType.ORDER_PRESERVING, PatternType.NONE), null));
		
		//** 3 - Define bicluster's structure **//
		//Object that encapsulates the configurations of the bicluster's structure
		BiclusterStructure bicStructure = new BiclusterStructure();
		
		//Distribution used to calculate the number of rows/cols/ctxs for a bic (NORMAL or UNIFORM)
		//Dist args: if dist=UNIFORM, then param1 and param2 represents the min and max, respectively
		//			 if dist=NORMAL, then param1 and param2 represents the mean and stdDev, respectively
		bicStructure.setRowsSettings(Distribution.UNIFORM, 5, 10);
		bicStructure.setColumnsSettings(Distribution.UNIFORM, 4, 7);
		
		//Contiguity can occour on COLUMNS or CONTEXTS. To avoid contiguity use NONE
		bicStructure.setContiguity(Contiguity.NONE);
		// ************* /

		//** Define overlapping settings ** //
		
		//Object to encapsulate overlapping parameters
		OverlappingSettings overlapping = new OverlappingSettings();
		
		//Plaid Coherency (ADDITIVE, MULTIPLICATIVE, INTERPOLED, NONE or NO_OVERLAPPING
		overlapping.setPlaidCoherency(PlaidCoherency.ADDITIVE);
		
		//Percentage of overlapping bics defines how many bics are allowed to overlap:
		//if 0.5 only half of the dataset biclusters will overlap
		overlapping.setPercOfOverlappingBics(1);
		
		//Maximum number of biclusters that can overlap together. if equal to 3, there will be, at max, 3 biclusters
		//that intersect each other(T1 ^ T2 ^ T3)
		overlapping.setMaxBicsPerOverlappedArea(3);
		
		//Maximum percentage of elements shared by overlapped biclusters. If 0.5, T1 ^ T2 will have, at max, 50% of the elements
		//of the smallest bic
		overlapping.setMaxPercOfOverlappingElements(0.5);
		
		//Percentage of allowed amount of overlaping across biclusters rows, columns and contexts. if rows=0.5, then 
		//T2 will intersect, at max, with half(50%) the rows of T1.
		//if you dont want any resbiction on the number of rows/cols/ctxs that can overlapp, use 1.0
		overlapping.setPercOfOverlappingRows(1.0);
		overlapping.setPercOfOverlappingColumns(1.0);	
		//** end of overlapping settings ** //
		
		startTimeBics = System.currentTimeMillis();
		HeterogeneousDataset generatedDataset = (HeterogeneousDataset) ((MixedDatasetGenerator) generator).generate(patterns, bicStructure, overlapping);
		stopTimeBics = System.currentTimeMillis();
		
		System.out.println("(GenerateBics) Execution Time: " + ((double) (stopTimeBics - startTimeBics)) / 1000);
		//System.out.println("Number of planted bics = " + generatedDataset.getBiclusters().size());
		
		/*
		for(int id = 0; id < numBics; id++) {
			NumericBicluster<?> t = generatedDataset.getBicluster(id);
			System.out.println("ID = " + id + "(" + t.getNumContexts() + "x" + t.getNumRows() + "x" + t.getNumCols() + ")");
			List<String> elements = generatedDataset.getBiclusterElements(id);
			Map<Integer, Integer> overlappedBicsCounter = new HashMap<>();
			for(String elem : elements) {
				List<Integer> overlappedBics = generatedDataset.getBicsByElem(elem);
				for(Integer bic : overlappedBics) {
					if(bic != id) {
						if(overlappedBicsCounter.containsKey(bic))
							overlappedBicsCounter.put(bic, overlappedBicsCounter.get(bic) + 1);
						else
							overlappedBicsCounter.put(bic, 1);
					}	
				}
			}
			for(Integer k : overlappedBicsCounter.keySet()) {
				int total = overlappedBicsCounter.get(k);
				double perc = ((double) total) / ((double) t.getSize());
				System.out.println("Overlaps with bic " + k + " in " + total + " (" + perc + ") positions");
				
				if(Double.compare(perc, overlapping.getMaxPercOfOverlappingElements()) > 0)
					throw new OutputErrorException("excedeu o max_overlap_elements (3)");
			}
		}
		*/
		
		
		//Percentage of missing values on the background, that is, values that do not belong to planted bics (Range = [0,1])
		//Maximum percentage of missing values on each bicluster. Range [0,1]. 
		//Ex: 0.1 significa que cada bic tem no maximo 10% de missings. Pode ter menos
		double missingPercOnBackground = 0.1;
		double missingPercOnPlantedBics = 0.2;
		
		//Same as above but for noise
		double noisePercOnBackground = 0.1;
		double noisePercOnPlantedBics = 0.2;
		//Level of symbol deviation, that is, the maximum difference between the current symbol on the matrix and the one that
		//will replaced it to be considered noise.
		//Ex: Let Alphabet = [1,2,3,4,5] and CurrentSymbol = 3, if the noiseDeviation is '1', then CurrentSymbol will be, randomly,
		//replaced by either '2' or '4'. If noiseDeviation is '2', CurrentSymbol can be replaced by either '1','2','4' or '5'.
		int noiseDeviation = 1;

		//Same as above but for errors
		//Similar as noise, a new value is considered an error if the difference between it and the current value in the matrix is
		//greater than noiseDeviation.
		//Ex: Alphabet = [1,2,3,4,5], If currentValue = 2, and errorDeviation = 2, to turn currentValue an error, it's value must be
		//replaced by '5', that is the only possible value that respects abs(currentValue - newValue) > noiseDeviation
		double errorPercOnBackground = 0.1;
		double errorPercOnPlantedBics = 0.2;
		
		generatedDataset.plantMissingElements(missingPercOnBackground, missingPercOnPlantedBics);
		generatedDataset.plantNoisyElements(noisePercOnBackground, noisePercOnPlantedBics, noiseDeviation);
		generatedDataset.plantErrors(errorPercOnBackground, errorPercOnPlantedBics, noiseDeviation);
		
		
		String bicDataFileName = "dataset_test_op_up_reg_bics";
		String datasetFileName = "dataset_test_op_up_reg_data";;
		 
		//Tests.testMissingNoiseError(generatedDataset);
		//System.out.println("Missings: " + Arrays.toString(generatedDataset.getMissingElements().toArray()));
		//System.out.println("Noise: " + Arrays.toString(generatedDataset.getNoisyElements().toArray()));
		//System.out.println("Errors: " + Arrays.toString(generatedDataset.getErrorElements().toArray()));
		
		GBicService serv = new GBicService();
		serv.setPath("/Users/atticus/git/G-Bic/G-Bic/temp/");
		serv.setSingleFileOutput(true);
		serv.saveResult(generatedDataset, bicDataFileName, datasetFileName);

		//IOUtils.generateHeatMap(bicDataFileName, datasetFileName);
		
		return generatedDataset;
	}
	
	public static void testArray(int []v) {
		for(int i = 0; i < v.length; i++)
			v[i] = i;
	}
}
