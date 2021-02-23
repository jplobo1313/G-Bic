package com.gbic.generator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.math3.util.Pair;

import com.gbic.domain.bicluster.Bicluster;
import com.gbic.domain.bicluster.MixedBicluster;
import com.gbic.domain.bicluster.NumericBicluster;
import com.gbic.domain.bicluster.SymbolicBicluster;
import com.gbic.domain.dataset.Dataset;
import com.gbic.domain.dataset.HeterogeneousDataset;
import com.gbic.domain.dataset.NumericDataset;
import com.gbic.exceptions.ExceedBiclusterBoundsException;
import com.gbic.exceptions.ExceedDatasetBoundsException;
import com.gbic.exceptions.OutputErrorException;
import com.gbic.types.Background;
import com.gbic.types.BiclusterType;
import com.gbic.types.Contiguity;
import com.gbic.types.PatternType;
import com.gbic.types.PlaidCoherency;
import com.gbic.types.TimeProfile;
import com.gbic.utils.BicMath;
import com.gbic.utils.BiclusterPattern;
import com.gbic.utils.BiclusterPatternWrapper;
import com.gbic.utils.BiclusterStructure;
import com.gbic.utils.ComposedBiclusterPattern;
import com.gbic.utils.OverlappingSettings;
import com.gbic.utils.SingleBiclusterPattern;

public class MixedDatasetGenerator extends BiclusterDatasetGenerator {

	private HeterogeneousDataset data;
	private Random random = new Random();
	private boolean allowsOverlap = false;
	private boolean realValued;
	private int numBics;
	private int numRows;
	private int numCols;
	private Set<Integer> chosenCols;

	/**
	 * Constructor
	 * @param realValued boolean that indicates if the dataset is real valued
	 * @param numRows the dataset's number of rows
	 * @param numCols the dataset's number of columns
	 * @param numContexts the dataset's number of contexts
	 * @param numBics the number of bics to plant
	 * @param background the dataset's background
	 * @param minM the dataset's minimum alphabet value
	 * @param maxM the dataset's maximum alphabet value
	 */
	public MixedDatasetGenerator(boolean realValued, int numRows, int numericCols, int symbolicCols, int numBics, Background numericBackground,
			Background symbolicBackground, double minM, double maxM, String[] alphabet) {

		this.realValued = realValued;
		this.numBics = numBics;

		this.data = new HeterogeneousDataset(numRows, numericCols, symbolicCols, numBics, numericBackground, symbolicBackground,
				minM, maxM, realValued, alphabet);
	}

