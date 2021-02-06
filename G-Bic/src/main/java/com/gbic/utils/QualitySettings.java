/**
 * QualitySettings Class
 * 
 * @author Joao Lobo - jlobo@lasige.di.fc.ul.pt
 * @version 1.0
 */
package com.gbic.utils;

public class QualitySettings {

	private double percMissingsOnBackground;
	private double percMissingsOnTrics;
	private double percNoiseOnBackground;
	private double percNoiseOnTrics;
	private double noiseDeviation;
	private double percErrorsOnBackground;
	private double percErrorsOnTrics;
	
	public QualitySettings() {}
	
	/** Constructor
	 * @param percMissingsOnBackground The percentage of missings on dataset's background
	 * @param percMissingsOnTrics The maximum percentage of missings on planted triclusters
	 * @param percNoiseOnBackground  The percentage of noise on dataset's background
	 * @param percNoiseOnTrics The maximum percentage of noise on planted triclusters
	 * @param noiseDeviation The noise deviation value
	 * @param percErrorsOnBackground The percentage of errors on dataset's background
	 * @param percErrorsOnTrics The maximum percentage of errors on planted triclusters
	 */
	public QualitySettings(double percMissingsOnBackground, double percMissingsOnTrics, double percNoiseOnBackground,
			double percNoiseOnTrics, double noiseDeviation, double percErrorsOnBackground, double percErrorsOnTrics) {
		this.percMissingsOnBackground = percMissingsOnBackground;
		this.percMissingsOnTrics = percMissingsOnTrics;
		this.percNoiseOnBackground = percNoiseOnBackground;
		this.percNoiseOnTrics = percNoiseOnTrics;
		this.noiseDeviation = noiseDeviation;
		this.percErrorsOnBackground = percErrorsOnBackground;
		this.percErrorsOnTrics = percErrorsOnTrics;
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
	 * @return The maximum percentage of missings on planted triclusters
	 */
	public double getPercMissingsOnTrics() {
		return percMissingsOnTrics;
	}

	/**
	 * @param percMissingsOnTrics The maximum percentage of missings on planted triclusters to set
	 */
	public void setPercMissingsOnTrics(double percMissingsOnTrics) {
		this.percMissingsOnTrics = percMissingsOnTrics;
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
	 * @return The maximum percentage of noise on planted triclusters
	 */
	public double getPercNoiseOnTrics() {
		return percNoiseOnTrics;
	}

	/**
	 * @param percNoiseOnTrics The maximum percentage of noise on planted triclusters to set
	 */
	public void setPercNoiseOnTrics(double percNoiseOnTrics) {
		this.percNoiseOnTrics = percNoiseOnTrics;
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
	 * @return The maximum percentage of errors on planted triclusters
	 */
	public double getPercErrorsOnTrics() {
		return percErrorsOnTrics;
	}

	/**
	 * @param percErrorsOnTrics The maximum percentage of errors on planted triclusters to set
	 */
	public void setPercErrorsOnTrics(double percErrorsOnTrics) {
		this.percErrorsOnTrics = percErrorsOnTrics;
	}
	
	
	
}
