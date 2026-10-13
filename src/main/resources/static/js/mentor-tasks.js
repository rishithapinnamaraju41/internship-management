/* src/main/resources/static/js/mentor-tasks.js */

document.addEventListener('DOMContentLoaded', function() {
    if (!api.isAuthenticated()) {
        window.location.href = '/pages/login.html';
        return;
    }
    const user = api.getUser();
    if (!user || (user.role !== 'MENTOR' && user.role !== 'ADMIN')) {
        window.location.href = '/pages/login.html';
        return;
    }

    document.getElementById('userName').textContent = user.username;
    updateTime();
    setInterval(updateTime, 1000);
    loadMentorTasks();
});

function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('open');
    document.getElementById('sidebarOverlay').classList.toggle('active');
}

function updateTime() {
    document.getElementById('currentTime').textContent = new Date().toLocaleString();
}

async function loadMentorTasks() {
    try {
        const result = await api.get('/tasks');
        if (result.success) {
            const tasks = result.data || [];
            const tbody = document.getElementById('mentorTasks');
            if (tasks.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No tasks found</td></tr>';
                return;
            }
            tbody.innerHTML = tasks.map(t => `
                <tr>
                    <td><strong>${t.title}</strong></td>
                    <td>${t.batch?.batchName || '-'}</td>
                    <td>${t.dueDate ? new Date(t.dueDate).toLocaleDateString() : '-'}</td>
                    <td><span class="badge bg-${t.priority === 'HIGH' ? 'danger' : t.priority === 'MEDIUM' ? 'warning' : 'info'}">${t.priority || 'MEDIUM'}</span></td>
                    <td><span class="badge-status ${t.status === 'COMPLETED' ? 'completed' : t.status === 'IN_PROGRESS' ? 'pending' : 'active'}">${t.status || 'PENDING'}</span></td>
                    <td>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteTask('${t.taskId}')">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>
            `).join('');
        }
    } catch (error) {
        console.error('Failed to load tasks:', error);
    }
}

async function createTask() {
    const data = {
        title: document.getElementById('taskTitle').value,
        description: document.getElementById('taskDescription').value,
        dueDate: document.getElementById('taskDueDate').value,
        totalPoints: parseInt(document.getElementById('taskTotalPoints').value) || 100,
        priority: document.getElementById('taskPriority').value
    };

    if (document.getElementById('taskBatchId').value) {
        data.batch = { batchId: document.getElementById('taskBatchId').value };
    }

    try {
        const result = await api.post('/tasks', data);
        if (result.success) {
            alert('✅ Task created successfully!');
            document.getElementById('createTaskModal').querySelector('.btn-close').click();
            loadMentorTasks();
        } else {
            alert('❌ Failed to create task: ' + (result.message || 'Unknown error'));
        }
    } catch (error) {
        alert('❌ Failed to create task: ' + error.message);
    }
}

async function deleteTask(taskId) {
    if (!confirm('Are you sure you want to delete this task?')) return;
    try {
        const result = await api.delete(`/tasks/${taskId}`);
        if (result.success) {
            alert('✅ Task deleted successfully');
            loadMentorTasks();
        } else {
            alert('❌ Failed to delete task');
        }
    } catch (error) {
        alert('❌ Failed to delete task: ' + error.message);
    }
}