	@Override
	public Dataset generate(List<BiclusterPattern> bicPatterns, BiclusterStructure bicStructure, OverlappingSettings overlapping) throws Exception {

		this.allowsOverlap = !overlapping.getPlaidCoherency().equals(PlaidCoherency.NO_OVERLAPPING);
		int maxBicsPerOverlappedArea = overlapping.getMaxBicsPerOverlappedArea();
		int overlappingThreshold = (int)(data.getNumBics() * overlapping.getPercOfOverlappingBics());
		PlaidCoherency plaidPattern = overlapping.getPlaidCoherency();

		//num rows/cols/ctx of the expression matrix
		this.numRows = data.getNumRows();
		this.numCols = data.getNumCols();
		this.chosenCols = new HashSet<>();

		int[][] bicsRows = new int[numBics][];
		int[][] bicsCols = new int[numBics][];

		//only useful when overlapping is required
		double maxOverlap = (plaidPattern.equals(PlaidCoherency.MULTIPLICATIVE)) ? 1 : 0;
		double minOverlap = (plaidPattern.equals(PlaidCoherency.MULTIPLICATIVE)) ? 1 : 0;
		
		int numAttempts = 0;
		
		for (Integer k = 0; k < numBics; k++) {
		
			changeState("Stage:1, Msg:Bicluster " + k);
			
			if(k >= overlappingThreshold)
				allowsOverlap = false;

			System.out.println("Generating bicluster " + (k+1) + " of " + numBics + "...");
			
			BiclusterPattern currentPattern = null;

			int patternsSize = bicPatterns.size();
			
			if(numBics <  patternsSize)
				currentPattern = bicPatterns.get(random.nextInt(patternsSize));
			else
				currentPattern = bicPatterns.get(k % patternsSize);
				
			/** PART IV: select biclusters with (non-)overlapping elements **/
			int[] bicsWithOverlap = null;
			int[] bicsExcluded = null;

			if (this.allowsOverlap) {				
				if (k % maxBicsPerOverlappedArea != 0)
					bicsWithOverlap = new int[] { k - 1 };
	
				int l = Math.max((k / maxBicsPerOverlappedArea) * maxBicsPerOverlappedArea, k-1);
				//int l = (k / maxBicsPerOverlappedArea) * maxBicsPerOverlappedArea;
				bicsExcluded = new int[l];
				for (int i = 0; i < l; i++)
					bicsExcluded[i] = i;
			}

			boolean success = false;
			
			if(currentPattern.getBiclusterType().equals(BiclusterType.MIXED)) {
				MixedBicluster bicK = generateMixedBicluster(k, (ComposedBiclusterPattern) currentPattern, bicStructure, bicsRows, bicsCols, 
						bicsWithOverlap, bicsExcluded, overlapping, maxBicsPerOverlappedArea, plaidPattern, minOverlap, maxOverlap);
				
				if(success = (bicK != null))
					data.addPlantedMixedBic(bicK);
			}
			else if(currentPattern.getBiclusterType().equals(BiclusterType.NUMERIC)) {
				NumericBicluster<Double> bicK = (NumericBicluster<Double>) generateNormalBiclusters(k, (SingleBiclusterPattern) currentPattern, 
						bicStructure, bicsRows, bicsCols, bicsWithOverlap, bicsExcluded, overlapping, maxBicsPerOverlappedArea, plaidPattern,
						minOverlap, maxOverlap);
				
				if(success = (bicK != null))
					data.addPlantedNumericBic(bicK);
			}
			else {
				SymbolicBicluster bicK = (SymbolicBicluster) generateNormalBiclusters(k, (SingleBiclusterPattern) currentPattern, bicStructure, 
						bicsRows, bicsCols, bicsWithOverlap, bicsExcluded, overlapping, maxBicsPerOverlappedArea, plaidPattern, minOverlap, maxOverlap);
				
				if(success = (bicK != null))
					data.addPlantedSymbolicBic(bicK);
			}
			
			if(!success) {
				k--;
				if(numAttempts == 15) {
					numBics--;
					numAttempts = 0;
				}
				else
					numAttempts++;
			}
			else
				numAttempts = 0;
		}

		return data;
	}
	
