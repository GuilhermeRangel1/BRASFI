import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import {
  ArrowRight,
  BookOpenCheck,
  CalendarDays,
  Check,
  CheckCircle2,
  CircleUserRound,
  Clock3,
  Compass,
  GraduationCap,
  Handshake,
  Leaf,
  Lightbulb,
  LogIn,
  LogOut,
  MessageSquareText,
  Pencil,
  PlayCircle,
  Plus,
  RefreshCw,
  Send,
  Settings,
  Sparkles,
  Trash2,
  UsersRound,
  Video,
  X
} from 'lucide-react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import './styles.css';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';

const views = [
  { id: 'dashboard', label: 'Início', icon: Compass },
  { id: 'learning', label: 'Trilhas', icon: GraduationCap },
  { id: 'events', label: 'Eventos', icon: CalendarDays },
  { id: 'communities', label: 'Comunidades', icon: UsersRound }
];

const profileView = { id: 'profile', label: 'Minha jornada', icon: CircleUserRound };
const adminView = { id: 'admin', label: 'Admin', icon: Settings };

async function api(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers ?? {})
    },
    ...options
  });

  if (response.status === 204) {
    return null;
  }

  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(data.message || 'Não foi possível concluir a ação.');
  }

  return data;
}

async function uploadFile(path, file) {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    credentials: 'include',
    body: formData
  });

  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(data.message || 'Não foi possível enviar o arquivo.');
  }

  return data;
}

