const EQUIPMENT_API_BASE_URL = 'http://localhost:8080/api';
const EQUIPMENT_DEFAULT_IMAGE = 'http://static.photos/technology/200x200/0';

let equipmentApiListings = [];

function getEquipmentToken() {
    return localStorage.getItem('token');
}

function getEquipmentRole() {
    return localStorage.getItem('role');
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

function renderEquipmentPageApi() {
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
            const slika = listing.slika || EQUIPMENT_DEFAULT_IMAGE;
            const lokacije = Array.isArray(listing.lokacije) && listing.lokacije.length
                ? listing.lokacije.map((lokacija) => escapeHtml(lokacija)).join(', ')
                : 'Lokacija nije navedena';

            return `
                <article class="bg-gray-800 rounded-xl overflow-hidden border border-gray-700 hover:border-purple-500/50 hover:-translate-y-1 transition duration-200 shadow-lg">
                    <img src="${slika}" alt="${naziv}" class="w-full h-48 object-cover" onerror="this.src='${EQUIPMENT_DEFAULT_IMAGE}'">
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

    function showLoadError(message) {
        emptyEquipmentState.classList.add('hidden');
        equipmentGrid.innerHTML = `
            <div class="sm:col-span-2 lg:col-span-4 rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-4 text-red-300 text-center">
                ${escapeHtml(message)}
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
    let selectedImageUrl = '';

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
        selectedImageUrl = '';
        imagePreview.src = '';
        imagePreviewWrapper.classList.add('hidden');
    }

    imageInput.addEventListener('input', () => {
        selectedImageUrl = imageInput.value.trim();

        if (!selectedImageUrl) {
            resetImagePreview();
            return;
        }

        imagePreview.src = selectedImageUrl;
        imagePreviewWrapper.classList.remove('hidden');
    });

    imagePreview.addEventListener('error', () => {
        imagePreviewWrapper.classList.add('hidden');
        setMessage(errorMessage, 'Uneseni URL slike nije valjan ili slika nije dostupna.');
    });

    imagePreview.addEventListener('load', () => {
        if (selectedImageUrl) {
            clearMessages();
            imagePreviewWrapper.classList.remove('hidden');
        }
    });

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        clearMessages();

        const lokacije = parseEquipmentLocations(document.getElementById('lokacije').value);

        if (!lokacije.length) {
            setMessage(errorMessage, 'Unesi barem jednu lokaciju dostupnosti.');
            return;
        }

        const payload = {
            nazivOpreme: document.getElementById('nazivOpreme').value.trim(),
            cijena: Number(document.getElementById('cijena').value),
            kategorija: document.getElementById('kategorija').value,
            slika: selectedImageUrl || null,
            lokacije
        };

        try {
            const response = await fetch(`${EQUIPMENT_API_BASE_URL}/equipment`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                body: JSON.stringify(payload)
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
