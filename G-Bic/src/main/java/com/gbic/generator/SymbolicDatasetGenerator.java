/**
 * SymbolicTriclusterDatasetGenerator Class
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

import com.gbic.domain.bicluster.SymbolicBicluster;
import com.gbic.domain.dataset.Dataset;
import com.gbic.domain.dataset.SymbolicDataset;
import com.gbic.domain.bicluster.SymbolicBicluster;
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

public class SymbolicDatasetGenerator extends BiclusterDatasetGenerator {


	private SymbolicDataset data;
	private Random random = RandomObject.getInstance();
	private boolean allowsOverlap = false;
	private int numBics;

	/**
	 * Constructor
	 * @param nRows the dataset's number of rows
	 * @param nCols the dataset's number of columns
	 * @param nCont the dataset's number of contexts
	 * @param numBics the number of bics to plant
	 * @param background the dataset's background
	 * @param alphabetL the alphabet length
	 * @param symmetric NOT IMPLEMENTED
	 */
	public SymbolicDatasetGenerator(int nRows, int nCols, int numBics, Background background, int alphabetL, boolean symmetric) {
		this.numBics = numBics;
		this.data = new SymbolicDataset(nRows, nCols, numBics, background, symmetric, alphabetL);
	}
	
	/**
	 * Constructor
	 * @param nRows the dataset's number of rows
	 * @param nCols the dataset's number of columns
	 * @param nCont the dataset's number of contexts
	 * @param numBics the number of bics to plant
	 * @param background the dataset's background
	 * @param alphabet array with alphabet symbols
	 * @param symmetric NOT IMPLEMENTED
	 */
	public SymbolicDatasetGenerator(int nRows, int nCols, int numBics, Background background, String[] alphabet, boolean symmetric) {
		this.numBics = numBics;
		this.data = new SymbolicDataset(nRows, nCols, numBics, background, symmetric, alphabet);
	}

	@Override
	public Dataset generate(List<BiclusterPattern> patterns, BiclusterStructure tricStructure,
			OverlappingSettings overlapping) throws Exception {
		
		//num rows expression matrix
		int numRows = data.getNumRows();
		
		//num cols expression matrix
		int numCols = data.getNumCols();
		
		this.allowsOverlap = !overlapping.getPlaidCoherency().equals(PlaidCoherency.NO_OVERLAPPING);
		int maxBicsPerOverlappedArea = overlapping.getMaxBicsPerOverlappedArea();
		int overlappingThreshold = (int)(numBics * overlapping.getPercOfOverlappingBics());
		
		//expression and biclusters symbols
		String[] alphabet = data.getAlphabet();

		//num of rows of a bic
		int numRowsBics = 0;
		//num of cols of a bic
		int numColsBics = 0;

		//Isto pode ser otimizado -> passar p/ dentro do for (1D em vez de 2D).
		int[][] bicsRows = new int[numBics][];
		int[][] bicsCols = new int[numBics][];

		Set<Integer> chosenCols = new HashSet<Integer>();

		int numAttempts = 0;
		
		/** PART I: generate pattern ranges **/
		for (int k = 0; k < numBics; k++) {
			
			boolean hasSpace = true;
			
			changeState("Stage:1, Msg:Bicluster " + k);
			
			System.out.println("Generating bicluster " + (k+1) + " of " + numBics + "...");
			
			if(k >= overlappingThreshold)
				allowsOverlap = false;
			
			SingleBiclusterPattern currentPattern;
			
			if(numBics < patterns.size())
				currentPattern = (SingleBiclusterPattern) patterns.get(random.nextInt(patterns.size()));
			else
				currentPattern = (SingleBiclusterPattern) patterns.get(k % patterns.size());
			
			PatternType rowType = currentPattern.getRowsPattern();
			PatternType columnType = currentPattern.getColumnsPattern();
			TimeProfile timeProfile = currentPattern.getTimeProfile();
			
			Map<String, Integer> structure = generateBicStructure(tricStructure, numRows, numCols);
			
			numRowsBics = structure.get("rows");
			numColsBics = structure.get("columns");


			/** PART IV: select biclusters with (non-)overlapping elements **/
			int[] bicsWithOverlap = null;
			int[] bicsExcluded = null;
			
			
			if (allowsOverlap) {
				
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
				bicsExcluded = new int[l];
				for (int i = 0; i < l; i++)
					bicsExcluded[i] = i;
			}

			/** PART V: generate rows and columns using overlapping constraints **/
			int tricSize = numRowsBics * numColsBics;
			if (allowsOverlap) {
				
				Map<String, Double> overlappingPercs = generateOverlappingDistribution(tricSize, overlapping, numRowsBics, numColsBics);
				
				double overlappingColsPerc = overlappingPercs.get("columnPerc");
				double overlappingRowsPerc = overlappingPercs.get("rowPerc");
				
				System.out.println("Tric " + (k+1) + " - Generating columns...");
				bicsCols[k] = generate(numColsBics, numCols, overlappingColsPerc, bicsCols, bicsWithOverlap,
						bicsExcluded, tricStructure.getContiguity().equals(Contiguity.COLUMNS), null);
				
				System.out.println("Tric " + (k+1) + " - Generating rows...");
				bicsRows[k] = generate(numRowsBics, numRows, overlappingRowsPerc, bicsRows, bicsWithOverlap,
						bicsExcluded, false, null);
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
				
				System.out.println("Tric " + (k+1) + " - Generating columns...");
				bicsCols[k] = generateNonOverlappingOthers(numColsBics, numCols, chosenCols, tricStructure.getContiguity().equals(Contiguity.COLUMNS),
						null);
				System.out.println("Columns: " + bicsCols[k].length);
				
				System.out.println("Tric " + (k+1) + " - Generating rows...");
				bicsRows[k] = generateNonOverlappingRows(numRowsBics, numRows, bicsCols[k], data.getElements());
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

			if(hasSpace) {
	
				System.out.println("Tric " + (k+1) + " - Has space, lets plant the patterns");
				
				for (Integer c : bicsCols[k])
					chosenCols.add(c);				
				
				Arrays.parallelSort(bicsRows[k]);
				Arrays.parallelSort(bicsCols[k]);
				
				SymbolicBicluster bicK;
				
				if(columnType.equals(PatternType.ORDER_PRESERVING))
					bicK = new SymbolicBicluster(k, BicMath.getSet(bicsRows[k]), BicMath.getSet(bicsCols[k]), rowType, columnType, 
							overlapping.getPlaidCoherency(), timeProfile);
				else
					bicK = new SymbolicBicluster(k, BicMath.getSet(bicsRows[k]), BicMath.getSet(bicsCols[k]), rowType, columnType, 
							overlapping.getPlaidCoherency());
	
				/** PART VI: generate biclusters coherencies **/
				String[][] bicsymbols = new String[bicsRows[k].length][bicsCols[k].length];
				
				if(rowType.equals(PatternType.ORDER_PRESERVING)) {
					bicsymbols = new String[bicsCols[k].length][bicsRows[k].length];
					Integer[] order = generateOrder(bicsRows[k].length);
					
					
					for(int col = 0; col < numColsBics; col++) {
						for (int row = 0; row < numRowsBics; row++)
							bicsymbols[col][row] = alphabet[random.nextInt(alphabet.length)];
						Arrays.parallelSort(bicsymbols[col]);
						bicsymbols[col] = shuffle(order, bicsymbols[col]);
					}					
					bicsymbols = transposeMatrix(bicsymbols, "x", "y");
				}
				else if(columnType.equals(PatternType.ORDER_PRESERVING)) {
					Integer[] order = generateOrder(bicsCols[k].length);
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
				
				for (int row = 0; row < bicsRows[k].length; row++) {
					for (int col = 0; col < bicsCols[k].length; col++) {
						this.data.setMatrixItem(bicsRows[k][row], bicsCols[k][col], bicsymbols[row][col]);
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
}