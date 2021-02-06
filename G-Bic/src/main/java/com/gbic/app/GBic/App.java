package com.gbic.app.GBic;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import com.gbic.app.GBic.controllers.MenuPrincipalController;
import com.gbic.app.GBic.models.MenuPrincipalModel;
import com.gbic.service.GBicService;

public class App extends Application {

	@Override
	public void start(Stage stage) throws IOException {

       
		int mb = 1024*1024;

		//Getting the runtime reference from system
		Runtime runtime = Runtime.getRuntime();

		System.out.println("##### Heap utilization statistics [MB] #####");

		//Print used memory
		System.out.println("Used Memory:"
			+ (runtime.totalMemory() - runtime.freeMemory()) / mb);

		//Print free memory
		System.out.println("Free Memory:"
			+ runtime.freeMemory() / mb);

		//Print total available memory
		System.out.println("Total Memory:" + runtime.totalMemory() / mb);

		//Print Maximum available memory
		System.out.println("Max Memory:" + runtime.maxMemory() / mb);
		
		
    	FXMLLoader menuPrincipalLoader = new FXMLLoader(App.class.getResource("G-Bic.fxml"));
    	
    	Parent root = menuPrincipalLoader.load();
    	
    	GBicService t = new GBicService();
    	MenuPrincipalController mpc = menuPrincipalLoader.getController();
    	MenuPrincipalModel mpm = new MenuPrincipalModel(t);
    	mpc.setModel(mpm);
    	mpc.setTriGenService(t);
    	mpc.setStage(stage);
    	
        Scene scene = new Scene(root, 1300, 800);
      
        stage.setTitle("G-Bic");
        stage.setScene(scene);
        stage.show();   
    }
	

	public static void main(String[] args) {
		launch(args);
	}

}