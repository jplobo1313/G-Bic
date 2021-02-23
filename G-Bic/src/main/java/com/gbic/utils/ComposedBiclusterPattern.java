package com.gbic.utils;

import org.apache.commons.math3.util.Pair;

import com.gbic.types.BiclusterType;
import com.gbic.types.PatternType;
import com.gbic.types.TimeProfile;

public class ComposedBiclusterPattern extends BiclusterPattern {

	Pair<PatternType, PatternType> numericP;
	Pair<PatternType, PatternType> symbolicP;
	TimeProfile numericTP;
	TimeProfile symbolicTP;
	
	public ComposedBiclusterPattern(BiclusterType type, Pair<PatternType, PatternType> numericP, TimeProfile numericTP, 
			Pair<PatternType, PatternType> symbolicP, TimeProfile symbolicTP) {
		super(type);
		
		this.numericP = numericP;
		this.symbolicP = symbolicP;
		this.numericTP = numericTP;
		this.symbolicTP = symbolicTP;
	}

	/**
	 * @return the numericP
	 */
	public Pair<PatternType, PatternType> getNumericP() {
		return numericP;
	}

	
	/**
	 * @param numericP the numericP to set
	 */
	public void setNumericP(Pair<PatternType, PatternType> numericP) {
		this.numericP = numericP;
	}

	/**
	 * @return the symbolicP
	 */
	public Pair<PatternType, PatternType> getSymbolicP() {
		return symbolicP;
	}

	/**
	 * @param symbolicP the symbolicP to set
	 */
	public void setSymbolicP(Pair<PatternType, PatternType> symbolicP) {
		this.symbolicP = symbolicP;
	}

	/**
	 * @return the numericTP
	 */
	public TimeProfile getNumericTP() {
		return numericTP;
	}

	/**
	 * @param numericTP the numericTP to set
	 */
	public void setNumericTP(TimeProfile numericTP) {
		this.numericTP = numericTP;
	}

	/**
	 * @return the symbolicTP
	 */
	public TimeProfile getSymbolicTP() {
		return symbolicTP;
	}

	/**
	 * @param symbolicTP the symbolicTP to set
	 */
	public void setSymbolicTP(TimeProfile symbolicTP) {
		this.symbolicTP = symbolicTP;
	}
	
	public SingleBiclusterPattern getComponentPattern(boolean symbolic) {
		SingleBiclusterPattern p;
		
		if(symbolic)
			p = new SingleBiclusterPattern(BiclusterType.SYMBOLIC, this.symbolicP.getFirst(), this.symbolicP.getSecond(), this.symbolicTP);
		else
			p = new SingleBiclusterPattern(BiclusterType.NUMERIC, this.numericP.getFirst(), this.numericP.getSecond(), this.numericTP);
		
		return p;
	}
	
}
