package com.imprev.swf;

import com.amazonaws.services.simpleworkflow.model.Decision;
import com.amazonaws.services.simpleworkflow.model.DecisionTask;
import com.amazonaws.services.simpleworkflow.model.HistoryEvent;
import com.github.oxo42.stateless4j.StateMachine;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public abstract class FsmDecider implements Decider {

    private static final Logger log = Logger.getLogger(FsmDecider.class);

    @Override
    public List<Decision> execute(DecisionTask task) {
        StateMachine<DecisionStep, EventType> fsm = newFSM();

        List<HistoryEvent> events = task.getEvents();
        Collections.sort(events, (a, b) -> a.getEventTimestamp().compareTo(b.getEventTimestamp()));

        int applied = 0;
        for (HistoryEvent event : events) {
            EventType eventType = EventType.valueOf(event.getEventType());
            if (fsm.canFire(eventType)) {
                fsm.fire(eventType);
                applied++;
            }
            else {
                log.warn("Cannot handle eventType=" + eventType + " when in state=" + fsm.getState());
            }
        }

        log.info("Applied " + applied + " event(s)  current state=" + fsm.getState() + " workflowId=" + task.getWorkflowExecution().getWorkflowId());

        Decision decision = fsm.getState().nextDecision(events);
        return (decision == null) ? null : Arrays.asList(decision);
    }

    @Override
    public int getDefaultExecutionStartToCloseTimeoutSeconds() {
        return 7200;
    }

    @Override
    public String getDeciderName() {
        // NOTE: if you rename a class it changes the default task list it will poll
        return getClass().getSimpleName();
    }

    public abstract StateMachine<DecisionStep, EventType> newFSM();

}
