package com.gbic.utils;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.HeatMapChart;
import org.knowm.xchart.HeatMapChartBuilder;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SymbolicHeatMapTableView extends HeatMapTableView{

	private String[] alphabet;
	private String[][] heatData;
	
	public SymbolicHeatMapTableView(List<String> xTicks, List<String> yTicks, String[] alphabet, String title, String[][] heatData, int contextID) {
		super(contextID, xTicks, yTicks);
		
		this.alphabet = alphabet;
		this.heatData = heatData;
		
		super.show.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("hi from symbolic heat map tv");
                
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

                
                chart.addSeries("heatMap", xTicks, yTicks, alphabet, heatData);
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

}
