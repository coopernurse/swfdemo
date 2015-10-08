package com.imprev.swf;

import com.amazonaws.services.simpleworkflow.model.Decision;
import com.amazonaws.services.simpleworkflow.model.HistoryEvent;

import java.util.List;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public interface DecisionStep {

    Decision nextDecision(List<HistoryEvent> events);

}
