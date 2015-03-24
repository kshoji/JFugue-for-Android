package org.jfugue.realtime;

public abstract class RealtimeInterpolator {
	private boolean started;
	private boolean active;
	private boolean ended;
	private long startTime;
	private long durationInMillis;
	
	public RealtimeInterpolator() {
		this.started = false;
		this.active = false;
		this.ended = false;
	}

	public void setDurationInMillis(long durationInMillis) {
	    this.durationInMillis = durationInMillis;
	}
	
	public void start(long startTime) {
		this.startTime = startTime;
		this.started = true;
		this.active = true;
		this.ended = false;
	}
	
	public void end() {
		this.started = true;
		this.active = false;
		this.ended = true;
	}
	
	public boolean isStarted() {
		return this.started;
	}
	
	public boolean isActive() {
		return this.active;
	}
	
	public boolean isEnded() {
		return this.ended;
	}
	
	public long getStartTime() {
		return this.startTime;
	}
	
	public long getDurationInMillis() {
		return this.durationInMillis;
	}
	
	public abstract void update(RealtimePlayer realtimePlayer, long elapsedTime, double percentComplete);
}
