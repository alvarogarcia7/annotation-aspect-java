package com.example.aspect;

public class CaseVerifier {
	private boolean unexpected;
	private boolean before;
	private boolean after;
	private boolean expected;

	public void reset(){
		this.unexpected = false;
		this.before = false;
		this.after = false;
		this.expected = false;
	}

	public boolean isExpected() {
		return expected;
	}

	public boolean isUnexpected() {
		return unexpected;
	}

	public boolean isBefore() {
		return before;
	}

	public boolean isAfter() {
		return after;
	}


	public void beforeJointPoint() {
		this.before =true;
	}

	public void afterJointPoint() {
		this.after = true;
	}

	public void capturedExpectedException() {
		this.expected = true;
	}

	public void capturedUnexpectedException() {
		this.unexpected  = true;
	}
}
