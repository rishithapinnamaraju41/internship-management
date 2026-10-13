/* src/main/resources/static/js/admin-mentors.js */

// Global variables
let allMentors = [];
let currentPage = 0;
let totalPages = 0;
let totalElements = 0;
let pageSize = 10;
let currentSearchTerm = '';

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('Mentor Management initialized');
    
    // Check if API exists
    if (typeof api === 'undefined') {
        console.error('API object not found!');
        showError('API not loaded. Please refresh the page.');
        return;
    }
    
    // Check authentication
    if (!api.isAuthenticated()) {
        console.log('Not authenticated, redirecting...');
        window.location.href = '/pages/login.html';
        return;
    }
    
    const user = api.getUser();
    console.log('Current user:', user);
    
    if (!user || user.role !== 'ADMIN') {
        console.log('Not admin, redirecting...');
        window.location.href = '/pages/login.html';
        return;
    }

    // Set user name
    const userNameElement = document.getElementById('userName');
    if (userNameElement) {
        userNameElement.textContent = user.username || 'Admin';
    }
    
    // Update time
    updateTime();
    setInterval(updateTime, 1000);
    
    // Load mentors
    loadMentors();
    
    // Setup event listeners
    setupEventListeners();
});

// Setup event listeners
function setupEventListeners() {
    // Search with debounce
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(function(e) {
            searchMentors(e.target.value);
        }, 300));
    }

    // Enter key for search
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchMentors(this.value);
            }
        });
    }

    // Clear search button
    const clearSearchBtn = document.getElementById('clearSearch');
    if (clearSearchBtn) {
        clearSearchBtn.addEventListener('click', clearSearch);
    }

    // Add mentor form submission
    const addForm = document.getElementById('addMentorForm');
    if (addForm) {
        addForm.addEventListener('submit', function(e) {
            e.preventDefault();
            addMentor();
        });
    }

    // Edit mentor form submission
    const editForm = document.getElementById('editMentorForm');
    if (editForm) {
        editForm.addEventListener('submit', function(e) {
            e.preventDefault();
            updateMentor();
        });
    }

    // Pagination buttons
    const prevBtn = document.getElementById('prevPage');
    const nextBtn = document.getElementById('nextPage');
    
    if (prevBtn) {
        prevBtn.addEventListener('click', function() {
            if (currentPage > 0) {
                loadMentors(currentPage - 1);
            }
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener('click', function() {
            if (currentPage < totalPages - 1) {
                loadMentors(currentPage + 1);
            }
        });
    }

    // Keyboard shortcuts
    document.addEventListener('keydown', function(e) {
        // Ctrl+Shift+N to open add mentor modal
        if (e.ctrlKey && e.shiftKey && e.key === 'N') {
            e.preventDefault();
            const modal = new bootstrap.Modal(document.getElementById('addMentorModal'));
            modal.show();
        }
        // Escape to close modals
        if (e.key === 'Escape') {
            const modals = document.querySelectorAll('.modal.show');
            modals.forEach(modal => {
                const instance = bootstrap.Modal.getInstance(modal);
                if (instance) instance.hide();
            });
        }
        // Ctrl+R to refresh
        if (e.ctrlKey && e.key === 'r') {
            e.preventDefault();
            loadMentors(currentPage);
        }
    });
}

// Debounce utility function
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Toggle sidebar for mobile
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebarOverlay');
    if (sidebar) sidebar.classList.toggle('open');
    if (overlay) overlay.classList.toggle('active');
}

// Update current time
function updateTime() {
    const timeElement = document.getElementById('currentTime');
    if (timeElement) {
        timeElement.textContent = new Date().toLocaleString();
    }
}

