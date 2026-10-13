/* src/main/resources/static/js/js-users.js */

// Global variables
let currentPage = 0;
let totalPages = 0;
let totalElements = 0;
let currentSearchTerm = '';
let isLoading = false;
let sortField = 'createdAt';
let sortDirection = 'desc';
let pageSize = 10;
let allUsers = [];

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('User Management initialized');
    
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
    
    // Load users
    loadUsers();
    
    // Setup event listeners
    setupEventListeners();
});

// Setup event listeners
function setupEventListeners() {
    // Search with debounce
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(function(e) {
            searchUsers(e.target.value);
        }, 300));
    }

    // Enter key for search
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchUsers(this.value);
            }
        });
    }

    // Clear search button
    const clearSearchBtn = document.getElementById('clearSearch');
    if (clearSearchBtn) {
        clearSearchBtn.addEventListener('click', clearSearch);
    }

    // Add user form submission
    const addForm = document.getElementById('addUserForm');
    if (addForm) {
        addForm.addEventListener('submit', function(e) {
            e.preventDefault();
            addUser();
        });
    }

    // Edit user form submission
    const editForm = document.getElementById('editUserForm');
    if (editForm) {
        editForm.addEventListener('submit', function(e) {
            e.preventDefault();
            updateUser();
        });
    }

    // Keyboard shortcuts
    document.addEventListener('keydown', function(e) {
        // Ctrl+Shift+N to open add user modal
        if (e.ctrlKey && e.shiftKey && e.key === 'N') {
            e.preventDefault();
            const modal = new bootstrap.Modal(document.getElementById('addUserModal'));
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
            loadUsers(currentPage);
        }
    });

    // Pagination buttons
    const prevBtn = document.getElementById('prevPage');
    const nextBtn = document.getElementById('nextPage');
    
    if (prevBtn) {
        prevBtn.addEventListener('click', function() {
            if (currentPage > 0) {
                loadUsers(currentPage - 1);
            }
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener('click', function() {
            if (currentPage < totalPages - 1) {
                loadUsers(currentPage + 1);
            }
        });
    }

    // Page size change
    const pageSizeSelect = document.getElementById('pageSize');
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener('change', function() {
            pageSize = parseInt(this.value);
            currentPage = 0;
            renderCurrentPage();
        });
    }
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
    const tbody = document.getElementById('usersTable');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center text-danger py-4">
                    <i class="fas fa-exclamation-circle fa-2x d-block mb-3"></i>
                    <h5>${message}</h5>
                    <button class="btn btn-outline-primary btn-sm mt-2" onclick="loadUsers(${currentPage})">
                        <i class="fas fa-redo me-1"></i> Retry
                    </button>
                </td>
            </tr>
        `;
    }
}

// Show loading spinner
function showLoading() {
    isLoading = true;
    const tbody = document.getElementById('usersTable');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center py-5">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                    <p class="mt-2 text-muted">Loading users...</p>
                </td>
            </tr>
        `;
    }
    const prevBtn = document.getElementById('prevPage');
    const nextBtn = document.getElementById('nextPage');
    if (prevBtn) prevBtn.disabled = true;
    if (nextBtn) nextBtn.disabled = true;
}

