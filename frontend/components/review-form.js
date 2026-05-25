// Business korisnik ostavlja recenziju za rezervaciju.
//
// Otkriva se globalna funkcija openReviewForm(rezervacija, opts).
//   rezervacija: objekt s idRezervacije, izvodacIme, izvodacPrezime, izvodacUsername, nazivPonude
//   opts.onSubmitted(recenzijaResponse): callback nakon uspješnog slanja
//
// Ovisi o star-rating.js (mountStarPicker) i o globalnom API_BASE_URL iz config.js.

(function () {
    'use strict';

    function ensureModalRoot() {
        let root = document.getElementById('reviewFormModalRoot');
        if (root) return root;
        root = document.createElement('div');
        root.id = 'reviewFormModalRoot';
        document.body.appendChild(root);
        return root;
    }

    function escapeHtml(s) {
        if (s == null) return '';
        return String(s)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function openReviewForm(rez, opts) {
        opts = opts || {};
        const root = ensureModalRoot();
        const ime = `${rez.izvodacIme || ''} ${rez.izvodacPrezime || ''}`.trim() || rez.izvodacUsername || 'izvođača';
        const ponuda = rez.nazivPonude ? `· ${rez.nazivPonude}` : '';

        root.innerHTML = `
            <div id="reviewFormOverlay" class="fixed inset-0 bg-black bg-opacity-60 z-50 flex items-center justify-center">
                <div class="bg-gray-800 rounded-lg p-6 w-full max-w-md mx-4 shadow-2xl border border-gray-700">
                    <div class="flex items-start justify-between mb-4">
                        <div>
                            <h3 class="text-lg font-semibold">Ostavi recenziju</h3>
                            <p class="text-sm text-gray-400 mt-1">${escapeHtml(ime)} ${escapeHtml(ponuda)}</p>
                        </div>
                        <button id="reviewFormClose" class="text-gray-400 hover:text-white text-xl leading-none">×</button>
                    </div>

                    <label class="block text-sm text-gray-400 mb-1">Ocjena</label>
                    <div id="reviewStarPicker" class="mb-4"></div>

                    <label class="block text-sm text-gray-400 mb-1" for="reviewKomentar">Komentar (neobavezno)</label>
                    <textarea id="reviewKomentar" maxlength="1000" rows="4"
                        class="w-full bg-gray-700 border border-gray-600 rounded-lg p-3 text-sm text-gray-100 focus:outline-none focus:border-purple-500 resize-none"
                        placeholder="Kako je prošla suradnja?"></textarea>
                    <p id="reviewKomentarCount" class="text-xs text-gray-500 mt-1">0 / 1000</p>

                    <div id="reviewFormError" class="hidden mt-3 text-sm text-red-400"></div>

                    <div class="flex justify-end gap-2 mt-5">
                        <button id="reviewFormCancel"
                            class="px-4 py-2 bg-gray-700 hover:bg-gray-600 rounded-lg text-sm">Odustani</button>
                        <button id="reviewFormSubmit"
                            class="px-4 py-2 bg-purple-600 hover:bg-purple-700 rounded-lg text-sm font-medium disabled:opacity-50">
                            Pošalji recenziju
                        </button>
                    </div>
                </div>
            </div>
        `;

        const overlay = document.getElementById('reviewFormOverlay');
        const closeBtn = document.getElementById('reviewFormClose');
        const cancelBtn = document.getElementById('reviewFormCancel');
        const submitBtn = document.getElementById('reviewFormSubmit');
        const komentarEl = document.getElementById('reviewKomentar');
        const komentarCount = document.getElementById('reviewKomentarCount');
        const errEl = document.getElementById('reviewFormError');
        const pickerEl = document.getElementById('reviewStarPicker');

        const picker = window.mountStarPicker(pickerEl, { initial: 0 });

        function close() {
            overlay.remove();
        }

        komentarEl.addEventListener('input', function () {
            komentarCount.textContent = `${komentarEl.value.length} / 1000`;
        });

        closeBtn.addEventListener('click', close);
        cancelBtn.addEventListener('click', close);
        overlay.addEventListener('click', function (e) {
            if (e.target === overlay) close();
        });

        submitBtn.addEventListener('click', async function () {
            errEl.classList.add('hidden');
            const ocjena = picker.getValue();
            if (ocjena < 1) {
                errEl.textContent = 'Molimo odaberite ocjenu od 1 do 5 zvjezdica.';
                errEl.classList.remove('hidden');
                return;
            }
            submitBtn.disabled = true;
            submitBtn.textContent = 'Šaljem...';
            try {
                const token = localStorage.getItem('token');
                const res = await fetch(`${API_BASE_URL}/recenzije`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': 'Bearer ' + token
                    },
                    body: JSON.stringify({
                        idRezervacije: rez.idRezervacije,
                        ocjena: ocjena,
                        komentar: komentarEl.value.trim() || null
                    })
                });
                if (!res.ok) {
                    let msg = 'Greška pri slanju recenzije.';
                    try {
                        const data = await res.json();
                        if (data && (data.message || data.error)) msg = data.message || data.error;
                    } catch (_) {}
                    throw new Error(msg);
                }
                const body = await res.json();
                close();
                if (typeof opts.onSubmitted === 'function') opts.onSubmitted(body);
            } catch (e) {
                errEl.textContent = e.message || 'Greška pri slanju recenzije.';
                errEl.classList.remove('hidden');
                submitBtn.disabled = false;
                submitBtn.textContent = 'Pošalji recenziju';
            }
        });
    }

    window.openReviewForm = openReviewForm;
})();