	private Bicluster generateNormalBiclusters(Integer k, SingleBiclusterPattern currentPattern, BiclusterStructure bicStructure, int[][] bicsRows, 
			int[][] bicsCols, int[] bicsWithOverlap, int[] bicsExcluded, OverlappingSettings overlapping, int maxBicsPerOverlappedArea, 
			PlaidCoherency plaidPattern, double minOverlap, double maxOverlap) throws Exception{
	
		int numRowsBics = 0;
		int numColsBics = 0;
		Bicluster bicK = null;

		Map<String, Integer> structure = generateBicStructure(bicStructure, numRows, numCols);

		numRowsBics = structure.get("rows");
		
		if(currentPattern.getBiclusterType().equals(BiclusterType.NUMERIC))
			numColsBics = Math.min(structure.get("columns"), data.getNumericCols());
			
		else
			numColsBics = Math.min(structure.get("columns"), data.getSymbolicCols());	


		/** PART V: generate rows and columns using overlapping constraints **/
		int bicSize = numRowsBics * numColsBics;
		
		int low = (currentPattern.getBiclusterType().equals(BiclusterType.NUMERIC)) ? 0 : data.getNumericCols();
		int high = (currentPattern.getBiclusterType().equals(BiclusterType.NUMERIC)) ? data.getNumericCols() : data.getNumCols();
		Pair<Integer, Integer> range = new Pair<>(low, high);
		
		if (allowsOverlap) {
			
			Map<String, Double> overlappingPercs = null;
		
			overlappingPercs = generateOverlappingDistribution(bicSize, overlapping, numRowsBics, numColsBics);

			double overlappingColsPerc = overlappingPercs.get("columnPerc");
			double overlappingRowsPerc = overlappingPercs.get("rowPerc");
			
			int validCols = (currentPattern.getBiclusterType().equals(BiclusterType.NUMERIC)) ? data.getNumericCols() : numCols;
			
			System.out.println("Bic " + (k+1) + " - Generating columns...");
			bicsCols[k] = generate(numColsBics, validCols, overlappingColsPerc, bicsCols, bicsWithOverlap,
					bicsExcluded, bicStructure.getContiguity().equals(Contiguity.COLUMNS), range);
			System.out.println("Columns: " + bicsCols[k].length);
			
			System.out.println("Bic " + (k+1) + " - Generating rows...");
			bicsRows[k] = generateRows(numRowsBics, numRows, overlappingRowsPerc, bicsRows, bicsWithOverlap,
					bicsExcluded, bicsCols[k], data.getElements());
			System.out.println("Rows: " + bicsRows[k].length);
		}
		else {
			
			System.out.println("Bic " + (k+1) + " - Generating columns...");
			bicsCols[k] = generateNonOverlappingOthers(numColsBics, numCols, chosenCols, bicStructure.getContiguity().equals(Contiguity.COLUMNS),
					range);
			System.out.println("Columns: " + bicsCols[k].length);
			
			
			System.out.println("Bic " + (k+1) + " - Generating rows...");
			bicsRows[k] = generateNonOverlappingRows(numRowsBics, numRows, bicsCols[k], data.getElements());
			System.out.println("Rows: " + bicsRows[k].length);
		}
		
		if(bicsRows[k] != null) {
			
			System.out.println("Bic " + (k+1) + " - Has space, lets plant the patterns");

			Arrays.parallelSort(bicsRows[k]);
			Arrays.parallelSort(bicsCols[k]);
			
			for (Integer c : bicsCols[k])
				chosenCols.add(c);

			PatternType rowType = currentPattern.getRowsPattern();
			PatternType columnType = currentPattern.getColumnsPattern();
			TimeProfile timeProfile = currentPattern.getTimeProfile();
			
			if(currentPattern.getBiclusterType().equals(BiclusterType.NUMERIC)) {
				
				bicK = new NumericBicluster<>(k, BicMath.getSet(bicsRows[k]), 
					BicMath.getSet(bicsCols[k]), rowType, columnType, new Double[numRowsBics], new Double[numColsBics], plaidPattern, timeProfile);
			
				bicK = generateNumericCoherency((NumericBicluster<Double>) bicK, bicsRows[k], bicsCols[k], maxBicsPerOverlappedArea, plaidPattern, maxOverlap,
						minOverlap, (SingleBiclusterPattern) currentPattern);
			}
			else {				
				
				bicK = new SymbolicBicluster(k, BicMath.getSet(bicsRows[k]), BicMath.getSet(bicsCols[k]), rowType, columnType, 
						overlapping.getPlaidCoherency(), timeProfile);
				
				bicK = generateSymbolicCoherency((SymbolicBicluster) bicK, bicsRows[k], bicsCols[k], (SingleBiclusterPattern) currentPattern, data.getAlphabet());
			}	
		}
		
		return bicK;
	}
	
