const express = require("express");
const axios = require("axios");
const cors = require("cors");
const cookieParser = require("cookie-parser");

const app = express();

app.use(cors());
app.use(express.json());
app.use(cookieParser());

const GATEWAY = process.env.GATEWAY_URL || "http://localhost:8080";
const API_KEY = "sg_live_default_system_secret_key_123456";

// Mock Employee Database
const EMPLOYEES = {
    "employee1": "password123",
    "manager": "adminpassword",
    "clerk": "clerk123"
};

// Renders Login Form or Employee Dashboard
app.get("/", (req, res) => {
    const session = req.cookies.session;

    if (session && session.startsWith("emp_token_")) {
        const username = session.split("_")[2];
        return res.send(`
            <!DOCTYPE html>
            <html>
            <head>
                <title>ABC Bank - Employee Portal</title>
                <style>
                    body { background-color: #080c14; color: #fff; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; }
                    .navbar { background: #0f172a; border-bottom: 1px solid rgba(255,255,255,0.06); padding: 15px 40px; display: flex; justify-content: space-between; align-items: center; }
                    .logo { font-weight: 800; font-size: 1.3rem; color: #6366f1; }
                    .user-info { display: flex; align-items: center; gap: 20px; }
                    .btn-logout { background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2); color: #ef4444; padding: 8px 16px; border-radius: 8px; cursor: pointer; font-weight: 600; transition: all 0.2s; }
                    .btn-logout:hover { background: #ef4444; color: #fff; }
                    .container { padding: 40px; max-width: 1200px; margin: 0 auto; }
                    .welcome { margin-bottom: 30px; }
                    .welcome h1 { margin: 0; font-size: 2rem; font-weight: 800; }
                    .welcome p { color: #64748b; margin: 5px 0 0 0; }
                    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 25px; margin-bottom: 40px; }
                    .card { background: rgba(15, 23, 42, 0.65); border: 1px solid rgba(255, 255, 255, 0.05); border-radius: 16px; padding: 25px; box-shadow: 0 10px 20px rgba(0,0,0,0.3); }
                    .card h3 { margin: 0 0 10px 0; color: #94a3b8; font-size: 0.9rem; text-transform: uppercase; letter-spacing: 0.5px; }
                    .card .value { font-size: 2.2rem; font-weight: 800; color: #fff; }
                    .card .desc { font-size: 0.85rem; color: #64748b; margin-top: 5px; }
                    .table-container { background: rgba(15, 23, 42, 0.65); border: 1px solid rgba(255, 255, 255, 0.05); border-radius: 16px; padding: 25px; box-shadow: 0 10px 20px rgba(0,0,0,0.3); margin-bottom: 40px; }
                    .table-container h2 { margin: 0 0 20px 0; font-size: 1.25rem; font-weight: 700; }
                    table { width: 100%; border-collapse: collapse; text-align: left; }
                    th { color: #64748b; font-weight: 600; padding: 12px 8px; border-bottom: 2px solid rgba(255,255,255,0.06); }
                    td { padding: 14px 8px; border-bottom: 1px solid rgba(255,255,255,0.04); color: #e2e8f0; }
                    .badge { display: inline-block; padding: 4px 8px; border-radius: 6px; font-size: 0.75rem; font-weight: 700; }
                    .badge-success { background: rgba(16, 185, 129, 0.1); color: #10b981; border: 1px solid rgba(16, 185, 129, 0.2); }
                    .badge-warning { background: rgba(245, 158, 11, 0.1); color: #f59e0b; border: 1px solid rgba(245, 158, 11, 0.2); }
                    .badge-danger { background: rgba(239, 68, 68, 0.1); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.2); }
                    .badge-primary { background: rgba(59, 130, 246, 0.1); color: #60a5fa; border: 1px solid rgba(59, 130, 246, 0.2); }
                    .btn-unblock { background: #6366f1; border: none; color: white; font-weight: 700; padding: 6px 14px; border-radius: 6px; cursor: pointer; transition: all 0.2s; }
                    .btn-unblock:hover { background: #4f46e5; }
                    .lock-icon { font-size: 0.8rem; color: #64748b; font-weight: 500; font-style: italic; }
                </style>
            </head>
            <body>
                <div class="navbar">
                    <div class="logo">🏦 ABC Bank Employee Hub</div>
                    <div class="user-info">
                        <span>Employee: <strong style="color: #6366f1;">\${username}</strong></span>
                        <form action="/logout" method="POST" style="margin: 0;">
                            <button type="submit" class="btn-logout">Logout</button>
                        </form>
                    </div>
                </div>

                <div class="container">
                    <div class="welcome">
                        <h1>Operational Control Center</h1>
                        <p>Internal employee interface for transaction monitoring and active threat mitigation.</p>
                    </div>

                    <div class="grid">
                        <div class="card">
                            <h3>Total Assets Under Management</h3>
                            <div class="value">$142,504,821</div>
                            <div class="desc">+12.4% this quarter</div>
                        </div>
                        <div class="card">
                            <h3>Active Core Customers</h3>
                            <div class="value">8,421</div>
                            <div class="desc">Across 14 regions</div>
                        </div>
                        <div class="card">
                            <h3>Gateway Integration</h3>
                            <div class="value" style="color: #10b981;">Online</div>
                            <div class="desc">SecureGate AI Active Shield</div>
                        </div>
                    </div>

                    <!-- WAF UNBLOCK CENTER -->
                    <div class="table-container">
                        <h2 style="color: #f87171;">🛡️ SecureGate AI - Blacklisted Visitors & Unblock Center</h2>
                        <p style="color: #64748b; font-size: 0.9rem; margin-top: -15px; margin-bottom: 20px;">
                            Below are the current visitor IPs blocked by WAF. You can release temporary restrictions. Permanent bans require SecureGate platform administrator credentials.
                        </p>
                        <table>
                            <thead>
                                <tr>
                                    <th>Visitor IP</th>
                                    <th>Block Status</th>
                                    <th>Reason for Ban</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody id="blacklistBody">
                                <tr>
                                    <td colspan="4" style="color: #64748b; text-align: center;">Loading WAF logs...</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="table-container">
                        <h2>Recent Wire Transfers</h2>
                        <table>
                            <thead>
                                <tr>
                                    <th>Transaction ID</th>
                                    <th>Sender</th>
                                    <th>Recipient</th>
                                    <th>Amount</th>
                                    <th>Status</th>
                                    <th>Date</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td style="font-family: monospace;">TX-998124</td>
                                    <td>Acme Corp</td>
                                    <td>Global Logistics Ltd</td>
                                    <td style="font-weight: 700; color: #10b981;">+$45,200.00</td>
                                    <td><span class="badge badge-success">Completed</span></td>
                                    <td>Today, 10:14 AM</td>
                                </tr>
                                <tr>
                                    <td style="font-family: monospace;">TX-998123</td>
                                    <td>Jane Doe</td>
                                    <td>John Smith</td>
                                    <td style="font-weight: 700; color: #fff;">-$120.00</td>
                                    <td><span class="badge badge-success">Completed</span></td>
                                    <td>Today, 09:30 AM</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                <script>
                    async function loadBlacklist() {
                        const tbody = document.getElementById("blacklistBody");
                        try {
                            const res = await fetch("/api/blacklist");
                            const list = await res.json();
                            
                            if (list.length === 0) {
                                tbody.innerHTML = '<tr><td colspan="4" style="color: #64748b; text-align: center;">🎉 No visitor IPs are currently blacklisted for this site.</td></tr>';
                                return;
                            }

                            tbody.innerHTML = list.map(item => {
                                const isTemp = item.status === "TEMPORARY";
                                const badgeClass = isTemp ? "badge-primary" : "badge-danger";
                                const actionCell = isTemp 
                                    ? \`<button class="btn-unblock" onclick="unblockIp('\${item.ipAddress}')">Unblock Visitor</button>\`
                                    : \`<span class="lock-icon">🔒 Super-Admin Locked</span>\`;

                                return \`
                                    <tr>
                                        <td style="font-family: monospace; font-weight: 600; color: #38bdf8;">\${item.ipAddress}</td>
                                        <td><span class="badge \${badgeClass}">\${item.status}</span></td>
                                        <td style="color: #94a3b8; font-size: 0.9rem;">\${item.reason}</td>
                                        <td>\${actionCell}</td>
                                    </tr>
                                \`;
                            }).join("");

                        } catch (err) {
                            tbody.innerHTML = '<tr><td colspan="4" style="color: #f87171; text-align: center;">Error contacting security gateway.</td></tr>';
                        }
                    }

                    async function unblockIp(ip) {
                        if (confirm(\`Lift temporary access restrictions for IP \${ip}?\`)) {
                            try {
                                const res = await fetch("/api/unblock", {
                                    method: "POST",
                                    headers: { "Content-Type": "application/json" },
                                    body: JSON.stringify({ ip })
                                });
                                const data = await res.json();
                                if (res.ok && data.message) {
                                    alert("Visitor unblocked successfully!");
                                    loadBlacklist();
                                } else {
                                    alert(data.message || "Failed to unblock visitor.");
                                }
                            } catch (err) {
                                alert("Failed to connect to gateway.");
                            }
                        }
                    }

                    loadBlacklist();
                </script>
            </body>
            </html>
        `);
    }

    // Render Login Page
    res.send(`
        <!DOCTYPE html>
        <html>
        <head>
            <title>Employee Secure Hub - Log In</title>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { background-color: #080c14; color: #fff; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; }
                .card { background: rgba(15, 23, 42, 0.65); border: 1px solid rgba(255, 255, 255, 0.05); border-radius: 24px; padding: 40px; max-width: 400px; width: 100%; box-shadow: 0 20px 50px rgba(0, 0, 0, 0.6); }
                .header { text-align: center; margin-bottom: 30px; }
                h1 { margin: 0; font-weight: 800; font-size: 1.8rem; letter-spacing: -0.5px; }
                h2 { color: #6366f1; margin: 5px 0 0 0; font-weight: 600; font-size: 0.95rem; text-transform: uppercase; letter-spacing: 1px; }
                .input-group { margin-bottom: 20px; text-align: left; }
                label { display: block; margin-bottom: 8px; color: #94a3b8; font-size: 0.9rem; font-weight: 500; }
                input { width: 100%; box-sizing: border-box; background: rgba(255, 255, 255, 0.02); border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 12px; padding: 14px; color: #fff; font-size: 1rem; transition: all 0.2s; }
                input:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2); }
                .btn { display: block; width: 100%; background: #6366f1; color: white; border: none; padding: 14px; border-radius: 12px; font-weight: 700; cursor: pointer; margin-top: 25px; transition: all 0.2s; font-size: 1rem; }
                .btn:hover { background: #4f46e5; }
                .error-box { display: none; background-color: rgba(239, 68, 68, 0.06); border: 1px solid rgba(239, 68, 68, 0.2); color: #f87171; padding: 12px; border-radius: 12px; margin-bottom: 20px; font-size: 0.88rem; line-height: 1.4; text-align: center; }
            </style>
        </head>
        <body>
            <div class="card">
                <div class="header">
                    <h2>ABC Bank Portal</h2>
                    <h1>Employee Portal Log In</h1>
                </div>

                <div class="error-box" id="errorBox"></div>

                <form id="loginForm" onsubmit="handleLogin(event)">
                    <div class="input-group">
                        <label>Username</label>
                        <input type="text" id="username" placeholder="Employee username" required>
                    </div>
                    <div class="input-group">
                        <label>Password</label>
                        <input type="password" id="password" placeholder="Password" required>
                    </div>
                    <button type="submit" class="btn" id="loginBtn">Secure Log In</button>
                </form>
            </div>

            <script>
                async function handleLogin(e) {
                    e.preventDefault();
                    const u = document.getElementById('username').value;
                    const p = document.getElementById('password').value;
                    const errorBox = document.getElementById('errorBox');
                    const loginBtn = document.getElementById('loginBtn');

                    errorBox.style.display = 'none';
                    loginBtn.disabled = true;
                    loginBtn.innerText = 'Verifying Credentials...';

                    try {
                        const response = await fetch('/login', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ username: u, password: p })
                        });

                        const result = await response.json();

                        if (result.status === 'SUCCESS') {
                            window.location.reload();
                        } else {
                            errorBox.innerText = result.message;
                            errorBox.style.display = 'block';
                            document.getElementById('password').value = '';
                        }
                    } catch (err) {
                        errorBox.innerText = 'Connection to gateway failed.';
                        errorBox.style.display = 'block';
                    } finally {
                        loginBtn.disabled = false;
                        loginBtn.innerText = 'Secure Log In';
                    }
                }
            </script>
        </body>
        </html>
    `);
});

