package org.openkilda.floodlight.kafka;

import static java.util.Objects.requireNonNull;

import java.util.Timer;
import java.util.TimerTask;

public class HeartBeat {
    private final Producer producer;
    private final long interval;
    private final String topoDiscoTopic;

    private final Timer timer;
    private TimerTask task;

    public HeartBeat(Producer producer, long interval, String topoDiscoTopic) {
        this.producer = producer;
        this.interval = interval;
        this.topoDiscoTopic = requireNonNull(topoDiscoTopic, "topoDiscoTopic cannot be null");

        task = new HeartBeatAction(producer, topoDiscoTopic);
        timer = new Timer("kafka.HeartBeat",true);
        timer.scheduleAtFixedRate(task, interval, interval);
    }

    public void reschedule() {
        TimerTask replace = new HeartBeatAction(producer, topoDiscoTopic);
        timer.scheduleAtFixedRate(replace, interval, interval);

        synchronized (this) {
            task.cancel();
            task = replace;
        }
    }
}
