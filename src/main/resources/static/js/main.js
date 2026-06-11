// ═══════════════════════════════════════════════════════════════════════════
//  CLASS MARKET — Frontend adaptado para a API REST (Spring Boot)
// ═══════════════════════════════════════════════════════════════════════════

const API = '/api';

// ── Auth helpers ─────────────────────────────────────────────────────────
const Auth = {
  getToken:   () => localStorage.getItem('cm_token'),
  getLogado:  () => JSON.parse(localStorage.getItem('cm_logado') || 'null'),
  setSession: (token, usuario) => {
    localStorage.setItem('cm_token', token);
    localStorage.setItem('cm_logado', JSON.stringify(usuario));
  },
  logout: () => {
    localStorage.removeItem('cm_token');
    localStorage.removeItem('cm_logado');
  },
};

// ── Fetch wrapper ─────────────────────────────────────────────────────────
async function apiFetch(path, options = {}) {
  const token = Auth.getToken();
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${API}${path}`, { ...options, headers });

  if (res.status === 204) return null;

  const body = await res.json().catch(() => null);

  if (!res.ok) {
    const msg = body?.erro || `Erro ${res.status}`;
    throw new Error(msg);
  }
  return body;
}

// ── Notificações ─────────────────────────────────────────────────────────
async function carregarBadgeNotif() {
  const logado = Auth.getLogado();
  if (!logado) return;
  try {
    const data = await apiFetch('/notificacoes/nao-lidas');
    const badge = document.getElementById('notifBadge');
    if (badge) {
      badge.textContent = data.total;
      badge.style.display = data.total > 0 ? 'inline-flex' : 'none';
    }
  } catch (_) {}
}

async function abrirPainelNotif() {
  const painel = document.getElementById('notifPanel');
  const lista  = document.getElementById('notifLista');
  if (!painel || !lista) return;

  const aberto = painel.style.display !== 'none';
  painel.style.display = aberto ? 'none' : 'block';
  if (aberto) return;

  lista.innerHTML = '<p style="color:#5e4024;padding:12px">Carregando...</p>';

  try {
    const notifs = await apiFetch('/notificacoes');
    if (!notifs.length) {
      lista.innerHTML = '<p style="color:#5e4024;padding:12px">Nenhuma notificação.</p>';
      return;
    }
    lista.innerHTML = notifs.map(n => `
      <div class="notif-item ${n.lida ? 'lida' : 'nao-lida'}">
        <div class="notif-titulo">${n.titulo}</div>
        <div class="notif-msg">${n.mensagem}</div>
        <div class="notif-data">${n.criadoEm}</div>
      </div>`).join('');
  } catch (_) {
    lista.innerHTML = '<p style="color:#5e4024;padding:12px">Erro ao carregar.</p>';
  }
}

function initNotifListeners() {
  const btn = document.getElementById('btnNotif');
  if (btn) {
    btn.addEventListener('click', e => { e.stopPropagation(); abrirPainelNotif(); });
  }
  const btnLer = document.getElementById('btnLerTodas');
  if (btnLer) {
    btnLer.addEventListener('click', async () => {
      await apiFetch('/notificacoes/ler-todas', { method: 'PATCH' });
      const badge = document.getElementById('notifBadge');
      if (badge) badge.style.display = 'none';
      document.querySelectorAll('.notif-item.nao-lida').forEach(el => {
        el.classList.replace('nao-lida', 'lida');
      });
    });
  }
  // Fechar painel ao clicar fora
  document.addEventListener('click', e => {
    const painel = document.getElementById('notifPanel');
    const btn    = document.getElementById('btnNotif');
    if (painel && !painel.contains(e.target) && e.target !== btn) {
      painel.style.display = 'none';
    }
  });
}

// ── Routing ───────────────────────────────────────────────────────────────
function getRoute() {
  const raw = window.location.hash.slice(1);
  const [route, search] = raw.split('?');
  return { route: route || 'home', params: new URLSearchParams(search || '') };
}

function activateRoute() {
  const validRoutes = [
    'home', 'login', 'cadastro', 'cadastro-produto',
    'catalogo', 'produto', 'avaliacao', 'adm',
    'esqueci-senha', 'redefinir-senha',
  ];
  const { route } = getRoute();
  const active = validRoutes.includes(route) ? route : 'home';
  if (route !== active) { window.location.hash = `#${active}`; return; }

  document.querySelectorAll('.page-section').forEach(s => {
    s.classList.toggle('active', s.dataset.page === active);
  });

  const titles = {
    home: 'Class Market', login: 'Login - Class Market',
    cadastro: 'Cadastro - Class Market', 'cadastro-produto': 'Cadastrar Produto - Class Market',
    catalogo: 'Catálogo - Class Market', produto: 'Produto - Class Market',
    avaliacao: 'Avaliações - Class Market', adm: 'Painel ADM - Class Market',
    'esqueci-senha': 'Esqueci minha senha - Class Market',
    'redefinir-senha': 'Nova senha - Class Market',
  };
  document.title = titles[active] || 'Class Market';

  // Disparar carregamento da página ativa
  pageLoaders[active]?.();
}

function updateNavbarActive() {
  const { route } = getRoute();
  document.querySelectorAll('.navbar-links a, .mobile-menu a').forEach(a => {
    const href = a.getAttribute('href') || '';
    if (!href.startsWith('#')) return;
    const [linkRoute] = href.slice(1).split('?');
    a.classList.toggle('active', linkRoute === route);
  });
}

