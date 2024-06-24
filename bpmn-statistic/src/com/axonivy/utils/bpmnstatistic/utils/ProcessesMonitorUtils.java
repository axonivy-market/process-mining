package com.axonivy.utils.bpmnstatistic.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.PF;

import com.axonivy.utils.bpmnstatistic.bo.Arrow;
import com.axonivy.utils.bpmnstatistic.enums.IvyVariable;
import com.axonivy.utils.bpmnstatistic.service.IvyTaskOccurrenceService;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.process.model.BaseElement;
import ch.ivyteam.ivy.process.model.Process;
import ch.ivyteam.ivy.process.model.connector.SequenceFlow;
import ch.ivyteam.ivy.process.model.value.PID;
import ch.ivyteam.ivy.process.rdm.IProcessManager;
import ch.ivyteam.ivy.workflow.IWorkflowProcessModelVersion;
import ch.ivyteam.ivy.workflow.start.IProcessWebStartable;
import ch.ivyteam.ivy.workflow.start.IWebStartable;

public class ProcessesMonitorUtils {
	private static ProcessesMonitorUtils instance;
	private static final String PORTAL_START_REQUEST_PATH = "/DefaultApplicationHomePage.ivp";
	private static final String PORTAL_IN_TEAMS_REQUEST_PATH = "InTeams.ivp";
	private static final String REMOVE_DEFAULT_HIGHLIGHT_JS_FUNCTION = "santizeDiagram();";
	private static final String UPDATE_FREQUENCY_COUNT_FOR_TASK = "addElementFrequency('%s', '%s', '%s', '%s');";
	private static final String FREQUENCY_BACKGROUND_COLOR_LEVEL_VARIABLE_PATTERN = "frequencyBackgroundColorLevel%s";
	private static final int DEFAULT_BACKGROUND_COLOR_LEVEL = 1;
	private static final int HIGHEST_LEVEL_OF_BACKGROUND_COLOR = 6;

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
			String pmvName = process.pmv().getName();
			if(process.getName().equals("aasdasd.ivp")) {
				getBaseElementOf((IProcessWebStartable) process);
			}
			result.computeIfAbsent(pmvName, key -> new ArrayList<>()).add((IProcessWebStartable) process);
		}
		return result;
	}

	private boolean isNotPortalHomeAndMSTeamsProcess(IWebStartable process) {

		String relativeEncoded = process.getLink().getRelativeEncoded();
		return !StringUtils.endsWithAny(relativeEncoded, PORTAL_START_REQUEST_PATH, PORTAL_IN_TEAMS_REQUEST_PATH);
	}

	public void showStatisticData(String pid) {
		Ivy.log().error(pid);
		Objects.requireNonNull(pid);
		HashMap<String, Integer> taskCountMap = IvyTaskOccurrenceService.countTaskOccurrencesByProcessId(pid);
		int maxFrequency = findMaxFrequency(taskCountMap);
		String textColorRGBCode = String.valueOf(Ivy.var().get(IvyVariable.FREQUENCY_NUMBER_COLOR.getVariableName()));
		PF.current().executeScript(REMOVE_DEFAULT_HIGHLIGHT_JS_FUNCTION);
		for (Entry<String, Integer> entry : taskCountMap.entrySet()) {
			String backgroundColorRGBCode = getRGBCodefromFrequency(maxFrequency, entry.getValue());
			PF.current().executeScript(String.format(UPDATE_FREQUENCY_COUNT_FOR_TASK, entry.getKey(), entry.getValue(),
					backgroundColorRGBCode, textColorRGBCode));
		}
	}

	private int findMaxFrequency(HashMap<String, Integer> taskCountMap) {
		int max = 0;
		for (Entry<String, Integer> entry : taskCountMap.entrySet()) {
			max = max < entry.getValue() ? entry.getValue() : max;
		}
		return max;
	}

	private String getRGBCodefromFrequency(int max, int current) {
		int level = (int) (max == 0 ? DEFAULT_BACKGROUND_COLOR_LEVEL
				: Math.ceil(current * HIGHEST_LEVEL_OF_BACKGROUND_COLOR / max));
		return String.valueOf(Ivy.var().get(String.format(FREQUENCY_BACKGROUND_COLOR_LEVEL_VARIABLE_PATTERN, level)));
	}

	public static List<Arrow> getBaseElementOf(IProcessWebStartable selectedWebStartable) {
		List<Arrow> arrows = new ArrayList<>();
		if (Objects.isNull(selectedWebStartable)) {
			return null;
		}
		PID pid = selectedWebStartable.pid();
		IWorkflowProcessModelVersion pmv = (IWorkflowProcessModelVersion) selectedWebStartable.pmv();
		String processGuid = pid.getRawPid().split("-")[0];
		List<SequenceFlow> flows = new ArrayList<>();
		var manager = IProcessManager.instance().getProjectDataModelFor(pmv);
		Process processRdm = manager.findProcess(processGuid, true).getModel();
		BaseElement taskElement = processRdm.search().pid(pid).findOneDeep();
		processRdm.getProcessElements().stream()
				.forEach(element -> arrows.addAll(getArrowFromProcessElement(element.getOutgoing())));
		arrows.forEach(item-> Ivy.log().error(item.toString()));
		return arrows;
	}

	private static List<Arrow> getArrowFromProcessElement(List<SequenceFlow> outFlow) {
		return outFlow.stream().map(flow -> new Arrow(flow.getPid().getFieldId(), null, flow.getName()))
				.collect(Collectors.toList());
	}

	public static BaseElement getBaseElementByPid(PID pid, IWorkflowProcessModelVersion pmv) {
		String processGuid = pid.getRawPid().split("-")[0];

		var manager = IProcessManager.instance().getProjectDataModelFor(pmv);
		Process processRdm = manager.findProcess(processGuid, true).getModel();
		BaseElement taskElement = processRdm.search().pid(pid).findOneDeep();
		return taskElement;
	}
}