	private MixedBicluster generateMixedBicluster(Integer k, ComposedBiclusterPattern currentPattern, BiclusterStructure bicStructure, 
			int[][] bicsRows, int[][] bicsCols, int[] bicsWithOverlap, int[] bicsExcluded, OverlappingSettings overlapping, 
			int maxBicsPerOverlappedArea,  PlaidCoherency plaidPattern, double minOverlap, double maxOverlap) throws Exception{
		
		int numRowsBics;
		int numColsBics;
		int[] numericCols;
		int[] symbolicCols;
		MixedBicluster bicK = null;
		
		SortedSet<Integer> chosenNumericCols = new TreeSet<>();
		SortedSet<Integer> chosenSymbolicCols = new TreeSet<>();

		Map<String, Integer> structure = generateBicStructure(bicStructure, numRows, numCols);

		numRowsBics = structure.get("rows");
		numColsBics = structure.get("columns");
			
			
		int numNumericColsBics = -1;
		int numSymbolicColsBics = -1;
		
		//Split bic columns in numeric and symbolic
		if(data.getSymbolicCols() < data.getNumericCols()) {
			numSymbolicColsBics = random.nextInt(Math.min(data.getSymbolicCols(), numColsBics) - 1) + 1;
			numNumericColsBics = numColsBics - numSymbolicColsBics;
		}
		else {
			numNumericColsBics = random.nextInt(Math.min(data.getNumericCols(), numColsBics) - 1) + 1;
			numSymbolicColsBics = numColsBics - numNumericColsBics;
		}

		/** PART V: generate rows and columns using overlapping constraints **/
		int bicSize = numRowsBics * numColsBics;
		if (allowsOverlap) {
			
			Map<String, Double> overlappingPercs = null;
			
			overlappingPercs = generateOverlappingDistribution(bicSize, overlapping, numRowsBics, numColsBics);

			double overlappingColsPerc = overlappingPercs.get("columnPerc");
			double overlappingRowsPerc = overlappingPercs.get("rowPerc");
			
			int lowNumeric = 0;
			int lowSymbolic = data.getNumericCols();
			
			int highNumeric = data.getNumericCols();
			int highSymbolic = data.getNumCols();
			
			Pair<Integer, Integer> rangeNumeric = new Pair<>(lowNumeric, highNumeric);
			Pair<Integer, Integer> rangeSymbolic = new Pair<>(lowSymbolic, highSymbolic);
			
			System.out.println("Bic " + (k+1) + " - Generating columns...");
			numericCols = generate(numNumericColsBics, data.getNumericCols(), overlappingColsPerc, bicsCols, bicsWithOverlap,
					bicsExcluded, bicStructure.getContiguity().equals(Contiguity.COLUMNS), rangeNumeric);
			System.out.println("NumericColumns: " + numericCols.length);
			
			symbolicCols = generate(numSymbolicColsBics, data.getNumCols(), overlappingColsPerc, bicsCols, bicsWithOverlap,
					bicsExcluded, bicStructure.getContiguity().equals(Contiguity.COLUMNS), rangeSymbolic);
			System.out.println("SymbolicColumns: " + symbolicCols.length);
			
			
			bicsCols[k] = new int[numericCols.length + symbolicCols.length];
			
			for(int n = 0; n < numericCols.length; n++) {
				chosenNumericCols.add(numericCols[n]);
				bicsCols[k][n] = numericCols[n];
				}
			
			for(int c = 0; c < symbolicCols.length; c++) {
				chosenSymbolicCols.add(symbolicCols[c]);
				bicsCols[k][c + numericCols.length] = symbolicCols[c];
			}
			
			System.out.println("Bic " + (k+1) + " - Generating rows...");
			bicsRows[k] = generateRows(numRowsBics, numRows, overlappingRowsPerc, bicsRows, bicsWithOverlap,
					bicsExcluded, bicsCols[k], data.getElements());
			System.out.println("Rows: " + bicsRows[k].length);
		}
		else {

			int lowNumeric = 0;
			int lowSymbolic = data.getNumericCols();
			
			int highNumeric = data.getNumericCols();
			int highSymbolic = data.getNumCols();
			
			Pair<Integer, Integer> rangeNumeric = new Pair<>(lowNumeric, highNumeric);
			Pair<Integer, Integer> rangeSymbolic = new Pair<>(lowSymbolic, highSymbolic);
			
			System.out.println("Bic " + (k+1) + " - Generating columns...");
			
			numericCols = generateNonOverlappingOthers(numNumericColsBics, data.getNumericCols(), chosenCols, bicStructure.getContiguity().equals(Contiguity.COLUMNS),
					rangeNumeric);
			System.out.println("Numeric Columns: " + numericCols.length);
			
			symbolicCols = generateNonOverlappingOthers(numSymbolicColsBics, data.getNumCols(), chosenCols, bicStructure.getContiguity().equals(Contiguity.COLUMNS),
					rangeSymbolic);
			System.out.println("Numeric Columns: " + symbolicCols.length);
			
			bicsCols[k] = new int[numericCols.length + symbolicCols.length];
			
			for(int n = 0; n < numericCols.length; n++) {
				chosenNumericCols.add(numericCols[n]);
				bicsCols[k][n] = numericCols[n];
				}
			
			for(int c = 0; c < symbolicCols.length; c++) {
				chosenSymbolicCols.add(symbolicCols[c]);
				bicsCols[k][c + numericCols.length] = symbolicCols[c];
			}
		
			System.out.println("Bic " + (k+1) + " - Generating rows...");
			bicsRows[k] = generateNonOverlappingRows(numRowsBics, numRows, bicsCols[k], data.getElements());
			System.out.println("Rows: " + bicsRows[k].length);
		}

		if(bicsRows[k] != null) {
			
			System.out.println("Bic " + (k+1) + " - Has space, lets plant the patterns");
			
			for (Integer c : bicsCols[k])
				chosenCols.add(c);

			Arrays.parallelSort(bicsRows[k]);
			Arrays.parallelSort(bicsCols[k]);
				
			Arrays.parallelSort(numericCols);
			Arrays.parallelSort(symbolicCols);
			
			NumericBicluster<Double> numericComponent;
			SymbolicBicluster symbolicComponent;
			
			PatternType numericRowType = currentPattern.getNumericP().getFirst();
			PatternType numericColumnType = currentPattern.getNumericP().getSecond();
			TimeProfile numericTimeProfile = currentPattern.getNumericTP();
			
			PatternType symbolicRowType = currentPattern.getSymbolicP().getFirst();
			PatternType symbolicColumnType = currentPattern.getSymbolicP().getSecond();
			TimeProfile symbolicTimeProfile = currentPattern.getSymbolicTP();
			
			
			numericComponent = new NumericBicluster<>(k, BicMath.getSet(bicsRows[k]), 
					chosenNumericCols, numericRowType, numericColumnType, new Double[numRowsBics], new Double[numNumericColsBics],
					plaidPattern, numericTimeProfile);
		
			
			
			symbolicComponent = new SymbolicBicluster(k, BicMath.getSet(bicsRows[k]), chosenSymbolicCols, symbolicRowType, symbolicColumnType, 
					overlapping.getPlaidCoherency(), symbolicTimeProfile);
			
			bicK = new MixedBicluster(k, numericComponent, symbolicComponent, BicMath.getSet(bicsRows[k]));
			
			generateNumericCoherency(numericComponent, bicsRows[k], numericCols, maxBicsPerOverlappedArea, plaidPattern, maxOverlap, minOverlap, 
					currentPattern.getComponentPattern(false));
			
			generateSymbolicCoherency(symbolicComponent, bicsRows[k], symbolicCols, currentPattern.getComponentPattern(true), data.getAlphabet());			
		}
		
		return bicK;
	}
	
