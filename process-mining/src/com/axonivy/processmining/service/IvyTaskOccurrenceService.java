package com.axonivy.processmining.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.processmining.bo.TaskOccurrence;
import com.axonivy.processmining.enums.IvyVariable;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.security.exec.Sudo;
import ch.ivyteam.ivy.workflow.ITask;
import ch.ivyteam.ivy.workflow.query.TaskQuery;

public class IvyTaskOccurrenceService {
	private static final String LIKE_TEXT_SEARCH = "%%%s%%";
	private static final String SLASH = "/";

	private IvyTaskOccurrenceService() {
	}

	public static HashMap<String, Integer> countTaskOccurrencesByProcessId(String processId) {
		HashMap<String, TaskOccurrence> taskOccurrenceMap = getHashMapTaskOccurrencesByProcessId(processId);
		HashMap<String, Integer> result = correctTaskOccurrences(taskOccurrenceMap);

		// TODO: This block code is used for verification
		// We will remove it when verification is finished
		for (Map.Entry<String, Integer> taskOccurrence : result.entrySet()) {
			Ivy.log().warn("Task {0} occurs {1} times", taskOccurrence.getKey(), taskOccurrence.getValue());
		}

		return result;
	}

	private static HashMap<String, TaskOccurrence> getHashMapTaskOccurrencesByProcessId(String processId) {
		return Sudo.get(() -> {
			TaskQuery taskQuery = TaskQuery.create().where().requestPath()
					.isLike(String.format(LIKE_TEXT_SEARCH, processId));
			HashMap<String, TaskOccurrence> map = new HashMap<>();
			countTaskOccurrencesByTaskQuery(map, taskQuery);
			return map;
		});
	}

	private static void countTaskOccurrencesByTaskQuery(HashMap<String, TaskOccurrence> map, TaskQuery taskQuery) {
		List<ITask> tasks = new ArrayList<>();
		int maxQueryResults = Integer.valueOf(Ivy.var().get(IvyVariable.MAX_QUERY_RESULTS.getVariableName()));
		Integer startIndex = 0;
		do {
			tasks = Ivy.wf().getTaskQueryExecutor().getResults(taskQuery, startIndex, maxQueryResults);
			countTaskOccurrences(map, tasks);
			startIndex += maxQueryResults;
		} while (maxQueryResults == tasks.size());
	}

	private static void countTaskOccurrences(HashMap<String, TaskOccurrence> taskOccurrenceMap, List<ITask> tasks) {
		for (ITask iTask : tasks) {
			String taskElementId = getTaskElementIdFromRequestPath(iTask.getRequestPath());
			updateTaskOccurrencesMap(taskOccurrenceMap, taskElementId, iTask.getRequestPath());
		}
	}

	private static String getTaskElementIdFromRequestPath(String requestPath) {
		String[] arr = requestPath.split(SLASH);
		// Request Path contains: {PROCESS ID}/.../{NAME OF TASK}
		// So we have get the node before /{NAME OF TASK}
		// Ignore case {PROCESS ID}/{NAME OF TASK}
		return arr != null && arr.length > 2 ? arr[arr.length - 2] : StringUtils.EMPTY;
	}

	private static void updateTaskOccurrencesMap(HashMap<String, TaskOccurrence> taskOccurrenceMap,
			String taskElementId, String requestPath) {
		if (StringUtils.isNotBlank(taskElementId)) {
			TaskOccurrence taskOccurrence = getCountedTaskOccurrence(taskOccurrenceMap, taskElementId, requestPath);
			taskOccurrenceMap.put(taskElementId, taskOccurrence);
		}
	}

	private static TaskOccurrence getCountedTaskOccurrence(HashMap<String, TaskOccurrence> taskOccurrenceMap,
			String taskElementId, String requestPath) {
		TaskOccurrence taskOccurrence = taskOccurrenceMap.get(taskElementId);
		if (taskOccurrence != null) {
			taskOccurrence.setOccurrence(taskOccurrence.getOccurrence() + 1);
			taskOccurrence.getRequestPaths().add(requestPath);
		} else {
			taskOccurrence = new TaskOccurrence(new HashSet<>(), 1);
			taskOccurrence.getRequestPaths().add(requestPath);
		}

		return taskOccurrence;
	}

	private static HashMap<String, Integer> correctTaskOccurrences(HashMap<String, TaskOccurrence> taskOccurrencemap) {
		HashMap<String, Integer> result = new HashMap<>();
		for (Map.Entry<String, TaskOccurrence> entry : taskOccurrencemap.entrySet()) {
			TaskOccurrence taskOccurrence = entry.getValue();
			// Correct task occurrence for the "Tasks" element (TaskSwitchGateway)
			Integer numberOfTaskOccurrence = taskOccurrence.getOccurrence() / taskOccurrence.getRequestPaths().size();
			result.put(entry.getKey(), numberOfTaskOccurrence);
		}

		return result;
	}
}
