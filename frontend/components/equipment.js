// equipment.js -- zajednički JS za opremu (listing, create/edit).

const STORAGE_KEY = 'beatsync_equipment_listings';
const DEFAULT_IMAGE = 'http://static.photos/technology/200x200/0';
const DEFAULT_EQUIPMENT_LISTINGS = [
    {
        id_opreme: 1,
        naziv_opreme: 'Pioneer DJM-900NXS2',
        cijena: 80,
        kategorija: 'Mixeri',
        slika: 'http://static.photos/technology/200x200/1',
        vlasnik_opreme: 'DJ Electro',
        lokacije: ['Zagreb', 'Split']
    },
    {
        id_opreme: 2,
        naziv_opreme: 'Martin MAC Aura',
        cijena: 120,
        kategorija: 'Rasvjeta',
        slika: 'http://static.photos/technology/200x200/2',
        vlasnik_opreme: 'Light Masters',
        lokacije: ['Zagreb']
    },
    {
        id_opreme: 3,
        naziv_opreme: 'Pioneer CDJ-3000',
        cijena: 100,
        kategorija: 'Playeri',
        slika: 'http://static.photos/technology/200x200/3',
        vlasnik_opreme: 'Club Sound Rental',
        lokacije: ['Zagreb', 'Rijeka']
    },
    {
        id_opreme: 4,
        naziv_opreme: 'JBL SRX835P',
        cijena: 90,
        kategorija: 'Zvučnici',
        slika: 'http://static.photos/technology/200x200/4',
        vlasnik_opreme: 'Audio Pro',
        lokacije: ['Osijek', 'Zagreb']
    },
    {
        id_opreme: 5,
        naziv_opreme: 'Shure SM58',
        cijena: 25,
        kategorija: 'Mikrofoni',
        slika: 'http://static.photos/technology/200x200/5',
        vlasnik_opreme: 'Vocal Rent',
        lokacije: ['Split', 'Zadar']
    },
    {
        id_opreme: 6,
        naziv_opreme: 'Chauvet DJ GigBAR',
        cijena: 70,
        kategorija: 'Rasvjeta',
        slika: 'http://static.photos/technology/200x200/6',
        vlasnik_opreme: 'Light Masters',
        lokacije: ['Zagreb', 'Varaždin']
    },
    {
        id_opreme: 7,
        naziv_opreme: 'Allen & Heath Xone:96',
        cijena: 85,
        kategorija: 'Mixeri',
        slika: 'http://static.photos/technology/200x200/7',
        vlasnik_opreme: 'DJ Warehouse',
        lokacije: ['Rijeka']
    },
    {
        id_opreme: 8,
        naziv_opreme: 'Pioneer DDJ-FLX10',
        cijena: 90,
        kategorija: 'Kontroleri',
        slika: 'http://static.photos/technology/200x200/8',
        vlasnik_opreme: 'Controller Hub',
        lokacije: ['Zagreb', 'Split']
    }
];

// --- Helper funkcije za obe stranice ---
function ensureSeedData() {
    const existing = localStorage.getItem(STORAGE_KEY);
    if (!existing) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(DEFAULT_EQUIPMENT_LISTINGS));
    }
}

function getListings() {
    ensureSeedData();
    try {
        const parsed = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
        return Array.isArray(parsed) ? parsed : [];
    } catch (error) {
        return [];
    }
}

function resolveCurrentUsername() {
    return localStorage.getItem('beatsync_username')
        || localStorage.getItem('username_izvodac')
        || localStorage.getItem('logged_username')
        || 'demo_korisnik';
}

function parseLocations(value) {
    return value
        .split(/\r?\n|,/)
        .map((location) => location.trim())
        .filter(Boolean);
}

