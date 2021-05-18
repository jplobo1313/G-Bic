/**
 * BiclusterDatasetGenerator Class
 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @version 1.0
 */
package com.gbic.generator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.math3.util.Pair;

import com.gbic.domain.bicluster.NumericBicluster;
import com.gbic.domain.dataset.Dataset;
import com.gbic.exceptions.ExceedBiclusterBoundsException;
import com.gbic.exceptions.OutputErrorException;
import com.gbic.types.Distribution;
import com.gbic.types.PatternType;
import com.gbic.types.TimeProfile;
import com.gbic.utils.OverlappingSettings;
import com.gbic.utils.SingleBiclusterPattern;
import com.gbic.utils.RandomObject;
import com.gbic.utils.BiclusterPattern;
import com.gbic.utils.BiclusterStructure;

public abstract class BiclusterDatasetGenerator extends Observable {

	private String path;
	private String datasetFileName;
	private String bicsInfoFileName;
	private String statsFileName;
	
	Random random = RandomObject.getInstance();
	
	/**
	 * Generate a dataset with planted biclusters
	 * @param patterns The list of patterns for each bicluster
	 * @param bicStructure The information about the bicluster's structure
	 * @param overlapping The information about overlapping properties
	 * @return The generated dataset
	 * @throws Exception
	 */
	public abstract Dataset generate(List<BiclusterPattern> patterns, BiclusterStructure bicStructure,
			OverlappingSettings overlapping) throws Exception;

	protected int[] generateRows(int bicSize, int dimSize, double percOverlap, int[][] bicsRows,
			int[] bicsWithOverlap, int[] bicsExcluded, int[] bicCols, Set<String> elements) throws Exception {
		
		//guardar rows escolhidas
		int[] result = new int[bicSize];
		//guardar rows escolhidas (same, redudante talvez)
		SortedSet<Integer> set = new TreeSet<>();
		boolean noSpace = false;
		//Se nao existir overlapping
		if (Double.compare(percOverlap,0) <= 0) { // no need for plaid calculus
			for (int i = 0, val = -1; i < bicSize; i++) {
				//guardar rows testadas
				SortedSet<Integer> testedRows = new TreeSet<>();

				boolean alreadyExists = false;
				boolean invalidOverlap = false;
				boolean notExhaustedDim = false;
				do {
					val = random.nextInt(dimSize);
					testedRows.add(val);
					
					//condicoes para garantir que a row eh valida
					alreadyExists = set.contains(val);
					invalidOverlap = isOverlap(val, bicCols, elements);
					notExhaustedDim = testedRows.size() < dimSize;
				} while (alreadyExists || (invalidOverlap && notExhaustedDim));
				
				//verificar se a ultima row testada eh ou nao valida
				if(i < bicSize && !alreadyExists && !invalidOverlap) {
					set.add(val);
					result[i] = val;
				}
				else {
					//throw new Exception("Not able to meet the non-overlapping row criteria for the generate sets of columns and contexts!\nSuggestions: "
					//		+ "increase the matrix size OR decrease the size of trics!");
					noSpace = true;
				}
			}
		}
		else {
			//Primeira tentativa - aproveitar ao maximo rows livres
			SortedSet<Integer> setExc = new TreeSet<>();
			for (int i = 0; i < bicsExcluded.length; i++)
				for (int j = 0; j < bicsRows[bicsExcluded[i]].length; j++)
					setExc.add(bicsRows[bicsExcluded[i]][j]);

			int currentIndex = 0;
			if (bicsWithOverlap != null) {
				for (Integer bicID : bicsWithOverlap) {
					for (int j = 0; j < bicsRows[bicID].length; j++)
						setExc.add(bicsRows[bicID][j]);

					//TODO: edit this
					int nrOverlapVals = 0;
					if(bicsRows[bicID].length < bicSize)
						nrOverlapVals = (int) (((double) bicsRows[bicID].length) * percOverlap);
					else
						nrOverlapVals = (int) (((double) bicSize) * percOverlap);

					for (int j = 0, val = -1; j < nrOverlapVals && currentIndex < bicSize; j++) {
						val = bicsRows[bicID][j];
						if (set.contains(val))
							continue;
						set.add(val);
						result[currentIndex++] = val;	
					}
				}
			}		

			//Depois de fazer o overlapping, caso não existam mais rows livres, usar as já escolhidas
			if (setExc.size() + (bicSize - currentIndex) > dimSize) {
				for(int val = -1; currentIndex < bicSize; currentIndex++) {
					SortedSet<Integer> testedRows = new TreeSet<>();
					boolean alreadyExists = false;
					boolean invalidOverlap = false;
					boolean notExhaustedDim = false;
					do {
						val = random.nextInt(dimSize);
						testedRows.add(val);
						
						//condicoes para garantir que a row eh valida
						alreadyExists = set.contains(val);
						invalidOverlap = isOverlap(val, bicCols, elements);
						notExhaustedDim = testedRows.size() < dimSize;
					} while (alreadyExists || (invalidOverlap && notExhaustedDim));
					
					//verificar se a ultima row testada eh ou nao valida
					if(currentIndex < bicSize && !alreadyExists && !invalidOverlap) {
						set.add(val);
						result[currentIndex] = val;
					}
					else {
						//throw new Exception("Not able to meet the non-overlapping row criteria for the generate sets of columns and contexts!\nSuggestions: "
						//		+ "increase the matrix size OR decrease the size of trics!");
						noSpace = true;
					}
				}
			}
			else {
				//Enquanto houver rows livres usa-las
				for (int val = -1; currentIndex < bicSize; currentIndex++) {
					SortedSet<Integer> testedRows = new TreeSet<>();
					do {
						val = random.nextInt(dimSize);
						testedRows.add(val);
					}while ((set.contains(val) || setExc.contains(val) || isOverlap(val, bicCols, elements)) && testedRows.size() < dimSize);
					
					if(testedRows.size() == dimSize && (set.contains(val) || setExc.contains(val) || isOverlap(val, bicCols, elements)))
						throw new OutputErrorException("noa ha espaço");
					else {
						set.add(val);
						result[currentIndex] = val;
					}
				}
			}

		}
		if(noSpace)
			result = null;
		
		return result;
	}

