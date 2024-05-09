package com.axonivy.processmining.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

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
		HashMap<String, Integer> map = getHashMapTaskOccurrencesByProcessId(processId);

		// TODO: This block code is used for verification
		// We will remove it when verification is finished
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			Ivy.log().warn("Task {0} occurs {1} times", entry.getKey(), entry.getValue());
		}

		return map;
	}

	private static HashMap<String, Integer> getHashMapTaskOccurrencesByProcessId(String processId) {
		return Sudo.get(() -> {
			TaskQuery taskQuery = TaskQuery.create().where().requestPath()
					.isLike(String.format(LIKE_TEXT_SEARCH, processId));
			HashMap<String, Integer> map = new HashMap<>();
			countTaskOccurrencesByTaskQuery(map, taskQuery);
			return map;
		});
	}

	private static void countTaskOccurrencesByTaskQuery(HashMap<String, Integer> map, TaskQuery taskQuery) {
		List<ITask> tasks = new ArrayList<>();
		int maxQueryResults = Integer.valueOf(Ivy.var().get(IvyVariable.MAX_QUERY_RESULTS.getVariableName()));
		Integer startIndex = 0;
		do {
			tasks = Ivy.wf().getTaskQueryExecutor().getResults(taskQuery, startIndex, maxQueryResults);
			countTaskOccurrences(map, tasks);
			startIndex += maxQueryResults;
		} while (maxQueryResults == tasks.size());
	}

	private static void countTaskOccurrences(HashMap<String, Integer> map, List<ITask> tasks) {
		for (ITask iTask : tasks) {
			String taskElementId = getTaskElementIdFromRequestPath(iTask.getRequestPath());
			updateNumberOfTaskOccurrences(map, taskElementId);
		}
	}

	private static String getTaskElementIdFromRequestPath(String requestPath) {
		String[] arr = requestPath.split(SLASH);
		// Request Path contains: {PROCESS ID}/.../{NAME OF TASK}
		// So we have get the node before /{NAME OF TASK}
		// Ignore case {PROCESS ID}/{NAME OF TASK}
		return arr != null && arr.length > 2 ? arr[arr.length - 2] : StringUtils.EMPTY;
	}

	private static void updateNumberOfTaskOccurrences(HashMap<String, Integer> map, String taskElementId) {
		if (StringUtils.isNotBlank(taskElementId)) {
			Integer numberOfTaskOccurrences = map.get(taskElementId);
			map.put(taskElementId, numberOfTaskOccurrences != null ? ++numberOfTaskOccurrences : 1);
		}
	}
}