// Show toast notification
function showToast(message, type = 'success', duration = 3000) {
    // Remove existing toasts
    const existingToasts = document.querySelectorAll('.toast');
    existingToasts.forEach(t => t.remove());
    
    const toastContainer = document.getElementById('toastContainer') || createToastContainer();
    const toastId = 'toast-' + Date.now();
    
    const icon = type === 'success' ? 'check-circle' : 
                 type === 'error' ? 'exclamation-circle' : 
                 type === 'warning' ? 'exclamation-triangle' : 'info-circle';
    
    const bgColor = type === 'success' ? 'bg-success' : 
                    type === 'error' ? 'bg-danger' : 
                    type === 'warning' ? 'bg-warning' : 'bg-info';
    
    const textColor = type === 'warning' ? 'text-dark' : 'text-white';
    
    const toastHtml = `
        <div id="${toastId}" class="toast align-items-center ${bgColor} ${textColor} border-0" role="alert" aria-live="assertive" aria-atomic="true" style="min-width: 300px; max-width: 500px;">
            <div class="d-flex">
                <div class="toast-body">
                    <i class="fas fa-${icon} me-2"></i>
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-${type === 'warning' ? 'dark' : 'white'} me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;
    
    toastContainer.innerHTML += toastHtml;
    
    const toastElement = document.getElementById(toastId);
    try {
        const toast = new bootstrap.Toast(toastElement, { delay: duration, autohide: true });
        toast.show();
        
        toastElement.addEventListener('hidden.bs.toast', function() {
            this.remove();
        });
    } catch(e) {
        console.error('Toast error:', e);
        setTimeout(() => {
            if (toastElement) toastElement.remove();
        }, duration);
    }
}

// Create toast container if it doesn't exist
function createToastContainer() {
    const container = document.createElement('div');
    container.id = 'toastContainer';
    container.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 9999; max-width: 500px; width: 100%;';
    document.body.appendChild(container);
    return container;
}

// Show error message
function showError(message) {
    const tbody = document.getElementById('mentorsTable');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center text-danger py-4">
                    <i class="fas fa-exclamation-circle fa-2x d-block mb-3"></i>
                    <h5>${message}</h5>
                    <button class="btn btn-outline-primary btn-sm mt-2" onclick="loadMentors(${currentPage})">
                        <i class="fas fa-redo me-1"></i> Retry
                    </button>
                </td>
            </tr>
        `;
    }
}

