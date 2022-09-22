package com.amazonaws.samples.connectors.timestream;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import imported.vnext.org.apache.flink.connector.base.sink.sink.writer.SinkMetricGroup;
import org.apache.flink.api.common.operators.MailboxExecutor;
import org.apache.flink.api.connector.sink.Sink;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.metrics.SimpleCounter;
import org.apache.flink.metrics.groups.SinkWriterMetricGroup;
import org.apache.flink.streaming.runtime.tasks.TestProcessingTimeService;
import org.apache.flink.util.UserCodeClassLoader;
import org.mockito.stubbing.Answer;

import java.util.OptionalLong;


public class SinkInitContext implements Sink.InitContext {
    private static final TestProcessingTimeService processingTimeService;

    static {
        processingTimeService = new TestProcessingTimeService();
    }

    private SinkMetricGroup sinkMetricGroup;

    @Override
    public UserCodeClassLoader getUserCodeClassLoader() {
        return null;
    }

    @Override
    public MailboxExecutor getMailboxExecutor() {
        return null;
    }

    @Override
    public Sink.ProcessingTimeService getProcessingTimeService() {
        return new Sink.ProcessingTimeService() {
            @Override
            public long getCurrentProcessingTime() {
                return processingTimeService.getCurrentProcessingTime();
            }

            @Override
            public void registerProcessingTimer(
                    long time, Sink.ProcessingTimeService.ProcessingTimeCallback processingTimerCallback) {
                processingTimeService.registerTimer(
                        time, processingTimerCallback::onProcessingTime);
            }
        };
    }

    @Override
    public int getSubtaskId() {
        return 0;
    }

    @Override
    public int getNumberOfParallelSubtasks() {
        return 0;
    }

    /**
     * @return The metric group this writer belongs to.
     */
    @Override
    public SinkWriterMetricGroup metricGroup() {
        SinkWriterMetricGroup sinkMetricGroup = mock(SinkWriterMetricGroup.class);
        when(sinkMetricGroup.counter(anyString()))
                .thenAnswer(((Answer<SimpleCounter>) invocation -> new SimpleCounter()));

        SinkWriterMetricGroup mg = mock(SinkWriterMetricGroup.class, RETURNS_DEEP_STUBS);
        when(mg.addGroup(anyString())).thenReturn(sinkMetricGroup);

        return mg;
    }

    @Override
    public OptionalLong getRestoredCheckpointId() {
        return null;
    }

    public TestProcessingTimeService getTestProcessingTimeService() {
        return processingTimeService;
    }

    public void setSinkMetricGroup(SinkMetricGroup sinkMetricGroup) {
        this.sinkMetricGroup = sinkMetricGroup;
    }

    public SinkMetricGroup getSinkMetricGroup() {
        return this.sinkMetricGroup;
    }
}
