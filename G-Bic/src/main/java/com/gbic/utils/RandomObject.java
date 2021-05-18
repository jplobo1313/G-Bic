package com.gbic.utils;

import java.util.Random;

public class RandomObject {
	
	private static Random single_instance = null;

	private RandomObject() {}
	
	public static void initialization(int seed) {
		if(seed != -1)
			single_instance = new Random(seed);
		else
			single_instance = new Random();
	}
	
	public static Random getInstance(){
        if (single_instance == null)
            single_instance = new Random();
  
        return single_instance;
    }
}
