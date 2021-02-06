package com.gbic.tests;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.gbic.domain.dataset.Dataset;
import com.gbic.domain.dataset.NumericDataset;
import com.gbic.domain.dataset.SymbolicDataset;
import com.gbic.domain.tricluster.NumericTricluster;
import com.gbic.domain.tricluster.SymbolicTricluster;
import com.gbic.domain.tricluster.Tricluster;
import com.gbic.exceptions.OutputErrorException;
import com.gbic.types.Contiguity;
import com.gbic.utils.OverlappingSettings;

public class Tests {

	protected static void testMaxTricsOnOverlappedArea(SymbolicDataset generatedDataset, OverlappingSettings overlapping,
			int numTrics) throws OutputErrorException {
		
		Set<String> elems =generatedDataset.getElements();
		for(String elem : elems) {
			List<Integer> trics = generatedDataset.getTricsByElem(elem);
			if(trics.size() > overlapping.getMaxTricsPerOverlappedArea())
				throw new OutputErrorException("Max Trics on overlapped area restriction exceeded! Element = " + elem + 
						" #Trics = " + trics.size());		
		}
	}

	
	protected static void testMissingNoiseError(Dataset generatedDataset) {


		//** TEST MISSINGS, NOISE and ERRORS planted **
		System.out.println("Dataset total size: " + generatedDataset.getSize());
		System.out.println("Background size: " + generatedDataset.getBackgroundSize());


		/*
		System.out.println("Background -> " + "Missings=" +  generatedDataset.getNumberOfMissings() + " Noisy=" + generatedDataset.getNumberOfNoisy() +
				" Errors=" + generatedDataset.getNumberOfErrors());		
		 */
		/*
		System.out.println("\n**Found in Background**");
		for(SymbolicTricluster t : generatedDataset.getPlantedBics()) {
			int numFoundMissings = 0;
			int numFoundNoisy = 0;
			int numFoundErrors = 0;
			List<String> elements = generatedDataset.getTriclusterElements(t.getId());

			for(int ctx = 0; ctx < generatedDataset.getNumContexts(); ctx++) {
				for(int row = 0; row < generatedDataset.getNumRows(); row++) {
					for(int col = 0; col < generatedDataset.getNumCols(); col++) {
						if(!generatedDataset.getElements().contains(ctx + ":" + row + ":" + col)) {
							String value = generatedDataset.getMatrixItem(ctx, row, col);
							if(value.equals("M"))
								numFoundMissings++;
							else if(value.equals("N"))
								numFoundNoisy++;
							else if(value.equals("E"))
								numFoundErrors++;
						}
					}
				}
			}
		}
		 */
		System.out.println("Background -> Missings=" +  generatedDataset.getNumberOfMissings() + " (" + ((double) generatedDataset.getNumberOfMissings() / generatedDataset.getBackgroundSize()) + "%)" 
				+ " Noisy=" + generatedDataset.getNumberOfNoisy() + " (" + ((double) generatedDataset.getNumberOfNoisy() / generatedDataset.getBackgroundSize()) + "%)"
				+ " Errors=" + generatedDataset.getNumberOfErrors() +  "(" + ((double) generatedDataset.getNumberOfErrors() / generatedDataset.getBackgroundSize()) + "%)");

		/*
		System.out.println("**Expected in Trics**");
		for(SymbolicTricluster t : generatedDataset.getPlantedBics()) {
			System.out.println("Tricluster " + t.getId() + ": Missings=" +  t.getNumberOfMissings() + " Noisy=" + t.getNumberOfNoisy() +
					" Errors=" + t.getNumberOfErrors());
		}
		 */
		
		System.out.println("\n**Found in Trics**");
		
		List<Tricluster> trics;
		
		if(generatedDataset instanceof SymbolicDataset) {
			
			for(SymbolicTricluster t : ((SymbolicDataset) generatedDataset).getPlantedTrics()) {

				List<String> elements = generatedDataset.getTriclusterElements(t.getId());
				/*
				int numFoundMissings = 0;
				int numFoundNoisy = 0;
				int numFoundErrors = 0;


				for(String elem : elements) {
					String[] positions = elem.split(":");
					int ctx = Integer.parseInt(positions[0]);
					int row = Integer.parseInt(positions[1]);
					int col = Integer.parseInt(positions[2]);

					String value = generatedDataset.getMatrixItem(ctx, row, col);

					if(value.equals("M"))
						numFoundMissings++;
					else if(value.equals("N"))
						numFoundNoisy++;
					else if(value.equals("E"))
						numFoundErrors++;
				}
				 */
				/*
				System.out.println("Tricluster " + t.getId() + ": Missings=" +  t.getNumberOfMissings() + " (" + ((double) t.getNumberOfMissings() / elements.size()) + "%)" 
						+ " Noisy=" + t.getNumberOfNoisy() + " (" + ((double) t.getNumberOfNoisy() / elements.size()) + "%)"
						+ " Errors=" + t.getNumberOfErrors() + " (" + ((double) t.getNumberOfErrors() / elements.size()) + "%)");
						*/
			}
		}
		else {
			for(NumericTricluster<Double> t : ((NumericDataset<Double>) generatedDataset).getPlantedTrics()) {

				List<String> elements = generatedDataset.getTriclusterElements(t.getId());
				/*
				int numFoundMissings = 0;
				int numFoundNoisy = 0;
				int numFoundErrors = 0;


				for(String elem : elements) {
					String[] positions = elem.split(":");
					int ctx = Integer.parseInt(positions[0]);
					int row = Integer.parseInt(positions[1]);
					int col = Integer.parseInt(positions[2]);

					String value = generatedDataset.getMatrixItem(ctx, row, col);

					if(value.equals("M"))
						numFoundMissings++;
					else if(value.equals("N"))
						numFoundNoisy++;
					else if(value.equals("E"))
						numFoundErrors++;
				}
				 */
				/*
				System.out.println("Tricluster " + t.getId() + ": Missings=" +  t.getNumberOfMissings() + " (" + ((double) t.getNumberOfMissings() / elements.size()) + "%)" 
						+ " Noisy=" + t.getNumberOfNoisy() + " (" + ((double) t.getNumberOfNoisy() / elements.size()) + "%)"
						+ " Errors=" + t.getNumberOfErrors() + " (" + ((double) t.getNumberOfErrors() / elements.size()) + "%)");
						*/
			}
		}
		
			
	}


