/**
 * IOUtils Class
 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @version 1.0
 */
package com.gbic.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gbic.domain.dataset.HeterogeneousDataset;
import com.gbic.domain.dataset.NumericDataset;
import com.gbic.domain.dataset.SymbolicDataset;
import com.gbic.types.PatternType;

public class IOUtils {

	private IOUtils() {}

	public static String matrixToString(double[][] matrix) {
		StringBuffer result = new StringBuffer();
		result.append("X\t"); 
		for(int j=0, l=matrix[0].length-1; j<l; j++) result.append("y"+j+"\t");
		result.append("y"+(matrix[0].length-1)+"\n"); 
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		for(int i=0, l1=matrix.length, l2=matrix[0].length; i<l1; i++){
			result.append("x"+i+"\t");
			for(int j=0; j<l2; j++) result.append(df.format(matrix[i][j])+"\t");
			result.replace(result.length()-1, result.length(),"\n"); 
		}
		return result.toString();
	}

	public static String matrixToString(int[][] matrix) {
		StringBuffer result = new StringBuffer();
		result.append("X\t"); 
		for(int j=0, l=matrix[0].length-1; j<l; j++) result.append("y"+j+"\t");
		result.append("y"+(matrix[0].length-1)+"\n"); 
		for(int i=0, l1=matrix.length, l2=matrix[0].length; i<l1; i++){
			result.append("x"+i+"\t");
			for(int j=0; j<l2; j++) result.append(matrix[i][j]+"\t");
			result.replace(result.length()-1, result.length(),"\n"); 
		}
		return result.toString();
	}

	public static String matrixToString(String[][][] matrix) {

		StringBuffer result = new StringBuffer();

		result.append("X\t"); 

		for(int y=0; y< matrix[0].length; y++) result.append("y"+y+"\t");
		result.append("z \n"); 

		for(int ctx = 0; ctx < matrix[0][0].length; ctx++) {
			for(int row = 0; row < matrix.length; row++){
				result.append("x"+row+"\t");
				for(int col = 0; col < matrix[0].length; col ++) 
					result.append(matrix[row][col][ctx]+"\t");
				result.append(ctx + "\n");
				//result.replace(result.length()-1, result.length(),"\n"); 
			}
		}

		return result.toString();
	}

	/**
	 * Converts the dataset into a string oriented by columns
	 * @param dataset The generated dataset
	 * @param threshold How many rows to convert
	 * @param step In which row to start printing
	 * @param printHeader boolean that indicates if the header should be added
	 * @return
	 */
	public static String matrixToStringColOriented(NumericDataset dataset, int threshold, int step, boolean printHeader) {

		StringBuilder result = new StringBuilder();
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setGroupingUsed(false);

		System.out.println("Writing dataset file: " + (((double) step) / (dataset.getNumRows() / threshold) * 100) + "%");
		
		if(printHeader) {
			result.append("X\t"); 

			//O Header é sempre igual para cada excerto logo podemos guardar numa variavel local/global
				for(int y=0; y< dataset.getNumCols(); y++) {
					if(y == dataset.getNumCols() - 1)
						result.append("y" + y + "\n");
					else
						result.append("y" + y + "\t");  			
				}
		}    	

		int max = ((threshold * (step + 1)) > dataset.getNumRows()) ? dataset.getNumRows() - (threshold * step) : threshold;
		for(int row = 0; row < max; row++){
			result.append("x"+ (step * threshold + row) +"\t");
			for(int col = 0; col < dataset.getNumCols(); col ++) {
				if(dataset.existsMatrixItem(step * threshold + row, col)) {
					if(dataset.getMatrixItem(step * threshold + row, col).intValue() == Integer.MIN_VALUE) {
						result.append("\t");
					}
					else
						result.append(df.format(dataset.getMatrixItem(step * threshold + row, col))+"\t");
				}
				else {
					result.append(df.format(dataset.generateBackgroundValue()) + "\t");
				}	
			}
			result.replace(result.length()-1, result.length(),"\n"); 
		}
		return result.toString();
	}

