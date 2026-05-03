<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <title>TrackingPath – Dashboard</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <!-- Bootstrap + Icons -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <!-- hls.js -->
  <script src="https://cdn.jsdelivr.net/npm/hls.js@latest"></script>
  <style>
    :root { --bg:#0b0f14; --panel:#0f141a; --panel-2:#121923; --line:#1b2632; --text:#d7e1ea; --text-dim:#94a3b8; --accent:#4cc9f0; --success:#23d18b; --radius:14px; --shadow:0 10px 30px rgba(0,0,0,.35); }
    body { background: var(--bg); color: var(--text); }
    .topbar { background: var(--panel); border-bottom: 1px solid var(--line); }
    .brand { font-weight:600; letter-spacing:.3px; background:linear-gradient(90deg,#4cc9f0,#a78bfa); -webkit-background-clip:text; background-clip:text; color:transparent; }
    .layout { min-height: calc(100vh - 60px); display:flex; gap:12px; }
    .sidebar { width:260px; background:linear-gradient(180deg,var(--panel),var(--panel-2)); border-right:1px solid var(--line); box-shadow:var(--shadow); }
    .sidebar-header { padding:14px 16px; border-bottom:1px solid var(--line); display:flex; align-items:center; justify-content:space-between; }
    .chip { background: rgba(76,201,240,.12); color:#4cc9f0; padding:2px 10px; border-radius:999px; font-size:12px; border:1px solid rgba(76,201,240,.25); }
    .channel-list { padding:10px; display:grid; gap:8px; }
    .channel-item { background:#0e141c; border:1px solid var(--line); border-radius:12px; padding:10px 12px; cursor:pointer; display:flex; align-items:center; justify-content:space-between; gap:10px; transition:transform .08s ease, border-color .15s ease, background .15s ease; }
    .channel-item:hover { transform: translateY(-1px); border-color: rgba(76,201,240,.45); background:#0d1621; }
    .channel-item.active { border-color:#4cc9f0; background:#0d1621; }
    .channel-left { display:flex; align-items:center; gap:10px; }
    .channel-dot { width:10px; height:10px; border-radius:50%; background:#475569; box-shadow:0 0 0 2px rgba(148,163,184,.25) inset; }
    .channel-item.live .channel-dot { background: var(--success); box-shadow: 0 0 10px rgba(35,209,139,.5); }
    .channel-title { font-weight:600; }
    .channel-sub { color:var(--text-dim); font-size:12px; }
    .content { flex:1; padding:14px; }
    .grid { display:grid; gap:12px; }
    .tile { position:relative; background:#000; border:1px solid var(--line); border-radius:var(--radius); overflow:hidden; box-shadow:var(--shadow); }
    .tile video { width:100%; height:100%; aspect-ratio:16/9; object-fit:contain; background:#000; }
    .tile .badge-top { position:absolute; top:10px; left:10px; z-index:2; background:rgba(0,0,0,.55); backdrop-filter: blur(4px); padding:4px 10px; border-radius:999px; font-size:12px; border:1px solid rgba(255,255,255,.15); }
    .tile .status-dot { display:inline-block; width:8px; height:8px; border-radius:50%; margin-right:6px; background:#64748b; }
    .tile.live .status-dot { background:#23d18b; box-shadow:0 0 8px rgba(35,209,139,.6); }
    .tile .hud { position:absolute; left:0; right:0; bottom:0; z-index:2; background:linear-gradient(to top, rgba(0,0,0,.8), rgba(0,0,0,0)); padding:8px 10px; display:flex; gap:10px; align-items:center; justify-content:space-between; }
    .hud-left, .hud-right { display:flex; align-items:center; gap:10px; }
    .select-stream { min-width:118px; background:#0e141c; color:#e5e7eb; border:1px solid var(--line); }
    .btn-ghost { background:rgba(255,255,255,.06); border:1px solid rgba(255,255,255,.15); color:#e5e7eb; }
    .btn-ghost:hover { background:rgba(255,255,255,.12); color:#fff; }
    .btn-ghost i { transition: transform .08s ease; }
    .btn-ghost:hover i { transform: scale(1.05); }
    .vol { width:120px; accent-color:#4cc9f0; }
    .empty-hint { position:absolute; inset:0; display:flex; align-items:center; justify-content:center; color:#94a3b8; font-size:14px; letter-spacing:.2px; }
  </style>
</head>
<body>

<!-- Topbar -->
<div class="topbar px-3 py-2 d-flex align-items-center justify-content-between">
  <div class="d-flex align-items-center gap-3">
    <div class="brand">TrackingPath Dashboard</div>
  </div>
  <div class="d-flex align-items-center gap-2">
    <span class="text-secondary">Screens</span>
    <select id="screenCount" class="form-select form-select-sm bg-dark text-light" style="width:110px">
      <option value="2">2</option>
      <option value="4" selected>4</option>
      <option value="8">8</option>
      <option value="16">16</option>
    </select>
    <button id="btnLogout" class="btn btn-sm btn-outline-light ms-2"><i class="bi bi-box-arrow-right me-1"></i> Logout</button>
  </div>
</div>

<div class="layout">
  <!-- Sidebar -->
  <aside class="sidebar">
    <div class="sidebar-header">
      <div>
        <div class="fw-semibold">Channels</div>
        <div class="channel-sub">Select to start streaming</div>
      </div>
      <span id="channelCountChip" class="chip">0</span>
    </div>
    <div id="channelList" class="channel-list"></div>
  </aside>

  <!-- Main -->
  <main class="content">
    <div id="grid" class="grid"></div>
  </main>
</div>

<script>
/* ==================== AUTH GUARD ==================== */
function getToken() {
  const t = localStorage.getItem('tp_token');
  const exp = Number(localStorage.getItem('tp_token_exp') || 0);
  if (!t || Date.now() >= exp) return null;
  return t;
}
(function guard(){
  const tok = getToken();
  if (!tok) { window.location.replace('login.html'); }
})();
document.getElementById('btnLogout').addEventListener('click', () => {
  localStorage.removeItem('tp_token'); localStorage.removeItem('tp_token_exp');
  window.location.replace('login.html');
});

/* ==================== CONFIG (your API + device constants) ==================== */
const API_BASE   = ''; // same-origin. If needed: 'http://103.171.96.229:8085'
const WS_BASE    = (location.protocol === 'https:' ? 'wss://' : 'ws://') + location.host + '/ws/live';

const SIM        = '240077033749';
const DEVICE_ID  = '163';
const TRANSPORT  = 'tcp';
const MODE       = 'vendor';
// We'll pass streamType based on tile.profile: 'main' or 'sub'

/* Attach Authorization header to fetch (JWT from login) */
async function authFetch(url, options={}) {
  const tok = getToken(); if (!tok) { window.location.replace('login.html'); return; }
  const headers = new Headers(options.headers || {});
  headers.set('Authorization', `Bearer ${tok}`);
  return fetch(url, { ...options, headers });
}

/* Build WS URL with token as query (server should verify) */
function wsUrlWithToken() {
  const tok = getToken(); if (!tok) return WS_BASE;
  const u = new URL(WS_BASE); u.searchParams.set('token', tok); return u.toString();
}

/* ==================== STATE ==================== */
let tiles = []; let nextSlot = 0;
let channelToSlot = new Map(); let streamToSlot = new Map();
const gridEl = document.getElementById('grid');
const channelListEl = document.getElementById('channelList');
const channelCountChip = document.getElementById('channelCountChip');
const screenCountEl = document.getElementById('screenCount');

/* ==================== LAYOUT HELPERS ==================== */
function setGridColumns(n){ const map={2:2,4:2,8:4,16:4}; gridEl.style.gridTemplateColumns = `repeat(${map[n]||Math.ceil(Math.sqrt(n))},1fr)`; }

/* ==================== TILES ==================== */
function buildTiles(n){
  tiles.forEach(t=>destroyTile(t)); tiles=[]; gridEl.innerHTML=''; setGridColumns(n);
  for(let i=0;i<n;i++){
    const tile=document.createElement('div'); tile.className='tile'; tile.dataset.slot=i;
    const badge=document.createElement('div'); badge.className='badge-top'; badge.innerHTML=`<span class="status-dot"></span>#${i+1} • idle`;
    const video=document.createElement('video'); video.playsInline=true; video.autoplay=true; video.muted=true; video.controls=false;
    const hud=document.createElement('div'); hud.className='hud';
    const left=document.createElement('div'); left.className='hud-left';
    const right=document.createElement('div'); right.className='hud-right';
    const select=document.createElement('select'); select.className='form-select form-select-sm select-stream'; select.innerHTML=`<option value="main">Main Stream</option><option value="sub" selected>Sub Stream</option>`;
    const muteBtn=document.createElement('button'); muteBtn.type='button'; muteBtn.className='btn btn-sm btn-ghost'; muteBtn.innerHTML=`<i class="bi bi-volume-mute"></i>`;
    const vol=document.createElement('input'); vol.type='range'; vol.min=0; vol.max=1; vol.step=0.01; vol.value=0.5; vol.className='form-range vol'; video.volume=0.5;
    const fsBtn=document.createElement('button'); fsBtn.type='button'; fsBtn.className='btn btn-sm btn-ghost'; fsBtn.title='Full screen'; const fsIcon=document.createElement('i'); fsIcon.className='bi bi-arrows-fullscreen'; fsBtn.appendChild(fsIcon);
    const stopBtn=document.createElement('button'); stopBtn.type='button'; stopBtn.className='btn btn-sm btn-ghost'; stopBtn.title='Stop'; stopBtn.innerHTML=`<i class="bi bi-stop-circle"></i>`;
    left.append(select, muteBtn, vol); right.append(fsBtn, stopBtn); hud.append(left,right);
    const hint=document.createElement('div'); hint.className='empty-hint'; hint.textContent='Pick a channel on the left…';
    tile.append(badge, video, hud, hint); gridEl.appendChild(tile);
    const t={el:tile, video, badge, hint, select, muteBtn, vol, fsBtn, fsIcon, hls:null, assignedChannel:null, profile:'sub', currentStreamId:null, pendingHlsUrl:null}; tiles.push(t);

    select.addEventListener('change', ()=>{
      t.profile = select.value; // 'main' | 'sub'
      if (t.assignedChannel) {
        startLiveForTile(t.assignedChannel, t, /*slot*/ i); // re-call API with new streamType
        setBadge(t, `${t.assignedChannel} (${t.profile}) • requesting…`, false);
      }
    });
    muteBtn.addEventListener('click', async()=>{ video.muted=!video.muted; muteBtn.innerHTML=video.muted?`<i class="bi bi-volume-mute"></i>`:`<i class="bi bi-volume-up"></i>`; await safePlay(video); });
    vol.addEventListener('input', async()=>{ const v=parseFloat(vol.value); video.volume=v; if(v>0 && video.muted){ video.muted=false; muteBtn.innerHTML=`<i class="bi bi-volume-up"></i>`; } await safePlay(video); });
    stopBtn.addEventListener('click', ()=>{ destroyTile(t); setBadge(t,'idle',false); t.hint.textContent='Stopped. Pick a channel on the left…'; t.assignedChannel=null; t.currentStreamId=null; t.pendingHlsUrl=null; });
    fsBtn.addEventListener('click', ()=>toggleFullscreen(t)); video.addEventListener('dblclick', ()=>toggleFullscreen(t));
  }
}
function destroyTile(t){ if(!t)return; if(t.hls){try{t.hls.destroy();}catch{} t.hls=null;} if(t.video){try{t.video.pause();}catch{} t.video.removeAttribute('src'); t.video.load();} t.el.classList.remove('live'); }
function setBadge(t,text,live=false){ t.el.classList.toggle('live', !!live); t.badge.innerHTML=`<span class="status-dot"></span>${text}`; }

/* ==================== SIDEBAR ==================== */
function buildChannelList(count){
  channelListEl.innerHTML=''; for(let i=1;i<=count;i++){ const ch=`CH${i}`; const item=document.createElement('div'); item.className='channel-item'; item.dataset.channel=ch;
    const left=document.createElement('div'); left.className='channel-left'; const dot=document.createElement('span'); dot.className='channel-dot';
    const text=document.createElement('div'); text.innerHTML=`<div class="channel-title">${ch}</div><div class="channel-sub">Click to start</div>`; left.append(dot,text);
    const right=document.createElement('div'); right.innerHTML=`<i class="bi bi-play-fill text-success"></i>`; item.append(left,right); channelListEl.appendChild(item);
    item.addEventListener('click', ()=>onChannelClick(item)); }
  channelCountChip.textContent=String(count);
}
function markChannelActive(el, active){ [...channelListEl.children].forEach(x=>x.classList.remove('active')); if(active) el.classList.add('active'); }

/* ==================== INTERACTIONS ==================== */
function onChannelClick(itemEl){
  const label=itemEl.dataset.channel; // "CH1"
  markChannelActive(itemEl,true);

  let slot=channelToSlot.get(label); if(slot==null){ slot=nextSlot; nextSlot=(nextSlot+1)%tiles.length; }
  const t=tiles[slot];
  t.assignedChannel = label;
  channelToSlot.set(label, slot);

  setBadge(t, `${label} (${t.profile}) • requesting…`, false);
  t.hint.textContent='Waiting for stream…';

  startLiveForTile(label, t, slot);
}

/* ==================== CALL YOUR START API ==================== */
function parseChannelNumber(label){ // "CH1" -> 1
  const m = String(label||'').match(/\d+/);
  return m ? parseInt(m[0],10) : 1;
}
function buildStartUrl(chLabel, streamType){ // streamType: 'main'|'sub'
  const channel = parseChannelNumber(chLabel);
  const url = new URL('/live/start', API_BASE || window.location.origin);
  url.searchParams.set('sim', SIM);
  url.searchParams.set('deviceId', DEVICE_ID);
  url.searchParams.set('channel', String(channel));
  url.searchParams.set('streamType', streamType);   // server maps 'sub'/'main' -> 1/0
  url.searchParams.set('transport', TRANSPORT);
  url.searchParams.set('mode', MODE);
  return url.toString();
}

/** Calls /live/start, stores streamId + HLS URL in tile, and waits for on_publish to actually play */
async function startLiveForTile(chLabel, tile, slotIdx){
  const url = buildStartUrl(chLabel, tile.profile || 'sub');
  try{
    const res = await authFetch(url, { method:'GET' });
    const data = await res.json();

    // Expecting your shape (example in your message)
    // { success:true, streamId:"240077033749_ch1", player:{ hlsA:"...", hlsB:"..." }, ... }
    tile.currentStreamId = data?.streamId || null;
    tile.pendingHlsUrl   = data?.player?.hlsB || data?.player?.hlsA || null;

    // Map stream_profile to slot so handleOnPublish can find it
    if (tile.currentStreamId) {
      streamToSlot.set(`${tile.currentStreamId}_${tile.profile}`, slotIdx);
    }

    // Optional: if you prefer auto-start without waiting WS, uncomment:
    // if (tile.pendingHlsUrl) playHlsInTile(slotIdx, tile.pendingHlsUrl);

  }catch(e){
    console.error('start live failed', e);
    setBadge(tile, `${chLabel} (${tile.profile}) • request failed`, false);
    tile.hint.textContent = 'Backend request failed.';
  }
}

/* ==================== PLAYBACK ==================== */
function buildHlsUrlFromStream(streamId, profile){ // fallback builder if WS has no URL
  // If your ZLM exposes HLS at /hls/rtp/<stream>/live.m3u8, you can mirror that here if needed.
  return `/hls/rtp/${streamId}/live.m3u8`; // adjust if you proxy ZLM
}
async function safePlay(video){ try{ await video.play(); }catch{} }
function playHlsInTile(slot, hlsUrl){
  const t=tiles[slot]; if(!t)return;
  if(t.hls){ try{t.hls.destroy();}catch{} t.hls=null; }
  const video=t.video;

  if(video.canPlayType('application/vnd.apple.mpegurl')){ video.src=hlsUrl; safePlay(video); }
  else if(Hls.isSupported()){ const hls=new Hls({lowLatencyMode:true, maxLiveSyncPlaybackRate:1.5, backBufferLength:30}); hls.loadSource(hlsUrl); hls.attachMedia(video); t.hls=hls; hls.on(Hls.Events.MANIFEST_PARSED, ()=>safePlay(video)); }
  else { t.hint.textContent='HLS not supported'; return; }
  t.hint.textContent='';
}

/* ==================== WEBSOCKET ==================== */
let ws;
function connectWS(){
  ws = new WebSocket(wsUrlWithToken());
  ws.onopen = () => console.log('WS connected');
  ws.onclose = () => { console.warn('WS closed; retrying in 3s'); setTimeout(connectWS,3000); };
  ws.onerror = (e) => console.error('WS error', e);
  ws.onmessage = (evt) => {
    try{
      const msg=JSON.parse(evt.data);
      if(msg.type==='on_publish') handleOnPublish(msg);
    }catch(e){ console.error('WS parse error', e); }
  };
}

/* Accept either {stream: "..."} or {streamId: "..."} and optional hls/hlsA/hlsB */
function handleOnPublish(e){
  const stream = e.stream || e.streamId; // "240077033749_ch1"
  if (!stream) return;

  // Figure out slot:
  // 1) if a tile declared currentStreamId == stream, use it
  let slot = tiles.findIndex(t => t.currentStreamId === stream);
  // 2) else if we mapped in streamToSlot (stream_profile), try both profiles
  if (slot < 0) slot = streamToSlot.get(`${stream}_sub`);
  if (slot == null || slot < 0) slot = streamToSlot.get(`${stream}_main`);
  // 3) else fall back round-robin
  if (slot == null || slot < 0) { slot = nextSlot; nextSlot=(nextSlot+1)%tiles.length; }

  const t = tiles[slot];
  t.currentStreamId = stream;

  // Pick the best URL to play
  let url = t.pendingHlsUrl
         || e.hls
         || e.hlsB
         || e.hlsA
         || (e.player && (e.player.hlsB || e.player.hlsA))
         || buildHlsUrlFromStream(stream, t.profile || 'sub');

  setBadge(t, `${t.assignedChannel || stream} (${t.profile || 'sub'})`, true);
  playHlsInTile(slot, url);

  // Mark sidebar live
  [...channelListEl.children].forEach(el => {
    if (el.dataset.channel === t.assignedChannel) el.classList.add('live');
  });
}

/* ==================== FULLSCREEN HELPERS ==================== */
function isElementFullscreen(el){ return document.fullscreenElement===el || document.webkitFullscreenElement===el || document.msFullscreenElement===el; }
async function requestElFullscreen(el){ if(el.requestFullscreen) return el.requestFullscreen(); if(el.webkitRequestFullscreen) return el.webkitRequestFullscreen(); if(el.msRequestFullscreen) return el.msRequestFullscreen(); }
async function exitFullscreen(){ if(document.exitFullscreen) return document.exitFullscreen(); if(document.webkitExitFullscreen) return document.webkitExitFullscreen(); if(document.msExitFullscreen) return document.msExitFullscreen(); }
function toggleFullscreen(t){
  const tileEl=t.el, videoEl=t.video;
  const isIOSMobile=/iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream;
  if(isIOSMobile && typeof videoEl.webkitEnterFullscreen==='function'){ try{ videoEl.webkitEnterFullscreen(); }catch{} return; }
  if(isElementFullscreen(tileEl)) exitFullscreen(); else requestElFullscreen(tileEl);
}
['fullscreenchange','webkitfullscreenchange','msfullscreenchange'].forEach(ev=>{
  document.addEventListener(ev, ()=>{
    tiles.forEach(t=>{
      const i=t?.fsBtn?.querySelector('i'); if(!i) return;
      if(isElementFullscreen(t.el)){ i.classList.remove('bi-arrows-fullscreen'); i.classList.add('bi-fullscreen-exit'); t.fsBtn.title='Exit full screen'; }
      else { i.classList.remove('bi-fullscreen-exit'); i.classList.add('bi-arrows-fullscreen'); t.fsBtn.title='Full screen'; }
    });
  });
});

/* ==================== INIT ==================== */
function setGridColumns(n){ const map={2:2,4:2,8:4,16:4}; gridEl.style.gridTemplateColumns = `repeat(${map[n]||Math.ceil(Math.sqrt(n))},1fr)`; }
function init(){
  const n=parseInt(screenCountEl.value,10); buildTiles(n); buildChannelList(n); connectWS();
  screenCountEl.addEventListener('change', ()=>{ const v=parseInt(screenCountEl.value,10); buildTiles(v); buildChannelList(v); channelToSlot.clear(); streamToSlot.clear(); nextSlot=0; });
}
init();
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