// Load users - FIXED for direct array response
async function loadUsers(page = 0) {
    console.log('loadUsers called with page:', page);
    
    if (isLoading) {
        console.log('Already loading, skipping');
        return;
    }
    
    currentPage = page;
    showLoading();
    
    try {
        // Check if api object exists
        if (typeof api === 'undefined') {
            throw new Error('API object not found. Please check if api.js is loaded.');
        }
        
        console.log('Fetching users from /users');
        const response = await api.get('/users');
        console.log('API Response:', response);
        
        // Handle different response formats
        let users = [];
        
        // Case 1: Response is the direct array (your case)
        if (Array.isArray(response)) {
            users = response;
            console.log('Direct array response, users count:', users.length);
        }
        // Case 2: Response has success flag and data property
        else if (response && response.success && response.data) {
            if (Array.isArray(response.data)) {
                users = response.data;
                console.log('Success.data array response, users count:', users.length);
            } else if (response.data.content && Array.isArray(response.data.content)) {
                users = response.data.content;
                console.log('Paginated response, users count:', users.length);
            }
        }
        // Case 3: Response has data property directly
        else if (response && response.data && Array.isArray(response.data)) {
            users = response.data;
            console.log('Data property response, users count:', users.length);
        }
        // Case 4: Response has content property (Spring paginated)
        else if (response && response.content && Array.isArray(response.content)) {
            users = response.content;
            console.log('Content property response, users count:', users.length);
        }
        // Case 5: Try to find any array property
        else if (response && typeof response === 'object') {
            for (const key in response) {
                if (Array.isArray(response[key]) && response[key].length > 0) {
                    users = response[key];
                    console.log(`Found users in property: ${key}, count: ${users.length}`);
                    break;
                }
            }
        }
        
        // If no users found, throw error
        if (!users || users.length === 0) {
            console.warn('No users found in response');
            users = [];
        }
        
        console.log('Final users array:', users);
        
        // Store all users
        allUsers = users;
        console.log('Total users stored:', allUsers.length);
        
        if (allUsers.length === 0) {
            showToast('No users found in the system', 'info');
        }
        
        // Apply search filter if needed
        let filteredUsers = allUsers;
        if (currentSearchTerm) {
            const searchLower = currentSearchTerm.toLowerCase();
            filteredUsers = allUsers.filter(user => 
                (user.username && user.username.toLowerCase().includes(searchLower)) ||
                (user.email && user.email.toLowerCase().includes(searchLower)) ||
                (user.firstName && user.firstName.toLowerCase().includes(searchLower)) ||
                (user.lastName && user.lastName.toLowerCase().includes(searchLower)) ||
                (user.role && user.role.toLowerCase().includes(searchLower))
            );
            console.log('Filtered users count:', filteredUsers.length);
        }
        
        // Apply sorting
        filteredUsers = sortUsersData(filteredUsers);
        
        // Update total elements
        totalElements = filteredUsers.length;
        totalPages = Math.max(1, Math.ceil(totalElements / pageSize));
        
        // Get current page data
        const start = currentPage * pageSize;
        const end = Math.min(start + pageSize, totalElements);
        const pageUsers = filteredUsers.slice(start, end);
        
        // Render table
        renderUsersTable(pageUsers);
        
        // Update UI
        updateUI(start, end);
        
        console.log('Users loaded successfully');
        
    } catch (error) {
        console.error('Failed to load users:', error);
        console.error('Error stack:', error.stack);
        
        // Show error message
        showToast('Failed to load users: ' + error.message, 'error', 5000);
        showError('Failed to load users: ' + error.message);
        
    } finally {
        isLoading = false;
    }
}

// Update UI elements
function updateUI(start, end) {
    const userCount = document.getElementById('userCount');
    const paginationInfo = document.getElementById('paginationInfo');
    const pageInfo = document.getElementById('pageInfo');
    const prevBtn = document.getElementById('prevPage');
    const nextBtn = document.getElementById('nextPage');
    
    if (userCount) userCount.textContent = totalElements;
    if (paginationInfo) {
        paginationInfo.textContent = totalElements > 0 ? 
            `Showing ${start + 1}-${end} of ${totalElements} users` : 
            'No users found';
    }
    if (pageInfo) {
        pageInfo.textContent = totalPages > 0 ? 
            `Page ${currentPage + 1} of ${totalPages}` : 
            'Page 1 of 1';
    }
    if (prevBtn) prevBtn.disabled = currentPage === 0 || totalPages <= 1;
    if (nextBtn) nextBtn.disabled = currentPage >= totalPages - 1 || totalPages <= 1;
}

