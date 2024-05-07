package com.axonivy.process.mining.demo.ui.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.PF;

import com.axonivy.process.mining.demo.ui.util.ProcessUtils;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.process.viewer.api.ProcessViewer;
import ch.ivyteam.ivy.workflow.start.IProcessWebStartable;
import ch.ivyteam.ivy.workflow.start.IWebStartable;

@ManagedBean
@ViewScoped
public class ProcessViewBean {
	private String selectedProcessName;
	private String selectedModuleName;
	private Map<String, List<IWebStartable>> processesMap = new HashMap<>();
	private String selectedProcessDiagramUrl;
	private String selectedProcessPidId;

	@PostConstruct
	private void init() {
		processesMap = ProcessUtils.getInstance().getProcessesWithPmv();
	}

	public void confirm() throws IOException {
		getProcessDiagramUrl();
		PF.current().executeScript("PF('process-diagram-dialog-var').show()");
	}

	public void getProcessDiagramUrl() {
		if (StringUtils.isNotBlank(selectedProcessName) && StringUtils.isNotBlank(selectedModuleName)) {
			var x = processesMap.get(selectedModuleName).stream()
					.filter(process -> process.getDisplayName().equalsIgnoreCase(selectedProcessName)).findAny()
					.orElse(null);
			IProcessWebStartable currentProcess = (IProcessWebStartable) x;
			selectedProcessPidId = currentProcess.pid().getParent().toString();
			selectedProcessDiagramUrl = ProcessViewer.of(currentProcess).url().toWebLink().getAbsolute();
			Ivy.log().warn(selectedProcessDiagramUrl);
		}
	}

	public List<String> getProcessesName() {
		if (StringUtils.isBlank(selectedModuleName)) {
			return new ArrayList<>();
		}
		return processesMap.get(selectedModuleName).stream().map(IWebStartable::getDisplayName)
				.collect(Collectors.toList());
	}

	public Set<String> getPmvNames() {
		return processesMap.keySet();
	}

	public String getSelectedProcessPidId() {
		return selectedProcessPidId;
	}

	public void setSelectedProcessPidId(String selectedProcessPidId) {
		this.selectedProcessPidId = selectedProcessPidId;
	}

	public void setSelectedProcessDiagramUrl(String selectedProcessDiagramUrl) {
		this.selectedProcessDiagramUrl = selectedProcessDiagramUrl;
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

	public String getSelectedProcessDiagramUrl() {
		return selectedProcessDiagramUrl;
	}
}
