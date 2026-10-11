/**
 * NourishNet Admin Module & Impact Analytics
 * System KPIs, food waste tracking & circular diversion, user directory, and activity audits.
 */
const AdminModule = (() => {

  function init() {
    setupWasteForm();
  }

  function setupWasteForm() {
    const form = document.getElementById('wasteForm');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
      e.preventDefault();

      const user = App.getCurrentUser();
      if (!user) {
        App.showToast('Please sign in to record food waste.', 'error');
        return;
      }

      const foodName = document.getElementById('wasteFoodName').value.trim();
      const quantity = parseFloat(document.getElementById('wasteQuantity').value);
      const unit = document.getElementById('wasteUnit').value.trim();
      const reason = document.getElementById('wasteReason').value.trim();
      const disposalCategory = document.getElementById('wasteCategory').value;

      try {
        App.showToast('Logging food waste diversion record...', 'info');
        await API.recordWaste({
          foodName,
          quantity,
          unit,
          reason,
          disposalCategory
        });

        App.showToast('Food waste record logged for safe recovery!', 'success');
        form.reset();
        loadWasteData();
        loadAdminData();
      } catch (err) {
        App.showToast(`Error: ${err.message}`, 'error');
      }
    });
  }

  async function loadAdminData() {
    await Promise.all([
      loadSummary(),
      loadWasteData(),
      loadUsers()
    ]);
  }

  async function loadSummary() {
    try {
      const res = await API.getDashboardSummary();
      renderSummary(res.data);
    } catch (err) {
      console.warn('Dashboard summary:', err.message);
    }
  }

  function renderSummary(data) {
    if (!data) return;

    const elDonated = document.getElementById('kpiFoodDonated');
    const elRedistributed = document.getElementById('kpiFoodRedistributed');
    const elWaste = document.getElementById('kpiFoodWaste');
    const elDrivers = document.getElementById('kpiDrivers');
    const elBatches = document.getElementById('kpiAvailableBatches');

    if (elDonated) elDonated.textContent = data.totalFoodDonatedKg || 0;
    if (elRedistributed) elRedistributed.textContent = data.totalFoodRedistributedKg || 0;
    if (elWaste) elWaste.textContent = data.totalFoodWasteKg || 0;
    if (elDrivers) elDrivers.textContent = data.registeredVolunteers || 0;
    if (elBatches) elBatches.textContent = data.availableFoodBatches || 0;

    renderActivities(data.recentActivities || []);
  }

  function renderActivities(activities) {
    const container = document.getElementById('recentActivityFeed');
    if (!container) return;

    if (!activities.length) {
      container.innerHTML = '<div style="color:var(--text-muted);padding:1rem;">No activity events recorded yet.</div>';
      return;
    }

    container.innerHTML = '';
    activities.forEach(act => {
      const item = document.createElement('div');
      item.style.cssText = 'padding:0.75rem;border-left:3px solid var(--primary);margin-bottom:0.75rem;background:rgba(255,255,255,0.03);border-radius:0 8px 8px 0;';

      const timeStr = act.time ? new Date(act.time).toLocaleTimeString() : '';

      item.innerHTML = `
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:0.25rem;">
          <strong style="color:#ffffff;font-size:0.9rem;">${act.title}</strong>
          <span style="font-size:0.75rem;color:var(--text-muted);">${timeStr}</span>
        </div>
        <div style="font-size:0.8rem;color:var(--text-secondary);">${act.description}</div>
      `;
      container.appendChild(item);
    });
  }

  async function loadWasteData() {
    const container = document.getElementById('wasteRecordsList');
    if (!container) return;

    try {
      const [recordsRes, statsRes] = await Promise.all([
        API.getAllWasteRecords(),
        API.getWasteStats()
      ]);

      const records = recordsRes.data || [];
      const stats = statsRes.data || {};

      renderWasteStats(stats);
      renderWasteRecords(records);
    } catch (err) {
      container.innerHTML = `<div style="color:var(--accent-rose);padding:1rem;">Error: ${err.message}</div>`;
    }
  }

  function renderWasteStats(stats) {
    const elTotal = document.getElementById('wasteTotalKgStat');
    if (elTotal) elTotal.textContent = `${stats.totalKg || 0} kg`;

    const breakdownContainer = document.getElementById('wasteBreakdownBars');
    if (!breakdownContainer || !stats.categoryBreakdown) return;

    breakdownContainer.innerHTML = '';
    for (const [cat, kg] of Object.entries(stats.categoryBreakdown)) {
      const div = document.createElement('div');
      div.style.cssText = 'display:flex;justify-content:space-between;align-items:center;margin-bottom:0.4rem;font-size:0.85rem;';
      div.innerHTML = `
        <span style="color:var(--text-secondary);">${cat.replace('_', ' ')}</span>
        <strong style="color:#34d399;">${kg} kg</strong>
      `;
      breakdownContainer.appendChild(div);
    }
  }

  function renderWasteRecords(records) {
    const container = document.getElementById('wasteRecordsList');
    if (!container) return;

    if (!records.length) {
      container.innerHTML = '<div style="color:var(--text-muted);padding:1rem;">No food waste logged yet.</div>';
      return;
    }

    container.innerHTML = '';
    records.forEach(r => {
      const item = document.createElement('div');
      item.style.cssText = 'padding:1rem;background:rgba(255,255,255,0.03);border:1px solid var(--border-color);border-radius:var(--radius-md);margin-bottom:0.75rem;';

      item.innerHTML = `
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:0.4rem;">
          <h4 style="font-size:1rem;color:#ffffff;">${r.foodName}</h4>
          <span class="badge" style="background:rgba(245,158,11,0.15);color:#f59e0b;border:1px solid rgba(245,158,11,0.3);">
            ${r.disposalCategory}
          </span>
        </div>
        <div style="font-size:0.85rem;color:var(--text-secondary);margin-bottom:0.25rem;">
          Quantity Diverted: <strong style="color:#34d399;">${r.quantity} ${r.unit}</strong>
        </div>
        <div style="font-size:0.8rem;color:var(--text-muted);">
          Reason / Condition: ${r.reason}
        </div>
      `;
      container.appendChild(item);
    });
  }

  async function loadUsers() {
    const container = document.getElementById('adminUsersTableBody');
    if (!container) return;

    const user = App.getCurrentUser();
    if (!user || user.role !== 'ADMIN') {
      container.innerHTML = '<tr><td colspan="5" style="padding:1rem;text-align:center;color:var(--text-muted);">Sign in as Admin to inspect full user directory.</td></tr>';
      return;
    }

    try {
      const res = await API.getAdminUsers();
      const users = res.data || [];

      container.innerHTML = '';
      users.forEach(u => {
        const row = document.createElement('tr');
        row.style.borderBottom = '1px solid var(--border-color)';
        row.innerHTML = `
          <td style="padding:0.75rem;font-weight:600;color:#ffffff;">${u.name}</td>
          <td style="padding:0.75rem;color:var(--text-secondary);">${u.email}</td>
          <td style="padding:0.75rem;"><span class="badge badge-${u.role.toLowerCase()}">${u.role}</span></td>
          <td style="padding:0.75rem;color:var(--text-secondary);">${u.organization || '-'}</td>
          <td style="padding:0.75rem;color:var(--text-muted);font-size:0.8rem;">${u.address || '-'}</td>
        `;
        container.appendChild(row);
      });
    } catch (err) {
      console.warn('Admin user directory:', err.message);
    }
  }

  return {
    init,
    loadAdminData,
    renderSummary
  };
})();

document.addEventListener('DOMContentLoaded', () => {
  AdminModule.init();
});
