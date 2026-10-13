/* src/main/resources/static/js/auth.js */

document.addEventListener('DOMContentLoaded', function() {
    // Check if already authenticated
    if (api.isAuthenticated()) {
        const user = api.getUser();
        if (user) {
            redirectToDashboard(user);
            return;
        }
    }

    // Login form
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Register form
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }
});

function redirectToDashboard(user) {
    const role = user.role;
    if (role === 'ADMIN') {
        window.location.href = '/pages/admin/dashboard.html';
    } else if (role === 'MENTOR') {
        window.location.href = '/pages/mentor/dashboard.html';
    } else {
        window.location.href = '/pages/intern/dashboard.html';
    }
}

async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();
    const btn = document.getElementById('loginBtn');
    const text = document.getElementById('loginText');
    const spinner = document.getElementById('loginSpinner');
    const error = document.getElementById('loginError');

    if (!username || !password) {
        error.textContent = 'Please enter both username and password';
        error.classList.remove('d-none');
        return;
    }

    btn.disabled = true;
    text.textContent = 'Signing in...';
    spinner.classList.remove('d-none');
    error.classList.add('d-none');

    try {
        const result = await api.login(username, password);
        if (result.success) {
            showToast('success', 'Login successful! Redirecting...');
            const user = api.getUser();
            setTimeout(() => redirectToDashboard(user), 500);
        } else {
            error.textContent = result.message || 'Login failed';
            error.classList.remove('d-none');
        }
    } catch (err) {
        error.textContent = err.message || 'Login failed. Please try again.';
        error.classList.remove('d-none');
    } finally {
        btn.disabled = false;
        text.textContent = 'Sign In';
        spinner.classList.add('d-none');
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (password !== confirmPassword) {
        document.getElementById('registerError').textContent = 'Passwords do not match';
        document.getElementById('registerError').classList.remove('d-none');
        return;
    }

    if (password.length < 6) {
        document.getElementById('registerError').textContent = 'Password must be at least 6 characters';
        document.getElementById('registerError').classList.remove('d-none');
        return;
    }

    const btn = document.getElementById('registerBtn');
    const text = document.getElementById('registerText');
    const spinner = document.getElementById('registerSpinner');
    const error = document.getElementById('registerError');

    btn.disabled = true;
    text.textContent = 'Creating...';
    spinner.classList.remove('d-none');
    error.classList.add('d-none');

    try {
        const result = await api.post('/auth/register', {
            username: document.getElementById('username').value.trim(),
            email: document.getElementById('email').value.trim(),
            password: password,
            firstName: document.getElementById('firstName').value.trim(),
            lastName: document.getElementById('lastName').value.trim(),
            phoneNumber: document.getElementById('phoneNumber')?.value || null,
            role: 'INTERN'
        });
        if (result.success) {
            showToast('success', 'Registration successful! Please login.');
            setTimeout(() => {
                window.location.href = '/pages/login.html';
            }, 1500);
        } else {
            error.textContent = result.message || 'Registration failed';
            error.classList.remove('d-none');
        }
    } catch (err) {
        error.textContent = err.message || 'Registration failed. Please try again.';
        error.classList.remove('d-none');
    } finally {
        btn.disabled = false;
        text.textContent = 'Create Account';
        spinner.classList.add('d-none');
    }
}

// Toast notification system
function showToast(type, message) {
    // Create container if it doesn't exist
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container';
        document.body.appendChild(container);
        
        // Add styles if not present
        if (!document.getElementById('toastStyles')) {
            const style = document.createElement('style');
            style.id = 'toastStyles';
            style.textContent = `
                .toast-container {
                    position: fixed;
                    top: 20px;
                    right: 20px;
                    z-index: 9999;
                    display: flex;
                    flex-direction: column;
                    gap: 10px;
                    max-width: 400px;
                }
                .toast {
                    padding: 12px 20px;
                    border-radius: 8px;
                    color: white;
                    font-weight: 500;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
                    animation: slideIn 0.3s ease;
                }
                .toast-success { background: #22c55e; }
                .toast-error { background: #ef4444; }
                .toast-warning { background: #f59e0b; }
                .toast-info { background: #3b82f6; }
                @keyframes slideIn {
                    from { transform: translateX(100%); opacity: 0; }
                    to { transform: translateX(0); opacity: 1; }
                }
                @keyframes fadeOut {
                    from { opacity: 1; transform: translateX(0); }
                    to { opacity: 0; transform: translateX(50px); }
                }
                .fade-out {
                    animation: fadeOut 0.3s ease forwards;
                }
            `;
            document.head.appendChild(style);
        }
    }
    
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.classList.add('fade-out');
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }, 5000);
}