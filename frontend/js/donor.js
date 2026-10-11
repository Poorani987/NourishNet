/**
 * NourishNet Donor Module
 * Surplus food donation registration and management of posted food batches.
 */
const DonorModule = (() => {

  function init() {
    setupForm();
    setupHelpers();
  }

  function setupForm() {
    const form = document.getElementById('donorForm');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
      e.preventDefault();

      const user = App.getCurrentUser();
      if (!user || (user.role !== 'DONOR' && user.role !== 'ADMIN')) {
        App.showToast('Please sign in as a DONOR to register food donations.', 'error');
        return;
      }

      const foodName = document.getElementById('donorFoodName').value.trim();
      const category = document.getElementById('donorCategory').value;
      const quantity = parseFloat(document.getElementById('donorQuantity').value);
      const unit = document.getElementById('donorUnit').value;
      const vegetarian = document.getElementById('donorVeg').checked;
      const perishable = document.getElementById('donorPerishable').checked;
      const hoursUntilExpiry = parseFloat(document.getElementById('donorHoursExpiry').value) || 6;
      const allergens = document.getElementById('donorAllergens').value.trim();
      const storageInstructions = document.getElementById('donorStorage').value.trim();
      const pickupAddress = document.getElementById('donorAddress').value.trim() || user.address || '42 Green Valley High Street';
      const pickupLatitude = parseFloat(document.getElementById('donorLat').value) || user.latitude || 13.0910;
      const pickupLongitude = parseFloat(document.getElementById('donorLon').value) || user.longitude || 80.2640;
      const nearestNodeId = document.getElementById('donorNearestNode').value || 'NODE-RESTAURANT-ROW';
      const pickupInstructions = document.getElementById('donorInstructions').value.trim();

      const now = new Date();
      const preparedAt = now.toISOString();
      const consumeBefore = new Date(now.getTime() + hoursUntilExpiry * 3600 * 1000).toISOString();

      const payload = {
        foodName,
        category,
        quantity,
        unit,
        vegetarian,
        perishable,
        preparedAt,
        consumeBefore,
        allergens,
        storageInstructions,
        pickupAddress,
        pickupLatitude,
        pickupLongitude,
        nearestNodeId,
        pickupInstructions
      };

      try {
        App.showToast('Registering surplus food batch...', 'info');
        await API.createDonation(payload);
        App.showToast(`Donation for "${foodName}" registered successfully!`, 'success');
        form.reset();
        loadDonorData();
      } catch (err) {
        App.showToast(`Failed to register donation: ${err.message}`, 'error');
      }
    });
  }

  function setupHelpers() {
    // Quick autofill button for testing
    const btnSample = document.getElementById('btnAutofillDonation');
    if (btnSample) {
      btnSample.addEventListener('click', () => {
        document.getElementById('donorFoodName').value = 'Fresh Vegetable Pulao & Raita (50 Servings)';
        document.getElementById('donorCategory').value = 'COOKED_MEALS';
        document.getElementById('donorQuantity').value = '25.0';
        document.getElementById('donorUnit').value = 'kg';
        document.getElementById('donorVeg').checked = true;
        document.getElementById('donorPerishable').checked = true;
        document.getElementById('donorHoursExpiry').value = '6';
        document.getElementById('donorAllergens').value = 'Dairy (Yogurt in Raita). Gluten-free.';
        document.getElementById('donorStorage').value = 'Store in warm thermal food transport container';
        document.getElementById('donorAddress').value = 'Green Bites Bistro, 42 Green Valley High Street';
        document.getElementById('donorLat').value = '13.0910';
        document.getElementById('donorLon').value = '80.2640';
        document.getElementById('donorNearestNode').value = 'NODE-RESTAURANT-ROW';
        document.getElementById('donorInstructions').value = 'Kitchen back service ramp, call Chef Vikram on arrival.';
      });
    }
  }

  async function loadDonorData() {
    const listContainer = document.getElementById('donorListings');
    if (!listContainer) return;

    listContainer.innerHTML = '<div style="color:var(--text-muted);padding:1rem;">Loading food listings...</div>';

    try {
      const user = App.getCurrentUser();
      let res;
      if (user && user.role === 'DONOR') {
        res = await API.getDonationsByDonor(user.id);
      } else {
        res = await API.getDonations();
      }

      const batches = res.data || [];
      renderDonorListings(batches);
    } catch (err) {
      listContainer.innerHTML = `<div style="color:var(--accent-rose);padding:1rem;">Error loading donations: ${err.message}</div>`;
    }
  }

  function renderDonorListings(batches) {
    const container = document.getElementById('donorListings');
    if (!container) return;

    if (!batches.length) {
      container.innerHTML = '<div style="color:var(--text-secondary);padding:1.5rem;text-align:center;">No food listings found for this donor account. Use the form above to register your first donation.</div>';
      return;
    }

    container.innerHTML = '';
    batches.forEach(b => {
      const card = document.createElement('div');
      card.className = 'item-card';

      const statusBadgeClass = b.status === 'AVAILABLE' ? 'badge-available' :
        b.status === 'PARTIALLY_ALLOCATED' ? 'badge-partial' :
        b.status === 'FULLY_ALLOCATED' ? 'badge-allocated' : 'badge-expired';

      const expiryDate = b.consumeBefore ? new Date(b.consumeBefore).toLocaleString() : 'N/A';
      const isExpired = b.consumeBefore && new Date(b.consumeBefore) < new Date();

      card.innerHTML = `
        <div>
          <div class="item-badge-row">
            <span class="badge ${statusBadgeClass}">${b.status}</span>
            ${b.vegetarian ? '<span class="badge badge-veg">🌱 Vegetarian</span>' : ''}
          </div>
          <h4 class="item-title">${b.foodName}</h4>
          <div class="item-donor">📍 ${b.pickupAddress || 'Address on file'}</div>

          <div class="item-meta-list">
            <div class="item-meta-row">
              <span class="item-meta-label">Category:</span>
              <span class="item-meta-val">${b.category}</span>
            </div>
            <div class="item-meta-row">
              <span class="item-meta-label">Remaining / Total:</span>
              <span class="item-meta-val" style="color:${b.remainingQuantity > 0 ? '#34d399' : '#94a3b8'}">${b.remainingQuantity} / ${b.originalQuantity} ${b.unit}</span>
            </div>
            <div class="item-meta-row">
              <span class="item-meta-label">Food Safety Deadline:</span>
              <span class="item-meta-val" style="${isExpired ? 'color:#ef4444' : ''}">${expiryDate}</span>
            </div>
            ${b.allergens ? `
            <div class="item-meta-row">
              <span class="item-meta-label">Allergens:</span>
              <span class="item-meta-val">${b.allergens}</span>
            </div>` : ''}
            <div class="item-meta-row">
              <span class="item-meta-label">Road Network Node:</span>
              <span class="item-meta-val">${b.nearestNodeId || 'NODE-CENTRAL'}</span>
            </div>
          </div>
        </div>

        <div style="display:flex;gap:0.5rem;margin-top:1rem;">
          ${(b.status === 'AVAILABLE' || b.status === 'PARTIALLY_ALLOCATED') ? `
            <button class="btn btn-secondary btn-sm" onclick="DonorModule.cancelDonation('${b.id}')" style="width:100%;color:#ef4444;">
              Cancel Listing
            </button>
          ` : `
            <span style="font-size:0.75rem;color:var(--text-muted);text-align:center;width:100%;">
              ${b.status === 'FULLY_ALLOCATED' ? '✓ Fully Claimed & Dispatched' : 'Listing Closed'}
            </span>
          `}
        </div>
      `;

      container.appendChild(card);
    });
  }

  async function cancelDonation(id) {
    if (!confirm('Are you sure you want to cancel this food donation?')) return;
    try {
      await API.cancelDonation(id);
      App.showToast('Donation listing cancelled.', 'info');
      loadDonorData();
    } catch (err) {
      App.showToast(`Error cancelling donation: ${err.message}`, 'error');
    }
  }

  return {
    init,
    loadDonorData,
    cancelDonation
  };
})();

document.addEventListener('DOMContentLoaded', () => {
  DonorModule.init();
});