	protected int[] generateOthers(int dimSize, int bicSize, double percOverlap, int[][] bicsDimIndex, Set<Integer> chosenIndexes,
			int[] bicsWithOverlap, int[] bicsExcluded, boolean contiguity, Pair<Integer, Integer> range) throws Exception {

		int[] result = new int[bicSize];
		SortedSet<Integer> set = new TreeSet<>();
		boolean noSpace = false;
		
		//Se nao existir overlapping
		if (Double.compare(percOverlap,0) <= 0) { // no need for plaid calculus
			if(contiguity)
				result = generateContiguous(bicSize, dimSize, range);
			else
				for (int i = 0, val = -1; i < bicSize; i++) {
					SortedSet<Integer> testedIndexes = new TreeSet<>();
					boolean alreadyExists = false;
					boolean chosenByOtherBic = false;
					boolean notExhaustedDim = false;
					do {
						val = random.nextInt(dimSize);
						
						testedIndexes.add(val);
						
						alreadyExists = set.contains(val);
						chosenByOtherBic = chosenIndexes.contains(val);
						notExhaustedDim = testedIndexes.size() < dimSize;
					} while ((alreadyExists || chosenByOtherBic) && notExhaustedDim);
					
					if(!alreadyExists) {
						set.add(val);
						result[i] = val;
					}
					else{
						do {
							val = random.nextInt(dimSize);
							alreadyExists = set.contains(val);
						} while (alreadyExists);
						set.add(val);
						result[i] = val;
					}
					
				}
		}
		else {
			//Primeira tentativa - aproveitar ao maximo colunas livres
			SortedSet<Integer> setExc = new TreeSet<>();
			for (int i = 0; i < bicsExcluded.length; i++)
				for (int j = 0; j < bicsDimIndex[bicsExcluded[i]].length; j++)
					setExc.add(bicsDimIndex[bicsExcluded[i]][j]);

			int currentIndex = 0;
			if (bicsWithOverlap != null) {
				for (Integer bicID : bicsWithOverlap) {
					for (int j = 0; j < bicsDimIndex[bicID].length; j++)
						setExc.add(bicsDimIndex[bicID][j]);

					//TODO: edit this
					int nrOverlapVals = (int) (((double) bicsDimIndex[bicID].length) * percOverlap);

					if(contiguity) {
						int first = bicsDimIndex[bicID][0];
						int last = bicsDimIndex[bicID][bicsDimIndex[bicID].length - 1];

						if(first >= (bicSize - nrOverlapVals)) {
							for (int j = 0, val = -1; currentIndex < bicSize; j++) {
								if(j < (bicSize - nrOverlapVals))
									val = first - (bicSize - nrOverlapVals - j);
								else
									val = bicsDimIndex[bicID][j - (bicSize - nrOverlapVals)];

								set.add(val);
								result[currentIndex++] = val;
							}
						}
						else if((dimSize - last) >= (bicSize - nrOverlapVals)) {
							for (int j = 0, val = -1; currentIndex < bicSize; j++) {
								if(j < nrOverlapVals)
									val = last - (nrOverlapVals - (currentIndex + 1));
								else
									val = last + (j - (nrOverlapVals - 1));

								set.add(val);
								result[currentIndex++] = val;
							}
						}
						else
							//throw new Exception("Not able to meet the contiguous overlapping criteria for the generate sets of columns/contexts!\n "
							//		+ "Increase the matrix size OR decrease the size of trics!");
							noSpace = true;
					}
					else {
						for (int j = 0, val = -1; j < nrOverlapVals && currentIndex < bicSize; j++) {
							val = bicsDimIndex[bicID][j];
							if (set.contains(val))
								continue;
							set.add(val);
							result[currentIndex++] = val;
						}
					}
				}
			}		

			//Depois de fazer o overlapping, caso não existam mais colunas livres, usar as já escolhidas
			if (setExc.size() + (bicSize - currentIndex) > dimSize) {
				for(int val = -1; currentIndex < bicSize; currentIndex++) {
					do {
						val = random.nextInt(dimSize);
					}while (set.contains(val));
					set.add(val);
					result[currentIndex] = val;
				}
			}
			else {
				//Enquanto houver colunas livres usa-las
				for (int val = -1; currentIndex < bicSize; currentIndex++) {
					if(contiguity) {
						if(currentIndex == 0) {
							result = generateContiguous(bicSize, dimSize, range);
							currentIndex = bicSize;
						}
						else
							result[currentIndex] = result[currentIndex-1] + 1;
					}
					else {
						do
							val = random.nextInt(dimSize);
						while (set.contains(val) || setExc.contains(val));
						set.add(val);
						result[currentIndex] = val;
					}
				}
			}

		}
		
		if(noSpace)
			result = null;
		
		return result;
	}


