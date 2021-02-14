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
import com.gbic.utils.BiclusterPattern;
import com.gbic.utils.BiclusterStructure;

public class NumericDatasetGenerator extends BiclusterDatasetGenerator {

	private NumericDataset data;
	private Random random = new Random();
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
			
			BiclusterPattern currentPattern;

			if(numBics < patterns.size())
				currentPattern = patterns.get(random.nextInt(patterns.size()));
			else
				currentPattern = patterns.get(k % patterns.size());

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
						bicsExcluded, bicStructure.getContiguity().equals(Contiguity.COLUMNS));
				System.out.println("Columns: " + bicsCols[k].length);
			
				System.out.println("Bic " + (k+1) + " - Generating rows...");
				bicsRows[k] = generateRows(numRows, numRowsBics, overlappingRowsPerc, bicsRows, bicsWithOverlap,
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
				bicsCols[k] = generateNonOverlappingOthers(numColsBics, numCols, chosenCols, bicStructure.getContiguity().equals(Contiguity.COLUMNS));
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
					bicsymbols = generateAdditiveFactors(currentPattern, bicK, minAllowed, maxAllowed);
				}

				else if(currentPattern.contains(PatternType.MULTIPLICATIVE)) {

					bicsymbols = generateMultiplicativeFactors(currentPattern, bicK, minAllowed, maxAllowed);
				} 

				else if(currentPattern.contains(PatternType.ORDER_PRESERVING)) {
					bicsymbols = generateOrderPreserving(currentPattern, timeProfile, bicK, minAllowed, maxAllowed);


				}
				else if (currentPattern.contains(PatternType.CONSTANT)){
					bicsymbols = generateConstant(currentPattern, bicK, minAllowed, maxAllowed);
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

	private Double[][] generateMultiplicativeFactors(BiclusterPattern pattern, NumericBicluster<Double> bicK, double minAllowed,
			double maxAllowed) throws ExceedBiclusterBoundsException {

		Double[][] bicsymbols = new Double[bicK.getNumRows()][bicK.getNumCols()];

		PatternType rowType = pattern.getRowsPattern();
		PatternType columnType = pattern.getColumnsPattern();

		double seed;
		double maxContribution;
		double minContribution;

		seed = minAllowed + (maxAllowed - minAllowed) * random.nextDouble();

		if(!realValued) {
			if(Double.compare(Math.round(seed), minAllowed) >= 0 && 
					Double.compare(Math.round(seed), maxAllowed) <= 0)
				seed = Math.round(seed);
			else
				seed = (int) seed;
		}

		bicK.setSeed(seed);

		//System.out.println("Seed = " + seed);

		if(Double.compare(seed, 0.0) == 0) {
			minContribution = minAllowed;
			maxContribution = maxAllowed;
		}
		else {
			minContribution = minAllowed / seed;
			maxContribution = maxAllowed / seed;
		}


		//System.out.println("Row MinContribution = " + minContribution);
		//System.out.println("Row MaxContribution = " + maxContribution);

		double minRowCont = maxContribution;
		double maxRowCont = minContribution;

		double minColCont = maxContribution;
		double maxColCont = minContribution;

		if(rowType.equals(PatternType.MULTIPLICATIVE)) {
			for(int r = 0; r < bicK.getNumRows(); r++) {

				double factor = minContribution + (maxContribution - minContribution) * 
						random.nextDouble();

				if(!realValued) {
					if(Double.compare(Math.round(factor), minContribution) >= 0 && 
							Double.compare(Math.round(factor), maxContribution) <= 0)
						factor = Math.round(factor);
					else
						factor = (int) factor;
				}

				bicK.setRowFactor(r, factor);

				maxRowCont = bicK.getRowFactor(r) > maxRowCont ? bicK.getRowFactor(r) : maxRowCont;
				minRowCont = bicK.getRowFactor(r) < minRowCont ? bicK.getRowFactor(r) : minRowCont;
				//System.out.println("Row " + r + " = " + tricK.getRowFactor(r));
			}
		}
		else {
			for(int r = 0; r < bicK.getNumRows(); r++) {
				bicK.setRowFactor(r, 1.0);

				maxRowCont = 1.0;
				minRowCont = 1.0;
			}
		}

		if(columnType.equals(PatternType.MULTIPLICATIVE)) {

			if (minAllowed >= 0) {

				maxContribution = maxAllowed / (seed * maxRowCont);
				minContribution = minAllowed / (seed * minRowCont);
			}
			else if (minAllowed < 0 && maxAllowed >= 0){

				if (seed >= 0) {

					if(minRowCont >= 0 && maxRowCont >=0) {

						maxContribution = maxAllowed / (seed * maxRowCont);
						minContribution = minAllowed / (seed * maxRowCont);
					}
					else if(minRowCont < 0 && maxRowCont >= 0) {

						double int1Min = maxAllowed / (seed * minRowCont);
						double int1Max = minAllowed / (seed * minRowCont);

						double int2Min = minAllowed / (seed * maxRowCont);
						double int2Max = maxAllowed / (seed * maxRowCont);

						minContribution = (int1Min > int2Min) ? int1Min : int2Min;
						maxContribution = (int1Max < int2Max) ? int1Max : int2Max;
					}
					else {

						maxContribution = minAllowed / (seed * minRowCont);
						minContribution = maxAllowed / (seed * minRowCont);
					}
				}
				else {

					if(minRowCont >= 0 && maxRowCont >=0) {

						maxContribution = minAllowed / (seed * maxRowCont);
						minContribution = maxAllowed / (seed * maxRowCont);
					}
					else if(minRowCont < 0 && maxRowCont >= 0) {

						double int1Min = minAllowed / (seed * minRowCont);
						double int1Max = maxAllowed / (seed * minRowCont);

						double int2Min = maxAllowed / (seed * maxRowCont);
						double int2Max = minAllowed / (seed * maxRowCont);

						minContribution = (int1Min > int2Min) ? int1Min : int2Min;
						maxContribution = (int1Max < int2Max) ? int1Max : int2Max;
					}
					else {

						maxContribution = maxAllowed / (seed * minRowCont);
						minContribution = minAllowed / (seed * minRowCont);
					}
				}
			}
			else {
				//not implemented (negative dataset)
			}

			//System.out.println("Col MinContribution = " + minContribution);
			//System.out.println("Col MaxContribution = " + maxContribution);

			for(int c = 0; c < bicK.getNumCols(); c++) {

				double factor = minContribution + (maxContribution - minContribution) * 
						random.nextDouble();

				if(!realValued) {
					if(Double.compare(Math.round(factor), minContribution) >= 0 && 
							Double.compare(Math.round(factor), maxContribution) <= 0)
						factor = Math.round(factor);
					else
						factor = (int) factor;
				}

				bicK.setColumnFactor(c, factor);

				maxColCont = bicK.getColumnFactor(c) > maxColCont ? bicK.getColumnFactor(c) : maxColCont;
				minColCont = bicK.getColumnFactor(c) < minColCont ? bicK.getColumnFactor(c) : minColCont;
				//System.out.println("Col " + c + " = " + tricK.getColumnFactor(c));
			}
		}
		else {
			for(int c = 0; c < bicK.getNumCols(); c++)
				bicK.setColumnFactor(c, 1.0);

			maxColCont = 1.0;
			minColCont = 1.0;
		}

		//criar o bic
		
		for(int row = 0; row < bicK.getNumRows(); row++)
			for(int col = 0; col < bicK.getNumCols(); col++) {

				double value = seed * bicK.getRowFactor(row) * bicK.getColumnFactor(col);

				if(Double.compare(value,  minAllowed) < 0 && Double.compare(Math.abs(value - minAllowed), 0.2) <= 0)
					value = minAllowed;

				if(Double.compare(value,  maxAllowed) > 0 && Double.compare(Math.abs(value - maxAllowed), 0.2) <= 0)
					value = maxAllowed;

				bicsymbols[row][col] = value;

				if(!realValued)
					bicsymbols[row][col] = (double) bicsymbols[row][col].intValue();

				if (Double.compare(bicsymbols[row][col], minAllowed) < 0 || Double.compare(bicsymbols[row][col], maxAllowed) > 0) 
					throw new ExceedBiclusterBoundsException("Exceeded Bicluster limits: Value = " + bicsymbols[row][col]);

			}
		return bicsymbols;
	}

	private Double[][] generateAdditiveFactors(BiclusterPattern pattern, NumericBicluster<Double> bicK, double min, double max) throws ExceedBiclusterBoundsException {

		Double[][] bicsymbols = new Double[bicK.getNumRows()][bicK.getNumCols()];

		PatternType rowType = pattern.getRowsPattern();
		PatternType columnType = pattern.getColumnsPattern();

		double seed = min + (max - min) * random.nextDouble();

		if(!realValued) {
			if(Double.compare(Math.round(seed), min) >= 0 && Double.compare(Math.round(seed), max) <= 0)
				seed = Math.round(seed);
			else
				seed = (int) seed;
		}

		bicK.setSeed(seed);

		//System.out.println("Seed:" +  seed);

		double minContribution = 0;
		double maxContribution = 0;

		double minRowCont = 0;
		double maxRowCont = 0;
		double minColCont = 0;
		double maxColCont = 0;

		double factor;

		if(rowType.equals(PatternType.ADDITIVE)) {

			minContribution = min - seed;
			maxContribution = max - seed;

			minRowCont = maxContribution;
			maxRowCont = minContribution;
			minColCont = maxContribution;
			maxColCont = minContribution;

			for(int r = 0; r < bicK.getNumRows(); r++) {
				//System.out.println("*** Row " + r + " ***");
				//System.out.println("MinContribution: " + minContribution);
				//System.out.println("MaxContribution: " + maxContribution);
				factor = minContribution + (maxContribution - minContribution) * random.nextDouble();

				if(!realValued) {
					if(Double.compare(Math.round(factor), minContribution) >= 0 && Double.compare(Math.round(factor), maxContribution) <= 0)
						factor = Math.round(factor);
					else
						factor = (int) factor;
				}

				bicK.setRowFactor(r, factor);
				//System.out.println("Factor: " + factor);
				maxRowCont = bicK.getRowFactor(r) > maxRowCont ? bicK.getRowFactor(r) : maxRowCont;
				minRowCont = bicK.getRowFactor(r) < minRowCont ? bicK.getRowFactor(r) : minRowCont;
				//System.out.println("******");
			}
		}
		else {
			for(int r = 0; r < bicK.getNumRows(); r++)
				bicK.setRowFactor(r, 0.0);

			maxRowCont = 0.0;
			minRowCont = 0.0;
		}

		//System.out.println("\nMinRowCont:" + minRowCont);
		//System.out.println("MaxRowCont:" + maxRowCont + "\n");

		if(columnType.equals(PatternType.ADDITIVE)) {

			minContribution = min - (seed + minRowCont);
			maxContribution = max - (seed + maxRowCont);

			for(int c = 0; c < bicK.getNumCols(); c++) {

				//System.out.println("*** Column " + c + " ***");
				//System.out.println("MinContribution: " + minContribution);
				//System.out.println("MaxContribution: " + maxContribution);
				factor = minContribution + (maxContribution - minContribution) * random.nextDouble();

				if(!realValued) {
					if(Double.compare(Math.round(factor), minContribution) >= 0 && Double.compare(Math.round(factor), maxContribution) <= 0)
						factor = Math.round(factor);
					else
						factor = (int) factor;
				}

				bicK.setColumnFactor(c, factor);
				//System.out.println("Factor: " + factor);
				maxColCont = bicK.getColumnFactor(c) > maxColCont ? bicK.getColumnFactor(c) : maxColCont;
				minColCont = bicK.getColumnFactor(c) < minColCont ? bicK.getColumnFactor(c) : minColCont;
				//System.out.println("******");
			}

		}
		else {

			for(int c = 0; c < bicK.getNumCols(); c++)
				bicK.setColumnFactor(c, 0.0);

			maxColCont = 0.0;
			minColCont = 0.0;
		}

		//System.out.println("\nMinColCont:" + minColCont);
		//System.out.println("MaxColCont:" + maxColCont + "\n");

		//criar o bic
		
		for(int r = 0; r < bicK.getNumRows(); r++)
			for(int c = 0; c < bicK.getNumCols(); c++) {
				bicsymbols[r][c] = seed + bicK.getRowFactor(r) +  bicK.getColumnFactor(c);
				if(Double.compare(bicsymbols[r][c], min) < 0 || Double.compare(bicsymbols[r][c], max) > 0) 
					throw new ExceedBiclusterBoundsException("Exceeded Bicluster limits: Value = " + bicsymbols[r][c]);


			}

		return bicsymbols;
	}

	private Double[][] generateOrderPreserving(BiclusterPattern pattern, TimeProfile timeProfile, NumericBicluster<Double> bicK, double min, double max) {

		Double[][] bicsymbols = null;

		PatternType rowType = pattern.getRowsPattern();
		PatternType columnType = pattern.getColumnsPattern();
		
		if(rowType.equals(PatternType.ORDER_PRESERVING)) {

			bicsymbols = new Double[bicK.getNumCols()][bicK.getNumRows()];
			Integer[] order = generateOrder(bicK.getNumRows());
				
			
			for(int col = 0; col < bicK.getNumCols(); col++) {

				double minValue = min;
				double maxValue = max;

				for (int row = 0; row < bicK.getNumRows(); row++) {

					bicsymbols[col][row] = minValue + (maxValue - minValue) * random.nextDouble();

					if(!realValued) {
						if(Double.compare(Math.round(bicsymbols[col][row]), minValue) >= 0 && 
								Double.compare(Math.round(bicsymbols[col][row]), maxValue) <= 0)
							bicsymbols[col][row] = (double) Math.round(bicsymbols[col][row]);
						else
							bicsymbols[col][row] = (double) bicsymbols[col][row].intValue();
					}	
				}
				Arrays.parallelSort(bicsymbols[col]);
				bicsymbols[col] = shuffle(order, bicsymbols[col]);
			}
			
			bicsymbols = transposeMatrix(bicsymbols, "x", "y");
		}
		else if(columnType.equals(PatternType.ORDER_PRESERVING)) {

			bicsymbols = new Double[bicK.getNumRows()][bicK.getNumCols()];
			
			Integer[] order = null;
			if(timeProfile.equals(TimeProfile.RANDOM))
				order = generateOrder(bicK.getNumRows());
			
			
			for(int row = 0; row < bicK.getNumRows(); row++) {
				for(int col = 0; col < bicK.getNumCols(); col++) {
					

					double minValue = min;
					double maxValue = max;

					bicsymbols[row][col] = minValue + (maxValue - minValue) * random.nextDouble();

					if(!realValued) {
						if(Double.compare(Math.round(bicsymbols[row][col]), minValue) >= 0 && 
								Double.compare(Math.round(bicsymbols[row][col]), maxValue) <= 0)
							bicsymbols[row][col] = (double) Math.round(bicsymbols[row][col]);
						else
							bicsymbols[row][col] = (double) bicsymbols[row][col].intValue();
					}	
				}
				
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
		return bicsymbols;
	}

	private Integer[] generateOrder(int size) {
		Integer[] order = new Integer[size];
		for(int i = 0; i < size; i++)
			order[i] = i;
		Collections.shuffle(Arrays.asList(order));
		return order;
	}
	
	private Double[] shuffle(Integer[] order, Double[] array) {
		
		Double[] newArray = new Double[array.length];
		
		for(int i = 0; i < order.length; i++) {
			newArray[order[i]] = array[i];
		}
		
		return newArray;
	}
	
	private Double[][] generateConstant(BiclusterPattern pattern, NumericBicluster<Double> bicK, double min, double max){

		Double[][] bicsymbols = new Double[bicK.getNumRows()][bicK.getNumCols()];

		PatternType rowType = pattern.getRowsPattern();
		PatternType columnType = pattern.getColumnsPattern();

		double seed;
		double maxValue = max;
		double minValue = min;

		if(rowType.equals(PatternType.CONSTANT) && columnType.equals(PatternType.CONSTANT)) {

			//System.out.println("MaxValue = " + maxValue);
			//System.out.println("MinValue = " + minValue);

			seed = minValue + (maxValue - minValue) * random.nextDouble();

			if(!realValued) {
				if(Double.compare(Math.round(seed), minValue) >= 0 && Double.compare(Math.round(seed), maxValue) <= 0)
					seed = Math.round(seed);
				else
					seed = (int) seed;
			}	

			//System.out.println("Seed: " +  seed);

			
			for(int row = 0; row < bicK.getNumRows(); row++) 
				for (int col = 0; col < bicK.getNumCols(); col++)
					bicsymbols[row][col] = seed;	
									
			bicK.setSeed(bicsymbols);
		}
		else if(columnType.equals(PatternType.CONSTANT)) {
			
			for (int row = 0; row < bicK.getNumRows(); row++) {

				seed = minValue + (maxValue - minValue) * random.nextDouble();

				if(!realValued) {
					if(Double.compare(Math.round(seed), minValue) >= 0 && Double.compare(Math.round(seed), maxValue) <= 0)
						seed = Math.round(seed);
					else
						seed = (int) seed;
				}

				for(int col = 0; col < bicK.getNumCols(); col++)
					bicsymbols[row][col] = seed;
			}
			
		}
		else if(rowType.equals(PatternType.CONSTANT)) {
			for (int col = 0; col < bicK.getNumCols(); col++) {

				seed = minValue + (maxValue - minValue) * random.nextDouble();

				if(!realValued) {
					if(Double.compare(Math.round(seed), minValue) >= 0 && Double.compare(Math.round(seed), maxValue) <= 0)
						seed = Math.round(seed);
					else
						seed = (int) seed;
				}

				for(int row = 0; row < bicK.getNumRows(); row++)
					bicsymbols[row][col] = seed;
			}
		}
		else {

			for(int row = 0; row < bicK.getNumRows(); row++) {
				for (int col = 0; col < bicK.getNumCols(); col++) {
					seed = minValue + (maxValue - minValue) * random.nextDouble();

					if(!realValued) {
						if(Double.compare(Math.round(seed), minValue) >= 0 && Double.compare(Math.round(seed), maxValue) <= 0)
							seed = Math.round(seed);
						else
							seed = (int) seed;
					}

					bicsymbols[row][col] = seed;
				}
			}
			bicK.setSeed(bicsymbols);
		}

		return bicsymbols;
	}

	private Pair<Double, Double> getCombinedContributions(double minRowCont, double maxRowCont, double minColCont,
			double maxColCont){

		double[] conts = {minRowCont * minColCont, minRowCont * maxColCont,	maxRowCont * maxColCont,
				maxRowCont * minColCont};

		Arrays.parallelSort(conts);

		return new Pair<>(conts[0], conts[3]);
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
