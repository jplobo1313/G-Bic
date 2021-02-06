package com.gbic.utils;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.HeatMapChart;
import org.knowm.xchart.HeatMapChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XChartPanel;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class NumericHeatMapTableView extends HeatMapTableView {

	private List<Number[]> heatData;
	private double min;
	private double max;
	private String title;
	
	public NumericHeatMapTableView(List<String> xTicks, List<String> yTicks, double min, double max, String title, Number[][] data, int contextID) {
		
		super(contextID, xTicks, yTicks);
		
		this.min = min;
		this.max = max;
		this.title = title;
		
		heatData = new ArrayList<>();
		for (int row = 0; row < data.length; row++) {
			for (int col = 0; col < data[row].length; col++) {
				Number[] numbers = {
						col,
						row,
						data[(data.length - 1) - row][col]
				};
				heatData.add(numbers);
			}
		}

		super.show.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("hi from numeric heat map tv");
                
                HeatMapChart chart = new HeatMapChartBuilder().width(1270).height(600).build();
                chart.getStyler().setPlotContentSize(0.999);
                chart.setXAxisTitle("Attribute y");
                chart.setYAxisTitle("Observation x");
                chart.getStyler().setXAxisTitleVisible(true);
                chart.getStyler().setYAxisTitleVisible(true);
                chart.getStyler().setShowValue(true);
                chart.getStyler().setChartBackgroundColor(new Color(245, 245, 245));
                chart.setTitle(title);
                
                int valueFontSize = 16;
                int axisFontSize = 12;
                
                if(xTicks.size() > 150 || yTicks.size() > 150) {
                	chart.getStyler().setShowValue(false);
                	axisFontSize = 0;
                }
                else if(xTicks.size() > 100 || yTicks.size() > 100) {
                	valueFontSize = 4;
                	axisFontSize = 4;
                }
                else if(xTicks.size() > 60 || yTicks.size() > 60) {
                	valueFontSize = 6;
                	axisFontSize = 6;
                }
                else if(xTicks.size() > 30 || yTicks.size() > 30) {
                	valueFontSize = 10;
                	axisFontSize = 10;
                }
                
                
                Font axisFont = new Font("SansSerif", Font.BOLD, axisFontSize);
                Font valueFont = new Font("SansSerif", Font.PLAIN, valueFontSize);
                
                chart.getStyler().setAxisTickLabelsFont(axisFont);
                chart.getStyler().setValueFont(valueFont);
                
                chart.addSeries("heatMap", xTicks, yTicks, heatData);
                ImageView imgView = new ImageView();
                try {
                	File directory = new File("temp");
				    if (!directory.exists()){
				        directory.mkdir();
				    }
                	
					BitmapEncoder.saveBitmapWithDPI(chart, "temp/heatmap", BitmapEncoder.BitmapFormat.PNG, 300);
					String filename = "heatmap.png";
					
	            	File f = new File("temp/" + filename);
	            	Image img = new Image(f.toURI().toString());
	            	imgView.setImage(img);
	            	imgView.setFitWidth(1270);
	            	imgView.setFitHeight(600);
				} catch (IOException e) {
					e.printStackTrace();
				}

                StackPane secondaryLayout = new StackPane();
                secondaryLayout.getChildren().add(imgView);
                
                System.out.println("Chart done");
                System.out.println("Chart displayed");
                
                Scene secondScene = new Scene(secondaryLayout, 1290, 650);

                Stage secondStage = new Stage();
                secondStage.setTitle("Context Visualization");
                secondStage.setScene(secondScene);
                
                
 
                secondStage.show();
            }
		});
	}


	/**
	 * @return the heatData
	 */
	public List<Number[]> getHeatData() {
		return heatData;
	}


	/**
	 * @param heatData the heatData to set
	 */
	public void setHeatData(List<Number[]> heatData) {
		this.heatData = heatData;
	}


	/**
	 * @return the min
	 */
	public double getMin() {
		return min;
	}


	/**
	 * @param min the min to set
	 */
	public void setMin(double min) {
		this.min = min;
	}


	/**
	 * @return the max
	 */
	public double getMax() {
		return max;
	}


	/**
	 * @param max the max to set
	 */
	public void setMax(double max) {
		this.max = max;
	}


	/**
	 * @return the show
	 */
	public Button getShow() {
		return show;
	}


	/**
	 * @param show the show to set
	 */
	public void setShow(Button show) {
		this.show = show;
	}
	
	
}
