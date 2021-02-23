package com.gbic.utils;

import com.gbic.exceptions.InvalidInputException;
import com.gbic.types.Distribution;
import com.gbic.types.PatternType;
import com.gbic.types.PlaidCoherency;

import java.util.ArrayList;
import java.util.List;

public class InputValidation {
	
	private InputValidation() {}

	public static String validateMissingNoiseAndErrorsOnPlantedBics(double missingPercOnPlantedBics,
			double noisePercOnPlantedBics, double errorPercOnPlantedBics, int noiseDeviation, int length){
		
		StringBuilder messages = new StringBuilder();
		
		if(Double.compare(missingPercOnPlantedBics, 0) < 0 || Double.compare(missingPercOnPlantedBics, 100) > 0)
			messages.append("(Quality) Error: The percentage of missing values on bics cannot be lower than 0 or higher than 100!\n");
		
		if(Double.compare(noisePercOnPlantedBics, 0) < 0 || Double.compare(noisePercOnPlantedBics, 100) > 0)
			messages.append("(Quality) Error: The percentage of noise on bics cannot be lower than 0 or higher than 100!\n");
		
		if(Double.compare(errorPercOnPlantedBics, 0) < 0 || Double.compare(errorPercOnPlantedBics, 100) > 0)
			messages.append("(Quality) Error: The percentage of errors on bics cannot be lower than 0 or higher than 100!\n");
		
		if(Double.compare(noiseDeviation, 0) < 0 || Double.compare(noiseDeviation, length) > 0)
			messages.append("(Quality) Error: The noise deviation value must to between 0 and the size of the alphabet!\n");
		
		if(Double.compare(errorPercOnPlantedBics, 0.0) > 0
				&&((length / 2) + (noiseDeviation + 1) > (length - 1)) 
				&& ((length / 2) - (noiseDeviation + 1) < 0))
			messages.append("(Quality) Error: NoiseDeviation is to high to allow for errors. It is impossible to replace the middle value of the alphabet for other that"
					+ " respects the error constraint!\n");
		
		if(messages.length() == 0 && Double.compare(missingPercOnPlantedBics + noisePercOnPlantedBics + errorPercOnPlantedBics, 100) > 0)
			messages.append("(Quality) Error: The sum of the percentages of missing, noise and error elements on bics should be lower or equal to 100!\n");
		
		return messages.toString();
	}

	public static String validateMissingNoiseAndErrorsOnPlantedBics(double missingPercOnPlantedBics,
			double noisePercOnPlantedBics, double errorPercOnPlantedBics, double noiseDeviation, double min, double max){
		
		StringBuilder messages = new StringBuilder();
		
		if(Double.compare(missingPercOnPlantedBics, 0) < 0 || Double.compare(missingPercOnPlantedBics, 100) > 0)
			messages.append("(Quality) Error: The percentage of missing values on bics cannot be lower than 0 or higher than 100!");
		
		if(Double.compare(noisePercOnPlantedBics, 0) < 0 || Double.compare(noisePercOnPlantedBics, 100) > 0)
			messages.append("(Quality) Error: The percentage of noise on bics cannot be lower than 0 or higher than 100!");
		
		if(Double.compare(errorPercOnPlantedBics, 0) < 0 || Double.compare(errorPercOnPlantedBics, 100) > 0)
			messages.append("(Quality) Error: The percentage of errors on bics cannot be lower than 0 or higher than 100!");
		
		if(Double.compare(noiseDeviation, 0) < 0 || Double.compare(noiseDeviation, max) > 0)
			messages.append("(Quality) Error: The noise deviation value must to between 0 and max value of the alphabet!");
		
		if(Double.compare(errorPercOnPlantedBics, 0.0) > 0
				&&(Double.compare(Math.round((max - min) / 2) + (noiseDeviation + 1), max) > 0) 
				&& (((max- min) / 2) - (noiseDeviation + 1) < 0))
			messages.append("(Quality) Error: NoiseDeviation is to high to allow for errors. It is impossible to replace the middle value of the alphabet for other that"
					+ " respects the error constraint!");
		
		if(messages.length() == 0 && Double.compare(missingPercOnPlantedBics + noisePercOnPlantedBics + errorPercOnPlantedBics, 100) > 0)
			messages.append("(Quality) Error: The sum of the percentages of missing, noise and error elements on bics should be lower or equal to 100!");
		
		return messages.toString();
	}
	
	public static String validateMissingNoiseAndErrorsOnBackground(double missingPercOnBackground,
			double noisePercOnBackground, double errorPercOnBackground){
		
		StringBuilder messages = new StringBuilder();
		
		if(Double.compare(missingPercOnBackground, 0) < 0 || Double.compare(missingPercOnBackground, 100) > 0)
			messages.append("(Quality) Error: The percentage of missing values on background cannot be lower than 0 or higher than 100!");
		
		if(Double.compare(noisePercOnBackground, 0) < 0 || Double.compare(noisePercOnBackground, 100) > 0)
			messages.append("(Quality) Error: The percentage of noise on background cannot be lower than 0 or higher than 100!");
		
		if(Double.compare(errorPercOnBackground, 0) < 0 || Double.compare(errorPercOnBackground, 100) > 0)
			messages.append("(Quality) Error: The percentage of errors on background cannot be lower than 0 or higher than 100!");
		
		
		if(messages.length() == 0 && Double.compare(missingPercOnBackground + noisePercOnBackground + errorPercOnBackground, 100) > 0)
			messages.append("(Quality) Error: The sum of the percentages of missing, noise and error elements on background should be lower or equal to 100!");
		
		
		return messages.toString();
	}

