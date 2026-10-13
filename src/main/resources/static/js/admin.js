/* src/main/resources/static/js/admin.js */

document.addEventListener('DOMContentLoaded', function() {
    if (!api.isAuthenticated()) {
        window.location.href = '/pages/login.html';
        return;
    }
    const user = api.getUser();
    if (!user || user.role !== 'ADMIN') {
        window.location.href = '/pages/login.html';
        return;
    }

    document.getElementById('userName').textContent = user.username;
    updateTime();
    setInterval(updateTime, 1000);

    loadDashboardStats();
    loadRecentUsers();
    loadUserChart();
});

function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('open');
    document.getElementById('sidebarOverlay').classList.toggle('active');
}

function updateTime() {
    document.getElementById('currentTime').textContent = new Date().toLocaleString();
}

async function loadDashboardStats() {
    try {
        const result = await api.get('/admin/dashboard/stats');
        if (result.success) {
            const data = result.data;
            document.getElementById('totalUsers').textContent = data.totalUsers || 0;
            document.getElementById('activeInterns').textContent = data.activeInterns || 0;
            document.getElementById('totalMentors').textContent = data.totalMentors || 0;
            document.getElementById('totalTasks').textContent = data.totalTasks || 0;
            document.getElementById('userCount').textContent = data.totalUsers || 0;
        }
    } catch (error) {
        console.error('Failed to load stats:', error);
    }
}

async function loadRecentUsers() {
    try {
        const result = await api.get('/admin/users?page=0&size=5');
        if (result.success && result.data) {
            const users = result.data.content || [];
            const tbody = document.getElementById('recentUsers');
            if (users.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No users found</td></tr>';
                return;
            }
            tbody.innerHTML = users.map(user => `
                <tr>
                    <td><strong>${user.firstName || ''} ${user.lastName || ''}</strong></td>
                    <td>${user.email}</td>
                    <td><span class="badge bg-${user.role === 'ADMIN' ? 'danger' : user.role === 'MENTOR' ? 'warning' : 'info'}">${user.role}</span></td>
                    <td><span class="badge-status ${user.isActive ? 'active' : 'inactive'}">${user.isActive ? 'Active' : 'Inactive'}</span></td>
                    <td>${user.createdAt ? new Date(user.createdAt).toLocaleDateString() : '-'}</td>
                </tr>
            `).join('');
        }
    } catch (error) {
        console.error('Failed to load users:', error);
    }
}

async function loadUserChart() {
    try {
        const result = await api.get('/admin/dashboard/user-growth?days=30');
        if (result.success && result.data) {
            const ctx = document.getElementById('userChart').getContext('2d');
            new Chart(ctx, {
                type: 'line',
                data: {
                    labels: result.data.labels || [],
                    datasets: [{
                        label: 'New Users',
                        data: result.data.values || [],
                        borderColor: '#4f46e5',
                        backgroundColor: 'rgba(79, 70, 229, 0.1)',
                        fill: true,
                        tension: 0.4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                    scales: { y: { beginAtZero: true } }
                }
            });
        }
    } catch (error) {
        console.error('Failed to load chart:', error);
    }
}