// Sort users data
function sortUsersData(users) {
    if (!users || users.length === 0) return users;
    
    return [...users].sort((a, b) => {
        let valA = a[sortField] || '';
        let valB = b[sortField] || '';
        
        // Handle different data types
        if (typeof valA === 'string') {
            valA = valA.toLowerCase();
            valB = valB.toLowerCase();
        }
        
        if (valA < valB) return sortDirection === 'asc' ? -1 : 1;
        if (valA > valB) return sortDirection === 'asc' ? 1 : -1;
        return 0;
    });
}

// Render current page (for after sort or filter changes)
function renderCurrentPage() {
    console.log('renderCurrentPage called');
    
    // Apply search filter
    let filteredUsers = allUsers;
    if (currentSearchTerm) {
        const searchLower = currentSearchTerm.toLowerCase();
        filteredUsers = allUsers.filter(user => 
            (user.username && user.username.toLowerCase().includes(searchLower)) ||
            (user.email && user.email.toLowerCase().includes(searchLower)) ||
            (user.firstName && user.firstName.toLowerCase().includes(searchLower)) ||
            (user.lastName && user.lastName.toLowerCase().includes(searchLower)) ||
            (user.role && user.role.toLowerCase().includes(searchLower))
        );
    }
    
    // Apply sorting
    filteredUsers = sortUsersData(filteredUsers);
    
    // Update total elements
    totalElements = filteredUsers.length;
    totalPages = Math.max(1, Math.ceil(totalElements / pageSize));
    
    // Get current page data
    const start = currentPage * pageSize;
    const end = Math.min(start + pageSize, totalElements);
    const pageUsers = filteredUsers.slice(start, end);
    
    // Render table
    renderUsersTable(pageUsers);
    
    // Update UI
    updateUI(start, end);
}