	//TODO: fatorizar isto
	protected int[] generate(int nBicDim, int nDim, double overlap, int[][] vecsL, int[] overlapVecs, int[] vecsExc, boolean contiguity, 
			Pair<Integer, Integer> range)
			throws Exception {

		int[] result = new int[nBicDim];
		SortedSet<Integer> set = new TreeSet<>();
		boolean noSpace = false;
		
		//Se nao existir overlapping
		if (Double.compare(overlap,0) < 0) { // no need for plaid calculus
			if (nBicDim == nDim)
				for (int i = 0; i < nBicDim; i++)
					result[i] = i;
			else if(contiguity)
				result = generateContiguous(nBicDim, nDim, range);
			else
				for (int i = 0, val = -1; i < nBicDim; i++) {
					do {
						if(range == null)
							val = random.nextInt(nDim);
						else
							val = random.nextInt(range.getSecond()-range.getFirst()) + range.getFirst();
					} while (set.contains(val));
					set.add(val);
					result[i] = val;
				}
			return result;
		}

		//Primeira tentativa - aproveitar ao maximo colunas livres
		SortedSet<Integer> setExc = new TreeSet<>();
		for (int i = 0; i < vecsExc.length; i++)
			for (int j = 0; j < vecsL[vecsExc[i]].length; j++)
				setExc.add(vecsL[vecsExc[i]][j]);

		if (nBicDim == nDim)
			for (int i = 0; i < nBicDim; i++)
				result[i] = i;
		else {
			int i = 0;
			if (overlapVecs != null) {
				for (Integer vecID : overlapVecs) {
					for (int j = 0; j < vecsL[vecID].length; j++)
						setExc.add(vecsL[vecID][j]);

					/*
					System.out.println("BicDimSize = " + nBicDim);
					System.out.println("OverlappedBicDimSize =" + vecsL[vecID].length);
					System.out.println("Overlap = " + overlap);
					*/
					
					int nrOverlapVals = 0;
					if(vecsL[vecID].length < nBicDim)
						nrOverlapVals = (int) (((double) vecsL[vecID].length) * overlap);
					else
						nrOverlapVals = (int) (((double) nBicDim) * overlap);

					//System.out.println("nrOverlapVals = " + nrOverlapVals);
					if(nrOverlapVals > 0) {
						if(contiguity) {
							
							int first = -1;
							int last = -1;
							
							if(range == null || (vecsL[vecID][0] >= range.getFirst() &&
									vecsL[vecID][vecsL[vecID].length - 1] < range.getSecond())) {
								first = vecsL[vecID][0];
								last = vecsL[vecID][vecsL[vecID].length - 1];
							}
							else {
								Pair<Integer, Integer> limits = getBiclusterLimitsWithinRange(vecsL[vecID], range);
								first = limits.getFirst();
								last = limits.getSecond();
							}
							
							if(first >= 0 && first < nDim && last >= 0 && last < nDim) {
	
								int dimStart = (range == null) ? 0 : range.getFirst(); 
								
								if((first - dimStart) >= (nBicDim - nrOverlapVals)) {
									for (int j = 0, val = -1; i < nBicDim && j < nBicDim; j++) {
										if(j < (nBicDim - nrOverlapVals))
											val = first - (nBicDim - nrOverlapVals - j);
										else
											val = vecsL[vecID][j - (nBicDim - nrOverlapVals)];
										
										if(range == null || (val >= range.getFirst() && val < range.getSecond())) {
											set.add(val);
											result[i++] = val;
										}
									}
								}
								else if((nDim - last) >= (nBicDim - nrOverlapVals)) {
									for (int j = 0, val = -1; i < nBicDim && j < nBicDim; j++) {
										if(j < nrOverlapVals)
											val = last - (nrOverlapVals - (i + 1));
										else
											val = last + (j - (nrOverlapVals - 1));
		
										if(range == null || (val >= range.getFirst() && val < range.getSecond())) {
											set.add(val);
											result[i++] = val;
										}
									}
								}
								else
									//throw new Exception("Not able to meet the contiguous overlapping criteria for the generate sets of columns/contexts!\n "
									//		+ "Increase the matrix size OR decrease the size of trics!");
									noSpace = true;
							}
						}
						else {
							for (int j = 0, val = -1; j < nrOverlapVals && i < nBicDim; j++) {
								val = vecsL[vecID][j];
								
								boolean isInRange = (range != null) && (val < range.getFirst() || val >= range.getSecond());
								
								if (set.contains(val) || isInRange)
									continue;
								set.add(val);
								result[i++] = val;
							}
						}
					}
				}
			}		

			//Depois de fazer o overlapping, caso não existam mais colunas livres, usar as já escolhidas
			if (setExc.size() + (nBicDim - i) > nDim) {
				for(int val = -1; i < nBicDim; i++) {
					do {
						if(range == null)
							val = random.nextInt(nDim);
						else
							val = random.nextInt(range.getSecond()-range.getFirst()) + range.getFirst();
					}while (set.contains(val));
					set.add(val);
					result[i] = val;
				}
			}

			//Enquanto houver colunas livres usa-las
			for (int val = -1; i < nBicDim; i++) {
				if(contiguity) {
					if(i == 0) {
						result = generateContiguous(nBicDim, nDim, range);
						i = nBicDim;
					}
					else
						result[i] = result[i-1] + 1;
				}
				else {
					
					boolean existsOrExcluded = false;
					boolean isInRange = false;
					
					do {
						if(range == null)
							val = random.nextInt(nDim);
						else
							val = random.nextInt(range.getSecond()-range.getFirst()) + range.getFirst();
					
					existsOrExcluded = set.contains(val) || setExc.contains(val);
					isInRange = (range != null) && (val < range.getFirst() || val >= range.getSecond());
					
					}while (existsOrExcluded || isInRange);
					set.add(val);
					result[i] = val;
				}
			}
		}
		
		if(noSpace)
			result = null;
		
		return result;
	}

