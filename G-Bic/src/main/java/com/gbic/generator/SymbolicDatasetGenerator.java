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
import com.gbic.domain.tricluster.SymbolicTricluster;
import com.gbic.types.Background;
import com.gbic.types.Contiguity;
import com.gbic.types.PatternType;
import com.gbic.types.PlaidCoherency;
import com.gbic.types.TimeProfile;
import com.gbic.utils.BicMath;
import com.gbic.utils.OverlappingSettings;
import com.gbic.utils.TriclusterPattern;
import com.gbic.utils.TriclusterStructure;

public class SymbolicDatasetGenerator extends TriclusterDatasetGenerator {


	private SymbolicDataset data;
	private Random random = new Random();
	private boolean allowsOverlap = false;
	private int numTrics;

	/**
	 * Constructor
	 * @param nRows the dataset's number of rows
	 * @param nCols the dataset's number of columns
	 * @param nCont the dataset's number of contexts
	 * @param numTrics the number of trics to plant
	 * @param background the dataset's background
	 * @param alphabetL the alphabet length
	 * @param symmetric NOT IMPLEMENTED
	 */
	public SymbolicDatasetGenerator(int nRows, int nCols, int nCont, int numTrics, Background background, int alphabetL, boolean symmetric) {
		this.numTrics = numTrics;
		this.data = new SymbolicDataset(nRows, nCols, nCont, numTrics, background, symmetric, alphabetL);
	}
	
	/**
	 * Constructor
	 * @param nRows the dataset's number of rows
	 * @param nCols the dataset's number of columns
	 * @param nCont the dataset's number of contexts
	 * @param numTrics the number of trics to plant
	 * @param background the dataset's background
	 * @param alphabet array with alphabet symbols
	 * @param symmetric NOT IMPLEMENTED
	 */
	public SymbolicDatasetGenerator(int nRows, int nCols, int nCont, int numTrics, Background background, String[] alphabet, boolean symmetric) {
		this.numTrics = numTrics;
		this.data = new SymbolicDataset(nRows, nCols, nCont, numTrics, background, symmetric, alphabet);
	}

