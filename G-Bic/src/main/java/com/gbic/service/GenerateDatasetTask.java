package com.gbic.service;

import javafx.concurrent.Task;

public abstract class GenerateDatasetTask<V> extends Task<V> {
	
	public GBicService gBicService;

	public GenerateDatasetTask(GBicService gBicService) {
	        this.gBicService = gBicService;
	}
	
}