// Show loading spinner
function showLoading() {
    const tbody = document.getElementById('mentorsTable');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center py-5">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                    <p class="mt-2 text-muted">Loading mentors...</p>
                </td>
            </tr>
        `;
    }
}

// Load mentors with pagination
async function loadMentors(page = 0) {
    console.log('loadMentors called with page:', page);
    
    currentPage = page;
    showLoading();
    
    try {
        // Try different endpoints
        let response = null;
        let usedEndpoint = '';
        
        // Try /mentors/getAll first
        try {
            console.log('Fetching mentors from /mentors/getAll');
            response = await api.get('/mentors/getAll');
            usedEndpoint = '/mentors/getAll';
        } catch (e) {
            console.log('Failed to fetch from /mentors/getAll, trying /mentors');
            try {
                response = await api.get('/mentors');
                usedEndpoint = '/mentors';
            } catch (e2) {
                console.log('Failed to fetch from /mentors, trying /api/mentors');
                response = await api.get('/api/mentors');
                usedEndpoint = '/api/mentors';
            }
        }
        
        console.log('API Response from', usedEndpoint, ':', response);
        
        // Handle different response formats
        let mentors = [];
        
        if (response) {
            // Case 1: Response has success flag and data
            if (response.success && response.data) {
                if (Array.isArray(response.data)) {
                    mentors = response.data;
                } else if (response.data.content && Array.isArray(response.data.content)) {
                    mentors = response.data.content;
                }
            }
            // Case 2: Response is the direct array
            else if (Array.isArray(response)) {
                mentors = response;
            }
            // Case 3: Response has data property
            else if (response.data && Array.isArray(response.data)) {
                mentors = response.data;
            }
            // Case 4: Response has content property (Spring paginated)
            else if (response.content && Array.isArray(response.content)) {
                mentors = response.content;
            }
            // Case 5: Try to find any array property
            else if (typeof response === 'object') {
                for (const key in response) {
                    if (Array.isArray(response[key]) && response[key].length > 0) {
                        mentors = response[key];
                        console.log(`Found mentors in property: ${key}`);
                        break;
                    }
                }
            }
        }
        
        // Store all mentors
        allMentors = mentors;
        console.log('Total mentors loaded:', allMentors.length);
        
        if (allMentors.length === 0) {
            showToast('No mentors found in the system', 'info');
            renderMentorsTable([]);
            updatePagination(0);
            return;
        }
        
        // Apply search filter if needed
        let filteredMentors = allMentors;
        if (currentSearchTerm) {
            const searchLower = currentSearchTerm.toLowerCase();
            filteredMentors = allMentors.filter(mentor => 
                (mentor.name && mentor.name.toLowerCase().includes(searchLower)) ||
                (mentor.department && mentor.department.toLowerCase().includes(searchLower)) ||
                (mentor.specialization && mentor.specialization.toLowerCase().includes(searchLower)) ||
                (mentor.employeeId && mentor.employeeId.toLowerCase().includes(searchLower)) ||
                (mentor.user && mentor.user.email && mentor.user.email.toLowerCase().includes(searchLower))
            );
            console.log('Filtered mentors count:', filteredMentors.length);
        }
        
        // Update pagination
        totalElements = filteredMentors.length;
        totalPages = Math.max(1, Math.ceil(totalElements / pageSize));
        
        // Get current page data
        const start = currentPage * pageSize;
        const end = Math.min(start + pageSize, totalElements);
        const pageMentors = filteredMentors.slice(start, end);
        
        // Render table
        renderMentorsTable(pageMentors);
        
        // Update pagination
        updatePagination(start, end);
        
        console.log('Mentors loaded successfully');
        
    } catch (error) {
        console.error('Failed to load mentors:', error);
        console.error('Error stack:', error.stack);
        
        showToast('Failed to load mentors: ' + error.message, 'error', 5000);
        showError('Failed to load mentors: ' + error.message);
    }
}

// Update pagination UI
function updatePagination(start, end) {
    const paginationInfo = document.getElementById('paginationInfo');
    const pageInfo = document.getElementById('pageInfo');
    const prevBtn = document.getElementById('prevPage');
    const nextBtn = document.getElementById('nextPage');
    const mentorCount = document.getElementById('mentorCount');
    
    if (mentorCount) mentorCount.textContent = totalElements;
    if (paginationInfo) {
        paginationInfo.textContent = totalElements > 0 ? 
            `Showing ${start + 1}-${end} of ${totalElements} mentors` : 
            'No mentors found';
    }
    if (pageInfo) {
        pageInfo.textContent = totalPages > 0 ? 
            `Page ${currentPage + 1} of ${totalPages}` : 
            'Page 1 of 1';
    }
    if (prevBtn) prevBtn.disabled = currentPage === 0 || totalPages <= 1;
    if (nextBtn) nextBtn.disabled = currentPage >= totalPages - 1 || totalPages <= 1;
}

// Render mentors table
function renderMentorsTable(mentors) {
    const tbody = document.getElementById('mentorsTable');
    
    if (!tbody) {
        console.error('mentorsTable element not found');
        return;
    }
    
    if (!mentors || mentors.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center text-muted py-5">
                    <i class="fas fa-inbox fa-2x d-block mb-3 text-muted"></i>
                    No mentors found
                    ${currentSearchTerm ? `<p class="mt-2">No results for "<strong>${escapeHtml(currentSearchTerm)}</strong>"</p>` : ''}
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = mentors.map((mentor, index) => `
        <tr class="mentor-row" data-mentor-id="${mentor.mentorId || ''}">
            <td>
                <div class="d-flex align-items-center">
                    <div class="avatar-sm me-2" style="width: 40px; height: 40px; border-radius: 50%; background: ${getAvatarColor(mentor.name)}; color: white; display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 16px; flex-shrink: 0;">
                        ${mentor.name ? mentor.name.charAt(0).toUpperCase() : 'M'}
                    </div>
                    <div>
                        <strong>${escapeHtml(mentor.name || '')}</strong>
                        <div class="small text-muted">${escapeHtml(mentor.employeeId || '')}</div>
                    </div>
                </div>
            </td>
            <td>${escapeHtml(mentor.user?.email || '-')}</td>
            <td>${escapeHtml(mentor.department || '-')}</td>
            <td>${escapeHtml(mentor.specialization || '-')}</td>
            <td>${escapeHtml(mentor.experienceYears || 0)} years</td>
            <td>
                <span class="badge ${mentor.currentInterns >= mentor.maxInterns ? 'bg-danger' : 'bg-success'}">
                    ${mentor.currentInterns || 0}/${mentor.maxInterns || 0}
                </span>
            </td>
            <td>
                <div class="btn-group btn-group-sm" role="group">
                    <button class="btn btn-outline-primary" onclick="openEditModal('${mentor.mentorId || ''}')" title="Edit Mentor">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-outline-danger" onclick="deleteMentor('${mentor.mentorId || ''}')" title="Delete Mentor">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Get avatar color based on name
function getAvatarColor(name) {
    if (!name) return '#6c757d';
    
    const colors = [
        '#4f46e5', '#7c3aed', '#2563eb', '#0891b2', '#059669',
        '#d97706', '#dc2626', '#7c3aed', '#4f46e5', '#2563eb',
        '#db2777', '#9333ea', '#0d9488', '#059669', '#d97706'
    ];
    
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
        hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    return colors[Math.abs(hash) % colors.length];
}

// Search mentors
function searchMentors(searchTerm) {
    const trimmedTerm = searchTerm.trim();
    console.log('Searching for:', trimmedTerm);
    
    if (trimmedTerm && trimmedTerm.length < 2) {
        console.log('Search term too short, ignoring');
        return;
    }
    
    currentSearchTerm = trimmedTerm;
    currentPage = 0;
    loadMentors(0);
}

// Clear search
function clearSearch() {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.value = '';
        currentSearchTerm = '';
        currentPage = 0;
        loadMentors(0);
        console.log('Search cleared');
    }
}

