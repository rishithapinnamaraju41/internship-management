package com.appticode.service;

import com.appticode.entity.Batch;
import com.appticode.entity.Task;
import com.appticode.repository.BatchRepository;
import com.appticode.repository.TaskRepository;
import com.appticode.utils.CustomIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private CustomIdGenerator idGenerator;

    public Task createTask(Task task) {
        task.setTaskId(idGenerator.generateTaskId());
        task.setStatus("PENDING");
        task.setCreatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public Optional<Task> updateTask(String taskId, Task taskDetails) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            if (taskDetails.getTitle() != null) task.setTitle(taskDetails.getTitle());
            if (taskDetails.getDescription() != null) task.setDescription(taskDetails.getDescription());
            if (taskDetails.getDueDate() != null) task.setDueDate(taskDetails.getDueDate());
            if (taskDetails.getTotalPoints() != null) task.setTotalPoints(taskDetails.getTotalPoints());
            if (taskDetails.getPriority() != null) task.setPriority(taskDetails.getPriority());
            if (taskDetails.getStatus() != null) task.setStatus(taskDetails.getStatus());
            task.setUpdatedAt(LocalDateTime.now());
            return Optional.of(taskRepository.save(task));
        }
        return Optional.empty();
    }

    public Optional<Task> getTaskById(String taskId) {
        return taskRepository.findById(taskId);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Page<Task> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    public List<Task> getTasksByBatch(String batchId) {
        return taskRepository.findByBatchId(batchId);
    }

    public List<Task> getTasksByStatus(String status) {
        return taskRepository.findByStatus(status);
    }

    public List<Task> getTasksByPriority(String priority) {
        return taskRepository.findByPriority(priority);
    }

    public boolean deleteTask(String taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            taskRepository.delete(taskOpt.get());
            return true;
        }
        return false;
    }

    public boolean assignTaskToBatch(String taskId, String batchId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        Optional<Batch> batchOpt = batchRepository.findById(batchId);
        
        if (taskOpt.isPresent() && batchOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setBatch(batchOpt.get());
            taskRepository.save(task);
            return true;
        }
        return false;
    }

    public List<Task> getUpcomingTasks() {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        return taskRepository.findUpcomingTasks(today, nextWeek);
    }

    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDate.now());
    }

    public List<Task> getTasksDueOn(LocalDate date) {
        return taskRepository.findTasksDueOn(date);
    }

    public List<Task> getTasksByDueDateRange(LocalDate startDate, LocalDate endDate) {
        return taskRepository.findTasksByDueDateRange(startDate, endDate);
    }

    public boolean updateTaskStatus(String taskId, String status) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            taskRepository.updateTaskStatus(taskId, status);
            return true;
        }
        return false;
    }

    public Double getAverageTaskPoints(String batchId) {
        return taskRepository.getAverageTaskPoints(batchId);
    }

    public List<Object[]> countTasksByStatus() {
        return taskRepository.countTasksByStatus();
    }

    public List<Object[]> countTasksByPriority() {
        return taskRepository.countTasksByPriority();
    }

    public List<Task> getTasksByMentor(String mentorId) {
        return taskRepository.findByAssignedByMentorId(mentorId);
    }
}