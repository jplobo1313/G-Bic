package com.gbic.utils;

import org.apache.commons.math3.util.Pair;

import com.gbic.types.BiclusterType;
import com.gbic.types.PatternType;
import com.gbic.types.TimeProfile;

public class SingleBiclusterPattern extends BiclusterPattern {
	
	private Pair<PatternType, PatternType> pattern;
	private TimeProfile timeProfile;
	
	public SingleBiclusterPattern(BiclusterType type, PatternType rows, PatternType columns, TimeProfile timeProfile) {
		super(type);
		this.pattern = new Pair<>(rows, columns);
		this.timeProfile = timeProfile;
	}
	
	public PatternType getRowsPattern() {
		return this.pattern.getFirst();
	}
	
	public PatternType getColumnsPattern() {
		return this.pattern.getSecond();
	}
	
	public boolean contains(PatternType pattern) {
		return (this.pattern.getFirst().equals(pattern) || this.pattern.getSecond().equals(pattern));
	}
	
	public Pair<PatternType, PatternType> getPattern(){
		return this.pattern;
	}

	/**
	 * @return the timeProfile
	 */
	public TimeProfile getTimeProfile() {
		return timeProfile;
	}

	/**
	 * @param timeProfile the timeProfile to set
	 */
	public void setTimeProfile(TimeProfile timeProfile) {
		this.timeProfile = timeProfile;
	}

}