	public static String validateOverlappingSettings(String coherency, double percOverlappingBics, int maxBicsPerArea, double maxPercOverlappingElements,
			double percOverlappingRows, double percOverlappingCols, int numBics) {
		
		StringBuilder messages = new StringBuilder();
		
		if(!coherency.equals("No Overlapping")) {
			if(Double.compare(percOverlappingBics, 0) < 0 || Double.compare(percOverlappingBics, 100) > 0)
				messages.append("(Overlapping) Error: The percentage of overlapping biclusters cannot be lower than 0 or greater than 100!");
			
			if(maxBicsPerArea > numBics)
				messages.append("(Overlapping) Error: The maximum number of overlapping biclusters cannot be greater that the number of biclusters"
						+ " on the dataset!");
			
			if(Double.compare(percOverlappingRows, 0) < 0 || Double.compare(percOverlappingRows, 100) > 0)
				messages.append("(Overlapping) Error: The percentage of overlapping rows cannot be lower than 0 or greater than 100!");
			
			if(Double.compare(percOverlappingCols, 0) < 0 || Double.compare(percOverlappingCols, 100) > 0)
				messages.append("(Overlapping) Error: The percentage of overlapping columns cannot be lower than 0 or greater than 100!");
		}
		
		
		return messages.toString();
	}

	public static String validateBiclusterStructure(int numRows, int numCols, String rowDist, double row1, double row2,
			String colDist, double col1, double col2) {
		
		StringBuilder messages = new StringBuilder();
		
		//Rows
		if(rowDist.equals("Uniform")) {
			
			if(Double.compare(row1, row2) > 0)
				messages.append("(Bicluster Structure) Error: Rows param2 should be greater or equal to param1!");
			
			if(Double.compare(row1, 1) < 0)
				messages.append("(Bicluster Structure) Error: Rows param1 should be greater than 0!");
			
			if(Double.compare(row2, numRows) > 0)
				messages.append("(Bicluster Structure) Error: Rows param2 should be less than the number of rows in the dataset!");			
		}
		else {
			if(Double.compare(row1, 0.0) < 0 || Double.compare(row2, 0.0) < 0)
				messages.append("(Bicluster Structure) Error: Rows params cannot be negative!");
		}
		
		//Columns
		if(colDist.equals("Uniform")) {
			if(Double.compare(col1, col2) > 0)
				messages.append("(Bicluster Structure) Error: Columns param2 should be greater or equal to param1!");
			
			if(Double.compare(col1, 1) < 0)
				messages.append("(Bicluster Structure) Error: Columns param1 should be greater than 0!");
			
			if(Double.compare(col2, numCols) > 0)
				messages.append("(Bicluster Structure) Error: Columns param2 should be less than the number of columns in the dataset!");
		}
		else {
			if(Double.compare(col1, 0.0) < 0 || Double.compare(col2, 0.0) < 0)
				messages.append("(Bicluster Structure) Error: Columns params cannot be negative!");
		}
		
		return messages.toString();
	}

	public static void validateDatasetSettings(int numRows, int numCols, int numBics, int alphabetL) throws InvalidInputException {
		
		if(numRows < 1 || numCols < 1 || alphabetL < 1)
			throw new InvalidInputException("Number of rows/columns/contexts, or alphabet length, cannot be lower than 1!");
		
		if(numBics < 0)
			throw new InvalidInputException("Number of desired biclusters cannot be lower than 0!");
		
	}
	
	public static String validateDatasetSettings(String numRows, String numCols, String minValue, String maxValue) {
		
		StringBuilder messages = new StringBuilder();
		
		messages.append(validateDatasetSettings(numRows, numCols));
		
		if(minValue.isEmpty())
			messages.append("(Dataset Properties) Error: Min value must be defined!\n");
		else if(!isDouble(minValue))
			messages.append("(Dataset Properties) Error: Min value must real valued!\n");
		else if(maxValue.isEmpty())
			messages.append("(Dataset Properties) Error: Max value must be defined!\n");
		else if(!isDouble(maxValue))
			messages.append("(Dataset Properties) Error: Min value must be defined!\n");
		else if(Double.compare(Double.parseDouble(minValue), Double.parseDouble(maxValue)) >= 0)
			messages.append("(Dataset Properties) Error: Max value must be higher than Min value!\n");
		
		return messages.toString();
	}
	