	private Pair<Integer, Integer> getBiclusterLimitsWithinRange(int[] bicCols, Pair<Integer, Integer> range){
		
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		
		for(int i = 0; i < bicCols.length; i++) {
			if(bicCols[i] < min && bicCols[i] >= range.getFirst())
				min = bicCols[i];
			if(bicCols[i] > max && bicCols[i] < range.getSecond())
				max = bicCols[i];
		}
		
		return new Pair<Integer, Integer>(min,max);
	}
	
	/**
	 * Seleciona nBicDim colunas que representam as colunas do bicluster
	 * @param nBicDim
	 * @param nDim
	 * @param overlap
	 * @param vecsL
	 * @param overlapVecs
	 * @param vecsExc
	 * @return
	 */
	protected int[] generateContiguous(int bicDimSize, int datasetDimSize, Pair<Integer, Integer> range) {

		int[] result = new int[bicDimSize];

		if (range == null && bicDimSize == datasetDimSize)
			for (int i = 0; i < bicDimSize; i++)
				result[i] = i;
		else if (range != null && bicDimSize == (range.getSecond() - range.getFirst())) {
			for (int i = range.getFirst(); i < range.getSecond(); i++)
				result[i] = i;
		}
		else {
			int posInicial = 0;
			if(range == null)
				posInicial = random.nextInt(datasetDimSize - bicDimSize);
			else
				posInicial = random.nextInt((range.getSecond()-range.getFirst()) - bicDimSize) + range.getFirst();
			for (int i = 0; i < bicDimSize; i++)
				result[i] = posInicial + i;
		}

		return result;
	}