	private NumericBicluster<Double> generateNumericCoherency(NumericBicluster<Double> bicK, int[] bicsRows, int[] bicsCols, int maxBicsPerOverlappedArea,
			PlaidCoherency plaidPattern, double maxOverlapp, double minOverlapp, SingleBiclusterPattern currentPattern) throws ExceedDatasetBoundsException, ExceedBiclusterBoundsException {
		
		int k = bicK.getId();
		
		/** PART VI: generate biclusters coherencies **/
		Double[][] bicsymbols = new Double[bicsRows.length][bicsCols.length];

		double maxAlphabet = realValued ? data.getMaxM().doubleValue() : data.getMaxM().intValue();
		double minAlphabet = realValued ? data.getMinM().doubleValue() : data.getMinM().intValue();
		double maxAllowed = maxAlphabet;
		double minAllowed = minAlphabet;
		double lowerBound = maxAlphabet;

		Pair<Double, Double> overlappLimits = null;

		//expression and biclusters symbols

		if(this.allowsOverlap) {
			//When (k % nrOverlappingBics == 0), we are on the first bicluster that will be overlapped, so, we
			//must reset maxOverlapp and minOverlapp.
			if (k % maxBicsPerOverlappedArea == 0) {
				if(plaidPattern.equals(PlaidCoherency.ADDITIVE)) {
					maxOverlapp = 0;
					minOverlapp = 0;
				}
				else if(plaidPattern.equals(PlaidCoherency.MULTIPLICATIVE)) {
					maxOverlapp = 1;
					minOverlapp = 1;

				}
			}
			//On the other hand, if we are on one of the other (nrOverlappingBics - 1) triclusters that will overlapp
			//the previous one, we should check which are the maximum and minimum values on the area that will be overlapped by
			//this new bicluster.
			else {
				overlappLimits = getLimitsOnOverlappedArea(this.data.getElements(), bicsRows, bicsCols);
			}


			if(plaidPattern.equals(PlaidCoherency.ADDITIVE)) {

				if(k % maxBicsPerOverlappedArea != 0) {

					double first = overlappLimits.getFirst();
					double second = overlappLimits.getSecond();

					if(Double.compare(first, maxAlphabet) < 0 || Double.compare(second, minAlphabet) > 0) {
						maxOverlapp += (Double.compare(second, 0) > 0) ? second : 0;
						minOverlapp += (Double.compare(first, 0) < 0) ? first : 0;
					}
				}

				if(Double.compare(maxAlphabet, 0) > 0)
					maxAllowed = (maxAlphabet - maxOverlapp) / (maxBicsPerOverlappedArea - (k % maxBicsPerOverlappedArea));

				if(Double.compare(minAlphabet, 0) < 0)
					minAllowed = (minAlphabet - minOverlapp) / (maxBicsPerOverlappedArea - (k % maxBicsPerOverlappedArea));
			}
			else if(plaidPattern.equals(PlaidCoherency.MULTIPLICATIVE)) {

				if (k % maxBicsPerOverlappedArea != 0) {
					double first = overlappLimits.getFirst();
					double second = overlappLimits.getSecond();

					if(Double.compare(first, maxAlphabet) < 0 || Double.compare(second, minAlphabet) > 0) {
						maxOverlapp = Math.max(Math.abs(first), Math.abs(second));
						maxOverlapp *= (Double.compare(maxOverlapp, 0.0) == 0) ? 1 : maxOverlapp;
					}
				}

				//when the alphabet is strictly positive
				if(Double.compare(minAlphabet, 0) >= 0)
					maxAllowed = BicMath.nthRoot(maxAlphabet / maxOverlapp, (maxBicsPerOverlappedArea - (k % maxBicsPerOverlappedArea)));
				else { 
					lowerBound = Math.min(Math.abs(minAlphabet), Math.abs(maxAlphabet));
					maxAllowed = BicMath.nthRoot(lowerBound / maxOverlapp, (maxBicsPerOverlappedArea - (k % maxBicsPerOverlappedArea)));

					if(Double.compare(maxAllowed, lowerBound) > 0)
						maxAllowed = lowerBound;

					minAllowed = - maxAllowed;
				}
			}
		}

		if(currentPattern.contains(PatternType.ADDITIVE)) {				
			bicsymbols = generateAdditiveFactors(realValued, currentPattern, bicK, minAllowed, maxAllowed);
		}

		else if(currentPattern.contains(PatternType.MULTIPLICATIVE)) {

			bicsymbols = generateMultiplicativeFactors(realValued, currentPattern, bicK, minAllowed, maxAllowed);
		} 

		else if(currentPattern.contains(PatternType.ORDER_PRESERVING)) {
			bicsymbols = generateOrderPreserving(realValued, currentPattern, bicK, minAllowed, maxAllowed);


		}
		else if (currentPattern.contains(PatternType.CONSTANT)){
			bicsymbols = generateConstant(realValued, currentPattern, bicK, minAllowed, maxAllowed);
		}

		
		/**
		 * Part VII: generate the layers according to plaid type and put them in the
		 * background
		 **/
		System.out.println("Bic " + (k+1) + " - planting the bic");
		
		for (int row = 0; row < bicsRows.length; row++) {
			for (int col = 0; col < bicsCols.length; col++) {

				Double value = bicsymbols[row][col];

				if(data.getElements().contains(bicsRows[row] + ":" + bicsCols[col])) {

					switch (plaidPattern) {
					case ADDITIVE:
						value += data.getNumericElement(bicsRows[row], bicsCols[col]).doubleValue();
						break;
					case MULTIPLICATIVE:
						value *= data.getNumericElement(bicsRows[row], bicsCols[col]).doubleValue();
						break;
					case INTERPOLED:
						value = ((value + data.getNumericElement(bicsRows[row], bicsCols[col]).doubleValue()) / 2);

						if(!realValued) {
							if(Double.compare(Math.round(value), minAlphabet) >= 0 && 
									Double.compare(Math.round(value), maxAlphabet) <= 0)
								value = (double) Math.round(value);
							else
								value = (double) value.intValue();
						}

						break;
					default:
						break;
					}			
				}

				if(realValued)
					data.setNumericElement(bicsRows[row], bicsCols[col], value);
				else
					data.setNumericElement(bicsRows[row], bicsCols[col], new Double(value.intValue()));

				if(Double.compare(value.doubleValue(), minAlphabet) < 0 || Double.compare(value.doubleValue(), maxAlphabet) > 0)
					throw new ExceedDatasetBoundsException("Exceeded dataset limits: Value = " + value.doubleValue());

				data.addElement(bicsRows[row] + ":" + bicsCols[col], bicK.getId());
			}
		}
		
		return bicK;
	}
	
