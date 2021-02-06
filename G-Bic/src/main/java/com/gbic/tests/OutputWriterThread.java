package com.gbic.tests;

import com.gbic.domain.dataset.NumericDataset;
import com.gbic.domain.dataset.SymbolicDataset;
import com.gbic.utils.IOUtils;

public class OutputWriterThread implements Runnable {

	private String path;
	private String name;
	private int step;
	private int threshold;
	
	private NumericDataset numericDataset;
	private SymbolicDataset symbolicDataset;
	
	@Override
	public void run() {
		try {
			
			if(symbolicDataset == null)
				IOUtils.writeFile(path, this.name + "_" + step + ".txt", IOUtils.matrixToStringColOriented(numericDataset, threshold, step, true), false);
			else
				IOUtils.writeFile(path, this.name + "_" + step + ".txt", IOUtils.matrixToStringColOriented(symbolicDataset, threshold, step, true), false);
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Hello from thread for file " + this.step);

	}

	public OutputWriterThread(String path, String name, int step, int threshold, NumericDataset numericDataset) {
		this.path = path;
		this.name = name;
		this.step = step;
		this.threshold = threshold;
		this.numericDataset = numericDataset;
		this.symbolicDataset = null;
	}
	
	public OutputWriterThread(String path, String name, int step, int threshold, SymbolicDataset symbolicDataset) {
		this.path = path;
		this.name = name;
		this.step = step;
		this.threshold = threshold;
		this.symbolicDataset = symbolicDataset;
		this.numericDataset = null;
	}
}
