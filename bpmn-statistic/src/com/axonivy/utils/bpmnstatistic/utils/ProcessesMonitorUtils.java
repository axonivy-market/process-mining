package com.axonivy.utils.bpmnstatistic.utils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PF;

import com.axonivy.utils.bpmnstatistic.bo.Arrow;
import com.axonivy.utils.bpmnstatistic.bo.WorkflowProgress;
import com.axonivy.utils.bpmnstatistic.enums.IvyVariable;
import com.axonivy.utils.bpmnstatistic.repo.WorkflowProgressRepository;
import com.axonivy.utils.bpmnstatistic.service.IvyTaskOccurrenceService;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.process.IProcessManager;
import ch.ivyteam.ivy.process.IProjectProcessManager;
import ch.ivyteam.ivy.process.model.Process;
import ch.ivyteam.ivy.process.model.connector.SequenceFlow;
import ch.ivyteam.ivy.process.model.element.ProcessElement;
import ch.ivyteam.ivy.process.model.value.PID;
import ch.ivyteam.ivy.workflow.ITask;
import ch.ivyteam.ivy.workflow.IWorkflowProcessModelVersion;
import ch.ivyteam.ivy.workflow.start.IProcessWebStartable;
import ch.ivyteam.ivy.workflow.start.IWebStartable;
import ch.ivyteam.util.collections.CollectionsUtil;

@SuppressWarnings("restriction")
public class ProcessesMonitorUtils {
	private static ProcessesMonitorUtils instance;
	private static final String PORTAL_START_REQUEST_PATH = "/DefaultApplicationHomePage.ivp";
	private static final String PORTAL_IN_TEAMS_REQUEST_PATH = "InTeams.ivp";
	private static final String REMOVE_DEFAULT_HIGHLIGHT_JS_FUNCTION = "santizeDiagram();";
	private static final String UPDATE_FREQUENCY_COUNT_FOR_TASK_FUNCTION = "addElementFrequency('%s', '%s', '%s', '%s');";
	private static final String FREQUENCY_BACKGROUND_COLOR_LEVEL_VARIABLE_PATTERN = "frequencyBackgroundColorLevel%s";
	private static final int DEFAULT_BACKGROUND_COLOR_LEVEL = 1;
	private static final int HIGHEST_LEVEL_OF_BACKGROUND_COLOR = 6;
	private static final String ADDIATION_INFORMATION_FORMAT = "%s instances (investigation period:%s - %s)";
	private static final String UPDATE_ADDITION_INFORMATION_FUNCTION = "updateAdditionalInformation('%s')";
	private static final WorkflowProgressRepository repo = WorkflowProgressRepository.getInstance();

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
			result.computeIfAbsent(pmvName, key -> new ArrayList<>()).add((IProcessWebStartable) process);
		}
		return result;
	}

	private boolean isNotPortalHomeAndMSTeamsProcess(IWebStartable process) {

		String relativeEncoded = process.getLink().getRelativeEncoded();
		return !StringUtils.endsWithAny(relativeEncoded, PORTAL_START_REQUEST_PATH, PORTAL_IN_TEAMS_REQUEST_PATH);
	}

	public void showStatisticData(String pid) {
		Objects.requireNonNull(pid);
		HashMap<String, Integer> taskCountMap = IvyTaskOccurrenceService.countTaskOccurrencesByProcessId(pid);
		int maxFrequency = findMaxFrequency(taskCountMap);
		String textColorRGBCode = String.valueOf(Ivy.var().get(IvyVariable.FREQUENCY_NUMBER_COLOR.getVariableName()));
		PF.current().executeScript(REMOVE_DEFAULT_HIGHLIGHT_JS_FUNCTION);
		for (Entry<String, Integer> entry : taskCountMap.entrySet()) {
			String backgroundColorRGBCode = getRGBCodefromFrequency(maxFrequency, entry.getValue());
			PF.current().executeScript(String.format(UPDATE_FREQUENCY_COUNT_FOR_TASK_FUNCTION, entry.getKey(),
					entry.getValue(), backgroundColorRGBCode, textColorRGBCode));
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

	private static void convertProcessElementToWorkflowProgress(SequenceFlow flow, String elementId,
			Long caseId) {
		WorkflowProgress result = new WorkflowProgress();
		result.setArrowId(flow.getPid().toString());
		result.setCaseId(caseId);
		result.setOriginElementId(elementId);
		result.setTargetElementId(flow.getTarget().getPid().toString());
		result.setStartTimeStamp(new Date());
		repo.save(result);
	}

	private static void showAdditionalInformation(String instancesCount, String fromDate, String toDate) {
		String additionalInformation = String.format(ADDIATION_INFORMATION_FORMAT, instancesCount, fromDate, toDate);
		PF.current().executeScript(String.format(UPDATE_ADDITION_INFORMATION_FUNCTION, additionalInformation));
	}

	private static void updateExistingWorkflowInfoForElement(String elementId, Long caseId) {
		//TODO: make query
		List<WorkflowProgress> oldArrows = repo.findByTargetElementIdAndCaseId(elementId,caseId);
		if(CollectionUtils.isEmpty(oldArrows)) {
			return;
		}
		oldArrows.stream().forEach(flow-> {
			flow.setEndTimeStamp(new Date());
			flow.setDuration(Duration.between(flow.getEndTimeStamp().toInstant(), flow.getStartTimeStamp().toInstant()));
		});
	}
	
	public static void updateWorkflowInfoForElement(String elementId) {
		Long caseId = Ivy.wf().getCurrentCase().getId();
		ITask currentTask = Ivy.wf().getCurrentTask();
		PID pid =  currentTask.getStart().getProcessElementId();
		String processGuid = pid.getRawPid().split("-")[0];

		IWorkflowProcessModelVersion pmv = currentTask.getProcessModelVersion();	
		IProjectProcessManager manager = IProcessManager.instance().getProjectDataModelFor(pmv);
		Process processRdm = manager.findProcess(processGuid, true).getModel();
		//TODO: find Element by id
		ProcessElement targetElement = processRdm.getProcessElements().stream().filter(element -> element.getPid().toString().equalsIgnoreCase(elementId)).findAny().orElse(null);
		if(Objects.isNull(targetElement)) {
			return;
		}
		updateExistingWorkflowInfoForElement(targetElement.getPid().toString(), caseId);
		targetElement.getOutgoing().stream().forEach(outFlow -> convertProcessElementToWorkflowProgress(outFlow, elementId, caseId));
	}

}
