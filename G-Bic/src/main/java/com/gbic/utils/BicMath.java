package com.gbic.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


/** @author Rui Henriques
 *  @version 1.0
 */
public class BicMath {

	/** CORE FUNCTIONS **/
	
	public static double maxindex(double[] vector) {
		double max = -1, index = -1;
		for(int i=0; i < vector.length; i++) 
			if(vector[i]>max) {
				index = i; 
				max = vector[i];
			}
		return index;
	}

	public static ArrayList<Integer> rank(ArrayList<Integer> values){
	    ArrayList<Integer> sortedValues = new ArrayList<Integer>(values);
	    Collections.sort(sortedValues);
	    ArrayList<Integer> ranks = new ArrayList<Integer>();
	    for (int i=0; i<values.size(); i++)
	        ranks.add((Integer) sortedValues.indexOf(values.get(i)));
	    return ranks;
	}
    
	public static long factorial(long n) { return recfact(1, n); }
	static long recfact(long start, long n) {
	    long i;
	    if (n <= 16) { 
	    	long r = start;
	        for (i = start + 1; i < start + n; i++) r *= i;
	        return r;
	    }
	    i = n / 2;
	    return recfact(start, i) * recfact(start + i, n - i);
	}
	
	public static double[][] reshape(double[] A, int m, int n) {
        if(A.length < m*n) throw new IllegalArgumentException("New matrix must be of same area as matix A");
        double[][] B = new double[n][m];
        for(int i = 0, index = 0; i < n; i++) for(int j = 0; j < m; j++) B[i][j] = A[index++];
        return B;
    }
    
    public static double[][] reshape(double[][] A, int m, int n) {
    	int origM = A.length, origN = A[0].length;
    	if(origM*origN != m*n) throw new IllegalArgumentException("New matrix must be of same area as matix A");
    	double[][] B = new double[n][m];
    	double[] A1D = new double[A.length*A[0].length];
    	for(int i = 0, index = 0; i < A.length; i++) for(int j = 0; j < A[0].length; j++) A1D[index++] = A[i][j];
    	for(int i = 0, index = 0; i < m; i++) for(int j = 0; j < n; j++) B[j][i] = A1D[index++];
    	return B;
    }

	public static int[][] reshape(int[][] A, int m, int n) {
    	int origM = A.length, origN = A[0].length;
    	if(origM*origN != m*n) throw new IllegalArgumentException("New matrix must be of same area as matix A");
    	int[][] B = new int[n][m];
    	int[] A1D = new int[A.length*A[0].length];
    	for(int i = 0, index = 0; i < A.length; i++) for(int j = 0; j < A[0].length; j++) A1D[index++] = A[i][j];
    	for(int i = 0, index = 0; i < m; i++) for(int j = 0; j < n; j++) B[j][i] = A1D[index++];
    	return B;
	}
	
	private static int gcd(int a, int b)	{
	    while (b > 0) {
	        int temp = b;
	        b = a % b;
	        a = temp;
	    }
	    return a;
	}

	private static int lcm(int a, int b){
	    return a*(b/gcd(a,b));
	}

	public static int lcmOfLinearVector(int n) {
		int[] vector = new int[n];
		for(int i=1; i<=n; i++) vector[i-1]=i;
		return lcm(vector);
	}

	public static int lcm(int[] vector){
	    int result = vector[0];
	    for(int i=1; i<vector.length; i++)
	    	result = lcm(result,vector[i]);
	    return result;
	}
	public static int lcm(List<Integer> vector) {
		for(int i=vector.size()-1; i>=0; i--) if(vector.get(i)==0) vector.remove(i);
		if(vector.size()==0) return -1;
	    int result = vector.remove(0);
	    for(Integer vecI : vector) result = lcm(result,vecI);
	    return result;
	}
	
	public static double roundOneDecimals(double d) {
		int result = (int) (d*10);
        return ((double)result)/10.0;
	}

	public static double combination(int n, int k){
		//System.out.println("B"+permutation(n)+","+permutation(n-k)+","+permutation(k)+","+permutation(n)/(permutation(k)*permutation(n-k)));
		//System.out.println("COMBIN("+n+","+k+")");
		//double d;
		//if(n<k) d=1/0;
		if(n==k) return 1;
		long a = permutation(n,n-k);
		long b = permutation(k);
		//long c = permutation(n-k);
	    return a/b;
	}

	private static long permutation(int n, int stop) {
	    if(n==stop) return 1;
	    return n*permutation(n-1,stop);
	}