// ── Card produto HTML ─────────────────────────────────────────────────────
function criarCardProduto(p) {
  const tagClass  = `tag-${p.categoria}`;
  const img       = p.fotoUrl || 'https://via.placeholder.com/400x200';
  const semEstoque = p.estoque === 0;
  const logado    = Auth.getLogado();
  const isOwner   = logado && p.vendedor?.id === logado.id;
  const ownerBtn  = isOwner
    ? `<button class="btn-danger owner-delete"
         onclick="event.stopPropagation();ownerExcluirProduto(${p.id})">Excluir</button>`
    : '';

  if (!logado) {
    return `
      <div class="card-produto locked ${semEstoque ? 'sem-estoque' : ''}"
           onclick="${semEstoque ? '' : "window.location.hash='#login'"}"
           style="${semEstoque ? 'cursor:default;opacity:0.6' : ''}">
        <div class="card-img-wrap">
          <img src="${img}" alt="${p.nome}" loading="lazy">
          ${semEstoque ? '<div class="badge-esgotado">Esgotado</div>' : ''}
          ${semEstoque ? '' : '<div class="locked-overlay">Entre para ver</div>'}
        </div>
        <div class="card-info-locked">
          <h3>${p.nome}</h3>
          <div class="preco">R$ ${parseFloat(p.preco).toFixed(2)}</div>
        </div>
      </div>`;
  }

  return `
    <div class="card-produto ${semEstoque ? 'sem-estoque' : ''}"
         onclick="${semEstoque ? '' : `verProduto(${p.id})`}"
         style="${semEstoque ? 'cursor:default;opacity:0.6' : ''}">
      <div class="card-img-wrap">
        <img src="${img}" alt="${p.nome}" loading="lazy">
        ${semEstoque ? '<div class="badge-esgotado">Esgotado</div>' : ''}
      </div>
      <div class="card-info">
        <span class="tag ${tagClass}">${p.categoria}</span>
        <h3>${p.nome}</h3>
        <div class="preco">R$ ${parseFloat(p.preco).toFixed(2)}</div>
        <div class="local">📍 Bloco ${p.bloco} – Sala ${p.sala}</div>
        <div class="estoque-info">${semEstoque ? '❌ Esgotado' : `✅ ${p.estoque} em estoque`}</div>
        ${ownerBtn}
      </div>
    </div>`;
}

function verProduto(id) {
  localStorage.setItem('cm_produtoSelecionado', id);
  window.location.hash = '#produto';
}

window.ownerExcluirProduto = async function(id) {
  if (!confirm('Deseja realmente excluir seu anúncio?')) return;
  try {
    await apiFetch(`/produtos/${id}`, { method: 'DELETE' });
    window.location.reload();
  } catch (e) { alert(e.message); }
};