	public static void testPercOfOverlappingTrics(SymbolicDataset generatedDataset, OverlappingSettings overlapping,
			int numTrics) throws OutputErrorException {
		
		int k = (int)(overlapping.getPercOfOverlappingTrics() * numTrics);
		
		for(int i = k; i < numTrics; i++) {
			for(String e : generatedDataset.getTriclusterElements(k)) {
				List<Integer> overlappedTrics = generatedDataset.getTricsByElem(e);
				if(overlappedTrics.size() > 1) {
					throw new OutputErrorException("PercOfOverlappedTrics not respected! Tric ID = " + k + " Element = " +  e +
							" Overlaps with = " + overlappedTrics.toString());
				}
			}
		}
	}


	public static void testContiguity(List<? extends Tricluster> trics, Contiguity contiguity) throws OutputErrorException {
		
		
		int dimSize = 0;
		Integer[] dim = null;
		
		if(contiguity.equals(Contiguity.COLUMNS)) {	
			
			for(Tricluster t : trics) {
				if(t instanceof SymbolicTricluster) {
					dimSize = ((SymbolicTricluster) t).getNumCols();
					dim = new Integer[dimSize];
					((SymbolicTricluster) t).getColumns().toArray(dim);
				}
				else {
					dimSize = ((NumericTricluster<?>) t).getNumCols();
					dim = new Integer[dimSize];
					((NumericTricluster<?>) t).getColumns().toArray(dim);
				}
				
				if((dim[dimSize - 1] - dim[0]) != (dimSize - 1))
					throw new OutputErrorException("Contiguity not respected on columns: " + Arrays.toString(dim));
			
			}
		}
		else if(contiguity.equals(Contiguity.CONTEXTS)) {
			
			
			for(Tricluster t : trics) {
				if(t instanceof SymbolicTricluster) {
					dimSize = ((SymbolicTricluster) t).getNumContexts();
					dim = new Integer[dimSize];
					((SymbolicTricluster) t).getContexts().toArray(dim);
				}
				else {
					dimSize = ((NumericTricluster<?>) t).getNumContexts();
					dim = new Integer[dimSize];
					((NumericTricluster<?>) t).getContexts().toArray(dim);
				}
				
				if((dim[dimSize - 1] - dim[0]) != (dimSize - 1))
					throw new OutputErrorException("Contiguity not respected on contexts: " + Arrays.toString(dim));
			
			}
		}
	}
}
