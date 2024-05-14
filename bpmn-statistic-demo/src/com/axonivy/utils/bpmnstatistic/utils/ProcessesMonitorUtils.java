package com.axonivy.utils.bpmnstatistic.utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.workflow.start.IProcessWebStartable;
import ch.ivyteam.ivy.workflow.start.IWebStartable;
public class ProcessesMonitorUtils {
	private static ProcessesMonitorUtils instance;
	private static final String PORTAL_START_REQUEST_PATH = "/DefaultApplicationHomePage.ivp";
	private static final String PORTAL_IN_TEAMS_REQUEST_PATH = "InTeams.ivp";

	private ProcessesMonitorUtils() {
	};

	public static ProcessesMonitorUtils getInstance() {

		if (instance == null) {
			instance = new ProcessesMonitorUtils();
		}
		return instance;
	}

	public List<IWebStartable> getAllProcesses() {
		return Ivy.session().getStartables().stream().filter(process -> isNotPortalHomeAndMSTeamsProcess(process))
				.collect(Collectors.toList());
	}

	public Map<String, List<IProcessWebStartable>> getProcessesWithPmv() {
		Map<String, List<IProcessWebStartable>> result = new HashMap<>();
		for (IWebStartable process : getAllProcesses()) {
			String pmvName = process.pmv().getProjectName();
			result.computeIfAbsent(pmvName, k -> new ArrayList<>()).add((IProcessWebStartable) process);
		}
		return result;
	}

	private boolean isNotPortalHomeAndMSTeamsProcess(IWebStartable process) {
		String relativeEncoded = process.getLink().getRelativeEncoded();
		return !StringUtils.endsWithAny(relativeEncoded, PORTAL_START_REQUEST_PATH, PORTAL_IN_TEAMS_REQUEST_PATH);
	}
}