// Render users table
function renderUsersTable(users) {
    const tbody = document.getElementById('usersTable');
    
    if (!tbody) {
        console.error('usersTable element not found');
        return;
    }
    
    if (!users || users.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center text-muted py-5">
                    <i class="fas fa-inbox fa-2x d-block mb-3 text-muted"></i>
                    No users found
                    ${currentSearchTerm ? `<p class="mt-2">No results for "<strong>${escapeHtml(currentSearchTerm)}</strong>"</p>` : ''}
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = users.map(user => `
        <tr class="user-row" data-user-id="${user.userId || ''}">
            <td>
                <div class="d-flex align-items-center">
                    <div class="avatar-sm me-2" style="width: 40px; height: 40px; border-radius: 50%; background: ${getAvatarColor(user.username)}; color: white; display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 16px; flex-shrink: 0;">
                        ${user.username ? user.username.charAt(0).toUpperCase() : 'U'}
                    </div>
                    <div>
                        <strong>${escapeHtml(user.username || '')}</strong>
                    </div>
                </div>
            </td>
            <td>${escapeHtml(user.email || '')}</td>
            <td>${escapeHtml(user.firstName || '')} ${escapeHtml(user.lastName || '')}</td>
            <td>
                <span class="badge bg-${user.role === 'ADMIN' ? 'danger' : user.role === 'MENTOR' ? 'warning text-dark' : 'info'}">
                    ${user.role || 'INTERN'}
                </span>
            </td>
            <td>
                <span class="badge-status ${user.isActive ? 'active' : 'inactive'}">
                    ${user.isActive ? 'Active' : 'Inactive'}
                </span>
            </td>
            <td>${user.createdAt ? formatDate(user.createdAt) : '-'}</td>
            <td>
                <div class="btn-group btn-group-sm" role="group">
                    <button class="btn btn-outline-primary" onclick="openEditModal('${user.userId || ''}')" title="Edit User">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-outline-${user.isActive ? 'warning' : 'success'}" 
                            onclick="toggleUserStatus('${user.userId || ''}', ${!user.isActive})" 
                            title="${user.isActive ? 'Deactivate' : 'Activate'} User">
                        <i class="fas fa-${user.isActive ? 'pause' : 'play'}"></i>
                    </button>
                    <button class="btn btn-outline-danger" onclick="deleteUser('${user.userId || ''}')" title="Delete User">
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

// Format date
function formatDate(dateString) {
    if (!dateString) return '-';
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { 
            year: 'numeric', 
            month: 'short', 
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch(e) {
        return dateString;
    }
}

// Get avatar color based on username
function getAvatarColor(username) {
    if (!username) return '#6c757d';
    
    const colors = [
        '#4f46e5', '#7c3aed', '#2563eb', '#0891b2', '#059669',
        '#d97706', '#dc2626', '#7c3aed', '#4f46e5', '#2563eb',
        '#db2777', '#9333ea', '#0d9488', '#059669', '#d97706'
    ];
    
    let hash = 0;
    for (let i = 0; i < username.length; i++) {
        hash = username.charCodeAt(i) + ((hash << 5) - hash);
    }
    return colors[Math.abs(hash) % colors.length];
}

// Search users with validation
function searchUsers(searchTerm) {
    const trimmedTerm = searchTerm.trim();
    console.log('Searching for:', trimmedTerm);
    
    // Only search if term has at least 2 characters or is empty
    if (trimmedTerm && trimmedTerm.length < 2) {
        console.log('Search term too short, ignoring');
        return;
    }
    
    currentSearchTerm = trimmedTerm;
    currentPage = 0;
    renderCurrentPage();
}

// Clear search
function clearSearch() {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.value = '';
        currentSearchTerm = '';
        currentPage = 0;
        renderCurrentPage();
        console.log('Search cleared');
    }
}

// Validate user data
function validateUserData(data, isEdit = false) {
    const errors = [];
    
    if (!data.username || data.username.length < 3) {
        errors.push('Username must be at least 3 characters');
    }
    
    if (!data.email || !isValidEmail(data.email)) {
        errors.push('Please enter a valid email address');
    }
    
    if (!data.firstName || data.firstName.length < 1) {
        errors.push('First name is required');
    }
    
    if (!data.lastName || data.lastName.length < 1) {
        errors.push('Last name is required');
    }
    
    if (!isEdit && (!data.password || data.password.length < 6)) {
        errors.push('Password must be at least 6 characters');
    }
    
    if (data.password && data.password.length > 0 && data.password.length < 6) {
        errors.push('Password must be at least 6 characters');
    }
    
    return errors;
}

// Validate email format
function isValidEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

// Add new user
async function addUser() {
    console.log('addUser called');
    
    // Get form data
    const username = document.getElementById('newUsername').value.trim();
    const email = document.getElementById('newEmail').value.trim();
    const firstName = document.getElementById('newFirstName').value.trim();
    const lastName = document.getElementById('newLastName').value.trim();
    const password = document.getElementById('newPassword').value;
    const role = document.getElementById('newRole').value;
    const phone = document.getElementById('newPhone').value.trim();

    // Validate
    const userData = { username, email, firstName, lastName, password, role, phone };
    const errors = validateUserData(userData, false);
    
    if (errors.length > 0) {
        showToast(errors.join('\n'), 'warning', 5000);
        return;
    }

    const data = {
        username: username,
        email: email,
        password: password,
        firstName: firstName,
        lastName: lastName,
        role: role,
        phoneNumber: phone || null
    };

    try {
        console.log('Creating user:', data);
        const result = await api.post('/users', data);
        console.log('Create response:', result);
        
        if (result && result.success !== false) {
            showToast('✅ User added successfully!', 'success');
            // Close modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('addUserModal'));
            if (modal) modal.hide();
            // Reset form
            document.getElementById('addUserForm').reset();
            // Reload users
            loadUsers(currentPage);
        } else {
            const errorMsg = result?.message || 'Unknown error occurred';
            showToast('❌ Failed to add user: ' + errorMsg, 'error');
        }
    } catch (error) {
        console.error('Add user error:', error);
        showToast('❌ Failed to add user: ' + error.message, 'error');
    }
}

// Open edit modal with user data
async function openEditModal(userId) {
    console.log('openEditModal for userId:', userId);
    
    if (!userId) {
        showToast('Invalid user ID', 'error');
        return;
    }
    
    try {
        // Find user in allUsers
        const user = allUsers.find(u => u.userId === userId);
        if (user) {
            console.log('User found in cache');
            fillEditForm(user);
            const modal = new bootstrap.Modal(document.getElementById('editUserModal'));
            modal.show();
        } else {
            // Fallback to API call
            console.log('User not in cache, fetching from API');
            const result = await api.get(`/users/${userId}`);
            console.log('API response:', result);
            
            // Handle different response formats
            let userData = null;
            if (Array.isArray(result) && result.length > 0) {
                userData = result[0];
            } else if (result && result.success && result.data) {
                userData = result.data;
            } else if (result && result.data) {
                userData = result.data;
            } else if (result && typeof result === 'object' && result.userId) {
                userData = result;
            }
            
            if (userData) {
                fillEditForm(userData);
                const modal = new bootstrap.Modal(document.getElementById('editUserModal'));
                modal.show();
            } else {
                showToast('❌ Failed to load user details', 'error');
            }
        }
    } catch (error) {
        console.error('Load user error:', error);
        showToast('❌ Failed to load user details: ' + error.message, 'error');
    }
}

// Fill edit form with user data
function fillEditForm(user) {
    document.getElementById('editUserId').value = user.userId || '';
    document.getElementById('editUsername').value = user.username || '';
    document.getElementById('editEmail').value = user.email || '';
    document.getElementById('editFirstName').value = user.firstName || '';
    document.getElementById('editLastName').value = user.lastName || '';
    document.getElementById('editPhone').value = user.phoneNumber || '';
    document.getElementById('editRole').value = user.role || 'INTERN';
    document.getElementById('editStatus').value = user.isActive ? 'true' : 'false';
    document.getElementById('editPassword').value = '';
    const helpText = document.getElementById('editPasswordHelp');
    if (helpText) helpText.style.display = 'block';
}

// Update user
async function updateUser() {
    console.log('updateUser called');
    
    const userId = document.getElementById('editUserId').value;
    const username = document.getElementById('editUsername').value.trim();
    const email = document.getElementById('editEmail').value.trim();
    const firstName = document.getElementById('editFirstName').value.trim();
    const lastName = document.getElementById('editLastName').value.trim();
    const phone = document.getElementById('editPhone').value.trim();
    const role = document.getElementById('editRole').value;
    const isActive = document.getElementById('editStatus').value === 'true';
    const password = document.getElementById('editPassword').value;

    // Validate
    const userData = { username, email, firstName, lastName, password, role };
    const errors = validateUserData(userData, true);
    
    if (errors.length > 0) {
        showToast(errors.join('\n'), 'warning', 5000);
        return;
    }

    const data = {
        username: username,
        email: email,
        firstName: firstName,
        lastName: lastName,
        phoneNumber: phone || null,
        role: role,
        isActive: isActive
    };

    if (password && password.length >= 6) {
        data.password = password;
    }

    try {
        console.log('Updating user:', data);
        const result = await api.put(`/users/${userId}`, data);
        console.log('Update response:', result);
        
        if (result && result.success !== false) {
            showToast('✅ User updated successfully!', 'success');
            const modal = bootstrap.Modal.getInstance(document.getElementById('editUserModal'));
            if (modal) modal.hide();
            loadUsers(currentPage);
        } else {
            const errorMsg = result?.message || 'Unknown error occurred';
            showToast('❌ Failed to update user: ' + errorMsg, 'error');
        }
    } catch (error) {
        console.error('Update user error:', error);
        showToast('❌ Failed to update user: ' + error.message, 'error');
    }
}

// Toggle user status (activate/deactivate) - FIXED for correct backend endpoint
async function toggleUserStatus(userId, activate) {
    console.log('toggleUserStatus:', userId, 'activate:', activate);
    
    if (!userId) {
        showToast('Invalid user ID', 'error');
        return;
    }
    
    const actionDisplay = activate ? 'activate' : 'deactivate';
    
    if (!confirm(`Are you sure you want to ${actionDisplay} this user?`)) {
        return;
    }
    
    try {
        // CORRECT ENDPOINT: PATCH /users/{userId}/status?status=true/false
        const endpoint = `/users/${userId}/status?status=${activate}`;
        console.log('Calling endpoint:', endpoint);
        
        const result = await api.patch(endpoint);
        console.log('Toggle status response:', result);
        
        // Check if update was successful
        // The backend returns the updated User object with HTTP 200 OK
        if (result) {
            // Even if result doesn't have a success flag, if we got a response it worked
            showToast(`✅ User ${actionDisplay}d successfully!`, 'success');
            loadUsers(currentPage);
        } else {
            throw new Error('No response received from server');
        }
        
    } catch (error) {
        console.error('Toggle user status error:', error);
        showToast(`❌ Failed to ${actionDisplay} user: ${error.message}`, 'error');
    }
}

// Delete user
async function deleteUser(userId) {
    console.log('deleteUser for userId:', userId);
    
    if (!userId) {
        showToast('Invalid user ID', 'error');
        return;
    }
    
    // Get username for confirmation message
    const row = document.querySelector(`.user-row[data-user-id="${userId}"]`);
    const username = row ? row.querySelector('strong')?.textContent || 'this user' : 'this user';
    
    if (!confirm(`Are you sure you want to delete user "${username}"? This action cannot be undone.`)) {
        return;
    }
    
    try {
        const result = await api.delete(`/users/${userId}`);
        console.log('Delete response:', result);
        
        if (result && result.success !== false) {
            showToast(`✅ User "${username}" deleted successfully!`, 'success');
            loadUsers(currentPage);
        } else {
            const errorMsg = result?.message || 'Unknown error occurred';
            showToast(`❌ Failed to delete user: ${errorMsg}`, 'error');
        }
    } catch (error) {
        console.error('Delete user error:', error);
        showToast(`❌ Failed to delete user: ${error.message}`, 'error');
    }
}

// Export users
async function exportUsers() {
    console.log('exportUsers called');
    
    try {
        showToast('📊 Exporting users...', 'info', 2000);
        
        const usersToExport = allUsers;
        
        if (!usersToExport || usersToExport.length === 0) {
            showToast('No users to export', 'warning');
            return;
        }
        
        // Create CSV
        const headers = ['User ID', 'Username', 'Email', 'First Name', 'Last Name', 'Phone', 'Role', 'Status', 'Created At'];
        const rows = usersToExport.map(user => [
            user.userId || '',
            user.username || '',
            user.email || '',
            user.firstName || '',
            user.lastName || '',
            user.phoneNumber || '',
            user.role || 'INTERN',
            user.isActive ? 'Active' : 'Inactive',
            user.createdAt ? new Date(user.createdAt).toLocaleDateString() : ''
        ]);
        
        const csvContent = [headers, ...rows].map(row => row.join(',')).join('\n');
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `users_export_${new Date().toISOString().split('T')[0]}.csv`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
        
        showToast(`✅ Exported ${usersToExport.length} users successfully!`, 'success');
    } catch (error) {
        console.error('Export error:', error);
        showToast('❌ Failed to export users: ' + error.message, 'error');
    }
}

// Sort users (click on column headers)
function sortUsers(field) {
    console.log('sortUsers:', field);
    
    if (sortField === field) {
        sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
        sortField = field;
        sortDirection = 'asc';
    }
    
    // Update sort indicators
    document.querySelectorAll('.sortable').forEach(el => {
        const icon = el.querySelector('i');
        if (el.dataset.sort === field) {
            el.classList.add('active');
            if (icon) {
                icon.className = `fas fa-sort-${sortDirection === 'asc' ? 'up' : 'down'}`;
            }
        } else {
            el.classList.remove('active');
            if (icon) {
                icon.className = 'fas fa-sort';
            }
        }
    });
    
    renderCurrentPage();
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

console.log('User Management script loaded successfully');