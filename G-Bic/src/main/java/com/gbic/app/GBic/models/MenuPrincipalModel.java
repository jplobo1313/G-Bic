package com.gbic.app.GBic.models;

import java.util.Arrays;
import java.util.List;

import com.gbic.service.GBicService;
import com.gbic.service.GBicService.BiclusterPatternWrapper;
import com.gbic.utils.DiscreteProbabilitiesTableView;
import com.gbic.utils.BiclusterPatternTableView;

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
	
	//Biclusters Properties
	private final IntegerProperty numBics;
	private final ObservableList<String> rowDistribution;
	private final StringProperty rowDistEscolhida;
	private final SimpleDoubleProperty rowDistParam1;
	private final SimpleDoubleProperty rowDistParam2;
	private final ObservableList<String> columnDistribution;
	private final StringProperty columnDistEscolhida;
	private final SimpleDoubleProperty columnDistParam1;
	private final SimpleDoubleProperty columnDistParam2;
	private final ObservableList<String> contiguity;
	private final SimpleStringProperty contiguityEscolhida;
	
	//Biclusters Patterns
	private boolean symbolicType;
	private final ObservableList<BiclusterPatternTableView> numericPatternsList;
	private final ObservableList<BiclusterPatternTableView> symbolicPatternsList;
	
	//Overlapping
	private final ObservableList<String> plaidCoherency;
	private final StringProperty plaidCoherencyEscolhida;
	private final SimpleDoubleProperty percOverlappingBics;
	private final IntegerProperty maxOverlappingBics;
	private final SimpleDoubleProperty percOverlappingElements;
	private final SimpleDoubleProperty percOverlappingRows;
	private final SimpleDoubleProperty percOverlappingColumns;	
	
	//Extras
	private final SimpleDoubleProperty percMissingsBackground;
	private final SimpleDoubleProperty percMissingsBics;
	private final SimpleDoubleProperty percNoiseBackground;
	private final SimpleDoubleProperty percNoiseBics;
	private final SimpleDoubleProperty noiseDeviation;
	private final SimpleDoubleProperty percErrorsBackground;
	private final SimpleDoubleProperty percErrorsBics;
	
	private final SimpleDoubleProperty progress;
	private final StringProperty statusT;
	private final StringProperty statusLB;
	
	private final StringProperty directoryChooserTF;
	private final StringProperty fileNameTF;
	
	private final ObservableList<String> biclusterID;
	private SimpleStringProperty bicIDSelected;
	
	public MenuPrincipalModel(GBicService gBicService){
		
		//Dataset settings
		this.numRows = new SimpleIntegerProperty();
		this.numColumns = new SimpleIntegerProperty();
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
		
		//Biclusters Properties
		this.numBics = new SimpleIntegerProperty();
		this.rowDistribution = FXCollections.observableArrayList();
		this.rowDistEscolhida = new SimpleStringProperty();
		this.rowDistParam1 = new SimpleDoubleProperty();
		this.rowDistParam2 = new SimpleDoubleProperty();
		
		this.numBics.setValue(8);
		this.rowDistParam1.setValue(6);
		this.rowDistParam2.setValue(8);
		
		this.columnDistribution = FXCollections.observableArrayList();
		this.columnDistEscolhida = new SimpleStringProperty();
		this.columnDistParam1 = new SimpleDoubleProperty();
		this.columnDistParam2 = new SimpleDoubleProperty();
		
		this.columnDistParam1.setValue(5);
		this.columnDistParam2.setValue(7);
		
		this.contiguity = FXCollections.observableArrayList();
		this.contiguityEscolhida = new SimpleStringProperty();
		
		gBicService.getDistributions().forEach(d->this.rowDistribution.add(d));
		this.rowDistEscolhida.setValue(this.rowDistribution.get(0));
		
		gBicService.getDistributions().forEach(d->this.columnDistribution.add(d));
		this.columnDistEscolhida.setValue(this.columnDistribution.get(0));
		
		gBicService.getContiguity().forEach(c->this.contiguity.add(c));
		this.contiguityEscolhida.setValue((this.contiguity.get(0)));
		
		//Biclusters Patterns
		this.numericPatternsList = FXCollections.observableArrayList();
		int i = 1;
		for(BiclusterPatternWrapper p : gBicService.getNumericPatterns()) {
			BiclusterPatternTableView t = new BiclusterPatternTableView(i, p.getRowPattern(), p.getColumnPattern(), 
					p.getImagePath(), new ComboBox<String>(), new Button("See"), new CheckBox());
			
			if(i == 1)
				t.getSelect().setSelected(true);
			
			this.numericPatternsList.add(t);
			i++;
		}
		
		this.symbolicPatternsList = FXCollections.observableArrayList();
		i = 1;
		for(BiclusterPatternWrapper p : gBicService.getSymbolicPatterns()) {
			BiclusterPatternTableView t = new BiclusterPatternTableView(i, p.getRowPattern(), p.getColumnPattern(), 
					p.getImagePath(), new ComboBox<String>(), new Button("See"), new CheckBox());
			
			if(i == 1)
				t.getSelect().setSelected(true);
			
			this.symbolicPatternsList.add(t);				
			i++;
		}
		
		this.symbolicType = true;
		
		//Overlapping
		this.plaidCoherency = FXCollections.observableArrayList();
		this.plaidCoherencyEscolhida = new SimpleStringProperty();
		//gBicService.getPlaidCoherency().forEach(s->this.plaidCoherency.add(s));
		//this.plaidCoherencyEscolhida.setValue((this.plaidCoherency.get(this.plaidCoherency.size() - 1)));
		
		this.plaidCoherency.add("No Overlapping");
		this.plaidCoherency.add("None");
		this.plaidCoherencyEscolhida.setValue((this.plaidCoherency.get(0)));
		
		this.percOverlappingBics = new SimpleDoubleProperty();
		this.maxOverlappingBics = new SimpleIntegerProperty();
		this.percOverlappingElements = new SimpleDoubleProperty();
		this.percOverlappingRows = new SimpleDoubleProperty();
		this.percOverlappingColumns = new SimpleDoubleProperty();
		this.percOverlappingRows.setValue(100);
		this.percOverlappingColumns.setValue(100);
		
		//Extras
		this.percMissingsBackground = new SimpleDoubleProperty();
		this.percMissingsBackground.setValue(0.0);
		this.percMissingsBics = new SimpleDoubleProperty();
		this.percMissingsBics.setValue(0.0);
		
		this.percNoiseBackground = new SimpleDoubleProperty();
		this.percNoiseBackground.setValue(0.0);
		this.percNoiseBics = new SimpleDoubleProperty();
		this.percNoiseBics.setValue(0.0);
		this.noiseDeviation = new SimpleDoubleProperty();
		this.noiseDeviation.setValue(0.0);
		
		this.percErrorsBackground = new SimpleDoubleProperty();
		this.percErrorsBackground.setValue(0.0);
		this.percErrorsBics = new SimpleDoubleProperty();
		this.percErrorsBics.setValue(0.0);
		
		this.progress = new SimpleDoubleProperty();
		this.statusLB = new SimpleStringProperty();
		this.statusT = new SimpleStringProperty();
		
		this.directoryChooserTF = new SimpleStringProperty();
		this.fileNameTF = new SimpleStringProperty();
		
		this.biclusterID = FXCollections.observableArrayList();
		this.bicIDSelected = new SimpleStringProperty();
	}

	public void setBiclusterIDList() {
		for(int i = 0; i < this.getNumBics(); i++)
			this.biclusterID.add("Bicluster " + i);
	}
	
	public ObservableList<String> getBiclusterIDs() {

		return this.biclusterID;
	}

	
	public void setSymbolicTypeBoolean(boolean b) {
		this.symbolicType = b;
	}
	
	public void setBiclusterIDSelected(String t) {
		this.bicIDSelected.set(t);
	}
	
	public int getBiclusterIDSelected() {
		return Integer.parseInt(this.bicIDSelected.get().split(" ")[2].strip());
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
	
	//Biclusters Properties
	
	public IntegerProperty getNumBicsProperty() {

		return this.numBics;
	}

	public int getNumBics() {

		return this.numBics.get();
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
	
	public ObservableList<String> getContiguity() {

		return this.contiguity;
	}

	public String getContiguityEscolhida() {
		
		return this.contiguityEscolhida.get();
	}
	
	public void setContiguityEscolhida(String value) {

		this.contiguityEscolhida.set(value);
	}
	
	
	//Bicluster Patterns
	public ObservableList<BiclusterPatternTableView> getNumericPatterns() {

		return this.numericPatternsList;
	}
	
	public ObservableList<BiclusterPatternTableView> getSymbolicPatterns() {

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
	
	public void updatePlaidCoherency(boolean numeric) {
		if(numeric) {
			this.plaidCoherency.clear();
			this.plaidCoherency.add("No Overlapping");
			this.plaidCoherency.add("None");
			this.plaidCoherency.add("Additive");
			this.plaidCoherency.add("Multiplicative");
			this.plaidCoherency.add("Interpoled");
			this.plaidCoherencyEscolhida.setValue((this.plaidCoherency.get(0)));
		}
		else
		{
			this.plaidCoherency.clear();
			this.plaidCoherency.add("No Overlapping");
			this.plaidCoherency.add("None");
			this.plaidCoherencyEscolhida.setValue((this.plaidCoherency.get(0)));
		}
	}
	
	public SimpleDoubleProperty getPercOverlappingBicsProperty() {

		return this.percOverlappingBics;
	}

	public double getPercOverlappingBics() {

		return this.percOverlappingBics.get();
	}
	
	public IntegerProperty getMaxOverlappingBicsProperty() {

		return this.maxOverlappingBics;
	}

	public int getMaxOverlappingBics() {

		return this.maxOverlappingBics.get();
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
	
	//Extras
	public SimpleDoubleProperty getPercMissingsBackgroundProperty() {
		return percMissingsBackground;
	}
	
	public double getPercMissingsBackground() {
		return percMissingsBackground.get();
	}

	public SimpleDoubleProperty getPercMissingsBicsProperty() {
		return percMissingsBics;
	}
	
	public double getPercMissingsBics() {
		return percMissingsBics.get();
	}

	public SimpleDoubleProperty getPercNoiseBackgroundProperty() {
		return percNoiseBackground;
	}
	
	public double getPercNoiseBackground() {
		return percNoiseBackground.get();
	}

	public SimpleDoubleProperty getPercNoiseBicsProperty() {
		return percNoiseBics;
	}
	
	public double getPercNoiseBics() {
		return percNoiseBics.get();
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
	
	public SimpleDoubleProperty getPercErrorsBicsProperty() {
		return percErrorsBics;
	}
	
	public double getPercErrorsBics() {
		return percErrorsBics.get();
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

	public void setNumBics(int numBics2) {
		this.numBics.set(numBics2);
		
	}

}