// ── Navbar ────────────────────────────────────────────────────────────────
function renderNavbar() {
  const nav = document.getElementById('navbar');
  if (!nav) return;

  document.getElementById('mobileMenu')?.remove();
  document.getElementById('confirmLogoutModal')?.remove();

  const logado = Auth.getLogado();
  const route  = getRoute().route;
  const link   = (href, label, target) =>
    `<a href="${href}" class="${route === target ? 'active' : ''}">${label}</a>`;

  const direita = logado
    ? `<span style="color:#ede0d4;font-size:0.85rem;white-space:nowrap">Olá, ${logado.nome.split(' ')[0]}</span>
       <a href="#cadastro-produto" class="btn-primary" style="padding:0.45rem 1rem;font-size:0.85rem">+ Vender</a>
       ${logado.adm ? `<a href="#adm" class="btn-outline" style="padding:0.4rem 0.9rem;font-size:0.85rem">ADM</a>` : ''}
       <button class="btn-ghost notif-btn" id="btnNotif" title="Notificações">
         🔔<span class="notif-badge" id="notifBadge" style="display:none">0</span>
       </button>
       <button class="btn-ghost" id="btnLogout">Sair</button>`
    : `<a href="#login" class="btn-ghost">Entrar</a>
       <a href="#cadastro" class="btn-primary" style="padding:0.45rem 1.1rem;font-size:0.85rem">Cadastrar</a>`;

  const mobileLinks = logado
    ? `<span class="mobile-nome">Olá, ${logado.nome.split(' ')[0]}</span>
       <a href="#home">🏠 Home</a><a href="#catalogo">📦 Catálogo</a>
       <a href="#catalogo?cat=doces" data-protected="true">🍫 Doces</a>
       <a href="#catalogo?cat=salgados" data-protected="true">🥟 Salgados</a>
       <a href="#catalogo?cat=bebidas" data-protected="true">🥤 Bebidas</a>
       <a href="#avaliacao">⭐ Avaliações</a>
       <div class="mobile-divider"></div>
       <a href="#cadastro-produto">+ Vender produto</a>
       ${logado.adm ? '<a href="#adm">⚙️ Painel ADM</a>' : ''}
       <button id="btnLogoutMobile">Sair</button>`
    : `<a href="#home">🏠 Home</a><a href="#catalogo">📦 Catálogo</a>
       <a href="#catalogo?cat=doces">🍫 Doces</a>
       <a href="#catalogo?cat=salgados">🥟 Salgados</a>
       <a href="#catalogo?cat=bebidas">🥤 Bebidas</a>
       <a href="#avaliacao">⭐ Avaliações</a>
       <div class="mobile-divider"></div>
       <a href="#login">Entrar</a><a href="#cadastro">Cadastrar</a>`;

  nav.innerHTML = `
    <div id="notifPanel" class="notif-panel" style="display:none">
      <div class="notif-panel-header">
        <span>🔔 Notificações</span>
        <button id="btnLerTodas" class="btn-ghost" style="font-size:0.78rem">Marcar todas como lidas</button>
      </div>
      <div id="notifLista" class="notif-lista"><p style="color:#5e4024;padding:12px">Carregando...</p></div>
    </div>
    <a class="navbar-logo" href="#home">Class Market</a>
    <ul class="navbar-links">
      <li>${link('#home', '🏠 Home', 'home')}</li>
      <li><a href="#catalogo" data-protected="true" class="${route === 'catalogo' ? 'active' : ''}">📦 Catálogo</a></li>
      <li class="menu-categorias">
        <a href="#">Categoria ▼</a>
        <ul class="dropdown-menu">
          <li><a href="#catalogo?cat=doces" data-protected="true">🍫 Doces</a></li>
          <li><a href="#catalogo?cat=salgados" data-protected="true">🥟 Salgados</a></li>
          <li><a href="#catalogo?cat=bebidas" data-protected="true">🥤 Bebidas</a></li>
        </ul>
      </li>
      <li><a href="#avaliacao" data-protected="true" class="${route === 'avaliacao' ? 'active' : ''}">⭐ Avaliações</a></li>
    </ul>
    <div class="navbar-right">${direita}</div>
    <button class="navbar-hamburger" id="hamburger" aria-label="Menu">
      <span></span><span></span><span></span>
    </button>`;

  const mobileMenu = document.createElement('div');
  mobileMenu.className = 'mobile-menu'; mobileMenu.id = 'mobileMenu';
  mobileMenu.innerHTML = mobileLinks;
  document.body.appendChild(mobileMenu);

  const logoutModal = document.createElement('div');
  logoutModal.id = 'confirmLogoutModal';
  Object.assign(logoutModal.style, {
    display: 'none', position: 'fixed', inset: '0',
    background: 'rgba(0,0,0,0.45)', zIndex: '9999',
    alignItems: 'center', justifyContent: 'center',
  });
  logoutModal.innerHTML = `
    <div style="background:#fff;color:#333;border-radius:16px;max-width:360px;width:90%;padding:24px;box-shadow:0 18px 45px rgba(0,0,0,0.18)">
      <h2 style="margin:0 0 8px;font-size:1.2rem;color:#222">Confirmar saída</h2>
      <p style="margin:0 0 20px;line-height:1.6;color:#555">Deseja realmente sair da sua conta?</p>
      <div style="display:flex;justify-content:flex-end;gap:0.75rem;flex-wrap:wrap">
        <button id="confirmLogoutCancel" style="background:#fff;color:#333;border:1px solid #ccc;border-radius:8px;padding:0.75rem 1rem;cursor:pointer">Cancelar</button>
        <button id="confirmLogoutConfirm" style="background:#54412e;color:#fff;border:none;border-radius:8px;padding:0.75rem 1rem;cursor:pointer">Sair</button>
      </div>
    </div>`;
  document.body.appendChild(logoutModal);

  // Hamburger
  const hamburger = document.getElementById('hamburger');
  hamburger.addEventListener('click', () => {
    hamburger.classList.toggle('aberto');
    mobileMenu.classList.toggle('aberto');
    document.body.style.overflow = mobileMenu.classList.contains('aberto') ? 'hidden' : '';
  });
  mobileMenu.querySelectorAll('a').forEach(a => {
    a.addEventListener('click', () => {
      hamburger.classList.remove('aberto');
      mobileMenu.classList.remove('aberto');
      document.body.style.overflow = '';
    });
  });

  // Proteção de links
  function protegerNavLinks(e) {
    const link = e.target.closest('a[data-protected="true"]');
    if (link && !Auth.getLogado()) { e.preventDefault(); window.location.hash = '#login'; }
  }
  nav.addEventListener('click', protegerNavLinks);
  mobileMenu.addEventListener('click', protegerNavLinks);

  // Logout
  const doLogout = () => {
    Auth.logout();
    window.location.hash = '#home';
    renderNavbar(); updateNavbarActive(); activateRoute();
  };
  const showModal = () => { logoutModal.style.display = 'flex'; };
  const hideModal = () => { logoutModal.style.display = 'none'; };

  document.getElementById('btnLogout')?.addEventListener('click', showModal);
  document.getElementById('btnLogoutMobile')?.addEventListener('click', showModal);
  document.getElementById('confirmLogoutConfirm')?.addEventListener('click', doLogout);
  document.getElementById('confirmLogoutCancel')?.addEventListener('click', hideModal);
  logoutModal.addEventListener('click', e => { if (e.target === logoutModal) hideModal(); });
}



// ═══════════════════════════════════════════════════════════════════════════
//  PAGE LOADERS — chamados por activateRoute()
// ═══════════════════════════════════════════════════════════════════════════
const pageLoaders = {};

