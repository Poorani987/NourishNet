/**
 * NourishNet — Interactive Dijkstra Road Network Route Calculator & Visualizer
 * Communicates with Java backend Dijkstra Routing Service (/api/routes/shortest-path).
 * Renders an interactive 2D graph with path highlighting and distance calculation.
 */

const RouteApp = (() => {
  let nodes = [];
  let edges = [];
  let currentResult = null;

  async function init() {
    await loadNetworkData();
    populateDropdowns();
    setupCanvas();
    handleUrlParams();
  }

  async function loadNetworkData() {
    try {
      const [nodesRes, edgesRes] = await Promise.all([
        API.getRoutingNodes(),
        API.getRoutingEdges()
      ]);
      nodes = nodesRes.data || [];
      edges = edgesRes.data || [];
    } catch (err) {
      console.warn('Using fallback node coordinates for visualizer:', err);
      nodes = [
        { id: "NODE-CENTRAL", name: "Central Food Bank Hub", latitude: 13.0827, longitude: 80.2707 },
        { id: "NODE-MARKET-WEST", name: "West Market Produce Center", latitude: 13.0780, longitude: 80.2520 },
        { id: "NODE-RESTAURANT-ROW", name: "Green Valley Dining Strip", latitude: 13.0910, longitude: 80.2640 },
        { id: "NODE-SHELTER-NORTH", name: "North Community Shelter", latitude: 13.1120, longitude: 80.2680 },
        { id: "NODE-CAMPUS-EAST", name: "East University Campus Hub", latitude: 13.0860, longitude: 80.2910 },
        { id: "NODE-ASHRAM-SOUTH", name: "South Shanti Care Ashram", latitude: 13.0510, longitude: 80.2590 },
        { id: "NODE-JUNCTION-A", name: "Crossroads Junction A", latitude: 13.0750, longitude: 80.2750 },
        { id: "NODE-JUNCTION-B", name: "Ring Road Junction B", latitude: 13.0980, longitude: 80.2820 },
        { id: "NODE-CHILDRENS-HOME", name: "Sunrise Children Home", latitude: 13.0650, longitude: 80.2450 },
        { id: "NODE-CIVIC-CENTER", name: "Civic Plaza Depot", latitude: 13.0700, longitude: 80.2650 }
      ];
      edges = [
        { fromNodeId: "NODE-CENTRAL", toNodeId: "NODE-RESTAURANT-ROW", distanceKm: 2.4 },
        { fromNodeId: "NODE-CENTRAL", toNodeId: "NODE-MARKET-WEST", distanceKm: 3.1 },
        { fromNodeId: "NODE-CENTRAL", toNodeId: "NODE-JUNCTION-A", distanceKm: 1.8 },
        { fromNodeId: "NODE-CENTRAL", toNodeId: "NODE-CAMPUS-EAST", distanceKm: 3.5 },
        { fromNodeId: "NODE-RESTAURANT-ROW", toNodeId: "NODE-JUNCTION-B", distanceKm: 2.2 },
        { fromNodeId: "NODE-RESTAURANT-ROW", toNodeId: "NODE-SHELTER-NORTH", distanceKm: 3.0 },
        { fromNodeId: "NODE-SHELTER-NORTH", toNodeId: "NODE-JUNCTION-B", distanceKm: 2.6 },
        { fromNodeId: "NODE-CAMPUS-EAST", toNodeId: "NODE-JUNCTION-B", distanceKm: 2.9 },
        { fromNodeId: "NODE-MARKET-WEST", toNodeId: "NODE-CIVIC-CENTER", distanceKm: 2.1 },
        { fromNodeId: "NODE-MARKET-WEST", toNodeId: "NODE-CHILDRENS-HOME", distanceKm: 2.5 },
        { fromNodeId: "NODE-CIVIC-CENTER", toNodeId: "NODE-JUNCTION-A", distanceKm: 1.5 },
        { fromNodeId: "NODE-CIVIC-CENTER", toNodeId: "NODE-ASHRAM-SOUTH", distanceKm: 2.8 },
        { fromNodeId: "NODE-JUNCTION-A", toNodeId: "NODE-ASHRAM-SOUTH", distanceKm: 3.2 },
        { fromNodeId: "NODE-CHILDRENS-HOME", toNodeId: "NODE-ASHRAM-SOUTH", distanceKm: 2.3 }
      ];
    }
  }

  function populateDropdowns() {
    const srcSelect = document.getElementById('routeSourceSelect');
    const dstSelect = document.getElementById('routeDestSelect');
    if (!srcSelect || !dstSelect) return;

    srcSelect.innerHTML = '';
    dstSelect.innerHTML = '';

    nodes.forEach(node => {
      const opt1 = document.createElement('option');
      opt1.value = node.id;
      opt1.textContent = `${node.name} (${node.id})`;
      srcSelect.appendChild(opt1);

      const opt2 = document.createElement('option');
      opt2.value = node.id;
      opt2.textContent = `${node.name} (${node.id})`;
      dstSelect.appendChild(opt2);
    });

    if (srcSelect.options.length > 0) srcSelect.selectedIndex = 0; // NODE-CENTRAL
    if (dstSelect.options.length > 3) dstSelect.selectedIndex = 3; // NODE-SHELTER-NORTH
  }

  function handleUrlParams() {
    const params = new URLSearchParams(window.location.search);
    const src = params.get('source');
    const dst = params.get('destination');

    if (src && document.getElementById('routeSourceSelect')) {
      document.getElementById('routeSourceSelect').value = src;
    }
    if (dst && document.getElementById('routeDestSelect')) {
      document.getElementById('routeDestSelect').value = dst;
    }

    if (src || dst) {
      calculateRoute();
    } else {
      drawGraph();
    }
  }

  async function calculateRoute() {
    const srcSelect = document.getElementById('routeSourceSelect');
    const dstSelect = document.getElementById('routeDestSelect');
    if (!srcSelect || !dstSelect) return;

    const source = srcSelect.value;
    const destination = dstSelect.value;

    const btn = document.getElementById('calcRouteBtn');
    if (btn) {
      btn.disabled = true;
      btn.textContent = 'Calculating Shortest Path...';
    }

    try {
      const res = await API.calculateShortestPath(source, destination);
      if (res.success && res.data) {
        currentResult = res.data;
        displayRouteResults(currentResult);
        drawGraph(currentResult.path);
        showToast(`Shortest path found: ${currentResult.totalDistanceKm} km across ${currentResult.path.length} junctions`, false);
      } else {
        throw new Error(res.message || 'Route calculation failed');
      }
    } catch (err) {
      showToast(err.message || 'Failed to calculate shortest path', true);
      const resBox = document.getElementById('routeResultBox');
      if (resBox) {
        resBox.innerHTML = `<div style="color:#c62828;padding:1rem;">⚠️ ${err.message}</div>`;
      }
    } finally {
      if (btn) {
        btn.disabled = false;
        btn.textContent = 'Run Dijkstra Algorithm 🗺️';
      }
    }
  }

  function displayRouteResults(result) {
    const resBox = document.getElementById('routeResultBox');
    if (!resBox) return;

    const pathSequence = result.path.map((nodeId, idx) => {
      const nodeObj = nodes.find(n => n.id === nodeId);
      const name = nodeObj ? nodeObj.name : nodeId;
      const isStart = idx === 0;
      const isEnd = idx === result.path.length - 1;

      return `
        <span class="route-node-pill" style="${isStart ? 'border-color:#2d6a4f;background:#d8f3dc;' : isEnd ? 'border-color:#1b4332;background:#b7e4c7;' : ''}">
          <span>${isStart ? '🟢 Start: ' : isEnd ? '🎯 End: ' : '📍 '}</span>
          <strong>${name}</strong>
        </span>
        ${idx < result.path.length - 1 ? '<span class="route-node-arrow">➔</span>' : ''}
      `;
    }).join(' ');

    resBox.innerHTML = `
      <div style="background:#ffffff;border:1px solid var(--border-subtle);border-radius:var(--radius-lg);padding:1.5rem;box-shadow:var(--shadow-sm);">
        <div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:1rem;margin-bottom:1.25rem;">
          <div>
            <span class="badge badge-available">Dijkstra Shortest Path Result</span>
            <div style="font-size:1.8rem;font-weight:800;color:var(--color-forest-dark);margin-top:0.35rem;">
              ${result.totalDistanceKm} <span style="font-size:1rem;font-weight:600;color:var(--color-green-emerald);">km total distance</span>
            </div>
          </div>
          <div style="text-align:right;">
            <span style="font-size:0.75rem;text-transform:uppercase;font-weight:700;color:var(--text-muted);display:block;">Data Mode</span>
            <span style="font-size:0.85rem;color:var(--color-forest-dark);font-family:monospace;background:var(--color-pastel-subtle);padding:0.2rem 0.6rem;border-radius:var(--radius-xs);">
              ${result.dataType || 'SAMPLE_ROAD_NETWORK'}
            </span>
          </div>
        </div>

        <div style="font-size:0.85rem;font-weight:700;color:var(--text-muted);text-transform:uppercase;margin-bottom:0.75rem;">
          Navigated Road Junction Sequence:
        </div>
        <div class="route-sequence-list" style="margin-top:0;">
          ${pathSequence}
        </div>
      </div>
    `;
  }

  // 2D Visualizer using HTML5 Canvas
  function setupCanvas() {
    const canvas = document.getElementById('roadGraphCanvas');
    if (!canvas) return;
    resizeCanvas(canvas);
    window.addEventListener('resize', () => {
      resizeCanvas(canvas);
      drawGraph(currentResult?.path);
    });
  }

  function resizeCanvas(canvas) {
    const container = canvas.parentElement;
    canvas.width = container.clientWidth || 800;
    canvas.height = 440;
  }

  function drawGraph(activePath = []) {
    const canvas = document.getElementById('roadGraphCanvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const width = canvas.width;
    const height = canvas.height;

    ctx.clearRect(0, 0, width, height);

    if (nodes.length === 0) return;

    // Normalize coordinates to canvas bounds
    const minLat = Math.min(...nodes.map(n => n.latitude));
    const maxLat = Math.max(...nodes.map(n => n.latitude));
    const minLon = Math.min(...nodes.map(n => n.longitude));
    const maxLon = Math.max(...nodes.map(n => n.longitude));

    const padding = 70;
    const scaleX = (width - padding * 2) / (maxLon - minLon || 1);
    const scaleY = (height - padding * 2) / (maxLat - minLat || 1);

    function getCoords(node) {
      const x = padding + (node.longitude - minLon) * scaleX;
      // Invert Y so north is top
      const y = height - padding - (node.latitude - minLat) * scaleY;
      return { x, y };
    }

    const nodeCoords = {};
    nodes.forEach(n => {
      nodeCoords[n.id] = getCoords(n);
    });

    // Draw all road edges
    edges.forEach(edge => {
      const p1 = nodeCoords[edge.fromNodeId];
      const p2 = nodeCoords[edge.toNodeId];
      if (!p1 || !p2) return;

      const isPathEdge = activePath.some((nodeId, idx) => {
        if (idx === 0) return false;
        const prev = activePath[idx - 1];
        return (prev === edge.fromNodeId && nodeId === edge.toNodeId) ||
               (prev === edge.toNodeId && nodeId === edge.fromNodeId);
      });

      ctx.beginPath();
      ctx.moveTo(p1.x, p1.y);
      ctx.lineTo(p2.x, p2.y);

      if (isPathEdge) {
        ctx.strokeStyle = '#2d6a4f';
        ctx.lineWidth = 6;
        ctx.shadowColor = 'rgba(45, 106, 79, 0.4)';
        ctx.shadowBlur = 10;
      } else {
        ctx.strokeStyle = 'rgba(45, 106, 79, 0.18)';
        ctx.lineWidth = 2;
        ctx.shadowBlur = 0;
      }
      ctx.stroke();

      // Draw distance label on edge
      const midX = (p1.x + p2.x) / 2;
      const midY = (p1.y + p2.y) / 2;
      ctx.fillStyle = isPathEdge ? '#1b4332' : '#839a8f';
      ctx.font = isPathEdge ? 'bold 11px Inter, sans-serif' : '10px Inter, sans-serif';
      ctx.fillText(`${edge.distanceKm} km`, midX + 3, midY - 3);
    });

    ctx.shadowBlur = 0;

    // Draw nodes
    nodes.forEach(node => {
      const p = nodeCoords[node.id];
      if (!p) return;

      const isInPath = activePath.includes(node.id);
      const isStart = activePath.length > 0 && activePath[0] === node.id;
      const isEnd = activePath.length > 0 && activePath[activePath.length - 1] === node.id;

      ctx.beginPath();
      ctx.arc(p.x, p.y, isInPath ? 12 : 8, 0, Math.PI * 2);

      if (isStart) {
        ctx.fillStyle = '#10b981';
      } else if (isEnd) {
        ctx.fillStyle = '#1b4332';
      } else if (isInPath) {
        ctx.fillStyle = '#52b788';
      } else {
        ctx.fillStyle = '#ffffff';
      }

      ctx.fill();
      ctx.lineWidth = isInPath ? 3 : 2;
      ctx.strokeStyle = '#2d6a4f';
      ctx.stroke();

      // Node Name Label
      ctx.fillStyle = isInPath ? '#143627' : '#5c7166';
      ctx.font = isInPath ? 'bold 12px Outfit, sans-serif' : '11px Inter, sans-serif';
      ctx.fillText(node.name.split(' ')[0], p.x + 14, p.y + 4);
    });
  }

  return {
    init,
    calculateRoute
  };
})();

document.addEventListener('DOMContentLoaded', RouteApp.init);
