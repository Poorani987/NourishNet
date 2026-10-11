/**
 * NourishNet Volunteer Driver Module
 * Vehicle management, delivery matching, 1-click acceptance, Dijkstra routing, and Handover OTP verification.
 */
const VolunteerModule = (() => {
  let myProfile = null;

  function init() {
    setupVehicleForm();
    setupAvailabilityToggle();
  }

  function setupVehicleForm() {
    const form = document.getElementById('vehicleForm');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
      e.preventDefault();

      const user = App.getCurrentUser();
      if (!user || (user.role !== 'VOLUNTEER' && user.role !== 'ADMIN')) {
        App.showToast('Please sign in as a VOLUNTEER to register vehicle.', 'error');
        return;
      }

      const vehicleType = document.getElementById('vehicleType').value;
      const licensePlate = document.getElementById('vehiclePlate').value.trim();
      const capacityKg = parseFloat(document.getElementById('vehicleCapacity').value);
      const vehicleDetails = document.getElementById('vehicleDetails').value.trim();
      const operatingArea = document.getElementById('vehicleArea').value.trim();

      try {
        App.showToast('Saving vehicle profile...', 'info');
        const res = await API.registerVehicle({
          vehicleType,
          licensePlate,
          capacityKg,
          vehicleDetails,
          operatingArea,
          coverageRadiusKm: 20.0
        });

        myProfile = res.data;
        App.showToast('Vehicle profile registered successfully!', 'success');
        renderProfile();
        loadDeliveries();
      } catch (err) {
        App.showToast(`Error saving vehicle: ${err.message}`, 'error');
      }
    });
  }

  function setupAvailabilityToggle() {
    const toggle = document.getElementById('driverAvailabilityToggle');
    if (!toggle) return;

    toggle.addEventListener('change', async () => {
      try {
        const res = await API.toggleAvailability(toggle.checked);
        myProfile = res.data;
        App.showToast(`Availability status updated: ${toggle.checked ? 'Available' : 'Busy'}`, 'info');
        renderProfile();
      } catch (err) {
        App.showToast(`Error: ${err.message}`, 'error');
        toggle.checked = !toggle.checked;
      }
    });
  }

  async function loadVolunteerData() {
    await loadProfile();
    await loadDeliveries();
  }

  async function loadProfile() {
    const user = App.getCurrentUser();
    if (!user || (user.role !== 'VOLUNTEER' && user.role !== 'ADMIN')) {
      return;
    }

    try {
      const res = await API.getMyVolunteerProfile();
      myProfile = res.data;
      renderProfile();
    } catch (err) {
      console.warn('Volunteer profile:', err.message);
    }
  }

  function renderProfile() {
    if (!myProfile) return;

    const toggle = document.getElementById('driverAvailabilityToggle');
    if (toggle) toggle.checked = myProfile.available;

    const summaryEl = document.getElementById('driverProfileSummary');
    if (!summaryEl) return;

    const veh = myProfile.vehicle || {};
    summaryEl.innerHTML = `
      <div style="display:flex;align-items:center;gap:1rem;flex-wrap:wrap;">
        <div><strong>Driver:</strong> ${myProfile.name}</div>
        <div><strong>Vehicle:</strong> ${veh.type || 'N/A'} (${veh.licensePlate || 'N/A'})</div>
        <div><strong>Capacity:</strong> <span style="color:#34d399;font-weight:700;">${veh.capacityKg || 0} kg</span></div>
        <div><strong>Completed Deliveries:</strong> ${myProfile.completedDeliveriesCount || 0}</div>
        <div><strong>Status:</strong> <span class="badge ${myProfile.available ? 'badge-available' : 'badge-expired'}">${myProfile.available ? 'AVAILABLE' : 'BUSY'}</span></div>
      </div>
    `;

    // Prepopulate form
    if (document.getElementById('vehicleType') && veh.type) document.getElementById('vehicleType').value = veh.type;
    if (document.getElementById('vehiclePlate') && veh.licensePlate) document.getElementById('vehiclePlate').value = veh.licensePlate;
    if (document.getElementById('vehicleCapacity') && veh.capacityKg) document.getElementById('vehicleCapacity').value = veh.capacityKg;
    if (document.getElementById('vehicleArea') && myProfile.operatingArea) document.getElementById('vehicleArea').value = myProfile.operatingArea;
  }

  async function loadDeliveries() {
    const pendingContainer = document.getElementById('pendingDeliveriesList');
    const activeContainer = document.getElementById('activeDeliveryContainer');
    if (!pendingContainer || !activeContainer) return;

    pendingContainer.innerHTML = '<div style="color:var(--text-muted);padding:1rem;">Checking delivery dispatches...</div>';

    try {
      const res = await API.getAllDeliveries();
      const allDeliveries = res.data || [];

      const user = App.getCurrentUser();
      const myActive = allDeliveries.find(d => 
        (d.status === 'ASSIGNED' || d.status === 'PICKED_UP') &&
        (myProfile && d.volunteerId === myProfile.id)
      );

      renderActiveDelivery(myActive);

      const pending = allDeliveries.filter(d => d.status === 'PENDING_ACCEPTANCE');
      renderPendingDeliveries(pending);
    } catch (err) {
      pendingContainer.innerHTML = `<div style="color:var(--accent-rose);padding:1rem;">Error loading deliveries: ${err.message}</div>`;
    }
  }

  function renderActiveDelivery(delivery) {
    const container = document.getElementById('activeDeliveryContainer');
    if (!container) return;

    if (!delivery) {
      container.innerHTML = `
        <div style="background:rgba(255,255,255,0.03);border:1px dashed var(--border-color);border-radius:var(--radius-md);padding:1.5rem;text-align:center;color:var(--text-muted);">
          No active delivery in progress. Select a pending delivery below to accept.
        </div>
      `;
      return;
    }

    const routeNodes = delivery.routeNodeIds || [];
    const routeText = routeNodes.length > 0 ? routeNodes.join(' ➔ ') : 'Direct route';

    container.innerHTML = `
      <div class="card" style="border-color:var(--border-accent);background:rgba(16,185,129,0.05);margin-bottom:1.5rem;">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:1rem;flex-wrap:wrap;gap:0.5rem;">
          <div>
            <span class="badge ${delivery.status === 'ASSIGNED' ? 'badge-partial' : 'badge-available'}">
              ● CURRENT ACTIVE MISSION (${delivery.status})
            </span>
            <h3 style="margin-top:0.4rem;">${delivery.foodName} (${delivery.quantity} ${delivery.unit})</h3>
          </div>
          <div style="font-size:0.9rem;color:var(--text-secondary);">
            Shortest Dijkstra Route: <strong style="color:#34d399;">${delivery.routeDistanceKm || 0} km</strong>
          </div>
        </div>

        <div style="display:grid;grid-template-columns:repeat(auto-fit, minmax(260px, 1fr));gap:1rem;margin-bottom:1.5rem;font-size:0.85rem;">
          <div style="background:rgba(0,0,0,0.25);padding:1rem;border-radius:var(--radius-md);">
            <div style="color:var(--text-muted);font-weight:600;">1. PICKUP (DONOR)</div>
            <div style="font-weight:700;color:#ffffff;margin-top:0.25rem;">${delivery.donorName}</div>
            <div style="color:var(--text-secondary);">${delivery.donorAddress}</div>
            <div style="margin-top:0.25rem;color:#60a5fa;">Hub: ${delivery.sourceNodeId || 'N/A'}</div>
          </div>
          <div style="background:rgba(0,0,0,0.25);padding:1rem;border-radius:var(--radius-md);">
            <div style="color:var(--text-muted);font-weight:600;">2. DROPOFF (RECIPIENT)</div>
            <div style="font-weight:700;color:#ffffff;margin-top:0.25rem;">${delivery.recipientName}</div>
            <div style="color:var(--text-secondary);">${delivery.recipientAddress}</div>
            <div style="margin-top:0.25rem;color:#60a5fa;">Hub: ${delivery.destinationNodeId || 'N/A'}</div>
          </div>
        </div>

        <div style="background:rgba(0,0,0,0.3);padding:1rem;border-radius:var(--radius-md);margin-bottom:1.5rem;">
          <div style="font-size:0.8rem;color:var(--text-muted);margin-bottom:0.25rem;">DIJKSTRA NAVIGATION PATH:</div>
          <div style="font-family:monospace;font-size:0.95rem;color:#34d399;font-weight:700;">${routeText}</div>
        </div>

        <!-- Workflow Step Actions -->
        ${delivery.status === 'ASSIGNED' ? `
          <div style="display:flex;gap:1rem;align-items:center;">
            <button class="btn btn-primary" onclick="VolunteerModule.markPickedUp('${delivery.id}')">
              📦 Confirm Food Picked Up from Donor
            </button>
            <span style="font-size:0.8rem;color:var(--text-muted);">
              Drive to donor address and confirm pickup to notify recipient.
            </span>
          </div>
        ` : `
          <!-- OTP Verification Section -->
          <div style="background:rgba(15,23,42,0.8);border:1px solid rgba(139,92,246,0.3);padding:1.25rem;border-radius:var(--radius-md);">
            <h4 style="margin-bottom:0.5rem;color:#c084fc;">🔐 Recipient Handover Verification</h4>
            <p style="font-size:0.85rem;color:var(--text-secondary);margin-bottom:1rem;">
              Food has been picked up! Ask the recipient for the 6-digit Handover Verification OTP displayed on their screen to complete delivery.
            </p>
            <div style="display:flex;gap:0.75rem;align-items:center;max-width:400px;">
              <input type="text" id="handoverOtpInput" placeholder="Enter 6-digit OTP" maxlength="6" style="letter-spacing:0.25em;font-size:1.1rem;text-align:center;font-weight:700;" />
              <button class="btn btn-accent" onclick="VolunteerModule.verifyOtp('${delivery.id}')">
                Verify & Complete
              </button>
            </div>
          </div>
        `}
      </div>
    `;
  }

  function renderPendingDeliveries(deliveries) {
    const container = document.getElementById('pendingDeliveriesList');
    if (!container) return;

    if (!deliveries.length) {
      container.innerHTML = '<div style="color:var(--text-secondary);padding:1.5rem;text-align:center;">No deliveries currently waiting for volunteer driver acceptance.</div>';
      return;
    }

    container.innerHTML = '';
    const myCap = myProfile?.vehicle?.capacityKg || 100.0;

    deliveries.forEach(d => {
      const card = document.createElement('div');
      card.className = 'item-card';

      const canCarry = myCap >= d.quantity;

      card.innerHTML = `
        <div>
          <div class="item-badge-row">
            <span class="badge badge-pending">WAITING DRIVER</span>
            <span class="badge" style="background:rgba(255,255,255,0.06);">${d.quantity} ${d.unit}</span>
          </div>
          <h4 class="item-title">${d.foodName}</h4>
          <div class="item-donor">From: <strong>${d.donorName}</strong> ➔ To: <strong>${d.recipientName}</strong></div>

          <div class="item-meta-list">
            <div class="item-meta-row">
              <span class="item-meta-label">Pickup:</span>
              <span class="item-meta-val">${d.donorAddress}</span>
            </div>
            <div class="item-meta-row">
              <span class="item-meta-label">Dropoff:</span>
              <span class="item-meta-val">${d.recipientAddress}</span>
            </div>
            <div class="item-meta-row">
              <span class="item-meta-label">Road Nodes:</span>
              <span class="item-meta-val">${d.sourceNodeId || 'CENTRAL'} ➔ ${d.destinationNodeId || 'NORTH'}</span>
            </div>
            <div class="item-meta-row">
              <span class="item-meta-label">Capacity Match:</span>
              <span class="item-meta-val" style="color:${canCarry ? '#34d399' : '#ef4444'};">
                ${canCarry ? `✓ Suitable (${d.quantity}kg ≤ ${myCap}kg)` : `✕ Too heavy (${d.quantity}kg > ${myCap}kg)`}
              </span>
            </div>
          </div>
        </div>

        <div style="margin-top:1rem;">
          <button class="btn ${canCarry ? 'btn-primary' : 'btn-secondary'}" 
                  onclick="VolunteerModule.acceptDelivery('${d.id}')"
                  ${!canCarry ? 'disabled' : ''}
                  style="width:100%;">
            ${canCarry ? '🚀 Accept Delivery (Run Dijkstra Route)' : 'Vehicle Capacity Insufficient'}
          </button>
        </div>
      `;

      container.appendChild(card);
    });
  }

  async function acceptDelivery(id) {
    const user = App.getCurrentUser();
    if (!user || (user.role !== 'VOLUNTEER' && user.role !== 'ADMIN')) {
      App.showToast('Please sign in as a VOLUNTEER to accept deliveries.', 'error');
      return;
    }

    try {
      App.showToast('Calculating Dijkstra shortest path & assigning mission...', 'info');
      const res = await API.acceptDelivery(id);
      App.showToast(`Delivery mission accepted! Route calculated: ${res.data.routeDistanceKm} km`, 'success');
      loadDeliveries();
    } catch (err) {
      App.showToast(`Cannot accept delivery: ${err.message}`, 'error');
    }
  }

  async function markPickedUp(id) {
    try {
      App.showToast('Updating delivery status to PICKED_UP...', 'info');
      await API.updateDeliveryStatus(id, 'PICKED_UP');
      App.showToast('Food marked as picked up! Proceed to recipient.', 'success');
      loadDeliveries();
    } catch (err) {
      App.showToast(`Status update failed: ${err.message}`, 'error');
    }
  }

  async function verifyOtp(id) {
    const input = document.getElementById('handoverOtpInput');
    const otp = input ? input.value.trim() : '';

    if (!otp || otp.length !== 6) {
      App.showToast('Please enter the valid 6-digit Handover OTP.', 'error');
      return;
    }

    try {
      App.showToast('Verifying handover OTP...', 'info');
      await API.verifyHandoverOtp(id, otp);
      App.showToast('🎉 Handover OTP verified! Delivery completed successfully.', 'success');
      loadVolunteerData();
    } catch (err) {
      App.showToast(`OTP verification failed: ${err.message}`, 'error');
    }
  }

  return {
    init,
    loadVolunteerData,
    acceptDelivery,
    markPickedUp,
    verifyOtp
  };
})();

document.addEventListener('DOMContentLoaded', () => {
  VolunteerModule.init();
});
