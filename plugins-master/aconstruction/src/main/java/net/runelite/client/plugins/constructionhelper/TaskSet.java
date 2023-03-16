package net.runelite.client.plugins.constructionhelper;

import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskSet {
    public List<Task> taskList = new ArrayList<>();

    public TaskSet(Task... tasks) {
        taskList.addAll(Arrays.asList(tasks));
    }

    public void addAll(AConstructionPlugin plugin, Client client, ClientThread clientThread, AConstructionConfig config, List<Class<?>> taskClazzes) {
        for (Class<?> taskClass : taskClazzes) {
            try {
                Constructor ctor = taskClass.getDeclaredConstructor(AConstructionPlugin.class, Client.class, ClientThread.class, AConstructionConfig.class);
                ctor.setAccessible(true);
                taskList.add((Task) ctor.newInstance(plugin, client, clientThread, config));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void clear() {
        taskList.clear();
    }

    /**
     * Iterates through all the tasks in the set and returns
     * the highest priority valid task.
     *
     * @return The first valid task from the task list or null if no valid task.
     */
    public Task getValidTask() {
        for (Task task : this.taskList) {
            if (task.validate()) {
                return task;
            }
        }
        return null;
    }
}
