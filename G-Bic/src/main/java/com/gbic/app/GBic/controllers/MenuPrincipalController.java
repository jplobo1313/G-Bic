package com.gbic.app.GBic.controllers;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.function.UnaryOperator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.HeatMapChart;
import org.knowm.xchart.HeatMapChartBuilder;

import com.gbic.app.GBic.models.MenuPrincipalModel;
import com.gbic.domain.dataset.Dataset;
import com.gbic.domain.dataset.HeterogeneousDataset;
import com.gbic.domain.dataset.NumericDataset;
import com.gbic.domain.dataset.SymbolicDataset;
import com.gbic.domain.bicluster.Bicluster;
import com.gbic.domain.bicluster.MixedBicluster;
import com.gbic.service.GBicService;
import com.gbic.service.GenerateDatasetTask;
import com.gbic.service.GBicService.BiclusterPatternWrapper;
import com.gbic.utils.DiscreteProbabilitiesTableView;
import com.gbic.utils.HeatMapData;
import com.gbic.utils.HeatMapTableView;
import com.gbic.utils.InputValidation;
import com.gbic.utils.NumericHeatMapTableView;
import com.gbic.utils.SymbolicHeatMapTableView;
import com.gbic.utils.BiclusterPatternTableView;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.NumberStringConverter;

public class MenuPrincipalController{
	//Atributos
	Stage primaryStage;

	int currentTriclusterVisualization;
	int currentSliceVisualization;

	//Dataset Settings
	@FXML private TextField numRowsTF;
	@FXML private TextField numColumnsTF;
	//@FXML private RadioButton symbolicTypeRB;
	//@FXML private RadioButton numericTypeRB;
	
	@FXML private ComboBox<String> datasetTypeCB;
	@FXML private Label featuresLB;
	@FXML private Label numericLB;
	@FXML private Label numericPercLB;
	@FXML private Label symbolicLB;
	@FXML private Label symbolicPercLB;
	@FXML private Slider featuresSlicer;
	
	@FXML private Label dataTypeLB;
	@FXML private ComboBox<String> dataTypeCB;
	@FXML private Label symbolTypeLB;
	@FXML private Label symbolTypeParamLB;
	@FXML private ComboBox<String> symbolTypeCB;
	@FXML private TextField symbolTypeParamTF;
	@FXML private Label minValueLB;
	@FXML private Label maxValueLB;
	@FXML private TextField minValueTF;
	@FXML private TextField maxValueTF;
	
	//Single background
	@FXML private Label singleBackgroundLB;
	@FXML private ComboBox<String> singleBackgroundTypeCB;
	@FXML private TextField singleDistMeanTF;
	@FXML private TextField singleDistStdTF;
	@FXML private Label distMeanLabel;
	@FXML private Label distStdLabel;
	@FXML private Pane singleBackgroundParamsPane;
	@FXML private Pane singleDiscreteProbsPane;
	@FXML private Separator backgroundSeparator;
	
	//Composed Background
	@FXML private Label composedBackgroundLB;
	@FXML private Label composedBackgroundTypeLB;
	@FXML private ComboBox<String> composedBackgroundTypeCB;
	@FXML private Pane composedBackgroundParamsPane;
	@FXML private Pane composedDiscreteProbsPane;
	@FXML private TextField composedDistMeanTF;
	@FXML private TextField composedDistStdTF;

	@FXML private TableView<DiscreteProbabilitiesTableView> singleDiscreteProbsTV;
	@FXML private TableColumn<DiscreteProbabilitiesTableView, String> singleSymbolTC;
	@FXML private TableColumn<DiscreteProbabilitiesTableView, String> singleProbTC;
	
	@FXML private TableView<DiscreteProbabilitiesTableView> composedDiscreteProbsTV;
	@FXML private TableColumn<DiscreteProbabilitiesTableView, String> composedSymbolTC;
	@FXML private TableColumn<DiscreteProbabilitiesTableView, String> composedProbTC;

	//Triclusters Properties
	@FXML private TextField numBicsTF;
	@FXML private ComboBox<String> rowStructureDistCB;
	@FXML private Label rowDistParam1Label;
	@FXML private TextField rowDistParam1TF;
	@FXML private Label rowDistParam2Label;
	@FXML private TextField rowDistParam2TF;

	@FXML private ComboBox<String> columnStructureDistCB;
	@FXML private Label columnDistParam1Label;
	@FXML private TextField columnDistParam1TF;
	@FXML private Label columnDistParam2Label;
	@FXML private TextField columnDistParam2TF;

	@FXML private ComboBox<String> contiguityCB;

	//Triclusters Patterns
	@FXML private TableView<BiclusterPatternTableView> patternsTV;
	@FXML private TableColumn<BiclusterPatternTableView, String> bicTypeTC;
	@FXML private TableColumn<BiclusterPatternTableView, String> rowTC;
	@FXML private TableColumn<BiclusterPatternTableView, String> columnTC;
	@FXML private TableColumn<BiclusterPatternTableView, String> contextTC;
	@FXML private TableColumn<BiclusterPatternTableView, ComboBox<String>> timeProfileTC;
	@FXML private TableColumn<BiclusterPatternTableView, CheckBox> selectTC;
	ObservableList<BiclusterPatternTableView> patternList;

	//Overlapping
	@FXML private ComboBox<String> plaidCoherencyCB;
	@FXML private Label percOverlappingBicsLB;
	@FXML private TextField percOverlappingBicsTF;
	@FXML private Label maxOverlappingBicsLB;
	@FXML private TextField maxOverlappingBicsTF;
	@FXML private Label percOverlappingElementsLB;
	@FXML private TextField percOverlappingElementsTF;
	@FXML private Label percOverlappingRowsLB;
	@FXML private TextField percOverlappingRowsTF;
	@FXML private Label percOverlappingColumnsLB;
	@FXML private TextField percOverlappingColumnsTF;

	//Extras
	@FXML private TextField percMissingBackgroundTF;
	@FXML private TextField percMissingBicsTF;
	@FXML private TextField percNoiseBackgroundTF;
	@FXML private TextField percNoiseBicsTF;
	@FXML private TextField noiseDeviationTF;
	@FXML private TextField percErrorsBackgroundTF;
	@FXML private TextField percErrorsBicsTF;

	@FXML private ProgressBar statusBar;
	@FXML private Text statusT;
	@FXML private Label statusLB;

	//Output
	@FXML private RadioButton singleFileRB;
	@FXML private RadioButton numericFileRB;
	@FXML private TextField directoryChooserTF;
	@FXML private Button directoryChooserB;
	@FXML private TextField fileNameTF;
	@FXML private ComboBox<String> randomSeedCB;
	@FXML private TextField randomSeedTF;

	//Visualization
	@FXML private Tab visualizationTab;
	@FXML private ComboBox<String> bicIDCB;
	
	@FXML private TextFlow bicSummaryTF;
	@FXML private Button singleHeatMapB;
	@FXML private Button composedHeatMapB;
	
	private MenuPrincipalModel model;

	private GBicService gBicService;


	public void setTriGenService(GBicService gBicService) {
		this.gBicService = gBicService;
	}


