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
  Leaf,
  LogIn,
  LogOut,
  MessageSquareText,
  PlayCircle,
  Plus,
  RefreshCw,
  Send,
  ShieldCheck,
  Sparkles,
  UsersRound,
  Video
} from 'lucide-react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import './styles.css';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';

const views = [
  { id: 'dashboard', label: 'Painel', icon: Compass },
  { id: 'learning', label: 'Trilhas', icon: GraduationCap },
  { id: 'events', label: 'Eventos', icon: CalendarDays },
  { id: 'communities', label: 'Comunidades', icon: UsersRound },
  { id: 'about', label: 'Sobre', icon: Leaf }
];

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

function App() {
  const [view, setView] = useState('dashboard');
  const [dashboard, setDashboard] = useState(null);
  const [events, setEvents] = useState([]);
  const [communities, setCommunities] = useState([]);
  const [learningTracks, setLearningTracks] = useState([]);
  const [selectedCommunityId, setSelectedCommunityId] = useState(null);
  const [posts, setPosts] = useState([]);
  const [auth, setAuth] = useState({ authenticated: false, user: null });
  const [authMode, setAuthMode] = useState('login');
  const [notice, setNotice] = useState('');
  const [loading, setLoading] = useState(true);

  const selectedCommunity = useMemo(
    () => communities.find((community) => community.id === selectedCommunityId),
    [communities, selectedCommunityId]
  );

  const isAdmin = auth.user?.role === 'ADMIN';
  const canManageCommunities = auth.user?.role === 'ADMIN' || auth.user?.role === 'MANAGER';

  async function loadAll() {
    setLoading(true);
    try {
      const [me, dash, allEvents, allCommunities, allLearningTracks] = await Promise.all([
        api('/api/v1/auth/me'),
        api('/api/v1/dashboard'),
        api('/api/v1/events'),
        api('/api/v1/communities'),
        api('/api/v1/learning-tracks')
      ]);
      setAuth(me);
      setDashboard(dash);
      setEvents(allEvents);
      setCommunities(allCommunities);
      setLearningTracks(allLearningTracks);
      setSelectedCommunityId((current) => current ?? allCommunities[0]?.id ?? null);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadAll().catch((error) => setNotice(error.message));
  }, []);

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

    setNotice('Login realizado.');
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
    setNotice('Cadastro criado. Entre com seu e-mail e senha.');
  }

  async function handleLogout() {
    await fetch(`${API_BASE}/logout`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'X-Requested-With': 'XMLHttpRequest', Accept: 'application/json' }
    });
    setAuth({ authenticated: false, user: null });
    setNotice('Você saiu da conta.');
  }

  async function createEvent(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await api('/api/v1/events', {
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
    event.currentTarget.reset();
    setNotice('Evento publicado.');
    await loadAll();
  }

  async function createCommunity(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const community = await api('/api/v1/communities', {
      method: 'POST',
      body: JSON.stringify({
        nome: form.get('nome'),
        descricao: form.get('descricao'),
        nivelDePermissao: form.get('nivelDePermissao')
      })
    });
    event.currentTarget.reset();
    setNotice('Comunidade criada.');
    await loadAll();
    setSelectedCommunityId(community.id);
  }

  async function sendPost(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await api(`/api/v1/communities/${selectedCommunityId}/posts`, {
      method: 'POST',
      body: JSON.stringify({ mensagem: form.get('mensagem') })
    });
    event.currentTarget.reset();
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark"><Leaf size={22} /></span>
          <span>BRASFI</span>
        </div>

        <nav className="nav-list" aria-label="Principal">
          {views.map((item) => {
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

        <section className="account-panel">
          {auth.authenticated ? (
            <>
              <div className="account-name">
                <CircleUserRound size={20} />
                <span>{auth.user.name}</span>
              </div>
              <span className="role-pill">{auth.user.role}</span>
              <button className="ghost-button" onClick={handleLogout}>
                <LogOut size={17} /> Sair
              </button>
            </>
          ) : (
            <AuthBox
              mode={authMode}
              setMode={setAuthMode}
              onLogin={handleLogin}
              onRegister={handleRegister}
            />
          )}
        </section>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Ecossistema de finanças sustentáveis</p>
            <h1>{titleFor(view)}</h1>
          </div>
          <button className="icon-button" onClick={() => loadAll()} title="Atualizar dados">
            <RefreshCw size={18} />
          </button>
        </header>

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
            {view === 'learning' && <LearningTracks tracks={learningTracks} setView={setView} />}
            {view === 'events' && <Events events={events} isAdmin={isAdmin} createEvent={createEvent} />}
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
            {view === 'about' && <About />}
          </>
        )}
      </section>
    </main>
  );
}

function AuthBox({ mode, setMode, onLogin, onRegister }) {
  const isLogin = mode === 'login';
  return (
    <form className="auth-box" onSubmit={isLogin ? onLogin : onRegister}>
      <div className="auth-tabs">
        <button type="button" className={isLogin ? 'active' : ''} onClick={() => setMode('login')}>Entrar</button>
        <button type="button" className={!isLogin ? 'active' : ''} onClick={() => setMode('register')}>Criar</button>
      </div>
      {!isLogin && <input name="nome" placeholder="Nome" required />}
      <input name="email" type="email" placeholder="E-mail" required />
      {!isLogin && <input name="cpf" placeholder="CPF com 11 números" minLength="11" maxLength="11" required />}
      {!isLogin && <input name="idade" type="number" min="1" placeholder="Idade" required />}
      <input name="password" type="password" placeholder="Senha" required />
      <button className="primary-button" type="submit">
        <LogIn size={17} /> {isLogin ? 'Entrar' : 'Cadastrar'}
      </button>
    </form>
  );
}

function Dashboard({ dashboard, setView }) {
  const stats = [
    ['Eventos', dashboard?.totalEventos ?? 0, CalendarDays],
    ['Futuros', dashboard?.eventosFuturos ?? 0, Sparkles],
    ['Gravados', dashboard?.eventosGravados ?? 0, Video],
    ['Comunidades', dashboard?.totalComunidades ?? 0, UsersRound]
  ];

  return (
    <div className="dashboard-grid">
      <section className="hero-band">
        <div>
          <span className="hero-kicker"><ShieldCheck size={18} /> Rede colaborativa</span>
          <h2>Conhecimento, eventos e comunidades em uma plataforma mais clara.</h2>
          <p>Um painel para acompanhar a agenda da BRASFI, entrar em conversas e aproximar pesquisa, mercado e impacto.</p>
          <button className="primary-button" onClick={() => setView('learning')}>
            <BookOpenCheck size={18} /> Começar trilha
          </button>
        </div>
      </section>

      <section className="stat-grid">
        {stats.map(([label, value, Icon]) => (
          <article className="metric-card" key={label}>
            <Icon size={22} />
            <strong>{value}</strong>
            <span>{label}</span>
          </article>
        ))}
      </section>

      <section className="content-column">
        <SectionHeader icon={CalendarDays} title="Próximos eventos" />
        <div className="event-list">
          {(dashboard?.proximosEventos ?? []).map((event) => <EventItem key={event.id} event={event} />)}
        </div>
      </section>

      <section className="content-column">
        <SectionHeader icon={UsersRound} title="Comunidades ativas" />
        <div className="community-mini-list">
          {(dashboard?.comunidades ?? []).map((community) => (
            <div className="mini-row" key={community.id}>
              <strong>{community.nome}</strong>
              <span>{community.totalPosts} posts</span>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}

function LearningTracks({ tracks, setView }) {
  const currentTrack = tracks[0];

  if (!currentTrack) {
    return <EmptyState text="Nenhuma trilha de aprendizagem disponível." />;
  }

  const recommendedEvents = currentTrack.recommendedEvents ?? [];
  const recommendedCommunities = currentTrack.recommendedCommunities ?? [];

  return (
    <div className="learning-layout">
      {tracks.map((track) => (
        <section className="track-hero" key={track.id}>
          <div>
            <span className="hero-kicker"><GraduationCap size={18} /> Trilha guiada</span>
            <h2>{track.title}</h2>
            <p>{track.description}</p>
            <div className="track-meta">
              <span><Clock3 size={16} /> {track.duration}</span>
              <span><Sparkles size={16} /> {track.level}</span>
              <span><CheckCircle2 size={16} /> {track.steps.length} etapas</span>
            </div>
          </div>
          <button className="primary-button" onClick={() => setView('events')}>
            <CalendarDays size={17} /> Ver eventos da trilha
          </button>
        </section>
      ))}

      <section className="track-steps">
        <SectionHeader icon={BookOpenCheck} title="Roteiro de aprendizagem" />
        <div className="step-list">
          {currentTrack.steps.map((step, index) => (
            <article className="step-item" key={step.title}>
              <span className="step-number">{index + 1}</span>
              <div>
                <h3>{step.title}</h3>
                <p>{step.description}</p>
                <span className="step-action">{step.action}</span>
              </div>
            </article>
          ))}
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
          <SectionHeader icon={BookOpenCheck} title="Materiais de apoio" />
          <ul className="outcome-list">
            {currentTrack.resources.map((resource) => (
              <li key={resource}><Check size={16} /> {resource}</li>
            ))}
          </ul>
        </section>

        <section className="content-column">
          <SectionHeader icon={CheckCircle2} title="Ao concluir" />
          <ul className="outcome-list">
            {currentTrack.outcomes.map((outcome) => (
              <li key={outcome}><Check size={16} /> {outcome}</li>
            ))}
          </ul>
        </section>
      </aside>
    </div>
  );
}

function Events({ events, isAdmin, createEvent }) {
  const upcoming = events.filter((event) => event.futuro);
  const past = events.filter((event) => !event.futuro);

  return (
    <div className="split-layout">
      <section className="content-column">
        <SectionHeader icon={CalendarDays} title="Agenda" />
        <div className="event-list">
          {upcoming.map((event) => <EventItem key={event.id} event={event} />)}
          {upcoming.length === 0 && <EmptyState text="Nenhum evento futuro cadastrado." />}
        </div>
      </section>

      <section className="content-column">
        <SectionHeader icon={Video} title="Gravados" />
        <div className="event-list">
          {past.map((event) => <EventItem key={event.id} event={event} />)}
          {past.length === 0 && <EmptyState text="Nenhum evento gravado cadastrado." />}
        </div>
      </section>

      {isAdmin && (
        <section className="editor-panel">
          <SectionHeader icon={Plus} title="Novo evento" />
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
            <button className="primary-button" type="submit"><Plus size={17} /> Publicar</button>
          </form>
        </section>
      )}
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

function About() {
  return (
    <section className="about-panel">
      <span className="hero-kicker"><Leaf size={18} /> BRASFI</span>
      <h2>Uma plataforma para fortalecer pesquisa, mercado e investimentos sustentáveis.</h2>
      <p>
        A experiência renovada organiza eventos, gravações e comunidades em uma interface única,
        preparada para evoluir com novos módulos sem depender de páginas acopladas ao servidor.
      </p>
      <div className="principles">
        <span>Colaboração</span>
        <span>Conhecimento aplicado</span>
        <span>Impacto sustentável</span>
      </div>
    </section>
  );
}

function EventItem({ event }) {
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
        </div>
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
    dashboard: 'Painel BRASFI',
    learning: 'Trilhas de aprendizagem',
    events: 'Eventos',
    communities: 'Comunidades',
    about: 'Sobre a plataforma'
  }[view];
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