// Validate mentor data
function validateMentorData(data, isEdit = false) {
    const errors = [];
    
    if (!data.userId) {
        errors.push('User ID is required');
    }
    
    if (!data.name || data.name.length < 2) {
        errors.push('Name must be at least 2 characters');
    }
    
    if (!data.employeeId || data.employeeId.length < 2) {
        errors.push('Employee ID is required');
    }
    
    if (data.experienceYears < 0) {
        errors.push('Experience years must be a positive number');
    }
    
    if (data.maxInterns < 1) {
        errors.push('Maximum interns must be at least 1');
    }
    
    return errors;
}

// Add new mentor
async function addMentor() {
    console.log('addMentor called');
    
    // Get form data
    const userId = document.getElementById('mentorUserId').value.trim();
    const name = document.getElementById('mentorName').value.trim();
    const employeeId = document.getElementById('mentorEmployeeId').value.trim();
    const department = document.getElementById('mentorDepartment').value.trim();
    const specialization = document.getElementById('mentorSpecialization').value.trim();
    const experienceYears = parseInt(document.getElementById('mentorExperience').value) || 0;
    const maxInterns = parseInt(document.getElementById('mentorMaxInterns').value) || 5;

    // Validate
    const data = { userId, name, employeeId, department, specialization, experienceYears, maxInterns };
    const errors = validateMentorData(data, false);
    
    if (errors.length > 0) {
        showToast(errors.join('\n'), 'warning', 5000);
        return;
    }

    const mentorData = {
        user: { userId: userId },
        name: name,
        employeeId: employeeId,
        department: department || null,
        specialization: specialization || null,
        experienceYears: experienceYears,
        maxInterns: maxInterns
    };

    try {
        console.log('Creating mentor:', mentorData);
        
        // Try different endpoints
        let result = null;
        
        try {
            result = await api.post('/mentors/create', mentorData);
        } catch (e) {
            console.log('Failed to create with /mentors/create, trying /mentors');
            result = await api.post('/mentors', mentorData);
        }
        
        console.log('Create response:', result);
        
        if (result && result.success !== false) {
            showToast('✅ Mentor added successfully!', 'success');
            // Close modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('addMentorModal'));
            if (modal) modal.hide();
            // Reset form
            document.getElementById('addMentorForm').reset();
            document.getElementById('mentorMaxInterns').value = 5;
            // Reload mentors
            loadMentors(currentPage);
        } else {
            const errorMsg = result?.message || 'Unknown error occurred';
            showToast('❌ Failed to add mentor: ' + errorMsg, 'error');
        }
    } catch (error) {
        console.error('Add mentor error:', error);
        showToast('❌ Failed to add mentor: ' + error.message, 'error');
    }
}

// Open edit modal with mentor data
async function openEditModal(mentorId) {
    console.log('openEditModal for mentorId:', mentorId);
    
    if (!mentorId) {
        showToast('Invalid mentor ID', 'error');
        return;
    }
    
    try {
        // Find mentor in allMentors
        const mentor = allMentors.find(m => m.mentorId === mentorId);
        if (mentor) {
            console.log('Mentor found in cache');
            fillEditForm(mentor);
            const modal = new bootstrap.Modal(document.getElementById('editMentorModal'));
            modal.show();
        } else {
            // Fallback to API call
            console.log('Mentor not in cache, fetching from API');
            let result = null;
            
            try {
                result = await api.get(`/mentors/${mentorId}`);
            } catch (e) {
                console.log('Failed to fetch from /mentors/${mentorId}, trying /mentors/get/${mentorId}');
                result = await api.get(`/mentors/get/${mentorId}`);
            }
            
            console.log('API response:', result);
            
            // Extract mentor data
            let mentorData = null;
            if (result && result.success && result.data) {
                mentorData = result.data;
            } else if (result && result.data) {
                mentorData = result.data;
            } else if (result && typeof result === 'object' && result.mentorId) {
                mentorData = result;
            }
            
            if (mentorData) {
                fillEditForm(mentorData);
                const modal = new bootstrap.Modal(document.getElementById('editMentorModal'));
                modal.show();
            } else {
                showToast('❌ Failed to load mentor details', 'error');
            }
        }
    } catch (error) {
        console.error('Load mentor error:', error);
        showToast('❌ Failed to load mentor details: ' + error.message, 'error');
    }
}

