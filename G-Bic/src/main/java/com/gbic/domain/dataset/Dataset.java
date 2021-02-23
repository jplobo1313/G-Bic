/**
 * Class that represents a dataset object
 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @author Rui Henriques - rmch@tecnico.ulisboa.pt
 */
package com.gbic.domain.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.gbic.domain.bicluster.Bicluster;
import com.gbic.types.Background;

public abstract class Dataset {

	private static final double MISSING = 999999;

	
	private int numRows;
	private int numCols;
	private int numBics;
	
	//private Background background;
	private int backgroundSize;

	private Map<String, List<Integer>> elementsReversed;
	
	private Set<String> missingElements;
	private Set<String> noisyElements;
	private Set<String> errorElements;


	/********************************
	 ********* CONSTRUCTORS *********
	 ********************************/
	
	/**
	 * Constructs a dataset
	 * @param numRows Dataset's number of rows
	 * @param numCols Dataset's number of columns
	 * @param numContexts Dataset's number of contexts
	 * @param background Background object with the background type and parameters
	 */
	public Dataset(int numRows, int numCols, int numBics) {
		
		this.numRows = numRows;
		this.numCols = numCols;
		this.elementsReversed = new HashMap<>();
		this.missingElements = new TreeSet<>();
		this.noisyElements = new TreeSet<>();
		this.errorElements = new TreeSet<>();
		this.numBics = numBics;
		this.backgroundSize = 0;
		
	}
	
	/**
	 * Get dataset size
	 * @return Num of Rows * Num of Cols * Num of Ctxs
	 */
	public int getSize() {
		return this.numRows * this.numCols;
	}
	
	/**
	 * Get the number of dataset's elements that do not belong to any bicluster
	 * @return Dataset Size - Biclusterlusters Size
	 */
	public int getBackgroundSize() {
		
		return this.getSize() - this.backgroundSize;
	}
	
	/**
	 * Adds an element as a missing element
	 * @param e The position of the element in ctx:row:col format
	 */
	public void addMissingElement(String e) {
		this.missingElements.add(e);
	}
	
	/**
	 * Checks if a certain element is a missing element
	 * @param e The element in ctx:row:col format
	 * @return True if the element is in the missing elements set, False otherwise
	 */
	public boolean isMissing(String e) {
		return this.missingElements.contains(e);
	}
	
	/**
	 * Adds an element as a noisy element
	 * @param e The position of the element in ctx:row:col format
	 */
	public void addNoisyElement(String e) {
		this.noisyElements.add(e);
	}
	
	/**
	 * Checks if a certain element is a noisy element
	 * @param e The element in ctx:row:col format
	 * @return True if the element is in the noisy elements set, False otherwise
	 */
	public boolean isNoisy(String e) {
		return this.noisyElements.contains(e);
	}
	
	/**
	 * Adds an element as an error element
	 * @param e The position of the element in ctx:row:col format
	 */
	public void addErrorElement(String e) {
		this.errorElements.add(e);
	}
	
	/**
	 * Checks if a certain elements is an error element
	 * @param e The element in ctx:row:col format
	 * @return True if the element is in the error elements set, False otherwise
	 */
	public boolean isError(String e) {
		return this.errorElements.contains(e);
	}
	
	/**
	 * Get dataet's number of rows
	 * @return the number of rows
	 */
	public int getNumRows() {
		return numRows;
	}

	/**
	 * Get dataet's number of columns
	 * @return the number of columns
	 */
	public int getNumCols() {
		return numCols;
	}

	/**
	 * Get the dataset's number of planted triclusters
	 * @return the number of planted triclusters
	 */
	public int getNumBics() {
		return this.numBics;
	}

	/**
	 * Get the dataset's number of missing elements
	 * @return |Missing elements set|
	 */
	public int getNumberOfMissings() {
		return this.missingElements.size();
	}

	/**
	 * Get the dataset's number of noisy elements
	 * @return |Noisy elements set|
	 */
	public int getNumberOfNoisy() {
		return this.noisyElements.size();
	}
	
	/**
	 * Get the dataset's number of error elements
	 * @return |Error elements set|
	 */
	public int getNumberOfErrors() {
		return this.errorElements.size();
	}
	
	/**
	 * Get the dataset's missing elements
	 * @return The set of missing elements
	 */
	public Set<String> getMissingElements(){
		return this.missingElements;
	}
	
	/**
	 * Get the dataset's noisy elements
	 * @return The set of noisy elements
	 */
	public Set<String> getNoisyElements(){
		return this.noisyElements;
	}
	
	/**
	 * Get the dataset's error elements
	 * @return The set of error elements
	 */
	public Set<String> getErrorElements(){
		return this.errorElements;
	}
	
	/**
	 * Adds an element to a bicluster
	 * @param e The element in ctx:row:col format
	 * @param k The bicluster ID
	 */
	public void addElement(String e, int k) {
		
		if(!this.elementsReversed.containsKey(e)) {
			List<Integer> bics = new ArrayList<>();
			bics.add(k);
			this.elementsReversed.put(e, bics);
		}
		else
			this.elementsReversed.get(e).add(k);
		
		this.backgroundSize++;
		
	}
	
	/**
	 * Get bicluster by its ID
	 * @param id The bicluster ID
	 * @return bicluster with the specified ID
	 */
	public abstract Bicluster getBiclusterById(int id);
	
	/**
	 * Get bicluster's elements
	 * @param id The bicluster ID
	 * @return The list of the bicluster's elements
	 */
	public List<String> getBiclusterElements(int id){
		List<String> elements = new ArrayList<String>();
		
		for(Entry<String, List<Integer>> entry : this.elementsReversed.entrySet()) {
			if(entry.getValue().contains(id))
				elements.add(entry.getKey());
		}
		
		return elements;
	}
	
	/**
	 * Get the set of elements that belong to a any bicluster
	 * @return The set of elements
	 */
	public Set<String> getElements() {

		return elementsReversed.keySet();
	}
	
	/**
	 * Get the bicluster's that contain a certain element
	 * @param e The element in ctx:row:col format
	 * @return List of bicluster to which the element belongs
	 */
	public List<Integer> getBicsByElem(String e){
		
		return this.elementsReversed.get(e);
	}
	
	/**
	 * Checks if a certain element belongs to any bicluster
	 * @param e The element
	 * @return True if the elements belong to a certain element, False otherwise
	 */
	public boolean isPlanted(String e) {
		
		return this.elementsReversed.containsKey(e);
	}
	
	public void destroyElementsMap() {
		this.elementsReversed = null;
	}
	
	/**
	 * Plant missing elements on the dataset
	 * @param percMissing The percentage of missing elements in the background (elements that do not belong to any bicluster)
	 * @param percBicluster The maximum percentage of missing elements in the triclusters
	 */
	public abstract void plantMissingElements(double percMissing, double percBicluster);

	public abstract String getBicsInfo(); 
	
}
