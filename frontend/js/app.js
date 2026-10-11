/**
 * NourishNet Core Application Script
 * Manages authentication, demo accounts, role-based views, tabs, and notifications.
 */
const App = (() => {
  let currentUser = null;
  let demoAccounts = [];

  async function init() {
    setupTabNavigation();
    await loadDemoAccounts();
    await checkCurrentUser();
    checkBackendHealth();

    // Refresh metrics periodically
    setInterval(checkBackendHealth, 30000);
  }

  async function checkBackendHealth() {
    try {
      const summary = await API.getDashboardSummary();
      const statusBadge = document.getElementById('backendStatus');
      if (statusBadge) {
        statusBadge.innerHTML = '<span class="status-dot"></span> Spring Boot Online';
        statusBadge.classList.remove('offline');
      }
      if (window.AdminModule && typeof window.AdminModule.renderSummary === 'function') {
        window.AdminModule.renderSummary(summary.data);
      }
    } catch (err) {
      const statusBadge = document.getElementById('backendStatus');
      if (statusBadge) {
        statusBadge.innerHTML = '<span class="status-dot" style="background:#ef4444;box-shadow:0 0 8px #ef4444"></span> Backend Offline';
      }
    }
  }

  async function loadDemoAccounts() {
    try {
      const res = await API.getDemoAccounts();
      demoAccounts = res.data || [];
      renderDemoAccountButtons();
    } catch (err) {
      console.warn('Could not load demo accounts:', err);
    }
  }

  function renderDemoAccountButtons() {
    const container = document.getElementById('demoAccountsContainer');
    if (!container) return;

    container.innerHTML = '';
    demoAccounts.forEach(acc => {
      const btn = document.createElement('button');
      btn.className = `btn-demo-pill ${acc.role.toLowerCase()}`;
      btn.dataset.email = acc.email;
      btn.dataset.role = acc.role;
      btn.innerHTML = `<span>●</span> <strong>${acc.role}:</strong> ${acc.label.split('(')[0]}`;
      btn.addEventListener('click', () => switchDemoAccount(acc.email, acc.password));
      container.appendChild(btn);
    });
  }

  async function switchDemoAccount(email, password) {
    try {
      showToast(`Signing in as demo account...`, 'info');
      const res = await API.login({ email, password });
      API.setToken(res.data.token);
      currentUser = res.data.user || res.data;
      updateUserUI();
      showToast(`Logged in as ${currentUser.name} (${currentUser.role})`, 'success');
      
      // Auto-switch to corresponding tab
      switchTabForRole(currentUser.role);
      refreshCurrentTab();
    } catch (err) {
      showToast(`Sign in failed: ${err.message}`, 'error');
    }
  }

  async function checkCurrentUser() {
    try {
      if (API.getToken()) {
        const res = await API.getCurrentUser();
        currentUser = res.data;
        updateUserUI();
      } else {
        // Automatically sign in as Admin for instant interactive exploration
        const adminAcc = demoAccounts.find(a => a.role === 'ADMIN');
        if (adminAcc) {
          await switchDemoAccount(adminAcc.email, adminAcc.password);
        }
      }
    } catch (err) {
      console.warn('Session check:', err.message);
      API.setToken(null);
      currentUser = null;
      updateUserUI();
    }
  }

  function updateUserUI() {
    const nameEl = document.getElementById('currentUserName');
    const roleEl = document.getElementById('currentUserRole');
    const avatarEl = document.getElementById('currentUserAvatar');
    const pills = document.querySelectorAll('.btn-demo-pill');

    if (currentUser) {
      if (nameEl) nameEl.textContent = currentUser.name || currentUser.organization;
      if (roleEl) roleEl.textContent = currentUser.role;
      if (avatarEl) avatarEl.textContent = (currentUser.name || 'U').charAt(0).toUpperCase();

      pills.forEach(p => {
        if (p.dataset.email === currentUser.email) {
          p.classList.add('active');
        } else {
          p.classList.remove('active');
        }
      });
    } else {
      if (nameEl) nameEl.textContent = 'Guest';
      if (roleEl) roleEl.textContent = 'Demo Mode';
      if (avatarEl) avatarEl.textContent = '?';
      pills.forEach(p => p.classList.remove('active'));
    }
  }

  function setupTabNavigation() {
    const tabs = document.querySelectorAll('.tab-btn');
    tabs.forEach(tab => {
      tab.addEventListener('click', () => {
        const targetId = tab.dataset.target;
        setActiveTab(targetId);
      });
    });
  }

  function setActiveTab(tabId) {
    document.querySelectorAll('.tab-btn').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.target === tabId);
    });

    document.querySelectorAll('.tab-panel').forEach(panel => {
      panel.classList.toggle('active', panel.id === tabId);
    });

    // Trigger module loaders
    if (tabId === 'tab-donor' && window.DonorModule) {
      window.DonorModule.loadDonorData();
    } else if (tabId === 'tab-recipient' && window.RecipientModule) {
      window.RecipientModule.loadRecipientData();
    } else if (tabId === 'tab-volunteer' && window.VolunteerModule) {
      window.VolunteerModule.loadVolunteerData();
    } else if (tabId === 'tab-routing' && window.RoutingModule) {
      window.RoutingModule.initGraph();
    } else if (tabId === 'tab-admin' && window.AdminModule) {
      window.AdminModule.loadAdminData();
    }
  }

  function switchTabForRole(role) {
    switch (role) {
      case 'DONOR':
        setActiveTab('tab-donor');
        break;
      case 'RECIPIENT':
        setActiveTab('tab-recipient');
        break;
      case 'VOLUNTEER':
        setActiveTab('tab-volunteer');
        break;
      case 'ADMIN':
      default:
        setActiveTab('tab-admin');
        break;
    }
  }

  function refreshCurrentTab() {
    const activePanel = document.querySelector('.tab-panel.active');
    if (activePanel) {
      setActiveTab(activePanel.id);
    }
  }

  function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    const icon = type === 'success' ? '✓' : type === 'error' ? '✕' : 'ℹ';
    toast.innerHTML = `<span style="font-weight:bold">${icon}</span> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(10px)';
      toast.style.transition = 'all 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  }

  return {
    init,
    getCurrentUser: () => currentUser,
    showToast,
    refreshCurrentTab,
    setActiveTab
  };
})();

document.addEventListener('DOMContentLoaded', () => {
  App.init();
});
