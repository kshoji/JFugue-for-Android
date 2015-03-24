package org.jfugue.realtime;


public interface ScheduledEvent {
	public void execute(RealtimePlayer player, long timeInMillis);
}