// ── Home ──────────────────────────────────────────────────────────────────
pageLoaders.home = async function() {
  const track   = document.getElementById('carrosselTrack');
  const prevBtn = document.getElementById('prevBtn');
  const nextBtn = document.getElementById('nextBtn');
  if (!track) return;

  track.innerHTML = '<p style="color:#5e4024;padding:1rem">Carregando...</p>';

  let produtos = [];
  try {
    produtos = await apiFetch('/produtos');
  } catch (e) {
    track.innerHTML = `<p style="color:#5e4024">Erro ao carregar produtos: ${e.message}</p>`;
    return;
  }

  let cardIdx = 0;
  const VISIBLE = window.innerWidth < 700 ? 1 : 3;
  const CARD_W  = () => track.querySelector('.card-produto')?.offsetWidth + 20 || 0;

  track.innerHTML = produtos.map(criarCardProduto).join('');

  function atualizarCarrossel() {
    track.style.transform = `translateX(-${cardIdx * CARD_W()}px)`;
    prevBtn.disabled = cardIdx === 0;
    nextBtn.disabled = cardIdx >= produtos.length - VISIBLE;
  }

  prevBtn.onclick = () => { cardIdx = Math.max(0, cardIdx - 1); atualizarCarrossel(); };
  nextBtn.onclick = () => { cardIdx = Math.min(produtos.length - VISIBLE, cardIdx + 1); atualizarCarrossel(); };
  atualizarCarrossel();

  const fill = (id, lista) => {
    const el = document.getElementById(id);
    if (!el) return;
    el.innerHTML = lista.length ? lista.map(criarCardProduto).join('') : '<p style="color:#5e4024">Nenhum produto.</p>';
  };

  fill('listaDoces',    produtos.filter(p => p.categoria === 'doces'));
  fill('listaSalgados', produtos.filter(p => p.categoria === 'salgados'));
  fill('listaBebidas',  produtos.filter(p => p.categoria === 'bebidas'));

  ['secaoDoces','secaoSalgados','secaoBebidas'].forEach(secId => {
    const sec  = document.getElementById(secId);
    const grid = document.getElementById(secId.replace('secao','lista').toLowerCase());
    if (sec && grid && !grid.querySelector('.card-produto')) sec.style.display = 'none';
  });

  // Busca
  const campo   = document.getElementById('campoBusca');
  const results = document.getElementById('searchResults');
  if (campo) {
    let debounce;
    campo.addEventListener('input', function() {
      clearTimeout(debounce);
      const q = this.value.trim();
      if (!q) { results.classList.remove('open'); results.innerHTML = ''; return; }
      debounce = setTimeout(async () => {
        try {
          const res = await apiFetch(`/produtos?busca=${encodeURIComponent(q)}`);
          if (!res.length) {
            results.innerHTML = '<div class="search-empty">Nenhum resultado encontrado.</div>';
          } else {
            results.innerHTML = res.map(p => `
              <div class="search-item" onclick="verProduto(${p.id})">
                <img src="${p.fotoUrl || 'https://via.placeholder.com/44'}" alt="${p.nome}">
                <div>
                  <div class="si-nome">${p.nome}</div>
                  <div class="si-preco">R$ ${parseFloat(p.preco).toFixed(2)} · ${p.categoria}</div>
                </div>
              </div>`).join('');
          }
          results.classList.add('open');
        } catch {}
      }, 300);
    });
    document.addEventListener('click', e => {
      if (!campo.contains(e.target) && !results.contains(e.target)) results.classList.remove('open');
    });
  }

  const btnComprar = document.getElementById('btnComprar');
  if (btnComprar) {
    btnComprar.addEventListener('click', e => {
      if (!Auth.getLogado()) { e.preventDefault(); window.location.hash = '#login'; }
    });
  }
};

// ── Login ─────────────────────────────────────────────────────────────────
pageLoaders.login = function() {
  if (!document.getElementById('btnLogin')) return;
  if (Auth.getLogado()) { window.location.hash = '#home'; return; }

  const btnLogin = document.getElementById('btnLogin');
  const erroEl   = document.getElementById('erroLogin');
  const emailEl  = document.getElementById('email');
  const senhaEl  = document.getElementById('senha');

  const mostrarErro = msg => { erroEl.textContent = msg; erroEl.style.display = 'block'; };

  const tentarLogin = async () => {
    erroEl.style.display = 'none';
    const email = emailEl.value.trim();
    const senha = senhaEl.value;
    if (!email || !senha) { mostrarErro('Preencha e-mail e senha.'); return; }

    btnLogin.disabled = true;
    btnLogin.innerHTML = '<span class="spinner"></span>Entrando...';

    try {
      const res = await apiFetch('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, senha }),
      });
      Auth.setSession(res.token, res.usuario);
      window.location.hash = '#home';
    } catch (e) {
      mostrarErro(e.message);
    } finally {
      btnLogin.disabled = false;
      btnLogin.textContent = 'Entrar';
    }
  };

  // Remover listeners antigos clonando o botão
  const newBtn = btnLogin.cloneNode(true);
  btnLogin.parentNode.replaceChild(newBtn, btnLogin);
  newBtn.addEventListener('click', tentarLogin);
  senhaEl.addEventListener('keydown', e => { if (e.key === 'Enter') tentarLogin(); });
};

// ── Cadastro ──────────────────────────────────────────────────────────────
pageLoaders.cadastro = function() {
  const btn = document.getElementById('btnCadastro');
  if (!btn) return;

  const erroEl = document.getElementById('erroForm');
  const mostrarErro = msg => {
    erroEl.textContent = msg; erroEl.style.display = 'block';
    erroEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
  };

  const newBtn = btn.cloneNode(true);
  btn.parentNode.replaceChild(newBtn, btn);

  newBtn.addEventListener('click', async () => {
    erroEl.style.display = 'none';
    const nome      = document.getElementById('nome').value.trim();
    const email     = document.getElementById('emailCadastro').value.trim();
    const telefone  = document.getElementById('telefone').value.trim();
    const curso     = document.getElementById('curso').value.trim();
    const senha     = document.getElementById('senhaCadastro').value;
    const confirmar = document.getElementById('confirmar').value;

    if (!nome || !email || !senha) { mostrarErro('Preencha os campos obrigatórios.'); return; }
    if (senha !== confirmar)       { mostrarErro('As senhas não coincidem!'); return; }
    if (senha.length < 4)          { mostrarErro('A senha deve ter pelo menos 4 caracteres.'); return; }

    newBtn.disabled = true;
    try {
      await apiFetch('/auth/cadastro', {
        method: 'POST',
        body: JSON.stringify({ nome, email, telefone, curso, senha }),
      });
      alert('Cadastro realizado com sucesso!');
      window.location.hash = '#login';
    } catch (e) {
      mostrarErro(e.message);
    } finally {
      newBtn.disabled = false;
    }
  });
};

