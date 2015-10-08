package com.imprev.swf;

import com.amazonaws.services.simpleworkflow.model.Decision;
import com.amazonaws.services.simpleworkflow.model.DecisionTask;

import java.util.List;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public interface Decider {

    String getDeciderName();
    int getDefaultExecutionStartToCloseTimeoutSeconds();
    List<Decision> execute(DecisionTask task);

}
