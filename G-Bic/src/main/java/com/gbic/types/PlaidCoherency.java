package com.gbic.types;

public enum PlaidCoherency {
	ADDITIVE ("Additive"),
	MULTIPLICATIVE ("Multiplicative"),
	INTERPOLED ("Interpoled"),
	NONE ("None"), 
	NO_OVERLAPPING ("No Overlapping");
	
	private final String name;
	
	PlaidCoherency(String type) {
		name = type;
	}
	
	public boolean equalsName(String otherName) {
 
        return name.equals(otherName);
    }

    public String toString() {
       return this.name;
    }
}
