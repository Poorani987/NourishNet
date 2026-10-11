/**
 * NourishNet — Authentication & Session Manager
 * Handles login, registration, role verification, demo account quick switching,
 * and user UI states.
 */

const Auth = (() => {
  let currentUser = null;

  function init() {
    const savedUser = localStorage.getItem('nourishnet_user');
    if (savedUser) {
      try {
        currentUser = JSON.parse(savedUser);
      } catch (e) {
        currentUser = null;
      }
    }
    updateNavUI();
    renderDemoSwitcher();
  }

  function getUser() {
    return currentUser;
  }

  function isAuthenticated() {
    return !!API.getToken() && !!currentUser;
  }

  function hasRole(role) {
    if (!currentUser) return false;
    return currentUser.role === role || currentUser.role === 'ADMIN';
  }

  async function login(email, password) {
    try {
      const res = await API.login({ email, password });
      if (res.success && res.data) {
        const loginData = res.data;
        API.setToken(loginData.token);
        currentUser = {
          id: loginData.id,
          name: loginData.name,
          email: loginData.email,
          role: loginData.role,
          organization: loginData.organization,
          phone: loginData.phone,
          address: loginData.address
        };
        localStorage.setItem('nourishnet_user', JSON.stringify(currentUser));
        updateNavUI();
        showToast(`Welcome back, ${currentUser.name}!`, false);
        return currentUser;
      }
      throw new Error(res.message || 'Login failed');
    } catch (err) {
      showToast(err.message || 'Login failed', true);
      throw err;
    }
  }

  async function register(userData) {
    try {
      const res = await API.register(userData);
      if (res.success && res.data) {
        const regData = res.data;
        API.setToken(regData.token);
        currentUser = {
          id: regData.id,
          name: regData.name,
          email: regData.email,
          role: regData.role,
          organization: regData.organization,
          phone: regData.phone,
          address: regData.address
        };
        localStorage.setItem('nourishnet_user', JSON.stringify(currentUser));
        updateNavUI();
        showToast(`Registration successful! Welcome to NourishNet.`, false);
        return currentUser;
      }
      throw new Error(res.message || 'Registration failed');
    } catch (err) {
      showToast(err.message || 'Registration failed', true);
      throw err;
    }
  }

  function logout() {
    API.setToken(null);
    currentUser = null;
    localStorage.removeItem('nourishnet_user');
    updateNavUI();
    showToast('Signed out successfully.', false);
    setTimeout(() => {
      window.location.href = 'index.html';
    }, 400);
  }

  async function quickLoginAs(role) {
    const demoCredentials = {
      'ADMIN': { email: 'admin@nourishnet.org', password: 'admin123' },
      'DONOR': { email: 'greenbites@restaurant.com', password: 'donor123' },
      'RECIPIENT': { email: 'hope.shelter@ngo.org', password: 'recip123' },
      'VOLUNTEER': { email: 'alex.driver@volunteer.org', password: 'volun123' }
    };

    const creds = demoCredentials[role];
    if (!creds) return;

    try {
      const user = await login(creds.email, creds.password);
      redirectToDashboard(user.role);
    } catch (err) {
      console.error('Quick demo login error:', err);
    }
  }

  function redirectToDashboard(role) {
    switch (role) {
      case 'DONOR':
        window.location.href = 'donor-dashboard.html';
        break;
      case 'RECIPIENT':
        window.location.href = 'recipient-dashboard.html';
        break;
      case 'VOLUNTEER':
        window.location.href = 'volunteer-dashboard.html';
        break;
      case 'ADMIN':
        window.location.href = 'admin-dashboard.html';
        break;
      default:
        window.location.href = 'index.html';
    }
  }

  function updateNavUI() {
    const userContainer = document.getElementById('navUserControls');
    if (!userContainer) return;

    if (currentUser) {
      let dashUrl = 'donor-dashboard.html';
      if (currentUser.role === 'RECIPIENT') dashUrl = 'recipient-dashboard.html';
      if (currentUser.role === 'VOLUNTEER') dashUrl = 'volunteer-dashboard.html';
      if (currentUser.role === 'ADMIN') dashUrl = 'admin-dashboard.html';

      userContainer.innerHTML = `
        <div style="display:flex;align-items:center;gap:0.75rem;">
          <a href="${dashUrl}" class="btn btn-secondary btn-sm" style="display:flex;align-items:center;gap:0.4rem;">
            <span>👤</span>
            <span><strong>${currentUser.name.split(' ')[0]}</strong> (${currentUser.role})</span>
          </a>
          <button onclick="Auth.logout()" class="btn btn-outline btn-sm" title="Sign out">
            Sign Out
          </button>
        </div>
      `;
    } else {
      userContainer.innerHTML = `
        <div style="display:flex;align-items:center;gap:0.6rem;">
          <a href="login.html" class="btn btn-outline btn-sm">Sign In</a>
          <a href="register.html" class="btn btn-primary btn-sm">Get Started</a>
        </div>
      `;
    }
  }

  function renderDemoSwitcher() {
    const bar = document.getElementById('demoQuickSwitcher');
    if (!bar) return;

    bar.innerHTML = `
      <div class="demo-account-bar">
        <div class="demo-account-label">
          <span>⚡ Demo 1-Click Role Switcher:</span>
        </div>
        <div class="demo-btn-group">
          <button class="btn-demo-pill" onclick="Auth.quickLoginAs('ADMIN')">👑 Admin</button>
          <button class="btn-demo-pill" onclick="Auth.quickLoginAs('DONOR')">🍲 Donor (Green Bites)</button>
          <button class="btn-demo-pill" onclick="Auth.quickLoginAs('RECIPIENT')">🤝 Recipient (Hope Shelter)</button>
          <button class="btn-demo-pill" onclick="Auth.quickLoginAs('VOLUNTEER')">🚚 Volunteer (Alex Rivera)</button>
        </div>
      </div>
    `;
  }

  return {
    init,
    getUser,
    isAuthenticated,
    hasRole,
    login,
    register,
    logout,
    quickLoginAs,
    redirectToDashboard
  };
})();

// Global Toast utility
function showToast(message, isError = false) {
  let container = document.getElementById('toastContainer');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toastContainer';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast ${isError ? 'toast-error' : 'toast-success'}`;
  toast.innerHTML = `
    <span>${isError ? '⚠️' : '✅'}</span>
    <div style="flex:1;">${message}</div>
  `;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// Auto-run on DOM ready
document.addEventListener('DOMContentLoaded', Auth.init);
