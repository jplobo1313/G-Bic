package com.gbic.domain.dataset;

import com.gbic.types.BackgroundType;
import com.gbic.types.FeatureType;

public class Feature {

	private int id;
	private FeatureType type;
	private BackgroundType distribution;
	private boolean balanced;
	private boolean order;
	/**
	 * @param type
	 * @param distribution
	 * @param balanced
	 * @param order
	 */
	public Feature(int id, FeatureType type, BackgroundType distribution, boolean balanced, boolean order) {
		this.id = id;
		this.type = type;
		this.distribution = distribution;
		this.balanced = balanced;
		this.order = order;
	}
	
	/**
	 * @return the id
	 */
	public int getFeatureId() {
		return this.id;
	}
	
	/**
	 * Set the feature id
	 * @param id
	 */
	public void setFeatureId(int id) {
		this.id = id;
	}
	
	/**
	 * @return the type
	 */
	public FeatureType getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(FeatureType type) {
		this.type = type;
	}
	/**
	 * @return the distribution
	 */
	public BackgroundType getDistribution() {
		return distribution;
	}
	/**
	 * @param distribution the distribution to set
	 */
	public void setDistribution(BackgroundType distribution) {
		this.distribution = distribution;
	}
	/**
	 * @return the balanced
	 */
	public boolean isBalanced() {
		return balanced;
	}
	/**
	 * @param balanced the balanced to set
	 */
	public void setBalanced(boolean balanced) {
		this.balanced = balanced;
	}
	/**
	 * @return the order
	 */
	public boolean isOrder() {
		return order;
	}
	/**
	 * @param order the order to set
	 */
	public void setOrder(boolean order) {
		this.order = order;
	}
	
	
}
