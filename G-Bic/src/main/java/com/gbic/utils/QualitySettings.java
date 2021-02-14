/**
 * QualitySettings Class
 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @version 1.0
 */
package com.gbic.utils;

public class QualitySettings {

	private double percMissingsOnBackground;
	private double percMissingsOnBics;
	private double percNoiseOnBackground;
	private double percNoiseOnBics;
	private double noiseDeviation;
	private double percErrorsOnBackground;
	private double percErrorsOnBics;
	
	public QualitySettings() {}
	
	/** Constructor
	 * @param percMissingsOnBackground The percentage of missings on dataset's background
	 * @param percMissingsOnBics The maximum percentage of missings on planted biclusters
	 * @param percNoiseOnBackground  The percentage of noise on dataset's background
	 * @param percNoiseOnBics The maximum percentage of noise on planted biclusters
	 * @param noiseDeviation The noise deviation value
	 * @param percErrorsOnBackground The percentage of errors on dataset's background
	 * @param percErrorsOnBics The maximum percentage of errors on planted biclusters
	 */
	public QualitySettings(double percMissingsOnBackground, double percMissingsOnBics, double percNoiseOnBackground,
			double percNoiseOnBics, double noiseDeviation, double percErrorsOnBackground, double percErrorsOnBics) {
		this.percMissingsOnBackground = percMissingsOnBackground;
		this.percMissingsOnBics = percMissingsOnBics;
		this.percNoiseOnBackground = percNoiseOnBackground;
		this.percNoiseOnBics = percNoiseOnBics;
		this.noiseDeviation = noiseDeviation;
		this.percErrorsOnBackground = percErrorsOnBackground;
		this.percErrorsOnBics = percErrorsOnBics;
	}

	/**
	 * @return The percentage of missings on dataset's background
	 */
	public double getPercMissingsOnBackground() {
		return percMissingsOnBackground;
	}

	/**
	 * @param percMissingsOnBackground The percentage of missings on dataset's background to set
	 */
	public void setPercMissingsOnBackground(double percMissingsOnBackground) {
		this.percMissingsOnBackground = percMissingsOnBackground;
	}

	/**
	 * @return The maximum percentage of missings on planted biclusters
	 */
	public double getPercMissingsOnBics() {
		return percMissingsOnBics;
	}

	/**
	 * @param percMissingsOnBics The maximum percentage of missings on planted biclusters to set
	 */
	public void setPercMissingsOnBics(double percMissingsOnBics) {
		this.percMissingsOnBics = percMissingsOnBics;
	}

	/**
	 * @return The percentage of noise on dataset's background
	 */
	public double getPercNoiseOnBackground() {
		return percNoiseOnBackground;
	}

	/**
	 * @param percNoiseOnBackground The percentage of noise on dataset's background to set
	 */
	public void setPercNoiseOnBackground(double percNoiseOnBackground) {
		this.percNoiseOnBackground = percNoiseOnBackground;
	}

	/**
	 * @return The maximum percentage of noise on planted biclusters
	 */
	public double getPercNoiseOnBics() {
		return percNoiseOnBics;
	}

	/**
	 * @param percNoiseOnBics The maximum percentage of noise on planted biclusters to set
	 */
	public void setPercNoiseOnBics(double percNoiseOnBics) {
		this.percNoiseOnBics = percNoiseOnBics;
	}

	/**
	 * @return the noise deviation value
	 */
	public double getNoiseDeviation() {
		return noiseDeviation;
	}

	/**
	 * @param noiseDeviation the noise deviation value to set
	 */
	public void setNoiseDeviation(double noiseDeviation) {
		this.noiseDeviation = noiseDeviation;
	}

	/**
	 * @return The percentage of errors on dataset's background
	 */
	public double getPercErrorsOnBackground() {
		return percErrorsOnBackground;
	}

	/**
	 * @param percErrorsOnBackground The percentage of errors on dataset's background to set
	 */
	public void setPercErrorsOnBackground(double percErrorsOnBackground) {
		this.percErrorsOnBackground = percErrorsOnBackground;
	}

	/**
	 * @return The maximum percentage of errors on planted biclusters
	 */
	public double getPercErrorsOnBics() {
		return percErrorsOnBics;
	}

	/**
	 * @param percErrorsOnBics The maximum percentage of errors on planted biclusters to set
	 */
	public void setPercErrorsOnBics(double percErrorsOnBics) {
		this.percErrorsOnBics = percErrorsOnBics;
	}
	
	
	
}