// GET /api/blacklist: Proxies request to gateway check blacklist
app.get("/api/blacklist", async (req, res) => {
    try {
        const response = await axios.get(`${GATEWAY}/api/check/blacklist?apiKey=${API_KEY}`);
        return res.json(response.data);
    } catch (err) {
        console.error(`[SecureGate AI] Blacklist fetch failed: ${err.message}`);
        return res.status(500).json({ error: "Failed to fetch blacklist" });
    }
});

// POST /api/unblock: Proxies unblock request to gateway
app.post("/api/unblock", async (req, res) => {
    const { ip } = req.body;
    try {
        const response = await axios.post(`${GATEWAY}/api/check/unblock`, {
            ip,
            apiKey: API_KEY
        });
        return res.json(response.data);
    } catch (err) {
        if (err.response && err.response.status === 403) {
            return res.status(403).json(err.response.data);
        }
        console.error(`[SecureGate AI] Unblock failed: ${err.message}`);
        return res.status(500).json({ error: "Failed to lift restriction" });
    }
});

// POST /login: Authenticate credentials
app.post("/login", async (req, res) => {
    const { username, password } = req.body;

    if (EMPLOYEES[username] && EMPLOYEES[username] === password) {
        res.cookie("session", `emp_token_${username}`, { httpOnly: true });
        return res.json({
            status: "SUCCESS",
            message: "Login successful!"
        });
    }

    return res.status(401).json({
        status: "FAILED",
        message: "Invalid employee username or password."
    });
});

// POST /logout: Clear session
app.post("/logout", (req, res) => {
    res.clearCookie("session");
    res.redirect("/");
});

app.listen(3001, () => {
    console.log("ABC Bank Employee Portal running on port 3001");
});
