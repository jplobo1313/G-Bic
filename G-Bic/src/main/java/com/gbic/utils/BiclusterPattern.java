package com.gbic.utils;

import com.gbic.types.PatternType;
import com.gbic.types.TimeProfile;

public class BiclusterPattern {

	PatternType rows;
	PatternType columns;
	TimeProfile timeProfile;
	
	public BiclusterPattern(PatternType rows, PatternType columns) {
		
		this.rows = rows;
		this.columns = columns;
	}
	
	public BiclusterPattern(PatternType rows, PatternType columns, TimeProfile timeProfile) {
		
		this.rows = rows;
		this.columns = columns;
		this.timeProfile = timeProfile;
	}
	
	public PatternType getRowsPattern() {
		return this.rows;
	}
	
	public PatternType getColumnsPattern() {
		return this.columns;
	}
	
	public boolean contains(PatternType pattern) {
		return (this.rows.equals(pattern) || this.columns.equals(pattern));
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
