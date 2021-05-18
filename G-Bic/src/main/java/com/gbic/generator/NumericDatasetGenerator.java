/**
 * NumericBiclusterDatasetGenerator Class
 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @version 1.0
 */
package com.gbic.generator;

import java.util.Arrays;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import com.gbic.domain.bicluster.NumericBicluster;
import com.gbic.domain.dataset.Dataset;
import com.gbic.domain.dataset.NumericDataset;
import com.gbic.exceptions.ExceedDatasetBoundsException;
import com.gbic.exceptions.ExceedBiclusterBoundsException;
import com.gbic.types.Background;
import com.gbic.types.Contiguity;
import com.gbic.types.PatternType;
import com.gbic.types.PlaidCoherency;
import com.gbic.types.TimeProfile;
import com.gbic.utils.BicMath;
import com.gbic.utils.OverlappingSettings;
import com.gbic.utils.SingleBiclusterPattern;
import com.gbic.utils.RandomObject;
import com.gbic.utils.BiclusterPattern;
import com.gbic.utils.BiclusterStructure;

public class NumericDatasetGenerator extends BiclusterDatasetGenerator {

	private NumericDataset data;
	private Random random = RandomObject.getInstance();
	private boolean allowsOverlap = false;
	private boolean realValued;
	private int numBics;

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
	public NumericDatasetGenerator(boolean realValued, int numRows, int numCols, int numBics, Background background, double minM, double maxM) {

		this.realValued = realValued;
		this.numBics = numBics;
		
		if(realValued)
			this.data = new NumericDataset<Double>(numRows, numCols, numBics, background,
					minM, maxM);
		else
			this.data = new NumericDataset<Integer>(numRows, numCols, numBics, background,
					(int) minM, (int) maxM);
	}