	/**
	 * Converts the dataset into a string oriented by columns
	 * @param dataset The generated dataset
	 * @param threshold How many rows to convert
	 * @param step In which row to start printing
	 * @param printHeader boolean that indicates if the header should be added
	 * @return
	 */
	public static String matrixToStringColOriented(SymbolicDataset dataset, int threshold, int step, boolean printHeade) {

		StringBuilder result = new StringBuilder();

		System.out.println("Writing dataset file: " + (((double) step) / (dataset.getNumRows() / threshold) * 100) + "%");
		
		if(printHeade) {
			result.append("X\t"); 
			for(int y=0; y< dataset.getNumCols(); y++) {
				if(y == dataset.getNumCols() - 1)
					result.append("y" + y + "\n");
				else
					result.append("y" + y + "\t");  			
			}
		}    	

		int max = ((threshold * (step + 1)) > dataset.getNumRows()) ? dataset.getNumRows() - (threshold * step) : threshold;
		for(int row = 0; row < max; row++){
			result.append("x"+ (step * threshold + row) +"\t");
			for(int col = 0; col < dataset.getNumCols(); col ++) {
				if(dataset.existsMatrixItem(step * threshold + row, col))
					result.append(dataset.getMatrixItem(step * threshold + row, col) + "\t");
				else
					result.append(dataset.generateBackgroundValue() + "\t");
			}
			
			result.replace(result.length()-1, result.length(),"\n"); 
		}

		return result.toString();
	}

	
	/**
	 * Converts the dataset into a string oriented by columns
	 * @param dataset The generated dataset
	 * @param threshold How many rows to convert
	 * @param step In which row to start printing
	 * @param printHeader boolean that indicates if the header should be added
	 * @return
	 */
	public static String matrixToStringColOriented(HeterogeneousDataset dataset, int threshold, int step, boolean printHeader) {

		StringBuilder result = new StringBuilder();
		DecimalFormat df = new DecimalFormat();
		int decimal = (dataset.isRealValued()) ? 2 : 0;
		df.setMaximumFractionDigits(decimal);
		df.setGroupingUsed(false);

		System.out.println("Writing dataset file: " + (((double) step) / (dataset.getNumRows() / threshold) * 100) + "%");
		
		if(printHeader) {
			result.append("X\t"); 

			//O Header é sempre igual para cada excerto logo podemos guardar numa variavel local/global
				for(int y=0; y< dataset.getNumCols(); y++) {
					if(y == dataset.getNumCols() - 1)
						result.append("y" + y + "\n");
					else
						result.append("y" + y + "\t");  			
				}
		}    	

		int max = ((threshold * (step + 1)) > dataset.getNumRows()) ? dataset.getNumRows() - (threshold * step) : threshold;
		for(int row = 0; row < max; row++){
			result.append("x"+ (step * threshold + row) +"\t");
			for(int col = 0; col < dataset.getNumCols(); col ++) {
				
				if(dataset.isSymbolicFeature(col)) {
					if(dataset.existsSymbolicElement(step * threshold + row, col))
						result.append(dataset.getSymbolicElement(step * threshold + row, col) + "\t");
					else
						result.append(dataset.generateSymbolicBackgroundValue() + "\t");
				}
				else {
					if(dataset.existsNumericElement(step * threshold + row, col)) {
						if(dataset.getNumericElement(step * threshold + row, col).intValue() == Integer.MIN_VALUE) {
							result.append("\t");
						}
						else
							result.append(df.format(dataset.getNumericElement(step * threshold + row, col))+"\t");
					}
					else {
						result.append(df.format(dataset.generateNumericBackgroundValue()) + "\t");
					}
				}				
			}
			result.replace(result.length()-1, result.length(),"\n"); 
		}
		return result.toString();
	}
	
	/**
	 * Writes the dataset into a file
	 * @param path The file path
	 * @param name The file name
	 * @param content The content of the dataset
	 * @throws Exception
	 */
	public static void writeFile(String path, String name, String content, boolean append) throws Exception {
		FileWriter fstream = new FileWriter(path+name, append);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(content);
		out.close();
	}

