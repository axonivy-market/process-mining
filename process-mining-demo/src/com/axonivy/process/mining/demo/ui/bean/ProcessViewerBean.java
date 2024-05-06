package com.axonivy.process.mining.demo.ui.bean;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.shaded.json.JSONObject;
import org.primefaces.shaded.json.JSONTokener;

import ch.ivyteam.ivy.environment.Ivy;

@ManagedBean
@ViewScoped
public class ProcessViewerBean {
	private String selectedProcessName;
	private String selectedModuleName;
	private HashMap<String, String> fileMap = new HashMap<>();
	private static final String DIARGAM_URL_PATTERN = "%s/process-editor/?pmv=%s%s&mode=viewer&pid=%s&theme=light";
	private String selectedProcessDiagramUrl;

	@PostConstruct
	private void init() {
		String processPath = Ivy.var().get("process-mining-demo.modulePath");
		File folder = new File(processPath);
		for (File module : folder.listFiles()) {
			File processesFolder = getListOfItemInFolder(module).stream()
					.filter(file -> file.getName().equalsIgnoreCase("processes")).findAny().orElse(null);
			getProcessFilesFromProcessesFolder(processesFolder, module.getName());
		}
	}

	public void confirm() throws IOException {
		String appHomeRef = Ivy.html().applicationHomeRef();
		String processId = fileMap.get(selectedProcessName);
		selectedProcessDiagramUrl = DIARGAM_URL_PATTERN.formatted(appHomeRef, selectedModuleName, "%241", processId);
		Ivy.log().warn(selectedProcessDiagramUrl);
	}

	private void getProcessFilesFromProcessesFolder(File file, String pmvName) {
		if (isFolder(file)) {
			for (File subFile : getListOfItemInFolder(file)) {
				getProcessFilesFromProcessesFolder(subFile, pmvName);
			}
		} else if (file != null && file.getName().endsWith(".json")) {
			processJsonFile(file, pmvName);
		}
	}

	private boolean isFolder(File file) {
		return Objects.nonNull(file) && file.isDirectory() && file.listFiles() != null;
	}

	private List<File> getListOfItemInFolder(File file) {
		if (isFolder(file)) {
			return Arrays.asList(file.listFiles());
		}
		return new ArrayList<>();
	}

	private void processJsonFile(File jsonFile, String pmvName) {
		try (FileReader reader = new FileReader(jsonFile)) {
			JSONTokener tokener = new JSONTokener(reader);
			JSONObject jsonObject = new JSONObject(tokener);
			String id = jsonObject.getString("id");
			String fileName = jsonFile.getName();
			fileMap.put(pmvName + " " + fileName, id);
		} catch (Exception e) {
			Ivy.log().error(e.getMessage());
		}
	}

	public String[] getModuleName() {
		String processPath = Ivy.var().get("process-mining-demo.modulePath");
		File folder = new File(processPath);
		return Optional.ofNullable(folder).map(File::list).orElse(new String[0]);
	}

	public Set<String> getProcessesName() {
		if (StringUtils.isBlank(selectedModuleName)) {
			return fileMap.keySet();
		}
		return fileMap.keySet().stream().filter(name -> name.startsWith(selectedModuleName))
				.collect(Collectors.toSet());
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
