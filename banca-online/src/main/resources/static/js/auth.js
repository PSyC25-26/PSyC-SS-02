const Auth = {
    TOKEN_KEY: 'banca_jwt',
    ROL_KEY: 'banca_rol',
    CLIENTE_ID_KEY: 'banca_clienteId',
    EMAIL_KEY: 'banca_email',

    guardarSesion(loginResponse) {
        localStorage.setItem(this.TOKEN_KEY, loginResponse.token);
        localStorage.setItem(this.ROL_KEY, loginResponse.rol);
        localStorage.setItem(this.CLIENTE_ID_KEY, loginResponse.clienteId ?? '');
        localStorage.setItem(this.EMAIL_KEY, loginResponse.email);
    },

    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    },

    getRol() {
        return localStorage.getItem(this.ROL_KEY);
    },

    getClienteId() {
        const id = localStorage.getItem(this.CLIENTE_ID_KEY);
        return id ? parseInt(id) : null;
    },

    getEmail() {
        return localStorage.getItem(this.EMAIL_KEY);
    },

    estaAutenticado() {
        return !!this.getToken();
    },

    // Headers listos para usar en cualquier fetch
    getHeaders() {
        return {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.getToken()}`
        };
    },

    cerrarSesion() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.ROL_KEY);
        localStorage.removeItem(this.CLIENTE_ID_KEY);
        localStorage.removeItem(this.EMAIL_KEY);
    }
};

// Wrapper de fetch que añade el token automáticamente y maneja el 401
async function fetchConAuth(url, opciones = {}) {
    const response = await fetch(url, {
        ...opciones,
        headers: {
            ...Auth.getHeaders(),
            ...(opciones.headers || {})
        }
    });

    if (response.status === 401) {
        Auth.cerrarSesion();
        location.reload();
        return;
    }

    return response;
}

window.Auth = Auth;
window.fetchConAuth = fetchConAuth;
