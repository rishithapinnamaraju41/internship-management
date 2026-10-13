/* src/main/resources/static/js/admin-batches.js */

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
    loadBatches();
});

function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('open');
    document.getElementById('sidebarOverlay').classList.toggle('active');
}

function updateTime() {
    document.getElementById('currentTime').textContent = new Date().toLocaleString();
}

async function loadBatches() {
    try {
        const result = await api.get('/batches');
        if (result.success) {
            const batches = result.data || [];
            const tbody = document.getElementById('batchesTable');
            if (batches.length === 0) {
                tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">No batches found</td></tr>';
                return;
            }
            tbody.innerHTML = batches.map(b => `
                <tr>
                    <td><strong>${b.batchName}</strong></td>
                    <td>${b.batchCode}</td>
                    <td>${b.startDate ? new Date(b.startDate).toLocaleDateString() : '-'}</td>
                    <td>${b.endDate ? new Date(b.endDate).toLocaleDateString() : '-'}</td>
                    <td>${b.maxCapacity}</td>
                    <td>${b.currentCount || 0}</td>
                    <td><span class="badge-status ${b.status === 'ACTIVE' ? 'active' : 'inactive'}">${b.status || 'ACTIVE'}</span></td>
                    <td>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteBatch('${b.batchId}')">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>
            `).join('');
        }
    } catch (error) {
        console.error('Failed to load batches:', error);
    }
}

async function addBatch() {
    const data = {
        batchName: document.getElementById('batchName').value,
        batchCode: document.getElementById('batchCode').value,
        description: document.getElementById('batchDescription').value,
        startDate: document.getElementById('batchStartDate').value,
        endDate: document.getElementById('batchEndDate').value,
        maxCapacity: parseInt(document.getElementById('batchMaxCapacity').value) || 20
    };

    try {
        const result = await api.post('/batches', data);
        if (result.success) {
            alert('✅ Batch created successfully!');
            document.getElementById('addBatchModal').querySelector('.btn-close').click();
            loadBatches();
        } else {
            alert('❌ Failed to create batch: ' + (result.message || 'Unknown error'));
        }
    } catch (error) {
        alert('❌ Failed to create batch: ' + error.message);
    }
}

async function deleteBatch(batchId) {
    if (!confirm('Are you sure you want to delete this batch?')) return;
    try {
        const result = await api.delete(`/batches/${batchId}`);
        if (result.success) {
            alert('✅ Batch deleted successfully');
            loadBatches();
        } else {
            alert('❌ Failed to delete batch');
        }
    } catch (error) {
        alert('❌ Failed to delete batch: ' + error.message);
    }
}