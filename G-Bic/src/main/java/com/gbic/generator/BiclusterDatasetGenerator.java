/**
 * BiclusterDatasetGenerator Class
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
import com.gbic.utils.BiclusterPattern;
import com.gbic.utils.BiclusterStructure;

public abstract class BiclusterDatasetGenerator extends Observable {

	private String path;
	private String datasetFileName;
	private String bicsInfoFileName;
	private String statsFileName;
	
	Random random = new Random();
	
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

	protected int[] generateRows(int dimSize, int bicSize, double percOverlap, int[][] bicsRows,
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
			int[] bicsWithOverlap, int[] bicsExcluded, boolean contiguity) throws Exception {

		int[] result = new int[bicSize];
		SortedSet<Integer> set = new TreeSet<>();
		boolean noSpace = false;
		
		//Se nao existir overlapping
		if (Double.compare(percOverlap,0) <= 0) { // no need for plaid calculus
			if(contiguity)
				result = generateContiguous(bicSize, dimSize);
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
							result = generateContiguous(bicSize, dimSize);
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
