package com.gbic.utils;

import java.io.File;
import java.util.Arrays;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class TriclusterPatternTableView {

	private Integer num;
	private String rowPattern;
	private String columnPattern;
	private String contextPattern;
	private String imageName;
	private ComboBox<String> timeProfile;
	private Button example;
	private CheckBox select;
	
	public TriclusterPatternTableView(Integer num, String rowPattern, String columnPattern, String contextPattern, String imageName,
			ComboBox<String> timeProfile, Button example, CheckBox select) {
		this.num = num;
		this.rowPattern = rowPattern;
		this.columnPattern = columnPattern;
		this.contextPattern = contextPattern;
		this.timeProfile = timeProfile;
		this.select = select;
		this.example = example;
		this.imageName = imageName;
		
		
		final ObservableList<String> timeProfiles = FXCollections.observableArrayList();
		String[] array = {"Random", "Monotonically Increasing", "Monotonically Decreasing"};
		Arrays.asList(array).forEach(t->timeProfiles.add(t));
		
		
		this.timeProfile.setItems(timeProfiles);
		this.timeProfile.setValue(timeProfiles.get(0)); 
		
		if(!this.contextPattern.equals("Order Preserving")) {
			this.timeProfile.setDisable(true);
			this.timeProfile.setValue("Not applicable");
		}
		
		this.example.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
            	Image img = new Image(getClass().getResourceAsStream(getImageName() + ".png"));
            	ImageView imgView = new ImageView();
            	imgView.setImage(img);
            	imgView.setPreserveRatio(true);
                
                StackPane secondaryLayout = new StackPane();
                secondaryLayout.getChildren().add(imgView);
                
                
                Scene secondScene = new Scene(secondaryLayout, 1200, 420);

                Stage secondStage = new Stage();
                secondStage.setTitle("Pattern overview");
                secondStage.setScene(secondScene);
                
                secondStage.show();
            }
		});
	}

	public String getImageName() {
		return this.imageName;
	}
	
	public Button getExample() {
		return this.example;
	}
	
	public void setExample(Button example) {
		this.example = example;
	}
	
	public String getRowPattern() {
		return rowPattern;
	}

	public void setRowPattern(String rowPattern) {
		this.rowPattern = rowPattern;
	}

	public String getColumnPattern() {
		return columnPattern;
	}

	public void setColumnPattern(String columnPattern) {
		this.columnPattern = columnPattern;
	}

	public String getContextPattern() {
		return contextPattern;
	}

	public void setContextPattern(String contextPattern) {
		this.contextPattern = contextPattern;
	}

	public CheckBox getSelect() {
		return select;
	}

	public void setSelect(CheckBox select) {
		this.select = select;
	}
	
	public void setNum(int num) {
		this.num = num;
	}
	
	public Integer getNum() {
		return this.num;
	}

	/**
	 * @return the timeProfile
	 */
	public ComboBox<String> getTimeProfile() {
		return timeProfile;
	}

	/**
	 * @param timeProfile the timeProfile to set
	 */
	public void setTimeProfile(ComboBox<String> timeProfile) {
		this.timeProfile = timeProfile;
	}
	
}
