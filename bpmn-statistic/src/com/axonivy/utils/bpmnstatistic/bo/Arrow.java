package com.axonivy.utils.bpmnstatistic.bo;

public class Arrow {
	private String arrowId;
	private Float value;
	private String label;
	private int frequency;
	private double medianDuration;
	
	public Arrow(String id, Float value, String label) {
		this.arrowId = id;
		this.value= value;
		this.label = label;
	}

	public String getArrowId() {
		return arrowId;
	}

	public void setArrowId(String arrowId) {
		this.arrowId = arrowId;
	}

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public double getMedianDuration() {
		return medianDuration;
	}

	public void setMedianDuration(double medianDuration) {
		this.medianDuration = medianDuration;
	}

	@Override
	public String toString() {
		return "Arrow [arrowId=" + arrowId + ", value=" + value + ", label=" + label + ", frequency=" + frequency
				+ ", medianDuration=" + medianDuration + "]";
	}
}