// --- Za equipment.html ----
function renderEquipmentPage() {
    const equipmentGrid = document.getElementById('equipment-grid');
    const emptyEquipmentState = document.getElementById('emptyEquipmentState');
    const listingCreatedMessage = document.getElementById('listingCreatedMessage');
    const filterBtns = document.querySelectorAll('#category-filters .filter-btn');
    const searchInput = document.getElementById('equipment-search');

    function escapeHtml(value) {
        return String(value ?? '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function formatPrice(value) {
        const numericValue = Number(value);
        if (!Number.isFinite(numericValue)) {
            return '0';
        }

        return numericValue.toFixed(2).replace('.00', '');
    }

    function getActiveCategory() {
        const activeBtn = document.querySelector('#category-filters .filter-btn.bg-purple-600');
        return activeBtn ? activeBtn.dataset.category : 'all';
    }

    function getFilteredListings() {
        const query = searchInput.value.toLowerCase().trim();
        const selectedCategory = getActiveCategory();

        return getListings().filter((listing) => {
            const naziv = String(listing.naziv_opreme || '').toLowerCase();
            const kategorija = String(listing.kategorija || '').toLowerCase();
            const lokacije = Array.isArray(listing.lokacije) ? listing.lokacije.join(' ').toLowerCase() : '';

            const matchesCategory = selectedCategory === 'all' || listing.kategorija === selectedCategory;
            const matchesSearch = !query
                || naziv.includes(query)
                || kategorija.includes(query)
                || lokacije.includes(query);

            return matchesCategory && matchesSearch;
        });
    }

    function renderEquipment() {
        const filteredListings = getFilteredListings();

        if (!filteredListings.length) {
            equipmentGrid.innerHTML = '';
            emptyEquipmentState.classList.remove('hidden');
            feather.replace();
            return;
        }

        emptyEquipmentState.classList.add('hidden');
        equipmentGrid.innerHTML = filteredListings.map((listing) => {
            const naziv = escapeHtml(listing.naziv_opreme || 'Nepoznata oprema');
            const kategorija = escapeHtml(listing.kategorija || 'Ostalo');
            const slika = listing.slika ? 'http://localhost:8080' + listing.slika : DEFAULT_IMAGE;
            const lokacije = Array.isArray(listing.lokacije) && listing.lokacije.length
                ? listing.lokacije.map((lokacija) => escapeHtml(lokacija)).join(', ')
                : 'Lokacija nije navedena';

            return `
                <article class="bg-gray-800 rounded-xl overflow-hidden border border-gray-700 hover:border-purple-500/50 hover:-translate-y-1 transition duration-200 shadow-lg">
                    <img src="${slika}" alt="${naziv}" class="w-full h-48 object-cover" onerror="this.src='${DEFAULT_IMAGE}'">
                    <div class="p-4">
                        <div class="flex justify-between items-start gap-3 mb-3">
                            <h3 class="font-semibold text-lg leading-tight">${naziv}</h3>
                            <span class="bg-purple-600 text-xs px-3 py-1 rounded-full whitespace-nowrap">${kategorija}</span>
                        </div>
                        <p class="text-gray-400 text-sm mb-4">Dostupno u: ${lokacije}</p>
                        <div class="flex justify-between items-center gap-3">
                            <span class="text-purple-400 font-medium">€${formatPrice(listing.cijena)}/dan</span>
                            <button type="button" class="bg-purple-600 hover:bg-purple-700 px-3 py-2 rounded text-sm">Pošalji upit</button>
                        </div>
                    </div>
                </article>
            `;
        }).join('');

        feather.replace();
    }

    function setActiveButton(button) {
        filterBtns.forEach((btn) => {
            btn.classList.remove('bg-purple-600', 'hover:bg-purple-700');
            btn.classList.add('bg-gray-800', 'hover:bg-gray-700');
        });

        button.classList.remove('bg-gray-800', 'hover:bg-gray-700');
        button.classList.add('bg-purple-600', 'hover:bg-purple-700');
    }

    filterBtns.forEach((btn) => {
        btn.addEventListener('click', () => {
            setActiveButton(btn);
            renderEquipment();
        });
    });

    searchInput.addEventListener('input', renderEquipment);

    if (new URLSearchParams(window.location.search).get('created') === '1') {
        listingCreatedMessage.textContent = 'Novi oglas za opremu je uspješno kreiran.';
        listingCreatedMessage.classList.remove('hidden');
    }

    renderEquipment();
}

function renderCreateEquipmentPage() {
    const form = document.getElementById('equipmentListingForm');
    const successMessage = document.getElementById('successMessage');
    const errorMessage = document.getElementById('errorMessage');
    const imageInput = document.getElementById('slika');
    const imagePreview = document.getElementById('imagePreview');
    const imagePreviewWrapper = document.getElementById('imagePreviewWrapper');
    let selectedImageData = '';

    function setMessage(element, message) {
        element.textContent = message;
        element.classList.remove('hidden');
    }

    function clearMessages() {
        successMessage.classList.add('hidden');
        errorMessage.classList.add('hidden');
        successMessage.textContent = '';
        errorMessage.textContent = '';
    }

    function resetImagePreview() {
        selectedImageData = '';
        imagePreview.src = '';
        imagePreviewWrapper.classList.add('hidden');
    }

    imageInput.addEventListener('change', () => {
        const [file] = imageInput.files || [];

        if (!file) {
            resetImagePreview();
            return;
        }

        const reader = new FileReader();
        reader.onload = (loadEvent) => {
            selectedImageData = loadEvent.target?.result || '';

            if (selectedImageData) {
                imagePreview.src = selectedImageData;
                imagePreviewWrapper.classList.remove('hidden');
            }
        };
        reader.onerror = () => {
            resetImagePreview();
            setMessage(errorMessage, 'Dogodila se greška pri učitavanju slike. Pokušaj ponovno.');
        };
        reader.readAsDataURL(file);
    });

    form.addEventListener('submit', (event) => {
        event.preventDefault();
        clearMessages();

        const lokacije = parseLocations(document.getElementById('lokacije').value);

        if (!lokacije.length) {
            setMessage(errorMessage, 'Unesi barem jednu lokaciju dostupnosti.');
            return;
        }

        const nazivOpreme = document.getElementById('nazivOpreme').value.trim();
        const cijenaValue = document.getElementById('cijena').value;
        const kategorija = document.getElementById('kategorija').value;
        const vlasnikOpreme = resolveCurrentUsername();
        const slika = selectedImageData || DEFAULT_IMAGE;

        const novoOglasavanje = {
            id_opreme: Date.now(),
            naziv_opreme: nazivOpreme,
            cijena: Number(cijenaValue),
            kategorija,
            slika,
            vlasnik_opreme: vlasnikOpreme,
            lokacije
        };

        const listings = getListings();
        listings.unshift(novoOglasavanje);
        localStorage.setItem(STORAGE_KEY, JSON.stringify(listings));

        setMessage(successMessage, 'Oglas je uspješno spremljen. Preusmjeravanje na stranicu opreme...');
        form.reset();
        resetImagePreview();

        window.setTimeout(() => {
            window.location.href = 'equipment.html?created=1';
        }, 900);
    });
}

// --- Auto-init na temelju ID elemenata ---
document.addEventListener('DOMContentLoaded', () => {
    feather.replace();

    // Ako postoji equipmentListingForm: create
    if (document.getElementById('equipmentListingForm')) {
        renderCreateEquipmentPage();
    }
    // Ako postoji equipment-grid: prikaz
    if (document.getElementById('equipment-grid')) {
        renderEquipmentPage();
    }
});