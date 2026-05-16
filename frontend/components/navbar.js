
class BeatSyncNavbar extends HTMLElement {
    connectedCallback() {
        const profileHref = this.getProfileHref();

        this.innerHTML = `
            <style>
                .navbar-link:hover {
                    color: #a78bfa;
                }
                .chat-icon-btn:hover {
                    background-color: #4b5563;
                }
                .notification-badge {
                    position: absolute;
                    top: 0;
                    right: 0;
                    background-color: #ef4444;
                    color: white;
                    border-radius: 50%;
                    width: 20px;
                    height: 20px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 12px;
                    font-weight: bold;
                }

                .profile-dropdown {
                    padding-bottom: 8px;
                }

                .profile-dropdown:hover .profile-menu {
                    display: block;
                }

                .profile-menu {
                    top: 100%;
                    right: 0;
                    margin-top: 0;
                }

                /* Chat panel */
                #chatPanel {
                    position: fixed;
                    top: 0;
                    right: -420px;
                    width: 400px;
                    height: 100vh;
                    background: #1f2937;
                    border-left: 1px solid #374151;
                    z-index: 1000;
                    display: flex;
                    flex-direction: column;
                    transition: right 0.3s ease;
                    box-shadow: -4px 0 20px rgba(0,0,0,0.4);
                }
                #chatPanel.open {
                    right: 0;
                }
                #chatPanelOverlay {
                    display: none;
                    position: fixed;
                    inset: 0;
                    background: rgba(0,0,0,0.5);
                    z-index: 999;
                }
                #chatPanelOverlay.open {
                    display: block;
                }
                .chat-tab-btn {
                    flex: 1;
                    padding: 10px;
                    background: #374151;
                    color: #9ca3af;
                    border: none;
                    cursor: pointer;
                    font-size: 13px;
                    font-weight: 600;
                    transition: background 0.2s;
                }
                .chat-tab-btn.active {
                    background: #4c1d95;
                    color: #e9d5ff;
                }
                .chat-tab-btn:first-child { border-radius: 0; }
                .inbox-item {
                    display: flex;
                    align-items: flex-start;
                    gap: 10px;
                    padding: 12px 14px;
                    border-bottom: 1px solid #374151;
                    cursor: pointer;
                    transition: background 0.15s;
                }
                .inbox-item:hover { background: #374151; }
                .inbox-item.unread .inbox-preview { color: #e5e7eb; font-weight: 600; }
                .inbox-avatar {
                    width: 38px;
                    height: 38px;
                    border-radius: 50%;
                    background: #4c1d95;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-weight: bold;
                    color: #e9d5ff;
                    font-size: 15px;
                    flex-shrink: 0;
                }
                .inbox-meta { flex: 1; min-width: 0; }
                .inbox-name { font-size: 14px; color: #f3f4f6; font-weight: 600; }
                .inbox-sub { font-size: 11px; color: #6b7280; margin-bottom: 2px; }
                .inbox-preview { font-size: 12px; color: #9ca3af; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
                .inbox-time { font-size: 11px; color: #6b7280; flex-shrink: 0; }
                .inbox-actions { display: flex; gap: 6px; margin-top: 6px; }
                .btn-sm-green {
                    padding: 4px 10px;
                    background: #065f46;
                    color: #6ee7b7;
                    border: none;
                    border-radius: 6px;
                    font-size: 11px;
                    font-weight: 600;
                    cursor: pointer;
                    transition: background 0.2s;
                }
                .btn-sm-green:hover { background: #047857; }
                .btn-sm-red {
                    padding: 4px 10px;
                    background: #7f1d1d;
                    color: #fca5a5;
                    border: none;
                    border-radius: 6px;
                    font-size: 11px;
                    font-weight: 600;
                    cursor: pointer;
                    transition: background 0.2s;
                }
                .btn-sm-red:hover { background: #991b1b; }
                /* Chat view */
                .chat-msg-bubble {
                    max-width: 78%;
                    padding: 8px 12px;
                    border-radius: 12px;
                    font-size: 13px;
                    line-height: 1.4;
                    word-break: break-word;
                }
                .chat-msg-mine {
                    align-self: flex-end;
                    background: #5b21b6;
                    color: #ede9fe;
                    border-bottom-right-radius: 2px;
                }
                .chat-msg-other {
                    align-self: flex-start;
                    background: #374151;
                    color: #e5e7eb;
                    border-bottom-left-radius: 2px;
                }
                .chat-msg-system {
                    align-self: center;
                    background: #1f2937;
                    border: 1px solid #4b5563;
                    color: #9ca3af;
                    font-size: 12px;
                    border-radius: 8px;
                    text-align: center;
                    max-width: 90%;
                    font-style: italic;
                }
                .chat-msg-time {
                    font-size: 10px;
                    color: #6b7280;
                    margin-top: 2px;
                }
                .status-badge {
                    display: inline-block;
                    padding: 2px 8px;
                    border-radius: 12px;
                    font-size: 11px;
                    font-weight: 600;
                }
                .status-REQUESTED { background: #78350f; color: #fde68a; }
                .status-ACCEPTED  { background: #064e3b; color: #6ee7b7; }
                .status-IN_PROGRESS { background: #1e3a8a; color: #93c5fd; }
                .status-COMPLETED { background: #374151; color: #9ca3af; }
                .status-CANCELLED { background: #450a0a; color: #fca5a5; }
            </style>

            <nav class="bg-gray-800 py-4 px-6 shadow-lg">
                <div class="container mx-auto flex justify-between items-center">
                    <a href="index.html" class="flex items-center">
                        <span class="text-2xl font-bold text-purple-400">BeatSync</span>
                    </a>
                    <div class="hidden md:flex space-x-8">
                        <a href="djs.html" class="navbar-link text-gray-300 hover:text-purple-400">DJ-evi</a>
                        <a href="equipment.html" class="navbar-link text-gray-300 hover:text-purple-400">Oprema</a>
                        <a href="job-offers.html" class="navbar-link text-gray-300 hover:text-purple-400">Ponude Poslova</a>
                        <a href="how-it-works.html" class="navbar-link text-gray-300 hover:text-purple-400">Kako funkcionira</a>
                        <a href="pricing.html" class="navbar-link text-gray-300 hover:text-purple-400">Cijene</a>
                        <a href="${profileHref}" data-profile-link class="navbar-link text-gray-300 hover:text-purple-400">Profil</a>
                    </div>
                    <div class="flex items-center space-x-4">
                        <a href="auth.html" id="loginBtn" class="bg-purple-600 hover:bg-purple-700 px-4 py-2 rounded-lg font-medium transition-colors">
                            Prijava
                        </a>
                        <div id="profileDropdown" class="profile-dropdown relative hidden">
                            <a href="${profileHref}" data-profile-link class="bg-gray-700 hover:bg-gray-600 px-4 py-2 rounded-lg font-medium transition-colors inline-block" id="profileBtn">
                                Profil
                            </a>
                            <div class="profile-menu hidden absolute right-0 w-40 bg-gray-800 border border-gray-700 rounded-lg shadow-lg z-50">
                                <button id="logoutBtn" class="w-full text-left px-4 py-3 text-sm text-gray-200 hover:bg-gray-700 rounded-lg">
                                    Odjavi se
                                </button>
                            </div>
                        </div>

                        <button id="chatBtn" class="chat-icon-btn bg-transparent hover:bg-gray-700 p-2 rounded-lg transition-colors hidden relative" title="Poruke">
                            <i data-feather="message-circle" class="w-6 h-6 text-purple-400"></i>
                            <span class="notification-badge hidden" id="notificationBadge">0</span>
                        </button>
                        <button class="md:hidden">
                            <i data-feather="menu"></i>
                        </button>
                    </div>
                </div>
                <!-- Mobile menu -->
                <div class="md:hidden hidden mt-4 space-y-2" id="mobile-menu">
                    <a href="djs.html" class="block px-2 py-1 text-gray-300 hover:text-purple-400">DJ-evi</a>
                    <a href="equipment.html" class="block px-2 py-1 text-gray-300 hover:text-purple-400">Oprema</a>
                    <a href="job-offers.html" class="block px-2 py-1 text-gray-300 hover:text-purple-400">Ponude Poslova</a>
                    <a href="how-it-works.html" class="block px-2 py-1 text-gray-300 hover:text-purple-400">Kako funkcionira</a>
                    <a href="pricing.html" class="block px-2 py-1 text-gray-300 hover:text-purple-400">Cijene</a>
                    <a href="${profileHref}" data-profile-link class="block px-2 py-1 text-gray-300 hover:text-purple-400">Profil</a>
                </div>
            </nav>

            <!-- Chat overlay -->
            <div id="chatPanelOverlay"></div>

            <!-- Chat panel -->
            <div id="chatPanel">
                <div style="display:flex; align-items:center; justify-content:space-between; padding:16px 14px 10px; border-bottom:1px solid #374151;">
                    <span id="chatPanelTitle" style="font-size:16px; font-weight:700; color:#e9d5ff;">Poruke</span>
                    <button id="chatPanelClose" style="background:none; border:none; color:#9ca3af; cursor:pointer; font-size:20px; line-height:1;">&#x2715;</button>
                </div>

                <!-- Tab bar (only for BUSINESS) -->
                <div id="chatTabBar" style="display:none; flex-direction:row;">
                    <button class="chat-tab-btn active" id="tabZahtjevi" data-tab="ZAHTJEVI">Zahtjevi</button>
                    <button class="chat-tab-btn" id="tabRazgovori" data-tab="RAZGOVORI">Razgovori</button>
                </div>

                <!-- Inbox list view -->
                <div id="chatInboxView" style="flex:1; overflow-y:auto;"></div>

                <!-- Chat conversation view (hidden by default) -->
                <div id="chatConvView" style="display:none; flex-direction:column; flex:1; min-height:0;">
                    <div id="chatConvHeader" style="padding:10px 14px; border-bottom:1px solid #374151; display:flex; align-items:center; gap:8px;">
                        <button id="chatBackBtn" style="background:none; border:none; color:#a78bfa; cursor:pointer; font-size:13px; font-weight:600;">&#8592; Nazad</button>
                        <div style="flex:1;">
                            <div id="chatConvName" style="font-size:14px; font-weight:600; color:#f3f4f6;"></div>
                            <div id="chatConvMeta" style="font-size:11px; color:#6b7280;"></div>
                        </div>
                        <span id="chatConvStatus" class="status-badge"></span>
                    </div>
                    <div id="chatConvActions" style="padding:8px 14px; border-bottom:1px solid #374151; display:flex; gap:8px; flex-wrap:wrap;"></div>
                    <div id="chatMsgList" style="flex:1; overflow-y:auto; padding:12px; display:flex; flex-direction:column; gap:8px;"></div>
                    <div style="padding:10px 14px; border-top:1px solid #374151; display:flex; gap:8px;">
                        <input id="chatMsgInput" type="text" placeholder="Napiši poruku..." style="flex:1; background:#374151; border:1px solid #4b5563; border-radius:8px; padding:8px 12px; color:#e5e7eb; font-size:13px; outline:none;" />
                        <button id="chatMsgSend" style="background:#5b21b6; color:#e9d5ff; border:none; border-radius:8px; padding:8px 14px; font-size:13px; font-weight:600; cursor:pointer;">Pošalji</button>
                    </div>
                </div>
            </div>
        `;

        // Mobile menu toggle
        const menuButton = this.querySelector('button.md\\:hidden');
        const mobileMenu = this.querySelector('#mobile-menu');
        if (menuButton && mobileMenu) {
            menuButton.addEventListener('click', () => mobileMenu.classList.toggle('hidden'));
        }

        this.checkLoginState();
        window.addEventListener('userLoggedIn', () => this.checkLoginState());
        window.addEventListener('userLoggedOut', () => this.checkLoginState());

        const logoutBtn = this.querySelector('#logoutBtn');
        if (logoutBtn) logoutBtn.addEventListener('click', () => this.logout());

        this._initChatPanel();
    }

