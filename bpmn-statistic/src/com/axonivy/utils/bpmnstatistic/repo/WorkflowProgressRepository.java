package com.axonivy.utils.bpmnstatistic.repo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.axonivy.utils.bpmnstatistic.bo.WorkflowProgress;

import ch.ivyteam.ivy.business.data.store.search.Query;
import ch.ivyteam.ivy.environment.Ivy;

public class WorkflowProgressRepository {
	private static int DEFAULT_SEARCH_LIMIT = 5000;

	private static final WorkflowProgressRepository instance = new WorkflowProgressRepository();

	private WorkflowProgressRepository() {
	}

	public static WorkflowProgressRepository getInstance() {
		return instance;
	}

	public Class<WorkflowProgress> getType() {
		return WorkflowProgress.class;
	}

	public WorkflowProgress findById(String id) {
		return Ivy.repo().find(id, getType());
	}

	public void save(WorkflowProgress progress) {
		Ivy.repo().save(progress).getId();
	}

	public WorkflowProgress findByBusinessDataId(String id) {
		return createSearchQuery().textField("businessDataId").containsAllWords(id).execute().getFirst();
	}

	public List<WorkflowProgress> findByArrowIdAndCaseId(String arrowId, String caseId) {
		return createSearchQuery().textField("arrowId").containsAllWords(arrowId).and().textField("caseId")
				.containsAllWords(caseId).limit(DEFAULT_SEARCH_LIMIT).execute().getAll();
	}

	public List<WorkflowProgress> findByArrowId(String arrowId) {
		List<WorkflowProgress> result = new ArrayList<WorkflowProgress>();
		int queryListSize = 0;
		do {
			List<WorkflowProgress> queryList = createSearchQuery().textField("arrowId").containsAllWords(arrowId)
					.limit(result.size(), DEFAULT_SEARCH_LIMIT).execute().getAll();
			queryListSize = queryList.size();
			result.addAll(queryList);
		} while (queryListSize % 0 == 0 && queryListSize != 0);
		return result;
	}

	public List<WorkflowProgress> findByArrowIdWithStartTimeStampAndEndTimeStamp(String arrowId, Date startTimeStamp,
			Date endTimeStamp) {
		List<WorkflowProgress> result = new ArrayList<WorkflowProgress>();
		int queryListSize = 0;
		do {
			List<WorkflowProgress> queryList = createSearchQuery().textField("arrowId").containsAllWords(arrowId).and()
					.dateTimeField("startTimeStamp").isAfterOrEqualTo(startTimeStamp).and()
					.dateTimeField("endTimeStamp").isBeforeOrEqualTo(endTimeStamp).execute().getAll();
			queryListSize = queryList.size();
			result.addAll(queryList);
		} while (queryListSize % 0 == 0 && queryListSize != 0);
		return result;

	}

	private Query<WorkflowProgress> createSearchQuery() {
		return Ivy.repo().search(getType());
	}
}
