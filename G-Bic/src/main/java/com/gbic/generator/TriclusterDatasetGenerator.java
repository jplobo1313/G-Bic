/**
 * TriclusterDatasetGenerator Class
 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @version 1.0
 */
package com.gbic.generator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.gbic.domain.dataset.Dataset;
import com.gbic.exceptions.OutputErrorException;
import com.gbic.types.Distribution;
import com.gbic.utils.OverlappingSettings;
import com.gbic.utils.TriclusterPattern;
import com.gbic.utils.TriclusterStructure;

public abstract class TriclusterDatasetGenerator extends Observable {

	private String path;
	private String datasetFileName;
	private String tricsInfoFileName;
	private String statsFileName;
	
	Random random = new Random();
	
	/**
	 * Generate a dataset with planted triclusters
	 * @param patterns The list of patterns for each tricluster
	 * @param tricStructure The information about the tricluster's structure
	 * @param overlapping The information about overlapping properties
	 * @return The generated dataset
	 * @throws Exception
	 */
	public abstract Dataset generate(List<TriclusterPattern> patterns, TriclusterStructure tricStructure,
			OverlappingSettings overlapping) throws Exception;

	protected int[] generateRows(int dimSize, int tricSize, double percOverlap, int[][] tricsRows,
			int[] tricsWithOverlap, int[] tricsExcluded, int[] tricCols, int[] tricConts, Set<String> elements) throws Exception {
		
		//guardar rows escolhidas
		int[] result = new int[tricSize];
		//guardar rows escolhidas (same, redudante talvez)
		SortedSet<Integer> set = new TreeSet<>();
		boolean noSpace = false;
		//Se nao existir overlapping
		if (Double.compare(percOverlap,0) <= 0) { // no need for plaid calculus
			for (int i = 0, val = -1; i < tricSize; i++) {
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
					invalidOverlap = isOverlap(val, tricCols, tricConts, elements);
					notExhaustedDim = testedRows.size() < dimSize;
				} while (alreadyExists || (invalidOverlap && notExhaustedDim));
				
				//verificar se a ultima row testada eh ou nao valida
				if(i < tricSize && !alreadyExists && !invalidOverlap) {
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
			for (int i = 0; i < tricsExcluded.length; i++)
				for (int j = 0; j < tricsRows[tricsExcluded[i]].length; j++)
					setExc.add(tricsRows[tricsExcluded[i]][j]);

			int currentIndex = 0;
			if (tricsWithOverlap != null) {
				for (Integer tricID : tricsWithOverlap) {
					for (int j = 0; j < tricsRows[tricID].length; j++)
						setExc.add(tricsRows[tricID][j]);

					//TODO: edit this
					int nrOverlapVals = 0;
					if(tricsRows[tricID].length < tricSize)
						nrOverlapVals = (int) (((double) tricsRows[tricID].length) * percOverlap);
					else
						nrOverlapVals = (int) (((double) tricSize) * percOverlap);

					for (int j = 0, val = -1; j < nrOverlapVals && currentIndex < tricSize; j++) {
						val = tricsRows[tricID][j];
						if (set.contains(val))
							continue;
						set.add(val);
						result[currentIndex++] = val;	
					}
				}
			}		

			//Depois de fazer o overlapping, caso não existam mais rows livres, usar as já escolhidas
			if (setExc.size() + (tricSize - currentIndex) > dimSize) {
				for(int val = -1; currentIndex < tricSize; currentIndex++) {
					SortedSet<Integer> testedRows = new TreeSet<>();
					boolean alreadyExists = false;
					boolean invalidOverlap = false;
					boolean notExhaustedDim = false;
					do {
						val = random.nextInt(dimSize);
						testedRows.add(val);
						
						//condicoes para garantir que a row eh valida
						alreadyExists = set.contains(val);
						invalidOverlap = isOverlap(val, tricCols, tricConts, elements);
						notExhaustedDim = testedRows.size() < dimSize;
					} while (alreadyExists || (invalidOverlap && notExhaustedDim));
					
					//verificar se a ultima row testada eh ou nao valida
					if(currentIndex < tricSize && !alreadyExists && !invalidOverlap) {
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
				for (int val = -1; currentIndex < tricSize; currentIndex++) {
					SortedSet<Integer> testedRows = new TreeSet<>();
					do {
						val = random.nextInt(dimSize);
						testedRows.add(val);
					}while ((set.contains(val) || setExc.contains(val) || isOverlap(val, tricCols, tricConts, elements)) && testedRows.size() < dimSize);
					
					if(testedRows.size() == dimSize && (set.contains(val) || setExc.contains(val) || isOverlap(val, tricCols, tricConts, elements)))
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

	protected int[] generateOthers(int dimSize, int tricSize, double percOverlap, int[][] tricsDimIndex, Set<Integer> chosenIndexes,
			int[] tricsWithOverlap, int[] tricsExcluded, boolean contiguity) throws Exception {

		int[] result = new int[tricSize];
		SortedSet<Integer> set = new TreeSet<>();
		boolean noSpace = false;
		
		//Se nao existir overlapping
		if (Double.compare(percOverlap,0) <= 0) { // no need for plaid calculus
			if(contiguity)
				result = generateContiguous(tricSize, dimSize);
			else
				for (int i = 0, val = -1; i < tricSize; i++) {
					SortedSet<Integer> testedIndexes = new TreeSet<>();
					boolean alreadyExists = false;
					boolean chosenByOtherTric = false;
					boolean notExhaustedDim = false;
					do {
						val = random.nextInt(dimSize);
						
						testedIndexes.add(val);
						
						alreadyExists = set.contains(val);
						chosenByOtherTric = chosenIndexes.contains(val);
						notExhaustedDim = testedIndexes.size() < dimSize;
					} while ((alreadyExists || chosenByOtherTric) && notExhaustedDim);
					
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
			for (int i = 0; i < tricsExcluded.length; i++)
				for (int j = 0; j < tricsDimIndex[tricsExcluded[i]].length; j++)
					setExc.add(tricsDimIndex[tricsExcluded[i]][j]);

			int currentIndex = 0;
			if (tricsWithOverlap != null) {
				for (Integer tricID : tricsWithOverlap) {
					for (int j = 0; j < tricsDimIndex[tricID].length; j++)
						setExc.add(tricsDimIndex[tricID][j]);

					//TODO: edit this
					int nrOverlapVals = (int) (((double) tricsDimIndex[tricID].length) * percOverlap);

					if(contiguity) {
						int first = tricsDimIndex[tricID][0];
						int last = tricsDimIndex[tricID][tricsDimIndex[tricID].length - 1];

						if(first >= (tricSize - nrOverlapVals)) {
							for (int j = 0, val = -1; currentIndex < tricSize; j++) {
								if(j < (tricSize - nrOverlapVals))
									val = first - (tricSize - nrOverlapVals - j);
								else
									val = tricsDimIndex[tricID][j - (tricSize - nrOverlapVals)];

								set.add(val);
								result[currentIndex++] = val;
							}
						}
						else if((dimSize - last) >= (tricSize - nrOverlapVals)) {
							for (int j = 0, val = -1; currentIndex < tricSize; j++) {
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
						for (int j = 0, val = -1; j < nrOverlapVals && currentIndex < tricSize; j++) {
							val = tricsDimIndex[tricID][j];
							if (set.contains(val))
								continue;
							set.add(val);
							result[currentIndex++] = val;
						}
					}
				}
			}		

			//Depois de fazer o overlapping, caso não existam mais colunas livres, usar as já escolhidas
			if (setExc.size() + (tricSize - currentIndex) > dimSize) {
				for(int val = -1; currentIndex < tricSize; currentIndex++) {
					do {
						val = random.nextInt(dimSize);
					}while (set.contains(val));
					set.add(val);
					result[currentIndex] = val;
				}
			}
			else {
				//Enquanto houver colunas livres usa-las
				for (int val = -1; currentIndex < tricSize; currentIndex++) {
					if(contiguity) {
						if(currentIndex == 0) {
							result = generateContiguous(tricSize, dimSize);
							currentIndex = tricSize;
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
	protected int[] generate(int nBicDim, int nDim, double overlap, int[][] vecsL, int[] overlapVecs, int[] vecsExc, boolean contiguity)
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
				result = generateContiguous(nBicDim, nDim);
			else
				for (int i = 0, val = -1; i < nBicDim; i++) {
					do {
						val = random.nextInt(nDim);
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

					//TODO: edit this
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
					
					if(contiguity) {
						int first = vecsL[vecID][0];
						int last = vecsL[vecID][vecsL[vecID].length - 1];

						if(first >= (nBicDim - nrOverlapVals)) {
							for (int j = 0, val = -1; i < nBicDim; j++) {
								if(j < (nBicDim - nrOverlapVals))
									val = first - (nBicDim - nrOverlapVals - j);
								else
									val = vecsL[vecID][j - (nBicDim - nrOverlapVals)];

								set.add(val);
								result[i++] = val;
							}
						}
						else if((nDim - last) >= (nBicDim - nrOverlapVals)) {
							for (int j = 0, val = -1; i < nBicDim; j++) {
								if(j < nrOverlapVals)
									val = last - (nrOverlapVals - (i + 1));
								else
									val = last + (j - (nrOverlapVals - 1));

								set.add(val);
								result[i++] = val;
							}
						}
						else
							//throw new Exception("Not able to meet the contiguous overlapping criteria for the generate sets of columns/contexts!\n "
							//		+ "Increase the matrix size OR decrease the size of trics!");
							noSpace = true;
					}
					else {
						for (int j = 0, val = -1; j < nrOverlapVals && i < nBicDim; j++) {
							val = vecsL[vecID][j];
							if (set.contains(val))
								continue;
							set.add(val);
							result[i++] = val;
						}
					}
				}
			}		

			//Depois de fazer o overlapping, caso não existam mais colunas livres, usar as já escolhidas
			if (setExc.size() + (nBicDim - i) > nDim) {
				for(int val = -1; i < nBicDim; i++) {
					do {
						val = random.nextInt(nDim);
					}while (set.contains(val));
					set.add(val);
					result[i] = val;
				}
			}

			//Enquanto houver colunas livres usa-las
			for (int val = -1; i < nBicDim; i++) {
				if(contiguity) {
					if(i == 0) {
						result = generateContiguous(nBicDim, nDim);
						i = nBicDim;
					}
					else
						result[i] = result[i-1] + 1;
				}
				else {
					do
						val = random.nextInt(nDim);
					while (set.contains(val) || setExc.contains(val));
					set.add(val);
					result[i] = val;
				}
			}
		}
		
		if(noSpace)
			result = null;
		
		return result;
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
	protected int[] generateContiguous(int bicDimSize, int datasetDimSize) {

		int[] result = new int[bicDimSize];

		if (bicDimSize == datasetDimSize)
			for (int i = 0; i < bicDimSize; i++)
				result[i] = i;
		else {
			int posInicial = random.nextInt(datasetDimSize - bicDimSize);
			for (int i = 0; i < bicDimSize; i++)
				result[i] = posInicial + i;
		}

		return result;
	}

	protected int[] generateNonOverlappingOthers(int nBicDim, int nDim, Set<Integer> chosenCols, boolean contiguity) {

		int[] result = new int[nBicDim];
		SortedSet<Integer> set = new TreeSet<>();
		long k = 0; 
		long limit = nDim * nDim;

		if(contiguity)
			result = generateContiguous(nBicDim, nDim);
		else {
			for (int i = 0, val = -1; i < nBicDim; i++) {
				do {
					val = random.nextInt(nDim);
				} while ((set.contains(val) || chosenCols.contains(val)) && k++ < limit);
				if (k > limit) {
					do {
						val = random.nextInt(nDim);
					} while (set.contains(val));
				}
				set.add(val);
				result[i] = val;
			}
		}

		return result;
	}

	protected int[] generateNonOverlappingRows(int nBicDim, int nDim, int[] bicCols, int[] bicConts, Set<String> elements) throws Exception {

		int[] result = new int[nBicDim];
		SortedSet<Integer> set = new TreeSet<>();
		Set<Integer> testedRows = new HashSet<>();

		for (int i = 0, val = -1; i < nBicDim; i++) {
			do {
				val = random.nextInt(nDim);
				testedRows.add(val);
			} while ((set.contains(val) || isOverlap(val, bicCols, bicConts, elements)) && (testedRows.size() < nDim));


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

	protected String[][][] transposeMatrix(String[][][] matrix, String oldDim, String newDim) {

		String[][][] transposed = null;

		if(oldDim.equals("z") && newDim.equals("x"))
			transposed = new String[matrix[0].length][matrix.length][matrix[0][0].length];
		else if(oldDim.equals("z") && newDim.equals("y"))
			transposed = new String[matrix[0][0].length][matrix[0].length][matrix.length];
		else
			transposed = new String[matrix.length][matrix[0][0].length][matrix[0].length];

		for(int ctx = 0; ctx < transposed.length; ctx++) {
			for(int row = 0; row < transposed[ctx].length; row++) {
				for(int col = 0; col < transposed[ctx][row].length; col++) {
					if(oldDim.equals("z") && newDim.equals("x"))
						transposed[ctx][row][col] = matrix[row][ctx][col];
					else if(oldDim.equals("z") && newDim.equals("y"))
						transposed[ctx][row][col] = matrix[col][row][ctx];
					else
						transposed[ctx][row][col] = matrix[ctx][col][row];
				}
			}
		}

		return transposed;
	}

	protected Double[][][] transposeMatrix(Double[][][] matrix, String oldDim, String newDim) {

		Double[][][] transposed = null;

		if(oldDim.equals("z") && newDim.equals("x"))
			transposed = new Double[matrix[0].length][matrix.length][matrix[0][0].length];
		else if(oldDim.equals("z") && newDim.equals("y"))
			transposed = new Double[matrix[0][0].length][matrix[0].length][matrix.length];
		else
			transposed = new Double[matrix.length][matrix[0][0].length][matrix[0].length];

		for(int ctx = 0; ctx < transposed.length; ctx++) {
			for(int row = 0; row < transposed[ctx].length; row++) {
				for(int col = 0; col < transposed[ctx][row].length; col++) {
					if(oldDim.equals("z") && newDim.equals("x"))
						transposed[ctx][row][col] = matrix[row][ctx][col];
					else if(oldDim.equals("z") && newDim.equals("y"))
						transposed[ctx][row][col] = matrix[col][row][ctx];
					else
						transposed[ctx][row][col] = matrix[ctx][col][row];
				}
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
	private boolean isOverlap(int val, int[] bicCols, int[] bicConts, Set<String> elements) {
		for(int i = 0; i < bicConts.length; i++)
			for (int j = 0, l = bicCols.length; j < l; j++)
				if (elements.contains(bicConts[i] + ":" + val + ":" + bicCols[j]))
					return true;
		return false;
	}

	protected Map<String, Double> generateOverlappingDistribution(int tricSize, OverlappingSettings overlapping,
			int numContsTrics, int numRowsTrics, int numColsTrics) throws OutputErrorException {
		
		Map<String, Double> overlappingDist = new HashMap<>();
		
		double maxOverlappingElems = overlapping.getMaxPercOfOverlappingElements();
		//System.out.println("Max_Overlap_Elems = " + maxOverlappingElems);
		
		//System.out.println("Tric size = " + tricSize);
		
		double minContPerc = 1.0 / ((double) numContsTrics);
		double maxContPerc = Math.min((maxOverlappingElems * tricSize) / numContsTrics, 1.0);
		double overlappingContsPerc = minContPerc + (maxContPerc - minContPerc) * random.nextDouble();
		
		if(Double.compare(overlappingContsPerc, overlapping.getPercOfOverlappingContexts()) > 0)
			overlappingContsPerc = overlapping.getPercOfOverlappingContexts();
		
		overlappingDist.put("contextPerc", overlappingContsPerc);
		
		int numOverlappingConts = (int) (overlappingContsPerc * numContsTrics);
		
		//System.out.println("PercCont = [" + minContPerc + ", " + maxContPerc + "] -> " + overlappingContsPerc + "(" + numOverlappingConts + ")");
		
		double minColPerc =  1.0 / ((double) numColsTrics);
		double maxColPerc = Math.min((maxOverlappingElems * tricSize) / (numColsTrics * numOverlappingConts), 1.0);
		double overlappingColsPerc = minColPerc + (maxColPerc - minColPerc) * random.nextDouble();
		
		if(Double.compare(overlappingColsPerc, overlapping.getPercOfOverlappingColumns()) > 0)
			overlappingColsPerc = overlapping.getPercOfOverlappingColumns();
		
		overlappingDist.put("columnPerc", overlappingColsPerc);
		
		int numOverlappingCols = (int) (overlappingColsPerc * numColsTrics);
		
		//System.out.println("PercCols = [" + minColPerc + ", " + maxColPerc + "] -> " + overlappingColsPerc + "(" + numOverlappingCols + ")");
		
		double minRowPerc = 1.0 / ((double) numRowsTrics);
		double maxRowPerc = Math.min((maxOverlappingElems * tricSize) / (numRowsTrics * numOverlappingCols * numOverlappingConts),
				1.0);
		double overlappingRowsPerc = minRowPerc + (maxRowPerc - minRowPerc) * random.nextDouble();
		
		if(Double.compare(overlappingRowsPerc, overlapping.getPercOfOverlappingRows()) > 0)
			overlappingRowsPerc = overlapping.getPercOfOverlappingRows();
		
		overlappingDist.put("rowPerc", overlappingRowsPerc);
		
		int numOverlappingRows = (int) (overlappingRowsPerc * numRowsTrics);
		
		//System.out.println("PercRows = [" + minRowPerc + ", " + maxRowPerc + "] -> " + overlappingRowsPerc + "(" + numOverlappingRows + ")");
		
		int total_overlapping = numOverlappingRows * numOverlappingCols * numOverlappingConts;
		
		//System.out.println("Total overlapping expected = " + total_overlapping);
		
		/*
		overlappingDist.put("rowPerc", overlapping.getMaxPercOfOverlappingElements());
		overlappingDist.put("columnPerc", overlapping.getMaxPercOfOverlappingElements());
		overlappingDist.put("contextPerc", overlapping.getMaxPercOfOverlappingElements());
		*/
		
		return overlappingDist;
	}
	
	protected Map<String, Integer> generateTricStructure(TriclusterStructure tricStructure, int maxRows, int maxCols, int maxConts){

		/**
		 * PART II: select number of rows and columns according to distribution
		 * U(min,max) or N(mean,std)
		 **/

		int rows;
		int cols;
		int ctxs;
		Map<String, Integer> result = new HashMap<>();
		double rows1 = tricStructure.getRowsParam1();
		double rows2 = tricStructure.getRowsParam2();
		double cols1 = tricStructure.getColumnsParam1();
		double cols2 = tricStructure.getColumnsParam2();
		double context1 = tricStructure.getContextsParam1();
		double context2 = tricStructure.getContextsParam2();

		do {
			if (tricStructure.getRowsDistribution().equals(Distribution.UNIFORM))
				rows = (int) rows1 + (rows1 == rows2 ? 0 : random.nextInt((int) (rows2 - rows1)));
			else
				rows = (int) Math.round(random.nextGaussian() * rows2 + rows1);
	
			if(rows < 1)
				rows = 1;
			else if(rows > maxRows)
				rows = maxRows;
	
			
	
			if (tricStructure.getColumnsDistribution().equals(Distribution.UNIFORM))
				cols = (int) cols1 + (cols1 == cols2 ? 0 : random.nextInt((int) (cols2 - cols1)));
			else
				cols = (int) Math.round(random.nextGaussian() * cols2 + cols1);
	
			if(cols < 1)
				cols = 1;
			else if(cols > maxCols)
				cols = maxCols;
	
			
	
			if (tricStructure.getContextsDistribution().equals(Distribution.UNIFORM))
				ctxs = (int) context1 + (context1 == context2 ? 0 : random.nextInt((int) (context2 - context1)));
			else
				ctxs = (int) Math.round(random.nextGaussian() * context2 + context1);
	
			if(ctxs < 1)
				ctxs = 1;
			else if(ctxs > maxConts)
				ctxs = maxConts;
		}while((rows == 1 && cols == 1) || (rows == 1 && ctxs == 1) || (cols == 1 && ctxs == 1));

		result.put("columns", cols);
		result.put("rows", rows);
		result.put("contexts", ctxs);

		return result;
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
	public String getTricsInfoFileName() {
		return tricsInfoFileName;
	}

	/**
	 * @param tricsInfoFileName the tricsInfoFileName to set
	 */
	public void setTricsInfoFileName(String tricsInfoFileName) {
		this.tricsInfoFileName = tricsInfoFileName;
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