// ── Catálogo ──────────────────────────────────────────────────────────────
pageLoaders.catalogo = async function() {
  const grid     = document.getElementById('catalogoGrid');
  const emptyMsg = document.getElementById('emptyMsg');
  const filterBar = document.getElementById('filterBar');
  if (!grid) return;

  const { params } = getRoute();
  let catAtual = params.get('cat') || 'todos';

  grid.innerHTML = '<p style="color:#5e4024;padding:1rem">Carregando...</p>';

  filterBar.querySelectorAll('.filter-btn').forEach(b => {
    b.classList.toggle('active', b.dataset.cat === catAtual);
  });

  const searchInput = document.getElementById('catalogoBusca');

  async function renderGrid() {
    const busca = searchInput?.value.trim() || '';
    const qs    = new URLSearchParams();
    if (catAtual !== 'todos' && catAtual !== 'meus') qs.set('categoria', catAtual);
    if (busca) qs.set('busca', busca);

    let filtrados;
    if (catAtual === 'meus') {
      if (!Auth.getLogado()) { filtrados = []; }
      else {
        try { filtrados = await apiFetch('/produtos/meus'); }
        catch { filtrados = []; }
      }
    } else {
      try { filtrados = await apiFetch(`/produtos?${qs}`); }
      catch { filtrados = []; }
    }

    if (!filtrados.length) {
      grid.innerHTML = ''; emptyMsg.style.display = 'block';
    } else {
      emptyMsg.style.display = 'none';
      grid.innerHTML = filtrados.map(criarCardProduto).join('');
    }
  }

  // Adicionar "Meus produtos" se logado
  const logado = Auth.getLogado();
  if (logado && !filterBar.querySelector('[data-cat="meus"]')) {
    const btn = document.createElement('button');
    btn.className = 'filter-btn'; btn.dataset.cat = 'meus'; btn.textContent = 'Meus produtos';
    filterBar.appendChild(btn);
  }

  filterBar.querySelectorAll('.filter-btn').forEach(btn => {
    const newBtn = btn.cloneNode(true);
    btn.parentNode.replaceChild(newBtn, btn);
    newBtn.addEventListener('click', function() {
      filterBar.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
      this.classList.add('active');
      catAtual = this.dataset.cat;
      renderGrid();
    });
  });

  searchInput?.addEventListener('input', renderGrid);
  await renderGrid();
};

// ── Cadastro de produto ───────────────────────────────────────────────────
pageLoaders['cadastro-produto'] = async function() {
  const btnCad = document.getElementById('btnCadastrar');
  if (!btnCad) return;

  const logado = Auth.getLogado();
  if (!logado) {
    document.getElementById('formWrap').innerHTML = `
      <p style="color:#5e4024;margin-bottom:1rem">Você precisa estar logado para cadastrar um produto.</p>
      <a href="#login" class="btn-primary">Fazer Login</a>`;
    return;
  }

  // Carregar categorias da API
  try {
    const cats = await apiFetch('/categorias');
    const sel  = document.getElementById('categoria');
    if (sel) {
      sel.innerHTML = '<option value="">Selecione...</option>';
      cats.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.id; opt.textContent = c.nome;
        sel.appendChild(opt);
      });
    }
  } catch {}

  // Preview de foto
  const fotoInput = document.getElementById('foto');
  fotoInput?.addEventListener('change', function() {
    const preview = document.getElementById('fotoPreview');
    if (this.files[0]) {
      const reader = new FileReader();
      reader.onload = e => { preview.src = e.target.result; preview.style.display = 'block'; };
      reader.readAsDataURL(this.files[0]);
    } else { preview.style.display = 'none'; }
  });

  const newBtn = btnCad.cloneNode(true);
  btnCad.parentNode.replaceChild(newBtn, btnCad);

  newBtn.addEventListener('click', async () => {
    const nome        = document.getElementById('nomeProduto').value.trim();
    const preco       = document.getElementById('preco').value.trim();
    const estoque     = document.getElementById('estoque').value.trim();
    const categoriaId = document.getElementById('categoria').value;
    const descricao   = document.getElementById('descricao').value.trim();
    const bloco       = document.getElementById('bloco').value.trim();
    const sala        = document.getElementById('sala').value.trim();
    const fotoUrl     = document.getElementById('foto')?.files[0]
      ? await toBase64(document.getElementById('foto').files[0])
      : '';

    if (!nome || !preco || !categoriaId || !bloco || !sala) {
      alert('Preencha os campos obrigatórios (*).'); return;
    }

    newBtn.disabled = true;
    try {
      await apiFetch('/produtos', {
        method: 'POST',
        body: JSON.stringify({
          nome, preco: parseFloat(preco), estoque: parseInt(estoque) || 0,
          categoriaId: parseInt(categoriaId), descricao, bloco, sala, fotoUrl,
        }),
      });
      alert('Produto cadastrado! Aguarde aprovação do ADM.');
      window.location.hash = '#home';
    } catch (e) {
      alert(e.message);
    } finally {
      newBtn.disabled = false;
    }
  });
};