	protected int[] generateNonOverlappingOthers(int nBicDim, int nDim, Set<Integer> chosenCols, boolean contiguity, 
			Pair<Integer, Integer> range) {

		int[] result = new int[nBicDim];
		SortedSet<Integer> set = new TreeSet<>();
		long k = 0; 
		long limit = nDim * nDim;

		if(contiguity)
			result = generateContiguous(nBicDim, nDim, range);
		else {
			for (int i = 0, val = -1; i < nBicDim; i++) {
				do {
					if(range == null)
						val = random.nextInt(nDim);
					else
						val = random.nextInt(range.getSecond()-range.getFirst()) + range.getFirst();
				} while ((set.contains(val) || chosenCols.contains(val)) && k++ < limit);
				if (k > limit) {
					do {
						if(range == null)
							val = random.nextInt(nDim);
						else
							val = random.nextInt(range.getSecond()-range.getFirst()) + range.getFirst();
					} while (set.contains(val));
				}
				set.add(val);
				result[i] = val;
			}
		}

		return result;
	}

	protected int[] generateNonOverlappingRows(int nBicDim, int nDim, int[] bicCols, Set<String> elements) throws Exception {

		int[] result = new int[nBicDim];
		SortedSet<Integer> set = new TreeSet<>();
		Set<Integer> testedRows = new HashSet<>();

		for (int i = 0, val = -1; i < nBicDim; i++) {
			do {
				val = random.nextInt(nDim);
				testedRows.add(val);
			} while ((set.contains(val) || isOverlap(val, bicCols, elements)) && (testedRows.size() < nDim));


			if (testedRows.size() >= nDim) {
				//throw new Exception("Not able to meet the non-overlapping row criteria for the generate sets of columns and contexts!\nSuggestions: "
				//		+ "increase the matrix size OR decrease the size of trics!");
				result = null;
			}	

			set.add(val);
			if(result != null)
				result[i] = val;
		}
		return result;
	}

	protected String[][] transposeMatrix(String[][] matrix, String oldDim, String newDim) {

		String[][] transposed = null;

		transposed = new String[matrix[0].length][matrix.length];

		
		for(int row = 0; row < transposed.length; row++) {
			for(int col = 0; col < transposed[row].length; col++) {
				transposed[row][col] = matrix[col][row];
			}
		}
		

		return transposed;
	}

