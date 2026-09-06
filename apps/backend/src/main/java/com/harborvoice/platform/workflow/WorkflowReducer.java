package com.harborvoice.platform.workflow;

public interface WorkflowReducer<S> {
    S initial();

    S apply(S state, WorkflowEvent event);
}