	public static String printSymbolicBicluster(Map<String, String> matrix, Set<Integer> rows, Set<Integer> cols) {

		StringBuilder result = new StringBuilder();

		result.append("X\t"); 
		
		Integer[] rowsArray = new Integer[rows.size()];
	    rows.toArray(rowsArray);
		
		Integer[] colsArray = new Integer[cols.size()];
	    cols.toArray(colsArray);
		
		for(int y = 0; y < colsArray.length; y++) {
			if((y == colsArray.length - 1))
				result.append("y" + colsArray[y] + "\n");
			else
				result.append("y" + colsArray[y] + "\t");  			
		}
	
		  
		for(int row = 0; row < rowsArray.length; row++){
			result.append("x"+ rowsArray[row] +"\t");
			
			for(int col = 0; col < colsArray.length; col ++) 
				result.append(matrix.get(rowsArray[row] + ":" + colsArray[col]) + "\t");
			
			result.replace(result.length()-1, result.length(),"\n"); 
		}
		
		return result.toString();
	}
	
	public static String printNumericBicluster(Map<String, ? extends Number> matrix, Set<Integer> rows, Set<Integer> cols) {

		DecimalFormat df = new DecimalFormat("#.##");
		df.setMaximumFractionDigits(2);
		StringBuilder result = new StringBuilder();

		result.append("X\t"); 
		
		Integer[] rowsArray = new Integer[rows.size()];
	    rows.toArray(rowsArray);
		
		Integer[] colsArray = new Integer[cols.size()];
	    cols.toArray(colsArray);
		
		for(int y = 0; y < colsArray.length; y++) {
			if((y == colsArray.length - 1))
				result.append("y" + colsArray[y] + "\n");
			else
				result.append("y" + colsArray[y] + "\t");  			
		}
	
		  
		for(int row = 0; row < rowsArray.length; row++){
			result.append("x"+ rowsArray[row] +"\t");
			
			for(int col = 0; col < colsArray.length; col ++)
				if(matrix.get(rowsArray[row] + ":" + colsArray[col]).intValue() == Integer.MIN_VALUE)
					result.append("\t");
				else
					result.append(df.format(matrix.get(rowsArray[row] + ":" + colsArray[col])) + "\t");
			
			result.replace(result.length()-1, result.length(),"\n"); 
		}
		
		return result.toString();
	}
	
	public static String printMixedBicluster(Map<String, ? extends Number> numericMatrix, Map<String, String> symbolicMatrix,
			Set<Integer> rows, Set<Integer> cols) {

		DecimalFormat df = new DecimalFormat("#.##");
		df.setMaximumFractionDigits(2);
		StringBuilder result = new StringBuilder();

		result.append("X\t"); 
		
		Integer[] rowsArray = new Integer[rows.size()];
	    rows.toArray(rowsArray);
		
		Integer[] colsArray = new Integer[cols.size()];
	    cols.toArray(colsArray);
		
		for(int y = 0; y < colsArray.length; y++) {
			if((y == colsArray.length - 1))
				result.append("y" + colsArray[y] + "\n");
			else
				result.append("y" + colsArray[y] + "\t");  			
		}
	
		  
		for(int row = 0; row < rowsArray.length; row++){
			result.append("x"+ rowsArray[row] +"\t");
			
			for(int col = 0; col < colsArray.length; col ++) {
				if(numericMatrix.containsKey(rowsArray[row] + ":" + colsArray[col])) {
					if(numericMatrix.get(rowsArray[row] + ":" + colsArray[col]).intValue() == Integer.MIN_VALUE)
						result.append("\t");
					else
						result.append(df.format(numericMatrix.get(rowsArray[row] + ":" + colsArray[col])) + "\t");
				}
				else {
					result.append(symbolicMatrix.get(rowsArray[row] + ":" + colsArray[col]) + "\t");
				}
				
			}
			
			result.replace(result.length()-1, result.length(),"\n"); 
		}
		
		return result.toString();
	}
}