// Fill edit form with mentor data
function fillEditForm(mentor) {
    document.getElementById('editMentorId').value = mentor.mentorId || '';
    document.getElementById('editMentorUserId').value = mentor.user?.userId || '';
    document.getElementById('editMentorName').value = mentor.name || '';
    document.getElementById('editMentorEmployeeId').value = mentor.employeeId || '';
    document.getElementById('editMentorDepartment').value = mentor.department || '';
    document.getElementById('editMentorSpecialization').value = mentor.specialization || '';
    document.getElementById('editMentorExperience').value = mentor.experienceYears || 0;
    document.getElementById('editMentorMaxInterns').value = mentor.maxInterns || 5;
}

// Update mentor
async function updateMentor() {
    console.log('updateMentor called');
    
    const mentorId = document.getElementById('editMentorId').value;
    const userId = document.getElementById('editMentorUserId').value.trim();
    const name = document.getElementById('editMentorName').value.trim();
    const employeeId = document.getElementById('editMentorEmployeeId').value.trim();
    const department = document.getElementById('editMentorDepartment').value.trim();
    const specialization = document.getElementById('editMentorSpecialization').value.trim();
    const experienceYears = parseInt(document.getElementById('editMentorExperience').value) || 0;
    const maxInterns = parseInt(document.getElementById('editMentorMaxInterns').value) || 5;

    // Validate
    const data = { userId, name, employeeId, department, specialization, experienceYears, maxInterns };
    const errors = validateMentorData(data, true);
    
    if (errors.length > 0) {
        showToast(errors.join('\n'), 'warning', 5000);
        return;
    }

    const mentorData = {
        user: { userId: userId },
        name: name,
        employeeId: employeeId,
        department: department || null,
        specialization: specialization || null,
        experienceYears: experienceYears,
        maxInterns: maxInterns
    };

    try {
        console.log('Updating mentor:', mentorData);
        const result = await api.put(`/mentors/${mentorId}`, mentorData);
        console.log('Update response:', result);
        
        if (result && result.success !== false) {
            showToast('✅ Mentor updated successfully!', 'success');
            const modal = bootstrap.Modal.getInstance(document.getElementById('editMentorModal'));
            if (modal) modal.hide();
            loadMentors(currentPage);
        } else {
            const errorMsg = result?.message || 'Unknown error occurred';
            showToast('❌ Failed to update mentor: ' + errorMsg, 'error');
        }
    } catch (error) {
        console.error('Update mentor error:', error);
        showToast('❌ Failed to update mentor: ' + error.message, 'error');
    }
}

// Delete mentor
async function deleteMentor(mentorId) {
    console.log('deleteMentor for mentorId:', mentorId);
    
    if (!mentorId) {
        showToast('Invalid mentor ID', 'error');
        return;
    }
    
    // Get name for confirmation message
    const row = document.querySelector(`.mentor-row[data-mentor-id="${mentorId}"]`);
    const name = row ? row.querySelector('strong')?.textContent || 'this mentor' : 'this mentor';
    
    if (!confirm(`Are you sure you want to delete mentor "${name}"? This action cannot be undone.`)) {
        return;
    }
    
    try {
        let result = null;
        
        try {
            result = await api.delete(`/mentors/${mentorId}`);
        } catch (e) {
            console.log('Failed to delete with /mentors/${mentorId}, trying /mentors/delete/${mentorId}');
            result = await api.delete(`/mentors/delete/${mentorId}`);
        }
        
        console.log('Delete response:', result);
        
        if (result && result.success !== false) {
            showToast(`✅ Mentor "${name}" deleted successfully!`, 'success');
            loadMentors(currentPage);
        } else {
            const errorMsg = result?.message || 'Unknown error occurred';
            showToast(`❌ Failed to delete mentor: ${errorMsg}`, 'error');
        }
    } catch (error) {
        console.error('Delete mentor error:', error);
        showToast(`❌ Failed to delete mentor: ${error.message}`, 'error');
    }
}

// Logout function
function logout() {
    console.log('logout called');
    if (confirm('Are you sure you want to logout?')) {
        if (api && api.logout) {
            api.logout();
        }
        window.location.href = '/pages/login.html';
    }
}

console.log('Mentor Management script loaded successfully');