	private Pair<Double, Double> getLimitsOnOverlappedArea(Set<String> existingValues, int[] rows, int[] cols) {

		double min = this.data.getMaxM().doubleValue() + 1;
		double max = this.data.getMinM().doubleValue() - 1 ;

		
		for(Integer row : rows) {
			for(Integer col : cols) {
				if(existingValues.contains(row + ":" + col)) {
					double value = this.data.getNumericElement(row, col).doubleValue();
					min = (Double.compare(value, min) < 0) ? value : min;
					max = (Double.compare(value, max) > 0) ? value : max;
				}
			}
		}
		return new Pair<>(min, max);
	}

	private SymbolicBicluster generateSymbolicCoherency(SymbolicBicluster bicK, int[] bicsRows, int[] bicsCols, SingleBiclusterPattern currentPattern, 
			String[] alphabet) {
		
		int k = bicK.getId();
		/** PART VI: generate biclusters coherencies **/
		String[][] bicsymbols = new String[bicsRows.length][bicsCols.length];
		
		int numColsBics = bicsCols.length;
		int numRowsBics = bicsRows.length;
		PatternType rowType = currentPattern.getRowsPattern();
		PatternType columnType = currentPattern.getColumnsPattern();
		TimeProfile timeProfile = currentPattern.getTimeProfile();
		
		if(rowType.equals(PatternType.ORDER_PRESERVING)) {
			bicsymbols = new String[bicsCols.length][bicsRows.length];
			Integer[] order = generateOrder(bicsRows.length);
			
			
			for(int col = 0; col < numColsBics; col++) {
				for (int row = 0; row < numRowsBics; row++)
					bicsymbols[col][row] = alphabet[random.nextInt(alphabet.length)];
				Arrays.parallelSort(bicsymbols[col]);
				bicsymbols[col] = shuffle(order, bicsymbols[col]);
			}					
			bicsymbols = transposeMatrix(bicsymbols, "x", "y");
		}
		else if(columnType.equals(PatternType.ORDER_PRESERVING)) {
			Integer[] order = generateOrder(bicsCols.length);
			for(int row = 0; row < numRowsBics; row++) {
				for (int col = 0; col < numColsBics; col++)
					bicsymbols[row][col] = alphabet[random.nextInt(alphabet.length)];
				
				Arrays.parallelSort(bicsymbols[row]);
				bicsymbols[row] = shuffle(order, bicsymbols[row]);
				
				if(timeProfile.equals(TimeProfile.RANDOM)) {
					Arrays.parallelSort(bicsymbols[row]);
					bicsymbols[row] = shuffle(order, bicsymbols[row]);
				}
				else if(timeProfile.equals(TimeProfile.MONONICALLY_INCREASING))
					Arrays.sort(bicsymbols[row]);
				
				else
					Arrays.sort(bicsymbols[row], Collections.reverseOrder());
			}
		}
		else if(rowType.equals(PatternType.CONSTANT) && columnType.equals(PatternType.CONSTANT)) {
			String seed = alphabet[random.nextInt(alphabet.length)];
			
			for(int row = 0; row < numRowsBics; row++) 
				for (int col = 0; col < numColsBics; col++)
					bicsymbols[row][col] = seed;	
			
		
			bicK.setSeed(bicsymbols);
		}
		else if(columnType.equals(PatternType.CONSTANT)) {
				for (int row = 0; row < numRowsBics; row++) {
					String seed = alphabet[random.nextInt(alphabet.length)];
					for(int col = 0; col < numColsBics; col++)
						bicsymbols[row][col] = seed;
				}
		}
		else if(rowType.equals(PatternType.CONSTANT)) {
				for (int col = 0; col < numColsBics; col++) {
					String seed = alphabet[random.nextInt(alphabet.length)];
					for(int row = 0; row < numRowsBics; row++)
						bicsymbols[row][col] = seed;
				}
		}

		/**
		 * Part VII: generate the layers according to plaid type and put them in the
		 * background
		 **/
		System.out.println("Tric " + (k+1) + " - planting the tric");
		
		for (int row = 0; row < bicsRows.length; row++) {
			for (int col = 0; col < bicsCols.length; col++) {
				this.data.setSymbolicElement(bicsRows[row], bicsCols[col], bicsymbols[row][col]);
				data.addElement(bicsRows[row] + ":" + bicsCols[col], k);
			}
		}
		
		return bicK;
	}
	
	private Set<Integer> getChosenCols(){
		return this.chosenCols;
	}
	
	private void addChosenCol(int col) {
		this.chosenCols.add(col);
	}
}