// ── Avaliações ────────────────────────────────────────────────────────────
pageLoaders.avaliacao = async function() {
  const sel = document.getElementById('produtoSelecionado');
  if (!sel) return;

  // Carregar produtos para o select
  try {
    const produtos = await apiFetch('/produtos');
    sel.innerHTML = '<option value="">Selecione um produto...</option>';
    produtos.forEach(p => {
      const opt = document.createElement('option');
      opt.value = p.id; opt.textContent = p.nome;
      sel.appendChild(opt);
    });
  } catch {}

  function initEstrelas(containerId) {
    const container = document.getElementById(containerId);
    container.dataset.nota = 0;
    container.querySelectorAll('span').forEach(star => {
      star.addEventListener('mouseenter', function() {
        const n = Number(this.dataset.nota);
        container.querySelectorAll('span').forEach(s => s.classList.toggle('hover', Number(s.dataset.nota) <= n));
      });
      star.addEventListener('mouseleave', () => container.querySelectorAll('span').forEach(s => s.classList.remove('hover')));
      star.addEventListener('click', function() {
        const n = Number(this.dataset.nota);
        container.dataset.nota = n;
        container.querySelectorAll('span').forEach(s => s.classList.toggle('ativa', Number(s.dataset.nota) <= n));
      });
    });
    return { getNota: () => Number(container.dataset.nota) };
  }

  const site    = initEstrelas('estrelasSite');
  const produto = initEstrelas('estrelasProduto');

  document.getElementById('btnAvaliarSite')?.addEventListener('click', async () => {
    const nota = site.getNota();
    if (!nota) { alert('Selecione uma nota.'); return; }
    const comentario = document.getElementById('comentarioSite').value.trim();
    try {
      await apiFetch('/avaliacoes', { method: 'POST', body: JSON.stringify({ tipo: 'site', nota, comentario }) });
      document.getElementById('comentarioSite').value = '';
      document.getElementById('estrelasSite').dataset.nota = 0;
      document.getElementById('estrelasSite').querySelectorAll('span').forEach(s => s.classList.remove('ativa'));
      alert('Avaliação enviada!');
      renderLista();
    } catch (e) { alert(e.message); }
  });

  document.getElementById('btnAvaliarProduto')?.addEventListener('click', async () => {
    const nota      = produto.getNota();
    const produtoId = Number(sel.value);
    if (!nota)      { alert('Selecione uma nota.'); return; }
    if (!produtoId) { alert('Selecione um produto.'); return; }
    const comentario = document.getElementById('comentarioProduto').value.trim();
    try {
      await apiFetch('/avaliacoes', { method: 'POST', body: JSON.stringify({ tipo: 'produto', nota, comentario, produtoId }) });
      sel.value = '';
      document.getElementById('comentarioProduto').value = '';
      document.getElementById('estrelasProduto').dataset.nota = 0;
      document.getElementById('estrelasProduto').querySelectorAll('span').forEach(s => s.classList.remove('ativa'));
      alert('Avaliação enviada!');
      renderLista();
    } catch (e) { alert(e.message); }
  });

  async function renderLista() {
    const el   = document.getElementById('listaAvaliacoes');
    const lista = await apiFetch('/avaliacoes');
    if (!lista?.length) { el.innerHTML = '<p style="color:#5e4024">Nenhuma avaliação ainda.</p>'; return; }
    el.innerHTML = lista.map(a => `
      <div class="aval-item">
        <div class="aval-item-header">
          <span class="aval-item-nome">${a.usuarioNome || 'Anônimo'}</span>
          <span class="aval-item-estrelas">${'★'.repeat(a.nota)}${'☆'.repeat(5 - a.nota)}</span>
        </div>
        <div class="aval-item-coment">${a.comentario || '<em>Sem comentário</em>'}</div>
        <div class="aval-item-meta">${a.criadoEm} · ${a.tipo === 'site' ? '🌐 Site' : '📦 ' + a.produtoNome}</div>
      </div>`).join('');
  }

  await renderLista();
};

// ── Produto ───────────────────────────────────────────────────────────────
pageLoaders.produto = async function() {
  const layout = document.getElementById('produtoLayout');
  if (!layout) return;

  const produtoId = Number(localStorage.getItem('cm_produtoSelecionado'));
  layout.innerHTML = '<p style="color:#5e4024">Carregando...</p>';

  let produto;
  try {
    produto = await apiFetch(`/produtos/${produtoId}`);
  } catch (e) {
    layout.innerHTML = `<p style="color:#5e4024">Produto não encontrado ou não disponível.</p>`;
    return;
  }

  const semEstoque = produto.estoque === 0;
  const mediaNota  = produto.mediaAvaliacoes;
  const totalAval  = produto.totalAvaliacoes;

  const msgWpp  = encodeURIComponent(`Olá ${produto.vendedor?.nome}! Vi seu produto "${produto.nome}" no Class Market e tenho interesse. Ainda está disponível?`);
  const linkWpp = produto.vendedor?.telefone
    ? `https://wa.me/${produto.vendedor.telefone}?text=${msgWpp}` : null;

  const btnWhatsapp = linkWpp
    ? `<a href="${linkWpp}" target="_blank" class="btn-whatsapp">
         <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 01-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 01-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 012.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0012.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 005.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 00-3.48-8.413z"/></svg>
         Falar com vendedor
       </a>`
    : `<button class="btn-whatsapp btn-whatsapp-disabled" disabled>Vendedor sem contato cadastrado</button>`;

  layout.innerHTML = `
    <div class="produto-img">
      <img src="${produto.fotoUrl || 'https://via.placeholder.com/600x360'}" alt="${produto.nome}">
    </div>
    <div class="produto-info">
      <span class="tag tag-${produto.categoria}">${produto.categoria}</span>
      <h1>${produto.nome}</h1>
      ${mediaNota ? `<div class="produto-rating">
        ${'★'.repeat(Math.round(mediaNota))}${'☆'.repeat(5 - Math.round(mediaNota))}
        <span>${mediaNota} (${totalAval} avaliação${totalAval !== 1 ? 'ões' : ''})</span>
      </div>` : ''}
      <div class="produto-preco">R$ ${parseFloat(produto.preco).toFixed(2)}</div>
      <div class="produto-campo">
        <div class="campo-label">Descrição</div>
        <p>${produto.descricao || 'Sem descrição disponível.'}</p>
      </div>
      <div class="produto-campo">
        <div class="campo-label">Vendedor</div>
        <p>${produto.vendedor?.nome || 'Anônimo'}</p>
      </div>
      <div class="produto-campo">
        <div class="campo-label">Local de retirada</div>
        <p>📍 Bloco ${produto.bloco} • Sala ${produto.sala}</p>
      </div>
      <div class="produto-campo">
        <div class="campo-label">Disponibilidade</div>
        <p class="${semEstoque ? 'estoque-zero' : 'estoque-ok'}">
          ${semEstoque ? '❌ Produto esgotado' : `✅ ${produto.estoque} unidade${produto.estoque !== 1 ? 's' : ''} disponível${produto.estoque !== 1 ? 'is' : ''}`}
        </p>
      </div>
      <div class="produto-actions">
        ${semEstoque
          ? '<button class="btn-primary" disabled style="opacity:0.5;cursor:not-allowed;flex:1">Esgotado</button>'
          : btnWhatsapp}
        <a href="#avaliacao" class="btn-outline-dark">⭐ Avaliar produto</a>
      </div>
    </div>`;

  // Avaliações do produto
  const secAval    = document.getElementById('secAvaliacoes');
  const listaAvalEl = document.getElementById('avaliacoesProduto');
  try {
    const avals = await apiFetch(`/avaliacoes/produto/${produtoId}`);
    if (secAval && listaAvalEl && avals.length) {
      secAval.style.display = 'block';
      listaAvalEl.innerHTML = avals.map(a => `
        <div class="aval-item">
          <div class="aval-item-header">
            <span class="aval-item-nome">${a.usuarioNome || 'Anônimo'}</span>
            <span class="aval-item-estrelas">${'★'.repeat(a.nota)}${'☆'.repeat(5 - a.nota)}</span>
          </div>
          <div class="aval-item-coment">${a.comentario || '<em>Sem comentário</em>'}</div>
          <div class="aval-item-meta">${a.criadoEm}</div>
        </div>`).join('');
    }
  } catch {}
};

