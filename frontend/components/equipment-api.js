const EQUIPMENT_API_BASE_URL = 'http://localhost:8080/api';
const EQUIPMENT_DEFAULT_IMAGE = 'http://static.photos/technology/200x200/0';

let equipmentApiListings = [];

function getEquipmentToken() {
    return localStorage.getItem('token');
}

function getEquipmentRole() {
    return localStorage.getItem('role');
}

function getEquipmentUsername() {
    return localStorage.getItem('username');
}

function parseEquipmentLocations(value) {
    return value
        .split(/\r?\n|,/)
        .map((location) => location.trim())
        .filter(Boolean);
}

function normalizeEquipmentListing(listing) {
    return {
        id_opreme: listing.idOpreme ?? listing.id_opreme,
        naziv_opreme: listing.nazivOpreme ?? listing.naziv_opreme,
        cijena: listing.cijena,
        kategorija: listing.kategorija,
        slika: listing.slika,
        vlasnik_opreme: listing.vlasnikUsername ?? listing.vlasnik_opreme,
        lokacije: Array.isArray(listing.lokacije) ? listing.lokacije : []
    };
}

async function parseEquipmentApiError(response, fallbackMessage) {
    try {
        const data = await response.json();
        return data.message || data.detail || fallbackMessage;
    } catch (error) {
        return fallbackMessage;
    }
}

async function fetchEquipmentListings() {
    const response = await fetch(`${EQUIPMENT_API_BASE_URL}/equipment`);

    if (!response.ok) {
        throw new Error(await parseEquipmentApiError(response, 'Greška pri dohvaćanju opreme.'));
    }

    const data = await response.json();
    equipmentApiListings = Array.isArray(data) ? data.map(normalizeEquipmentListing) : [];
}

