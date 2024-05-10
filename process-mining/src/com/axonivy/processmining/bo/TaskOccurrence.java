package com.axonivy.processmining.bo;

import java.util.Set;

public class TaskOccurrence {
	private Set<String> requestPaths;
	private Integer occurrence;

	public TaskOccurrence(Set<String> requestPaths, Integer occurrence) {
		super();
		this.requestPaths = requestPaths;
		this.occurrence = occurrence;
	}

	public Set<String> getRequestPaths() {
		return requestPaths;
	}

	public void setRequestPaths(Set<String> requestPaths) {
		this.requestPaths = requestPaths;
	}

	public Integer getOccurrence() {
		return occurrence;
	}

	public void setOccurrence(Integer occurrence) {
		this.occurrence = occurrence;
	}
}