// ── ADM ───────────────────────────────────────────────────────────────────
pageLoaders.adm = async function() {
  const el = document.getElementById('admContent');
  if (!el) return;

  const logado = Auth.getLogado();
  if (!logado?.adm) {
    el.innerHTML = `<div class="adm-blocked"><h2>Acesso negado</h2><p>Apenas administradores podem acessar esta página.</p></div>`;
    return;
  }

  el.innerHTML = '<p style="color:#5e4024;padding:1rem">Carregando...</p>';

  let produtos = [], usuarios = [], avaliacoes = [];
  try {
    [produtos, usuarios, avaliacoes] = await Promise.all([
      apiFetch('/adm/produtos'),
      apiFetch('/adm/usuarios'),
      apiFetch('/avaliacoes'),
    ]);
  } catch (e) {
    el.innerHTML = `<p style="color:#c00">Erro ao carregar dados: ${e.message}</p>`;
    return;
  }

  const pendentes = produtos.filter(p => p.status === 'pendente');
  const aprovados = produtos.filter(p => p.status === 'aprovado');
  const negados   = produtos.filter(p => p.status === 'negado');

  el.innerHTML = `
    <div class="adm-stats">
      <div class="adm-stat-card"><div class="adm-stat-num">${produtos.length}</div><div class="adm-stat-label">Produtos</div></div>
      <div class="adm-stat-card"><div class="adm-stat-num">${aprovados.length}</div><div class="adm-stat-label">Aprovados</div></div>
      <div class="adm-stat-card"><div class="adm-stat-num">${pendentes.length}</div><div class="adm-stat-label">Pendentes</div></div>
      <div class="adm-stat-card"><div class="adm-stat-num">${negados.length}</div><div class="adm-stat-label">Negados</div></div>
    </div>

    <div class="adm-grid">
      <div class="card">
        <h2>🕒 Produtos pendentes de aprovação</h2>
        ${pendentes.length ? pendentes.map(p => `
          <div class="adm-row">
            <div>
              <div class="adm-row-name">${p.nome}</div>
              <div class="adm-row-sub">R$ ${parseFloat(p.preco).toFixed(2)} · ${p.categoria} · ${p.vendedor?.nome || '—'} · Estoque: ${p.estoque}</div>
            </div>
            <div class="adm-row-actions">
              <button class="btn-primary" data-action="aprovar" data-id="${p.id}">Aprovar</button>
              <button class="btn-danger"  data-action="negar"   data-id="${p.id}">Negar</button>
            </div>
          </div>`).join('') : '<p style="color:#5e4024">Nenhum produto pendente.</p>'}
      </div>

      <div class="card">
        <h2>📦 Todos os produtos</h2>
        ${produtos.length ? produtos.map(p => `
          <div class="adm-row">
            <div>
              <div class="adm-row-name">${p.nome}</div>
              <div class="adm-row-sub">R$ ${parseFloat(p.preco).toFixed(2)} · ${p.categoria} · ${p.vendedor?.nome || '—'} · Status: <strong>${p.status}</strong></div>
            </div>
            <button class="btn-danger" data-action="excluir-produto" data-id="${p.id}">Excluir</button>
          </div>`).join('') : '<p style="color:#5e4024">Nenhum produto.</p>'}
      </div>

      <div class="card">
        <h2>👤 Usuários cadastrados</h2>
        ${usuarios.length ? usuarios.map(u => `
          <div class="adm-row">
            <div>
              <div class="adm-row-name">${u.nome}</div>
              <div class="adm-row-sub">${u.email} · ${u.curso || '—'}</div>
            </div>
            ${u.adm ? '' : `<button class="btn-danger" data-action="excluir-usuario" data-id="${u.id}">Excluir</button>`}
          </div>`).join('') : '<p style="color:#5e4024">Nenhum usuário.</p>'}
      </div>
    </div>

    <div class="card" style="margin-top:24px">
      <h2>⭐ Avaliações recentes</h2>
      ${avaliacoes.slice(0,20).map(a => `
        <div class="aval-item">
          <div class="aval-item-header">
            <span class="aval-item-nome">${a.usuarioNome || 'Anônimo'}</span>
            <span class="aval-item-estrelas">${'★'.repeat(a.nota)}${'☆'.repeat(5-a.nota)}</span>
            <button class="btn-danger" style="margin-left:auto;font-size:0.75rem;padding:0.25rem 0.6rem"
              data-action="deletar-avaliacao" data-id="${a.id}">🗑 Remover</button>
          </div>
          <div class="aval-item-coment">${a.comentario || '<em>Sem comentário</em>'}</div>
          <div class="aval-item-meta">${a.criadoEm} · ${a.tipo === 'site' ? '🌐 Site' : '📦 ' + a.produtoNome}</div>
        </div>`).join('') || '<p style="color:#5e4024">Nenhuma avaliação.</p>'}
    </div>`;

  // Delegação de eventos no painel ADM
  // Substituir o elemento por um clone para garantir que não haja listeners acumulados
  // de chamadas anteriores de pageLoaders.adm() (evita requisições duplicadas)
  const elClone = el.cloneNode(true);
  el.parentNode.replaceChild(elClone, el);

  elClone.addEventListener('click', async e => {
    const btn = e.target.closest('[data-action]');
    if (!btn) return;
    const { action, id } = btn.dataset;

    // Desabilitar botão durante a requisição para evitar duplo clique
    btn.disabled = true;
    const textoOriginal = btn.textContent;
    btn.textContent = 'Aguarde...';

    try {
      if (action === 'aprovar') {
        await apiFetch(`/adm/produtos/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status: 'aprovado' }) });
      } else if (action === 'negar') {
        await apiFetch(`/adm/produtos/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status: 'negado' }) });
      } else if (action === 'excluir-produto') {
        if (!confirm('Excluir este produto?')) { btn.disabled = false; btn.textContent = textoOriginal; return; }
        await apiFetch(`/adm/produtos/${id}`, { method: 'DELETE' });
      } else if (action === 'excluir-usuario') {
        if (!confirm('Desativar este usuário?')) { btn.disabled = false; btn.textContent = textoOriginal; return; }
        await apiFetch(`/adm/usuarios/${id}`, { method: 'DELETE' });
      } else if (action === 'deletar-avaliacao') {
        const motivo = prompt('Informe o motivo da remoção (será enviado ao usuário):');
        if (!motivo || !motivo.trim()) { btn.disabled = false; btn.textContent = textoOriginal; return; }
        await apiFetch(`/adm/avaliacoes/${id}`, {
          method: 'DELETE',
          body: JSON.stringify({ motivo: motivo.trim() })
        });
      }

      pageLoaders.adm(); // Re-renderizar
    } catch (e) {
      alert('Erro: ' + e.message);
      btn.disabled = false;
      btn.textContent = textoOriginal;
    }
  });
};