async function deleteEquipmentListing(id, cardElement) {
    const token = getEquipmentToken();
    if (!token) return;

    const confirmed = window.confirm('Jeste li sigurni da želite ukloniti ovaj oglas?');
    if (!confirmed) return;

    try {
        const response = await fetch(`${EQUIPMENT_API_BASE_URL}/equipment/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });

        if (!response.ok) {
            const message = await parseEquipmentApiError(response, 'Greška pri brisanju oglasa.');
            alert(message);
            return;
        }

        // Ukloni iz lokalne liste i DOM-a
        equipmentApiListings = equipmentApiListings.filter(l => l.id_opreme !== id);
        cardElement.remove();

        const grid = document.getElementById('equipment-grid');
        if (grid && grid.children.length === 0) {
            document.getElementById('emptyEquipmentState').classList.remove('hidden');
        }
    } catch (error) {
        alert('Greška pri brisanju oglasa.');
    }
}

function renderEquipmentPageApi() {
    const equipmentGrid = document.getElementById('equipment-grid');
    const emptyEquipmentState = document.getElementById('emptyEquipmentState');
    const listingCreatedMessage = document.getElementById('listingCreatedMessage');
    const filterBtns = document.querySelectorAll('#category-filters .filter-btn');
    const searchInput = document.getElementById('equipment-search');
    const createEquipmentBtn = document.getElementById('createEquipmentBtn');
    const equipmentCalloutBox = document.getElementById('equipmentCalloutBox');
    const currentRoleForBtn = getEquipmentRole(); //vezu se za logiku prikaza gumba za kreiranje opreme
    const currentTokenForBtn = getEquipmentToken(); //vezu se za logiku prikaza gumba za kreiranje opreme
    
    // Prikaži gumb samo ako je korisnik prijavljen i ima ulogu IZVODAC
    if (createEquipmentBtn) {
        if (currentTokenForBtn && currentRoleForBtn === 'IZVODAC') {
            createEquipmentBtn.style.display = 'inline-flex';
            equipmentCalloutBox.style.display = 'flex';
        } else {
            createEquipmentBtn.style.display = 'none';
            equipmentCalloutBox.style.display = 'none';
        }
    }

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

        return equipmentApiListings.filter((listing) => {
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
        const currentUsername = getEquipmentUsername() || null;
        const currentRole = getEquipmentRole() || null;

        const filteredListings = getFilteredListings();

        if (!filteredListings.length) {
            equipmentGrid.innerHTML = '';
            emptyEquipmentState.classList.remove('hidden');
            feather.replace();
            return;
        }

        emptyEquipmentState.classList.add('hidden');
        equipmentGrid.innerHTML = filteredListings.map((listing) => {
            const id = listing.id_opreme;
            const naziv = escapeHtml(listing.naziv_opreme || 'Nepoznata oprema');
            const kategorija = escapeHtml(listing.kategorija || 'Ostalo');
            const slika = listing.slika ? 'http://localhost:8080' + listing.slika : EQUIPMENT_DEFAULT_IMAGE;
            const lokacije = Array.isArray(listing.lokacije) && listing.lokacije.length
                ? listing.lokacije.map((lokacija) => escapeHtml(lokacija)).join(', ')
                : 'Lokacija nije navedena';

            // Prikaži gumb za brisanje samo vlasniku
            const isVlasnik = currentRole === 'IZVODAC' && currentUsername === listing.vlasnik_opreme;
            const deleteBtn = isVlasnik
                ? `<button
                      data-delete-id="${id}"
                      title="Ukloni oglas"
                      class="delete-equipment-btn flex items-center justify-center w-8 h-8 rounded-full bg-red-700/20 hover:bg-red-600 text-red-400 hover:text-white transition"
                      aria-label="Ukloni oglas">
                      <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4 pointer-events-none" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                          <polyline points="3 6 5 6 21 6"></polyline>
                          <path d="M19 6l-1 14H6L5 6"></path>
                          <path d="M10 11v6"></path>
                          <path d="M14 11v6"></path>
                          <path d="M9 6V4h6v2"></path>
                      </svg>
                   </button>`
                : '';

            return `
                <article id="equipment-card-${id}" class="bg-gray-800 rounded-xl overflow-hidden border border-gray-700 hover:border-purple-500/50 hover:-translate-y-1 transition duration-200 shadow-lg">
                    <img src="${slika}" alt="${naziv}" class="w-full h-48 object-cover" onerror="this.src='${EQUIPMENT_DEFAULT_IMAGE}'">
                    <div class="p-4">
                        <div class="flex justify-between items-start gap-2 mb-3">
                            <h3 class="font-semibold text-lg leading-tight">${naziv}</h3>
                            <div class="flex items-center gap-2 flex-shrink-0">
                                <span class="bg-purple-600 text-xs px-3 py-1 rounded-full whitespace-nowrap">${kategorija}</span>
                                ${deleteBtn}
                            </div>
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

        // Veži event listenere za brisanje
        equipmentGrid.querySelectorAll('.delete-equipment-btn').forEach((btn) => {
            btn.addEventListener('click', () => {
                const id = parseInt(btn.dataset.deleteId, 10);
                const card = document.getElementById(`equipment-card-${id}`);
                deleteEquipmentListing(id, card);
            });
        });

        feather.replace();
    }

    function showLoadError(message) {
        emptyEquipmentState.classList.add('hidden');
        equipmentGrid.innerHTML = `
            <div class="sm:col-span-2 lg:col-span-4 rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-4 text-red-300 text-center">
                ${String(message ?? '').replace(/</g, '&lt;')}
            </div>
        `;
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

    equipmentGrid.innerHTML = '<p class="sm:col-span-2 lg:col-span-4 text-center text-gray-400">Učitavanje opreme...</p>';

    fetchEquipmentListings()
        .then(renderEquipment)
        .catch((error) => {
            showLoadError(error.message || 'Greška pri dohvaćanju opreme.');
        });
}

function renderCreateEquipmentPageApi() {
    const token = getEquipmentToken();
    const role = getEquipmentRole();

    if (!token || role !== 'IZVODAC') {
        window.location.href = 'equipment.html';
        return;
    }

    const form = document.getElementById('equipmentListingForm');
    const successMessage = document.getElementById('successMessage');
    const errorMessage = document.getElementById('errorMessage');
    const imageInput = document.getElementById('slika');
    const imagePreview = document.getElementById('imagePreview');
    const imagePreviewWrapper = document.getElementById('imagePreviewWrapper');
    let selectedImageFile = null;

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
        selectedImageFile = null;
        imagePreview.src = '';
        imagePreviewWrapper.classList.add('hidden');
    }

    imageInput.addEventListener('change', () => {
        const file = imageInput.files[0];

        if (!file) {
            resetImagePreview();
            return;
        }

        // Validacija
        if (file.size > 5 * 1024 * 1024) {
            setMessage(errorMessage, 'Datoteka je prevelika. Maksimalna veličina je 5MB.');
            imageInput.value = '';
            return;
        }

        if (!file.type.startsWith('image/')) {
            setMessage(errorMessage, 'Datoteka nije slika. Odaberi sliku u formatu JPG, PNG, GIF ili WebP.');
            imageInput.value = '';
            return;
        }

        selectedImageFile = file;
        clearMessages();

        // Prikaži pregled
        const reader = new FileReader();
        reader.onload = (e) => {
            imagePreview.src = e.target.result;
            imagePreviewWrapper.classList.remove('hidden');
        };
        reader.onerror = () => {
            setMessage(errorMessage, 'Greška pri čitanju datoteke.');
            resetImagePreview();
        };
        reader.readAsDataURL(file);
    });

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        clearMessages();

        const lokacije = parseEquipmentLocations(document.getElementById('lokacije').value);

        if (!lokacije.length) {
            setMessage(errorMessage, 'Unesi barem jednu lokaciju dostupnosti.');
            return;
        }

        const formData = new FormData();
        formData.append('nazivOpreme', document.getElementById('nazivOpreme').value.trim());
        formData.append('cijena', Number(document.getElementById('cijena').value));
        formData.append('kategorija', document.getElementById('kategorija').value);
        lokacije.forEach((lokacija, index) => {
            formData.append(`lokacije`, lokacija);
        });

        if (selectedImageFile) {
            formData.append('slika', selectedImageFile);
        }

        try {
            const response = await fetch(`${EQUIPMENT_API_BASE_URL}/equipment`, {
                method: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + token
                },
                body: formData
                // Napomena: Ne postavljamo Content-Type header - browser će automatski postaviti multipart/form-data
            });

            if (!response.ok) {
                throw new Error(await parseEquipmentApiError(response, 'Greška pri spremanju oglasa za opremu.'));
            }

            setMessage(successMessage, 'Oglas je uspješno spremljen. Preusmjeravanje na stranicu opreme...');
            form.reset();
            resetImagePreview();

            window.setTimeout(() => {
                window.location.href = 'equipment.html?created=1';
            }, 900);
        } catch (error) {
            setMessage(errorMessage, error.message || 'Greška pri spremanju oglasa za opremu.');
        }
    });
}

document.addEventListener('DOMContentLoaded', () => {
    feather.replace();

    if (document.getElementById('equipmentListingForm')) {
        renderCreateEquipmentPageApi();
    }

    if (document.getElementById('equipment-grid')) {
        renderEquipmentPageApi();
    }
});