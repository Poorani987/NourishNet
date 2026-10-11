/**
 * NourishNet Interactive Dijkstra Road Network Routing Visualizer
 * Renders graph nodes, weighted road edges, and highlights Dijkstra shortest path calculation.
 */
const RoutingModule = (() => {
  let nodes = [];
  let edges = [];
  let currentRoute = null;

  // Custom 2D layout coordinates for the 10 road network nodes
  const NODE_COORDINATES = {
    'NODE-CENTRAL': { x: 450, y: 220, label: 'Central Hub' },
    'NODE-RESTAURANT-ROW': { x: 340, y: 130, label: 'Restaurant Row' },
    'NODE-MARKET-WEST': { x: 230, y: 240, label: 'Market West' },
    'NODE-SHELTER-NORTH': { x: 470, y: 60, label: 'North Shelter' },
    'NODE-CAMPUS-EAST': { x: 670, y: 210, label: 'University East' },
    'NODE-JUNCTION-A': { x: 480, y: 320, label: 'Junction A' },
    'NODE-JUNCTION-B': { x: 570, y: 110, label: 'Junction B' },
    'NODE-CIVIC-CENTER': { x: 310, y: 330, label: 'Civic Plaza' },
    'NODE-ASHRAM-SOUTH': { x: 430, y: 420, label: 'South Ashram' },
    'NODE-CHILDRENS-HOME': { x: 200, y: 370, label: 'Children Home' }
  };

  function init() {
    setupSelectors();
  }

  function setupSelectors() {
    const btnCalc = document.getElementById('btnRunDijkstra');
    if (btnCalc) {
      btnCalc.addEventListener('click', runDijkstra);
    }

    const canvas = document.getElementById('graphCanvas');
    if (canvas) {
      canvas.addEventListener('click', handleCanvasClick);
    }
  }

  async function initGraph() {
    try {
      const [nodesRes, edgesRes] = await Promise.all([
        API.getRoutingNodes(),
        API.getRoutingEdges()
      ]);

      nodes = nodesRes.data || [];
      edges = edgesRes.data || [];

      populateNodeDropdowns();
      drawGraph();
    } catch (err) {
      console.error('Failed to load routing graph data:', err);
    }
  }

  function populateNodeDropdowns() {
    const srcSelect = document.getElementById('routeSourceSelect');
    const dstSelect = document.getElementById('routeDestSelect');
    if (!srcSelect || !dstSelect) return;

    srcSelect.innerHTML = '';
    dstSelect.innerHTML = '';

    nodes.forEach(n => {
      const opt1 = document.createElement('option');
      opt1.value = n.id;
      opt1.textContent = `${n.id}: ${n.name}`;
      srcSelect.appendChild(opt1);

      const opt2 = document.createElement('option');
      opt2.value = n.id;
      opt2.textContent = `${n.id}: ${n.name}`;
      dstSelect.appendChild(opt2);
    });

    srcSelect.value = 'NODE-CENTRAL';
    dstSelect.value = 'NODE-SHELTER-NORTH';
  }

  async function runDijkstra() {
    const source = document.getElementById('routeSourceSelect')?.value;
    const dest = document.getElementById('routeDestSelect')?.value;

    if (!source || !dest) {
      App.showToast('Please select source and destination road hubs.', 'error');
      return;
    }

    try {
      App.showToast(`Calculating Dijkstra shortest path from ${source} to ${dest}...`, 'info');
      const res = await API.calculateShortestPath(source, dest);
      currentRoute = res.data;
      renderRouteResult(currentRoute);
      drawGraph();
      App.showToast(`Shortest path found: ${currentRoute.totalDistanceKm} km`, 'success');
    } catch (err) {
      App.showToast(`Routing failed: ${err.message}`, 'error');
    }
  }

  function renderRouteResult(route) {
    const container = document.getElementById('routeResultContainer');
    if (!container || !route) return;

    const pathNodes = route.path || [];
    const stepsHtml = pathNodes.map((nId, idx) => {
      const isLast = idx === pathNodes.length - 1;
      return `<span class="step-node ${isLast ? 'active-dest' : ''}">${nId}</span>${!isLast ? '<span class="step-arrow">➔</span>' : ''}`;
    }).join(' ');

    container.innerHTML = `
      <div class="route-info-box">
        <div>
          <div style="font-size:0.75rem;color:var(--text-muted);text-transform:uppercase;letter-spacing:0.04em;">Dijkstra Shortest Route Sequence</div>
          <div class="route-steps-sequence" style="margin-top:0.4rem;">${stepsHtml}</div>
        </div>
        <div style="text-align:right;">
          <div style="font-size:0.75rem;color:var(--text-muted);text-transform:uppercase;">Total Route Distance</div>
          <div style="font-size:1.75rem;font-weight:800;color:#34d399;font-family:var(--font-heading);">
            ${route.totalDistanceKm} <span style="font-size:1rem;color:var(--text-secondary);">km</span>
          </div>
        </div>
      </div>
    `;
  }

  function drawGraph() {
    const canvas = document.getElementById('graphCanvas');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const width = canvas.parentElement.clientWidth || 800;
    const height = 480;

    canvas.width = width;
    canvas.height = height;

    ctx.clearRect(0, 0, width, height);

    // Scale coordinates to fit canvas
    const scaleX = width / 900;
    const scaleY = height / 500;

    // Draw Edges
    const pathEdges = new Set();
    if (currentRoute && currentRoute.path) {
      for (let i = 0; i < currentRoute.path.length - 1; i++) {
        const u = currentRoute.path[i];
        const v = currentRoute.path[i + 1];
        pathEdges.add(`${u}--${v}`);
        pathEdges.add(`${v}--${u}`);
      }
    }

    // Draw base edges
    edges.forEach(e => {
      const p1 = NODE_COORDINATES[e.fromNodeId];
      const p2 = NODE_COORDINATES[e.toNodeId];
      if (!p1 || !p2) return;

      const isPathEdge = pathEdges.has(`${e.fromNodeId}--${e.toNodeId}`);

      ctx.beginPath();
      ctx.moveTo(p1.x * scaleX, p1.y * scaleY);
      ctx.lineTo(p2.x * scaleX, p2.y * scaleY);

      if (isPathEdge) {
        ctx.strokeStyle = '#10b981';
        ctx.lineWidth = 4;
        ctx.shadowColor = 'rgba(16, 185, 129, 0.8)';
        ctx.shadowBlur = 12;
      } else {
        ctx.strokeStyle = 'rgba(255, 255, 255, 0.12)';
        ctx.lineWidth = 1.5;
        ctx.shadowBlur = 0;
      }
      ctx.stroke();

      // Draw edge distance badge
      const midX = (p1.x + p2.x) / 2 * scaleX;
      const midY = (p1.y + p2.y) / 2 * scaleY;
      ctx.fillStyle = isPathEdge ? '#34d399' : 'rgba(148, 163, 184, 0.7)';
      ctx.font = '10px Inter, sans-serif';
      ctx.fillText(`${e.distanceKm}km`, midX + 4, midY - 4);
    });

    // Reset shadow
    ctx.shadowBlur = 0;

    // Draw Nodes
    nodes.forEach(n => {
      const coord = NODE_COORDINATES[n.id];
      if (!coord) return;

      const x = coord.x * scaleX;
      const y = coord.y * scaleY;

      const isSource = currentRoute && currentRoute.source === n.id;
      const isDest = currentRoute && currentRoute.destination === n.id;
      const isPath = currentRoute && currentRoute.path && currentRoute.path.includes(n.id);

      ctx.beginPath();
      ctx.arc(x, y, isPath ? 14 : 10, 0, Math.PI * 2);

      if (isSource) {
        ctx.fillStyle = '#3b82f6';
      } else if (isDest) {
        ctx.fillStyle = '#f59e0b';
      } else if (isPath) {
        ctx.fillStyle = '#10b981';
      } else {
        ctx.fillStyle = '#1e293b';
      }
      ctx.fill();

      ctx.lineWidth = 2;
      ctx.strokeStyle = isPath ? '#ffffff' : 'rgba(255, 255, 255, 0.3)';
      ctx.stroke();

      // Node text label
      ctx.fillStyle = '#ffffff';
      ctx.font = 'bold 11px Outfit, sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText(coord.label, x, y + 24);
    });
  }

  function handleCanvasClick(e) {
    const canvas = document.getElementById('graphCanvas');
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const clickY = e.clientY - rect.top;

    const scaleX = canvas.width / 900;
    const scaleY = canvas.height / 500;

    // Find nearest node
    for (const [nodeId, coord] of Object.entries(NODE_COORDINATES)) {
      const nodeX = coord.x * scaleX;
      const nodeY = coord.y * scaleY;
      const dist = Math.hypot(clickX - nodeX, clickY - nodeY);

      if (dist < 20) {
        const srcSelect = document.getElementById('routeSourceSelect');
        const dstSelect = document.getElementById('routeDestSelect');
        if (srcSelect && dstSelect) {
          if (!srcSelect.value || (srcSelect.value && dstSelect.value)) {
            srcSelect.value = nodeId;
          } else {
            dstSelect.value = nodeId;
          }
          runDijkstra();
        }
        break;
      }
    }
  }

  return {
    init,
    initGraph,
    runDijkstra
  };
})();

document.addEventListener('DOMContentLoaded', () => {
  RoutingModule.init();
});