	@Override
	public Dataset generate(List<BiclusterPattern> patterns, BiclusterStructure bicStructure,
			OverlappingSettings overlapping) throws Exception {

		this.allowsOverlap = !overlapping.getPlaidCoherency().equals(PlaidCoherency.NO_OVERLAPPING);
		int maxBicsPerOverlappedArea = overlapping.getMaxBicsPerOverlappedArea();
		int overlappingThreshold = (int)(data.getNumBics() * overlapping.getPercOfOverlappingBics());
		PlaidCoherency plaidPattern = overlapping.getPlaidCoherency();

		//num rows/cols/ctx of the expression matrix
		int numRows = data.getNumRows();
		int numCols = data.getNumCols();

		//num of rows/cols/ctxs of a bic
		int numRowsBics = 0;
		int numColsBics = 0;

		//Isto pode ser otimizado -> passar p/ dentro do for (1D em vez de 2D).
		int[][] bicsRows = new int[numBics][];
		int[][] bicsCols = new int[numBics][];

		Set<Integer> chosenCols = new HashSet<>();

		/** PART I: generate pattern ranges **/

		//only useful when overlapping is required
		double maxOverlapp = -1;
		double minOverlapp = -1;

		if(this.allowsOverlap) {
			//default case when plaid is addictive
			maxOverlapp = 0;
			minOverlapp = 0;

			if(plaidPattern.equals(PlaidCoherency.MULTIPLICATIVE)) {
				maxOverlapp = 1;
				minOverlapp = 1;
			}
		}

		int numAttempts = 0;
		
		for (int k = 0; k < numBics; k++) {

			boolean hasSpace = true;

			changeState("Stage:1, Msg:Bicluster " + k);
			
			if(k >= overlappingThreshold)
				allowsOverlap = false;

			System.out.println("Generating bicluster " + (k+1) + " of " + numBics + "...");
			
			SingleBiclusterPattern currentPattern;

			if(numBics < patterns.size())
				currentPattern = (SingleBiclusterPattern) patterns.get(random.nextInt(patterns.size()));
			else
				currentPattern = (SingleBiclusterPattern) patterns.get(k % patterns.size());

			PatternType rowType = currentPattern.getRowsPattern();
			PatternType columnType = currentPattern.getColumnsPattern();
			TimeProfile timeProfile = currentPattern.getTimeProfile();

			Map<String, Integer> structure = generateBicStructure(bicStructure, numRows, numCols);

			numRowsBics = structure.get("rows");
			numColsBics = structure.get("columns");

			/** PART IV: select biclusters with (non-)overlapping elements **/
			int[] bicsWithOverlap = null;
			int[] bicsExcluded = null;

			if (this.allowsOverlap) {

				boolean dispersed = false;
				if (dispersed) {
					if ((k + 1) % maxBicsPerOverlappedArea == 0) {
						bicsWithOverlap = new int[Math.min(k, maxBicsPerOverlappedArea - 1)];
						for (int i = k - 1, j = 0; i >= 0 && j < maxBicsPerOverlappedArea - 1; i--, j++)
							bicsWithOverlap[j] = i;
					}
				} 
				else if (k % maxBicsPerOverlappedArea != 0)
					bicsWithOverlap = new int[] { k - 1 };

				int l = Math.max((k / maxBicsPerOverlappedArea) * maxBicsPerOverlappedArea, k-1);
				//int l = (k / maxBicsPerOverlappedArea) * maxBicsPerOverlappedArea;
				bicsExcluded = new int[l];
				for (int i = 0; i < l; i++)
					bicsExcluded[i] = i;
			}

			/** PART V: generate rows and columns using overlapping constraints **/
			//Se forem colunas contiguas nunca gera overlapping nas colunas?
			int bicSize = numRowsBics * numColsBics;
			if (allowsOverlap) {
				
				Map<String, Double> overlappingPercs = generateOverlappingDistribution(bicSize, overlapping, numRowsBics, numColsBics);

				double overlappingColsPerc = overlappingPercs.get("columnPerc");
				double overlappingRowsPerc = overlappingPercs.get("rowPerc");
				
				System.out.println("Bic " + (k+1) + " - Generating columns...");
				bicsCols[k] = generate(numColsBics, numCols, overlappingColsPerc, bicsCols, bicsWithOverlap,
						bicsExcluded, bicStructure.getContiguity().equals(Contiguity.COLUMNS), null);
				System.out.println("Columns: " + bicsCols[k].length);
			
				System.out.println("Bic " + (k+1) + " - Generating rows...");
				bicsRows[k] = generateRows(numRowsBics, numRows, overlappingRowsPerc, bicsRows, bicsWithOverlap,
						bicsExcluded, bicsCols[k], data.getElements());
				System.out.println("Rows: " + bicsRows[k].length);
				
				if(bicsRows[k] == null) {
					hasSpace = false;
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
			else {
				System.out.println("Bic " + (k+1) + " - Generating columns...");
				bicsCols[k] = generateNonOverlappingOthers(numColsBics, numCols, chosenCols, bicStructure.getContiguity().equals(Contiguity.COLUMNS),
						null);
				System.out.println("Columns: " + bicsCols[k].length);
				
				System.out.println("Bic " + (k+1) + " - Generating rows...");
				bicsRows[k] = generateNonOverlappingRows(numRowsBics, numRows, bicsCols[k], data.getElements());
				System.out.println("Rows: " + bicsRows[k].length);

				if(bicsRows[k] == null) {
					hasSpace = false;
					System.out.println("Bic " + (k+1) + " no space :(");
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

			if(hasSpace) {
				
				System.out.println("Bic " + (k+1) + " - Has space, lets plant the patterns");
				
				for (Integer c : bicsCols[k])
					chosenCols.add(c);

				Arrays.parallelSort(bicsRows[k]);
				Arrays.parallelSort(bicsCols[k]);
				
				NumericBicluster<Double> bicK;
				if(columnType.equals(PatternType.ORDER_PRESERVING))
					bicK = new NumericBicluster<>(k, BicMath.getSet(bicsRows[k]), 
						BicMath.getSet(bicsCols[k]), rowType, columnType, new Double[numRowsBics], new Double[numColsBics], plaidPattern, timeProfile);
				else
					bicK = new NumericBicluster<>(k, BicMath.getSet(bicsRows[k]), 
							BicMath.getSet(bicsCols[k]), rowType, columnType, new Double[numRowsBics], new Double[numColsBics], plaidPattern);

				
				/** PART VI: generate biclusters coherencies **/
				Double[][] bicsymbols = new Double[bicsRows[k].length][bicsCols[k].length];

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
						overlappLimits = getLimitsOnOverlappedArea(this.data.getElements(), bicsRows[k], bicsCols[k]);
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
				
				for (int row = 0; row < bicsRows[k].length; row++) {
					for (int col = 0; col < bicsCols[k].length; col++) {

						Double value = bicsymbols[row][col];

						if(data.getElements().contains(bicsRows[k][row] + ":" + bicsCols[k][col])) {

							switch (plaidPattern) {
							case ADDITIVE:
								value += data.getMatrixItem(bicsRows[k][row], bicsCols[k][col]).doubleValue();
								break;
							case MULTIPLICATIVE:
								value *= data.getMatrixItem(bicsRows[k][row], bicsCols[k][col]).doubleValue();
								break;
							case INTERPOLED:
								value = ((value + data.getMatrixItem(bicsRows[k][row], bicsCols[k][col]).doubleValue()) / 2);

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
							data.setMatrixItem(bicsRows[k][row], bicsCols[k][col], value);
						else
							data.setMatrixItem(bicsRows[k][row], bicsCols[k][col], new Integer(value.intValue()));

						if(Double.compare(value.doubleValue(), minAlphabet) < 0 || Double.compare(value.doubleValue(), maxAlphabet) > 0)
							throw new ExceedDatasetBoundsException("Exceeded dataset limits: Value = " + value.doubleValue());

						data.addElement(bicsRows[k][row] + ":" + bicsCols[k][col], k);
					}
				}
				data.addBicluster(bicK);
				
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
			}
		}
		return data;
	}
	
	private Pair<Double, Double> getLimitsOnOverlappedArea(Set<String> existingValues, int[] rows, int[] cols) {

		double min = this.data.getMaxM().doubleValue() + 1;
		double max = this.data.getMinM().doubleValue() - 1 ;

		
		for(Integer row : rows) {
			for(Integer col : cols) {
				if(existingValues.contains(row + ":" + col)) {
					double value = this.data.getMatrixItem(row, col).doubleValue();
					min = (Double.compare(value, min) < 0) ? value : min;
					max = (Double.compare(value, max) > 0) ? value : max;
				}
			}
		}
		return new Pair<>(min, max);
	}
}
