module GBic {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
	requires org.json;
	requires xchart;
	requires java.desktop;
	requires commons.math3;
	requires java.instrument;
	
	opens com.gbic.app.GBic to javafx.fxml;
	opens com.gbic.utils to javafx.base;
	opens com.gbic.app.GBic.controllers to javafx.fxml;
	exports com.gbic.app.GBic;
	exports com.gbic.app.GBic.controllers;
}