// ── Esqueci a senha ────────────────────────────────────────────────────────
pageLoaders['esqueci-senha'] = function() {
  const btn = document.getElementById('btnEnviarCodigo');
  if (!btn) return;

  const newBtn = btn.cloneNode(true);
  btn.parentNode.replaceChild(newBtn, btn);

  newBtn.addEventListener('click', async () => {
    const email = document.getElementById('emailReset')?.value.trim();
    if (!email) { alert('Informe o e-mail.'); return; }
    newBtn.disabled = true;
    try {
      const res = await apiFetch('/auth/esqueci-senha', { method: 'POST', body: JSON.stringify({ email }) });
      alert(res.mensagem);
    } catch (e) { alert(e.message); }
    finally { newBtn.disabled = false; }
  });
};

// ── Redefinir senha ────────────────────────────────────────────────────────
pageLoaders['redefinir-senha'] = function() {
  const btn = document.getElementById('btnRedefinir');
  if (!btn) return;

  // Pegar token da URL: #redefinir-senha?token=xxx
  const token = getRoute().params.get('token') || '';

  const newBtn = btn.cloneNode(true);
  btn.parentNode.replaceChild(newBtn, btn);

  newBtn.addEventListener('click', async () => {
    const novaSenha  = document.getElementById('novaSenha')?.value;
    const confirmar  = document.getElementById('confirmarNova')?.value;
    if (!novaSenha || novaSenha !== confirmar) { alert('As senhas não coincidem.'); return; }
    if (!token) { alert('Token inválido. Solicite um novo link.'); return; }

    newBtn.disabled = true;
    try {
      await apiFetch('/auth/redefinir-senha', { method: 'POST', body: JSON.stringify({ token, novaSenha }) });
      alert('Senha redefinida com sucesso!');
      window.location.hash = '#login';
    } catch (e) { alert(e.message); }
    finally { newBtn.disabled = false; }
  });
};

// ── Inicialização ─────────────────────────────────────────────────────────
renderNavbar();
updateNavbarActive();
activateRoute();
initNotifListeners();
carregarBadgeNotif();
window.addEventListener('hashchange', () => {
  renderNavbar(); updateNavbarActive(); activateRoute();
  initNotifListeners();
  carregarBadgeNotif();
});

// ── Util: File → base64 ────────────────────────────────────────────────────
function toBase64(file) {
  return new Promise((res, rej) => {
    const r = new FileReader();
    r.onload  = e => res(e.target.result);
    r.onerror = rej;
    r.readAsDataURL(file);
  });
}
