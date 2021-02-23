package com.gbic.utils;

import java.util.List;

import org.apache.commons.math3.util.Pair;

import com.gbic.types.BiclusterType;

public class BiclusterPatternWrapper {

	BiclusterType bicType;
	List<BiclusterPattern> singlePatterns;
	List<Pair<BiclusterPattern, BiclusterPattern>> composedPatterns;
	
	/**
	 * @param bicType
	 * @param singlePatterns
	 * @param composedPatters
	 */
	public BiclusterPatternWrapper(BiclusterType bicType, List<BiclusterPattern> singlePatterns,
			List<Pair<BiclusterPattern, BiclusterPattern>> composedPatters) {
		this.bicType = bicType;
		this.singlePatterns = singlePatterns;
		this.composedPatterns = composedPatters;
	}

	/**
	 * @return the bicType
	 */
	public BiclusterType getBicType() {
		return bicType;
	}

	/**
	 * @param bicType the bicType to set
	 */
	public void setBicType(BiclusterType bicType) {
		this.bicType = bicType;
	}

	/**
	 * @return the singlePatterns
	 */
	public List<BiclusterPattern> getSinglePatterns() {
		return singlePatterns;
	}

	/**
	 * @param singlePatterns the singlePatterns to set
	 */
	public void setSinglePatterns(List<BiclusterPattern> singlePatterns) {
		this.singlePatterns = singlePatterns;
	}
	
	public void addSinglePattern(BiclusterPattern p) {
		this.singlePatterns.add(p);
	}

	/**
	 * @return the composedPatters
	 */
	public List<Pair<BiclusterPattern, BiclusterPattern>> getComposedPatters() {
		return composedPatterns;
	}

	/**
	 * @param composedPatters the composedPatters to set
	 */
	public void setComposedPatters(List<Pair<BiclusterPattern, BiclusterPattern>> composedPatters) {
		this.composedPatterns = composedPatters;
	}
	
	public void addComposedPattern(BiclusterPattern numericPattern, BiclusterPattern symbolicPattern) {
		this.composedPatterns.add(new Pair<BiclusterPattern, BiclusterPattern>(numericPattern, symbolicPattern));
	}
	
}