    _initChatPanel() {
        const chatBtn = this.querySelector('#chatBtn');
        const panel = this.querySelector('#chatPanel');
        const overlay = this.querySelector('#chatPanelOverlay');
        const closeBtn = this.querySelector('#chatPanelClose');

        chatBtn.addEventListener('click', () => this._openPanel());
        overlay.addEventListener('click', () => this._closePanel());
        closeBtn.addEventListener('click', () => this._closePanel());

        // Tab switching (BUSINESS only)
        this.querySelector('#tabZahtjevi').addEventListener('click', () => this._switchTab('ZAHTJEVI'));
        this.querySelector('#tabRazgovori').addEventListener('click', () => this._switchTab('RAZGOVORI'));

        // Back button
        this.querySelector('#chatBackBtn').addEventListener('click', () => this._showInbox());

        // Send message
        this.querySelector('#chatMsgSend').addEventListener('click', () => this._sendMessage());
        this.querySelector('#chatMsgInput').addEventListener('keydown', e => {
            if (e.key === 'Enter') this._sendMessage();
        });

        this._currentTab = 'ZAHTJEVI';
        this._currentRezervacija = null;
        this._currentConvData = null;
        this._pollInterval = null;
    }

    _openPanel() {
        const panel = this.querySelector('#chatPanel');
        const overlay = this.querySelector('#chatPanelOverlay');
        panel.classList.add('open');
        overlay.classList.add('open');
        this._loadInbox();
    }

