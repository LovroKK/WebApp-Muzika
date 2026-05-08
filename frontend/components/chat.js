class BeatSyncChat extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
        this.isOpen = false;
    }

    connectedCallback() {
        this.render();
    }

    render() {
        this.shadowRoot.innerHTML = `
            <style>
                :host {
                    all: initial;
                    font-family: Arial, sans-serif;
                }

                .chat-container {
                    position: fixed;
                    bottom: 20px;
                    right: 20px;
                    z-index: 15;
                    display: flex;
                    flex-direction: column;
                    align-items: flex-end;
                    gap: 10px;                    pointer-events: none;                }       

                .chat-window {
                    width: 350px;
                    height: 500px;
                    background: #1f2937;
                    border-radius: 12px;
                    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5);
                    transition: all 0.3s ease;
                    transform: translateY(20px);
                    opacity: 0;

                    display: flex;
                    flex-direction: column;
                    overflow: hidden;
                    pointer-events: none;
                }

                .chat-window.open {
                    transform: translateY(0);
                    opacity: 1;
                    pointer-events: auto;
                }

                .chat-toggle {
                    width: 60px;
                    height: 60px;
                    background: #7c3aed;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    cursor: pointer;
                    box-shadow: 0 4px 12px rgba(124, 58, 237, 0.4);
                    border: none;
                    pointer-events: auto;
                }

                .chat-header {
                    background: #374151;
                    padding: 1rem;
                }

                .chat-tabs {
                    display: flex;
                    border-bottom: 1px solid #4b5563;
                }

                .chat-tab {
                    flex: 1;
                    padding: 0.75rem;
                    text-align: center;
                    cursor: pointer;
                    color: #d1d5db;
                    user-select: none;
                }

                .chat-tab.active {
                    color: #a78bfa;
                    border-bottom: 2px solid #a78bfa;
                }

                .chat-content {
                    flex: 1;
                    overflow-y: auto;
                    padding: 1rem;
                    color: #d1d5db;
                }

                .chat-input {
                    padding: 1rem;
                    border-top: 1px solid #4b5563;
                    flex-shrink: 0;
                }

                .input-row {
                    display: flex;
                    align-items: center;
                    gap: 0.5rem;
                }

                .input-row input {
                    flex: 1;
                    background: #374151;
                    border: 1px solid #4b5563;
                    border-radius: 8px;
                    padding: 0.75rem;
                    color: white;
                    outline: none;
                }

                .input-row input::placeholder {
                    color: #9ca3af;
                }

                .new-message-btn {
                    width: 40px;
                    height: 40px;
                    background: #7c3aed;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    cursor: pointer;
                    border: none;
                    flex-shrink: 0;
                }

                svg {
                    width: 20px;
                    height: 20px;
                    stroke: white;
                }
            </style>

            <div class="chat-container">
                <div class="chat-window ${this.isOpen ? 'open' : ''}">
                    <div class="chat-header">
                        <div class="chat-tabs">
                            <div class="chat-tab active">Poruke</div>
                        </div>
                    </div>

                    <div class="chat-content">
                        <p style="text-align:center; color:#9ca3af;">Nema poruka</p>
                    </div>

                    <div class="chat-input">
                        <div class="input-row">
                            <input type="text" placeholder="Napišite poruku...">
                            <button class="new-message-btn" type="button" aria-label="Nova poruka">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <line x1="12" y1="5" x2="12" y2="19"></line>
                                    <line x1="5" y1="12" x2="19" y2="12"></line>
                                </svg>
                            </button>
                        </div>
                    </div>
                </div>

                <button class="chat-toggle" type="button" aria-label="Otvori chat">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M21 15a4 4 0 0 1-4 4H8l-5 5V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4z"></path>
                    </svg>
                </button>
            </div>
        `;

        this.addEventListeners();
    }

    addEventListeners() {
        const toggle = this.shadowRoot.querySelector('.chat-toggle');
        const newMessageBtn = this.shadowRoot.querySelector('.new-message-btn');

        if (toggle) {
            toggle.addEventListener('click', () => {
                this.isOpen = !this.isOpen;
                this.render();
            });
        }

        if (newMessageBtn) {
            newMessageBtn.addEventListener('click', () => {
                this.openNewMessageDialog();
            });
        }
    }

    openNewMessageDialog() {
        console.log('Open new message dialog');
    }
}

customElements.define('beat-sync-chat', BeatSyncChat);