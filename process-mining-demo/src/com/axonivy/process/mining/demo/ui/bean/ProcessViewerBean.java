package com.axonivy.process.mining.demo.ui.bean;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class ProcessViewerBean {
	private String selectedProcessName;
	private String selectedModuleName;

	public List<String> getModuleName() {
		return List.of("onboarding", "workforce-admin", "ticketting", "knowledge-base", "extra");
	}

	public List<String> getProcessesName() {
		return List.of("Initiate Pre Hire onboarding", "Dev Mock Data from queue", "Position maintainance",
				"Image-Change-Award", "Update Worker Job Data");
	}

	public String getSelectedModuleName() {
		return selectedModuleName;
	}

	public void setSelectedModuleName(String selectedModuleName) {
		this.selectedModuleName = selectedModuleName;
	}

	public String getSelectedProcessName() {
		return selectedProcessName;
	}

	public void setSelectedProcessName(String selectedProcessName) {
		this.selectedProcessName = selectedProcessName;
	}
}
