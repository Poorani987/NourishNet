/**
 * NourishNet REST API Client
 * Centralized HTTP service communicating with the Java Spring Boot backend.
 */
const API = (() => {
  // Determine API base: Use origin or default to http://localhost:8080 if file://
  const BASE_URL = (window.location.protocol === 'file:' || !window.location.port)
    ? 'http://localhost:8080/api'
    : '/api';

  let authToken = localStorage.getItem('nourishnet_token') || null;

  function setToken(token) {
    authToken = token;
    if (token) {
      localStorage.setItem('nourishnet_token', token);
    } else {
      localStorage.removeItem('nourishnet_token');
    }
  }

  function getToken() {
    return authToken;
  }

  async function request(endpoint, options = {}) {
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...(options.headers || {})
    };

    if (authToken) {
      headers['Authorization'] = `Bearer ${authToken}`;
    }

    try {
      const response = await fetch(`${BASE_URL}${endpoint}`, {
        ...options,
        headers
      });

      const data = await response.json().catch(() => null);

      if (!response.ok) {
        const errorMsg = (data && data.message) 
          ? data.message 
          : (data && data.error) ? data.error : `HTTP ${response.status}: ${response.statusText}`;
        throw new Error(errorMsg);
      }

      return data;
    } catch (err) {
      console.error(`API Error on [${options.method || 'GET'} ${endpoint}]:`, err);
      throw err;
    }
  }

  return {
    setToken,
    getToken,

    // Auth
    login: (credentials) => request('/auth/login', { method: 'POST', body: JSON.stringify(credentials) }),
    register: (userData) => request('/auth/register', { method: 'POST', body: JSON.stringify(userData) }),
    getCurrentUser: () => request('/auth/me'),
    getDemoAccounts: () => request('/auth/demo-accounts'),

    // Donations
    getDonations: (params = {}) => {
      const q = new URLSearchParams(params).toString();
      return request(`/donations${q ? '?' + q : ''}`);
    },
    getDonationById: (id) => request(`/donations/${id}`),
    getDonationsByDonor: (donorId) => request(`/donations/donor/${donorId}`),
    createDonation: (donationData) => request('/donations', { method: 'POST', body: JSON.stringify(donationData) }),
    cancelDonation: (id) => request(`/donations/${id}/cancel`, { method: 'PATCH' }),

    // Allocations
    claimDonation: (claimData) => request('/allocations', { method: 'POST', body: JSON.stringify(claimData) }),
    getAllocations: (params = {}) => {
      const q = new URLSearchParams(params).toString();
      return request(`/allocations${q ? '?' + q : ''}`);
    },
    getAllocationById: (id) => request(`/allocations/${id}`),

    // Deliveries
    getAllDeliveries: (params = {}) => {
      const q = new URLSearchParams(params).toString();
      return request(`/deliveries${q ? '?' + q : ''}`);
    },
    getPendingDeliveries: () => request('/deliveries/pending'),
    getDeliveryById: (id) => request(`/deliveries/${id}`),
    acceptDelivery: (id) => request(`/deliveries/${id}/accept`, { method: 'POST' }),
    updateDeliveryStatus: (id, status) => request(`/deliveries/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status })
    }),
    getDeliveryRoute: (id) => request(`/deliveries/${id}/route`),
    verifyHandoverOtp: (id, otp) => request(`/deliveries/${id}/verify-otp`, {
      method: 'POST',
      body: JSON.stringify({ otp })
    }),

    // Volunteers
    registerVehicle: (vehicleData) => request('/volunteers', { method: 'POST', body: JSON.stringify(vehicleData) }),
    getAllVolunteers: () => request('/volunteers'),
    getMyVolunteerProfile: () => request('/volunteers/me'),
    getEligibleVolunteers: (deliveryId) => request(`/volunteers/eligible${deliveryId ? '?deliveryId=' + deliveryId : ''}`),
    toggleAvailability: (available) => request('/volunteers/availability', {
      method: 'PATCH',
      body: JSON.stringify({ available })
    }),

    // Demands (Recipient Food Requests)
    createDemand: (demandData) => request('/demands', { method: 'POST', body: JSON.stringify(demandData) }),
    getAllDemands: (status) => request(`/demands${status ? '?status=' + status : ''}`),
    getDemandsByRecipient: (recipId) => request(`/demands/recipient/${recipId}`),
    fulfillDemand: (id) => request(`/demands/${id}/fulfill`, { method: 'PATCH' }),

    // Food Waste Management
    recordWaste: (wasteData) => request('/waste', { method: 'POST', body: JSON.stringify(wasteData) }),
    getAllWasteRecords: () => request('/waste'),
    getWasteStats: () => request('/waste/stats'),

    // Dashboard & Metrics
    getDashboardSummary: () => request('/dashboard/summary'),

    // Dijkstra Road Routing
    getRoutingNodes: () => request('/routing/nodes'),
    getRoutingEdges: () => request('/routing/edges'),
    calculateShortestPath: (source, destination) => request(`/routing/calculate?source=${encodeURIComponent(source)}&destination=${encodeURIComponent(destination)}`),

    // Notifications
    getNotifications: () => request('/notifications'),

    // Admin
    getAdminUsers: (role) => request(`/admin/users${role ? '?role=' + role : ''}`)
  };
})();
