/* src/main/resources/static/js/intern.js */

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

    loadInternStats();
    loadMyTasks();
});

function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('open');
    document.getElementById('sidebarOverlay').classList.toggle('active');
}

function updateTime() {
    document.getElementById('currentTime').textContent = new Date().toLocaleString();
}

async function loadInternStats() {
    try {
        const result = await api.get('/intern/dashboard');
        if (result.success && result.data) {
            const data = result.data;
            document.getElementById('totalTasks').textContent = data.totalTasks || 0;
            document.getElementById('completedTasks').textContent = data.completedTasks || 0;
            document.getElementById('pendingTasks').textContent = data.pendingTasks || 0;
            document.getElementById('attendancePercent').textContent = (data.attendancePercentage || 0) + '%';
            document.getElementById('taskCount').textContent = data.pendingTasks || 0;
        }
    } catch (error) {
        console.error('Failed to load stats:', error);
    }
}

async function loadMyTasks() {
    try {
        const result = await api.get('/tasks/my-tasks?page=0&size=5');
        if (result.success && result.data) {
            const tasks = result.data.content || [];
            const tbody = document.getElementById('myTasks');
            if (tasks.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No tasks assigned</td></tr>';
                return;
            }
            tbody.innerHTML = tasks.map(task => `
                <tr>
                    <td>${task.title}</td>
                    <td>${task.dueDate ? new Date(task.dueDate).toLocaleDateString() : '-'}</td>
                    <td><span class="badge-status ${task.status === 'COMPLETED' ? 'completed' : task.status === 'IN_PROGRESS' ? 'pending' : 'active'}">${task.status || 'PENDING'}</span></td>
                    <td><span class="badge bg-${task.priority === 'HIGH' ? 'danger' : task.priority === 'MEDIUM' ? 'warning' : 'info'}">${task.priority || 'MEDIUM'}</span></td>
                </tr>
            `).join('');
        }
    } catch (error) {
        console.error('Failed to load tasks:', error);
    }
}

async function checkIn() {
    try {
        const result = await api.post('/attendance/check-in', {});
        if (result.success) {
            alert('✅ Check-in successful!');
            loadInternStats();
        } else {
            alert('❌ Check-in failed: ' + (result.message || 'Unknown error'));
        }
    } catch (error) {
        alert('❌ Check-in failed: ' + error.message);
    }
}

async function checkOut() {
    try {
        const result = await api.post('/attendance/check-out', {});
        if (result.success) {
            alert('✅ Check-out successful!');
            loadInternStats();
        } else {
            alert('❌ Check-out failed: ' + (result.message || 'Unknown error'));
        }
    } catch (error) {
        alert('❌ Check-out failed: ' + error.message);
    }
}