	@Override
	public Dataset generate(List<TriclusterPattern> patterns, TriclusterStructure tricStructure,
			OverlappingSettings overlapping) throws Exception {
		
		//num rows expression matrix
		int numRows = data.getNumRows();
		
		//num cols expression matrix
		int numCols = data.getNumCols();
		
		//num contexts expression matrix
		int numConts = data.getNumContexts();
		
		this.allowsOverlap = !overlapping.getPlaidCoherency().equals(PlaidCoherency.NO_OVERLAPPING);
		int maxTricsPerOverlappedArea = overlapping.getMaxTricsPerOverlappedArea();
		int overlappingThreshold = (int)(numTrics * overlapping.getPercOfOverlappingTrics());
		
		//expression and biclusters symbols
		String[] alphabet = data.getAlphabet();

		//does the bics contain simmetric values
		//TODO: ?? simetria em q dimensao???
		boolean symmetries = data.hasSymmetries();

		//num of rows of a bic
		int numRowsTrics = 0;
		//num of cols of a bic
		int numColsTrics = 0;
		//num of contexts of a bic
		int numContsTrics = 0;

		//Isto pode ser otimizado -> passar p/ dentro do for (1D em vez de 2D).
		int[][] bicsRows = new int[numTrics][];
		int[][] bicsCols = new int[numTrics][];
		int[][] bicsConts = new int[numTrics][];

		Set<Integer> chosenCols = new HashSet<Integer>();
		Set<Integer> chosenConts = new HashSet<Integer>();

		/* Nao estou a contemplar simetricos
		if (type.equals(PatternType.SYMMETRIC) && alphabet[0] >= 0)
			throw new BicException("SYMMETRIC coherency cannot be used with positive range!");
		 */

		int numAttempts = 0;
		
		/** PART I: generate pattern ranges **/
		for (int k = 0; k < numTrics; k++) {
			
			boolean hasSpace = true;
			
			changeState("Stage:1, Msg:Tricluster " + k);
			
			System.out.println("Generating tricluster " + (k+1) + " of " + numTrics + "...");
			
			if(k >= overlappingThreshold)
				allowsOverlap = false;
			
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
			
			
			if (allowsOverlap) {
				
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
				bicsExcluded = new int[l];
				for (int i = 0; i < l; i++)
					bicsExcluded[i] = i;
			}

			/** PART V: generate rows and columns using overlapping constraints **/
			int tricSize = numContsTrics * numRowsTrics * numColsTrics;
			if (allowsOverlap) {
				
				Map<String, Double> overlappingPercs = generateOverlappingDistribution(tricSize, overlapping, numContsTrics, numRowsTrics, numColsTrics);
				
				double overlappingContsPerc = overlappingPercs.get("contextPerc");
				double overlappingColsPerc = overlappingPercs.get("columnPerc");
				double overlappingRowsPerc = overlappingPercs.get("rowPerc");
				
				System.out.println("Tric " + (k+1) + " - Generating columns...");
				bicsCols[k] = generate(numColsTrics, numCols, overlappingContsPerc, bicsCols, bicsWithOverlap,
						bicsExcluded, tricStructure.getContiguity().equals(Contiguity.COLUMNS));
				
				
				
				System.out.println("Tric " + (k+1) + " - Generating contexts...");
				bicsConts[k] = generate(numContsTrics, numConts, overlappingColsPerc, bicsConts, bicsWithOverlap,
						bicsExcluded, tricStructure.getContiguity().equals(Contiguity.CONTEXTS));
				System.out.println("Contexts: " + bicsConts[k].length);
				
				System.out.println("Tric " + (k+1) + " - Generating rows...");
				bicsRows[k] = generate(numRowsTrics, numRows, overlappingRowsPerc, bicsRows, bicsWithOverlap,
						bicsExcluded, false);
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
				
				System.out.println("Tric " + (k+1) + " - Generating columns...");
				bicsCols[k] = generateNonOverlappingOthers(numColsTrics, numCols, chosenCols, tricStructure.getContiguity().equals(Contiguity.COLUMNS));
				System.out.println("Columns: " + bicsCols[k].length);
				
				System.out.println("Tric " + (k+1) + " - Generating contexts...");
				bicsConts[k] = generateNonOverlappingOthers(numContsTrics, numConts, chosenConts, tricStructure.getContiguity().equals(Contiguity.CONTEXTS));
				System.out.println("Contexts: " + bicsConts[k].length);
				
				System.out.println("Tric " + (k+1) + " - Generating rows...");
				bicsRows[k] = generateNonOverlappingRows(numRowsTrics, numRows, bicsCols[k], bicsConts[k], data.getElements());
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

			if(hasSpace) {
	
				System.out.println("Tric " + (k+1) + " - Has space, lets plant the patterns");
				
				for (Integer c : bicsCols[k])
					chosenCols.add(c);
				
				for (Integer c : bicsConts[k])
					chosenConts.add(c);
				
				Arrays.parallelSort(bicsRows[k]);
				Arrays.parallelSort(bicsCols[k]);
				Arrays.parallelSort(bicsConts[k]);
				
				SymbolicBicluster bicK = new SymbolicBicluster(BicMath.getSet(bicsRows[k]), BicMath.getSet(bicsCols[k]), rowType, columnType);
				
				SymbolicTricluster tricK;
				
				if(contextType.equals(PatternType.ORDER_PRESERVING))
					tricK = new SymbolicTricluster(k, bicK, contextType, timeProfile);
				else
					tricK = new SymbolicTricluster(k, bicK, contextType);
	
				/** PART VI: generate biclusters coherencies **/
				String[][][] bicsymbols = new String[bicsConts[k].length][bicsRows[k].length][bicsCols[k].length];
				
				if(rowType.equals(PatternType.ORDER_PRESERVING)) {
					bicsymbols = new String[bicsConts[k].length][bicsCols[k].length][bicsRows[k].length];
					Integer[] order = generateOrder(bicsRows[k].length);
					
					for(int ctx = 0; ctx < numContsTrics; ctx++) {
						for(int row = 0; row < numColsTrics; row++) {
							for (int col = 0; col < numRowsTrics; col++)
								bicsymbols[ctx][row][col] = alphabet[random.nextInt(alphabet.length)];
							Arrays.parallelSort(bicsymbols[ctx][row]);
							bicsymbols[ctx][row] = shuffle(order, bicsymbols[ctx][row]);
						}
						tricK.addContext(bicsConts[k][ctx]);
					}
					bicsymbols = transposeMatrix(bicsymbols, "x", "y");
				}
				else if(columnType.equals(PatternType.ORDER_PRESERVING)) {
					Integer[] order = generateOrder(bicsCols[k].length);
					for(int ctx = 0; ctx < numContsTrics; ctx++) {
						for(int row = 0; row < numRowsTrics; row++) {
							for (int col = 0; col < numColsTrics; col++)
								bicsymbols[ctx][row][col] = alphabet[random.nextInt(alphabet.length)];
							Arrays.parallelSort(bicsymbols[ctx][row]);
							bicsymbols[ctx][row] = shuffle(order, bicsymbols[ctx][row]);
						}
						tricK.addContext(bicsConts[k][ctx]);
					}
				}
				else if(contextType.equals(PatternType.ORDER_PRESERVING)) {
					bicsymbols = new String[bicsCols[k].length][bicsRows[k].length][bicsConts[k].length];
					
					Integer[] order = generateOrder(bicsConts[k].length);
					
					for(int ctx = 0; ctx < numColsTrics; ctx++) {
						for(int row = 0; row < numRowsTrics; row++) {
							for (int col = 0; col < numContsTrics; col++) {
								bicsymbols[ctx][row][col] = alphabet[random.nextInt(alphabet.length)];
								
								if(ctx == 0 && row == 0)
									tricK.addContext(bicsConts[k][col]);
							}
							
							if(timeProfile.equals(TimeProfile.RANDOM)) {
								Arrays.parallelSort(bicsymbols[ctx][row]);
								bicsymbols[ctx][row] = shuffle(order, bicsymbols[ctx][row]);
							}
							else if(timeProfile.equals(TimeProfile.MONONICALLY_INCREASING))
								Arrays.parallelSort(bicsymbols[ctx][row]);
							
							else
								Arrays.parallelSort(bicsymbols[ctx][row], Collections.reverseOrder());
						}
					}
					
					bicsymbols = transposeMatrix(bicsymbols, "z", "y");
				}
				else if(rowType.equals(PatternType.CONSTANT) && columnType.equals(PatternType.CONSTANT) && 
						(contextType.equals(PatternType.CONSTANT))) {
					String seed = alphabet[random.nextInt(alphabet.length)];
					for(int ctx = 0; ctx < numContsTrics; ctx++) {
						for(int row = 0; row < numRowsTrics; row++) 
							for (int col = 0; col < numColsTrics; col++)
								bicsymbols[ctx][row][col] = seed;	
						tricK.addContext(bicsConts[k][ctx]);
					}
					tricK.setSeed(bicsymbols[0]);
				}
				else if(rowType.equals(PatternType.CONSTANT) && columnType.equals(PatternType.CONSTANT)) {
					for(int ctx = 0; ctx < numContsTrics; ctx++) {
						String seed = alphabet[random.nextInt(alphabet.length)];
						for(int row = 0; row < numRowsTrics; row++) {
							for (int col = 0; col < numColsTrics; col++)
								bicsymbols[ctx][row][col] = seed;
						}
						tricK.addContext(bicsymbols[ctx], bicsConts[k][ctx]);
					}
					
				}
				else if(columnType.equals(PatternType.CONSTANT) && contextType.equals(PatternType.CONSTANT)) {
					for(int row = 0; row < numRowsTrics; row++) {
						String seed = alphabet[random.nextInt(alphabet.length)];
						for(int ctx = 0; ctx < numContsTrics; ctx++)
							for (int col = 0; col < numColsTrics; col++)
								bicsymbols[ctx][row][col] = seed;
					}
					tricK.setSeed(bicsymbols[0]);
					
					for(int ctx = 0; ctx < numContsTrics; ctx++) {
						tricK.addContext(bicsConts[k][ctx]);
					}
				}
				else if(rowType.equals(PatternType.CONSTANT) && contextType.equals(PatternType.CONSTANT)) {
					for(int col = 0; col < numColsTrics; col++) {
						String seed = alphabet[random.nextInt(alphabet.length)];
						for(int ctx = 0; ctx < numContsTrics; ctx++)
							for (int row = 0; row < numRowsTrics; row++)
								bicsymbols[ctx][row][col] = seed;
					}
					tricK.setSeed(bicsymbols[0]);
					
					for(int ctx = 0; ctx < numContsTrics; ctx++) {
						tricK.addContext(bicsConts[k][ctx]);
					}
				}
				else if(columnType.equals(PatternType.CONSTANT)) {
					for(int ctx = 0; ctx < numContsTrics; ctx++) {
						for (int row = 0; row < numRowsTrics; row++) {
							String seed = alphabet[random.nextInt(alphabet.length)];
							for(int col = 0; col < numColsTrics; col++)
								bicsymbols[ctx][row][col] = seed;
						}
						tricK.addContext(bicsymbols[ctx], bicsConts[k][ctx]);
					}
				}
				else if(rowType.equals(PatternType.CONSTANT)) {
					for(int ctx = 0; ctx < numContsTrics; ctx++) {
						for (int col = 0; col < numColsTrics; col++) {
							String seed = alphabet[random.nextInt(alphabet.length)];
							for(int row = 0; row < numRowsTrics; row++)
								bicsymbols[ctx][row][col] = seed;
						}
						tricK.addContext(bicsymbols[ctx], bicsConts[k][ctx]);
					}
				}
				else{
					for(int row = 0; row < numRowsTrics; row++) {
						for (int col = 0; col < numColsTrics; col++) {
							String seed = alphabet[random.nextInt(alphabet.length)];
							for(int ctx = 0; ctx < numContsTrics; ctx++)
								bicsymbols[ctx][row][col] = seed;
						}
					}
					tricK.setSeed(bicsymbols[0]);
					
					for(int ctx = 0; ctx < numContsTrics; ctx++) {
						tricK.addContext(bicsConts[k][ctx]);
					}
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
							this.data.setMatrixItem(bicsConts[k][ctx], bicsRows[k][row], bicsCols[k][col], bicsymbols[ctx][row][col]);
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
	
	private Integer[] generateOrder(int size) {
		Integer[] order = new Integer[size];
		for(int i = 0; i < size; i++)
			order[i] = i;
		Collections.shuffle(Arrays.asList(order));
		return order;
	}
	
	private  String[] shuffle(Integer[] order, String[] array) {
		
		String[] newArray = new String[array.length];
		
		for(int i = 0; i < order.length; i++) {
			newArray[order[i]] = array[i];
		}
		
		return newArray;
	}
	
	
}