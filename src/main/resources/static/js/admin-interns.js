/* src/main/resources/static/js/admin-interns.js */

// Global variables
let allInterns = [];
let allUsers = [];
let allMentors = [];
let allBatches = [];
let currentPage = 0;
let totalPages = 0;
let totalElements = 0;
let pageSize = 10;
let currentSearchTerm = '';

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('Intern Management initialized');
    
    if (typeof api === 'undefined') {
        console.error('API object not found!');
        showError('API not loaded. Please refresh the page.');
        return;
    }
    
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

    const userNameElement = document.getElementById('userName');
    if (userNameElement) {
        userNameElement.textContent = user.username || 'Admin';
    }
    
    updateTime();
    setInterval(updateTime, 1000);
    
    loadDropdownData();
    loadInterns();
    setupEventListeners();
});

// Setup event listeners
function setupEventListeners() {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(function(e) {
            searchInterns(e.target.value);
        }, 300));
    }

    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchInterns(this.value);
            }
        });
    }

    const clearSearchBtn = document.getElementById('clearSearch');
    if (clearSearchBtn) {
        clearSearchBtn.addEventListener('click', clearSearch);
    }

    const addForm = document.getElementById('addInternForm');
    if (addForm) {
        addForm.addEventListener('submit', function(e) {
            e.preventDefault();
            addIntern();
        });
    }

    const editForm = document.getElementById('editInternForm');
    if (editForm) {
        editForm.addEventListener('submit', function(e) {
            e.preventDefault();
            updateIntern();
        });
    }

    const prevBtn = document.getElementById('prevPage');
    const nextBtn = document.getElementById('nextPage');
    
    if (prevBtn) {
        prevBtn.addEventListener('click', function() {
            if (currentPage > 0) {
                loadInterns(currentPage - 1);
            }
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener('click', function() {
            if (currentPage < totalPages - 1) {
                loadInterns(currentPage + 1);
            }
        });
    }

    const pageSizeSelect = document.getElementById('pageSize');
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener('change', function() {
            pageSize = parseInt(this.value);
            currentPage = 0;
            renderCurrentPage();
        });
    }

    document.addEventListener('keydown', function(e) {
        if (e.ctrlKey && e.shiftKey && e.key === 'N') {
            e.preventDefault();
            const modal = new bootstrap.Modal(document.getElementById('addInternModal'));
            modal.show();
        }
        if (e.key === 'Escape') {
            const modals = document.querySelectorAll('.modal.show');
            modals.forEach(modal => {
                const instance = bootstrap.Modal.getInstance(modal);
                if (instance) instance.hide();
            });
        }
        if (e.ctrlKey && e.key === 'r') {
            e.preventDefault();
            loadInterns(currentPage);
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
    const tbody = document.getElementById('internsTable');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="text-center text-danger py-4">
                    <i class="fas fa-exclamation-circle fa-2x d-block mb-3"></i>
                    <h5>${message}</h5>
                    <button class="btn btn-outline-primary btn-sm mt-2" onclick="loadInterns(${currentPage})">
                        <i class="fas fa-redo me-1"></i> Retry
                    </button>
                </td>
            </tr>
        `;
    }
}

// Show loading spinner
function showLoading() {
    const tbody = document.getElementById('internsTable');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="text-center py-5">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                    <p class="mt-2 text-muted">Loading interns...</p>
                </td>
            </tr>
        `;
    }
}

// Load dropdown data (Users, Mentors, Batches)
async function loadDropdownData() {
    console.log('Loading dropdown data...');
    
    try {
        // Load Users (only INTERN role)
        const usersResult = await api.get('/users');
        console.log('Users API Response:', usersResult);
        
        let users = [];
        if (Array.isArray(usersResult)) {
            users = usersResult;
        } else if (usersResult && usersResult.success && usersResult.data) {
            if (Array.isArray(usersResult.data)) {
                users = usersResult.data;
            }
        } else if (usersResult && usersResult.data && Array.isArray(usersResult.data)) {
            users = usersResult.data;
        }
        
        allUsers = users.filter(u => u.role === 'INTERN');
        console.log('Users loaded:', allUsers.length);
        
        // Load Mentors
        try {
            const mentorsResult = await api.get('/mentors/getAll');
            console.log('Mentors API Response:', mentorsResult);
            
            if (Array.isArray(mentorsResult)) {
                allMentors = mentorsResult;
            } else if (mentorsResult && mentorsResult.success && mentorsResult.data) {
                if (Array.isArray(mentorsResult.data)) {
                    allMentors = mentorsResult.data;
                }
            } else if (mentorsResult && mentorsResult.data && Array.isArray(mentorsResult.data)) {
                allMentors = mentorsResult.data;
            }
            console.log('Mentors loaded:', allMentors.length);
        } catch (e) {
            console.log('Failed to load mentors:', e);
            allMentors = [];
        }
        
        // Load Batches
        try {
            const batchesResult = await api.get('/batches');
            console.log('Batches API Response:', batchesResult);
            
            if (Array.isArray(batchesResult)) {
                allBatches = batchesResult;
            } else if (batchesResult && batchesResult.success && batchesResult.data) {
                if (Array.isArray(batchesResult.data)) {
                    allBatches = batchesResult.data;
                }
            } else if (batchesResult && batchesResult.data && Array.isArray(batchesResult.data)) {
                allBatches = batchesResult.data;
            }
            console.log('Batches loaded:', allBatches.length);
        } catch (e) {
            console.log('Failed to load batches:', e);
            allBatches = [];
        }
        
        populateUserDropdown();
        populateMentorDropdown();
        populateBatchDropdown();
        
    } catch (error) {
        console.error('Failed to load dropdown data:', error);
        showToast('Failed to load dropdown data: ' + error.message, 'error');
    }
}

// Populate User dropdown
function populateUserDropdown() {
    const userSelect = document.getElementById('internUserId');
    const editUserSelect = document.getElementById('editInternUserId');
    
    console.log('Populating user dropdown with:', allUsers.length, 'users');
    
    if (userSelect) {
        userSelect.innerHTML = '<option value="">Select User</option>';
        allUsers.forEach(user => {
            const option = document.createElement('option');
            option.value = user.userId;
            const displayName = `${user.username} (${user.firstName || ''} ${user.lastName || ''}) - ${user.email}`;
            option.textContent = displayName;
            userSelect.appendChild(option);
        });
    }
    
    if (editUserSelect) {
        editUserSelect.innerHTML = '<option value="">Select User</option>';
        allUsers.forEach(user => {
            const option = document.createElement('option');
            option.value = user.userId;
            const displayName = `${user.username} (${user.firstName || ''} ${user.lastName || ''}) - ${user.email}`;
            option.textContent = displayName;
            editUserSelect.appendChild(option);
        });
    }
}

// Populate Mentor dropdown
function populateMentorDropdown() {
    const mentorSelect = document.getElementById('internMentorId');
    const editMentorSelect = document.getElementById('editInternMentorId');
    
    if (mentorSelect) {
        mentorSelect.innerHTML = '<option value="">Select Mentor</option>';
        allMentors.forEach(mentor => {
            const option = document.createElement('option');
            option.value = mentor.mentorId;
            const mentorName = mentor.name || (mentor.user ? mentor.user.firstName + ' ' + mentor.user.lastName : 'Unknown');
            const displayName = `${mentorName} (${mentor.employeeId || 'No ID'})`;
            option.textContent = displayName;
            mentorSelect.appendChild(option);
        });
    }
    
    if (editMentorSelect) {
        editMentorSelect.innerHTML = '<option value="">Select Mentor</option>';
        allMentors.forEach(mentor => {
            const option = document.createElement('option');
            option.value = mentor.mentorId;
            const mentorName = mentor.name || (mentor.user ? mentor.user.firstName + ' ' + mentor.user.lastName : 'Unknown');
            const displayName = `${mentorName} (${mentor.employeeId || 'No ID'})`;
            option.textContent = displayName;
            editMentorSelect.appendChild(option);
        });
    }
}

// Populate Batch dropdown
function populateBatchDropdown() {
    const batchSelect = document.getElementById('internBatchId');
    const editBatchSelect = document.getElementById('editInternBatchId');
    
    if (batchSelect) {
        batchSelect.innerHTML = '<option value="">Select Batch</option>';
        allBatches.forEach(batch => {
            const option = document.createElement('option');
            option.value = batch.batchId;
            const displayName = `${batch.batchName || 'Unnamed'} (${batch.startDate || 'N/A'} - ${batch.endDate || 'N/A'})`;
            option.textContent = displayName;
            batchSelect.appendChild(option);
        });
    }
    
    if (editBatchSelect) {
        editBatchSelect.innerHTML = '<option value="">Select Batch</option>';
        allBatches.forEach(batch => {
            const option = document.createElement('option');
            option.value = batch.batchId;
            const displayName = `${batch.batchName || 'Unnamed'} (${batch.startDate || 'N/A'} - ${batch.endDate || 'N/A'})`;
            option.textContent = displayName;
            editBatchSelect.appendChild(option);
        });
    }
}

// Load interns
async function loadInterns(page = 0) {
    console.log('loadInterns called with page:', page);
    
    currentPage = page;
    showLoading();
    
    try {
        console.log('Fetching interns from /interns');
        const response = await api.get('/interns');
        console.log('API Response:', response);
        
        let interns = [];
        
        if (Array.isArray(response)) {
            interns = response;
        } else if (response && response.success && response.data) {
            if (Array.isArray(response.data)) {
                interns = response.data;
            } else if (response.data.content && Array.isArray(response.data.content)) {
                interns = response.data.content;
            }
        } else if (response && response.data && Array.isArray(response.data)) {
            interns = response.data;
        }
        
        allInterns = interns;
        console.log('Total interns loaded:', allInterns.length);
        
        if (allInterns.length === 0) {
            showToast('No interns found in the system', 'info');
            renderInternsTable([]);
            updatePagination(0, 0);
            return;
        }
        
        let filteredInterns = allInterns;
        if (currentSearchTerm) {
            const searchLower = currentSearchTerm.toLowerCase();
            filteredInterns = allInterns.filter(intern => 
                (intern.user?.firstName && intern.user.firstName.toLowerCase().includes(searchLower)) ||
                (intern.user?.lastName && intern.user.lastName.toLowerCase().includes(searchLower)) ||
                (intern.user?.email && intern.user.email.toLowerCase().includes(searchLower)) ||
                (intern.department && intern.department.toLowerCase().includes(searchLower)) ||
                (intern.batch?.batchName && intern.batch.batchName.toLowerCase().includes(searchLower)) ||
                (intern.mentor?.name && intern.mentor.name.toLowerCase().includes(searchLower))
            );
        }
        
        totalElements = filteredInterns.length;
        totalPages = Math.max(1, Math.ceil(totalElements / pageSize));
        
        const start = currentPage * pageSize;
        const end = Math.min(start + pageSize, totalElements);
        const pageInterns = filteredInterns.slice(start, end);
        
        renderInternsTable(pageInterns);
        updatePagination(start, end);
        
        const internCount = document.getElementById('internCount');
        if (internCount) internCount.textContent = totalElements;
        
        console.log('Interns loaded successfully');
        
    } catch (error) {
        console.error('Failed to load interns:', error);
        showToast('Failed to load interns: ' + error.message, 'error', 5000);
        showError('Failed to load interns: ' + error.message);
    }
}

// Update pagination UI
function updatePagination(start, end) {
    const paginationInfo = document.getElementById('paginationInfo');
    const pageInfo = document.getElementById('pageInfo');
    const prevBtn = document.getElementById('prevPage');
    const nextBtn = document.getElementById('nextPage');
    
    if (paginationInfo) {
        paginationInfo.textContent = totalElements > 0 ? 
            `Showing ${start + 1}-${end} of ${totalElements} interns` : 
            'No interns found';
    }
    if (pageInfo) {
        pageInfo.textContent = totalPages > 0 ? 
            `Page ${currentPage + 1} of ${totalPages}` : 
            'Page 1 of 1';
    }
    if (prevBtn) prevBtn.disabled = currentPage === 0 || totalPages <= 1;
    if (nextBtn) nextBtn.disabled = currentPage >= totalPages - 1 || totalPages <= 1;
}

// Render interns table
function renderInternsTable(interns) {
    const tbody = document.getElementById('internsTable');
    
    if (!tbody) {
        console.error('internsTable element not found');
        return;
    }
    
    if (!interns || interns.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="text-center text-muted py-5">
                    <i class="fas fa-inbox fa-2x d-block mb-3 text-muted"></i>
                    No interns found
                    ${currentSearchTerm ? `<p class="mt-2">No results for "<strong>${escapeHtml(currentSearchTerm)}</strong>"</p>` : ''}
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = interns.map(intern => `
        <tr class="intern-row" data-intern-id="${intern.internId || ''}">
            <td>
                <div class="d-flex align-items-center">
                    <div class="avatar-sm me-2" style="width: 40px; height: 40px; border-radius: 50%; background: ${getAvatarColor(intern.user?.firstName || 'I')}; color: white; display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 16px; flex-shrink: 0;">
                        ${intern.user?.firstName ? intern.user.firstName.charAt(0).toUpperCase() : 'I'}
                    </div>
                    <div>
                        <strong>${escapeHtml(intern.user?.firstName || '')} ${escapeHtml(intern.user?.lastName || '')}</strong>
                        <div class="small text-muted">${escapeHtml(intern.internId || '')}</div>
                    </div>
                </div>
            </td>
            <td>${escapeHtml(intern.user?.email || '-')}</td>
            <td>${escapeHtml(intern.department || '-')}</td>
            <td>${escapeHtml(intern.batch?.batchName || '-')}</td>
            <td>${escapeHtml(intern.mentor?.name || intern.mentor?.user?.firstName || '-')}</td>
            <td>
                <span class="badge-status ${intern.status === 'ACTIVE' ? 'active' : 'inactive'}">
                    ${intern.status || 'ACTIVE'}
                </span>
            </td>
            <td>${intern.averageRating ? intern.averageRating.toFixed(1) : '0.0'}</td>
            <td>
                <div class="btn-group btn-group-sm" role="group">
                    <button class="btn btn-outline-primary" onclick="openEditModal('${intern.internId || ''}')" title="Edit Intern">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-outline-danger" onclick="deleteIntern('${intern.internId || ''}')" title="Delete Intern">
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

// Search interns
function searchInterns(searchTerm) {
    const trimmedTerm = searchTerm.trim();
    
    if (trimmedTerm && trimmedTerm.length < 2) {
        return;
    }
    
    currentSearchTerm = trimmedTerm;
    currentPage = 0;
    loadInterns(0);
}

// Clear search
function clearSearch() {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.value = '';
        currentSearchTerm = '';
        currentPage = 0;
        loadInterns(0);
    }
}

// Validate intern data
function validateInternData(data, isEdit = false) {
    const errors = [];
    
    if (!data.userId) {
        errors.push('User is required');
    }
    
    if (data.graduationYear && (data.graduationYear < 1900 || data.graduationYear > 2100)) {
        errors.push('Please enter a valid graduation year (1900-2100)');
    }
    
    return errors;
}

// Add new intern - FIXED: Send only userId in user object
async function addIntern() {
    console.log('addIntern called');
    
    const userId = document.getElementById('internUserId').value;
    const department = document.getElementById('internDepartment').value.trim();
    const university = document.getElementById('internUniversity').value.trim();
    const degree = document.getElementById('internDegree').value.trim();
    const graduationYear = parseInt(document.getElementById('internGraduationYear').value) || null;
    const startDate = document.getElementById('internStartDate').value || null;
    const endDate = document.getElementById('internEndDate').value || null;
    const batchId = document.getElementById('internBatchId').value || null;
    const mentorId = document.getElementById('internMentorId').value || null;

    if (!userId) {
        showToast('Please select a user', 'warning', 5000);
        return;
    }

    try {
        // Build the intern object with ONLY userId in the user field
        const internData = {
            user: { userId: userId },  // Send only userId
            department: department || null,
            university: university || null,
            degree: degree || null,
            graduationYear: graduationYear,
            startDate: startDate,
            endDate: endDate
        };

        if (batchId) {
            internData.batch = { batchId: batchId };
        }
        if (mentorId) {
            internData.mentor = { mentorId: mentorId };
        }

        console.log('Creating intern with data:', internData);
        const result = await api.post('/interns', internData);
        console.log('Create response:', result);
        
        if (result && result.success === true) {
            showToast('✅ Intern added successfully!', 'success');
            const modal = bootstrap.Modal.getInstance(document.getElementById('addInternModal'));
            if (modal) modal.hide();
            document.getElementById('addInternForm').reset();
            loadInterns(currentPage);
            loadDropdownData();
        } else {
            const errorMsg = result?.message || 'Unknown error occurred';
            showToast('❌ Failed to add intern: ' + errorMsg, 'error');
        }
    } catch (error) {
        console.error('Add intern error:', error);
        showToast('❌ Failed to add intern: ' + error.message, 'error');
    }
}

// Open edit modal with intern data
async function openEditModal(internId) {
    console.log('openEditModal for internId:', internId);
    
    if (!internId) {
        showToast('Invalid intern ID', 'error');
        return;
    }
    
    try {
        const intern = allInterns.find(i => i.internId === internId);
        if (intern) {
            console.log('Intern found in cache');
            fillEditForm(intern);
            const modal = new bootstrap.Modal(document.getElementById('editInternModal'));
            modal.show();
        } else {
            console.log('Intern not in cache, fetching from API');
            const result = await api.get(`/interns/${internId}`);
            console.log('API response:', result);
            
            let internData = null;
            if (result && result.success && result.data) {
                internData = result.data;
            } else if (result && result.data) {
                internData = result.data;
            }
            
            if (internData) {
                fillEditForm(internData);
                const modal = new bootstrap.Modal(document.getElementById('editInternModal'));
                modal.show();
            } else {
                showToast('❌ Failed to load intern details', 'error');
            }
        }
    } catch (error) {
        console.error('Load intern error:', error);
        showToast('❌ Failed to load intern details: ' + error.message, 'error');
    }
}

// Fill edit form with intern data
function fillEditForm(intern) {
    document.getElementById('editInternId').value = intern.internId || '';
    document.getElementById('editInternUserId').value = intern.user?.userId || '';
    document.getElementById('editInternDepartment').value = intern.department || '';
    document.getElementById('editInternUniversity').value = intern.university || '';
    document.getElementById('editInternDegree').value = intern.degree || '';
    document.getElementById('editInternGraduationYear').value = intern.graduationYear || '';
    document.getElementById('editInternStartDate').value = intern.startDate || '';
    document.getElementById('editInternEndDate').value = intern.endDate || '';
    document.getElementById('editInternBatchId').value = intern.batch?.batchId || '';
    document.getElementById('editInternMentorId').value = intern.mentor?.mentorId || '';
    document.getElementById('editInternStatus').value = intern.status || 'ACTIVE';
}

// Update intern - FIXED: Send only userId in user object
async function updateIntern() {
    console.log('updateIntern called');
    
    const internId = document.getElementById('editInternId').value;
    const userId = document.getElementById('editInternUserId').value;
    const department = document.getElementById('editInternDepartment').value.trim();
    const university = document.getElementById('editInternUniversity').value.trim();
    const degree = document.getElementById('editInternDegree').value.trim();
    const graduationYear = parseInt(document.getElementById('editInternGraduationYear').value) || null;
    const startDate = document.getElementById('editInternStartDate').value || null;
    const endDate = document.getElementById('editInternEndDate').value || null;
    const batchId = document.getElementById('editInternBatchId').value || null;
    const mentorId = document.getElementById('editInternMentorId').value || null;
    const status = document.getElementById('editInternStatus').value;

    if (!userId) {
        showToast('Please select a user', 'warning', 5000);
        return;
    }

    try {
        // Build the intern object with ONLY userId in the user field
        const internData = {
            user: { userId: userId },  // Send only userId
            department: department || null,
            university: university || null,
            degree: degree || null,
            graduationYear: graduationYear,
            startDate: startDate,
            endDate: endDate,
            status: status
        };

        if (batchId) {
            internData.batch = { batchId: batchId };
        }
        if (mentorId) {
            internData.mentor = { mentorId: mentorId };
        }

        console.log('Updating intern with data:', internData);
        const result = await api.put(`/interns/${internId}`, internData);
        console.log('Update response:', result);
        
        if (result && result.success === true) {
            showToast('✅ Intern updated successfully!', 'success');
            const modal = bootstrap.Modal.getInstance(document.getElementById('editInternModal'));
            if (modal) modal.hide();
            loadInterns(currentPage);
        } else {
            const errorMsg = result?.message || 'Unknown error occurred';
            showToast('❌ Failed to update intern: ' + errorMsg, 'error');
        }
    } catch (error) {
        console.error('Update intern error:', error);
        showToast('❌ Failed to update intern: ' + error.message, 'error');
    }
}

// Delete intern
async function deleteIntern(internId) {
    console.log('deleteIntern for internId:', internId);
    
    if (!internId) {
        showToast('Invalid intern ID', 'error');
        return;
    }
    
    const row = document.querySelector(`.intern-row[data-intern-id="${internId}"]`);
    const name = row ? row.querySelector('strong')?.textContent || 'this intern' : 'this intern';
    
    if (!confirm(`Are you sure you want to delete intern "${name}"? This action cannot be undone.`)) {
        return;
    }
    
    try {
        const result = await api.delete(`/interns/${internId}`);
        console.log('Delete response:', result);
        
        if (result && result.success === true) {
            showToast(`✅ Intern "${name}" deleted successfully!`, 'success');
            loadInterns(currentPage);
        } else {
            const errorMsg = result?.message || 'Unknown error occurred';
            showToast(`❌ Failed to delete intern: ${errorMsg}`, 'error');
        }
    } catch (error) {
        console.error('Delete intern error:', error);
        showToast(`❌ Failed to delete intern: ${error.message}`, 'error');
    }
}

// Export interns to CSV
function exportInterns() {
    console.log('exportInterns called');
    
    try {
        const internsToExport = allInterns;
        
        if (!internsToExport || internsToExport.length === 0) {
            showToast('No interns to export', 'warning');
            return;
        }
        
        showToast(`📊 Exporting ${internsToExport.length} interns...`, 'info', 2000);
        
        const headers = ['Intern ID', 'Name', 'Email', 'Department', 'University', 'Degree', 'Graduation Year', 'Batch', 'Mentor', 'Status', 'Rating'];
        const rows = internsToExport.map(intern => [
            intern.internId || '',
            `${intern.user?.firstName || ''} ${intern.user?.lastName || ''}`.trim(),
            intern.user?.email || '',
            intern.department || '',
            intern.university || '',
            intern.degree || '',
            intern.graduationYear || '',
            intern.batch?.batchName || '',
            intern.mentor?.name || '',
            intern.status || 'ACTIVE',
            intern.averageRating || 0
        ]);
        
        const csvContent = [headers, ...rows].map(row => row.join(',')).join('\n');
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `interns_export_${new Date().toISOString().split('T')[0]}.csv`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
        
        showToast(`✅ Exported ${internsToExport.length} interns successfully!`, 'success');
    } catch (error) {
        console.error('Export error:', error);
        showToast('❌ Failed to export interns: ' + error.message, 'error');
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

console.log('Intern Management script loaded successfully');