    _closePanel() {
        const panel = this.querySelector('#chatPanel');
        const overlay = this.querySelector('#chatPanelOverlay');
        panel.classList.remove('open');
        overlay.classList.remove('open');
        this._stopPolling();
    }

    _switchTab(tab) {
        this._currentTab = tab;
        this.querySelector('#tabZahtjevi').classList.toggle('active', tab === 'ZAHTJEVI');
        this.querySelector('#tabRazgovori').classList.toggle('active', tab === 'RAZGOVORI');
        this._loadInbox();
    }

    _showInbox() {
        this._stopPolling();
        this.querySelector('#chatConvView').style.display = 'none';
        this.querySelector('#chatInboxView').style.display = '';
        const role = localStorage.getItem('role');
        if (role === 'BUSINESS') {
            this.querySelector('#chatTabBar').style.display = 'flex';
        }
        this.querySelector('#chatPanelTitle').textContent = 'Poruke';
        this._loadInbox();
    }

    async _loadInbox() {
        const token = localStorage.getItem('token');
        const role = localStorage.getItem('role');
        const inboxView = this.querySelector('#chatInboxView');
        inboxView.innerHTML = '<div style="padding:20px; text-align:center; color:#6b7280; font-size:13px;">Učitavanje...</div>';

        try {
            if (role === 'BUSINESS') {
                this.querySelector('#chatTabBar').style.display = 'flex';
                const type = this._currentTab;
                const res = await fetch(`http://localhost:8080/api/poruke/inbox?type=${type}`, {
                    headers: { 'Authorization': 'Bearer ' + token }
                });
                const items = await res.json();
                if (type === 'ZAHTJEVI') {
                    this._renderZahtjevi(items);
                } else {
                    this._renderRazgovori(items);
                }
            } else {
                this.querySelector('#chatTabBar').style.display = 'none';
                const res = await fetch('http://localhost:8080/api/poruke/inbox?type=RAZGOVORI', {
                    headers: { 'Authorization': 'Bearer ' + token }
                });
                const items = await res.json();
                this._renderRazgovori(items);
            }
        } catch (e) {
            inboxView.innerHTML = '<div style="padding:20px; text-align:center; color:#ef4444; font-size:13px;">Greška pri učitavanju.</div>';
        }
    }

