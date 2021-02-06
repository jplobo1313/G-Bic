package com.gbic.app.GBic.models;

import java.util.Arrays;
import java.util.List;

import com.gbic.service.GBicService;
import com.gbic.service.GBicService.TriclusterPatternWrapper;
import com.gbic.utils.DiscreteProbabilitiesTableView;
import com.gbic.utils.TriclusterPatternTableView;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

public class MenuPrincipalModel {

	// Atributos
	
	//Dataset Settings
	private final IntegerProperty numRows;
	private final IntegerProperty numColumns;
	private final IntegerProperty numContexts;
	private final ObservableList<String> dataType;
	private final SimpleDoubleProperty minValue;
	private final SimpleDoubleProperty maxValue;
	private final ObservableList<String> backgroundType;
	private final SimpleDoubleProperty distMeanValue;
	private final SimpleDoubleProperty distStdValue;
	private final StringProperty dataTypeEscolhido;
	private final StringProperty backgroundTypeEscolhido;
	
	private final ObservableList<String> symbolType;
	private final StringProperty symbolTypeEscolhido;
	private final StringProperty symbolList;
	private final SimpleIntegerProperty numberOfSymbols;
	
	private ObservableList<DiscreteProbabilitiesTableView> discreteProbs;
	
	//Triclusters Properties
	private final IntegerProperty numTrics;
	private final ObservableList<String> rowDistribution;
	private final StringProperty rowDistEscolhida;
	private final SimpleDoubleProperty rowDistParam1;
	private final SimpleDoubleProperty rowDistParam2;
	private final ObservableList<String> columnDistribution;
	private final StringProperty columnDistEscolhida;
	private final SimpleDoubleProperty columnDistParam1;
	private final SimpleDoubleProperty columnDistParam2;
	private final ObservableList<String> contextDistribution;
	private final StringProperty contextDistEscolhida;
	private final SimpleDoubleProperty contextDistParam1;
	private final SimpleDoubleProperty contextDistParam2;
	private final ObservableList<String> contiguity;
	private final SimpleStringProperty contiguityEscolhida;
	
	//Triclusters Patterns
	private boolean symbolicType;
	private final ObservableList<TriclusterPatternTableView> numericPatternsList;
	private final ObservableList<TriclusterPatternTableView> symbolicPatternsList;
	
	//Overlapping
	private final ObservableList<String> plaidCoherency;
	private final StringProperty plaidCoherencyEscolhida;
	private final SimpleDoubleProperty percOverlappingTrics;
	private final IntegerProperty maxOverlappingTrics;
	private final SimpleDoubleProperty percOverlappingElements;
	private final SimpleDoubleProperty percOverlappingRows;
	private final SimpleDoubleProperty percOverlappingColumns;
	private final SimpleDoubleProperty percOverlappingContexts;
	
	//Extras
	private final SimpleDoubleProperty percMissingsBackground;
	private final SimpleDoubleProperty percMissingsTrics;
	private final SimpleDoubleProperty percNoiseBackground;
	private final SimpleDoubleProperty percNoiseTrics;
	private final SimpleDoubleProperty noiseDeviation;
	private final SimpleDoubleProperty percErrorsBackground;
	private final SimpleDoubleProperty percErrorsTrics;
	
	private final SimpleDoubleProperty progress;
	private final StringProperty statusT;
	private final StringProperty statusLB;
	
	private final StringProperty directoryChooserTF;
	private final StringProperty fileNameTF;
	
	private final ObservableList<String> triclusterID;
	private SimpleStringProperty triclusterIDSelected;
	
