package com.gbic.app.GBic.controllers;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gbic.app.GBic.models.MenuPrincipalModel;
import com.gbic.domain.dataset.Dataset;
import com.gbic.domain.dataset.NumericDataset;
import com.gbic.domain.dataset.SymbolicDataset;
import com.gbic.domain.tricluster.Tricluster;
import com.gbic.service.GBicService;
import com.gbic.service.GenerateDatasetTask;
import com.gbic.service.GBicService.TriclusterPatternWrapper;
import com.gbic.utils.DiscreteProbabilitiesTableView;
import com.gbic.utils.HeatMapData;
import com.gbic.utils.HeatMapTableView;
import com.gbic.utils.InputValidation;
import com.gbic.utils.NumericHeatMapTableView;
import com.gbic.utils.SymbolicHeatMapTableView;
import com.gbic.utils.TriclusterPatternTableView;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
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
	@FXML private TextField numContextsTF;
	@FXML private RadioButton symbolicTypeRB;
	@FXML private RadioButton numericTypeRB;
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
	@FXML private ComboBox<String> backgroundTypeCB;
	@FXML private TextField distMeanTF;
	@FXML private TextField distStdTF;
	@FXML private Label distMeanLabel;
	@FXML private Label distStdLabel;
	@FXML private Pane backgroundParamsPane;
	@FXML private Pane discreteProbsPane;

	@FXML private TableView<DiscreteProbabilitiesTableView> discreteProbsTV;
	@FXML private TableColumn<DiscreteProbabilitiesTableView, String> symbolTC;
	@FXML private TableColumn<DiscreteProbabilitiesTableView, String> probTC;

	//Triclusters Properties
	@FXML private TextField numTricsTF;
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

	@FXML private ComboBox<String> contextStructureDistCB;
	@FXML private Label contextDistParam1Label;
	@FXML private TextField contextDistParam1TF;
	@FXML private Label contextDistParam2Label;
	@FXML private TextField contextDistParam2TF;

	@FXML private ComboBox<String> contiguityCB;

	//Triclusters Patterns
	@FXML private TableView<TriclusterPatternTableView> patternsTV;
	@FXML private TableColumn<TriclusterPatternTableView, Integer> numTC;
	@FXML private TableColumn<TriclusterPatternTableView, String> rowTC;
	@FXML private TableColumn<TriclusterPatternTableView, String> columnTC;
	@FXML private TableColumn<TriclusterPatternTableView, String> contextTC;
	@FXML private TableColumn<TriclusterPatternTableView, ComboBox<String>> timeProfileTC;
	@FXML private TableColumn<TriclusterPatternTableView, Label> exampleTC;
	@FXML private TableColumn<TriclusterPatternTableView, CheckBox> selectTC;
	ObservableList<TriclusterPatternTableView> patternList;

	//Overlapping
	@FXML private ComboBox<String> plaidCoherencyCB;
	@FXML private Label percOverlappingTricsLB;
	@FXML private TextField percOverlappingTricsTF;
	@FXML private Label maxOverlappingTricsLB;
	@FXML private TextField maxOverlappingTricsTF;
	@FXML private Label percOverlappingElementsLB;
	@FXML private TextField percOverlappingElementsTF;
	@FXML private Label percOverlappingRowsLB;
	@FXML private TextField percOverlappingRowsTF;
	@FXML private Label percOverlappingColumnsLB;
	@FXML private TextField percOverlappingColumnsTF;
	@FXML private Label percOverlappingContextsLB;
	@FXML private TextField percOverlappingContextsTF;

	//Extras
	@FXML private TextField percMissingBackgroundTF;
	@FXML private TextField percMissingTricsTF;
	@FXML private TextField percNoiseBackgroundTF;
	@FXML private TextField percNoiseTricsTF;
	@FXML private TextField noiseDeviationTF;
	@FXML private TextField percErrorsBackgroundTF;
	@FXML private TextField percErrorsTricsTF;

	@FXML private ProgressBar statusBar;
	@FXML private Text statusT;
	@FXML private Label statusLB;

	//Output
	@FXML private RadioButton singleFileRB;
	@FXML private RadioButton numericFileRB;
	@FXML private TextField directoryChooserTF;
	@FXML private Button directoryChooserB;
	@FXML private TextField fileNameTF;

	//Visualization
	@FXML private Text rowPatternVizTF;
	@FXML private Text colPatternVizTF;
	@FXML private Text ctxPatternVizTF;
	@FXML private Tab visualizationTab;
	private Map<Integer, HeatMapData[]> triclusterVizData;
	@FXML private ComboBox<String> tricIDCB;
	
	@FXML private TextFlow tricSummaryTF;
	@FXML private TableView<HeatMapTableView> heatMapTV;
	@FXML private TableColumn<HeatMapTableView, Integer> contextIDTC;
	@FXML private TableColumn<HeatMapTableView, Button> showButtonTC;
	ObservableList<HeatMapTableView> contextList;
	
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

		this.numContextsTF.textProperty().bindBidirectional(model.getNumContextsProperty(), new NumberStringConverter());
		this.numContextsTF.textProperty().setValue("100");

		this.numContextsTF.focusedProperty().addListener((obs, oldText, newText) -> {
			if(newText == false && oldText == true && model.getNumContexts() == 1 ) {
				enableBiclusteringGeneration();
			}
			else
				disableBiclusteringGeneration();
			// ...
		});

		
		this.dataTypeCB.setItems(model.getDataTypes());
		this.dataTypeCB.setValue(model.getDataTypeEscolhido());

		this.symbolTypeCB.setItems(model.getSymbolTypes());
		this.symbolTypeCB.setValue(model.getSymbolTypeEscolhido());

		this.symbolicTypeRB.setSelected(true);
		this.symbolTypeParamTF.textProperty().bindBidirectional(model.getNumberOfSymbolsProperty(), new NumberStringConverter());
		this.symbolTypeParamTF.textProperty().setValue("10");
		
		this.symbolTypeParamTF.focusedProperty().addListener((obs, oldText, newText) -> {
			if(newText == false && oldText == true && model.getBackgroundTypeEscolhido().equals("Discrete")) {
				initDiscreteTable();
			}

			// ...
		});

		this.minValueTF.textProperty().bindBidirectional(model.getMinValueProperty(), new NumberStringConverter());
		this.minValueTF.focusedProperty().addListener((obs, oldText, newText) -> {
			if(newText == false && oldText == true && model.getBackgroundTypeEscolhido().equals("Discrete")) {
				initDiscreteTable();
			}
		});

		this.maxValueTF.textProperty().bindBidirectional(model.getMaxValueProperty(), new NumberStringConverter());
		this.maxValueTF.focusedProperty().addListener((obs, oldText, newText) -> {
			if(newText == false && oldText == true && model.getBackgroundTypeEscolhido().equals("Discrete")) {
				initDiscreteTable();
			}
		});

		setNumericDatasetParametersVisible(false);

		this.backgroundTypeCB.setItems(model.getBackgroundTypes());
		this.backgroundTypeCB.setValue(model.getBackgroundTypeEscolhido());

		this.distMeanTF.textProperty().bindBidirectional(model.getDistMeanProperty(), new NumberStringConverter());
		this.distStdTF.textProperty().bindBidirectional(model.getDistStdProperty(), new NumberStringConverter());

		this.backgroundParamsPane.setVisible(false);
		this.discreteProbsPane.setVisible(false);

		//Triclusters properties
		this.numTricsTF.textProperty().bindBidirectional(model.getNumTricsProperty(), new NumberStringConverter());
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

		this.contextStructureDistCB.setItems(model.getContextDistributions());
		this.contextStructureDistCB.setValue(model.getContextDistributionEscolhida());
		this.contextDistParam1TF.textProperty().bindBidirectional(model.getContextDistParam1Property(), new NumberStringConverter());
		this.contextDistParam2TF.textProperty().bindBidirectional(model.getContextDistParam2Property(), new NumberStringConverter());
		setDistributionLabels(this.contextDistParam1Label, this.contextDistParam2Label, model.getContextDistributionEscolhida());	

		this.contiguityCB.setItems(model.getContiguity());
		this.contiguityCB.setValue(model.getContiguityEscolhida());

		//Triclusters Patterns
		this.patternsTV.setItems(model.getSymbolicPatterns());
		this.numTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, Integer>("num"));
		this.rowTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, String>("rowPattern"));
		this.columnTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, String>("columnPattern"));
		this.contextTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, String>("contextPattern"));
		this.timeProfileTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, ComboBox<String>>("timeProfile"));
		this.exampleTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, Label>("example"));
		this.selectTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, CheckBox>("select"));

		//Overlapping
		this.plaidCoherencyCB.setItems(model.getPlaidCoherency());
		this.plaidCoherencyCB.setValue(model.getPlaidCoherencyEscolhida());
		this.percOverlappingTricsTF.textProperty().bindBidirectional(model.getPercOverlappingTricsProperty(), new NumberStringConverter());
		this.maxOverlappingTricsTF.textProperty().bindBidirectional(model.getMaxOverlappingTricsProperty(), new NumberStringConverter());
		this.percOverlappingElementsTF.textProperty().bindBidirectional(model.getPercOverlappingElementsProperty(), new NumberStringConverter());
		this.percOverlappingRowsTF.textProperty().bindBidirectional(model.getPercOverlappingRowsProperty(), new NumberStringConverter());
		this.percOverlappingColumnsTF.textProperty().bindBidirectional(model.getPercOverlappingColumnsProperty(), new NumberStringConverter());
		this.percOverlappingContextsTF.textProperty().bindBidirectional(model.getPercOverlappingContextsProperty(), new NumberStringConverter());
		plaidCoherencySelecionada(new ActionEvent());


		//Extras
		this.percMissingBackgroundTF.textProperty().bindBidirectional(model.getPercMissingsBackgroundProperty(), new NumberStringConverter());
		this.percMissingTricsTF.textProperty().bindBidirectional(model.getPercMissingsTricsProperty(), new NumberStringConverter());
		this.percNoiseBackgroundTF.textProperty().bindBidirectional(model.getPercNoiseBackgroundProperty(), new NumberStringConverter());
		this.percNoiseTricsTF.textProperty().bindBidirectional(model.getPercNoiseTricsProperty(), new NumberStringConverter());
		this.noiseDeviationTF.textProperty().bindBidirectional(model.getNoiseDeviationProperty(), new NumberStringConverter());
		this.percErrorsBackgroundTF.textProperty().bindBidirectional(model.getPercErrorsBackgroundProperty(), new NumberStringConverter());
		this.percErrorsTricsTF.textProperty().bindBidirectional(model.getPercErrorsTricsProperty(), new NumberStringConverter());


		this.statusBar.progressProperty().bindBidirectional(model.getProgressProperty());
		this.statusT.setVisible(false);
		this.statusLB.setVisible(false);

		this.directoryChooserTF.textProperty().bindBidirectional(model.getDirectoryChooserProperty());
		model.setDirectory(System.getProperty("user.dir"));

		this.fileNameTF.textProperty().bindBidirectional(model.getFileNameProperty());
		model.setFileName("example_dataset");

		this.triclusterVizData = new HashMap<>();
	}

	private void enableBiclusteringGeneration() {
		
		this.contextStructureDistCB.setValue("Uniform");
		this.contextStructureDistCB.setDisable(true);
		this.contextDistParam1TF.setText("1");
		this.contextDistParam1TF.setDisable(true);
		this.contextDistParam2TF.setText("1");
		this.contextDistParam2TF.setDisable(true);
		this.percOverlappingContextsTF.setText("100");
		this.percOverlappingContextsTF.setDisable(true);
		
		this.model.getContiguity().remove((String) "Contexts");
		this.contiguityCB.setValue(this.model.getContiguity().get(0));
	}
	
	private void disableBiclusteringGeneration() {
		
		this.contextStructureDistCB.setDisable(false);
		this.contextDistParam1TF.setText("3");
		this.contextDistParam1TF.setDisable(false);
		this.contextDistParam2TF.setText("5");
		this.contextDistParam2TF.setDisable(false);
		
		boolean ov = (this.plaidCoherencyCB.getValue() == "No Overlapping");
		this.percOverlappingContextsLB.setDisable(ov);
		this.percOverlappingContextsTF.setDisable(ov);
		
				
		
		this.model.getContiguity().clear();
		gBicService.getContiguity().forEach(c->this.model.getContiguity().add(c));
		this.contiguityCB.setValue(this.model.getContiguity().get(0));
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
		this.numContextsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.numTricsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percOverlappingTricsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.maxOverlappingTricsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percOverlappingElementsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percOverlappingRowsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percOverlappingColumnsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percOverlappingContextsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percMissingBackgroundTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percMissingTricsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percNoiseBackgroundTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percNoiseTricsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percErrorsBackgroundTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				0, integerFilter));

		this.percErrorsTricsTF.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
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

		this.contextDistParam1TF.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(),
				0.0, doubleFilter));

		this.contextDistParam2TF.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(),
				0.0, doubleFilter));

	}

	@FXML
	void dataTypeSelecionado(ActionEvent event) {

		if(model.getBackgroundTypeEscolhido().equals("Discrete")) {
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
	void backgroundTypeSelecionado(ActionEvent event) {
		String option = this.backgroundTypeCB.getValue();
		model.setBackgroundTypeEscolhido(option);

		if(option.equals("Normal")) {
			this.backgroundParamsPane.setVisible(true);
			this.discreteProbsPane.setVisible(false);
		}
		else if(option.equals("Discrete")) {

			boolean error = false;
			String errorMsg = "";

			if(this.numericTypeRB.isSelected() && model.getDataTypeEscolhido().equals("Real Valued")) {
				error = true;
				errorMsg = "Discrete background is not available on Real Valued datasets";
			}
			else if(this.numericTypeRB.isSelected() && (this.maxValueTF.getText().isEmpty() || this.minValueTF.getText().isEmpty())) {
				error = true;
				errorMsg = "The dataset's min and mac values must be defined before setting a discrete background";

			}
			else if(this.symbolicTypeRB.isSelected() && this.symbolTypeParamTF.getText().isEmpty()) {
				error = true;
				errorMsg = "The dataset's symbols must be defined before setting a discrete background";
			}
			if(error) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error Dialog");
				alert.setHeaderText("Input data error");
				alert.setContentText(errorMsg);
				alert.showAndWait();

				this.backgroundTypeCB.setValue("Uniform");
				model.setBackgroundTypeEscolhido(this.backgroundTypeCB.getValue());
				this.backgroundParamsPane.setVisible(false);
				this.discreteProbsPane.setVisible(false);
			}
			else {
				this.backgroundParamsPane.setVisible(false);
				this.discreteProbsPane.setVisible(true);

				initDiscreteTable();
			}

		}
		else {			
			this.backgroundParamsPane.setVisible(false);
			this.discreteProbsPane.setVisible(false);
		}

	}

	private void initDiscreteTable() {

		ObservableList<DiscreteProbabilitiesTableView> symbols = FXCollections.observableArrayList();

		if(this.numericTypeRB.isSelected()) {
			for(int i = (int) model.getMinValue(); i <= (int) model.getMaxValue(); i++)
				symbols.add(new DiscreteProbabilitiesTableView(String.valueOf(i), String.valueOf(0.0)));
			System.out.println("hi from numeric");
		}
		else if(this.symbolicTypeRB.isSelected() && model.getSymbolTypeEscolhido().equals("Default")) {
			for(int i = 1; i <= model.getNumberOfSymbols(); i++)
				symbols.add(new DiscreteProbabilitiesTableView(String.valueOf(i), String.valueOf(0.0)));
			System.out.println("hi from symbolic default " + symbols.size());
		}
		else {
			for(String s : model.getListOfSymbols())
				symbols.add(new DiscreteProbabilitiesTableView(s, String.valueOf(0.0)));
			System.out.println("hi from symbolic custom");
		}

		this.discreteProbsTV.setEditable(true);

		this.discreteProbsTV.setItems(symbols);

		this.symbolTC.setCellValueFactory(new PropertyValueFactory<>("Symbol"));
		this.probTC.setCellValueFactory(new PropertyValueFactory<>("Prob"));
		this.probTC.setCellFactory(TextFieldTableCell.forTableColumn());

		this.probTC.setOnEditCommit(e -> {
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
	void contextStructureDistSelecionada(ActionEvent event) {
		model.setContextDistributionEscolhida(this.contextStructureDistCB.getValue());
		setDistributionLabels(this.contextDistParam1Label, this.contextDistParam2Label, this.contextStructureDistCB.getValue());
	}

	@FXML
	void contiguitySelecionada(ActionEvent event) {
		model.setContiguityEscolhida(this.contiguityCB.getValue());
	}

	@FXML
	void plaidCoherencySelecionada(ActionEvent event) {
		model.setPlaidCoherencyEscolhida(this.plaidCoherencyCB.getValue());
		if(this.plaidCoherencyCB.getValue().equals("No Overlapping")) {
			this.percOverlappingTricsLB.setDisable(true);
			this.percOverlappingTricsTF.setDisable(true);
			this.maxOverlappingTricsLB.setDisable(true);
			this.maxOverlappingTricsTF.setDisable(true);
			this.percOverlappingElementsLB.setDisable(true);
			this.percOverlappingElementsTF.setDisable(true);
			this.percOverlappingRowsLB.setDisable(true);
			this.percOverlappingRowsTF.setDisable(true);
			this.percOverlappingColumnsLB.setDisable(true);
			this.percOverlappingColumnsTF.setDisable(true);
			this.percOverlappingContextsLB.setDisable(true);
			this.percOverlappingContextsTF.setDisable(true);
		}
		else {
			this.percOverlappingTricsLB.setDisable(false);
			this.percOverlappingTricsTF.setDisable(false);
			this.maxOverlappingTricsLB.setDisable(false);
			this.maxOverlappingTricsTF.setDisable(false);
			this.percOverlappingElementsLB.setDisable(false);
			this.percOverlappingElementsTF.setDisable(false);
			this.percOverlappingRowsLB.setDisable(false);
			this.percOverlappingRowsTF.setDisable(false);
			this.percOverlappingColumnsLB.setDisable(false);
			this.percOverlappingColumnsTF.setDisable(false);
			if(model.getNumContexts() != 1 ) {
				this.percOverlappingContextsLB.setDisable(false);
				this.percOverlappingContextsTF.setDisable(false);
			}
			
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
	void symbolicTypeSelected(ActionEvent event) {

		this.setSymbolicDatasetParametersVisible(true);
		this.setNumericDatasetParametersVisible(false);

		model.setSymbolicTypeBoolean(true);

		this.patternsTV.setItems(model.getSymbolicPatterns());
		this.numTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, Integer>("num"));
		this.rowTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, String>("rowPattern"));
		this.columnTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, String>("columnPattern"));
		this.contextTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, String>("contextPattern"));
		this.timeProfileTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, ComboBox<String>>("timeProfile"));
		this.exampleTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, Label>("example"));
		this.selectTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, CheckBox>("select"));
	}

	@FXML
	void numericTypeSelected(ActionEvent event) {

		this.setSymbolicDatasetParametersVisible(false);
		this.setNumericDatasetParametersVisible(true);

		model.setSymbolicTypeBoolean(false);

		this.patternsTV.setItems(model.getNumericPatterns());
		this.numTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, Integer>("num"));
		this.rowTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, String>("rowPattern"));
		this.columnTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, String>("columnPattern"));
		this.contextTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, String>("contextPattern"));
		this.timeProfileTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, ComboBox<String>>("timeProfile"));
		this.exampleTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, Label>("example"));
		this.selectTC.setCellValueFactory(new PropertyValueFactory<TriclusterPatternTableView, CheckBox>("select"));

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

		if(model.getBackgroundTypeEscolhido().equals("Discrete")) {
			model.setDiscreteProbabilities(this.discreteProbsTV.getItems());

			for(DiscreteProbabilitiesTableView p : model.getDiscreteProbabilities())
				System.out.println(p.getSymbol() + " -> " + p.getProb());

		}


		errors = validateInput();

		GenerateDatasetTask<Void> task = new GenerateDatasetTask<Void>(this.gBicService) {

			@Override protected Void call() throws Exception {

				gBicService.setProgressUpdate((workDone, totalWork) -> updateProgress(workDone, totalWork));
				gBicService.setMessageUpdate((message) -> updateMessage(message));

				//call triGen setters
				double backgroundParam1 = 0;
				double backgroundParam2 = 0;
				double[] backgroundParam3 = null;

				//Set background type and validate input
				if(model.getBackgroundTypeEscolhido().equals("Normal")) {
					backgroundParam1 = model.getDistMean();
					backgroundParam2 = model.getDistStd();
				}
				else if(model.getBackgroundTypeEscolhido().equals("Discrete")) {
					int index = 0;
					backgroundParam3 = new double[model.getDiscreteProbabilities().size()];
					for(DiscreteProbabilitiesTableView p : model.getDiscreteProbabilities()) {
						backgroundParam3[index] = Double.parseDouble(p.getProb());
						index++;
					}
				}

				if(!model.isSymbolic()) {

					this.gBicService.setDatasetType("Numeric");
					boolean realValued = model.getDataTypeEscolhido().equals("Real Valued");

					this.gBicService.setDatasetProperties(model.getNumRows(), model.getNumColumns(), model.getNumContexts(), realValued, 
							model.getMinValue(), model.getMaxValue(), model.getBackgroundTypeEscolhido(), backgroundParam1,
							backgroundParam2, backgroundParam3);
				}
				else {
					this.gBicService.setDatasetType("Symbolic");
					if(model.getSymbolTypeEscolhido().equals("Default")) {
						this.gBicService.setDatasetProperties(model.getNumRows(), model.getNumColumns(), model.getNumContexts(), true, model.getNumberOfSymbols(), 
								null, model.getBackgroundTypeEscolhido(), backgroundParam1, backgroundParam2, backgroundParam3);
					}
					else {
						String[] symbols = model.getListOfSymbols().toArray(new String[0]);
						this.gBicService.setDatasetProperties(model.getNumRows(), model.getNumColumns(), model.getNumContexts(), false, -1, 
								symbols, model.getBackgroundTypeEscolhido(), backgroundParam1, backgroundParam2, backgroundParam3);
					}
				}

				this.gBicService.setTriclustersProperties(model.getNumTrics(), model.getRowDistributionEscolhida(), model.getRowDistParam1(), 
						model.getRowDistParam2(), model.getColumnDistributionEscolhida(), model.getColumnDistParam1(), model.getColumnDistParam2(), 
						model.getContextDistributionEscolhida(), model.getContextDistParam1(), model.getContextDistParam2(), model.getContiguityEscolhida());

				//set patterns
				List<TriclusterPatternWrapper> listPatterns = new ArrayList<>();
				List<TriclusterPatternTableView> availablePatterns = null;

				if(model.isSymbolic())
					availablePatterns = model.getSymbolicPatterns();
				else
					availablePatterns = model.getNumericPatterns();

				for(TriclusterPatternTableView p : availablePatterns) {
					if(p.getSelect().isSelected()) {
						TriclusterPatternWrapper tric;
						if(p.getContextPattern().equals("Order Preserving"))
							tric = this.gBicService.new TriclusterPatternWrapper(p.getRowPattern(), p.getColumnPattern(), p.getContextPattern(), p.getTimeProfile().getValue(), null);
						else
							tric = this.gBicService.new TriclusterPatternWrapper(p.getRowPattern(), p.getColumnPattern(), p.getContextPattern(), null);
						System.out.println(tric.toString());
						listPatterns.add(tric);
					}
				}
				this.gBicService.setTriclusterPatterns(listPatterns);

				this.gBicService.setOverlappingSettings(model.getPlaidCoherencyEscolhida(), model.getPercOverlappingTrics() / 100, model.getMaxOverlappingTrics(),
						model.getPercOverlappingElements() / 100, model.getPercOverlappingRows() / 100, model.getPercOverlappingColumns() / 100, model.getPercOverlappingContexts() / 100);
				
				this.gBicService.setQualitySettings(model.getPercMissingsBackground() / 100, model.getPercMissingsTrics() / 100, 
						model.getPercNoiseBackground() / 100, model.getPercNoiseTrics() / 100, model.getNoiseDeviation(), 
						model.getPercErrorsBackground() / 100, model.getPercErrorsTrics() / 100);

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
					if(model.isSymbolic())
						this.gBicService.generateSymbolicDataset();
					else
						this.gBicService.generateNumericDataset();
					
					System.out.println(gBicService.getGeneratedDataset().getNumTrics());
					System.out.println(model.getNumTrics());
					
					boolean noSpace = gBicService.getGeneratedDataset().getNumTrics() < model.getNumTrics();
					
					if(noSpace) {
						model.setNumTrics(gBicService.getGeneratedDataset().getNumTrics());
					}
					
					tricIDCB.getItems().clear();
					visualizationTab.setDisable(false);
					model.setTriclusterIDList();
					tricIDCB.setItems(model.getTriclusterIDs());
					tricIDCB.setValue(model.getTriclusterIDs().get(0));
					
					updateProgress(100, 100);
					updateMessage("Completed!");
					
					Task<Void> successBox = new Task<Void>() {
						@Override
						protected Void call() throws Exception {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									
									int generatedTrics = gBicService.getGeneratedDataset().getNumTrics();
									
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
												generatedTrics + " triclusters!\nTry planting less, or smaller triclusters, or incresing"
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

		List<TriclusterPatternTableView> patterns = null;
		boolean errorsOnDatasetProperties = false;

		if(model.isSymbolic()) {

			if(model.getSymbolTypeEscolhido().equals("Default")) {
				messages.append(InputValidation.validateDatasetSettings(this.numRowsTF.getText(), this.numColumnsTF.getText(), 
						this.numContextsTF.getText(), this.symbolTypeParamTF.getText(), new ArrayList<String>()));
			}
			else {
				messages.append(InputValidation.validateDatasetSettings(this.numRowsTF.getText(), this.numColumnsTF.getText(), 
						this.numContextsTF.getText(), null, (List<String>) model.getListOfSymbols()));
			}	

			patterns = model.getSymbolicPatterns();
		}
		else {

			messages.append(InputValidation.validateDatasetSettings(this.numRowsTF.getText(), this.numColumnsTF.getText(),
					this.numContextsTF.getText(), this.minValueTF.getText(), this.maxValueTF.getText()));

			patterns = model.getNumericPatterns();
		}

		errorsOnDatasetProperties = messages.length() > 1;

		boolean selectedPattern = false;
		for(TriclusterPatternTableView p : patterns)
			if(p.getSelect().isSelected())
				selectedPattern = true;
		if(!selectedPattern)
			messages.append("(Tricluster Patterns) Error: At least one pattern should be selected!\n");

		if(model.getBackgroundTypeEscolhido().equals("Normal")) {
			messages.append(InputValidation.validateBackgroundSettings(this.distMeanTF.getText(), this.distStdTF.getText()));
		}
		else if(model.getBackgroundTypeEscolhido().equals("Discrete")){
			List<DiscreteProbabilitiesTableView> probs = model.getDiscreteProbabilities();
			messages.append(InputValidation.validateBackgroundSettings(probs));
		}

		// ** Validate Triclusters Structure ** 

		if(errorsOnDatasetProperties) {
			messages.append(InputValidation.validateTriclusterStructure(model.getNumRows(), model.getNumColumns(), model.getNumContexts(), 
					model.getRowDistributionEscolhida(), model.getRowDistParam1(), model.getRowDistParam2(), 
					model.getColumnDistributionEscolhida(), model.getColumnDistParam1(), model.getColumnDistParam2(), 
					model.getContextDistributionEscolhida(), model.getContextDistParam1(), model.getContextDistParam2()));
		}

		// ** Validate Overlapping **
		messages.append(InputValidation.validateOverlappingSettings(model.getPlaidCoherencyEscolhida(), model.getPercOverlappingTrics(), 
				model.getMaxOverlappingTrics(), model.getPercOverlappingElements(), model.getPercOverlappingRows(), 
				model.getPercOverlappingColumns(), model.getPercOverlappingContexts(), model.getNumTrics()));

		// ** Validate Quality **
		messages.append(InputValidation.validateMissingNoiseAndErrorsOnBackground(model.getPercMissingsBackground(), model.getPercNoiseBackground(),
				model.getPercErrorsBackground()));

		if(model.isSymbolic()) {
			if(model.getSymbolTypeEscolhido().equals("Default"))
				messages.append(InputValidation.validateMissingNoiseAndErrorsOnPlantedTrics(model.getPercMissingsTrics(), model.getPercNoiseTrics(),
						model.getPercErrorsTrics(), (int) model.getNoiseDeviation(), model.getNumberOfSymbols()));
			else
				messages.append(InputValidation.validateMissingNoiseAndErrorsOnPlantedTrics(model.getPercMissingsTrics(), model.getPercNoiseTrics(),
						model.getPercErrorsTrics(), (int) model.getNoiseDeviation(), model.getListOfSymbols().size()));
		}
			
		else
			messages.append(InputValidation.validateMissingNoiseAndErrorsOnPlantedTrics(model.getPercMissingsTrics(), model.getPercNoiseTrics(),
					model.getPercErrorsTrics(), model.getNoiseDeviation(), model.getMinValue(), model.getMaxValue()));

		return messages.toString();
	}

	@FXML
	void changeTriclusterVisualization(ActionEvent event) {
		
		if(this.tricIDCB.getItems().size() > 0) {
			int tricID = Integer.parseInt(this.tricIDCB.getValue().split(" ")[1].strip());
			//if(!this.triclusterVizData.containsKey(tricID))
				//this.triclusterVizData.put(tricID, generateTriclusterVizData(tricID));
	
			this.currentTriclusterVisualization = tricID;
			//this.currentSliceVisualization = 0;
			
			//Restart context buttons
			//this.nextCtxB.setDisable(false);
			//this.previousCtxB.setDisable(true);
			
			JSONObject tric = gBicService.getTriclustersJSON().getJSONObject(String.valueOf(currentTriclusterVisualization));
			
			Task<Void> nextContext = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							
							tricSummaryTF.getChildren().clear();
							
							Text title = new Text("Tricluster " + tricID + " information: \n\n");
							title.setFont(Font.font("System", FontPosture.REGULAR, 14));
							
							
							Text dimensions = new Text("Dimensions:\t\t" + tric.getInt("#rows") + "x" + tric.getInt("#columns") +  "x" + 
									tric.getInt("#contexts") + "\n\n");
							dimensions.setFont(Font.font("System", FontPosture.REGULAR, 14));
							
							Text rows = new Text("Rows:\t\t\t\t" + tric.getJSONArray("X").toString() + "\n\n");
							Text cols = new Text("Columns:\t\t\t" + tric.getJSONArray("Y").toString() + "\n\n");
							Text ctxs = new Text("Contexts:\t\t\t" + tric.getJSONArray("Z").toString() + "\n\n");
							
							rows.setFont(Font.font("System", FontPosture.REGULAR, 14));
							cols.setFont(Font.font("System", FontPosture.REGULAR, 14));
							ctxs.setFont(Font.font("System", FontPosture.REGULAR, 14));
							
							Text rowPattern = new Text("Row Pattern:\t\t" + tric.getString("RowPattern").toString() + "\n\n");
							Text colPattern = new Text("Column Pattern:\t" + tric.getString("ColumnPattern").toString() + "\n\n");
							Text ctxPattern = new Text("Context Pattern:\t" + tric.getString("ContextPattern").toString() + "\n\n");
							
							rowPattern.setFont(Font.font("System", FontPosture.REGULAR, 14));
							colPattern.setFont(Font.font("System", FontPosture.REGULAR, 14));
							ctxPattern.setFont(Font.font("System", FontPosture.REGULAR, 14));
							
							tricSummaryTF.getChildren().addAll(title, dimensions, rows, cols, ctxs, rowPattern, colPattern, ctxPattern);
							
							if(tric.getString("ContextPattern").toString().equals("OrderPreserving")) {
								Text timeProfile = new Text("Time Profile:\t\t" + tric.getString("TimeProfile").toString() + "\n\n");
								timeProfile.setFont(Font.font("System", FontPosture.REGULAR, 14));
								tricSummaryTF.getChildren().add(timeProfile);
							}
							
							
							if(tric.has("Seed")) {
								Text seed = new Text("Seed:\t\t\t\t" + tric.getString("Seed").toString() + "\n\n");
								Text rowFactors = new Text("Row Factors:\t\t" + tric.getString("RowFactors").toString() + "\n\n");
								Text colFactors = new Text("Column Factors:\t" + tric.getString("ColumnFactors").toString() + "\n\n");
								Text ctxFactors = new Text("Context Factors:\t" + tric.getString("ContextFactors").toString() + "\n\n");
								
								seed.setFont(Font.font("System", FontPosture.REGULAR, 14));
								rowFactors.setFont(Font.font("System", FontPosture.REGULAR, 14));
								colFactors.setFont(Font.font("System", FontPosture.REGULAR, 14));
								ctxFactors.setFont(Font.font("System", FontPosture.REGULAR, 14));
									
								tricSummaryTF.getChildren().addAll(seed, rowFactors, colFactors, ctxFactors);
							}
							
							Text plaid = new Text("Plaid Coherency:\t" + tric.getString("PlaidCoherency").toString() + "\n\n");
							Text missings = new Text("Missings:\t\t\t" + tric.getString("%Missings").toString() + "%" + "\n\n");
							Text noise = new Text("Noise:\t\t\t\t" + tric.getString("%Noise").toString() + "%" + "\n\n");
							Text errors = new Text("Errors:\t\t\t\t" + tric.getString("%Errors").toString() + "%" + "\n\n");
							
							plaid.setFont(Font.font("System", FontPosture.REGULAR, 14));
							missings.setFont(Font.font("System", FontPosture.REGULAR, 14));
							noise.setFont(Font.font("System", FontPosture.REGULAR, 14));
							errors.setFont(Font.font("System", FontPosture.REGULAR, 14));
							
							tricSummaryTF.getChildren().addAll(plaid, missings, noise, errors);
						}
					});
					return null;
				}
			};

			Thread nc = new Thread(nextContext);
			nc.start();
			
			
			//showTriclusterVisualization(tricID, this.currentSliceVisualization);
			
			//Triclusters Patterns
			this.heatMapTV.setItems(generateTriclusterVizData(tricID));
			this.contextIDTC.setCellValueFactory(new PropertyValueFactory<HeatMapTableView, Integer>("contextID"));
			this.showButtonTC.setCellValueFactory(new PropertyValueFactory<>("show"));
			
		}
	}
	
	private ObservableList<HeatMapTableView> generateTriclusterVizData(int id) {

		System.out.println("Generating chartData");
		//get generated dataset
		Dataset dataset = gBicService.getGeneratedDataset();
		//get context's slices for the desired tricluster
		JSONObject data = gBicService.getTriclustersJSON().getJSONObject(String.valueOf(id)).getJSONObject("Data");
		//get the tricluster to obtain rows/columns/contexts
		Tricluster t = dataset.getTriclusterById(id);
		ObservableList<HeatMapTableView> contextList = FXCollections.observableArrayList();
		
		List<String> yTicks = new ArrayList<>();
		List<String> xTicks = new ArrayList<>();

		for(Integer r : t.getRows())
			yTicks.add("x" + r);

		for(Integer c : t.getColumns())
			xTicks.add("y" + c);
		Collections.reverse(yTicks);

		int chartNumber = 1;

		for(Integer ctx : t.getContexts()) {
			
			JSONArray slice = data.getJSONArray(String.valueOf(ctx));
			String title = "Context " + ctx + " (" + (chartNumber++) + " of " + t.getContexts().size() + ")";
			HeatMapData sliceHeatMap = null;
			
			if(model.isSymbolic()) {
				String[][] chartData = new String[t.getRows().size()][t.getColumns().size()];
				
				for(int s = 0; s < slice.length(); s++) 
					for(int c = 0; c < slice.getJSONArray(s).length(); c++) 	
						chartData[s][c] = slice.getJSONArray(s).get(c).toString();

				String[] alphabet = ((SymbolicDataset) this.gBicService.getGeneratedDataset()).getAlphabet();
				HeatMapTableView contextChart = new SymbolicHeatMapTableView(xTicks, yTicks, alphabet, title, chartData, ctx.intValue());
				contextList.add(contextChart);
			}
			else {
				Number[][] chartData = new Number[t.getRows().size()][t.getColumns().size()];
				
				for(int s = 0; s < slice.length(); s++) {
					for(int c = 0; c < slice.getJSONArray(s).length(); c++) {
						if(slice.getJSONArray(s).get(c) == null)
							chartData[s][c] = null;
						else {
							String value = slice.getJSONArray(s).get(c).toString();
							value = value.replaceAll(",","");
							if(value.equals(""))
								chartData[s][c] = null;
							else
								chartData[s][c] = (Number) new Double(value);
						}
							
					}
				}
				double min = ((NumericDataset) this.gBicService.getGeneratedDataset()).getMinM().doubleValue();
				double max = ((NumericDataset) this.gBicService.getGeneratedDataset()).getMaxM().doubleValue();
				HeatMapTableView contextChart = new NumericHeatMapTableView(xTicks, yTicks, min, max, title, chartData, ctx.intValue());
				contextList.add(contextChart);
			}
		}
		return contextList;
	}
}
