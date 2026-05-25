// Centralna konfiguracija backend adrese.
// API_BASE se racuna iz adrese kojom je otvoren frontend:
//  - na laptopu (host):       http://localhost:8081     -> API_BASE = http://localhost:8080
//  - na drugom uredjaju:      http://192.168.1.50:8081  -> API_BASE = http://192.168.1.50:8080
// Nista ne treba mijenjati rucno kad demo radi na lokalnoj mrezi:
// svaki uredaj zove backend na istom hostu/IP-u s kojeg je dohvatio frontend.
//
// VAZNO!!! skripta se mora loadati PRIJE navbar.js, equipment-api.js,
// review-form.js i bilo koje inline skripte koja zove API.
const API_BASE = `${window.location.protocol}//${window.location.hostname}:8080`;
const API_BASE_URL = `${API_BASE}/api`;

// povezana i na window da bude dostupno i iz ES modula (admin/admin.js),
// koji ne dijele uvijek isti scope s klasicnim <script> tagovima.
window.API_BASE = API_BASE;
window.API_BASE_URL = API_BASE_URL;