	/**
	 * Faz bind de todas as propriedades dos atributos da classe
	 * @param model
	 */
	public void setModel(MenuPrincipalModel model) {

		this.model = model;

		//Dataset settings
		this.numRowsTF.textProperty().bindBidirectional(model.getNumRowsProperty(), new NumberStringConverter());
		this.numRowsTF.textProperty().setValue("100");

		this.numColumnsTF.textProperty().bindBidirectional(model.getNumColumnsProperty(), new NumberStringConverter());
		this.numColumnsTF.textProperty().setValue("100");
		
		this.dataTypeCB.setItems(model.getDataTypes());
		this.dataTypeCB.setValue(model.getDataTypeEscolhido());

		this.symbolTypeCB.setItems(model.getSymbolTypes());
		this.symbolTypeCB.setValue(model.getSymbolTypeEscolhido());

		//this.symbolicTypeRB.setSelected(true);
		this.datasetTypeCB.setItems(model.getDatasetTypes());
		this.datasetTypeCB.setValue(model.getDatasetTypeEscolhido());
		
		this.symbolTypeParamTF.textProperty().bindBidirectional(model.getNumberOfSymbolsProperty(), new NumberStringConverter());
		this.symbolTypeParamTF.textProperty().setValue("10");
		
		this.symbolTypeParamTF.focusedProperty().addListener((obs, oldText, newText) -> {
			if(newText == false && oldText == true && model.getSingleBackgroundTypeEscolhido().equals("Discrete")) {
				initSingleDiscreteTable();
			}

			// ...
		});

		this.minValueTF.textProperty().bindBidirectional(model.getMinValueProperty(), new NumberStringConverter());
		this.minValueTF.focusedProperty().addListener((obs, oldText, newText) -> {
			if(this.datasetTypeCB.getValue().equals("Numeric")) {
				if(newText == false && oldText == true && model.getSingleBackgroundTypeEscolhido().equals("Discrete"))
					initSingleDiscreteTable();
			}
			else {
				if(newText == false && oldText == true && model.getComposedBackgroundTypeEscolhido().equals("Discrete"))
					initComposedDiscreteTable();
			}
					
		});

		this.maxValueTF.textProperty().bindBidirectional(model.getMaxValueProperty(), new NumberStringConverter());
		this.maxValueTF.focusedProperty().addListener((obs, oldText, newText) -> {
			if(this.datasetTypeCB.getValue().equals("Numeric")) {
				if(newText == false && oldText == true && model.getSingleBackgroundTypeEscolhido().equals("Discrete"))
					initSingleDiscreteTable();
			}
			else {
				if(newText == false && oldText == true && model.getComposedBackgroundTypeEscolhido().equals("Discrete"))
					initComposedDiscreteTable();
			}
		});

		setNumericDatasetParametersVisible(false);
		
		setHeterogeneousDatasetParametersVisible(false);
		
		 this.featuresSlicer.valueProperty().addListener(new ChangeListener<Number>() {
	            @Override
	            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
	            	int numericPerc = (int) Math.round(featuresSlicer.getValue());
	            	int symbolicPerc = 100 - numericPerc;
	            	numericPercLB.setText(numericPerc + "%");
	            	symbolicPercLB.setText(symbolicPerc + "%");
	            }
	        });


		this.singleBackgroundTypeCB.setItems(model.getSingleBackgroundTypes());
		this.singleBackgroundTypeCB.setValue(model.getSingleBackgroundTypeEscolhido());
		
		this.composedBackgroundTypeCB.setItems(model.getComposedBackgroundTypes());
		this.composedBackgroundTypeCB.setValue(model.getComposedBackgroundTypeEscolhido());

		this.singleDistMeanTF.textProperty().bindBidirectional(model.getSingleDistMeanProperty(), new NumberStringConverter());
		this.singleDistStdTF.textProperty().bindBidirectional(model.getSingleDistStdProperty(), new NumberStringConverter());

		this.composedDistMeanTF.textProperty().bindBidirectional(model.getComposedDistMeanProperty(), new NumberStringConverter());
		this.composedDistStdTF.textProperty().bindBidirectional(model.getComposedDistStdProperty(), new NumberStringConverter());
		
		this.singleBackgroundParamsPane.setVisible(false);
		this.singleDiscreteProbsPane.setVisible(false);
		this.composedBackgroundLB.setVisible(false);
		this.composedBackgroundTypeLB.setVisible(false);
		this.composedBackgroundTypeCB.setVisible(false);
		this.composedBackgroundParamsPane.setVisible(false);
		this.composedDiscreteProbsPane.setVisible(false);
		this.backgroundSeparator.setVisible(false);

		//Biclusters properties
		this.numBicsTF.textProperty().bindBidirectional(model.getNumBicsProperty(), new NumberStringConverter());
		this.rowStructureDistCB.setItems(model.getRowDistributions());
		this.rowStructureDistCB.setValue(model.getRowDistributionEscolhida());
		this.rowDistParam1TF.textProperty().bindBidirectional(model.getRowDistParam1Property(), new NumberStringConverter());
		this.rowDistParam2TF.textProperty().bindBidirectional(model.getRowDistParam2Property(), new NumberStringConverter());
		setDistributionLabels(this.rowDistParam1Label, this.rowDistParam2Label, model.getRowDistributionEscolhida());

		this.columnStructureDistCB.setItems(model.getColumnDistributions());
		this.columnStructureDistCB.setValue(model.getColumnDistributionEscolhida());
		this.columnDistParam1TF.textProperty().bindBidirectional(model.getColumnDistParam1Property(), new NumberStringConverter());
		this.columnDistParam2TF.textProperty().bindBidirectional(model.getColumnDistParam2Property(), new NumberStringConverter());
		setDistributionLabels(this.columnDistParam1Label, this.columnDistParam2Label, model.getColumnDistributionEscolhida());

		this.contiguityCB.setItems(model.getContiguity());
		this.contiguityCB.setValue(model.getContiguityEscolhida());

		//Biclusters Patterns
		System.out.println("Hi from controller setModel()!");
		this.patternsTV.setItems(model.getSymbolicPatterns());
		this.bicTypeTC.setCellValueFactory(new PropertyValueFactory<BiclusterPatternTableView, String>("biclusterType"));
		this.rowTC.setCellValueFactory(new PropertyValueFactory<BiclusterPatternTableView, String>("rowPattern"));
		this.columnTC.setCellValueFactory(new PropertyValueFactory<BiclusterPatternTableView, String>("columnPattern"));
		this.timeProfileTC.setCellValueFactory(new PropertyValueFactory<BiclusterPatternTableView, ComboBox<String>>("timeProfile"));
		this.selectTC.setCellValueFactory(new PropertyValueFactory<BiclusterPatternTableView, CheckBox>("select"));

		//Overlapping
		this.plaidCoherencyCB.setItems(model.getPlaidCoherency());
		this.plaidCoherencyCB.setValue(model.getPlaidCoherencyEscolhida());
		this.percOverlappingBicsTF.textProperty().bindBidirectional(model.getPercOverlappingBicsProperty(), new NumberStringConverter());
		this.maxOverlappingBicsTF.textProperty().bindBidirectional(model.getMaxOverlappingBicsProperty(), new NumberStringConverter());
		this.percOverlappingElementsTF.textProperty().bindBidirectional(model.getPercOverlappingElementsProperty(), new NumberStringConverter());
		this.percOverlappingRowsTF.textProperty().bindBidirectional(model.getPercOverlappingRowsProperty(), new NumberStringConverter());
		this.percOverlappingColumnsTF.textProperty().bindBidirectional(model.getPercOverlappingColumnsProperty(), new NumberStringConverter());
		plaidCoherencySelecionada(new ActionEvent());


		//Extras
		this.percMissingBackgroundTF.textProperty().bindBidirectional(model.getPercMissingsBackgroundProperty(), new NumberStringConverter());
		this.percMissingBicsTF.textProperty().bindBidirectional(model.getPercMissingsBicsProperty(), new NumberStringConverter());
		this.percNoiseBackgroundTF.textProperty().bindBidirectional(model.getPercNoiseBackgroundProperty(), new NumberStringConverter());
		this.percNoiseBicsTF.textProperty().bindBidirectional(model.getPercNoiseBicsProperty(), new NumberStringConverter());
		this.noiseDeviationTF.textProperty().bindBidirectional(model.getNoiseDeviationProperty(), new NumberStringConverter());
		this.percErrorsBackgroundTF.textProperty().bindBidirectional(model.getPercErrorsBackgroundProperty(), new NumberStringConverter());
		this.percErrorsBicsTF.textProperty().bindBidirectional(model.getPercErrorsBicsProperty(), new NumberStringConverter());


		this.statusBar.progressProperty().bindBidirectional(model.getProgressProperty());
		this.statusT.setVisible(false);
		this.statusLB.setVisible(false);

		this.composedHeatMapB.setVisible(false);
		this.composedHeatMapB.setText("Show Symbolic");
		
		this.directoryChooserTF.textProperty().bindBidirectional(model.getDirectoryChooserProperty());
		model.setDirectory(System.getProperty("user.dir"));

		this.fileNameTF.textProperty().bindBidirectional(model.getFileNameProperty());
		model.setFileName("example_dataset");
		