	public static String validateDatasetSettings(String numRows, String numCols, String alphabetSize, List<String> symbols) {
		
		StringBuilder messages = new StringBuilder();
		
		messages.append(validateDatasetSettings(numRows, numCols));
		
		if(alphabetSize != null) {
			
			if(alphabetSize.isEmpty())
				messages.append("(Dataset Properties) Error: Number of Symbols must be defined!\n");
			else if(!isInteger(alphabetSize))
				messages.append("(Dataset Properties) Error: Number of Symbols must be an integer value!\n");
			else if(Integer.parseInt(alphabetSize) < 2)
				messages.append("(Dataset Properties) Error: Invalid Number of Symbols!\n");
		}
		else {
			if(symbols.isEmpty())
				messages.append("(Dataset Properties) Error: The Symbol's list must be defined! Each symbol should be separated by a comma. Ex: \"1,2,3,..,10\"!\n");
			else if(symbols.size() < 2)
				messages.append("(Dataset Properties) Error: You have to define at least two symbols\n");
		}
		
		return messages.toString();
	}
	
	private static String validateDatasetSettings(String numRows, String numCols){
		
		StringBuilder messages = new StringBuilder();
		
		if(numRows.isEmpty())
			messages.append("(Dataset Properties) Error: Number of Rows must be defined!\n");
		
		if(numCols.isEmpty())
			messages.append("(Dataset Properties) Error: Number of Columns must be defined!\n");
		
		if(Integer.parseInt(numRows) < 1 || Integer.parseInt(numCols) < 1)
			messages.append("(Dataset Properties) Error: Number of rows/columns/contexts cannot be lower than 1!\n");
		
		if((Integer.parseInt(numRows) == 1 && Integer.parseInt(numCols) == 1) || 
				(Integer.parseInt(numRows) == 1) || 
				(Integer.parseInt(numCols) == 1))
			messages.append("(Dataset Properties) Error: At least two of the dataset's dimensions must larger than 1!\n");
			
		return messages.toString();
		
	}
	
	public static String validateBackgroundSettings(String mean, String stdDev){
		
		StringBuilder messages = new StringBuilder();
		double meanD = 0;
		double stdDevD = 0;
		
		if(mean.isEmpty())
			messages.append("(Dataset Properties) Error: The distribution's mean parameter must be defined!\n");
		else if(!isDouble(mean))
			messages.append("(Dataset Properties) Error: The distribution's mean parameter must be real valued\n");
		else
			meanD = Double.parseDouble(mean);
		
		if(stdDev.isEmpty())
			messages.append("(Dataset Properties) Error: The distribution's standard deviation parameter must be defined!\n");
		else if(!isDouble(stdDev))
			messages.append("(Dataset Properties) Error: The distribution's standard deviation parameter must be real valued\n");
		else
			stdDevD = Double.parseDouble(stdDev);
		
		if(Double.compare(meanD, 0.0) < 0 || Double.compare(stdDevD, 0.0) < 0)
			messages.append("The distribution parameters for the dataset's background cannot be negative!");
		
		return messages.toString();
	}
	
	public static String validateBackgroundSettings(List<DiscreteProbabilitiesTableView> probs){
		
		StringBuilder messages = new StringBuilder();
		double[] probsArray = new double[probs.size()];
		int pIndex = 0;
		
		for(DiscreteProbabilitiesTableView p : probs) {
			if(p.getProb().isBlank() || !isDouble(p.getProb()))
				messages.append("(Dataset Properties) Error: Probability for symbol '" + p.getSymbol() + "' must be real valued\n");
			else
				probsArray[pIndex++] = Double.parseDouble(p.getProb());
		}
		
		double sum = 0.0;
		
		if(pIndex == probsArray.length - 1) {
			for(double p : probsArray)
				sum += p;
			if(Double.compare(Math.abs(sum - 1.0), 0.001) > 0)
				messages.append("The sum of background symbol's probabilites should be equal to 1.0!");	
		}
		
		return messages.toString();
	}
	
	public static void validatePatterns(List<BiclusterPattern> patterns) throws InvalidInputException {
		
		boolean valid = true;
		
		for(int i = 0; i < patterns.size(); i++) {
			
			if(patterns.get(i) instanceof SingleBiclusterPattern) {
				SingleBiclusterPattern p = (SingleBiclusterPattern) patterns.get(i);
				
				if(p.contains(PatternType.ADDITIVE) || p.contains(PatternType.MULTIPLICATIVE))
					throw new InvalidInputException("Invalid pattern on symbolic dataset (additive or multiplicative patterns"
							+ " are not allowed!)");
			}
		}
		
	}
	
	private static boolean isInteger(String num) {
		
		boolean res = true;
		
	    try {
	        int d = Integer.parseInt(num);
	    } catch (NumberFormatException nfe) {
	        res = false;
	    }
	    
	    return res;
	}
	    
	private static boolean isDouble(String num) {
		
		boolean res = true;
		
	    try {
	        double d = Double.parseDouble(num);
	    } catch (NumberFormatException nfe) {
	        res = false;
	    }
	    
	    return res;
	}
}