	public static long permutation(int i){
	    if(i==1) return 1;
	    return i*permutation(i-1);
	}

	/** SIMPLE STATISTICS **/
	
	public static double mean(List<Double> vector) {
		double sum = 0;
		for(Double val : vector) sum += val;
		return sum/(vector.size());
	}
	public static double mean(int[] vec) {
		double sum = 0;
		for(int val : vec) sum += val;
		return sum/(double)vec.length;
	}

	public static double std(List<Double> vector) {
		double sum = 0, avg = mean(vector);
		for(Double val : vector) sum += Math.pow(val-avg,2);
		return Math.sqrt(sum/vector.size());
	}
	
	public static double count(List<List<Integer>> dataset) {
		double count = 0;
		for(int i=0, l1=dataset.size(); i<l1; i++)
			for(int j=0, l2=dataset.get(i).size(); j<l2; j++) count++;
		return count;
	}
	public static double meanL(List<List<Double>> matrix) {
		double sum = 0, count = 0;
		for(int i=0, l1=matrix.size(); i<l1; i++){
			for(int j=0, l2=matrix.get(i).size(); j<l2; j++, count++)
				sum += matrix.get(i).get(j);
		}
		return sum/count;
	}

	public static double stdL(List<List<Double>> matrix) {
		double sum = 0, count = 0, avg = meanL(matrix);
		for(int i=0, l1=matrix.size(); i<l1; i++){
			for(int j=0, l2=matrix.get(i).size(); j<l2; j++, count++)
				sum += Math.pow(matrix.get(i).get(j)-avg,2);
		}
		return Math.sqrt(sum/count);
	}
	public static double mean(double[][] matrix) {
		double sum = 0;
		for(int i=0; i < matrix.length; i++) 
			for(int j=0, l=matrix[0].length; j<l; j++)
				sum += matrix[i][j];
		return sum/(matrix.length*matrix[0].length);
	}
	public static double std(double[][] matrix) {
		double sum = 0, avg = mean(matrix);
		for(int i=0; i < matrix.length; i++) 
			for(int j=0, l=matrix[0].length; j<l; j++)
				sum += Math.pow(matrix[i][j]-avg,2);
		return Math.sqrt(sum/(matrix.length*matrix[0].length));
	}
	
	public static double variance(double[][] matrix) {
		double sum = 0, avg = mean(matrix);
		for(int i=0; i < matrix.length; i++) 
			for(int j=0, l=matrix[0].length; j<l; j++)
				sum += Math.pow(matrix[i][j]-avg,2);
		return sum/(matrix.length*matrix[0].length);
	}

	public static int max(int[] dataset) {
		int max=-1000;
		for(int i=0; i<dataset.length; i++) 
			if(dataset[i]>max) max = dataset[i]; 
		return max;
	}
	
	public static int min(int[] dataset) {
		int min=1000;
		for(int i=0; i<dataset.length; i++) 
			if(dataset[i]<min) min = dataset[i]; 
		return min;
	}
	public static int max(int[][] dataset) {
		int max=0;
		for(int i=0; i<dataset.length; i++) 
			for(int j=0, l=dataset[i].length; j<l; j++)
				if(dataset[i][j]>max) max = dataset[i][j]; 
		return max;
	}
	public static double sum(int[] vector) {
		double result=0;
		for(int i=0; i<vector.length; i++) result+=vector[i]; 
		return result;
	}

	public static double sum(double[] vector) {
		double result=0;
		for(int i=0; i<vector.length; i++) result+=vector[i]; 
		return result;
	}

	public static double sum(int[][] matrix) {
		double sum=0;
		for(int i=0; i<matrix.length; i++) 
			for(int j=0, l=matrix[i].length; j<l; j++)
				sum+=matrix[i][j]; 
		return sum;
	}
	
	public static int[][] cutMatrix(int[][] items, int minX, int minY) {
		minX = (minX<0) ? items.length : minX;
		minY = (minY<0) ? items[0].length : minY;
		int[][] result = new int[minX][minY];
		for(int i=0; i<minX; i++) 
			for(int j=0; j<minY; j++)
				result[i][j] = items[i][j];
		return result;
	}

	public static double max(double[] dataset) {
		double max=-1000;
		for(int i=0; i<dataset.length; i++) 
			if(dataset[i]>max) max = dataset[i]; 
		return max;
	}
	
	public static int min(int[][] dataset) {
		int min=1000;
		for(int i=0; i<dataset.length; i++) 
			for(int j=0, l=dataset[i].length; j<l; j++)
				if(dataset[i][j]<min) min = dataset[i][j]; 
		return min;
	}
	
