
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
                
            </style>
            <nav class="bg-gray-800 py-4 px-6 shadow-lg">
                <div class="container mx-auto flex justify-between items-center">
                    <a href="/" class="flex items-center">
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
                            <span class="notification-badge hidden" id="notificationBadge">3</span>
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
        `;
        
        // Add mobile menu toggle functionality
        const menuButton = this.querySelector('button.md\\:hidden');
        const mobileMenu = this.querySelector('#mobile-menu');
        
        if (menuButton && mobileMenu) {
            menuButton.addEventListener('click', () => {
                mobileMenu.classList.toggle('hidden');
            });
        }

        // Check login state on page load
        this.checkLoginState();

        // Listen for login/logout events
        window.addEventListener('userLoggedIn', () => this.checkLoginState());
        window.addEventListener('userLoggedOut', () => this.checkLoginState());

        // Add chat button click handler
        const chatBtn = this.querySelector('#chatBtn');
        if (chatBtn) {
            chatBtn.addEventListener('click', () => {
                console.log('Chat/Messages clicked');
                // TODO: Navigate to messages page or open messages modal
            });
        }

        const logoutBtn = this.querySelector('#logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => this.logout());
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
        } else {
            loginBtn.classList.remove('hidden');
            profileDropdown.classList.add('hidden');

            chatBtn.classList.add('hidden');
        }

        // Feather icons need to be replaced after DOM updates
        feather.replace();
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
}
customElements.define('beat-sync-navbar', BeatSyncNavbar);