	public MenuPrincipalModel(GBicService gBicService){
		
		//Dataset settings
		this.numRows = new SimpleIntegerProperty();
		this.numColumns = new SimpleIntegerProperty();
		this.numContexts = new SimpleIntegerProperty();
		this.dataType = FXCollections.observableArrayList();
		this.dataTypeEscolhido = new SimpleStringProperty();
		this.minValue = new SimpleDoubleProperty();
		this.minValue.setValue(-10);
		this.maxValue = new SimpleDoubleProperty();
		this.maxValue.setValue(10);
		this.backgroundType = FXCollections.observableArrayList();
		this.backgroundTypeEscolhido = new SimpleStringProperty();
		
		this.symbolType = FXCollections.observableArrayList();
		this.symbolTypeEscolhido = new SimpleStringProperty();
		
		gBicService.getSymbolType().forEach(t->this.symbolType.add(t));
		this.symbolTypeEscolhido.setValue(this.symbolType.get(0));
		
		this.symbolList = new SimpleStringProperty();
		this.numberOfSymbols = new SimpleIntegerProperty();
		
		this.distMeanValue = new SimpleDoubleProperty();
		this.distStdValue = new SimpleDoubleProperty();

		gBicService.getDataTypes().forEach(t->this.dataType.add(t));
		this.dataTypeEscolhido.setValue(this.dataType.get(0));
		
		gBicService.getDatasetBackground().forEach(b->this.backgroundType.add(b));
		this.backgroundTypeEscolhido.setValue(this.backgroundType.get(0));
		
		this.discreteProbs = FXCollections.observableArrayList();
		
		//Triclusters Properties
		this.numTrics = new SimpleIntegerProperty();
		this.rowDistribution = FXCollections.observableArrayList();
		this.rowDistEscolhida = new SimpleStringProperty();
		this.rowDistParam1 = new SimpleDoubleProperty();
		this.rowDistParam2 = new SimpleDoubleProperty();
		
		this.numTrics.setValue(8);
		this.rowDistParam1.setValue(6);
		this.rowDistParam2.setValue(8);
		
		this.columnDistribution = FXCollections.observableArrayList();
		this.columnDistEscolhida = new SimpleStringProperty();
		this.columnDistParam1 = new SimpleDoubleProperty();
		this.columnDistParam2 = new SimpleDoubleProperty();
		
		this.columnDistParam1.setValue(5);
		this.columnDistParam2.setValue(7);
		
		this.contextDistribution = FXCollections.observableArrayList();
		this.contextDistEscolhida = new SimpleStringProperty();
		this.contextDistParam1 = new SimpleDoubleProperty();
		this.contextDistParam2 = new SimpleDoubleProperty();
		
		this.contextDistParam1.setValue(5);
		this.contextDistParam2.setValue(9);
		
		this.contiguity = FXCollections.observableArrayList();
		this.contiguityEscolhida = new SimpleStringProperty();
		
		gBicService.getDistributions().forEach(d->this.rowDistribution.add(d));
		this.rowDistEscolhida.setValue(this.rowDistribution.get(0));
		
		gBicService.getDistributions().forEach(d->this.columnDistribution.add(d));
		this.columnDistEscolhida.setValue(this.columnDistribution.get(0));
		
		gBicService.getDistributions().forEach(d->this.contextDistribution.add(d));
		this.contextDistEscolhida.setValue(this.contextDistribution.get(0));
		
		gBicService.getContiguity().forEach(c->this.contiguity.add(c));
		this.contiguityEscolhida.setValue((this.contiguity.get(0)));
		
		//Triclusters Patterns
		this.numericPatternsList = FXCollections.observableArrayList();
		int i = 1;
		for(TriclusterPatternWrapper p : gBicService.getNumericPatterns()) {
			TriclusterPatternTableView t = new TriclusterPatternTableView(i, p.getRowPattern(), p.getColumnPattern(), 
					p.getContextPattern(), p.getImagePath(), new ComboBox<String>(), new Button("See"), new CheckBox());
			
			if(i == 1)
				t.getSelect().setSelected(true);
			
			this.numericPatternsList.add(t);
			i++;
		}
		
		this.symbolicPatternsList = FXCollections.observableArrayList();
		i = 1;
		for(TriclusterPatternWrapper p : gBicService.getSymbolicPatterns()) {
			TriclusterPatternTableView t = new TriclusterPatternTableView(i, p.getRowPattern(), p.getColumnPattern(), 
					p.getContextPattern(), p.getImagePath(), new ComboBox<String>(), new Button("See"), new CheckBox());
			
			if(i == 1)
				t.getSelect().setSelected(true);
			
			this.symbolicPatternsList.add(t);				
			i++;
		}
		
		this.symbolicType = true;
		
		//Overlapping
		this.plaidCoherency = FXCollections.observableArrayList();
		this.plaidCoherencyEscolhida = new SimpleStringProperty();
		gBicService.getPlaidCoherency().forEach(s->this.plaidCoherency.add(s));
		this.plaidCoherencyEscolhida.setValue((this.plaidCoherency.get(this.plaidCoherency.size() - 1)));
		this.percOverlappingTrics = new SimpleDoubleProperty();
		this.maxOverlappingTrics = new SimpleIntegerProperty();
		this.percOverlappingElements = new SimpleDoubleProperty();
		this.percOverlappingRows = new SimpleDoubleProperty();
		this.percOverlappingColumns = new SimpleDoubleProperty();
		this.percOverlappingContexts = new SimpleDoubleProperty();
		this.percOverlappingRows.setValue(100);
		this.percOverlappingColumns.setValue(100);
		this.percOverlappingContexts.setValue(100);
		
		//Extras
		this.percMissingsBackground = new SimpleDoubleProperty();
		this.percMissingsBackground.setValue(0.0);
		this.percMissingsTrics = new SimpleDoubleProperty();
		this.percMissingsTrics.setValue(0.0);
		
		this.percNoiseBackground = new SimpleDoubleProperty();
		this.percNoiseBackground.setValue(0.0);
		this.percNoiseTrics = new SimpleDoubleProperty();
		this.percNoiseTrics.setValue(0.0);
		this.noiseDeviation = new SimpleDoubleProperty();
		this.noiseDeviation.setValue(0.0);
		
		this.percErrorsBackground = new SimpleDoubleProperty();
		this.percErrorsBackground.setValue(0.0);
		this.percErrorsTrics = new SimpleDoubleProperty();
		this.percErrorsTrics.setValue(0.0);
		
		this.progress = new SimpleDoubleProperty();
		this.statusLB = new SimpleStringProperty();
		this.statusT = new SimpleStringProperty();
		
		this.directoryChooserTF = new SimpleStringProperty();
		this.fileNameTF = new SimpleStringProperty();
		
		this.triclusterID = FXCollections.observableArrayList();
		this.triclusterIDSelected = new SimpleStringProperty();
	}

