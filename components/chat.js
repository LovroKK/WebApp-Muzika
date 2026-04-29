class BeatSyncChat extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
        this.isOpen = false;
    }

    connectedCallback() {
        this.render();
        this.addEventListeners();
    }

    render() {
        this.shadowRoot.innerHTML = `
            <style>
                .chat-container {
                    position: fixed;
                    bottom: 20px;
                    right: 20px;
                    z-index: 50;
                }
                .chat-window {
                    width: 350px;
                    height: 500px;
                    background: #1f2937;
                    border-radius: 12px;
                    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5);
                    transition: all 0.3s ease;
                    transform: translateY(100%);
                    opacity: 0;
                }
                .chat-window.open {
                    transform: translateY(0);
                    opacity: 1;
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
                    position: absolute;
                    bottom: 0;
                    right: 0;
                }
                .chat-header {
                    background: #374151;
                    padding: 1rem;
                    border-radius: 12px 12px 0 0;
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
                }
                .chat-tab.active {
                    color: #a78bfa;
                    border-bottom: 2px solid #a78bfa;
                }
                .chat-content {
                    height: 350px;
                    overflow-y: auto;
                    padding: 1rem;
                }
                .chat-input {
                    padding: 1rem;
                    border-top: 1px solid #4b5563;
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
                    margin-left: auto;
                }
            </style>
            <div class="chat-container">
                <div class="chat-window ${this.isOpen ? 'open' : ''}">
                    <div class="chat-header">
                        <div class="chat-tabs">
                            <div class="chat-tab active">Poruke</div>
                    </div>
                    <div class="chat-content">
                        <p class="text-center text-gray-400">Nema poruka</p>
                    </div>
                    <div class="chat-input">
                        <div class="flex items-center gap-2">
                            <input type="text" placeholder="Napišite poruku..." class="flex-1 bg-gray-700 border border-gray-600 rounded-lg px-3 py-2 text-sm">
                            <button class="new-message-btn">
                                <i data-feather="plus"></i>
                            </button>
                        </div>
                    </div>
                </div>
                <div class="chat-toggle">
                    <i data-feather="message-circle"></i>
                </div>
            </div>
        `;
    }

    addEventListeners() {
        const toggle = this.shadowRoot.querySelector('.chat-toggle');
        const newMessageBtn = this.shadowRoot.querySelector('.new-message-btn');
        
        toggle.addEventListener('click', () => {
            this.isOpen = !this.isOpen;
            this.render();
            this.addEventListeners();
            
            if (this.isOpen) {
                feather.replace();
            }
        });
        
        if (newMessageBtn) {
            newMessageBtn.addEventListener('click', () => {
                // Open new message dialog
                this.openNewMessageDialog();
            });
        }
    }

    openNewMessageDialog() {
        // Implementation for new message dialog
        console.log('Open new message dialog');
    }
}

customElements.define('beat-sync-chat', BeatSyncChat);