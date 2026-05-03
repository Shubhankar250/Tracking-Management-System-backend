<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <title>TrackingPath — Sign In</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <!-- Bootstrap + Icons -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <style>
    :root {
      --bg: #0b0f14; --panel:#0f141a; --line:#1b2632; --text:#d7e1ea; --accent:#4cc9f0;
    }
    body{background: radial-gradient(1200px 800px at 70% -10%, rgba(76,201,240,.12), transparent), var(--bg); color: var(--text);}
    .card{background: linear-gradient(180deg, #0f141a, #111a24); border:1px solid var(--line); border-radius:16px;}
    .brand{font-weight:700; letter-spacing:.3px; background:linear-gradient(90deg,#4cc9f0,#a78bfa);
      -webkit-background-clip:text; background-clip:text; color:transparent;}
    .form-control, .form-control:focus{background:#0e141c; color:#e5e7eb; border:1px solid #243242;}
    .btn-accent{background:linear-gradient(90deg,#26c6da,#42a5f5); border:none;}
    .btn-accent:hover{filter:brightness(1.05);}
  </style>
</head>
<body>
  <div class="container d-flex align-items-center justify-content-center" style="min-height:100vh">
    <div class="row justify-content-center w-100">
      <div class="col-12 col-sm-10 col-md-7 col-lg-5">
        <div class="text-center mb-4">
          <div class="brand fs-3">TrackingPath</div>
          <div class="text-secondary">Sign in to your account</div>
        </div>

        <div class="card shadow-lg">
          <div class="card-body p-4">
            <form id="loginForm" autocomplete="on">
              <div class="mb-3">
                <label class="form-label">Email</label>
                <input id="email" type="email" class="form-control form-control-lg" placeholder="you@example.com" required />
              </div>
              <div class="mb-2">
                <label class="form-label d-flex justify-content-between align-items-center">
                  <span>Password</span>
                  <a href="#" class="small text-decoration-none">Forgot?</a>
                </label>
                <input id="password" type="password" class="form-control form-control-lg" placeholder="••••••••" required minlength="3" />
              </div>

              <div id="alertBox" class="alert alert-danger d-none mt-3" role="alert"></div>

              <button id="btnSignIn" type="submit" class="btn btn-accent w-100 mt-3">
                <span class="me-2"><i class="bi bi-box-arrow-in-right"></i></span> Sign In
              </button>
            </form>
          </div>
        </div>

        <div class="text-center mt-3 text-secondary small">
          By signing in you agree to our Terms & Privacy.
        </div>
      </div>
    </div>
  </div>

<script>
  // ====== CONFIG: replace with your backend auth endpoint ======
  const AUTH_API = '/auth/login'; // POST {email,password} -> {token, expiresIn}

  // ====== Helpers ======
  function saveAuth(token, expiresInMs) {
    // store absolute expiry time
    const expiresAt = Date.now() + Number(expiresInMs || 0);
    localStorage.setItem('tp_token', token);
    localStorage.setItem('tp_token_exp', String(expiresAt));
  }
  function showError(msg) {
    const box = document.getElementById('alertBox');
    box.textContent = msg || 'Login failed';
    box.classList.remove('d-none');
  }
  function hideError() {
    document.getElementById('alertBox').classList.add('d-none');
  }

  // ====== Submit handler ======
  document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault(); hideError();
    const btn = document.getElementById('btnSignIn');
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;

    if (!email || !password) { showError('Email and password are required.'); return; }

    btn.disabled = true; btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Signing in…';

    try {
      const res = await fetch(AUTH_API, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });
      const data = await res.json().catch(() => ({}));
      if (!res.ok) throw new Error(data?.message || `HTTP ${res.status}`);

      // Expecting: { token: "JWT...", expiresIn: 86400000 }
      if (!data?.token) throw new Error('Token missing in response');
      saveAuth(data.token, data.expiresIn);

      // Go to dashboard
      window.location.href = 'dashboard.html';
    } catch (err) {
      showError(err.message || 'Login failed, please try again.');
    } finally {
      btn.disabled = false; btn.innerHTML = '<span class="me-2"><i class="bi bi-box-arrow-in-right"></i></span> Sign In';
    }
  });

  // If already logged in and not expired, skip login
  (function maybeAutoRedirect(){
    const t = localStorage.getItem('tp_token');
    const exp = Number(localStorage.getItem('tp_token_exp') || 0);
    if (t && Date.now() < exp) { window.location.href = 'dashboard.html'; }
  })();
</script>
</body>
</html>
