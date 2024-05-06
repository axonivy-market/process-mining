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

import com.axonivy.portal.components.service.impl.ProcessService;
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
	private static final String DIARGAM_URL_PATTERN = "%s/process-editor/?pmv=%s%s&mode=viewer&pid=%s&theme=light";
	private String selectedProcessDiagramUrl;

	@PostConstruct
	private void init() {
		processesMap = ProcessUtils.getInstance().getProcessesWithPmv();
	}

	public void confirm() throws IOException {
		List<IWebStartable> processes = ProcessService.getInstance().findProcesses().getProcesses();
		for (IWebStartable process : processes) {
			ProcessViewer.of((IProcessWebStartable) process).url().toWebLink();
			if (process.pmv().getProjectName().equalsIgnoreCase("adobe-esign-connector")) {

				Ivy.log().warn(ProcessViewer.of((IProcessWebStartable) process).url().toWebLink().getRelative());
//			Ivy.log().warn("id " + process.getId());
//				Ivy.log().warn("pmv " + process.pmv().getId());
//				for (String name : process.customFields().names()) {
//					Ivy.log().warn(name);
//				}
//				Ivy.log().warn("pid " + process.);

//			Ivy.log().warn("getDisplayName " + process.getDisplayName());
//			Ivy.log().warn("getName " + process.getName());

				Ivy.log().warn("getRelative " + process);
				Ivy.log().warn("getAbsolute " + process.getLink().getAbsolute());
			}

		}
		String appHomeRef = Ivy.html().applicationHomeRef();
//		String processId = fileMap.get(selectedProcessName);
		selectedProcessDiagramUrl = DIARGAM_URL_PATTERN.formatted(appHomeRef, selectedModuleName, "%241", "");
		Ivy.log().warn(selectedProcessDiagramUrl);
	}

	public Set<String> getPmvNames() {
		return processesMap.keySet();
	}

	public List<String> getProcessesName() {
		if (StringUtils.isBlank(selectedModuleName)) {
			return new ArrayList<>();
		}
		return processesMap.get(selectedModuleName).stream().map(IWebStartable::getDisplayName)
				.collect(Collectors.toList());
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
