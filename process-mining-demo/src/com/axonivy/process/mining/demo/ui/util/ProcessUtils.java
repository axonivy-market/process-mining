package com.axonivy.process.mining.demo.ui.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axonivy.portal.components.service.impl.ProcessService;

import ch.ivyteam.ivy.workflow.start.IWebStartable;

public class ProcessUtils {
	private static ProcessUtils instance;

	private ProcessUtils() {
	};

	public static ProcessUtils getInstance() {
		if (instance == null) {
			instance = new ProcessUtils();
		}
		return instance;
	}

	public List<IWebStartable> getAllProcesses() {
		return ProcessService.getInstance().findProcesses().getProcesses();
	}

	public Map<String, List<IWebStartable>> getProcessesWithPmv() {
		Map<String, List<IWebStartable>> result = new HashMap<>();
		for (IWebStartable process : getAllProcesses()) {
			String pmvName = process.pmv().getProjectName();
			result.computeIfAbsent(pmvName, k -> new ArrayList<>()).add(process);
		}
		return result;
	}

}