	protected Double[][] transposeMatrix(Double[][] matrix, String oldDim, String newDim) {

		Double[][] transposed = null;
		
		transposed = new Double[matrix[0].length][matrix.length];
		
		for(int row = 0; row < transposed.length; row++) {
			for(int col = 0; col < transposed[row].length; col++) {
				transposed[row][col] = matrix[col][row];
			}
		}
	
		return transposed;
	}

	/**
	 * Verifica se um par <row, column> já pertence a algum bicluster
	 * @param val
	 * @param bicCols
	 * @param elements
	 * @return
	 */
	private boolean isOverlap(int val, int[] bicCols, Set<String> elements) {
		
		for (int j = 0, l = bicCols.length; j < l; j++)
			if (elements.contains(val + ":" + bicCols[j]))
				return true;
		return false;
	}

	protected Map<String, Double> generateOverlappingDistribution(int bicSize, OverlappingSettings overlapping, int numRowsBics,
			int numColsBics) throws OutputErrorException {
		
		Map<String, Double> overlappingDist = new HashMap<>();
		
		double maxOverlappingElems = overlapping.getMaxPercOfOverlappingElements();
		//System.out.println("Max_Overlap_Elems = " + maxOverlappingElems);
		
		//System.out.println("Tric size = " + tricSize);
		
		//System.out.println("PercCont = [" + minContPerc + ", " + maxContPerc + "] -> " + overlappingContsPerc + "(" + numOverlappingConts + ")");
		
		double minColPerc =  1.0 / ((double) numColsBics);
		double maxColPerc = Math.min((maxOverlappingElems * bicSize) / numColsBics, 1.0);
		double overlappingColsPerc = minColPerc + (maxColPerc - minColPerc) * random.nextDouble();
		
		if(Double.compare(overlappingColsPerc, overlapping.getPercOfOverlappingColumns()) > 0)
			overlappingColsPerc = overlapping.getPercOfOverlappingColumns();
		
		overlappingDist.put("columnPerc", overlappingColsPerc);
		
		int numOverlappingCols = (int) (overlappingColsPerc * numColsBics);
		
		//System.out.println("PercCols = [" + minColPerc + ", " + maxColPerc + "] -> " + overlappingColsPerc + "(" + numOverlappingCols + ")");
		
		double minRowPerc = 1.0 / ((double) numRowsBics);
		double maxRowPerc = Math.min((maxOverlappingElems * bicSize) / (numRowsBics * numOverlappingCols),
				1.0);
		double overlappingRowsPerc = minRowPerc + (maxRowPerc - minRowPerc) * random.nextDouble();
		
		if(Double.compare(overlappingRowsPerc, overlapping.getPercOfOverlappingRows()) > 0)
			overlappingRowsPerc = overlapping.getPercOfOverlappingRows();
		
		overlappingDist.put("rowPerc", overlappingRowsPerc);
		
		int numOverlappingRows = (int) (overlappingRowsPerc * numRowsBics);
		
		//System.out.println("PercRows = [" + minRowPerc + ", " + maxRowPerc + "] -> " + overlappingRowsPerc + "(" + numOverlappingRows + ")");
		
		int total_overlapping = numOverlappingRows * numOverlappingCols;
		
		//System.out.println("Total overlapping expected = " + total_overlapping);
		
		/*
		overlappingDist.put("rowPerc", overlapping.getMaxPercOfOverlappingElements());
		overlappingDist.put("columnPerc", overlapping.getMaxPercOfOverlappingElements());
		overlappingDist.put("contextPerc", overlapping.getMaxPercOfOverlappingElements());
		*/
		
		return overlappingDist;
	}
	
