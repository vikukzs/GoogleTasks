package com.example.quickstart;

import com.google.api.services.tasks.model.Task;

import java.util.List;

public class MessageEvent {

    private List<Task> tasks;

    public MessageEvent(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Task> getTasks() {
        return tasks;
    }
}
