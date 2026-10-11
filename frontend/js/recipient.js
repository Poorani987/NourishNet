/**
 * NourishNet Recipient Module
 * Marketplace of available surplus donations, atomic claims, 15-km policy check, and demand requests.
 */
const RecipientModule = (() => {
  let selectedBatch = null;

  function init() {
    setupFilters();
    setupClaimModal();
    setupDemandForm();
  }

  function setupFilters() {
    const categorySelect = document.getElementById('recipCategoryFilter');
    const vegCheckbox = document.getElementById('recipVegFilter');
    const searchInput = document.getElementById('recipSearchInput');

    if (categorySelect) categorySelect.addEventListener('change', loadDonations);
    if (vegCheckbox) vegCheckbox.addEventListener('change', loadDonations);
    if (searchInput) searchInput.addEventListener('input', debounce(loadDonations, 300));
  }

  function debounce(fn, delay) {
    let timer;
    return (...args) => {
      clearTimeout(timer);
      timer = setTimeout(() => fn(...args), delay);
    };
  }

  async function loadRecipientData() {
    await Promise.all([
      loadDonations(),
      loadDemands()
    ]);
  }

  async function loadDonations() {
    const container = document.getElementById('recipientMarketplace');
    if (!container) return;

    container.innerHTML = '<div style="color:var(--text-muted);padding:1rem;">Finding available surplus food...</div>';

    const category = document.getElementById('recipCategoryFilter')?.value || '';
    const veg = document.getElementById('recipVegFilter')?.checked;
    const search = document.getElementById('recipSearchInput')?.value || '';

    const params = { availableOnly: 'true' };
    if (category) params.category = category;
    if (veg) params.vegetarian = 'true';
    if (search) params.search = search;

    try {
      const res = await API.getDonations(params);
      const batches = res.data || [];
      renderMarketplace(batches);
    } catch (err) {
      container.innerHTML = `<div style="color:var(--accent-rose);padding:1rem;">Failed to load food donations: ${err.message}</div>`;
    }
  }

  function renderMarketplace(batches) {
    const container = document.getElementById('recipientMarketplace');
    if (!container) return;

    if (!batches.length) {
      container.innerHTML = '<div style="color:var(--text-secondary);padding:2rem;text-align:center;">No available surplus food currently matches your filters. Please check back shortly or publish a request on the Demand Board.</div>';
      return;
    }

    container.innerHTML = '';
    batches.forEach(b => {
      const card = document.createElement('div');
      card.className = 'item-card';

      const expiryDate = b.consumeBefore ? new Date(b.consumeBefore).toLocaleString() : 'N/A';

      card.innerHTML = `
        <div>
          <div class="item-badge-row">
            <span class="badge badge-available">● AVAILABLE</span>
            ${b.vegetarian ? '<span class="badge badge-veg">🌱 Vegetarian</span>' : ''}
          </div>
          <h4 class="item-title">${b.foodName}</h4>
          <div class="item-donor">🏢 Listed by: <strong>${b.donorName || 'Registered Donor'}</strong></div>

          <div class="item-meta-list">
            <div class="item-meta-row">
              <span class="item-meta-label">Category:</span>
              <span class="item-meta-val">${b.category}</span>
            </div>
            <div class="item-meta-row">
              <span class="item-meta-label">Available Quantity:</span>
              <span class="item-meta-val" style="color:#34d399;font-size:1.05rem;">${b.remainingQuantity} ${b.unit}</span>
            </div>
            <div class="item-meta-row">
              <span class="item-meta-label">Consume Before:</span>
              <span class="item-meta-val">${expiryDate}</span>
            </div>
            <div class="item-meta-row">
              <span class="item-meta-label">Pickup Location:</span>
              <span class="item-meta-val">${b.pickupAddress || 'Address on file'}</span>
            </div>
            <div class="item-meta-row">
              <span class="item-meta-label">Road Hub:</span>
              <span class="item-meta-val">${b.nearestNodeId || 'NODE-CENTRAL'}</span>
            </div>
          </div>
        </div>

        <div style="margin-top:1rem;">
          <button class="btn btn-primary" onclick="RecipientModule.openClaimModal('${b.id}')" style="width:100%;">
            Claim Food Donation
          </button>
        </div>
      `;

      container.appendChild(card);
    });
  }

  async function openClaimModal(batchId) {
    const user = App.getCurrentUser();
    if (!user || (user.role !== 'RECIPIENT' && user.role !== 'ADMIN')) {
      App.showToast('Please sign in as a RECIPIENT (Shelter/NGO) to claim food.', 'error');
      return;
    }

    try {
      const res = await API.getDonationById(batchId);
      selectedBatch = res.data;

      document.getElementById('claimBatchName').textContent = selectedBatch.foodName;
      document.getElementById('claimBatchDonor').textContent = selectedBatch.donorName;
      document.getElementById('claimMaxQty').textContent = `${selectedBatch.remainingQuantity} ${selectedBatch.unit}`;

      const qtyInput = document.getElementById('claimQuantity');
      qtyInput.max = selectedBatch.remainingQuantity;
      qtyInput.value = Math.min(25, selectedBatch.remainingQuantity);

      // Prepopulate recipient info
      document.getElementById('claimAddress').value = user.address || '24 Northern Ring Road';
      document.getElementById('claimLat').value = user.latitude || 13.1120;
      document.getElementById('claimLon').value = user.longitude || 80.2680;
      document.getElementById('claimNode').value = 'NODE-SHELTER-NORTH';

      updatePolicyPreview();

      const modal = document.getElementById('claimModal');
      if (modal) modal.classList.add('active');
    } catch (err) {
      App.showToast(`Error opening claim modal: ${err.message}`, 'error');
    }
  }

  function updatePolicyPreview() {
    if (!selectedBatch) return;

    const recipLat = parseFloat(document.getElementById('claimLat').value);
    const recipLon = parseFloat(document.getElementById('claimLon').value);
    const donorLat = selectedBatch.pickupLatitude || 13.0827;
    const donorLon = selectedBatch.pickupLongitude || 80.2707;

    const dist = calculateHaversine(donorLat, donorLon, recipLat, recipLon);
    const policyBox = document.getElementById('claimPolicyPreview');
    if (!policyBox) return;

    if (dist <= 15.0) {
      policyBox.style.background = 'rgba(16, 185, 129, 0.1)';
      policyBox.style.border = '1px solid rgba(16, 185, 129, 0.3)';
      policyBox.innerHTML = `<strong>✓ Distance: ${dist.toFixed(2)} km (Within 15 km limit)</strong><br><span style="font-size:0.8rem;color:var(--text-secondary)">LOCAL TRANSPORT ELIGIBLE: Delivery coordination is eligible for volunteer drivers.</span>`;
    } else {
      policyBox.style.background = 'rgba(245, 158, 11, 0.1)';
      policyBox.style.border = '1px solid rgba(245, 158, 11, 0.3)';
      policyBox.innerHTML = `<strong>⚠️ Distance: ${dist.toFixed(2)} km (Exceeds 15 km limit)</strong><br><span style="font-size:0.8rem;color:var(--text-secondary)">EXTENDED DISTANCE: Recipient is responsible for coordinating pickup transport.</span>`;
    }
  }

  function calculateHaversine(lat1, lon1, lat2, lon2) {
    if (!lat1 || !lon1 || !lat2 || !lon2) return 0;
    const R = 6371; // Earth radius in km
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
  }

  function setupClaimModal() {
    const closeBtn = document.getElementById('closeClaimModal');
    const modal = document.getElementById('claimModal');
    const form = document.getElementById('claimForm');

    if (closeBtn && modal) {
      closeBtn.addEventListener('click', () => modal.classList.remove('active'));
    }

    ['claimLat', 'claimLon'].forEach(id => {
      const el = document.getElementById(id);
      if (el) el.addEventListener('input', updatePolicyPreview);
    });

    if (form) {
      form.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!selectedBatch) return;

        const quantity = parseFloat(document.getElementById('claimQuantity').value);
        const deliveryAddress = document.getElementById('claimAddress').value.trim();
        const recipientLatitude = parseFloat(document.getElementById('claimLat').value);
        const recipientLongitude = parseFloat(document.getElementById('claimLon').value);
        const destinationNodeId = document.getElementById('claimNode').value;

        try {
          App.showToast('Processing atomic food allocation...', 'info');
          const res = await API.claimDonation({
            batchId: selectedBatch.id,
            quantity,
            deliveryAddress,
            recipientLatitude,
            recipientLongitude,
            destinationNodeId
          });

          modal.classList.remove('active');
          App.showToast(`Successfully claimed ${quantity} ${selectedBatch.unit} of "${selectedBatch.foodName}"! Delivery order created.`, 'success');

          // Refresh data
          loadRecipientData();
        } catch (err) {
          App.showToast(`Allocation failed: ${err.message}`, 'error');
        }
      });
    }
  }

  function setupDemandForm() {
    const form = document.getElementById('demandForm');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
      e.preventDefault();

      const user = App.getCurrentUser();
      if (!user || (user.role !== 'RECIPIENT' && user.role !== 'ADMIN')) {
        App.showToast('Please sign in as a RECIPIENT to post demands.', 'error');
        return;
      }

      const category = document.getElementById('demandCategory').value;
      const requiredQuantity = parseFloat(document.getElementById('demandQuantity').value);
      const unit = document.getElementById('demandUnit').value.trim();
      const location = document.getElementById('demandLocation').value.trim() || user.address || '24 Northern Ring Road';
      const urgency = document.getElementById('demandUrgency').value;
      const notes = document.getElementById('demandNotes').value.trim();

      try {
        App.showToast('Publishing food request to Demand Board...', 'info');
        await API.createDemand({
          category,
          requiredQuantity,
          unit,
          location,
          urgency,
          notes
        });

        App.showToast('Demand request published successfully!', 'success');
        form.reset();
        loadDemands();
      } catch (err) {
        App.showToast(`Error publishing demand: ${err.message}`, 'error');
      }
    });
  }

  async function loadDemands() {
    const container = document.getElementById('demandsBoard');
    if (!container) return;

    try {
      const res = await API.getAllDemands();
      const demands = res.data || [];
      renderDemandsBoard(demands);
    } catch (err) {
      container.innerHTML = `<div style="color:var(--accent-rose);padding:1rem;">Failed to load demands: ${err.message}</div>`;
    }
  }

  function renderDemandsBoard(demands) {
    const container = document.getElementById('demandsBoard');
    if (!container) return;

    if (!demands.length) {
      container.innerHTML = '<div style="color:var(--text-secondary);padding:1.5rem;text-align:center;">No open recipient demand requests on the board.</div>';
      return;
    }

    container.innerHTML = '';
    demands.forEach(d => {
      const card = document.createElement('div');
      card.className = 'item-card';

      const urgencyClass = d.urgency === 'CRITICAL' ? 'badge-urgency-critical' :
        d.urgency === 'HIGH' ? 'badge-urgency-high' : 'badge-urgency-med';

      card.innerHTML = `
        <div>
          <div class="item-badge-row">
            <span class="badge ${urgencyClass}">🔥 ${d.urgency} URGENCY</span>
            <span class="badge" style="background:rgba(255,255,255,0.08);">${d.status}</span>
          </div>
          <h4 class="item-title">${d.recipientName}</h4>
          <div class="item-donor">📍 ${d.location}</div>

          <div class="item-meta-list">
            <div class="item-meta-row">
              <span class="item-meta-label">Requested Food:</span>
              <span class="item-meta-val">${d.category}</span>
            </div>
            <div class="item-meta-row">
              <span class="item-meta-label">Quantity Needed:</span>
              <span class="item-meta-val" style="color:#f59e0b;font-weight:700;">${d.requiredQuantity} ${d.unit}</span>
            </div>
            ${d.notes ? `
            <div class="item-meta-row">
              <span class="item-meta-label">Notes:</span>
              <span class="item-meta-val">${d.notes}</span>
            </div>` : ''}
          </div>
        </div>

        ${d.status === 'OPEN' ? `
          <div style="margin-top:1rem;">
            <button class="btn btn-secondary btn-sm" onclick="RecipientModule.fulfillDemand('${d.id}')" style="width:100%;">
              Mark as Fulfilled
            </button>
          </div>
        ` : `
          <div style="margin-top:1rem;color:var(--text-muted);font-size:0.8rem;text-align:center;">
            ✓ Fulfilled
          </div>
        `}
      `;

      container.appendChild(card);
    });
  }

  async function fulfillDemand(id) {
    try {
      await API.fulfillDemand(id);
      App.showToast('Demand marked as fulfilled!', 'success');
      loadDemands();
    } catch (err) {
      App.showToast(`Error: ${err.message}`, 'error');
    }
  }

  return {
    init,
    loadRecipientData,
    openClaimModal,
    fulfillDemand
  };
})();

document.addEventListener('DOMContentLoaded', () => {
  RecipientModule.init();
});