	public void setTriclusterIDList() {
		for(int i = 0; i < this.getNumTrics(); i++)
			this.triclusterID.add("Tricluster " + i);
	}
	
	public ObservableList<String> getTriclusterIDs() {

		return this.triclusterID;
	}

	
	public void setSymbolicTypeBoolean(boolean b) {
		this.symbolicType = b;
	}
	
	public void setTriclusterIDSelected(String t) {
		this.triclusterIDSelected.set(t);
	}
	
	public int getTriclusterIDSelected() {
		return Integer.parseInt(this.triclusterIDSelected.get().split(" ")[2].strip());
	}
	
	public boolean isSymbolic() {
		return this.symbolicType;
	}
	
	// Getters e Setters

	public IntegerProperty getNumRowsProperty() {

		return this.numRows;
	}

	public int getNumRows() {

		return this.numRows.get();
	}
	
	public IntegerProperty getNumColumnsProperty() {

		return this.numColumns;
	}

	public int getNumColumns() {

		return this.numColumns.get();
	}
	
	public IntegerProperty getNumContextsProperty() {

		return this.numContexts;
	}

	public int getNumContexts() {

		return this.numContexts.get();
	}

	public ObservableList<String> getDataTypes() {

		return this.dataType;
	}


	public String getDataTypeEscolhido() {

		return this.dataTypeEscolhido.get();
	}

	public SimpleDoubleProperty getMinValueProperty() {

		return this.minValue;
	}

	public double getMinValue() {

		return this.minValue.get();
	}

	public SimpleDoubleProperty getMaxValueProperty() {

		return this.maxValue;
	}

	public double getMaxValue() {

		return this.maxValue.get();
	}
	
	public ObservableList<String> getBackgroundTypes() {

		return this.backgroundType;
	}

	public String getBackgroundTypeEscolhido() {
		return this.backgroundTypeEscolhido.get();
	}

	public void setDataTypeEscolhido(String value) {

		this.dataTypeEscolhido.set(value);
	}

	public void setBackgroundTypeEscolhido(String value) {

		this.backgroundTypeEscolhido.set(value);
	}
	
	public SimpleDoubleProperty getDistMeanProperty() {

		return this.distMeanValue;
	}

	public double getDistMean() {

		return this.distMeanValue.get();
	}
	
