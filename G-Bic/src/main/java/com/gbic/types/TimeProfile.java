package com.gbic.types;

public enum TimeProfile {
	RANDOM ("Random"), 
	MONONICALLY_INCREASING ("Monotonically Increasing"), 
	MONONICALLY_DECREASING ("Monotonically Decreasing");
	
	private final String name;
	
	TimeProfile(String type) {
		name = type;
	}
	
	public boolean equalsName(String otherName) {
 
        return name.equals(otherName);
    }

    public String toString() {
       return this.name;
    }
}
