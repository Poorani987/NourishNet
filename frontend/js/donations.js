/**
 * NourishNet — Donations Discovery & Food Allocation Marketplace
 * Filter, search, and claim available food batches with food-safety deadline checks.
 */

const DonationsApp = (() => {
  let allBatches = [];
  let selectedBatch = null;

  async function init() {
    await loadDonations();
    setupEventListeners();
  }

  async function loadDonations() {
    const listContainer = document.getElementById('donationsList');
    if (!listContainer) return;

    listContainer.innerHTML = '<div style="text-align:center;padding:3rem;color:var(--text-muted);">🔄 Loading available food batches...</div>';

    try {
      const category = document.getElementById('categoryFilter')?.value || '';
      const vegetarian = document.getElementById('vegFilter')?.value || '';
      const search = document.getElementById('searchInput')?.value || '';

      const params = { availableOnly: true };
      if (category) params.category = category;
      if (vegetarian !== '') params.vegetarian = vegetarian === 'true';
      if (search) params.search = search;

      const res = await API.getDonations(params);
      allBatches = res.data || [];
      renderDonationCards(allBatches);
    } catch (err) {
      listContainer.innerHTML = `<div style="text-align:center;padding:3rem;color:#dc3545;">⚠️ Failed to load donations: ${err.message}</div>`;
    }
  }

  function renderDonationCards(batches) {
    const listContainer = document.getElementById('donationsList');
    if (!listContainer) return;

    if (batches.length === 0) {
      listContainer.innerHTML = `
        <div style="text-align:center;padding:3.5rem;grid-column:1/-1;background:#fff;border-radius:var(--radius-lg);border:1px solid var(--border-subtle);">
          <div style="font-size:3rem;margin-bottom:0.75rem;">🌱</div>
          <h3 style="color:var(--color-forest-dark);margin-bottom:0.5rem;">No matching food batches available</h3>
          <p style="color:var(--text-muted);max-width:460px;margin:0 auto 1.5rem;">All food donations may have been allocated, or no active batches match your filter criteria.</p>
          <button onclick="DonationsApp.resetFilters()" class="btn btn-secondary btn-sm">Reset Filters</button>
        </div>
      `;
      return;
    }

    const cardsHtml = batches.map(batch => {
      const deadline = new Date(batch.consumeBefore);
      const isExpiringSoon = (deadline - new Date()) < (4 * 3600 * 1000); // under 4 hours
      const formattedDeadline = deadline.toLocaleString('en-US', {
        month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
      });

      return `
        <div class="card donation-card" style="display:flex;flex-direction:column;justify-content:space-between;">
          <div>
            <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:0.85rem;">
              <span class="badge ${batch.remainingQuantity === batch.originalQuantity ? 'badge-available' : 'badge-partially-allocated'}">
                ${batch.remainingQuantity === batch.originalQuantity ? 'Available' : 'Partially Allocated'}
              </span>
              <span style="font-size:0.8rem;font-weight:700;color:${batch.vegetarian ? '#2e7d32' : '#c62828'};background:${batch.vegetarian ? '#e8f5e9' : '#ffebee'};padding:0.2rem 0.6rem;border-radius:var(--radius-pill);">
                ${batch.vegetarian ? '🟢 Vegetarian' : '🔴 Non-Veg'}
              </span>
            </div>

            <h3 style="font-size:1.25rem;color:var(--color-forest-dark);margin-bottom:0.35rem;">${batch.foodName}</h3>
            <p style="font-size:0.85rem;color:var(--text-muted);margin-bottom:1rem;">
              Listed by <strong>${batch.donorName}</strong>
            </p>

            <div style="background:var(--color-pastel-tint);border-radius:var(--radius-md);padding:0.85rem;margin-bottom:1rem;">
              <div style="display:flex;justify-content:space-between;margin-bottom:0.35rem;font-size:0.88rem;">
                <span style="color:var(--text-muted);">Available Quantity:</span>
                <strong style="color:var(--color-forest-dark);font-size:1.05rem;">${batch.remainingQuantity} ${batch.unit}</strong>
              </div>
              <div style="display:flex;justify-content:space-between;font-size:0.82rem;color:var(--text-muted);">
                <span>Original Batch:</span>
                <span>${batch.originalQuantity} ${batch.unit}</span>
              </div>
            </div>

            <div style="font-size:0.85rem;color:var(--text-body);margin-bottom:0.5rem;display:flex;align-items:flex-start;gap:0.4rem;">
              <span>📍</span>
              <span style="flex:1;"><strong>Pickup:</strong> ${batch.pickupAddress}</span>
            </div>

            <div style="font-size:0.85rem;margin-bottom:0.75rem;display:flex;align-items:center;gap:0.4rem;color:${isExpiringSoon ? '#c62828' : 'var(--text-muted)'};font-weight:${isExpiringSoon ? '700' : '500'};">
              <span>⏰</span>
              <span><strong>Consume Before:</strong> ${formattedDeadline}</span>
            </div>

            ${batch.allergenInfo ? `
              <div style="font-size:0.8rem;color:var(--text-muted);margin-bottom:0.75rem;background:#fff8e1;padding:0.4rem 0.65rem;border-radius:var(--radius-xs);">
                ⚠️ <strong>Allergens:</strong> ${batch.allergenInfo}
              </div>
            ` : ''}
          </div>

          <div style="padding-top:1rem;border-top:1px solid var(--border-subtle);display:flex;align-items:center;justify-content:space-between;">
            <span style="font-size:0.78rem;color:var(--text-muted);text-transform:uppercase;font-weight:600;">
              ${batch.category.replace('_', ' ')}
            </span>
            <button onclick="DonationsApp.openClaimModal('${batch.id}')" class="btn btn-primary btn-sm">
              Claim Food 🤝
            </button>
          </div>
        </div>
      `;
    }).join('');

    listContainer.innerHTML = cardsHtml;
  }

  function openClaimModal(batchId) {
    const user = Auth.getUser();
    if (!user) {
      showToast('Please sign in as a recipient (shelter/NGO) to claim food.', true);
      setTimeout(() => window.location.href = 'login.html', 800);
      return;
    }

    if (user.role !== 'RECIPIENT' && user.role !== 'ADMIN') {
      showToast(`Only Recipient accounts can claim food. Current role: ${user.role}`, true);
      return;
    }

    selectedBatch = allBatches.find(b => b.id === batchId);
    if (!selectedBatch) return;

    document.getElementById('claimBatchName').textContent = selectedBatch.foodName;
    document.getElementById('claimBatchDonor').textContent = selectedBatch.donorName;
    document.getElementById('claimBatchRemaining').textContent = `${selectedBatch.remainingQuantity} ${selectedBatch.unit}`;
    document.getElementById('claimQuantityInput').max = selectedBatch.remainingQuantity;
    document.getElementById('claimQuantityInput').value = selectedBatch.remainingQuantity;
    document.getElementById('claimUnitLabel').textContent = selectedBatch.unit;

    // Prefill recipient address if known
    if (user.address) {
      document.getElementById('claimDeliveryAddress').value = user.address;
    }

    document.getElementById('claimModal').classList.add('active');
  }

  function closeClaimModal() {
    document.getElementById('claimModal').classList.remove('active');
    selectedBatch = null;
  }

  async function submitClaim(e) {
    e.preventDefault();
    if (!selectedBatch) return;

    const quantity = parseFloat(document.getElementById('claimQuantityInput').value);
    const address = document.getElementById('claimDeliveryAddress').value.trim();

    if (!quantity || quantity <= 0) {
      showToast('Please enter a valid positive quantity', true);
      return;
    }

    if (quantity > selectedBatch.remainingQuantity) {
      showToast(`Quantity cannot exceed remaining available (${selectedBatch.remainingQuantity} ${selectedBatch.unit})`, true);
      return;
    }

    if (!address) {
      showToast('Delivery address is required', true);
      return;
    }

    const submitBtn = document.getElementById('submitClaimBtn');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Processing Allocation...';

    try {
      const claimData = {
        batchId: selectedBatch.id,
        quantity: quantity,
        deliveryAddress: address,
        recipientLatitude: Auth.getUser()?.latitude || 13.0827,
        recipientLongitude: Auth.getUser()?.longitude || 80.2707,
        destinationNodeId: null // Backend automatically resolves closest road junction
      };

      const res = await API.claimDonation(claimData);
      if (res.success) {
        showToast('Donation allocated successfully! Delivery request dispatched to volunteer drivers.', false);
        closeClaimModal();
        await loadDonations();
      } else {
        throw new Error(res.message);
      }
    } catch (err) {
      showToast(err.message || 'Failed to claim donation', true);
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = 'Confirm Allocation';
    }
  }

  function resetFilters() {
    if (document.getElementById('categoryFilter')) document.getElementById('categoryFilter').value = '';
    if (document.getElementById('vegFilter')) document.getElementById('vegFilter').value = '';
    if (document.getElementById('searchInput')) document.getElementById('searchInput').value = '';
    loadDonations();
  }

  function setupEventListeners() {
    document.getElementById('categoryFilter')?.addEventListener('change', loadDonations);
    document.getElementById('vegFilter')?.addEventListener('change', loadDonations);
    document.getElementById('searchInput')?.addEventListener('input', debounce(loadDonations, 300));
    document.getElementById('claimForm')?.addEventListener('submit', submitClaim);
  }

  function debounce(func, wait) {
    let timeout;
    return (...args) => {
      clearTimeout(timeout);
      timeout = setTimeout(() => func(...args), wait);
    };
  }

  return {
    init,
    loadDonations,
    openClaimModal,
    closeClaimModal,
    submitClaim,
    resetFilters
  };
})();

document.addEventListener('DOMContentLoaded', DonationsApp.init);
