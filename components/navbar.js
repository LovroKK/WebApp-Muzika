
class BeatSyncNavbar extends HTMLElement {
    connectedCallback() {
        this.innerHTML = `
            <style>
                .navbar-link:hover {
                    color: #a78bfa;
                }
            </style>
            <nav class="bg-gray-800 py-4 px-6 shadow-lg">
                <div class="container mx-auto flex justify-between items-center">
                    <a href="/" class="flex items-center">
                        <span class="text-2xl font-bold text-purple-400">BeatSync</span>
                    </a>
                    <div class="hidden md:flex space-x-8">
                        <a href="index.html#dj-list" class="navbar-link text-gray-300 hover:text-purple-400">DJ-evi</a>
                        <a href="index.html#equipment" class="navbar-link text-gray-300 hover:text-purple-400">Oprema</a>
                        <a href="job-offers.html" class="navbar-link text-gray-300 hover:text-purple-400">Ponude Poslova</a>
                        <a href="how-it-works.html" class="navbar-link text-gray-300 hover:text-purple-400">Kako funkcionira</a>
                        <a href="pricing.html" class="navbar-link text-gray-300 hover:text-purple-400">Cijene</a>
                        <a href="profile.html" class="navbar-link text-gray-300 hover:text-purple-400">Profil</a>
                    </div>
                    <div class="flex items-center space-x-4">
                        <a href="auth.html" class="bg-purple-600 hover:bg-purple-700 px-4 py-2 rounded-lg font-medium transition-colors">
                            Prijava
                        </a>
                        <a href="profile.html" class="bg-gray-700 hover:bg-gray-600 px-4 py-2 rounded-lg font-medium transition-colors hidden" id="profileBtn">
                            Profil
                        </a>
                        <button class="md:hidden">
                            <i data-feather="menu"></i>
                        </button>
                    </div>
</div>
                <!-- Mobile menu -->
                <div class="md:hidden hidden mt-4 space-y-2" id="mobile-menu">
                            <a href="index.html#dj-list" class="block px-2 py-1 text-gray-300 hover:text-purple-400">DJ-evi</a>
                            <a href="index.html#equipment" class="block px-2 py-1 text-gray-300 hover:text-purple-400">Oprema</a>
                            <a href="job-offers.html" class="block px-2 py-1 text-gray-300 hover:text-purple-400">Ponude Poslova</a>
                            <a href="how-it-works.html" class="block px-2 py-1 text-gray-300 hover:text-purple-400">Kako funkcionira</a>
                            <a href="pricing.html" class="block px-2 py-1 text-gray-300 hover:text-purple-400">Cijene</a>
                            <a href="profile.html" class="block px-2 py-1 text-gray-300 hover:text-purple-400">Profil</a>
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
    }
}
customElements.define('beat-sync-navbar', BeatSyncNavbar);