		this.randomSeedTF.setVisible(false);
		this.randomSeedCB.setItems(model.getRandomSeedOptions());
		this.randomSeedCB.setValue(model.getRandomSeedEscolhida());
	}

	private void setNumericDatasetParametersVisible(boolean b) {

		this.minValueLB.setVisible(b);
		this.minValueTF.setVisible(b);
		this.maxValueLB.setVisible(b);
		this.maxValueTF.setVisible(b);
		this.dataTypeLB.setVisible(b);
		this.dataTypeCB.setVisible(b);
	}

	private void setSymbolicDatasetParametersVisible(boolean b) {

		this.symbolTypeLB.setVisible(b);
		this.symbolTypeCB.setVisible(b);
		this.symbolTypeParamLB.setVisible(b);
		this.symbolTypeParamTF.setVisible(b);
	}
	
	private void setHeterogeneousDatasetParametersVisible(boolean b) {

		this.featuresLB.setVisible(b);
		this.featuresSlicer.setVisible(b);
		this.numericLB.setVisible(b);
		this.numericPercLB.setVisible(b);
		this.symbolicLB.setVisible(b);
		this.symbolicPercLB.setVisible(b);
	}

	@FXML
	private void initialize() {
		UnaryOperator<Change> integerFilter = change -> {
			String newText = change.getControlNewText();
			if (newText.matches("[0-9]*")) { 
				return change;
			}
			return null;
		};

		UnaryOperator<Change> doubleFilter = change -> {
			String newText = change.getControlNewText();
			if (newText.matches("^-?((\\d+\\.?|\\.(?=\\d))?\\d{0,3})$")) { 
				return change;
			}
			return null;
		};



		this.numRowsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));
		this.numColumnsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.numBicsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percOverlappingBicsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.maxOverlappingBicsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percOverlappingElementsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percOverlappingRowsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percOverlappingColumnsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percMissingBackgroundTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percMissingBicsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percNoiseBackgroundTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percNoiseBicsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percErrorsBackgroundTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percErrorsBicsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.minValueTF.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(),
				0.0, doubleFilter));

		this.maxValueTF.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(),
				0.0, doubleFilter));

		this.rowDistParam1TF.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(),
				0.0, doubleFilter));

		this.rowDistParam2TF.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(),
				0.0, doubleFilter));

		this.columnDistParam1TF.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(),
				0.0, doubleFilter));

		this.columnDistParam2TF.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(),
				0.0, doubleFilter));

	}

	@FXML
	void dataTypeSelecionado(ActionEvent event) {

		if(model.getSingleBackgroundTypeEscolhido().equals("Discrete")) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error Dialog");
			alert.setHeaderText("Input data error");
			alert.setContentText("Real Valued data type cannot be used simultaneously with a discrete background");
			alert.showAndWait();

			this.dataTypeCB.setValue("Integer");
		}
		else
			model.setDataTypeEscolhido(this.dataTypeCB.getValue());
	}

	@FXML
	void singleBackgroundTypeSelecionado(ActionEvent event) {		
		String option = this.singleBackgroundTypeCB.getValue();
		model.setSingleBackgroundTypeEscolhido(option);

		if(option.equals("Normal")) {
			this.singleBackgroundParamsPane.setVisible(true);
			this.singleDiscreteProbsPane.setVisible(false);
		}
		else if(option.equals("Discrete")) {

			boolean error = false;
			String errorMsg = "";

			if(this.datasetTypeCB.getValue().equals("Numeric") && model.getDataTypeEscolhido().equals("Real Valued")) {
				error = true;
				errorMsg = "Discrete background is not available on Real Valued datasets";
			}
			else if(this.datasetTypeCB.getValue().equals("Numeric") && (this.maxValueTF.getText().isEmpty() || this.minValueTF.getText().isEmpty())) {
				error = true;
				errorMsg = "The dataset's min and mac values must be defined before setting a discrete background";

			}
			else if((this.datasetTypeCB.getValue().equals("Heterogeneous") || (this.datasetTypeCB.getValue().equals("Symbolic"))) && 
					this.symbolTypeParamTF.getText().isEmpty()) {
				error = true;
				errorMsg = "The dataset's symbols must be defined before setting a discrete background";
			}
			
			if(error) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error Dialog");
				alert.setHeaderText("Input data error");
				alert.setContentText(errorMsg);
				alert.showAndWait();

				this.singleBackgroundTypeCB.setValue("Uniform");
				model.setSingleBackgroundTypeEscolhido(this.singleBackgroundTypeCB.getValue());
				this.singleBackgroundParamsPane.setVisible(false);
				this.singleDiscreteProbsPane.setVisible(false);
			}
			else {
				this.singleBackgroundParamsPane.setVisible(false);
				this.singleDiscreteProbsPane.setVisible(true);

				initSingleDiscreteTable();
			}

		}
		else {			
			this.singleBackgroundParamsPane.setVisible(false);
			this.singleDiscreteProbsPane.setVisible(false);
		}

	}
	
	@FXML
	void composedBackgroundTypeSelecionado(ActionEvent event) {		
		String option = this.composedBackgroundTypeCB.getValue();
		model.setComposedBackgroundTypeEscolhido(option);

		if(option.equals("Normal")) {
			this.composedBackgroundParamsPane.setVisible(true);
			this.composedDiscreteProbsPane.setVisible(false);
		}
		else if(option.equals("Discrete")) {

			boolean error = false;
			String errorMsg = "";

			if((this.datasetTypeCB.getValue().equals("Heterogeneous") || this.datasetTypeCB.getValue().equals("Numeric")) && 
					model.getDataTypeEscolhido().equals("Real Valued")) {
				error = true;
				errorMsg = "Discrete background is not available on Real Valued datasets";
			}
			else if((this.datasetTypeCB.getValue().equals("Heterogeneous") || this.datasetTypeCB.getValue().equals("Numeric")) && 
					(this.maxValueTF.getText().isEmpty() || this.minValueTF.getText().isEmpty())) {
				error = true;
				errorMsg = "The dataset's min and mac values must be defined before setting a discrete background";

			}
			
			if(error) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error Dialog");
				alert.setHeaderText("Input data error");
				alert.setContentText(errorMsg);
				alert.showAndWait();

				this.composedBackgroundTypeCB.setValue("Uniform");
				model.setComposedBackgroundTypeEscolhido(this.composedBackgroundTypeCB.getValue());
				this.composedBackgroundParamsPane.setVisible(false);
				this.composedDiscreteProbsPane.setVisible(false);
			}
			else {
				this.composedBackgroundParamsPane.setVisible(false);
				this.composedDiscreteProbsPane.setVisible(true);

				initComposedDiscreteTable();
			}

		}
		else {			
			this.composedBackgroundParamsPane.setVisible(false);
			this.composedDiscreteProbsPane.setVisible(false);
		}

	}

	private void initSingleDiscreteTable() {

		ObservableList<DiscreteProbabilitiesTableView> symbols = FXCollections.observableArrayList();

		if(this.datasetTypeCB.getValue().equals("Numeric")) {
			for(int i = (int) model.getMinValue(); i <= (int) model.getMaxValue(); i++)
				symbols.add(new DiscreteProbabilitiesTableView(String.valueOf(i), String.valueOf(0.0)));
			System.out.println("hi from numeric");
		}
		else if((this.datasetTypeCB.getValue().equals("Heterogeneous") || (this.datasetTypeCB.getValue().equals("Symbolic"))) 
				&& model.getSymbolTypeEscolhido().equals("Default")) {
			for(int i = 1; i <= model.getNumberOfSymbols(); i++)
				symbols.add(new DiscreteProbabilitiesTableView(String.valueOf(i), String.valueOf(0.0)));
			System.out.println("hi from symbolic default " + symbols.size());
		}
		else {
			for(String s : model.getListOfSymbols())
				symbols.add(new DiscreteProbabilitiesTableView(s, String.valueOf(0.0)));
			System.out.println("hi from symbolic custom");
		}

		this.singleDiscreteProbsTV.setEditable(true);

		this.singleDiscreteProbsTV.setItems(symbols);

		this.singleSymbolTC.setCellValueFactory(new PropertyValueFactory<>("Symbol"));
		this.singleProbTC.setCellValueFactory(new PropertyValueFactory<>("Prob"));
		this.singleProbTC.setCellFactory(TextFieldTableCell.forTableColumn());

		this.singleProbTC.setOnEditCommit(e -> {
			e.getTableView().getItems().get(e.getTablePosition().getRow()).setProb(e.getNewValue());
		});
	}
	
	private void initComposedDiscreteTable() {

		ObservableList<DiscreteProbabilitiesTableView> symbols = FXCollections.observableArrayList();

		
		for(int i = (int) model.getMinValue(); i <= (int) model.getMaxValue(); i++)
			symbols.add(new DiscreteProbabilitiesTableView(String.valueOf(i), String.valueOf(0.0)));
		System.out.println("hi from numeric");
	

		this.composedDiscreteProbsTV.setEditable(true);

		this.composedDiscreteProbsTV.setItems(symbols);

		this.composedSymbolTC.setCellValueFactory(new PropertyValueFactory<>("Symbol"));
		this.composedProbTC.setCellValueFactory(new PropertyValueFactory<>("Prob"));
		this.composedProbTC.setCellFactory(TextFieldTableCell.forTableColumn());

		this.composedProbTC.setOnEditCommit(e -> {
			e.getTableView().getItems().get(e.getTablePosition().getRow()).setProb(e.getNewValue());
		});
	}

	@FXML
	void chooseDirectoryAction(ActionEvent event) {

		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(primaryStage);
		model.setDirectory(selectedDirectory.getAbsolutePath());
	}

	@FXML
	void rowStructureDistSelecionada(ActionEvent event) {
		model.setRowDistributionEscolhida(this.rowStructureDistCB.getValue());
		setDistributionLabels(this.rowDistParam1Label, this.rowDistParam2Label, this.rowStructureDistCB.getValue());
	}

	@FXML
	void columnStructureDistSelecionada(ActionEvent event) {
		model.setColumnDistributionEscolhida(this.columnStructureDistCB.getValue());
		setDistributionLabels(this.columnDistParam1Label, this.columnDistParam2Label, this.columnStructureDistCB.getValue());
	}

	@FXML
	void contiguitySelecionada(ActionEvent event) {
		model.setContiguityEscolhida(this.contiguityCB.getValue());
	}

	@FXML
	void randomSeedSelecionada(ActionEvent event) {
		
		model.setRandomSeedEscolhida(this.randomSeedCB.getValue());
		if(model.getRandomSeedEscolhida().equals("Yes")) {
			this.randomSeedTF.setVisible(true);
			this.randomSeedTF.setText("100");
		}
		else
			this.randomSeedTF.setVisible(false);
	}
	
	@FXML
	void plaidCoherencySelecionada(ActionEvent event) {
		model.setPlaidCoherencyEscolhida(this.plaidCoherencyCB.getValue());
		if(this.plaidCoherencyCB.getValue().equals("No Overlapping")) {
			this.percOverlappingBicsLB.setDisable(true);
			this.percOverlappingBicsTF.setDisable(true);
			this.maxOverlappingBicsLB.setDisable(true);
			this.maxOverlappingBicsTF.setDisable(true);
			this.percOverlappingElementsLB.setDisable(true);
			this.percOverlappingElementsTF.setDisable(true);
			this.percOverlappingRowsLB.setDisable(true);
			this.percOverlappingRowsTF.setDisable(true);
			this.percOverlappingColumnsLB.setDisable(true);
			this.percOverlappingColumnsTF.setDisable(true);
		}
		else {
			this.percOverlappingBicsLB.setDisable(false);
			this.percOverlappingBicsTF.setDisable(false);
			this.maxOverlappingBicsLB.setDisable(false);
			this.maxOverlappingBicsTF.setDisable(false);
			this.percOverlappingElementsLB.setDisable(false);
			this.percOverlappingElementsTF.setDisable(false);
			this.percOverlappingRowsLB.setDisable(false);
			this.percOverlappingRowsTF.setDisable(false);
			this.percOverlappingColumnsLB.setDisable(false);
			this.percOverlappingColumnsTF.setDisable(false);
		}

	}

	private void setDistributionLabels(Label l1, Label l2, String distEscolhida) {

		if(distEscolhida.equals("Normal")) {
			l1.setText("Mean: ");
			l2.setText("Std. Dev:");
		}
		else {
			l1.setText("Min: ");
			l2.setText("Max:");
		}
	}

	@FXML
	void datasetTypeSelecionado(ActionEvent event) {
		
		System.out.println("Hi from datasetTypeSelecionado!");
		
		model.setDatasetTypeEscolhido(this.datasetTypeCB.getValue());
		
		if(this.datasetTypeCB.getValue().equals("Symbolic")) {
			this.setComposedBackgroundParametersVisible(false);
			this.setSymbolicDatasetParametersVisible(true);
			this.setNumericDatasetParametersVisible(false);
			this.setHeterogeneousDatasetParametersVisible(false);
			this.singleBackgroundLB.setText("Symbolic Background");

			model.setSymbolicTypeBoolean(true);
			
			this.patternsTV.setItems(model.getSymbolicPatterns());
			model.updatePlaidCoherency(false);
			this.plaidCoherencyCB.setItems(model.getPlaidCoherency());
			this.plaidCoherencyCB.setValue(model.getPlaidCoherencyEscolhida());
		}
		else if(this.datasetTypeCB.getValue().equals("Numeric")) {
			this.setComposedBackgroundParametersVisible(false);
			this.setSymbolicDatasetParametersVisible(false);
			this.setNumericDatasetParametersVisible(true);
			this.setHeterogeneousDatasetParametersVisible(false);
			this.singleBackgroundLB.setText("Numeric Background");

			this.patternsTV.setItems(model.getNumericPatterns());
			model.setSymbolicTypeBoolean(false);
			model.updatePlaidCoherency(true);
			this.plaidCoherencyCB.setItems(model.getPlaidCoherency());
			this.plaidCoherencyCB.setValue(model.getPlaidCoherencyEscolhida());
		}
		else {
			this.singleBackgroundLB.setText("Symbolic Background");
			this.setComposedBackgroundParametersVisible(true);
			this.setHeterogeneousDatasetParametersVisible(true);
			this.setSymbolicDatasetParametersVisible(true);
			this.setNumericDatasetParametersVisible(true);
			
			this.patternsTV.setItems(model.getMixedPatterns());
			model.setSymbolicTypeBoolean(false);
			model.updatePlaidCoherency(true);
			this.plaidCoherencyCB.setItems(model.getPlaidCoherency());
			this.plaidCoherencyCB.setValue(model.getPlaidCoherencyEscolhida());
		}
		
		
		
		this.bicTypeTC.setCellValueFactory(new PropertyValueFactory<BiclusterPatternTableView, String>("biclusterType"));
		this.rowTC.setCellValueFactory(new PropertyValueFactory<BiclusterPatternTableView, String>("rowPattern"));
		this.columnTC.setCellValueFactory(new PropertyValueFactory<BiclusterPatternTableView, String>("columnPattern"));
		this.timeProfileTC.setCellValueFactory(new PropertyValueFactory<BiclusterPatternTableView, ComboBox<String>>("timeProfile"));
		this.selectTC.setCellValueFactory(new PropertyValueFactory<BiclusterPatternTableView, CheckBox>("select"));
	}

	private void setComposedBackgroundParametersVisible(boolean visible) {
		this.composedBackgroundLB.setText("Numeric Background");
		this.composedBackgroundLB.setVisible(visible);
		this.composedBackgroundTypeLB.setVisible(visible);
		this.composedBackgroundTypeCB.setVisible(visible);
		this.backgroundSeparator.setVisible(visible);
	}
	
	@FXML
	void symbolTypeSelected(ActionEvent event) {
		model.setSymbolTypeEscolhido(this.symbolTypeCB.getValue());
		if(model.getSymbolTypeEscolhido().equals("Default")){
			this.symbolTypeParamLB.setText("Number of Symbols:");
			this.symbolTypeParamTF.textProperty().unbindBidirectional(model.getListOfSymbolsProperty());
			this.symbolTypeParamTF.textProperty().bindBidirectional(model.getNumberOfSymbolsProperty(), new NumberStringConverter());
			this.symbolTypeParamTF.setPromptText("5");
		}
		else {
			this.symbolTypeParamLB.setText("List of Symbols:");
			this.symbolTypeParamTF.clear();
			this.symbolTypeParamTF.setPromptText("1,2,3,4,5");
			this.symbolTypeParamTF.textProperty().unbindBidirectional(model.getNumberOfSymbolsProperty());
			this.symbolTypeParamTF.textProperty().bindBidirectional(model.getListOfSymbolsProperty());
		}


	}


	@FXML
	void generateDatasetAction(ActionEvent event) {

		String errors = null;

		if(model.getSingleBackgroundTypeEscolhido().equals("Discrete")) {
			model.setSingleDiscreteProbabilities(this.singleDiscreteProbsTV.getItems());

			for(DiscreteProbabilitiesTableView p : model.getSingleDiscreteProbabilities())
				System.out.println(p.getSymbol() + " -> " + p.getProb());

		}
		
		if(this.composedBackgroundTypeCB.getValue().equals("Discrete")) {
			model.setComposedDiscreteProbabilities(this.composedDiscreteProbsTV.getItems());

			for(DiscreteProbabilitiesTableView p : model.getComposedDiscreteProbabilities())
				System.out.println(p.getSymbol() + " -> " + p.getProb());

		}


		errors = validateInput();
		
		if(this.randomSeedCB.getValue().equals("Yes"))
			gBicService.initializeRandom(Integer.parseInt(this.randomSeedTF.getText()));
		else
			gBicService.initializeRandom(-1);

		GenerateDatasetTask<Void> task = new GenerateDatasetTask<Void>(this.gBicService) {

			@Override protected Void call() throws Exception {

				gBicService.setProgressUpdate((workDone, totalWork) -> updateProgress(workDone, totalWork));
				gBicService.setMessageUpdate((message) -> updateMessage(message));

				//call triGen setters
				double singleBackgroundParam1 = 0;
				double composedBackgroundParam1 = 0;
				double singleBackgroundParam2 = 0;
				double composedBackgroundParam2 = 0;
				double[] singleBackgroundParam3 = null;
				double[] composedBackgroundParam3 = null;

				//Set background type and validate input
				if(model.getSingleBackgroundTypeEscolhido().equals("Normal")) {
					singleBackgroundParam1 = model.getSingleDistMean();
					singleBackgroundParam2 = model.getSingleDistStd();
				}
				else if(model.getSingleBackgroundTypeEscolhido().equals("Discrete")) {
					int index = 0;
					singleBackgroundParam3 = new double[model.getSingleDiscreteProbabilities().size()];
					for(DiscreteProbabilitiesTableView p : model.getSingleDiscreteProbabilities()) {
						singleBackgroundParam3[index] = Double.parseDouble(p.getProb());
						index++;
					}
				}
				
				if(datasetTypeCB.getValue().equals("Numeric")) {

					this.gBicService.setDatasetType("Numeric");
					boolean realValued = model.getDataTypeEscolhido().equals("Real Valued");

					this.gBicService.setDatasetProperties(model.getNumRows(), model.getNumColumns(), realValued, 
							model.getMinValue(), model.getMaxValue(), model.getSingleBackgroundTypeEscolhido(), singleBackgroundParam1,
							singleBackgroundParam2, singleBackgroundParam3);
				}
				else if(datasetTypeCB.getValue().equals("Symbolic")) {
					this.gBicService.setDatasetType("Symbolic");
					if(model.getSymbolTypeEscolhido().equals("Default")) {
						this.gBicService.setDatasetProperties(model.getNumRows(), model.getNumColumns(), true, model.getNumberOfSymbols(), 
								null, model.getSingleBackgroundTypeEscolhido(), singleBackgroundParam1, singleBackgroundParam2, singleBackgroundParam3);
					}
					else {
						String[] symbols = model.getListOfSymbols().toArray(new String[0]);
						this.gBicService.setDatasetProperties(model.getNumRows(), model.getNumColumns(), false, -1, 
								symbols, model.getSingleBackgroundTypeEscolhido(), singleBackgroundParam1, singleBackgroundParam2, singleBackgroundParam3);
					}
				}
				else {
					this.gBicService.setDatasetType("Heterogeneous");
					boolean realValued = model.getDataTypeEscolhido().equals("Real Valued");

					//Set background type and validate input for heterogeneous dataset
					if(model.getComposedBackgroundTypeEscolhido().equals("Normal")) {
						composedBackgroundParam1 = model.getComposedDistMean();
						composedBackgroundParam2 = model.getComposedDistStd();
					}
					else if(model.getComposedBackgroundTypeEscolhido().equals("Discrete")) {
						int index = 0;
						composedBackgroundParam3 = new double[model.getComposedDiscreteProbabilities().size()];
						for(DiscreteProbabilitiesTableView p : model.getComposedDiscreteProbabilities()) {
							composedBackgroundParam3[index] = Double.parseDouble(p.getProb());
							index++;
						}
					}

					if(model.getSymbolTypeEscolhido().equals("Default")) {
						this.gBicService.setDatasetProperties(model.getNumRows(), model.getNumColumns(), featuresSlicer.getValue()/100, realValued, model.getMinValue(), model.getMaxValue(), 
								model.getNumberOfSymbols(), null, model.getSingleBackgroundTypeEscolhido(), singleBackgroundParam1, singleBackgroundParam2, 
								singleBackgroundParam3, model.getComposedBackgroundTypeEscolhido(), composedBackgroundParam1, composedBackgroundParam2,
								composedBackgroundParam3);
					
					}
					else {
						String[] symbols = model.getListOfSymbols().toArray(new String[0]);
						this.gBicService.setDatasetProperties(model.getNumRows(), model.getNumColumns(), featuresSlicer.getValue()/100, realValued, model.getMinValue(), model.getMaxValue(), 
								-1, symbols, model.getSingleBackgroundTypeEscolhido(), singleBackgroundParam1, singleBackgroundParam2, 
								singleBackgroundParam3, model.getComposedBackgroundTypeEscolhido(), composedBackgroundParam1, composedBackgroundParam2,
								composedBackgroundParam3);
					
					}
				}

				this.gBicService.setBiclustersProperties(model.getNumBics(), model.getRowDistributionEscolhida(), model.getRowDistParam1(), 
						model.getRowDistParam2(), model.getColumnDistributionEscolhida(), model.getColumnDistParam1(), model.getColumnDistParam2(),
						model.getContiguityEscolhida());

				//set patterns
				List<BiclusterPatternWrapper> listPatterns = new ArrayList<>();
				List<BiclusterPatternTableView> availablePatterns = null;

				if(datasetTypeCB.getValue().equals("Symbolic"))
					availablePatterns = model.getSymbolicPatterns();
				else if(datasetTypeCB.getValue().equals("Numeric"))
					availablePatterns = model.getNumericPatterns();
				else {
					availablePatterns = model.getSymbolicPatterns();
					availablePatterns.addAll(model.getNumericPatterns());
				}

				for(BiclusterPatternTableView p : availablePatterns) {
					if(p.getSelect().isSelected()) {
						BiclusterPatternWrapper bic;
						if(p.getColumnPattern().equals("Order Preserving"))
							bic = this.gBicService.new BiclusterPatternWrapper(p.getBiclusterType(), p.getRowPattern(), p.getColumnPattern(), p.getTimeProfile().getValue(), null);
						else
							bic = this.gBicService.new BiclusterPatternWrapper(p.getBiclusterType(), p.getRowPattern(), p.getColumnPattern(), null);
						System.out.println(bic.toString());
						listPatterns.add(bic);
					}
				}
				this.gBicService.setBiclusterPatterns(listPatterns, datasetTypeCB.getValue());

				this.gBicService.setOverlappingSettings(model.getPlaidCoherencyEscolhida(), model.getPercOverlappingBics() / 100, model.getMaxOverlappingBics(),
						model.getPercOverlappingElements() / 100, model.getPercOverlappingRows() / 100, model.getPercOverlappingColumns() / 100);
				
				this.gBicService.setQualitySettings(model.getPercMissingsBackground() / 100, model.getPercMissingsBics() / 100, 
						model.getPercNoiseBackground() / 100, model.getPercNoiseBics() / 100, model.getNoiseDeviation(), 
						model.getPercErrorsBackground() / 100, model.getPercErrorsBics() / 100);

				if(!model.getFileName().isEmpty()){
					this.gBicService.setFilename(model.getFileName());
				}

				if(!model.getDirectory().isEmpty()) {
					if(model.getDirectory().charAt(model.getDirectory().length()-1) == '/')
						this.gBicService.setPath(model.getDirectory());
					else
						this.gBicService.setPath(model.getDirectory() + "/");
				}
				
				if(singleFileRB.isSelected())
					this.gBicService.setSingleFileOutput(true);
				else
					this.gBicService.setSingleFileOutput(false);

				try {
					if(datasetTypeCB.getValue().equals("Symbolic"))
						this.gBicService.generateSymbolicDataset();
					else if(datasetTypeCB.getValue().equals("Numeric"))
						this.gBicService.generateNumericDataset();
					else
						this.gBicService.generateHeterogeneousDataset();
					
					System.out.println(gBicService.getGeneratedDataset().getNumBics());
					System.out.println(model.getNumBics());
					
					boolean noSpace = gBicService.getGeneratedDataset().getNumBics() < model.getNumBics();
					
					if(noSpace) {
						model.setNumBics(gBicService.getGeneratedDataset().getNumBics());
					}
					
					bicIDCB.getItems().clear();
					visualizationTab.setDisable(false);
					model.setBiclusterIDList();
					bicIDCB.setItems(model.getBiclusterIDs());
					bicIDCB.setValue(model.getBiclusterIDs().get(0));
					
					updateProgress(100, 100);
					updateMessage("Completed!");
					
					//patternsTV.getItems().clear();
					
					Task<Void> successBox = new Task<Void>() {
						@Override
						protected Void call() throws Exception {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									
									int generatedBics = gBicService.getGeneratedDataset().getNumBics();
									
									if(!noSpace) {
										Alert alert = new Alert(AlertType.INFORMATION);
										alert.setTitle("Information Dialog");
										alert.setHeaderText("Dataset generated!");
										alert.showAndWait();
									}
									else {
										Alert alert = new Alert(AlertType.WARNING);
										alert.setTitle("Warning Dialog");
										alert.setHeaderText("Due to space restrictions the dataset was generated with only " +
												generatedBics + " biclusters!\nTry planting less, or smaller biclusters, or incresing"
														+ " the dataset's size!");
										alert.showAndWait();
									}
								}
							});
							return null;
						}
					};
					
					Thread sb = new Thread(successBox);
					sb.start();

				} catch (Exception e) {

					Task<Void> insuccessBox = new Task<Void>() {
						@Override
						protected Void call() throws Exception {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									Alert alert = new Alert(AlertType.ERROR);
									alert.setTitle("Error Dialog");
									alert.setHeaderText("Input data error");
									e.printStackTrace();
									alert.setContentText(e.getMessage());
									alert.showAndWait();	
								}
							});
							return null;
						}
					};

					Thread ib = new Thread(insuccessBox);
					ib.start();
				}	
				return null;
			}
		};


		Thread t = new Thread(task);
		if(errors.isBlank()) {
			this.statusBar.progressProperty().bind(task.progressProperty());
			this.statusT.setVisible(true);
			this.statusLB.setVisible(true);
			this.statusLB.textProperty().bind(task.messageProperty());
			System.out.println("Gonna start the task...");
			t.start();
		}
		else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error Dialog");
			alert.setHeaderText("Input data error");
			alert.setContentText(errors);
			alert.showAndWait();	
		}
	}

	public void setStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	/**
	 * Valida o input e coloca as mensagens de erro numa String
	 * @return String com os erros encontrados
	 */
	private String validateInput() {

		StringBuilder messages = new StringBuilder();

		// ** Validate Dataset Properties **

		List<BiclusterPatternTableView> patterns = null;
		boolean errorsOnDatasetProperties = false;

		if(datasetTypeCB.getValue().equals("Symbolic")) {

			if(model.getSymbolTypeEscolhido().equals("Default")) {
				messages.append(InputValidation.validateDatasetSettings(this.numRowsTF.getText(), this.numColumnsTF.getText(), 
						this.symbolTypeParamTF.getText(), new ArrayList<String>()));
			}
			else {
				messages.append(InputValidation.validateDatasetSettings(this.numRowsTF.getText(), this.numColumnsTF.getText(), 
						null, (List<String>) model.getListOfSymbols()));
			}	

			patterns = model.getSymbolicPatterns();
		}
		else if(datasetTypeCB.getValue().equals("Numeric")) {

			messages.append(InputValidation.validateDatasetSettings(this.numRowsTF.getText(), this.numColumnsTF.getText(),
					this.minValueTF.getText(), this.maxValueTF.getText()));

			patterns = model.getNumericPatterns();
		}
		else {
			//messages.append(InputValidation.validateDatasetSettings(this.numRowsTF.getText(), this.numColumnsTF.getText(),
			//		this.minValueTF.getText(), this.maxValueTF.getText()));

			patterns = model.getNumericPatterns();
			patterns.addAll(model.getSymbolicPatterns());
		}

		errorsOnDatasetProperties = messages.length() > 1;

		boolean selectedPattern = false;
		for(BiclusterPatternTableView p : patterns)
			if(p.getSelect().isSelected())
				selectedPattern = true;
		if(!selectedPattern)
			messages.append("(Bicluster Patterns) Error: At least one pattern should be selected!\n");

		if(model.getSingleBackgroundTypeEscolhido().equals("Normal")) {
			messages.append(InputValidation.validateBackgroundSettings(this.singleDistMeanTF.getText(), this.singleDistStdTF.getText()));
		}
		else if(model.getSingleBackgroundTypeEscolhido().equals("Discrete")){
			List<DiscreteProbabilitiesTableView> probs = model.getSingleDiscreteProbabilities();
			messages.append(InputValidation.validateBackgroundSettings(probs));
		}

		// ** Validate Biclusters Structure ** 

		if(errorsOnDatasetProperties) {
			messages.append(InputValidation.validateBiclusterStructure(model.getNumRows(), model.getNumColumns(), 
					model.getRowDistributionEscolhida(), model.getRowDistParam1(), model.getRowDistParam2(), 
					model.getColumnDistributionEscolhida(), model.getColumnDistParam1(), model.getColumnDistParam2()));
		}

		// ** Validate Overlapping **
		messages.append(InputValidation.validateOverlappingSettings(model.getPlaidCoherencyEscolhida(), model.getPercOverlappingBics(), 
				model.getMaxOverlappingBics(), model.getPercOverlappingElements(), model.getPercOverlappingRows(), 
				model.getPercOverlappingColumns(), model.getNumBics()));

		// ** Validate Quality **
		messages.append(InputValidation.validateMissingNoiseAndErrorsOnBackground(model.getPercMissingsBackground(), model.getPercNoiseBackground(),
				model.getPercErrorsBackground()));

		if(model.isSymbolic()) {
			if(model.getSymbolTypeEscolhido().equals("Default"))
				messages.append(InputValidation.validateMissingNoiseAndErrorsOnPlantedBics(model.getPercMissingsBics(), model.getPercNoiseBics(),
						model.getPercErrorsBics(), (int) model.getNoiseDeviation(), model.getNumberOfSymbols()));
			else
				messages.append(InputValidation.validateMissingNoiseAndErrorsOnPlantedBics(model.getPercMissingsBics(), model.getPercNoiseBics(),
						model.getPercErrorsBics(), (int) model.getNoiseDeviation(), model.getListOfSymbols().size()));
		}
			
		else
			messages.append(InputValidation.validateMissingNoiseAndErrorsOnPlantedBics(model.getPercMissingsBics(), model.getPercNoiseBics(),
					model.getPercErrorsBics(), model.getNoiseDeviation(), model.getMinValue(), model.getMaxValue()));

		return messages.toString();
	}

	@FXML
	void changeTriclusterVisualization(ActionEvent event) {
		
		if(this.bicIDCB.getItems().size() > 0) {
			int bicID = Integer.parseInt(this.bicIDCB.getValue().split(" ")[1].strip());
			//if(!this.triclusterVizData.containsKey(tricID))
				//this.triclusterVizData.put(tricID, generateTriclusterVizData(tricID));
	
			this.currentTriclusterVisualization = bicID;
			//this.currentSliceVisualization = 0;
			
			//Restart context buttons
			//this.nextCtxB.setDisable(false);
			//this.previousCtxB.setDisable(true);
			
			JSONObject bic = gBicService.getBiclustersJSON().getJSONObject(String.valueOf(currentTriclusterVisualization));
			
			if(bic.getString("Type").contentEquals("Symbolic")) {
				this.composedHeatMapB.setVisible(false);
				this.singleHeatMapB.setText("Show");
			}
			else if(bic.getString("Type").contentEquals("Numeric")) {
				this.composedHeatMapB.setVisible(false);
				this.singleHeatMapB.setText("Show");
			}
			else {
				this.composedHeatMapB.setVisible(true);
				this.singleHeatMapB.setText("Show Numeric");
				this.composedHeatMapB.setText("Show Symbolic");
			}
			
			Task<Void> nextContext = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							
							boolean isMixed = bic.has("NumericColumns");
							
							bicSummaryTF.getChildren().clear();
							
						
							Text title = new Text("Tricluster " + bicID + " information: \n\n");
							title.setFont(Font.font("System", FontPosture.REGULAR, 14));
							
							Text type = new Text("Type:\t\t\t\t" + bic.getString("Type") + "\n\n");
							type.setFont(Font.font("System", FontPosture.REGULAR, 14));
							
							Text dimensions = new Text("Dimensions:\t\t" + bic.getInt("#rows") + "x" + bic.getInt("#columns") + "\n\n");
							dimensions.setFont(Font.font("System", FontPosture.REGULAR, 14));
							
							Text rows = null;
							
							if(bic.has("X"))
								rows = new Text("Rows:\t\t\t\t" + bic.getJSONArray("X").toString() + "\n\n");
							else
								rows = new Text("Rows:\t\t\t\t" + bic.getJSONArray("Rows").toString() + "\n\n");
							rows.setFont(Font.font("System", FontPosture.REGULAR, 14));
							
							bicSummaryTF.getChildren().addAll(title, type, dimensions, rows);
							
							if(isMixed) {
								Text numCols = new Text("Numeric Columns:\t\t\t" + bic.getJSONArray("NumericColumns").toString() + "\n\n");
								Text symbCols = new Text("Symbolic Columns:\t\t\t" + bic.getJSONArray("SymbolicColumns").toString() + "\n\n");
								rows.setFont(Font.font("System", FontPosture.REGULAR, 14));
								numCols.setFont(Font.font("System", FontPosture.REGULAR, 14));
								symbCols.setFont(Font.font("System", FontPosture.REGULAR, 14));
								
								bicSummaryTF.getChildren().addAll(numCols, symbCols);
								
								JSONObject numericProps = bic.getJSONObject("NumericProperties");
								JSONObject symbolicProps = bic.getJSONObject("SymbolicProperties");
								
								Text numRowPattern = new Text("Numeric Row Pattern:\t\t" + numericProps.getString("RowPattern").toString() + "\n\n");
								Text numColPattern = new Text("Numeric Column Pattern:\t" + numericProps.getString("ColumnPattern").toString() + "\n\n");
								
								numRowPattern.setFont(Font.font("System", FontPosture.REGULAR, 14));
								numColPattern.setFont(Font.font("System", FontPosture.REGULAR, 14));
								
								if(numericProps.has("TimeProfile")) {
									Text timeProfile = new Text("Numeric TimeProfile:\t\t" + numericProps.getString("TimeProfile").toString() + "\n\n");
									bicSummaryTF.getChildren().addAll(numRowPattern, numColPattern, timeProfile);
								}
								else {
									bicSummaryTF.getChildren().addAll(numRowPattern, numColPattern);
								}
								
								Text symbRowPattern = new Text("Symbolic Row Pattern:\t\t" + symbolicProps.getString("RowPattern").toString() + "\n\n");
								Text symbColPattern = new Text("Symbolic Column Pattern:\t" + symbolicProps.getString("ColumnPattern").toString() + "\n\n");
								
								symbRowPattern.setFont(Font.font("System", FontPosture.REGULAR, 14));
								symbColPattern.setFont(Font.font("System", FontPosture.REGULAR, 14));
								
								if(symbolicProps.has("TimeProfile")) {
									Text timeProfile = new Text("Symbolic TimeProfile:\t\t" + symbolicProps.getString("TimeProfile").toString() + "\n\n");
									bicSummaryTF.getChildren().addAll(symbRowPattern, symbColPattern, timeProfile);
								}
								else {
									bicSummaryTF.getChildren().addAll(symbRowPattern, symbColPattern);
								}
								
								Text plaid = new Text("Plaid Coherency:\t" + numericProps.getString("PlaidCoherency").toString() + "\n\n");
								plaid.setFont(Font.font("System", FontPosture.REGULAR, 14));
								
								bicSummaryTF.getChildren().addAll(plaid);
							}
							else {
								Text cols = new Text("Columns:\t\t\t" + bic.getJSONArray("Y").toString() + "\n\n");
								cols.setFont(Font.font("System", FontPosture.REGULAR, 14));
								
								Text rowPattern = new Text("Row Pattern:\t\t" + bic.getString("RowPattern").toString() + "\n\n");
								Text colPattern = new Text("Column Pattern:\t" + bic.getString("ColumnPattern").toString() + "\n\n");
								
								rowPattern.setFont(Font.font("System", FontPosture.REGULAR, 14));
								colPattern.setFont(Font.font("System", FontPosture.REGULAR, 14));
								
								bicSummaryTF.getChildren().addAll(cols, rowPattern, colPattern);
								
								if(bic.getString("ColumnPattern").toString().equals("OrderPreserving")) {
									Text timeProfile = new Text("Time Profile:\t\t" + bic.getString("TimeProfile").toString() + "\n\n");
									timeProfile.setFont(Font.font("System", FontPosture.REGULAR, 14));
									bicSummaryTF.getChildren().add(timeProfile);
								}
								
								Text plaid = new Text("Plaid Coherency:\t" + bic.getString("PlaidCoherency").toString() + "\n\n");
								plaid.setFont(Font.font("System", FontPosture.REGULAR, 14));
								
								bicSummaryTF.getChildren().addAll(plaid);
							}

							
							if(bic.has("Seed") || isMixed) {
								
								JSONObject o = null;
								
								if(bic.has("Seed"))
									o = bic;
								else if(bic.getJSONObject("NumericProperties").has("Seed"))
									o = bic.getJSONObject("NumericProperties");
								
								if(o != null) {
									Text seed = new Text("Seed:\t\t\t\t" + o.getString("Seed").toString() + "\n\n");
									Text rowFactors = new Text("Row Factors:\t\t" + o.getString("RowFactors").toString() + "\n\n");
									Text colFactors = new Text("Column Factors:\t" + o.getString("ColumnFactors").toString() + "\n\n");
									
									seed.setFont(Font.font("System", FontPosture.REGULAR, 14));
									rowFactors.setFont(Font.font("System", FontPosture.REGULAR, 14));
									colFactors.setFont(Font.font("System", FontPosture.REGULAR, 14));
										
									bicSummaryTF.getChildren().addAll(seed, rowFactors, colFactors);
								}
							}
							
							Text missings = new Text("Missings:\t\t\t" + bic.getString("%Missings").toString() + "%" + "\n\n");
							Text noise = new Text("Noise:\t\t\t\t" + bic.getString("%Noise").toString() + "%" + "\n\n");
							Text errors = new Text("Errors:\t\t\t\t" + bic.getString("%Errors").toString() + "%" + "\n\n");
							
							
							missings.setFont(Font.font("System", FontPosture.REGULAR, 14));
							noise.setFont(Font.font("System", FontPosture.REGULAR, 14));
							errors.setFont(Font.font("System", FontPosture.REGULAR, 14));
							
							bicSummaryTF.getChildren().addAll(missings, noise, errors);
						}
					});
					return null;
				}
			};

			Thread nc = new Thread(nextContext);
			nc.start();
			
			
			//showTriclusterVisualization(tricID, this.currentSliceVisualization);
			
			//Triclusters Patterns
			//this.heatMapTV.setItems(generateTriclusterVizData(tricID));
			//this.contextIDTC.setCellValueFactory(new PropertyValueFactory<HeatMapTableView, Integer>("contextID"));
			//this.showButtonTC.setCellValueFactory(new PropertyValueFactory<>("show"));
			
		}
	}
	
	private HeatMapTableView generateTriclusterVizData(int id) {

		HeatMapTableView bicChart = null;
		System.out.println("Generating chartData");
		
		if(gBicService.getBiclustersJSON().getJSONObject(String.valueOf(id)).getString("Type").equals("Mixed")) {
			bicChart = generateNumericComponentBiclusterViz(id);
		} 
		else {
			bicChart = generateSingleBiclusterViz(id);
		}
		
		return bicChart;
	}
	
	private HeatMapTableView generateSingleBiclusterViz(int id) {
		
		Dataset dataset = gBicService.getGeneratedDataset();
		HeatMapTableView bicChart = null;
		JSONArray data = gBicService.getBiclustersJSON().getJSONObject(String.valueOf(id)).getJSONArray("Data");
		String bicType = gBicService.getBiclustersJSON().getJSONObject(String.valueOf(id)).getString("Type");
		//get the tricluster to obtain rows/columns/contexts
		Bicluster t = dataset.getBiclusterById(id);
		//ObservableList<HeatMapTableView> contextList = FXCollections.observableArrayList();
		
		List<String> yTicks = new ArrayList<>();
		List<String> xTicks = new ArrayList<>();

		for(Integer r : t.getRows())
			yTicks.add("x" + r);

		for(Integer c : t.getColumns())
			xTicks.add("y" + c);
		Collections.reverse(yTicks);

		//int chartNumber = 1;
		String title = "Bicluster " + id;
		HeatMapData sliceHeatMap = null;
		
		if(bicType.equals("Symbolic")) {
			String[][] chartData = new String[t.getRows().size()][t.getColumns().size()];
			System.out.println(data.toString());
			System.out.println(data.length());
			System.out.println(data.getJSONArray(0).length());
			
			for(int s = 0; s < data.length(); s++) 
				for(int c = 0; c < data.getJSONArray(s).length(); c++) {
					System.out.println("Row " + s + " Column " + c);
					chartData[s][c] = data.getJSONArray(s).get(c).toString();
				}
			
			String[] alphabet = null;
			if(datasetTypeCB.getValue().equals("Heterogeneous"))
				alphabet = ((HeterogeneousDataset)this.gBicService.getGeneratedDataset()).getAlphabet();
			else
				alphabet = ((SymbolicDataset)this.gBicService.getGeneratedDataset()).getAlphabet();
			
			bicChart = new SymbolicHeatMapTableView(xTicks, yTicks, alphabet, title, chartData, id);
		}
		else {
			Number[][] chartData = new Number[t.getRows().size()][t.getColumns().size()];
			
			for(int s = 0; s < data.length(); s++) {
				for(int c = 0; c < data.getJSONArray(s).length(); c++) {
					if(data.getJSONArray(s).get(c) == null)
						chartData[s][c] = null;
					else {
						String value = data.getJSONArray(s).get(c).toString();
						value = value.replaceAll(",","");
						if(value.equals(""))
							chartData[s][c] = null;
						else
							chartData[s][c] = (Number) new Double(value);
					}
						
				}
			}
			double min = 0;
			double max = 0;
			
			if(datasetTypeCB.getValue().equals("Heterogeneous")) {
				min = ((HeterogeneousDataset) this.gBicService.getGeneratedDataset()).getMinM().doubleValue();
				max = ((HeterogeneousDataset) this.gBicService.getGeneratedDataset()).getMaxM().doubleValue();
			}
			else {
				min = ((NumericDataset) this.gBicService.getGeneratedDataset()).getMinM().doubleValue();
				max = ((NumericDataset) this.gBicService.getGeneratedDataset()).getMaxM().doubleValue();
			}
			
			bicChart = new NumericHeatMapTableView(xTicks, yTicks, min, max, title, chartData, id);
		}
		
		return bicChart;
	}
	
	private HeatMapTableView generateNumericComponentBiclusterViz(int id) {
		
		Dataset dataset = gBicService.getGeneratedDataset();
		HeatMapTableView bicChart = null;
		JSONArray data = gBicService.getBiclustersJSON().getJSONObject(String.valueOf(id)).getJSONArray("Data");
	
		//get the tricluster to obtain rows/columns/contexts
		MixedBicluster t = (MixedBicluster) dataset.getBiclusterById(id);
		//ObservableList<HeatMapTableView> contextList = FXCollections.observableArrayList();
		
		List<String> yTicks = new ArrayList<>();
		List<String> xTicks = new ArrayList<>();

		SortedSet<Integer> cols = t.getNumericComponent().getColumns();
		
		for(Integer r : t.getRows())
			yTicks.add("x" + r);

		for(Integer c : cols)
			xTicks.add("y" + c);
		Collections.reverse(yTicks);

		//int chartNumber = 1;
		String title = "Bicluster " + id;
		HeatMapData sliceHeatMap = null;
		
		
		Number[][] chartData = new Number[t.getRows().size()][cols.size()];
			
		for(int s = 0; s < data.length(); s++) {
			for(int c = 0; c < cols.size(); c++) {
				if(data.getJSONArray(s).get(c) == null)
					chartData[s][c] = null;
				else {
					String value = data.getJSONArray(s).get(c).toString();
					value = value.replaceAll(",","");
					if(value.equals(""))
						chartData[s][c] = null;
					else
						chartData[s][c] = (Number) new Double(value);
				}
					
			}
		}
		
		double min = ((HeterogeneousDataset) this.gBicService.getGeneratedDataset()).getMinM().doubleValue();
		double max = ((HeterogeneousDataset) this.gBicService.getGeneratedDataset()).getMaxM().doubleValue();
		
		bicChart = new NumericHeatMapTableView(xTicks, yTicks, min, max, title, chartData, id);
	
		
		return bicChart;
	}
	
	private HeatMapTableView generateSymbolicComponentBiclusterViz(int id) {
		
		Dataset dataset = gBicService.getGeneratedDataset();
		HeatMapTableView bicChart = null;
		JSONArray data = gBicService.getBiclustersJSON().getJSONObject(String.valueOf(id)).getJSONArray("Data");
	
		//get the tricluster to obtain rows/columns/contexts
		MixedBicluster t = (MixedBicluster) dataset.getBiclusterById(id);
		//ObservableList<HeatMapTableView> contextList = FXCollections.observableArrayList();
		
		List<String> yTicks = new ArrayList<>();
		List<String> xTicks = new ArrayList<>();

		SortedSet<Integer> cols = t.getSymbolicComponent().getColumns();
		
		for(Integer r : t.getRows())
			yTicks.add("x" + r);

		for(Integer c : cols)
			xTicks.add("y" + c);
		Collections.reverse(yTicks);

		//int chartNumber = 1;
		String title = "Bicluster " + id;
		
		String[][] chartData = new String[t.getRows().size()][cols.size()];
		int offset = 0;
		for(int s = 0; s < data.length(); s++) { 
			offset = data.getJSONArray(s).length() - cols.size();
			for(int c = 0; c < cols.size(); c++) {
				chartData[s][c] = data.getJSONArray(s).get(c + offset).toString();
			}
		}
		
		String[] alphabet = ((HeterogeneousDataset)this.gBicService.getGeneratedDataset()).getAlphabet();
		
		bicChart = new SymbolicHeatMapTableView(xTicks, yTicks, alphabet, title, chartData, id);
		
		return bicChart;
	}
	
	@FXML
	private void showSingleHeatMap(ActionEvent event) {
		
		int bicID = Integer.parseInt(this.bicIDCB.getValue().split(" ")[1].strip());
		HeatMapTableView chartData = generateTriclusterVizData(bicID);
		chartData.produceChart();
		
		
	}
	
	@FXML
	private void showComposedHeatMap(ActionEvent event) {
		
		int bicID = Integer.parseInt(this.bicIDCB.getValue().split(" ")[1].strip());
		HeatMapTableView chartData = generateSymbolicComponentBiclusterViz(bicID);
		chartData.produceChart();
		
		
	}
}