	protected Map<String, Integer> generateBicStructure(BiclusterStructure bicStructure, int maxRows, int maxCols){

		/**
		 * PART II: select number of rows and columns according to distribution
		 * U(min,max) or N(mean,std)
		 **/

		int rows;
		int cols;
		Map<String, Integer> result = new HashMap<>();
		double rows1 = bicStructure.getRowsParam1();
		double rows2 = bicStructure.getRowsParam2();
		double cols1 = bicStructure.getColumnsParam1();
		double cols2 = bicStructure.getColumnsParam2();
		
		do {
			if (bicStructure.getRowsDistribution().equals(Distribution.UNIFORM))
				rows = (int) rows1 + (rows1 == rows2 ? 0 : random.nextInt((int) (rows2 - rows1)));
			else
				rows = (int) Math.round(random.nextGaussian() * rows2 + rows1);
	
			if(rows < 1)
				rows = 1;
			else if(rows > maxRows)
				rows = maxRows;
	
			
	
			if (bicStructure.getColumnsDistribution().equals(Distribution.UNIFORM))
				cols = (int) cols1 + (cols1 == cols2 ? 0 : random.nextInt((int) (cols2 - cols1)));
			else
				cols = (int) Math.round(random.nextGaussian() * cols2 + cols1);
	
			if(cols < 1)
				cols = 1;
			else if(cols > maxCols)
				cols = maxCols;
	
		}while((rows == 1 && cols == 1));

		result.put("columns", cols);
		result.put("rows", rows);

		return result;
	}
	
	protected Double[][] generateAdditiveFactors(boolean realValued, SingleBiclusterPattern pattern, NumericBicluster<Double> bicK, double min, 
			double max) throws ExceedBiclusterBoundsException {

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
	
	protected Double[][] generateMultiplicativeFactors(boolean realValued, SingleBiclusterPattern pattern, NumericBicluster<Double> bicK, double minAllowed,
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

	protected Double[][] generateOrderPreserving(boolean realValued, SingleBiclusterPattern pattern, NumericBicluster<Double> bicK, double min, double max) {

		Double[][] bicsymbols = null;

		PatternType rowType = pattern.getRowsPattern();
		PatternType columnType = pattern.getColumnsPattern();
		TimeProfile timeProfile = pattern.getTimeProfile();
		
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

	protected Integer[] generateOrder(int size) {
		Integer[] order = new Integer[size];
		for(int i = 0; i < size; i++)
			order[i] = i;
		Collections.shuffle(Arrays.asList(order), RandomObject.getInstance());
		return order;
	}
	
	private Double[] shuffle(Integer[] order, Double[] array) {
		
		Double[] newArray = new Double[array.length];
		
		for(int i = 0; i < order.length; i++) {
			newArray[order[i]] = array[i];
		}
		
		return newArray;
	}
	
	protected Double[][] generateConstant(boolean realValued, SingleBiclusterPattern pattern, NumericBicluster<Double> bicK, double min,
			double max){

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
	
	protected  String[] shuffle(Integer[] order, String[] array) {
		
		String[] newArray = new String[array.length];
		
		for(int i = 0; i < order.length; i++) {
			newArray[order[i]] = array[i];
		}
		
		return newArray;
	}
	
	
	public void changeState(String state) {
		
		setChanged();
		notifyObservers(state);
	}

	/**
	 * @return the datasetFileName
	 */
	public String getDatasetFileName() {
		return datasetFileName;
	}

	/**
	 * @param datasetFileName the datasetFileName to set
	 */
	public void setDatasetFileName(String datasetFileName) {
		this.datasetFileName = datasetFileName;
	}

	/**
	 * @return the tricsInfoFileName
	 */
	public String getBicsInfoFileName() {
		return bicsInfoFileName;
	}

	/**
	 * @param bicsInfoFileName the tricsInfoFileName to set
	 */
	public void setBicsInfoFileName(String bicsInfoFileName) {
		this.bicsInfoFileName = bicsInfoFileName;
	}

	/**
	 * @return the statsFileName
	 */
	public String getStatsFileName() {
		return statsFileName;
	}

	/**
	 * @param statsFileName the statsFileName to set
	 */
	public void setStatsFileName(String statsFileName) {
		this.statsFileName = statsFileName;
	}
	
	/**
	 * Set the file path
	 * @param path the file path
	 */
	public void setPath(String path) {
		this.path = path;
	}
	
	/**
	 * Get the file path
	 * @return the file path
	 */
	public String getPath() {
		return this.path;
	}
}