	public static double min(double[][] dataset) {
		double min=1000;
		for(int i=0; i<dataset.length; i++) 
			for(int j=0, l=dataset[i].length; j<l; j++)
				if(dataset[i][j]<min) min = dataset[i][j]; 
		return min;
	}
	
	public static double max(double[][] dataset) {
		double max=-1000;
		for(int i=0; i<dataset.length; i++) 
			for(int j=0, l=dataset[i].length; j<l; j++)
				if(dataset[i][j]>max) max = dataset[i][j]; 
		return max;
	}
	
	public static double min(double[] dataset) {
		double min=1000;
		for(int i=0; i<dataset.length; i++) 
			if(dataset[i]<min) min = dataset[i]; 
		return min;
	}

	public static SortedSet<Integer> getSet(int[] vector) {
		SortedSet<Integer> result = new TreeSet<Integer>();
		for(int i=0, l=vector.length; i<l; i++) result.add(vector[i]);
		return result;
	}
	
	public static SortedSet<Integer> getSet(String[] vector) {
		SortedSet<Integer> result = new TreeSet<Integer>();
		for(int i=0, l=vector.length; i<l; i++) result.add(Integer.valueOf(vector[i]));
		return result;
	}

	public static List<Integer> getList(int[] vector) {
		List<Integer> result = new ArrayList<Integer>();
		for(int i=0, l=vector.length; i<l; i++) result.add(vector[i]);
		return result;
	}

	public static String[][] tanspose(String[][] matrix) {
		String[][] result = new String[matrix[0].length][matrix.length];
    	for(int i=0, l1=matrix.length; i<l1; i++)
		    for(int j=0, l2=matrix[i].length; j<l2; j++)
		    	result[j][i]=matrix[i][j];
		return result;
	}

	public static double sum(List<Integer> values) {
		int result=0;
		for(Integer val : values) result+=val;
		return result;
	}
	public static double countValues(List<Integer> values) {
		Set<Integer> result = new HashSet<Integer>();
		for(Integer val : values) result.add(val);
		return result.size();
	}
	public static double average(List<Integer> values) {
		return sum(values)/(double)values.size();
	}
	public static double standardVariation(List<Integer> vector) {
		double sum = 0, avg = average(vector);
		for(Integer val : vector) sum += Math.pow(val-avg,2);
		return Math.sqrt(sum/vector.size());
	}
	public static int min(List<Integer> values) {
		int min=1000;
		for(Integer val : values) min=Math.min(min, val);
		return min;
	}
	public static int max(List<Integer> values) {
		int max=-1000;
		for(Integer val : values) max=Math.max(max, val);
		return max;
	}

	public static int countWithin(List<Double> values, double min, double max) {
		int count = 0;
		for(Double val : values) if(val>=min && val<max) count++;
		return count;
	}

	public static double max(Double[][][] dataset) {
		double max = Integer.MIN_VALUE;
		for(int i=0; i<dataset.length; i++) 
			for(int j=0, l=dataset[i].length; j<l; j++)
				for(int z = 0; z < dataset[i][j].length; z++)
					if(Double.compare(dataset[i][j][z], max) > 0) max = dataset[i][j][z]; 
		
		return max;
	}
	
	public static double min(Double[][][] dataset) {
		double min = Double.MAX_VALUE;
		for(int i=0; i<dataset.length; i++) 
			for(int j=0, l=dataset[i].length; j<l; j++)
				for(int z = 0; z < dataset[i][j].length; z++)
					if(Double.compare(dataset[i][j][z], min) < 0) min = dataset[i][j][z]; 
		return min;
	}
	
	public static double nthRoot(double A, double N) {
          
        // intially guessing a random number between 
        // 0 and 9 
        double xPre = Math.random() % 10; 
      
        // smaller eps, denotes more accuracy 
        double eps = 0.001; 
      
        // initializing difference between two 
        // roots by INT_MAX 
        double delX = 2147483647; 
      
        // xK denotes current value of x 
        double xK = 0.0; 
      
        // loop untill we reach desired accuracy 
        while (delX > eps) 
        { 
            // calculating current value from previous 
            // value by newton's method 
            xK = ((N - 1.0) * xPre + 
            A / Math.pow(xPre, N - 1)) / N; 
            delX = Math.abs(xK - xPre); 
            xPre = xK; 
        } 
      
        return xK; 
    } 
}
