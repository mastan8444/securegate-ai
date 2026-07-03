const BACKEND_URL = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', () => {
  const token = localStorage.getItem('token');
  if (token) {
    showDashboard();
  } else {
    showLogin();
  }

  // Setup Event Listeners
  document.getElementById('login-btn').addEventListener('click', handleLogin);
  document.getElementById('logout-btn').addEventListener('click', handleLogout);
  document.getElementById('email-alerts-toggle').addEventListener('change', handleToggleEmailAlerts);
});

function showLogin() {
  document.getElementById('login-view').classList.remove('hidden');
  document.getElementById('dashboard-view').classList.add('hidden');
}

function showDashboard() {
  document.getElementById('login-view').classList.add('hidden');
  document.getElementById('dashboard-view').classList.remove('hidden');
  fetchStats();
  fetchRules();
}

async function handleLogin() {
  const usernameInput = document.getElementById('username').value;
  const passwordInput = document.getElementById('password').value;
  const errorAlert = document.getElementById('login-error');

  errorAlert.classList.add('hidden');

  try {
    const response = await fetch(`${BACKEND_URL}/api/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username: usernameInput, password: passwordInput }),
    });

    if (response.ok) {
      const data = await response.json();
      localStorage.setItem('token', data.token);
      localStorage.setItem('username', data.username);
      showDashboard();
    } else {
      const errMsg = await response.text();
      errorAlert.textContent = errMsg || 'Login failed. Check your password.';
      errorAlert.classList.remove('hidden');
    }
  } catch (error) {
    errorAlert.textContent = 'Unable to connect to SecureGate AI backend.';
    errorAlert.classList.remove('hidden');
  }
}

function handleLogout() {
  localStorage.removeItem('token');
  localStorage.removeItem('username');
  showLogin();
}

async function fetchStats() {
  const token = localStorage.getItem('token');
  if (!token) return;

  try {
    const response = await fetch(`${BACKEND_URL}/api/logs/stats`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (response.ok) {
      const data = await response.json();
      document.getElementById('blocked-count').textContent = data.blockedIpsCount;
      document.getElementById('allowed-count').textContent = data.allowedIpsCount;
      document.getElementById('blacklist-size').textContent = data.blacklistSize;
      document.getElementById('whitelist-size').textContent = data.whitelistSize;
      document.getElementById('top-attacker-ip').textContent = data.topAttacker?.ip || 'None';
      document.getElementById('top-attacker-count').textContent = data.topAttacker?.count || 0;
    } else if (response.status === 403 || response.status === 401) {
      handleLogout();
    }
  } catch (error) {
    console.error('Failed to fetch stats:', error);
  }
}

async function fetchRules() {
  const token = localStorage.getItem('token');
  if (!token) return;

  try {
    const response = await fetch(`${BACKEND_URL}/api/rules`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (response.ok) {
      const rules = await response.json();
      const emailRule = rules.find(r => r.ruleKey === 'EMAIL_ALERTS_ENABLED');
      if (emailRule) {
        document.getElementById('email-alerts-toggle').checked = emailRule.enabled;
      }
    }
  } catch (error) {
    console.error('Failed to fetch rules:', error);
  }
}

async function handleToggleEmailAlerts(e) {
  const token = localStorage.getItem('token');
  if (!token) return;

  const isChecked = e.target.checked;

  try {
    await fetch(`${BACKEND_URL}/api/rules/EMAIL_ALERTS_ENABLED`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        ruleValue: String(isChecked),
        enabled: isChecked
      })
    });
  } catch (error) {
    console.error('Failed to update email alert rule:', error);
  }
}
