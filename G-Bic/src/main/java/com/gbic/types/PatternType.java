package com.gbic.types;

public enum PatternType {
	CONSTANT ("Constant"), 
	ADDITIVE ("Additive"), 
	MULTIPLICATIVE ("Multiplicative"), 
	ORDER_PRESERVING ("OrderPreserving"), 
	SYMMETRIC ("Symmetric"), 
	NONE ("None");

	private final String name;
	
	PatternType(String type) {
		name = type;
	}
	
	public boolean equalsName(String otherName) {
 
        return name.equals(otherName);
    }

    public String toString() {
       return this.name;
    }
	
}
