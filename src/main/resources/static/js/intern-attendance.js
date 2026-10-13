/* src/main/resources/static/js/intern-attendance.js */

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
    
    // Set default dates
    const today = new Date().toISOString().split('T')[0];
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
    document.getElementById('attendanceStartDate').value = thirtyDaysAgo.toISOString().split('T')[0];
    document.getElementById('attendanceEndDate').value = today;
    
    loadAttendance();
});

function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('open');
    document.getElementById('sidebarOverlay').classList.toggle('active');
}

function updateTime() {
    document.getElementById('currentTime').textContent = new Date().toLocaleString();
}

async function loadAttendance() {
    const startDate = document.getElementById('attendanceStartDate').value;
    const endDate = document.getElementById('attendanceEndDate').value;
    
    try {
        // Get attendance summary
        const summaryResult = await api.get(`/attendance/intern/INT001/summary?startDate=${startDate}&endDate=${endDate}`);
        if (summaryResult.success && summaryResult.data) {
            const data = summaryResult.data;
            document.getElementById('totalDays').textContent = data.totalDays || 0;
            document.getElementById('presentDays').textContent = data.presentDays || 0;
            document.getElementById('absentDays').textContent = (data.totalDays || 0) - (data.presentDays || 0);
            document.getElementById('attendancePercent').textContent = (data.attendancePercentage || 0) + '%';
        }

        // Get attendance history
        const historyResult = await api.get(`/attendance/intern/INT001/range?startDate=${startDate}&endDate=${endDate}`);
        if (historyResult.success) {
            const records = historyResult.data || [];
            const tbody = document.getElementById('attendanceHistory');
            if (records.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No attendance records found</td></tr>';
                return;
            }
            tbody.innerHTML = records.map(r => `
                <tr>
                    <td>${r.date ? new Date(r.date).toLocaleDateString() : '-'}</td>
                    <td>${r.checkInTime || '-'}</td>
                    <td>${r.checkOutTime || '-'}</td>
                    <td><span class="badge-status ${r.status === 'PRESENT' ? 'active' : r.status === 'LATE' ? 'pending' : 'inactive'}">${r.status || '-'}</span></td>
                    <td>${r.workHours || 0}h</td>
                </tr>
            `).join('');
        }
    } catch (error) {
        console.error('Failed to load attendance:', error);
    }
}

function filterAttendance() {
    loadAttendance();
}

async function checkIn() {
    try {
        const result = await api.post('/attendance/check-in', {});
        if (result.success) {
            alert('✅ Check-in successful!');
            loadAttendance();
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
            loadAttendance();
        } else {
            alert('❌ Check-out failed: ' + (result.message || 'Unknown error'));
        }
    } catch (error) {
        alert('❌ Check-out failed: ' + error.message);
    }
}