	public SimpleDoubleProperty getDistStdProperty() {

		return this.distStdValue;
	}

	public double getDistStd() {

		return this.distStdValue.get();
	}
	
	public ObservableList<String> getSymbolTypes() {

		return this.symbolType;
	}

	public String getSymbolTypeEscolhido() {
		return this.symbolTypeEscolhido.get();
	}

	public void setSymbolTypeEscolhido(String value) {

		this.symbolTypeEscolhido.set(value);
	}
	
	public SimpleIntegerProperty getNumberOfSymbolsProperty() {

		return this.numberOfSymbols;
	}

	public int getNumberOfSymbols() {

		return this.numberOfSymbols.get();
	}
	
	public void setNumberOfSymbols(int numberOfSymbols) {
		this.numberOfSymbols.set(numberOfSymbols);
	}
	
	public StringProperty getListOfSymbolsProperty() {

		return this.symbolList;
	}

	public ObservableList<String> getListOfSymbols() {

		String[] tokens = this.symbolList.get().strip().split(",");
		List<String> symbols = Arrays.asList(tokens);
		ObservableList<String> listOfSymbols = FXCollections.observableArrayList(symbols);
		
		return listOfSymbols;
	}
	
	public void setListOfSymbols(String symbolList) {
		this.symbolList.set(symbolList);
	}
	
	//Triclusters Properties
	
	public IntegerProperty getNumTricsProperty() {

		return this.numTrics;
	}

	public int getNumTrics() {

		return this.numTrics.get();
	}
	
	public ObservableList<String> getRowDistributions() {

		return this.rowDistribution;
	}

	public String getRowDistributionEscolhida() {
		
		return this.rowDistEscolhida.get();
	}
	
	public void setRowDistributionEscolhida(String value) {

		this.rowDistEscolhida.set(value);
	}
	
	public ObservableList<String> getColumnDistributions() {

		return this.columnDistribution;
	}

	public String getColumnDistributionEscolhida() {
		
		return this.columnDistEscolhida.get();
	}
	
	public void setColumnDistributionEscolhida(String value) {

		this.columnDistEscolhida.set(value);
	}
	
	public ObservableList<String> getContextDistributions() {

		return this.contextDistribution;
	}

	public String getContextDistributionEscolhida() {
		
		return this.contextDistEscolhida.get();
	}
	
	public void setContextDistributionEscolhida(String value) {

		this.contextDistEscolhida.set(value);
	}
	
	public SimpleDoubleProperty getRowDistParam1Property() {

		return this.rowDistParam1;
	}

	public double getRowDistParam1() {

		return this.rowDistParam1.get();
	}

	public SimpleDoubleProperty getRowDistParam2Property() {

		return this.rowDistParam2;
	}

	public double getRowDistParam2() {

		return this.rowDistParam2.get();
	}
	
	public SimpleDoubleProperty getColumnDistParam1Property() {

		return this.columnDistParam1;
	}

	public double getColumnDistParam1() {

		return this.columnDistParam1.get();
	}

	public SimpleDoubleProperty getColumnDistParam2Property() {

		return this.columnDistParam2;
	}

	public double getColumnDistParam2() {

		return this.columnDistParam2.get();
	}
	
	public SimpleDoubleProperty getContextDistParam1Property() {

		return this.contextDistParam1;
	}

	public double getContextDistParam1() {

		return this.contextDistParam1.get();
	}

	public SimpleDoubleProperty getContextDistParam2Property() {

		return this.contextDistParam2;
	}

	public double getContextDistParam2() {

		return this.contextDistParam2.get();
	}
	
	public ObservableList<String> getContiguity() {

		return this.contiguity;
	}

	public String getContiguityEscolhida() {
		
		return this.contiguityEscolhida.get();
	}
	
	public void setContiguityEscolhida(String value) {

		this.contiguityEscolhida.set(value);
	}
	
	
	//Tricluster Patterns
	public ObservableList<TriclusterPatternTableView> getNumericPatterns() {

		return this.numericPatternsList;
	}
	
	public ObservableList<TriclusterPatternTableView> getSymbolicPatterns() {

		return this.symbolicPatternsList;
	}

	//Overlapping
	public ObservableList<String> getPlaidCoherency() {

		return this.plaidCoherency;
	}