    _renderZahtjevi(items) {
        const inboxView = this.querySelector('#chatInboxView');
        if (!items.length) {
            inboxView.innerHTML = '<div style="padding:20px; text-align:center; color:#6b7280; font-size:13px;">Nema novih zahtjeva.</div>';
            return;
        }
        inboxView.innerHTML = items.map(item => `
            <div class="inbox-item ${!item.readStatus ? 'unread' : ''}" data-rez="${item.idRezervacije}">
                <div class="inbox-avatar">${(item.izvodacIme || '?')[0].toUpperCase()}</div>
                <div class="inbox-meta">
                    <div class="inbox-name">${item.izvodacIme} ${item.izvodacPrezime}</div>
                    <div class="inbox-sub">${item.nazivPonude || ''}</div>
                    <div class="inbox-preview">${item.tekst}</div>
                    <div class="inbox-actions">
                        <button class="btn-sm-green" data-action="otvori-chat" data-rez="${item.idRezervacije}" data-izvodac="${item.izvodacUsername}">Otvori chat</button>
                        <button class="btn-sm-red" data-action="odbij" data-rez="${item.idRezervacije}">Odbij</button>
                    </div>
                </div>
                <div class="inbox-time">${this._formatTime(item.timestamp)}</div>
            </div>
        `).join('');

        inboxView.querySelectorAll('[data-action="otvori-chat"]').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                e.stopPropagation();
                const rezId = parseInt(btn.dataset.rez);
                const token = localStorage.getItem('token');
                await fetch(`http://localhost:8080/api/poruke/otvori-chat/${rezId}`, {
                    method: 'POST',
                    headers: { 'Authorization': 'Bearer ' + token }
                });
                this._openConversation(rezId, { izvodacUsername: btn.dataset.izvodac });
            });
        });

        inboxView.querySelectorAll('[data-action="odbij"]').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                e.stopPropagation();
                const rezId = parseInt(btn.dataset.rez);
                const token = localStorage.getItem('token');
                await fetch(`http://localhost:8080/api/rezervacije/${rezId}/otkazi`, {
                    method: 'PUT',
                    headers: { 'Authorization': 'Bearer ' + token }
                });
                this._loadInbox();
                this.ucitajBadge();
            });
        });
    }

    _renderRazgovori(items) {
        const inboxView = this.querySelector('#chatInboxView');
        if (!items.length) {
            inboxView.innerHTML = '<div style="padding:20px; text-align:center; color:#6b7280; font-size:13px;">Nema aktivnih razgovora.</div>';
            return;
        }
        const role = localStorage.getItem('role');
        inboxView.innerHTML = items.map(item => {
            const name = role === 'BUSINESS'
                ? `${item.izvodacIme} ${item.izvodacPrezime}`
                : item.nazivKluba;
            const initial = (name || '?')[0].toUpperCase();
            return `
                <div class="inbox-item ${!item.readStatus ? 'unread' : ''}" data-action="open-conv" data-rez="${item.idRezervacije}" data-izvodac="${item.izvodacUsername}" data-business="${item.businessUsername}">
                    <div class="inbox-avatar">${initial}</div>
                    <div class="inbox-meta">
                        <div class="inbox-name">${name}</div>
                        <div class="inbox-sub">${item.nazivPonude || ''} · <span class="status-badge status-${item.statusRezervacije}">${this._statusLabel(item.statusRezervacije)}</span></div>
                        <div class="inbox-preview">${item.lastMessage}</div>
                    </div>
                    <div class="inbox-time">${this._formatTime(item.timestamp)}</div>
                </div>
            `;
        }).join('');

        inboxView.querySelectorAll('[data-action="open-conv"]').forEach(el => {
            el.addEventListener('click', () => {
                this._openConversation(parseInt(el.dataset.rez), el.dataset);
            });
        });
    }

    async _openConversation(rezId, data) {
        this._currentRezervacija = rezId;
        this._currentConvData = data;
        this.querySelector('#chatInboxView').style.display = 'none';
        this.querySelector('#chatTabBar').style.display = 'none';
        const convView = this.querySelector('#chatConvView');
        convView.style.display = 'flex';
        this.querySelector('#chatPanelTitle').textContent = 'Razgovor';
        await this._loadConversation();
        this._startPolling();
    }

    async _loadConversation() {
        const rezId = this._currentRezervacija;
        const token = localStorage.getItem('token');
        const role = localStorage.getItem('role');

        try {
            const [porukeRes, rezRes] = await Promise.all([
                fetch(`http://localhost:8080/api/poruke?rezervacija=${rezId}`, {
                    headers: { 'Authorization': 'Bearer ' + token }
                }),
                fetch(`http://localhost:8080/api/rezervacije`, {
                    headers: { 'Authorization': 'Bearer ' + token }
                })
            ]);

            const poruke = await porukeRes.json();
            const sveRez = await rezRes.json();
            const rez = sveRez.find(r => r.idRezervacije === rezId);

            if (rez) {
                const name = role === 'BUSINESS'
                    ? `${rez.izvodacIme} ${rez.izvodacPrezime}`
                    : rez.nazivKluba;
                this.querySelector('#chatConvName').textContent = name;
                this.querySelector('#chatConvMeta').textContent = rez.nazivPonude || '';
                const statusEl = this.querySelector('#chatConvStatus');
                statusEl.textContent = this._statusLabel(rez.statusRezervacije);
                statusEl.className = `status-badge status-${rez.statusRezervacije}`;
                this._renderConvActions(rez, role);
            }

            this._renderMessages(poruke, role);
            this.ucitajBadge();
        } catch (e) {
            console.error('Greška pri učitavanju razgovora', e);
        }
    }

    _renderConvActions(rez, role) {
        const actionsEl = this.querySelector('#chatConvActions');
        actionsEl.innerHTML = '';
        if (rez.statusRezervacije !== 'REQUESTED') return;

        const token = localStorage.getItem('token');

        if (role === 'BUSINESS') {
            if (!rez.potvrdaBusiness) {
                const btnPotvrdi = document.createElement('button');
                btnPotvrdi.className = 'btn-sm-green';
                btnPotvrdi.style.padding = '6px 14px';
                btnPotvrdi.textContent = 'Potvrdi suradnju';
                btnPotvrdi.addEventListener('click', async () => {
                    await fetch(`http://localhost:8080/api/rezervacije/${rez.idRezervacije}/potvrdi`, {
                        method: 'PUT', headers: { 'Authorization': 'Bearer ' + token }
                    });
                    this._loadConversation();
                });
                actionsEl.appendChild(btnPotvrdi);
            } else {
                const info = document.createElement('span');
                info.style.cssText = 'font-size:12px; color:#6ee7b7;';
                info.textContent = '✓ Ti si potvrdio/la · Čeka izvođača';
                actionsEl.appendChild(info);
            }

            const btnOdbij = document.createElement('button');
            btnOdbij.className = 'btn-sm-red';
            btnOdbij.style.padding = '6px 14px';
            btnOdbij.textContent = 'Odbij prijavu';
            btnOdbij.addEventListener('click', async () => {
                await fetch(`http://localhost:8080/api/rezervacije/${rez.idRezervacije}/otkazi`, {
                    method: 'PUT', headers: { 'Authorization': 'Bearer ' + token }
                });
                this._showInbox();
                this.ucitajBadge();
            });
            actionsEl.appendChild(btnOdbij);

        } else {
            // IZVODAC
            if (rez.potvrdaBusiness && !rez.potvrdaIzvodac) {
                const btnPotvrdi = document.createElement('button');
                btnPotvrdi.className = 'btn-sm-green';
                btnPotvrdi.style.padding = '6px 14px';
                btnPotvrdi.textContent = 'Potvrdi suradnju';
                btnPotvrdi.addEventListener('click', async () => {
                    await fetch(`http://localhost:8080/api/rezervacije/${rez.idRezervacije}/potvrdi`, {
                        method: 'PUT', headers: { 'Authorization': 'Bearer ' + token }
                    });
                    this._loadConversation();
                });
                actionsEl.appendChild(btnPotvrdi);
            } else if (!rez.potvrdaBusiness) {
                const info = document.createElement('span');
                info.style.cssText = 'font-size:12px; color:#9ca3af;';
                info.textContent = 'Čeka potvrdu kluba...';
                actionsEl.appendChild(info);
            }
        }
    }

    _renderMessages(poruke, role) {
        const list = this.querySelector('#chatMsgList');
        const myRole = role === 'BUSINESS' ? 'BUSINESS' : 'IZVODAC';
        list.innerHTML = poruke.map(p => {
            if (p.messageType === 'SYSTEM_NOTIFICATION') {
                return `<div class="chat-msg-bubble chat-msg-system">${p.sadrzajPoruke}</div>`;
            }
            const isMine = p.posiljatelj === myRole;
            return `
                <div style="display:flex; flex-direction:column; align-items:${isMine ? 'flex-end' : 'flex-start'};">
                    <div class="chat-msg-bubble ${isMine ? 'chat-msg-mine' : 'chat-msg-other'}">${this._escHtml(p.sadrzajPoruke)}</div>
                    <div class="chat-msg-time">${this._formatTime(p.timestampPoruke)}</div>
                </div>
            `;
        }).join('');
        list.scrollTop = list.scrollHeight;
    }

    async _sendMessage() {
        const input = this.querySelector('#chatMsgInput');
        const text = input.value.trim();
        if (!text || !this._currentRezervacija) return;

        const token = localStorage.getItem('token');
        input.value = '';

        try {
            await fetch('http://localhost:8080/api/poruke', {
                method: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ sadrzajPoruke: text, idRezervacije: this._currentRezervacija })
            });
            await this._loadConversation();
        } catch (e) {
            input.value = text;
        }
    }

    _startPolling() {
        this._stopPolling();
        this._pollInterval = setInterval(() => {
            if (this._currentRezervacija) this._loadConversation();
        }, 5000);
    }

    _stopPolling() {
        if (this._pollInterval) {
            clearInterval(this._pollInterval);
            this._pollInterval = null;
        }
    }

    getProfileHref() {
        const role = localStorage.getItem('role');
        return role === 'BUSINESS' ? 'profile-business.html' : 'profile.html';
    }

    updateProfileLinks() {
        const profileHref = this.getProfileHref();
        this.querySelectorAll('[data-profile-link]').forEach(link => {
            link.setAttribute('href', profileHref);
        });
    }

    checkLoginState() {
        const isLoggedIn = localStorage.getItem('user_logged_in') === 'true';
        const loginBtn = this.querySelector('#loginBtn');
        const profileDropdown = this.querySelector('#profileDropdown');
        const chatBtn = this.querySelector('#chatBtn');

        this.updateProfileLinks();

        if (isLoggedIn) {
            loginBtn.classList.add('hidden');
            profileDropdown.classList.remove('hidden');
            chatBtn.classList.remove('hidden');
            this.ucitajBadge();
            // Badge polling svakih 30s
            if (!this._badgeInterval) {
                this._badgeInterval = setInterval(() => this.ucitajBadge(), 30000);
            }
        } else {
            loginBtn.classList.remove('hidden');
            profileDropdown.classList.add('hidden');
            chatBtn.classList.add('hidden');
            if (this._badgeInterval) {
                clearInterval(this._badgeInterval);
                this._badgeInterval = null;
            }
        }

        feather.replace();
    }

    async ucitajBadge() {
        const token = localStorage.getItem('token');
        if (!token) return;
        try {
            const res = await fetch('http://localhost:8080/api/poruke/unread-count', {
                headers: { 'Authorization': 'Bearer ' + token }
            });
            if (!res.ok) return;
            const data = await res.json();
            this.updateNotificationBadge(data.count);
        } catch {}
    }

    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        localStorage.removeItem('role');
        localStorage.removeItem('user_logged_in');
        window.dispatchEvent(new Event('userLoggedOut'));
        window.location.href = 'index.html';
    }

    setLoginState(loggedIn) {
        if (loggedIn) {
            localStorage.setItem('user_logged_in', 'true');
            window.dispatchEvent(new Event('userLoggedIn'));
        } else {
            localStorage.removeItem('user_logged_in');
            window.dispatchEvent(new Event('userLoggedOut'));
        }
    }

    updateNotificationBadge(count) {
        const badge = this.querySelector('#notificationBadge');
        if (badge) {
            if (count > 0) {
                badge.textContent = count > 99 ? '99+' : count;
                badge.classList.remove('hidden');
            } else {
                badge.classList.add('hidden');
            }
        }
    }

    _formatTime(ts) {
        if (!ts) return '';
        const d = new Date(ts);
        const now = new Date();
        if (d.toDateString() === now.toDateString()) {
            return d.toLocaleTimeString('hr', { hour: '2-digit', minute: '2-digit' });
        }
        return d.toLocaleDateString('hr', { day: '2-digit', month: '2-digit' });
    }

    _statusLabel(s) {
        const labels = {
            REQUESTED: 'Na čekanju', ACCEPTED: 'Prihvaćeno',
            IN_PROGRESS: 'U tijeku', COMPLETED: 'Završeno', CANCELLED: 'Otkazano'
        };
        return labels[s] || s;
    }

    _escHtml(str) {
        return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
    }
}
customElements.define('beat-sync-navbar', BeatSyncNavbar);
