/* src/main/resources/static/js/api.js */
   const API_BASE = 'https://internship-management-ha24.onrender.com';

class ApiService {
    constructor() {
        this.token = localStorage.getItem('accessToken');
        this.user = JSON.parse(localStorage.getItem('user') || 'null');
    }

    getHeaders() {
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };
        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }
        return headers;
    }

    async request(method, endpoint, data = null) {
        const url = `${API_BASE}${endpoint}`;
        const options = {
            method,
            headers: this.getHeaders()
        };
        if (data) {
            options.body = JSON.stringify(data);
        }

        try {
            const response = await fetch(url, options);
            
            // Handle non-JSON responses
            let result;
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                result = await response.json();
            } else {
                const text = await response.text();
                result = { success: false, message: text || 'Request failed' };
            }

            if (response.status === 401) {
                localStorage.removeItem('accessToken');
                localStorage.removeItem('user');
                window.location.href = '/pages/login.html';
                throw new Error('Session expired');
            }
            if (!response.ok) {
                throw new Error(result.message || 'Request failed');
            }
            return result;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }

    get(endpoint) { return this.request('GET', endpoint); }
    post(endpoint, data) { return this.request('POST', endpoint, data); }
    put(endpoint, data) { return this.request('PUT', endpoint, data); }
    delete(endpoint) { return this.request('DELETE', endpoint); }
    patch(endpoint, data) { return this.request('PATCH', endpoint, data); }

    async login(username, password) {
        try {
            const result = await this.post('/auth/login', { username, password });
            if (result.success) {
                this.token = result.accessToken;
                this.user = {
                    userId: result.userId,
                    username: result.username,
                    email: result.email,
                    role: result.role
                };
                localStorage.setItem('accessToken', this.token);
                localStorage.setItem('user', JSON.stringify(this.user));
            }
            return result;
        } catch (error) {
            console.error('Login error:', error);
            throw error;
        }
    }

    logout() {
        this.token = null;
        this.user = null;
        localStorage.removeItem('accessToken');
        localStorage.removeItem('user');
        window.location.href = '/pages/login.html';
    }

    getToken() { return this.token; }
    getUser() { return this.user; }
    isAuthenticated() { return !!this.token; }
    hasRole(role) { return this.user && this.user.role === role; }
}

const api = new ApiService();