	public String getPlaidCoherencyEscolhida() {
		
		return this.plaidCoherencyEscolhida.get();
	}
	
	public void setPlaidCoherencyEscolhida(String value) {

		this.plaidCoherencyEscolhida.set(value);
	}
	
	public SimpleDoubleProperty getPercOverlappingTricsProperty() {

		return this.percOverlappingTrics;
	}

	public double getPercOverlappingTrics() {

		return this.percOverlappingTrics.get();
	}
	
	public IntegerProperty getMaxOverlappingTricsProperty() {

		return this.maxOverlappingTrics;
	}

	public int getMaxOverlappingTrics() {

		return this.maxOverlappingTrics.get();
	}
	
	public SimpleDoubleProperty getPercOverlappingElementsProperty() {

		return this.percOverlappingElements;
	}

	public double getPercOverlappingElements() {

		return this.percOverlappingElements.get();
	}
	
	public SimpleDoubleProperty getPercOverlappingRowsProperty() {

		return this.percOverlappingRows;
	}

	public double getPercOverlappingRows() {

		return this.percOverlappingRows.get();
	}
	
	public SimpleDoubleProperty getPercOverlappingColumnsProperty() {

		return this.percOverlappingColumns;
	}

	public double getPercOverlappingColumns() {

		return this.percOverlappingColumns.get();
	}
	
	public SimpleDoubleProperty getPercOverlappingContextsProperty() {

		return this.percOverlappingContexts;
	}

	public double getPercOverlappingContexts() {

		return this.percOverlappingContexts.get();
	}
	
	//Extras
	public SimpleDoubleProperty getPercMissingsBackgroundProperty() {
		return percMissingsBackground;
	}
	
	public double getPercMissingsBackground() {
		return percMissingsBackground.get();
	}

	public SimpleDoubleProperty getPercMissingsTricsProperty() {
		return percMissingsTrics;
	}
	
	public double getPercMissingsTrics() {
		return percMissingsTrics.get();
	}

	public SimpleDoubleProperty getPercNoiseBackgroundProperty() {
		return percNoiseBackground;
	}
	
	public double getPercNoiseBackground() {
		return percNoiseBackground.get();
	}

	public SimpleDoubleProperty getPercNoiseTricsProperty() {
		return percNoiseTrics;
	}
	
	public double getPercNoiseTrics() {
		return percNoiseTrics.get();
	}

	public SimpleDoubleProperty getNoiseDeviationProperty() {
		return noiseDeviation;
	}
	
	public double getNoiseDeviation() {
		return noiseDeviation.get();
	}

	public SimpleDoubleProperty getPercErrorsBackgroundProperty() {
		return percErrorsBackground;
	}

	public double getPercErrorsBackground() {
		return percErrorsBackground.get();
	}
	
	public SimpleDoubleProperty getPercErrorsTricsProperty() {
		return percErrorsTrics;
	}
	
	public double getPercErrorsTrics() {
		return percErrorsTrics.get();
	}
	
	public SimpleDoubleProperty getProgressProperty() {
		return this.progress;
	}
	
	public double getProgress() {
		return this.progress.get();
	}
	
	public void setProgress(double prog) {
		this.progress.setValue(prog);
	}
	
	public StringProperty getDirectoryChooserProperty() {
		return this.directoryChooserTF;
	}
	
	public String getDirectory() {
		return this.directoryChooserTF.get();
	}
	
	public void setDirectory(String path) {
		this.directoryChooserTF.set(path);
	}
	
	public StringProperty getFileNameProperty() {
		return this.fileNameTF;
	}
	
	public String getFileName() {
		return this.fileNameTF.get();
	}
	
	public void setFileName(String name) {
		this.fileNameTF.set(name);
	}
	
	public void setDiscreteProbabilities(ObservableList<DiscreteProbabilitiesTableView> probs) {
		this.discreteProbs = probs;
	}
	
	public ObservableList<DiscreteProbabilitiesTableView> getDiscreteProbabilities(){
		return this.discreteProbs;
	}
	/*
	public void clearProperties() {

		this.designacao.set("");
		this.numeroParticipantes.set(0); 
		this.duracao.set(0);
		this.numEncontros.set(0);
	}
	*/

	public void setNumTrics(int numTrics2) {
		this.numTrics.set(numTrics2);
		
	}

}
