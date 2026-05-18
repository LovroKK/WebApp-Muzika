const API_BASE = 'http://localhost:8080/api/admin';

function getToken() {
    return localStorage.getItem('token');
}

export function requireAdmin() {
    if (localStorage.getItem('role') !== 'ADMIN') {
        window.location.href = '../admin-login.html';
    }
}

export function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    window.location.href = '../admin-login.html';
}

export async function apiFetch(path, options = {}) {
    const response = await fetch(API_BASE + path, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${getToken()}`,
            ...(options.headers || {})
        }
    });

    if (response.status === 401 || response.status === 403) {
        logout();
        throw new Error('Neovlašteni pristup');
    }

    if (!response.ok) {
        const text = await response.text();
        throw new Error(`Server greška ${response.status}: ${text.substring(0, 200)}`);
    }

    return response;
}
