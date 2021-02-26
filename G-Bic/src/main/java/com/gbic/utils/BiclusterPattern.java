package com.gbic.utils;

import com.gbic.types.BiclusterType;
import com.gbic.types.PatternType;
import com.gbic.types.TimeProfile;

public abstract class BiclusterPattern {
	
	BiclusterType bicType;
	
	public BiclusterPattern(BiclusterType type) {
		this.bicType = type;
	}
	
	public BiclusterType getBiclusterType() {
		return this.bicType;
	}
	
	public abstract String toString();
}