function App() {
  const [view, setView] = useState('dashboard');
  const [dashboard, setDashboard] = useState(null);
  const [events, setEvents] = useState([]);
  const [communities, setCommunities] = useState([]);
  const [learningTracks, setLearningTracks] = useState([]);
  const [profile, setProfile] = useState(null);
  const [selectedCommunityId, setSelectedCommunityId] = useState(null);
  const [posts, setPosts] = useState([]);
  const [auth, setAuth] = useState({ authenticated: false, user: null });
  const [authMode, setAuthMode] = useState('login');
  const [authOpen, setAuthOpen] = useState(false);
  const [notice, setNotice] = useState('');
  const [loading, setLoading] = useState(true);

  const selectedCommunity = useMemo(
    () => communities.find((community) => community.id === selectedCommunityId),
    [communities, selectedCommunityId]
  );

  const isAdmin = auth.user?.role === 'ADMIN';
  const canManageCommunities = auth.user?.role === 'ADMIN' || auth.user?.role === 'MANAGER';
  const canAccessAdmin = isAdmin || canManageCommunities;
  const memberViews = auth.authenticated ? [...views, profileView] : views;
  const visibleViews = canAccessAdmin ? [...memberViews, adminView] : memberViews;

  async function loadAll() {
    setLoading(true);
    try {
      const me = await api('/api/v1/auth/me');
      const [dash, allEvents, allCommunities, allLearningTracks, currentProfile] = await Promise.all([
        api('/api/v1/dashboard'),
        api('/api/v1/events'),
        api('/api/v1/communities'),
        api('/api/v1/learning-tracks'),
        me.authenticated ? api('/api/v1/profile') : Promise.resolve(null)
      ]);
      setAuth(me);
      setDashboard(dash);
      setEvents(allEvents);
      setCommunities(allCommunities);
      setLearningTracks(allLearningTracks);
      setProfile(currentProfile);
      setSelectedCommunityId((current) => current ?? allCommunities[0]?.id ?? null);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadAll().catch((error) => setNotice(error.message));
  }, []);

  useEffect(() => {
    if (!notice) {
      return undefined;
    }

    const timer = window.setTimeout(() => setNotice(''), 3600);
    return () => window.clearTimeout(timer);
  }, [notice]);

  useEffect(() => {
    if (view === 'admin' && !canAccessAdmin) {
      setView('dashboard');
    }
  }, [canAccessAdmin, view]);

  useEffect(() => {
    if (view === 'profile' && !auth.authenticated) {
      setView('dashboard');
    }
  }, [auth.authenticated, view]);

  useEffect(() => {
    if (!selectedCommunityId) {
      setPosts([]);
      return undefined;
    }

    let stompClient;
    api(`/api/v1/communities/${selectedCommunityId}/posts`)
      .then(setPosts)
      .catch((error) => setNotice(error.message));

    stompClient = new Client({
      webSocketFactory: () => new SockJS(`${API_BASE}/brasfi-webapp-websocket`),
      reconnectDelay: 4000,
      onConnect: () => {
        stompClient.subscribe(`/topic/${selectedCommunityId}`, (message) => {
          const post = JSON.parse(message.body);
          setPosts((current) => [...current, post]);
        });
      }
    });
    stompClient.activate();

    return () => stompClient.deactivate();
  }, [selectedCommunityId]);

  async function handleLogin(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const body = new URLSearchParams({
      username: form.get('email'),
      password: form.get('password')
    });

    await fetch(`${API_BASE}/login`, {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Requested-With': 'XMLHttpRequest',
        Accept: 'application/json'
      },
      body
    }).then(async (response) => {
      if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'E-mail ou senha inválidos.');
      }
    });

    setAuthOpen(false);
    await loadAll();
  }

  async function handleRegister(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await api('/api/v1/auth/register', {
      method: 'POST',
      body: JSON.stringify({
        nome: form.get('nome'),
        email: form.get('email'),
        cpf: form.get('cpf'),
        senha: form.get('password'),
        idade: Number(form.get('idade'))
      })
    });
    setAuthMode('login');
    setAuthOpen(false);
    setNotice('Cadastro criado. Entre com seu e-mail e senha.');
  }

  async function handleLogout() {
    await fetch(`${API_BASE}/logout`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'X-Requested-With': 'XMLHttpRequest', Accept: 'application/json' }
    });
    setAuth({ authenticated: false, user: null });
    setProfile(null);
    if (view === 'profile') {
      setView('dashboard');
    }
    setNotice('Você saiu da conta.');
  }

  async function createEvent(event) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);
    const created = await api('/api/v1/events', {
      method: 'POST',
      body: JSON.stringify({
        titulo: form.get('titulo'),
        dataEvento: form.get('dataEvento'),
        convidados: form.get('convidados'),
        conteudo: form.get('conteudo'),
        categoria: form.get('categoria'),
        urlVideo: form.get('urlVideo')
      })
    });
    formElement.reset();
    setEvents((current) => sortEvents([...current, created]));
    setNotice('Evento publicado.');
    loadAll().catch((error) => setNotice(error.message));
    return created;
  }

  async function updateEvent(eventId, event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const updated = await api(`/api/v1/events/${eventId}`, {
      method: 'PUT',
      body: JSON.stringify({
        titulo: form.get('titulo'),
        dataEvento: form.get('dataEvento'),
        convidados: form.get('convidados'),
        conteudo: form.get('conteudo'),
        categoria: form.get('categoria'),
        urlVideo: form.get('urlVideo')
      })
    });
    setEvents((current) => sortEvents(current.map((item) => (item.id === eventId ? updated : item))));
    setNotice('Evento atualizado.');
    loadAll().catch((error) => setNotice(error.message));
    return updated;
  }

  async function deleteEvent(eventId) {
    await api(`/api/v1/events/${eventId}`, { method: 'DELETE' });
    setEvents((current) => current.filter((event) => event.id !== eventId));
    setNotice('Evento removido.');
    loadAll().catch((error) => setNotice(error.message));
  }

  async function toggleEventRegistration(eventId, isRegistered) {
    if (!auth.authenticated) {
      setNotice('Entre na sua conta para se inscrever em eventos.');
      return;
    }

    await api(`/api/v1/events/${eventId}/registrations`, {
      method: isRegistered ? 'DELETE' : 'POST'
    });
    setNotice(isRegistered ? 'Inscrição cancelada.' : 'Inscrição confirmada.');
    await loadAll();
  }

  async function createCommunity(event) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);
    const community = await api('/api/v1/communities', {
      method: 'POST',
      body: JSON.stringify({
        nome: form.get('nome'),
        descricao: form.get('descricao'),
        nivelDePermissao: form.get('nivelDePermissao')
      })
    });
    formElement.reset();
    setNotice('Comunidade criada.');
    await loadAll();
    setSelectedCommunityId(community.id);
  }

  async function deleteCommunity(communityId) {
    await api(`/api/v1/communities/${communityId}`, { method: 'DELETE' });
    setNotice('Comunidade removida.');
    await loadAll();
    setSelectedCommunityId((current) => (current === communityId ? null : current));
  }

  async function createLearningTrack(event) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);
    const created = await api('/api/v1/learning-tracks', {
      method: 'POST',
      body: JSON.stringify({
        id: form.get('id'),
        title: form.get('title'),
        level: form.get('level'),
        duration: form.get('duration'),
        description: form.get('description'),
        outcomes: readNamedList(form, 'outcomes[]'),
        resources: readNamedList(form, 'resources[]'),
        steps: readStepFields(form)
      })
    });
    formElement.reset();
    setLearningTracks((current) => [created, ...current.filter((track) => track.id !== created.id)]);
    setNotice('Trilha criada.');
    loadAll().catch((error) => setNotice(error.message));
    return created;
  }

  async function updateLearningTrack(trackId, event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const updated = await api(`/api/v1/learning-tracks/${trackId}`, {
      method: 'PUT',
      body: JSON.stringify({
        id: form.get('id'),
        title: form.get('title'),
        level: form.get('level'),
        duration: form.get('duration'),
        description: form.get('description'),
        outcomes: readNamedList(form, 'outcomes[]'),
        resources: readNamedList(form, 'resources[]'),
        steps: readStepFields(form)
      })
    });
    setLearningTracks((current) => current.map((track) => (track.id === trackId ? updated : track)));
    setNotice('Trilha atualizada.');
    loadAll().catch((error) => setNotice(error.message));
    return updated;
  }

  async function deleteLearningTrack(trackId) {
    await api(`/api/v1/learning-tracks/${trackId}`, { method: 'DELETE' });
    setLearningTracks((current) => current.filter((track) => track.id !== trackId));
    setNotice('Trilha removida.');
    loadAll().catch((error) => setNotice(error.message));
  }

  async function enrollLearningTrack(trackId) {
    if (!auth.authenticated) {
      setNotice('Entre na sua conta para começar uma trilha.');
      setAuthOpen(true);
      return null;
    }

    const updated = await api(`/api/v1/learning-tracks/${trackId}/enrollment`, { method: 'POST' });
    setLearningTracks((current) => current.map((track) => (track.id === trackId ? mergeTrack(track, updated) : track)));
    setNotice('Trilha iniciada.');
    return updated;
  }

  async function saveLearningProgress(trackId, completedSteps) {
    const updated = await api(`/api/v1/learning-tracks/${trackId}/progress`, {
      method: 'PUT',
      body: JSON.stringify({ completedSteps })
    });
    setLearningTracks((current) => current.map((track) => (track.id === trackId ? mergeTrack(track, updated) : track)));
    return updated;
  }

  async function uploadLearningMaterial(file) {
    return uploadFile('/api/v1/uploads/learning-materials', file);
  }

  async function sendPost(event) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);
    await api(`/api/v1/communities/${selectedCommunityId}/posts`, {
      method: 'POST',
      body: JSON.stringify({ mensagem: form.get('mensagem') })
    });
    formElement.reset();
  }

  return (
    <main className="site-shell">
      <header className="site-header">
        <button className="brand site-brand" type="button" onClick={() => setView('dashboard')}>
          <span className="brand-mark"><Leaf size={22} /></span>
          <span>BRASFI</span>
        </button>

        <nav className="nav-list site-nav" aria-label="Principal">
          {visibleViews.map((item) => {
            const Icon = item.icon;
            return (
              <button
                key={item.id}
                className={view === item.id ? 'nav-item active' : 'nav-item'}
                onClick={() => setView(item.id)}
                title={item.label}
              >
                <Icon size={19} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>

        <section className={authOpen ? 'account-panel site-account open' : 'account-panel site-account'}>
          {auth.authenticated ? (
            <>
              <button className="account-name account-button" type="button" onClick={() => setView('profile')}>
                <CircleUserRound size={20} />
                <span>{auth.user.name}</span>
              </button>
              {canAccessAdmin && <span className="role-pill">{roleLabel(auth.user.role)}</span>}
              <button className="ghost-button" onClick={handleLogout}>
                <LogOut size={17} /> Sair
              </button>
            </>
          ) : (
            <>
              <button className="member-trigger" type="button" onClick={() => setAuthOpen((current) => !current)}>
                <CircleUserRound size={18} />
                Área de membros
              </button>
              {authOpen && (
                <div className="auth-popover">
                  <div className="member-copy">
                    <strong>Entre ou crie sua conta</strong>
                    <span>Use a conta para se inscrever em eventos e participar das comunidades.</span>
                  </div>
                  <AuthBox
                    mode={authMode}
                    setMode={setAuthMode}
                    onLogin={handleLogin}
                    onRegister={handleRegister}
                  />
                </div>
              )}
            </>
          )}
        </section>
      </header>

      <section className="workspace site-workspace">
        {view !== 'dashboard' && (
          <header className="topbar">
            <div>
              <p className="eyebrow">Ecossistema de finanças sustentáveis</p>
              <h1>{titleFor(view)}</h1>
            </div>
            <button className="icon-button" onClick={() => loadAll()} title="Atualizar dados">
              <RefreshCw size={18} />
            </button>
          </header>
        )}

        {notice && (
          <button className="notice" onClick={() => setNotice('')}>
            <Check size={18} />
            <span>{notice}</span>
          </button>
        )}

        {loading ? (
          <div className="loading-state">Carregando plataforma...</div>
        ) : (
          <>
            {view === 'dashboard' && <Dashboard dashboard={dashboard} setView={setView} />}
            {view === 'profile' && auth.authenticated && (
              <ProfilePage
                profile={profile}
                setView={setView}
                setSelectedCommunityId={setSelectedCommunityId}
              />
            )}
            {view === 'learning' && (
              <LearningTracks
                tracks={learningTracks}
                auth={auth}
                isAdmin={isAdmin}
                setView={setView}
                createLearningTrack={createLearningTrack}
                updateLearningTrack={updateLearningTrack}
                deleteLearningTrack={deleteLearningTrack}
                enrollLearningTrack={enrollLearningTrack}
                saveLearningProgress={saveLearningProgress}
                uploadLearningMaterial={uploadLearningMaterial}
              />
            )}
            {view === 'events' && (
              <Events
                events={events}
                auth={auth}
                isAdmin={isAdmin}
                createEvent={createEvent}
                updateEvent={updateEvent}
                deleteEvent={deleteEvent}
                toggleEventRegistration={toggleEventRegistration}
              />
            )}
            {view === 'communities' && (
              <Communities
                communities={communities}
                selectedCommunity={selectedCommunity}
                selectedCommunityId={selectedCommunityId}
                setSelectedCommunityId={setSelectedCommunityId}
                posts={posts}
                auth={auth}
                sendPost={sendPost}
                canManageCommunities={canManageCommunities}
                createCommunity={createCommunity}
              />
            )}
            {view === 'admin' && canAccessAdmin && (
              <AdminPanel
                events={events}
                communities={communities}
                learningTracks={learningTracks}
                isAdmin={isAdmin}
                canManageCommunities={canManageCommunities}
                createEvent={createEvent}
                updateEvent={updateEvent}
                deleteEvent={deleteEvent}
                createCommunity={createCommunity}
                deleteCommunity={deleteCommunity}
                createLearningTrack={createLearningTrack}
                updateLearningTrack={updateLearningTrack}
                deleteLearningTrack={deleteLearningTrack}
                uploadLearningMaterial={uploadLearningMaterial}
                setView={setView}
              />
            )}
          </>
        )}
      </section>
    </main>
  );
}

function AuthBox({ mode, setMode, onLogin, onRegister }) {
  const isLogin = mode === 'login';
  const [message, setMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event) {
    setMessage('');
    setSubmitting(true);
    try {
      await (isLogin ? onLogin(event) : onRegister(event));
    } catch (error) {
      setMessage(error.message || 'Não foi possível concluir. Confira os dados e tente novamente.');
    } finally {
      setSubmitting(false);
    }
  }

  function switchMode(nextMode) {
    setMessage('');
    setMode(nextMode);
  }

  return (
    <form className="auth-box" onSubmit={handleSubmit}>
      <div className="auth-tabs">
        <button type="button" className={isLogin ? 'active' : ''} onClick={() => switchMode('login')}>Entrar</button>
        <button type="button" className={!isLogin ? 'active' : ''} onClick={() => switchMode('register')}>Criar</button>
      </div>
      {!isLogin && <input name="nome" placeholder="Nome" required />}
      <input name="email" type="email" placeholder="E-mail" required />
      {!isLogin && <input name="cpf" placeholder="CPF com 11 números" minLength="11" maxLength="11" required />}
      {!isLogin && <input name="idade" type="number" min="1" placeholder="Idade" required />}
      <input name="password" type="password" placeholder="Senha" required />
      {message && <p className="auth-message" role="alert">{message}</p>}
      <button className="primary-button" type="submit" disabled={submitting}>
        <LogIn size={17} /> {submitting ? 'Verificando...' : isLogin ? 'Entrar' : 'Cadastrar'}
      </button>
    </form>
  );
}

function ProfilePage({ profile, setView, setSelectedCommunityId }) {
  if (!profile) {
    return <div className="loading-state">Carregando sua jornada...</div>;
  }

  const startedTracks = profile.trilhas ?? [];
  const registeredEvents = profile.eventos ?? [];
  const recentMessages = profile.mensagensRecentes ?? [];
  const nextTrack = startedTracks.find((track) => !track.progress?.completed) ?? startedTracks[0];

  return (
    <div className="profile-page">
      <section className="profile-hero">
        <div>
          <span className="hero-kicker"><CircleUserRound size={18} /> Perfil do usuário</span>
          <h2>{profile.user.name}</h2>
          <p>{profile.user.email}</p>
          <div className="profile-tags">
            <span>{roleLabel(profile.user.role)}</span>
            <span>{profile.user.idade} anos</span>
          </div>
        </div>
        <div className="profile-highlight">
          <span>Próximo passo</span>
          <strong>{nextTrack ? nextTrack.title : 'Começar uma trilha'}</strong>
          <button className="primary-button" type="button" onClick={() => setView('learning')}>
            <ArrowRight size={17} /> Continuar
          </button>
        </div>
      </section>

      <section className="profile-metrics">
        <MetricCard value={profile.stats.trilhasIniciadas} label="trilhas iniciadas" />
        <MetricCard value={profile.stats.trilhasConcluidas} label="trilhas concluídas" />
        <MetricCard value={profile.stats.eventosInscritos} label="eventos inscritos" />
        <MetricCard value={profile.stats.mensagensEnviadas} label="mensagens enviadas" />
      </section>

      <div className="profile-grid">
        <section className="content-column profile-card">
          <SectionHeader icon={GraduationCap} title="Trilhas em andamento" />
          <div className="profile-track-list">
            {startedTracks.map((track) => (
              <article className="profile-track" key={track.id}>
                <div>
                  <strong>{track.title}</strong>
                  <span>{track.level} · {track.duration}</span>
                </div>
                <div className="progress-bar" aria-label={`Progresso: ${track.progress?.percent ?? 0}%`}>
                  <span style={{ width: `${track.progress?.percent ?? 0}%` }} />
                </div>
                <p>{track.progress?.completedCount ?? 0} de {track.progress?.totalSteps ?? track.steps.length} etapas concluídas.</p>
              </article>
            ))}
            {startedTracks.length === 0 && (
              <EmptyAction
                text="Você ainda não iniciou nenhuma trilha."
                action="Ver trilhas"
                onClick={() => setView('learning')}
              />
            )}
          </div>
        </section>

        <section className="content-column profile-card">
          <SectionHeader icon={CalendarDays} title="Eventos inscritos" />
          <div className="compact-list">
            {registeredEvents.map((event) => (
              <button className="compact-row" key={event.id} onClick={() => setView('events')}>
                <strong>{event.titulo}</strong>
                <span>{formatDate(event.dataEvento)} · {event.categoriaLabel} <ArrowRight size={15} /></span>
              </button>
            ))}
            {registeredEvents.length === 0 && (
              <EmptyAction
                text="Nenhuma inscrição em evento por enquanto."
                action="Ver agenda"
                onClick={() => setView('events')}
              />
            )}
          </div>
        </section>

        <section className="content-column profile-card profile-wide-card">
          <SectionHeader icon={MessageSquareText} title="Atividade nas comunidades" />
          <div className="message-history">
            {recentMessages.map((message) => (
              <button
                className="message-history-item"
                type="button"
                key={message.id}
                onClick={() => {
                  setSelectedCommunityId(message.comunidadeId);
                  setView('communities');
                }}
              >
                <strong>{message.comunidadeNome}</strong>
                <p>{message.mensagem}</p>
                <time>{formatDateTime(message.dataCriacao)}</time>
              </button>
            ))}
            {recentMessages.length === 0 && (
              <EmptyAction
                text="Suas mensagens recentes vão aparecer aqui."
                action="Participar"
                onClick={() => setView('communities')}
              />
            )}
          </div>
        </section>
      </div>
    </div>
  );
}

function MetricCard({ value, label }) {
  return (
    <article className="metric-card">
      <strong>{value}</strong>
      <span>{label}</span>
    </article>
  );
}

function EmptyAction({ text, action, onClick }) {
  return (
    <div className="empty-action">
      <span>{text}</span>
      <button className="ghost-button surface compact" type="button" onClick={onClick}>
        {action} <ArrowRight size={15} />
      </button>
    </div>
  );
}

function Dashboard({ dashboard, setView }) {
  const impactItems = [
    ['15 estados e 10 países', 'presença nacional e conexões internacionais'],
    ['+200', 'profissionais capacitados desde 2020'],
    ['+40', 'conexões qualificadas com o mercado'],
    ['+15', 'instituições parceiras brasileiras e internacionais']
  ];

  const paths = [
    ['Aprenda', 'Siga trilhas guiadas para entrar nos temas centrais de finanças sustentáveis.', 'learning', GraduationCap],
    ['Participe', 'Acompanhe a agenda, inscreva-se em eventos e acesse conteúdos gravados.', 'events', CalendarDays],
    ['Conecte-se', 'Entre nas comunidades para compartilhar dúvidas, referências e oportunidades.', 'communities', UsersRound]
  ];

  const pillars = [
    ['Disseminação de conhecimento', 'Estudos, pesquisas e metodologias ajudam a orientar decisões em ESG, finanças climáticas e investimentos sustentáveis.', Lightbulb],
    ['Formação de lideranças', 'A rede aproxima especialistas, estudantes e decisores para ampliar repertório técnico e capacidade de atuação.', Sparkles],
    ['Viabilização de soluções', 'Projetos, parcerias e suporte técnico conectam conhecimento e capital aos desafios reais da agenda climática.', Handshake]
  ];

  const hubs = [
    ['Hub de Projetos', 'Transforma ideias em iniciativas concretas por meio de grupos de trabalho, pesquisa aplicada, parcerias e produção técnica.'],
    ['Hub de Networking', 'Promove mentorias, eventos, rodas de conversa e articulações regionais para fortalecer talentos e trocas entre gerações.']
  ];

  const homeNav = [
    ['Quem somos', 'home-about'],
    ['Como fazemos', 'home-how'],
    ['Nosso impacto', 'home-impact'],
    ['Depoimentos', 'home-voices']
  ];

  function scrollHomeSection(sectionId) {
    document.getElementById(sectionId)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  return (
    <div className="home-page">
      <nav className="home-local-nav" aria-label="Seções da página inicial">
        {homeNav.map(([label, sectionId]) => (
          <button key={sectionId} onClick={() => scrollHomeSection(sectionId)}>
            {label}
          </button>
        ))}
      </nav>

      <section className="home-hero">
        <div className="wave-line wave-line-one" />
        <div className="wave-line wave-line-two" />
        <div>
          <span className="hero-kicker"><Leaf size={18} /> BRASFI</span>
          <h1>Desenvolvendo lideranças, viabilizando soluções.</h1>
          <p>
            A BRASFI conecta conhecimento, liderança e capital para acelerar finanças e investimentos
            sustentáveis no Brasil. Aqui, a experiência digital organiza trilhas, eventos e comunidades
            para aproximar aprendizado, rede e prática.
          </p>
          <div className="hero-actions">
            <button className="primary-button" onClick={() => setView('learning')}>
              <BookOpenCheck size={18} /> Começar por uma trilha
            </button>
            <button className="secondary-button" onClick={() => scrollHomeSection('home-about')}>
              <Leaf size={18} /> Conhecer a BRASFI
            </button>
          </div>
        </div>
      </section>

      <div className="ticker-strip">
        <span>Aliança Brasileira em Finanças e Investimentos Sustentáveis</span>
        <span>Conhecimento</span>
        <span>Liderança</span>
        <span>Capital para impacto</span>
        <span>Finanças climáticas</span>
      </div>

      <section className="intro-section organic-section" id="home-about">
        <div>
          <p className="eyebrow">Quem somos</p>
          <h2>Uma rede para fortalecer a agenda climática e de investimentos no Brasil.</h2>
        </div>
        <p>
          A BRASFI atua na intersecção entre desenvolvimento de capital humano e estruturação de projetos
          de impacto. Sua atuação combina diversidade regional, articulação multissetorial e conhecimento
          técnico para conectar desafios complexos a lideranças preparadas para construir soluções.
        </p>
      </section>

      <section className="pillar-grid organic-pillars" id="home-how">
        {pillars.map(([title, description, Icon]) => (
          <article className="pillar-card" key={title}>
            <Icon size={42} />
            <h3>{title}</h3>
            <p>{description}</p>
          </article>
        ))}
      </section>

      <section className="hub-section organic-band">
        <div>
          <p className="eyebrow">Como fazemos</p>
          <h2>Dois hubs para transformar rede em ação.</h2>
        </div>
        <div className="hub-grid">
          {hubs.map(([title, description]) => (
            <article className="hub-card" key={title}>
              <h3>{title}</h3>
              <p>{description}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="impact-strip organic-impact" id="home-impact">
        {impactItems.map(([value, label]) => (
          <article className="impact-item" key={label}>
            <strong>{value}</strong>
            <span>{label}</span>
          </article>
        ))}
      </section>

      <section className="path-grid organic-paths">
        {paths.map(([title, description, target, Icon]) => (
          <button className="path-card" key={title} onClick={() => setView(target)}>
            <Icon size={22} />
            <strong>{title}</strong>
            <span>{description}</span>
            <small>Explorar <ArrowRight size={14} /></small>
          </button>
        ))}
      </section>

      <section className="voice-section organic-voice" id="home-voices">
        <div>
          <p className="eyebrow">Depoimentos</p>
          <h2>Vozes que constroem a BRASFI.</h2>
        </div>
        <blockquote>
          Participantes destacam a BRASFI como uma rede que aproxima propósito, carreira,
          conhecimento aplicado e conexões no ecossistema de finanças sustentáveis.
        </blockquote>
      </section>

      <section className="home-feature organic-feature">
        <div>
          <SectionHeader icon={CalendarDays} title="Próximos encontros" />
          <div className="event-list">
            {(dashboard?.proximosEventos ?? []).slice(0, 2).map((event) => <EventItem key={event.id} event={event} />)}
            {(dashboard?.proximosEventos ?? []).length === 0 && <EmptyState text="Nenhum evento futuro cadastrado." />}
          </div>
        </div>
        <div className="community-preview">
          <SectionHeader icon={UsersRound} title="Comunidades em movimento" />
          <div className="community-mini-list">
            {(dashboard?.comunidades ?? []).slice(0, 4).map((community) => (
              <button className="mini-row interactive" key={community.id} onClick={() => setView('communities')}>
                <strong>{community.nome}</strong>
                <span>{community.totalPosts} posts</span>
              </button>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}

function LearningTracks({
  tracks,
  auth,
  isAdmin,
  setView,
  createLearningTrack,
  updateLearningTrack,
  deleteLearningTrack,
  enrollLearningTrack,
  saveLearningProgress,
  uploadLearningMaterial
}) {
  const [selectedTrackId, setSelectedTrackId] = useState(tracks[0]?.id ?? null);
  const [editingTrack, setEditingTrack] = useState(null);
  const [trackModalOpen, setTrackModalOpen] = useState(false);
  const [savingProgress, setSavingProgress] = useState(false);

  const currentTrack = tracks.find((track) => track.id === selectedTrackId) ?? tracks[0];

  useEffect(() => {
    if (!currentTrack && tracks[0]) {
      setSelectedTrackId(tracks[0].id);
    }
  }, [currentTrack, tracks]);

  if (!currentTrack) {
    return (
      <section className="learning-empty">
        <EmptyState text="Nenhuma trilha de aprendizagem disponível." />
        {isAdmin && (
          <button className="primary-button" type="button" onClick={() => setTrackModalOpen(true)}>
            <Plus size={17} /> Criar primeira trilha
          </button>
        )}
        {trackModalOpen && (
            <TrackFormModal
              uploadLearningMaterial={uploadLearningMaterial}
              onClose={() => setTrackModalOpen(false)}
              onSubmit={async (event) => {
              const created = await createLearningTrack(event);
              setSelectedTrackId(created.id);
              setTrackModalOpen(false);
            }}
          />
        )}
      </section>
    );
  }

  const recommendedEvents = currentTrack.recommendedEvents ?? [];
  const recommendedCommunities = currentTrack.recommendedCommunities ?? [];
  const progress = currentTrack.progress ?? { enrolled: false, completedSteps: [], completedCount: 0, totalSteps: currentTrack.steps.length, percent: 0 };
  const completedSteps = progress.completedSteps ?? [];

  async function toggleStep(index) {
    if (!progress.enrolled) {
      const enrolled = await enrollLearningTrack(currentTrack.id);
      if (!enrolled) {
        return;
      }
    }

    setSavingProgress(true);
    try {
      const nextSteps = completedSteps.includes(index)
        ? completedSteps.filter((stepIndex) => stepIndex !== index)
        : [...completedSteps, index];
      await saveLearningProgress(currentTrack.id, nextSteps);
    } finally {
      setSavingProgress(false);
    }
  }

  async function handleTrackSubmit(event) {
    if (editingTrack) {
      const updated = await updateLearningTrack(editingTrack.id, event);
      setSelectedTrackId(updated.id);
    } else {
      const created = await createLearningTrack(event);
      setSelectedTrackId(created.id);
    }
    setTrackModalOpen(false);
    setEditingTrack(null);
  }

  return (
    <>
      {isAdmin && (
        <section className="event-admin-strip">
          <div>
            <span className="hero-kicker"><Settings size={17} /> Modo administrador</span>
            <strong>Crie, edite e acompanhe trilhas de aprendizagem publicadas.</strong>
          </div>
          <button className="primary-button" type="button" onClick={() => {
            setEditingTrack(null);
            setTrackModalOpen(true);
          }}>
            <Plus size={17} /> Nova trilha
          </button>
        </section>
      )}

      <div className="learning-layout">
        <section className="track-picker">
          <SectionHeader icon={GraduationCap} title="Trilhas disponíveis" />
          <div className="track-picker-list">
            {tracks.map((track) => {
              const trackProgress = track.progress ?? { percent: 0, enrolled: false };
              return (
                <button
                  key={track.id}
                  className={currentTrack.id === track.id ? 'track-picker-card active' : 'track-picker-card'}
                  onClick={() => setSelectedTrackId(track.id)}
                >
                  <strong>{track.title}</strong>
                  <span>{track.level} · {track.duration}</span>
                  <small>{trackProgress.enrolled ? `${trackProgress.percent}% concluído` : 'Não iniciada'}</small>
                </button>
              );
            })}
          </div>
        </section>

        <section className="track-hero">
          <div>
            <span className="hero-kicker"><GraduationCap size={18} /> Trilha guiada</span>
            <h2>{currentTrack.title}</h2>
            <p>{currentTrack.description}</p>
            <div className="track-meta">
              <span><Clock3 size={16} /> {currentTrack.duration}</span>
              <span><Sparkles size={16} /> {currentTrack.level}</span>
              <span><CheckCircle2 size={16} /> {currentTrack.steps.length} etapas</span>
            </div>
          </div>
          <div className="track-hero-actions">
            {isAdmin && (
              <>
                <button className="ghost-button surface" type="button" onClick={() => {
                  setEditingTrack(currentTrack);
                  setTrackModalOpen(true);
                }}>
                  <Pencil size={17} /> Editar
                </button>
                <button className="danger-button compact" type="button" onClick={() => deleteLearningTrack(currentTrack.id)}>
                  <Trash2 size={17} /> Remover
                </button>
              </>
            )}
            <button className="primary-button" onClick={() => progress.enrolled ? setView('events') : enrollLearningTrack(currentTrack.id)}>
              {progress.enrolled ? <CalendarDays size={17} /> : <PlayCircle size={17} />}
              {progress.enrolled ? 'Ver eventos da trilha' : auth.authenticated ? 'Começar trilha' : 'Entrar para começar'}
            </button>
          </div>
        </section>

        <section className="track-progress-panel">
          <div>
            <span>Progresso</span>
            <strong>{progress.percent ?? 0}%</strong>
          </div>
          <div className="progress-bar" aria-label={`Progresso da trilha: ${progress.percent ?? 0}%`}>
            <span style={{ width: `${progress.percent ?? 0}%` }} />
          </div>
          <p>{progress.enrolled ? `${progress.completedCount} de ${progress.totalSteps} etapas concluídas.` : 'Comece a trilha para salvar seu avanço.'}</p>
        </section>

        <section className="track-steps">
          <SectionHeader icon={BookOpenCheck} title="Roteiro de aprendizagem" />
          <div className="step-list">
            {currentTrack.steps.map((step, index) => {
              const completed = completedSteps.includes(index);
              return (
                <article className={completed ? 'step-item completed' : 'step-item'} key={`${currentTrack.id}-${step.title}`}>
                  <button className="step-number" type="button" onClick={() => toggleStep(index)} disabled={savingProgress}>
                    {completed ? <Check size={18} /> : index + 1}
                  </button>
                  <div>
                    <h3>{step.title}</h3>
                    <p>{step.description}</p>
                    {step.materials?.length > 0 && (
                      <div className="step-materials">
                        <strong>Materiais desta etapa</strong>
                        <div>
                          {step.materials.map((material) => (
                            isMaterialLink(material) ? (
                              <a key={material} href={materialHref(material)} target="_blank" rel="noreferrer">
                                <BookOpenCheck size={15} /> {materialLabel(material)}
                              </a>
                            ) : (
                              <span key={material}>
                                <BookOpenCheck size={15} /> {material}
                              </span>
                            )
                          ))}
                        </div>
                      </div>
                    )}
                    <button className="step-action" type="button" onClick={() => toggleStep(index)} disabled={savingProgress}>
                      {completed ? 'Concluída' : step.action}
                    </button>
                  </div>
                </article>
              );
            })}
          </div>
        </section>

        <aside className="track-sidebar">
          <section className="content-column">
            <SectionHeader icon={PlayCircle} title="Eventos recomendados" />
            <div className="compact-list">
              {recommendedEvents.map((event) => (
                <button className="compact-row" key={event.id} onClick={() => setView('events')}>
                  <strong>{event.titulo}</strong>
                  <span>{formatDate(event.dataEvento)} <ArrowRight size={15} /></span>
                </button>
              ))}
              {recommendedEvents.length === 0 && <EmptyState text="Nenhum evento futuro disponível." />}
            </div>
          </section>

          <section className="content-column">
            <SectionHeader icon={MessageSquareText} title="Comunidades para praticar" />
            <div className="compact-list">
              {recommendedCommunities.map((community) => (
                <button className="compact-row" key={community.id} onClick={() => setView('communities')}>
                  <strong>{community.nome}</strong>
                  <span>{community.totalPosts} posts <ArrowRight size={15} /></span>
                </button>
              ))}
              {recommendedCommunities.length === 0 && <EmptyState text="Nenhuma comunidade disponível." />}
            </div>
          </section>

          <section className="content-column">
            <SectionHeader icon={CheckCircle2} title="Ao concluir" />
            <ul className="outcome-list">
              {currentTrack.outcomes.map((outcome) => (
                <li key={outcome}><Check size={16} /> {outcome}</li>
              ))}
            </ul>
          </section>

          <section className="content-column">
            <SectionHeader icon={BookOpenCheck} title="Materiais de apoio" />
            <ul className="outcome-list">
              {currentTrack.resources.map((resource) => (
                <li key={resource}><Check size={16} /> {resource}</li>
              ))}
            </ul>
          </section>
        </aside>
      </div>

      {trackModalOpen && (
        <TrackFormModal
          track={editingTrack}
          uploadLearningMaterial={uploadLearningMaterial}
          onClose={() => {
            setTrackModalOpen(false);
            setEditingTrack(null);
          }}
          onSubmit={handleTrackSubmit}
        />
      )}
    </>
  );
}

function TrackFormModal({ track, onClose, onSubmit, uploadLearningMaterial }) {
  const isEditing = Boolean(track);
  const [outcomes, setOutcomes] = useState(track?.outcomes?.length ? track.outcomes : ['']);
  const [resources, setResources] = useState(track?.resources?.length ? track.resources : ['']);
  const [steps, setSteps] = useState(track?.steps?.length
    ? track.steps.map((step) => ({ ...step, materials: step.materials?.length ? step.materials : [''] }))
    : [{ title: '', description: '', action: '', materials: [''] }]);
  const [uploadingStepIndex, setUploadingStepIndex] = useState(null);
  const [uploadError, setUploadError] = useState(null);

  function addOutcome() {
    setOutcomes((current) => [...current, '']);
  }

  function updateOutcome(index, value) {
    setOutcomes((current) => current.map((item, itemIndex) => (itemIndex === index ? value : item)));
  }

  function removeOutcome(index) {
    setOutcomes((current) => current.length === 1 ? [''] : current.filter((_, itemIndex) => itemIndex !== index));
  }

  function addResource() {
    setResources((current) => [...current, '']);
  }

  function updateResource(index, value) {
    setResources((current) => current.map((item, itemIndex) => (itemIndex === index ? value : item)));
  }

  function removeResource(index) {
    setResources((current) => current.length === 1 ? [''] : current.filter((_, itemIndex) => itemIndex !== index));
  }

  function addStep() {
    setSteps((current) => [...current, { title: '', description: '', action: '', materials: [''] }]);
  }

  function updateStep(index, field, value) {
    setSteps((current) => current.map((step, stepIndex) => (
      stepIndex === index ? { ...step, [field]: value } : step
    )));
  }

  function removeStep(index) {
    setSteps((current) => current.length === 1 ? current : current.filter((_, stepIndex) => stepIndex !== index));
  }

  function addStepMaterial(stepIndex) {
    setSteps((current) => current.map((step, index) => (
      index === stepIndex ? { ...step, materials: [...(step.materials ?? ['']), ''] } : step
    )));
  }

  function updateStepMaterial(stepIndex, materialIndex, value) {
    setSteps((current) => current.map((step, index) => (
      index === stepIndex
        ? {
            ...step,
            materials: (step.materials ?? ['']).map((material, currentMaterialIndex) => (
              currentMaterialIndex === materialIndex ? value : material
            ))
          }
        : step
    )));
  }

  function removeStepMaterial(stepIndex, materialIndex) {
    setSteps((current) => current.map((step, index) => {
      if (index !== stepIndex) {
        return step;
      }

      const materials = step.materials ?? [''];
      return {
        ...step,
        materials: materials.length === 1 ? [''] : materials.filter((_, currentMaterialIndex) => currentMaterialIndex !== materialIndex)
      };
    }));
  }

  async function uploadStepMaterial(stepIndex, file) {
    if (!file || !uploadLearningMaterial) {
      return;
    }

    setUploadingStepIndex(stepIndex);
    setUploadError(null);
    try {
      const uploaded = await uploadLearningMaterial(file);
      const materialReference = formatUploadedMaterial(uploaded);
      setSteps((current) => current.map((step, index) => {
        if (index !== stepIndex) {
          return step;
        }

        const filledMaterials = (step.materials ?? []).filter(Boolean);
        return { ...step, materials: [...filledMaterials, materialReference] };
      }));
    } finally {
      setUploadingStepIndex(null);
    }
  }

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section className="event-modal track-modal" role="dialog" aria-modal="true" aria-labelledby="track-modal-title" onMouseDown={(clickEvent) => clickEvent.stopPropagation()}>
        <header className="modal-header">
          <div>
            <span className="hero-kicker"><GraduationCap size={17} /> Trilha</span>
            <h2 id="track-modal-title">{isEditing ? 'Editar trilha' : 'Nova trilha'}</h2>
          </div>
          <button className="icon-button" type="button" onClick={onClose} title="Fechar">
            <X size={18} />
          </button>
        </header>

        <form className="stack-form" onSubmit={onSubmit}>
          <label className="field-label">
            Identificador
            <input name="id" placeholder="ex: financas-sustentaveis" pattern="[a-z0-9]+(-[a-z0-9]+)*" defaultValue={track?.id ?? ''} required />
          </label>
          <label className="field-label">
            Título da trilha
            <input name="title" placeholder="Ex: Fundamentos de finanças sustentáveis" defaultValue={track?.title ?? ''} required />
          </label>
          <div className="form-pair">
            <label className="field-label">
              Nível
              <input name="level" placeholder="Iniciante, Intermediário..." defaultValue={track?.level ?? ''} required />
            </label>
            <label className="field-label">
              Duração
              <input name="duration" placeholder="4 semanas" defaultValue={track?.duration ?? ''} required />
            </label>
          </div>
          <label className="field-label">
            Descrição
            <textarea name="description" placeholder="Explique o propósito da trilha" rows="4" defaultValue={track?.description ?? ''} required />
          </label>

          <DynamicListEditor
            title="Resultados esperados"
            description="O que a pessoa deve conseguir ao concluir a trilha."
            items={outcomes}
            name="outcomes[]"
            placeholder="Ex: Entender os conceitos centrais"
            onAdd={addOutcome}
            onUpdate={updateOutcome}
            onRemove={removeOutcome}
          />

          <DynamicListEditor
            title="Materiais de apoio"
            description="Links, checklists, leituras ou referências úteis."
            items={resources}
            name="resources[]"
            placeholder="Ex: Glossário ESG e finanças sustentáveis"
            onAdd={addResource}
            onUpdate={updateResource}
            onRemove={removeResource}
          />

          <section className="form-section">
            <div className="form-section-heading">
              <div>
                <h3>Etapas da trilha</h3>
                <p>Crie uma etapa por atividade. Cada etapa vira um item marcável na tela de aprendizagem.</p>
              </div>
              <button className="ghost-button surface compact" type="button" onClick={addStep}>
                <Plus size={16} /> Adicionar etapa
              </button>
            </div>
            <div className="step-editor-list">
              {steps.map((step, index) => (
                <article className="step-editor-card" key={`step-editor-${index}`}>
                  <div className="step-editor-header">
                    <strong>Etapa {index + 1}</strong>
                    <button className="danger-button compact" type="button" onClick={() => removeStep(index)} disabled={steps.length === 1}>
                      <Trash2 size={15} /> Remover
                    </button>
                  </div>
                  <input
                    name="stepTitle[]"
                    placeholder="Título da etapa"
                    value={step.title}
                    onChange={(event) => updateStep(index, 'title', event.target.value)}
                    required
                  />
                  <textarea
                    name="stepDescription[]"
                    placeholder="Descrição da etapa"
                    rows="3"
                    value={step.description}
                    onChange={(event) => updateStep(index, 'description', event.target.value)}
                    required
                  />
                  <input
                    name="stepAction[]"
                    placeholder="Chamada para ação"
                    value={step.action}
                    onChange={(event) => updateStep(index, 'action', event.target.value)}
                    required
                  />
                  <div className="step-material-editor">
                    <div className="step-material-editor-header">
                      <span>Materiais da etapa</span>
                      <button className="ghost-button surface compact" type="button" onClick={() => addStepMaterial(index)}>
                        <Plus size={15} /> Adicionar material
                      </button>
                    </div>
                    {(step.materials ?? ['']).map((material, materialIndex) => (
                      <div className="dynamic-field-row" key={`step-${index}-material-${materialIndex}`}>
                        <input
                          name={`stepMaterials[${index}][]`}
                          placeholder="Link, leitura, checklist ou referência"
                          value={material}
                          onChange={(event) => updateStepMaterial(index, materialIndex, event.target.value)}
                        />
                        <button
                          className="danger-button compact"
                          type="button"
                          onClick={() => removeStepMaterial(index, materialIndex)}
                          disabled={(step.materials ?? ['']).length === 1}
                        >
                          <Trash2 size={15} />
                        </button>
                      </div>
                    ))}
                    <label className={uploadingStepIndex === index ? 'file-upload-button uploading' : 'file-upload-button'}>
                      <BookOpenCheck size={16} />
                      {uploadingStepIndex === index ? 'Enviando arquivo...' : 'Anexar arquivo'}
                      <input
                        type="file"
                        onChange={(event) => {
                          uploadStepMaterial(index, event.target.files?.[0]).catch((error) => setUploadError({ stepIndex: index, message: error.message }));
                          event.target.value = '';
                        }}
                        disabled={uploadingStepIndex !== null}
                      />
                    </label>
                    {uploadError?.stepIndex === index && uploadingStepIndex === null && <p className="upload-message">{uploadError.message}</p>}
                  </div>
                </article>
              ))}
            </div>
          </section>

          <div className="modal-actions">
            <button className="ghost-button surface" type="button" onClick={onClose}>Cancelar</button>
            <button className="primary-button" type="submit">
              {isEditing ? <Pencil size={17} /> : <Plus size={17} />}
              {isEditing ? 'Salvar trilha' : 'Publicar trilha'}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}

function DynamicListEditor({ title, description, items, name, placeholder, onAdd, onUpdate, onRemove }) {
  return (
    <section className="form-section">
      <div className="form-section-heading">
        <div>
          <h3>{title}</h3>
          <p>{description}</p>
        </div>
        <button className="ghost-button surface compact" type="button" onClick={onAdd}>
          <Plus size={16} /> Adicionar
        </button>
      </div>
      <div className="dynamic-field-list">
        {items.map((item, index) => (
          <div className="dynamic-field-row" key={`${name}-${index}`}>
            <input
              name={name}
              placeholder={placeholder}
              value={item}
              onChange={(event) => onUpdate(index, event.target.value)}
            />
            <button className="danger-button compact" type="button" onClick={() => onRemove(index)} disabled={items.length === 1}>
              <Trash2 size={15} />
            </button>
          </div>
        ))}
      </div>
    </section>
  );
}

function Events({ events, auth, isAdmin, createEvent, updateEvent, deleteEvent, toggleEventRegistration }) {
  const [editingEvent, setEditingEvent] = useState(null);
  const [eventModalOpen, setEventModalOpen] = useState(false);
  const [eventSaving, setEventSaving] = useState(false);
  const upcoming = events.filter((event) => event.futuro);
  const past = events.filter((event) => !event.futuro);

  function openCreateModal() {
    setEditingEvent(null);
    setEventModalOpen(true);
  }

  function openEditModal(event) {
    setEditingEvent(event);
    setEventModalOpen(true);
  }

  async function handleModalSubmit(event) {
    setEventSaving(true);
    try {
      if (editingEvent) {
        await updateEvent(editingEvent.id, event);
      } else {
        await createEvent(event);
      }
      setEventModalOpen(false);
      setEditingEvent(null);
    } finally {
      setEventSaving(false);
    }
  }

  return (
    <>
      {isAdmin && (
        <section className="event-admin-strip">
          <div>
            <span className="hero-kicker"><Settings size={17} /> Modo administrador</span>
            <strong>Gerencie a agenda sem sair da página de eventos.</strong>
          </div>
          <button className="primary-button" type="button" onClick={openCreateModal}>
            <Plus size={17} /> Novo evento
          </button>
        </section>
      )}

      <div className="split-layout">
        <section className="content-column">
          <SectionHeader icon={CalendarDays} title="Agenda" />
          <div className="event-list">
            {upcoming.map((event) => (
              <EventItem
                key={event.id}
                event={event}
                auth={auth}
                isAdmin={isAdmin}
                onEdit={openEditModal}
                onDelete={deleteEvent}
                toggleEventRegistration={toggleEventRegistration}
              />
            ))}
            {upcoming.length === 0 && <EmptyState text="Nenhum evento futuro cadastrado." />}
          </div>
        </section>

        <section className="content-column">
          <SectionHeader icon={Video} title="Gravados" />
          <div className="event-list">
            {past.map((event) => (
              <EventItem
                key={event.id}
                event={event}
                isAdmin={isAdmin}
                onEdit={openEditModal}
                onDelete={deleteEvent}
              />
            ))}
            {past.length === 0 && <EmptyState text="Nenhum evento gravado cadastrado." />}
          </div>
        </section>
      </div>

      {eventModalOpen && (
        <EventFormModal
          event={editingEvent}
          saving={eventSaving}
          onClose={() => {
            setEventModalOpen(false);
            setEditingEvent(null);
          }}
          onSubmit={handleModalSubmit}
        />
      )}
    </>
  );
}

function EventFormModal({ event, saving, onClose, onSubmit }) {
  const isEditing = Boolean(event);

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section className="event-modal" role="dialog" aria-modal="true" aria-labelledby="event-modal-title" onMouseDown={(clickEvent) => clickEvent.stopPropagation()}>
        <header className="modal-header">
          <div>
            <span className="hero-kicker"><CalendarDays size={17} /> Evento</span>
            <h2 id="event-modal-title">{isEditing ? 'Editar evento' : 'Novo evento'}</h2>
          </div>
          <button className="icon-button" type="button" onClick={onClose} title="Fechar" disabled={saving}>
            <X size={18} />
          </button>
        </header>

        <form className="stack-form" onSubmit={onSubmit}>
          <input name="titulo" placeholder="Título" defaultValue={event?.titulo ?? ''} required />
          <input name="dataEvento" type="date" defaultValue={event?.dataEvento ?? ''} required />
          <input name="convidados" placeholder="Convidados" defaultValue={event?.convidados ?? ''} required />
          <textarea name="conteudo" placeholder="Descrição" rows="4" defaultValue={event?.conteudo ?? ''} required />
          <select name="categoria" defaultValue={event?.categoria ?? 'PALESTRA'}>
            <option value="PALESTRA">Palestra</option>
            <option value="WORKSHOP">Workshop</option>
            <option value="AULA">Aula</option>
          </select>
          <input name="urlVideo" type="url" placeholder="URL do vídeo" defaultValue={event?.urlVideo ?? ''} required />
          <div className="modal-actions">
            <button className="ghost-button surface" type="button" onClick={onClose} disabled={saving}>Cancelar</button>
            <button className="primary-button" type="submit" disabled={saving}>
              {isEditing ? <Pencil size={17} /> : <Plus size={17} />}
              {saving ? 'Salvando...' : isEditing ? 'Salvar alterações' : 'Publicar evento'}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}

function Communities({
  communities,
  selectedCommunity,
  selectedCommunityId,
  setSelectedCommunityId,
  posts,
  auth,
  sendPost,
  canManageCommunities,
  createCommunity
}) {
  return (
    <div className="community-layout">
      <section className="community-list">
        {communities.map((community) => (
          <button
            key={community.id}
            className={selectedCommunityId === community.id ? 'community-button active' : 'community-button'}
            onClick={() => setSelectedCommunityId(community.id)}
          >
            <strong>{community.nome}</strong>
            <span>{community.nivelLabel}</span>
          </button>
        ))}
      </section>

      <section className="chat-panel">
        <SectionHeader icon={MessageSquareText} title={selectedCommunity?.nome ?? 'Comunidade'} />
        <p className="muted">{selectedCommunity?.descricao}</p>
        <div className="post-stream">
          {posts.map((post) => (
            <article className="post-bubble" key={`${post.id}-${post.dataCriacao}`}>
              <strong>{post.autorNome}</strong>
              <p>{post.mensagem}</p>
              <time>{formatDateTime(post.dataCriacao)}</time>
            </article>
          ))}
          {posts.length === 0 && <EmptyState text="Ainda não há mensagens nesta comunidade." />}
        </div>
        {auth.authenticated ? (
          <form className="message-form" onSubmit={sendPost}>
            <input name="mensagem" placeholder="Escreva uma mensagem" required />
            <button className="icon-button filled" type="submit" title="Enviar">
              <Send size={18} />
            </button>
          </form>
        ) : (
          <div className="signin-callout">Entre na sua conta para participar da conversa.</div>
        )}
      </section>

      {canManageCommunities && (
        <section className="editor-panel">
          <SectionHeader icon={Plus} title="Nova comunidade" />
          <form className="stack-form" onSubmit={createCommunity}>
            <input name="nome" placeholder="Nome" required />
            <textarea name="descricao" rows="4" placeholder="Descrição" required />
            <select name="nivelDePermissao" defaultValue="PUBLICA">
              <option value="PUBLICA">Pública</option>
              <option value="APENAS_LIDERES">Apenas líderes</option>
              <option value="PERSONALIZADA">Personalizada</option>
            </select>
            <button className="primary-button" type="submit"><Plus size={17} /> Criar</button>
          </form>
        </section>
      )}
    </div>
  );
}

function AdminPanel({
  events,
  communities,
  learningTracks,
  isAdmin,
  canManageCommunities,
  createEvent,
  deleteEvent,
  createCommunity,
  deleteCommunity,
  createLearningTrack,
  updateLearningTrack,
  deleteLearningTrack,
  uploadLearningMaterial,
  setView
}) {
  const [section, setSection] = useState(isAdmin ? 'events' : 'communities');
  const [editingTrack, setEditingTrack] = useState(null);
  const [trackModalOpen, setTrackModalOpen] = useState(false);

  useEffect(() => {
    if (!isAdmin && section !== 'communities') {
      setSection('communities');
    }
  }, [isAdmin, section]);

  const sections = [
    ...(isAdmin ? [['events', 'Eventos'], ['tracks', 'Trilhas']] : []),
    ...(canManageCommunities ? [['communities', 'Comunidades']] : [])
  ];

  async function handleAdminTrackSubmit(event) {
    if (editingTrack) {
      await updateLearningTrack(editingTrack.id, event);
    } else {
      const created = await createLearningTrack(event);
      setTrackModalOpen(false);
      setEditingTrack(null);
      window.setTimeout(() => setView('learning'), 0);
      return created;
    }
    setTrackModalOpen(false);
    setEditingTrack(null);
    return null;
  }

  return (
    <div className="admin-layout">
      <section className="admin-hero">
        <div>
          <span className="hero-kicker"><Settings size={18} /> Administração</span>
          <h2>Gerencie conteúdo publicado na plataforma.</h2>
          <p>Crie e remova eventos, trilhas e comunidades com dados salvos no backend.</p>
        </div>
      </section>

      <nav className="admin-tabs" aria-label="Áreas administrativas">
        {sections.map(([id, label]) => (
          <button key={id} className={section === id ? 'active' : ''} onClick={() => setSection(id)}>
            {label}
          </button>
        ))}
      </nav>

      {section === 'events' && isAdmin && (
        <section className="admin-grid">
          <article className="admin-editor">
            <SectionHeader icon={CalendarDays} title="Novo evento" />
            <form className="stack-form" onSubmit={createEvent}>
              <input name="titulo" placeholder="Título" required />
              <input name="dataEvento" type="date" required />
              <input name="convidados" placeholder="Convidados" required />
              <textarea name="conteudo" placeholder="Descrição" rows="4" required />
              <select name="categoria" defaultValue="PALESTRA">
                <option value="PALESTRA">Palestra</option>
                <option value="WORKSHOP">Workshop</option>
                <option value="AULA">Aula</option>
              </select>
              <input name="urlVideo" type="url" placeholder="URL do vídeo" required />
              <button className="primary-button" type="submit"><Plus size={17} /> Publicar evento</button>
            </form>
          </article>

          <article className="admin-list">
            <SectionHeader icon={Video} title="Eventos publicados" />
            {events.map((event) => (
              <div className="admin-row" key={event.id}>
                <div>
                  <strong>{event.titulo}</strong>
                  <span>{formatDate(event.dataEvento)} · {event.categoriaLabel} · {event.totalInscritos ?? 0} inscritos</span>
                </div>
                <button className="danger-button" onClick={() => deleteEvent(event.id)} title="Remover evento">
                  <Trash2 size={17} />
                </button>
              </div>
            ))}
            {events.length === 0 && <EmptyState text="Nenhum evento cadastrado." />}
          </article>
        </section>
      )}

      {section === 'tracks' && isAdmin && (
        <>
          <section className="admin-action-panel">
            <div>
              <SectionHeader icon={GraduationCap} title="Trilhas de aprendizagem" />
              <p>Crie trilhas com etapas, materiais, resultados esperados e progresso salvo por usuário.</p>
            </div>
            <button className="primary-button" type="button" onClick={() => {
              setEditingTrack(null);
              setTrackModalOpen(true);
            }}>
              <Plus size={17} /> Nova trilha
            </button>
          </section>

          <article className="admin-list">
            <SectionHeader icon={BookOpenCheck} title="Trilhas publicadas" />
            {learningTracks.map((track) => (
              <div className="admin-row admin-row-expanded" key={track.id}>
                <div>
                  <strong>{track.title}</strong>
                  <span>{track.level} · {track.duration} · {track.steps.length} etapas · {track.progress?.enrolled ? `${track.progress.percent}% em andamento` : 'sem progresso iniciado nesta sessão'}</span>
                </div>
                <div className="admin-row-actions">
                  <button className="ghost-button surface compact" type="button" onClick={() => {
                    setEditingTrack(track);
                    setTrackModalOpen(true);
                  }}>
                    <Pencil size={16} /> Editar
                  </button>
                  <button className="ghost-button surface compact" type="button" onClick={() => setView('learning')}>
                    <ArrowRight size={16} /> Ver
                  </button>
                  <button className="danger-button compact" onClick={() => deleteLearningTrack(track.id)} title="Remover trilha">
                    <Trash2 size={16} /> Remover
                  </button>
                </div>
              </div>
            ))}
            {learningTracks.length === 0 && <EmptyState text="Nenhuma trilha cadastrada." />}
          </article>
          {trackModalOpen && (
            <TrackFormModal
              track={editingTrack}
              uploadLearningMaterial={uploadLearningMaterial}
              onClose={() => {
                setTrackModalOpen(false);
                setEditingTrack(null);
              }}
              onSubmit={async (event) => {
                await handleAdminTrackSubmit(event);
                setTrackModalOpen(false);
                setEditingTrack(null);
              }}
            />
          )}
        </>
      )}

      {section === 'communities' && canManageCommunities && (
        <section className="admin-grid">
          <article className="admin-editor">
            <SectionHeader icon={UsersRound} title="Nova comunidade" />
            <form className="stack-form" onSubmit={createCommunity}>
              <input name="nome" placeholder="Nome" required />
              <textarea name="descricao" rows="4" placeholder="Descrição" required />
              <select name="nivelDePermissao" defaultValue="PUBLICA">
                <option value="PUBLICA">Pública</option>
                <option value="APENAS_LIDERES">Apenas líderes</option>
                <option value="PERSONALIZADA">Personalizada</option>
              </select>
              <button className="primary-button" type="submit"><Plus size={17} /> Criar comunidade</button>
            </form>
          </article>

          <article className="admin-list">
            <SectionHeader icon={MessageSquareText} title="Comunidades publicadas" />
            {communities.map((community) => (
              <div className="admin-row" key={community.id}>
                <div>
                  <strong>{community.nome}</strong>
                  <span>{community.nivelLabel} · {community.totalPosts} posts · {community.totalMembros} membros</span>
                </div>
                <button className="danger-button" onClick={() => deleteCommunity(community.id)} title="Remover comunidade">
                  <Trash2 size={17} />
                </button>
              </div>
            ))}
            {communities.length === 0 && <EmptyState text="Nenhuma comunidade cadastrada." />}
          </article>
        </section>
      )}
    </div>
  );
}

function EventItem({ event, auth, isAdmin = false, onEdit, onDelete, toggleEventRegistration }) {
  const canRegister = !isAdmin && event.futuro && toggleEventRegistration;

  return (
    <article className="event-item">
      <div className="date-box">
        <span>{new Date(`${event.dataEvento}T00:00:00`).toLocaleDateString('pt-BR', { month: 'short' })}</span>
        <strong>{new Date(`${event.dataEvento}T00:00:00`).getDate()}</strong>
      </div>
      <div>
        <h3>{event.titulo}</h3>
        <p>{event.conteudo}</p>
        <div className="meta-line">
          <span>{event.categoriaLabel}</span>
          <span>{event.convidados}</span>
          <span>{event.totalInscritos ?? 0} inscritos</span>
        </div>
        {canRegister && (
          <button
            className={event.inscrito ? 'event-register-button active' : 'event-register-button'}
            onClick={() => toggleEventRegistration(event.id, event.inscrito)}
          >
            <CheckCircle2 size={16} />
            {event.inscrito ? 'Inscrito' : auth?.authenticated ? 'Inscrever-se' : 'Entrar para se inscrever'}
          </button>
        )}
        {isAdmin && (
          <div className="event-admin-actions">
            <button className="ghost-button surface compact" type="button" onClick={() => onEdit(event)}>
              <Pencil size={16} /> Editar
            </button>
            <button className="danger-button compact" type="button" onClick={() => onDelete(event.id)} title="Remover evento">
              <Trash2 size={16} /> Remover
            </button>
          </div>
        )}
      </div>
      <a href={event.urlVideo} target="_blank" rel="noreferrer" className="icon-link" title="Abrir vídeo">
        <Video size={18} />
      </a>
    </article>
  );
}

function SectionHeader({ icon: Icon, title }) {
  return (
    <header className="section-header">
      <Icon size={20} />
      <h2>{title}</h2>
    </header>
  );
}

function EmptyState({ text }) {
  return <div className="empty-state">{text}</div>;
}

function titleFor(view) {
  return {
    dashboard: 'Início BRASFI',
    profile: 'Minha jornada',
    learning: 'Trilhas de aprendizagem',
    events: 'Eventos',
    communities: 'Comunidades',
    admin: 'Administração'
  }[view];
}

function roleLabel(role) {
  return {
    USER: 'Usuário',
    ADMIN: 'Administrador',
    MANAGER: 'Gerente'
  }[role] ?? 'Usuário';
}

function isExternalUrl(value) {
  return /^https?:\/\//i.test(String(value ?? '').trim());
}

function isUploadedMaterial(value) {
  return String(value ?? '').includes('||/uploads/');
}

function isMaterialLink(value) {
  const text = String(value ?? '').trim();
  return isExternalUrl(text) || text.startsWith('/uploads/') || isUploadedMaterial(text);
}

function materialHref(value) {
  const text = String(value ?? '').trim();
  if (isUploadedMaterial(text)) {
    return text.split('||').at(-1);
  }
  return text;
}

function materialLabel(value) {
  const text = String(value ?? '').trim();
  if (isUploadedMaterial(text)) {
    return text.split('||')[0];
  }
  if (text.startsWith('/uploads/')) {
    return decodeURIComponent(text.split('/').at(-1) ?? text);
  }
  return text;
}

function formatUploadedMaterial(uploaded) {
  return `${uploaded.fileName}||${uploaded.url}`;
}

function sortEvents(events) {
  return [...events].sort((first, second) => (
    new Date(`${first.dataEvento}T00:00:00`) - new Date(`${second.dataEvento}T00:00:00`)
  ));
}

function mergeTrack(currentTrack, updatedTrack) {
  return {
    ...currentTrack,
    ...updatedTrack,
    recommendedEvents: updatedTrack.recommendedEvents?.length ? updatedTrack.recommendedEvents : currentTrack.recommendedEvents,
    recommendedCommunities: updatedTrack.recommendedCommunities?.length ? updatedTrack.recommendedCommunities : currentTrack.recommendedCommunities
  };
}

function parseLines(value) {
  return String(value ?? '')
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean);
}

function readNamedList(form, name) {
  return form.getAll(name)
    .map((value) => String(value ?? '').trim())
    .filter(Boolean);
}

function readStepFields(form) {
  const titles = form.getAll('stepTitle[]');
  const descriptions = form.getAll('stepDescription[]');
  const actions = form.getAll('stepAction[]');
  const total = Math.max(titles.length, descriptions.length, actions.length);

  return Array.from({ length: total }, (_, index) => ({
    title: String(titles[index] ?? '').trim(),
    description: String(descriptions[index] ?? '').trim(),
    action: String(actions[index] ?? '').trim(),
    materials: readNamedList(form, `stepMaterials[${index}][]`)
  })).filter((step) => step.title || step.description || step.action);
}

function parseStepLines(value) {
  return parseLines(value).map((line) => {
    const [title = '', description = '', action = ''] = line.split('|').map((part) => part.trim());
    return { title, description, action };
  });
}

function formatDate(value) {
  if (!value) {
    return '';
  }
  return new Date(`${value}T00:00:00`).toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: 'short'
  });
}

function formatDateTime(value) {
  if (!value) {
    return '';
  }
  return new Date(value).toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

createRoot(document.getElementById('root')).render(<App />);
