/**
 * NourishNet — Deliveries Tracking & Logistics Coordination
 * Tracks delivery status transitions, distance-based transport policy, 
 * volunteer assignments, and Dijkstra shortest route details.
 */

const DeliveriesApp = (() => {
  let allDeliveries = [];

  async function init() {
    await loadDeliveries();
    setupEventListeners();
  }

  async function loadDeliveries() {
    const listContainer = document.getElementById('deliveriesList');
    if (!listContainer) return;

    listContainer.innerHTML = '<div style="text-align:center;padding:3rem;color:var(--text-muted);">🔄 Loading active deliveries...</div>';

    try {
      const statusFilter = document.getElementById('deliveryStatusFilter')?.value || '';
      const params = {};
      if (statusFilter) params.status = statusFilter;

      const res = await API.getAllDeliveries(params);
      allDeliveries = res.data || [];
      renderDeliveries(allDeliveries);
    } catch (err) {
      listContainer.innerHTML = `<div style="text-align:center;padding:3rem;color:#dc3545;">⚠️ Failed to load deliveries: ${err.message}</div>`;
    }
  }

  function renderDeliveries(deliveries) {
    const listContainer = document.getElementById('deliveriesList');
    if (!listContainer) return;

    if (deliveries.length === 0) {
      listContainer.innerHTML = `
        <div style="text-align:center;padding:3.5rem;background:#fff;border-radius:var(--radius-lg);border:1px solid var(--border-subtle);">
          <div style="font-size:3rem;margin-bottom:0.75rem;">🚚</div>
          <h3 style="color:var(--color-forest-dark);margin-bottom:0.5rem;">No deliveries found</h3>
          <p style="color:var(--text-muted);max-width:420px;margin:0 auto;">No active food redistribution deliveries match the selected filter.</p>
        </div>
      `;
      return;
    }

    const html = deliveries.map(deliv => {
      let statusBadgeClass = 'badge-assigned';
      if (deliv.status === 'PENDING_ACCEPTANCE') statusBadgeClass = 'badge-partially-allocated';
      if (deliv.status === 'PICKED_UP') statusBadgeClass = 'badge-picked-up';
      if (deliv.status === 'DELIVERED') statusBadgeClass = 'badge-delivered';
      if (deliv.status === 'CANCELLED') statusBadgeClass = 'badge-cancelled';

      const routeNodes = deliv.routeNodeIds && deliv.routeNodeIds.length > 0 
        ? deliv.routeNodeIds.join(' ➔ ') 
        : 'Route pending volunteer assignment';

      return `
        <div class="card" style="margin-bottom:1.25rem;">
          <div style="display:flex;justify-content:space-between;align-items:flex-start;flex-wrap:wrap;gap:0.75rem;margin-bottom:1rem;">
            <div>
              <div style="display:flex;align-items:center;gap:0.6rem;margin-bottom:0.25rem;">
                <span class="badge ${statusBadgeClass}">${deliv.status.replace('_', ' ')}</span>
                <span style="font-size:0.8rem;color:var(--text-light);font-family:monospace;">#${deliv.id.substring(0, 8)}</span>
              </div>
              <h3 style="font-size:1.3rem;color:var(--color-forest-dark);">${deliv.foodName}</h3>
              <div style="font-size:0.9rem;font-weight:600;color:var(--color-green-emerald);margin-top:0.15rem;">
                Quantity: ${deliv.quantity} ${deliv.unit}
              </div>
            </div>

            <div style="text-align:right;">
              <span class="policy-badge ${deliv.routeDistanceKm <= 15 ? 'policy-local' : 'policy-extended'}">
                ${deliv.routeDistanceKm > 0 ? `${deliv.routeDistanceKm} km Dijkstra Route` : 'Distance Pending'}
              </span>
            </div>
          </div>

          <div style="display:grid;grid-template-columns:repeat(auto-fit, minmax(260px, 1fr));gap:1rem;background:var(--bg-cream);padding:1rem;border-radius:var(--radius-md);margin-bottom:1rem;border:1px solid var(--border-subtle);">
            <div>
              <div style="font-size:0.78rem;text-transform:uppercase;font-weight:700;color:var(--text-muted);margin-bottom:0.25rem;">Pickup Location</div>
              <div style="font-size:0.88rem;color:var(--text-heading);font-weight:600;">${deliv.donorName}</div>
              <div style="font-size:0.82rem;color:var(--text-muted);">${deliv.pickupAddress}</div>
            </div>

            <div>
              <div style="font-size:0.78rem;text-transform:uppercase;font-weight:700;color:var(--text-muted);margin-bottom:0.25rem;">Dropoff Destination</div>
              <div style="font-size:0.88rem;color:var(--text-heading);font-weight:600;">${deliv.recipientName}</div>
              <div style="font-size:0.82rem;color:var(--text-muted);">${deliv.dropoffAddress}</div>
            </div>

            <div>
              <div style="font-size:0.78rem;text-transform:uppercase;font-weight:700;color:var(--text-muted);margin-bottom:0.25rem;">Assigned Volunteer</div>
              <div style="font-size:0.88rem;color:var(--text-heading);font-weight:600;">
                ${deliv.volunteerName ? `🚚 ${deliv.volunteerName}` : '⏳ Awaiting Volunteer Driver'}
              </div>
              <div style="font-size:0.82rem;color:var(--text-muted);">
                ${deliv.status === 'DELIVERED' ? '✅ Food safely received' : deliv.status === 'PICKED_UP' ? '📦 En route with food' : 'Pending pickup'}
              </div>
            </div>
          </div>

          <!-- Dijkstra Road Routing Visualization Summary -->
          <div style="background:var(--color-pastel-tint);border-radius:var(--radius-sm);padding:0.75rem 1rem;font-size:0.85rem;display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:0.75rem;">
            <div style="display:flex;align-items:center;gap:0.5rem;flex-wrap:wrap;">
              <span>🗺️ <strong>Shortest Road Route:</strong></span>
              <span style="font-family:monospace;font-size:0.82rem;color:var(--color-forest-dark);background:#fff;padding:0.2rem 0.5rem;border-radius:var(--radius-xs);border:1px solid var(--border-subtle);">
                ${routeNodes}
              </span>
            </div>
            <div>
              <a href="route.html?source=${deliv.sourceNodeId || 'NODE-CENTRAL'}&destination=${deliv.destinationNodeId || 'NODE-SHELTER-NORTH'}" class="btn btn-outline btn-sm">
                Explore in Route Visualizer ↗
              </a>
            </div>
          </div>
        </div>
      `;
    }).join('');

    listContainer.innerHTML = html;
  }

  function setupEventListeners() {
    document.getElementById('deliveryStatusFilter')?.addEventListener('change', loadDeliveries);
  }

  return {
    init,
    loadDeliveries
  };
})();

document.addEventListener('DOMContentLoaded', DeliveriesApp.init);
