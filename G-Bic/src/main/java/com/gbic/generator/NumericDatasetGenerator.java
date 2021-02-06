/**
 * NumericTriclusterDatasetGenerator Class
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
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.math3.util.Pair;

import com.gbic.domain.bicluster.NumericBicluster;
import com.gbic.domain.dataset.Dataset;
import com.gbic.domain.dataset.NumericDataset;
import com.gbic.domain.tricluster.NumericTricluster;
import com.gbic.exceptions.ExceedDatasetBoundsException;
import com.gbic.exceptions.ExceedTriclusterBoundsException;
import com.gbic.types.Background;
import com.gbic.types.Contiguity;
import com.gbic.types.PatternType;
import com.gbic.types.PlaidCoherency;
import com.gbic.types.TimeProfile;
import com.gbic.utils.BicMath;
import com.gbic.utils.OverlappingSettings;
import com.gbic.utils.TriclusterPattern;
import com.gbic.utils.TriclusterStructure;

public class NumericDatasetGenerator extends TriclusterDatasetGenerator {

	private NumericDataset data;
	private Random random = new Random();
	private boolean allowsOverlap = false;
	private boolean realValued;
	private int numTrics;

	/**
	 * Constructor
	 * @param realValued boolean that indicates if the dataset is real valued
	 * @param numRows the dataset's number of rows
	 * @param numCols the dataset's number of columns
	 * @param numContexts the dataset's number of contexts
	 * @param numTrics the number of trics to plant
	 * @param background the dataset's background
	 * @param minM the dataset's minimum alphabet value
	 * @param maxM the dataset's maximum alphabet value
	 */
	public NumericDatasetGenerator(boolean realValued, int numRows, int numCols, int numContexts, int numTrics, Background background, double minM, double maxM) {

		this.realValued = realValued;
		this.numTrics = numTrics;
		
		if(realValued)
			this.data = new NumericDataset<Double>(numRows, numCols, numContexts, numTrics, background,
					minM, maxM);
		else
			this.data = new NumericDataset<Integer>(numRows, numCols, numContexts, numTrics, background,
					(int) minM, (int) maxM);
	}

	@Override
	public Dataset generate(List<TriclusterPattern> patterns, TriclusterStructure tricStructure,
			OverlappingSettings overlapping) throws Exception {

		this.allowsOverlap = !overlapping.getPlaidCoherency().equals(PlaidCoherency.NO_OVERLAPPING);
		int maxTricsPerOverlappedArea = overlapping.getMaxTricsPerOverlappedArea();
		int overlappingThreshold = (int)(data.getNumTrics() * overlapping.getPercOfOverlappingTrics());
		PlaidCoherency plaidPattern = overlapping.getPlaidCoherency();

		//num rows/cols/ctx of the expression matrix
		int numRows = data.getNumRows();
		int numCols = data.getNumCols();
		int numConts = data.getNumContexts();

		//num of rows/cols/ctxs of a bic
		int numRowsTrics = 0;
		int numColsTrics = 0;
		int numContsTrics = 0;

		//Isto pode ser otimizado -> passar p/ dentro do for (1D em vez de 2D).
		int[][] bicsRows = new int[numTrics][];
		int[][] bicsCols = new int[numTrics][];
		int[][] bicsConts = new int[numTrics][];

		Set<Integer> chosenCols = new HashSet<>();
		Set<Integer> chosenConts = new HashSet<>();

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
		
		for (int k = 0; k < numTrics; k++) {

			boolean hasSpace = true;

			changeState("Stage:1, Msg:Tricluster " + k);
			
			if(k >= overlappingThreshold)
				allowsOverlap = false;

			System.out.println("Generating tricluster " + (k+1) + " of " + numTrics + "...");
			
			TriclusterPattern currentPattern;

			if(numTrics < patterns.size())
				currentPattern = patterns.get(random.nextInt(patterns.size()));
			else
				currentPattern = patterns.get(k % patterns.size());

			PatternType rowType = currentPattern.getRowsPattern();
			PatternType columnType = currentPattern.getColumnsPattern();
			PatternType contextType = currentPattern.getContextsPattern();
			TimeProfile timeProfile = currentPattern.getTimeProfile();

			Map<String, Integer> structure = generateTricStructure(tricStructure, numRows, numCols, numConts);

			numRowsTrics = structure.get("rows");
			numColsTrics = structure.get("columns");
			numContsTrics = structure.get("contexts");

			/** PART IV: select biclusters with (non-)overlapping elements **/
			int[] bicsWithOverlap = null;
			int[] bicsExcluded = null;

			if (this.allowsOverlap) {

				boolean dispersed = false;
				if (dispersed) {
					if ((k + 1) % maxTricsPerOverlappedArea == 0) {
						bicsWithOverlap = new int[Math.min(k, maxTricsPerOverlappedArea - 1)];
						for (int i = k - 1, j = 0; i >= 0 && j < maxTricsPerOverlappedArea - 1; i--, j++)
							bicsWithOverlap[j] = i;
					}
				} 
				else if (k % maxTricsPerOverlappedArea != 0)
					bicsWithOverlap = new int[] { k - 1 };

				int l = Math.max((k / maxTricsPerOverlappedArea) * maxTricsPerOverlappedArea, k-1);
				//int l = (k / maxTricsPerOverlappedArea) * maxTricsPerOverlappedArea;
				bicsExcluded = new int[l];
				for (int i = 0; i < l; i++)
					bicsExcluded[i] = i;
			}

			/** PART V: generate rows and columns using overlapping constraints **/
			//Se forem colunas contiguas nunca gera overlapping nas colunas?
			int tricSize = numContsTrics * numRowsTrics * numColsTrics;
			if (allowsOverlap) {
				
				Map<String, Double> overlappingPercs = generateOverlappingDistribution(tricSize, overlapping, numContsTrics, numRowsTrics, numColsTrics);

				double overlappingContsPerc = overlappingPercs.get("contextPerc");
				double overlappingColsPerc = overlappingPercs.get("columnPerc");
				double overlappingRowsPerc = overlappingPercs.get("rowPerc");
				
				System.out.println("Tric " + (k+1) + " - Generating contexts...");
				bicsConts[k] = generate(numContsTrics, numConts, overlappingContsPerc, bicsConts, bicsWithOverlap,
						bicsExcluded, tricStructure.getContiguity().equals(Contiguity.CONTEXTS));
				System.out.println("Contexts: " + bicsConts[k].length);
				
				System.out.println("Tric " + (k+1) + " - Generating columns...");
				bicsCols[k] = generate(numColsTrics, numCols, overlappingColsPerc, bicsCols, bicsWithOverlap,
						bicsExcluded, tricStructure.getContiguity().equals(Contiguity.COLUMNS));
				System.out.println("Columns: " + bicsCols[k].length);
			
				System.out.println("Tric " + (k+1) + " - Generating rows...");
				bicsRows[k] = generateRows(numRows, numRowsTrics, overlappingRowsPerc, bicsRows, bicsWithOverlap,
						bicsExcluded, bicsCols[k], bicsConts[k], data.getElements());
				System.out.println("Rows: " + bicsRows[k].length);
				
				if(bicsRows[k] == null) {
					hasSpace = false;
					k--;
					if(numAttempts == 15) {
						numTrics--;
						numAttempts = 0;
					}
					else
						numAttempts++;
				}
				else
					numAttempts = 0;
			}
			else {
				
				System.out.println("Tric " + (k+1) + " - Generating contexts...");
				bicsConts[k] = generateNonOverlappingOthers(numContsTrics, numConts, chosenConts, tricStructure.getContiguity().equals(Contiguity.CONTEXTS));
				System.out.println("Contexts: " + bicsConts[k].length);
				
				System.out.println("Tric " + (k+1) + " - Generating columns...");
				bicsCols[k] = generateNonOverlappingOthers(numColsTrics, numCols, chosenCols, tricStructure.getContiguity().equals(Contiguity.COLUMNS));
				System.out.println("Columns: " + bicsCols[k].length);
				
				System.out.println("Tric " + (k+1) + " - Generating rows...");
				bicsRows[k] = generateNonOverlappingRows(numRowsTrics, numRows, bicsCols[k], bicsConts[k], data.getElements());
				System.out.println("Rows: " + bicsRows[k].length);

				if(bicsRows[k] == null) {
					hasSpace = false;
					System.out.println("Tric " + (k+1) + " no space :(");
					k--;
					if(numAttempts == 15) {
						numTrics--;
						numAttempts = 0;
					}
					else
						numAttempts++;
				}
				else
					numAttempts = 0;
			}

			if(hasSpace) {
				
				System.out.println("Tric " + (k+1) + " - Has space, lets plant the patterns");
				
				for (Integer c : bicsCols[k])
					chosenCols.add(c);
				for (Integer c : bicsConts[k])
					chosenConts.add(c);

				Arrays.parallelSort(bicsRows[k]);
				Arrays.parallelSort(bicsCols[k]);
				Arrays.parallelSort(bicsConts[k]);

				NumericBicluster<Double> bicK = new NumericBicluster<>(BicMath.getSet(bicsRows[k]), 
						BicMath.getSet(bicsCols[k]), rowType, columnType, new Double[numRowsTrics], new Double[numColsTrics]);

				NumericTricluster<Double> tricK;
				
				if(contextType.equals(PatternType.ORDER_PRESERVING))
					tricK = new NumericTricluster<>(k, bicK, contextType, timeProfile, plaidPattern, bicsConts[k]);
				else
					tricK = new NumericTricluster<>(k, bicK, contextType, plaidPattern, bicsConts[k]);

				/** PART VI: generate biclusters coherencies **/
				Double[][][] bicsymbols = new Double[bicsConts[k].length][bicsRows[k].length][bicsCols[k].length];

				double maxAlphabet = realValued ? data.getMaxM().doubleValue() : data.getMaxM().intValue();
				double minAlphabet = realValued ? data.getMinM().doubleValue() : data.getMinM().intValue();
				double maxAllowed = maxAlphabet;
				double minAllowed = minAlphabet;
				double lowerBound = maxAlphabet;

				Pair<Double, Double> overlappLimits = null;

				//expression and biclusters symbols

				if(this.allowsOverlap) {
					//When (k % nrOverlappingBics == 0), we are on the first tricluster that will be overlapped, so, we
					//must reset maxOverlapp and minOverlapp.
					if (k % maxTricsPerOverlappedArea == 0) {
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
					//this new tricluster.
					else {
						overlappLimits = getLimitsOnOverlappedArea(this.data.getElements(), bicsRows[k], bicsCols[k],
								bicsConts[k]);
					}


					if(plaidPattern.equals(PlaidCoherency.ADDITIVE)) {

						if(k % maxTricsPerOverlappedArea != 0) {

							double first = overlappLimits.getFirst();
							double second = overlappLimits.getSecond();

							if(Double.compare(first, maxAlphabet) < 0 || Double.compare(second, minAlphabet) > 0) {
								maxOverlapp += (Double.compare(second, 0) > 0) ? second : 0;
								minOverlapp += (Double.compare(first, 0) < 0) ? first : 0;
							}
						}

						if(Double.compare(maxAlphabet, 0) > 0)
							maxAllowed = (maxAlphabet - maxOverlapp) / (maxTricsPerOverlappedArea - (k % maxTricsPerOverlappedArea));

						if(Double.compare(minAlphabet, 0) < 0)
							minAllowed = (minAlphabet - minOverlapp) / (maxTricsPerOverlappedArea - (k % maxTricsPerOverlappedArea));
					}
					else if(plaidPattern.equals(PlaidCoherency.MULTIPLICATIVE)) {

						if (k % maxTricsPerOverlappedArea != 0) {
							double first = overlappLimits.getFirst();
							double second = overlappLimits.getSecond();

							if(Double.compare(first, maxAlphabet) < 0 || Double.compare(second, minAlphabet) > 0) {
								maxOverlapp = Math.max(Math.abs(first), Math.abs(second));
								maxOverlapp *= (Double.compare(maxOverlapp, 0.0) == 0) ? 1 : maxOverlapp;
							}
						}

						//when the alphabet is strictly positive
						if(Double.compare(minAlphabet, 0) >= 0)
							maxAllowed = BicMath.nthRoot(maxAlphabet / maxOverlapp, (maxTricsPerOverlappedArea - (k % maxTricsPerOverlappedArea)));
						else { 
							lowerBound = Math.min(Math.abs(minAlphabet), Math.abs(maxAlphabet));
							maxAllowed = BicMath.nthRoot(lowerBound / maxOverlapp, (maxTricsPerOverlappedArea - (k % maxTricsPerOverlappedArea)));

							if(Double.compare(maxAllowed, lowerBound) > 0)
								maxAllowed = lowerBound;

							minAllowed = - maxAllowed;
						}
					}
				}

				if(currentPattern.contains(PatternType.ADDITIVE)) {				
					bicsymbols = generateAdditiveFactors(currentPattern, tricK, minAllowed, maxAllowed);
				}

				else if(currentPattern.contains(PatternType.MULTIPLICATIVE)) {

					bicsymbols = generateMultiplicativeFactors(currentPattern, tricK, minAllowed, maxAllowed);
				} 

				else if(currentPattern.contains(PatternType.ORDER_PRESERVING)) {
					bicsymbols = generateOrderPreserving(currentPattern, timeProfile, tricK, minAllowed, maxAllowed);


				}
				else if (currentPattern.contains(PatternType.CONSTANT)){
					bicsymbols = generateConstant(currentPattern, tricK, minAllowed, maxAllowed);
				}

				
				/**
				 * Part VII: generate the layers according to plaid type and put them in the
				 * background
				 **/
				System.out.println("Tric " + (k+1) + " - planting the tric");
				for(int ctx = 0; ctx < bicsConts[k].length; ctx++) 
					for (int row = 0; row < bicsRows[k].length; row++) {
						
						if(row % 10000 == 0)
							System.out.println("Planting on ctx " + ctx + " row " + row);
				
						for (int col = 0; col < bicsCols[k].length; col++) {

							Double value = bicsymbols[ctx][row][col];

							if(data.getElements().contains(bicsConts[k][ctx] + ":" + bicsRows[k][row] + ":" + bicsCols[k][col])) {

								switch (plaidPattern) {
								case ADDITIVE:
									value += data.getMatrixItem(bicsConts[k][ctx], bicsRows[k][row], bicsCols[k][col]).doubleValue();
									break;
								case MULTIPLICATIVE:
									value *= data.getMatrixItem(bicsConts[k][ctx], bicsRows[k][row], bicsCols[k][col]).doubleValue();
									break;
								case INTERPOLED:
									value = ((value + data.getMatrixItem(bicsConts[k][ctx], bicsRows[k][row], bicsCols[k][col]).doubleValue()) / 2);

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
								data.setMatrixItem(bicsConts[k][ctx], bicsRows[k][row], bicsCols[k][col], value);
							else
								data.setMatrixItem(bicsConts[k][ctx], bicsRows[k][row], bicsCols[k][col], new Integer(value.intValue()));

							if(Double.compare(value.doubleValue(), minAlphabet) < 0 || Double.compare(value.doubleValue(), maxAlphabet) > 0)
								throw new ExceedDatasetBoundsException("Exceeded dataset limits: Value = " + value.doubleValue());

							data.addElement(bicsConts[k][ctx] + ":" + bicsRows[k][row] + ":" + bicsCols[k][col], k);
						}
					}
				data.addTricluster(tricK);
				
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

	private Double[][][] generateMultiplicativeFactors(TriclusterPattern pattern, NumericTricluster<Double> tricK, double minAllowed,
			double maxAllowed) throws ExceedTriclusterBoundsException {

		Double[][][] bicsymbols = new Double[tricK.getNumContexts()][tricK.getNumRows()][tricK.getNumCols()];

		PatternType rowType = pattern.getRowsPattern();
		PatternType columnType = pattern.getColumnsPattern();
		PatternType contextType = pattern.getContextsPattern();

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

		tricK.setSeed(seed);

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
			for(int r = 0; r < tricK.getNumRows(); r++) {

				double factor = minContribution + (maxContribution - minContribution) * 
						random.nextDouble();

				if(!realValued) {
					if(Double.compare(Math.round(factor), minContribution) >= 0 && 
							Double.compare(Math.round(factor), maxContribution) <= 0)
						factor = Math.round(factor);
					else
						factor = (int) factor;
				}

				tricK.setRowFactor(r, factor);

				maxRowCont = tricK.getRowFactor(r) > maxRowCont ? tricK.getRowFactor(r) : maxRowCont;
				minRowCont = tricK.getRowFactor(r) < minRowCont ? tricK.getRowFactor(r) : minRowCont;
				//System.out.println("Row " + r + " = " + tricK.getRowFactor(r));
			}
		}
		else {
			for(int r = 0; r < tricK.getNumRows(); r++) {
				tricK.setRowFactor(r, 1.0);

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

			for(int c = 0; c < tricK.getNumCols(); c++) {

				double factor = minContribution + (maxContribution - minContribution) * 
						random.nextDouble();

				if(!realValued) {
					if(Double.compare(Math.round(factor), minContribution) >= 0 && 
							Double.compare(Math.round(factor), maxContribution) <= 0)
						factor = Math.round(factor);
					else
						factor = (int) factor;
				}

				tricK.setColumnFactor(c, factor);

				maxColCont = tricK.getColumnFactor(c) > maxColCont ? tricK.getColumnFactor(c) : maxColCont;
				minColCont = tricK.getColumnFactor(c) < minColCont ? tricK.getColumnFactor(c) : minColCont;
				//System.out.println("Col " + c + " = " + tricK.getColumnFactor(c));
			}
		}
		else {
			for(int c = 0; c < tricK.getNumCols(); c++)
				tricK.setColumnFactor(c, 1.0);

			maxColCont = 1.0;
			minColCont = 1.0;
		}

		if(contextType.equals(PatternType.MULTIPLICATIVE)) {

			if (minAllowed >= 0) {
				maxContribution = maxAllowed / (seed * maxRowCont * maxColCont);
				minContribution = minAllowed / (seed * minRowCont * minColCont);
			}
			else if (minAllowed < 0 && maxAllowed >= 0){

				Pair<Double, Double> combined = getCombinedContributions(minRowCont, maxRowCont, minColCont, 
						maxColCont);

				double minCombined = combined.getFirst();
				double maxCombined = combined.getSecond();

				if (seed >= 0) {
					if(minCombined >= 0 && maxCombined >=0) {
						maxContribution = maxAllowed / (seed * maxCombined);
						minContribution = minAllowed / (seed * maxCombined);
					}
					else if(minCombined < 0 && maxCombined >= 0) {
						double int1Min = maxAllowed / (seed * minCombined);
						double int1Max = minAllowed / (seed * minCombined);

						double int2Min = minAllowed / (seed * maxCombined);
						double int2Max = maxAllowed / (seed * maxCombined);

						minContribution = (int1Min > int2Min) ? int1Min : int2Min;
						maxContribution = (int1Max < int2Max) ? int1Max : int2Max;
					}
					else {
						maxContribution = minAllowed / (seed * minCombined);
						minContribution = maxAllowed / (seed * minCombined);
					}
				}
				else {
					if(minCombined >= 0 && maxCombined >=0) {
						maxContribution = minAllowed / (seed * maxCombined);
						minContribution = maxAllowed / (seed * maxCombined);
					}
					else if(minCombined < 0 && maxCombined >= 0) {
						double int1Min = minAllowed / (seed * minCombined);
						double int1Max = maxAllowed / (seed * minCombined);

						double int2Min = maxAllowed / (seed * maxCombined);
						double int2Max = minAllowed / (seed * maxCombined);

						minContribution = (int1Min > int2Min) ? int1Min : int2Min;
						maxContribution = (int1Max < int2Max) ? int1Max : int2Max;
					}
					else {
						maxContribution = maxAllowed / (seed * minCombined);
						minContribution = minAllowed / (seed * minCombined);
					}
				}
			}
			else {
				//not implemented (negative dataset)
			}

			//System.out.println("Ctx MinContribution = " + minContribution);
			//System.out.println("Ctx MaxContribution = " + maxContribution);

			for(int ctx = 0; ctx < tricK.getNumContexts(); ctx++) {

				double factor = minContribution + (maxContribution - minContribution) * 
						random.nextDouble();

				if(!realValued) {
					if(Double.compare(Math.round(factor), minContribution) >= 0 && 
							Double.compare(Math.round(factor), maxContribution) <= 0)
						factor = Math.round(factor);
					else
						factor = (int) factor;
				}
				tricK.setContextFactor(ctx, factor);
				//System.out.println("Ctx " + ctx + " = " + tricK.getContextFactor(ctx));
			}
		}
		else 
			for(int ctx = 0; ctx < tricK.getNumContexts(); ctx++)
				tricK.setContextFactor(ctx, 1.0);



		//criar o bic
		for(int ctx = 0; ctx < tricK.getNumContexts(); ctx++)
			for(int row = 0; row < tricK.getNumRows(); row++)
				for(int col = 0; col < tricK.getNumCols(); col++) {

					double value = seed * tricK.getRowFactor(row) * tricK.getColumnFactor(col) * 
							tricK.getContextFactor(ctx);

					if(Double.compare(value,  minAllowed) < 0 && Double.compare(Math.abs(value - minAllowed), 0.2) <= 0)
						value = minAllowed;

					if(Double.compare(value,  maxAllowed) > 0 && Double.compare(Math.abs(value - maxAllowed), 0.2) <= 0)
						value = maxAllowed;

					bicsymbols[ctx][row][col] = value;

					if(!realValued)
						bicsymbols[ctx][row][col] = (double) bicsymbols[ctx][row][col].intValue();

					if (Double.compare(bicsymbols[ctx][row][col], minAllowed) < 0 || Double.compare(bicsymbols[ctx][row][col], maxAllowed) > 0) 
						throw new ExceedTriclusterBoundsException("Exceeded Tricluster limits: Value = " + bicsymbols[ctx][row][col]);

				}
		return bicsymbols;
	}

	private Double[][][] generateAdditiveFactors(TriclusterPattern pattern, NumericTricluster<Double> tricK, double min, double max) throws ExceedTriclusterBoundsException {

		Double[][][] bicsymbols = new Double[tricK.getNumContexts()][tricK.getNumRows()][tricK.getNumCols()];

		PatternType rowType = pattern.getRowsPattern();
		PatternType columnType = pattern.getColumnsPattern();
		PatternType contextType = pattern.getContextsPattern();

		double seed = min + (max - min) * random.nextDouble();

		if(!realValued) {
			if(Double.compare(Math.round(seed), min) >= 0 && Double.compare(Math.round(seed), max) <= 0)
				seed = Math.round(seed);
			else
				seed = (int) seed;
		}

		tricK.setSeed(seed);

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

			for(int r = 0; r < tricK.getNumRows(); r++) {
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

				tricK.setRowFactor(r, factor);
				//System.out.println("Factor: " + factor);
				maxRowCont = tricK.getRowFactor(r) > maxRowCont ? tricK.getRowFactor(r) : maxRowCont;
				minRowCont = tricK.getRowFactor(r) < minRowCont ? tricK.getRowFactor(r) : minRowCont;
				//System.out.println("******");
			}
		}
		else {
			for(int r = 0; r < tricK.getNumRows(); r++)
				tricK.setRowFactor(r, 0.0);

			maxRowCont = 0.0;
			minRowCont = 0.0;
		}

		//System.out.println("\nMinRowCont:" + minRowCont);
		//System.out.println("MaxRowCont:" + maxRowCont + "\n");

		if(columnType.equals(PatternType.ADDITIVE)) {

			minContribution = min - (seed + minRowCont);
			maxContribution = max - (seed + maxRowCont);

			for(int c = 0; c < tricK.getNumCols(); c++) {

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

				tricK.setColumnFactor(c, factor);
				//System.out.println("Factor: " + factor);
				maxColCont = tricK.getColumnFactor(c) > maxColCont ? tricK.getColumnFactor(c) : maxColCont;
				minColCont = tricK.getColumnFactor(c) < minColCont ? tricK.getColumnFactor(c) : minColCont;
				//System.out.println("******");
			}

		}
		else {

			for(int c = 0; c < tricK.getNumCols(); c++)
				tricK.setColumnFactor(c, 0.0);

			maxColCont = 0.0;
			minColCont = 0.0;
		}

		//System.out.println("\nMinColCont:" + minColCont);
		//System.out.println("MaxColCont:" + maxColCont + "\n");

		if(contextType.equals(PatternType.ADDITIVE)) {

			minContribution = min - (seed + minRowCont + minColCont);
			maxContribution = max - (seed + maxRowCont + maxColCont);

			for(int ctx = 0; ctx < tricK.getNumContexts(); ctx++) {
				//System.out.println("*** Context " + ctx + " ***");
				//System.out.println("MinContribution: " + minContribution);
				//System.out.println("MaxContribution: " + maxContribution);
				factor = minContribution + (maxContribution - minContribution) * random.nextDouble();

				if(!realValued) {
					if(Double.compare(Math.round(factor), minContribution) >= 0 && Double.compare(Math.round(factor), maxContribution) <= 0)
						factor = Math.round(factor);
					else
						factor = (int) factor;
				}

				tricK.setContextFactor(ctx, factor);
				//System.out.println("Factor: " + factor);
				//System.out.println("******");
			}		
		}
		else {
			for(int ctx = 0; ctx < tricK.getNumContexts(); ctx++)
				tricK.setContextFactor(ctx, 0.0);
		}

		//criar o bic
		for(int ctx = 0; ctx < tricK.getNumContexts(); ctx++)
			for(int r = 0; r < tricK.getNumRows(); r++)
				for(int c = 0; c < tricK.getNumCols(); c++) {
					bicsymbols[ctx][r][c] = seed + tricK.getRowFactor(r) +  tricK.getColumnFactor(c) + 
							tricK.getContextFactor(ctx);
					if(Double.compare(bicsymbols[ctx][r][c], min) < 0 || Double.compare(bicsymbols[ctx][r][c], max) > 0) 
						throw new ExceedTriclusterBoundsException("Exceeded Tricluster limits: Value = " + bicsymbols[ctx][r][c]);


				}

		return bicsymbols;
	}

	private Double[][][] generateOrderPreserving(TriclusterPattern pattern, TimeProfile timeProfile, NumericTricluster<Double> tricK, double min, double max) {

		Double[][][] bicsymbols = null;

		PatternType rowType = pattern.getRowsPattern();
		PatternType columnType = pattern.getColumnsPattern();
		PatternType contextType = pattern.getContextsPattern();
		
		if(rowType.equals(PatternType.ORDER_PRESERVING)) {

			bicsymbols = new Double[tricK.getNumContexts()][tricK.getNumCols()][tricK.getNumRows()];
			Integer[] order = generateOrder(tricK.getNumRows());
				
			for(int ctx = 0; ctx < tricK.getNumContexts(); ctx++) {
				for(int col = 0; col < tricK.getNumCols(); col++) {

					double minValue = min;
					double maxValue = max;

					for (int row = 0; row < tricK.getNumRows(); row++) {

						bicsymbols[ctx][col][row] = minValue + (maxValue - minValue) * random.nextDouble();

						if(!realValued) {
							if(Double.compare(Math.round(bicsymbols[ctx][col][row]), minValue) >= 0 && 
									Double.compare(Math.round(bicsymbols[ctx][col][row]), maxValue) <= 0)
								bicsymbols[ctx][col][row] = (double) Math.round(bicsymbols[ctx][col][row]);
							else
								bicsymbols[ctx][col][row] = (double) bicsymbols[ctx][col][row].intValue();
						}	
					}
					Arrays.parallelSort(bicsymbols[ctx][col]);
					bicsymbols[ctx][col] = shuffle(order, bicsymbols[ctx][col]);
				}
			}
			bicsymbols = transposeMatrix(bicsymbols, "x", "y");
		}
		else if(columnType.equals(PatternType.ORDER_PRESERVING)) {

			bicsymbols = new Double[tricK.getNumContexts()][tricK.getNumRows()][tricK.getNumCols()];
			Integer[] order = generateOrder(tricK.getNumCols());
			
			for(int ctx = 0; ctx < tricK.getNumContexts(); ctx++) {
				for(int row = 0; row < tricK.getNumRows(); row++) {

					double minValue = min;
					double maxValue = max;

					for (int col = 0; col < tricK.getNumCols(); col++) {

						bicsymbols[ctx][row][col] = minValue + (maxValue - minValue) * random.nextDouble();

						if(!realValued) {
							if(Double.compare(Math.round(bicsymbols[ctx][row][col]), minValue) >= 0 && 
									Double.compare(Math.round(bicsymbols[ctx][row][col]), maxValue) <= 0)
								bicsymbols[ctx][row][col] = (double) Math.round(bicsymbols[ctx][row][col]);
							else
								bicsymbols[ctx][row][col] = (double) bicsymbols[ctx][row][col].intValue();
						}	
					}
					Arrays.parallelSort(bicsymbols[ctx][row]);
					bicsymbols[ctx][row] = shuffle(order, bicsymbols[ctx][row]);
				}
			}
		}
		else if(contextType.equals(PatternType.ORDER_PRESERVING)) {

			bicsymbols = new Double[tricK.getNumCols()][tricK.getNumRows()][tricK.getNumContexts()];
			
			Integer[] order = null;
			if(timeProfile.equals(TimeProfile.RANDOM))
				order = generateOrder(tricK.getNumContexts());
			
			
			for(int col = 0; col < tricK.getNumCols(); col++) {
				for(int row = 0; row < tricK.getNumRows(); row++) {
					for (int ctx = 0; ctx < tricK.getNumContexts(); ctx++) {

						double minValue = min;
						double maxValue = max;

						bicsymbols[col][row][ctx] = minValue + (maxValue - minValue) * random.nextDouble();

						if(!realValued) {
							if(Double.compare(Math.round(bicsymbols[col][row][ctx]), minValue) >= 0 && 
									Double.compare(Math.round(bicsymbols[col][row][ctx]), maxValue) <= 0)
								bicsymbols[col][row][ctx] = (double) Math.round(bicsymbols[col][row][ctx]);
							else
								bicsymbols[col][row][ctx] = (double) bicsymbols[col][row][ctx].intValue();
						}	
					}
					
					if(timeProfile.equals(TimeProfile.RANDOM)) {
						Arrays.parallelSort(bicsymbols[col][row]);
						bicsymbols[col][row] = shuffle(order, bicsymbols[col][row]);
					}
					else if(timeProfile.equals(TimeProfile.MONONICALLY_INCREASING))
						Arrays.sort(bicsymbols[col][row]);
					
					else
						Arrays.sort(bicsymbols[col][row], Collections.reverseOrder());
					
				}
			}
			bicsymbols = transposeMatrix(bicsymbols, "z", "y");
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
	
	private Double[][][] generateConstant(TriclusterPattern pattern, NumericTricluster<Double> tricK, double min, double max){

		Double[][][] bicsymbols = new Double[tricK.getNumContexts()][tricK.getNumRows()][tricK.getNumCols()];

		PatternType rowType = pattern.getRowsPattern();
		PatternType columnType = pattern.getColumnsPattern();
		PatternType contextType = pattern.getContextsPattern();

		double seed;
		double maxValue = max;
		double minValue = min;

		if(rowType.equals(PatternType.CONSTANT) && columnType.equals(PatternType.CONSTANT) && 
				(contextType.equals(PatternType.CONSTANT))) {

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

			for(int ctx = 0; ctx < tricK.getNumContexts(); ctx++) {
				for(int row = 0; row < tricK.getNumRows(); row++) 
					for (int col = 0; col < tricK.getNumCols(); col++)
						bicsymbols[ctx][row][col] = seed;	
			}						
			tricK.setSeed(bicsymbols[0]);
		}
		else if(rowType.equals(PatternType.CONSTANT) && columnType.equals(PatternType.CONSTANT)) {

			for(int ctx = 0; ctx < tricK.getNumContexts(); ctx++) {

				seed = minValue + (maxValue - minValue) * random.nextDouble();

				if(!realValued) {
					if(Double.compare(Math.round(seed), minValue) >= 0 && Double.compare(Math.round(seed), maxValue) <= 0)
						seed = Math.round(seed);
					else
						seed = (int) seed;
				}

				for(int row = 0; row < tricK.getNumRows(); row++) {
					for (int col = 0; col < tricK.getNumCols(); col++)
						bicsymbols[ctx][row][col] = seed;
				}
				tricK.setContextPattern(ctx, bicsymbols[ctx]);
			}
		}
		else if(columnType.equals(PatternType.CONSTANT) && contextType.equals(PatternType.CONSTANT)) {

			for(int row = 0; row < tricK.getNumRows(); row++) {

				seed = minValue + (maxValue - minValue) * random.nextDouble();

				if(!realValued) {
					if(Double.compare(Math.round(seed), minValue) >= 0 && Double.compare(Math.round(seed), maxValue) <= 0)
						seed = Math.round(seed);
					else
						seed = (int) seed;
				}

				for(int ctx = 0; ctx < tricK.getNumContexts(); ctx++)
					for (int col = 0; col < tricK.getNumCols(); col++)
						bicsymbols[ctx][row][col] = seed;
			}

			tricK.setSeed(bicsymbols[0]);

		}
		else if(rowType.equals(PatternType.CONSTANT) && contextType.equals(PatternType.CONSTANT)) {

			for(int col = 0; col < tricK.getNumCols(); col++) {

				seed = minValue + (maxValue - minValue) * random.nextDouble();

				if(!realValued) {
					if(Double.compare(Math.round(seed), minValue) >= 0 && Double.compare(Math.round(seed), maxValue) <= 0)
						seed = Math.round(seed);
					else
						seed = (int) seed;
				}

				for(int ctx = 0; ctx < tricK.getNumContexts(); ctx++)
					for (int row = 0; row < tricK.getNumRows(); row++)
						bicsymbols[ctx][row][col] = seed;
			}
			tricK.setSeed(bicsymbols[0]);
		}
		else if(columnType.equals(PatternType.CONSTANT)) {
			for(int ctx = 0; ctx < tricK.getNumContexts(); ctx++) {
				for (int row = 0; row < tricK.getNumRows(); row++) {

					seed = minValue + (maxValue - minValue) * random.nextDouble();

					if(!realValued) {
						if(Double.compare(Math.round(seed), minValue) >= 0 && Double.compare(Math.round(seed), maxValue) <= 0)
							seed = Math.round(seed);
						else
							seed = (int) seed;
					}

					for(int col = 0; col < tricK.getNumCols(); col++)
						bicsymbols[ctx][row][col] = seed;
				}
				tricK.setContextPattern(ctx, bicsymbols[ctx]);
			}
		}
		else if(rowType.equals(PatternType.CONSTANT)) {
			for(int ctx = 0; ctx < tricK.getNumContexts(); ctx++) {
				for (int col = 0; col < tricK.getNumCols(); col++) {

					seed = minValue + (maxValue - minValue) * random.nextDouble();

					if(!realValued) {
						if(Double.compare(Math.round(seed), minValue) >= 0 && Double.compare(Math.round(seed), maxValue) <= 0)
							seed = Math.round(seed);
						else
							seed = (int) seed;
					}

					for(int row = 0; row < tricK.getNumRows(); row++)
						bicsymbols[ctx][row][col] = seed;
				}
				tricK.setContextPattern(ctx, bicsymbols[ctx]);
			}
		}
		else {

			for(int row = 0; row < tricK.getNumRows(); row++) {
				for (int col = 0; col < tricK.getNumCols(); col++) {
					seed = minValue + (maxValue - minValue) * random.nextDouble();

					if(!realValued) {
						if(Double.compare(Math.round(seed), minValue) >= 0 && Double.compare(Math.round(seed), maxValue) <= 0)
							seed = Math.round(seed);
						else
							seed = (int) seed;
					}

					bicsymbols[0][row][col] = seed;
				}
			}

			tricK.setSeed(bicsymbols[0]);

			for(int ctx = 1; ctx < tricK.getNumContexts(); ctx++) {
				bicsymbols[ctx] = bicsymbols[0];
			}
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

	private Pair<Double, Double> getLimitsOnOverlappedArea(Set<String> existingValues, int[] rows, int[] cols, int[] ctxs) {

		double min = this.data.getMaxM().doubleValue() + 1;
		double max = this.data.getMinM().doubleValue() - 1 ;

		for(Integer ctx : ctxs) {
			for(Integer row : rows) {
				for(Integer col : cols) {
					if(existingValues.contains(ctx + ":" + row + ":" + col)) {
						double value = this.data.getMatrixItem(ctx, row, col).doubleValue();
						min = (Double.compare(value, min) < 0) ? value : min;
						max = (Double.compare(value, max) > 0) ? value : max;
					}
				}
			}
		}
		return new Pair<>(min, max);
	}
}
