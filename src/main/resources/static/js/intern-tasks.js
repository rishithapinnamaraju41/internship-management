/* src/main/resources/static/js/intern-tasks.js */

let allTasks = [];

document.addEventListener('DOMContentLoaded', function() {
    if (!api.isAuthenticated()) {
        window.location.href = '/pages/login.html';
        return;
    }
    const user = api.getUser();
    if (!user) {
        window.location.href = '/pages/login.html';
        return;
    }

    document.getElementById('userName').textContent = user.username;
    updateTime();
    setInterval(updateTime, 1000);
    loadInternTasks();
});

function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('open');
    document.getElementById('sidebarOverlay').classList.toggle('active');
}

function updateTime() {
    document.getElementById('currentTime').textContent = new Date().toLocaleString();
}

async function loadInternTasks() {
    try {
        const result = await api.get('/tasks/my-tasks');
        if (result.success) {
            allTasks = result.data.content || [];
            displayTasks(allTasks);
        }
    } catch (error) {
        console.error('Failed to load tasks:', error);
    }
}

function displayTasks(tasks) {
    const tbody = document.getElementById('internTasks');
    if (tasks.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No tasks found</td></tr>';
        return;
    }
    tbody.innerHTML = tasks.map(t => `
        <tr>
            <td><strong>${t.title}</strong></td>
            <td>${t.dueDate ? new Date(t.dueDate).toLocaleDateString() : '-'}</td>
            <td><span class="badge bg-${t.priority === 'HIGH' ? 'danger' : t.priority === 'MEDIUM' ? 'warning' : 'info'}">${t.priority || 'MEDIUM'}</span></td>
            <td><span class="badge-status ${t.status === 'COMPLETED' ? 'completed' : t.status === 'IN_PROGRESS' ? 'pending' : 'active'}">${t.status || 'PENDING'}</span></td>
            <td>
                ${t.status !== 'COMPLETED' ? `<button class="btn btn-sm btn-success" onclick="openSubmitModal('${t.taskId}')">
                    <i class="fas fa-upload"></i> Submit
                </button>` : '<span class="text-success">✅ Completed</span>'}
            </td>
        </tr>
    `).join('');
}

function filterTasks() {
    const filter = document.getElementById('taskFilter').value;
    if (filter === 'all') {
        displayTasks(allTasks);
    } else {
        const filtered = allTasks.filter(t => t.status === filter);
        displayTasks(filtered);
    }
}

function openSubmitModal(taskId) {
    document.getElementById('submitTaskId').value = taskId;
    document.getElementById('submissionText').value = '';
    document.getElementById('attachmentUrl').value = '';
    new bootstrap.Modal(document.getElementById('submitTaskModal')).show();
}

async function submitTask() {
    const taskId = document.getElementById('submitTaskId').value;
    const submissionText = document.getElementById('submissionText').value;
    const attachmentUrl = document.getElementById('attachmentUrl').value;

    if (!submissionText.trim()) {
        alert('Please enter submission text');
        return;
    }

    const data = {
        task: { taskId: taskId },
        submissionText: submissionText,
        attachmentUrl: attachmentUrl || null
    };

    try {
        const result = await api.post('/task-submissions', data);
        if (result.success) {
            alert('✅ Task submitted successfully!');
            document.getElementById('submitTaskModal').querySelector('.btn-close').click();
            loadInternTasks();
        } else {
            alert('❌ Failed to submit task: ' + (result.message || 'Unknown error'));
        }
    } catch (error) {
        alert('❌ Failed to submit task: ' + error.message);
    }
}