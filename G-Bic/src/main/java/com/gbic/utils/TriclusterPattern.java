package com.gbic.utils;

import com.gbic.types.PatternType;
import com.gbic.types.TimeProfile;

public class TriclusterPattern {

	PatternType rows;
	PatternType columns;
	PatternType contexts;
	TimeProfile timeProfile;
	
	public TriclusterPattern(PatternType rows, PatternType columns, PatternType contexts) {
		
		this.rows = rows;
		this.columns = columns;
		this.contexts = contexts;
	}
	
	public TriclusterPattern(PatternType rows, PatternType columns, PatternType contexts, TimeProfile timeProfile) {
		
		this.rows = rows;
		this.columns = columns;
		this.contexts = contexts;
		this.timeProfile = timeProfile;
	}
	
	public PatternType getRowsPattern() {
		return this.rows;
	}
	
	public PatternType getColumnsPattern() {
		return this.columns;
	}
	
	public PatternType getContextsPattern() {
		return this.contexts;
	}
	
	public boolean contains(PatternType pattern) {
		return (this.rows.equals(pattern) || this.columns.equals(pattern) || this.contexts.equals(pattern));
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
