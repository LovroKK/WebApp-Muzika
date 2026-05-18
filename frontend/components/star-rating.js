// Star rating helpers — koriste se za prikaz prosječne ocjene i
// kao interaktivni picker u review formi.
//
// Globalne funkcije izložene preko window:
//   renderStars(avg, count, opts?) -> string (read-only display)
//   mountStarPicker(container, opts?) -> { getValue, setValue }
//
// Nema vanjskih ovisnosti; čisti Tailwind za stilove.

(function () {
    'use strict';

    function clamp(v, lo, hi) {
        v = Number(v);
        if (isNaN(v)) return lo;
        return Math.max(lo, Math.min(hi, v));
    }

    function starSvg(fill) {
        // fill: 'full' | 'half' | 'empty'
        // Koristimo isti viewBox za sve, ali mijenjamo gradient maskom za 'half'.
        const id = 'g' + Math.random().toString(36).slice(2, 8);
        if (fill === 'full') {
            return `<svg viewBox="0 0 24 24" class="w-4 h-4 inline" fill="#facc15" stroke="#facc15" stroke-width="1">
                <polygon points="12,2 15.09,8.26 22,9.27 17,14.14 18.18,21.02 12,17.77 5.82,21.02 7,14.14 2,9.27 8.91,8.26"/>
            </svg>`;
        }
        if (fill === 'half') {
            return `<svg viewBox="0 0 24 24" class="w-4 h-4 inline" stroke="#facc15" stroke-width="1">
                <defs>
                    <linearGradient id="${id}">
                        <stop offset="50%" stop-color="#facc15"/>
                        <stop offset="50%" stop-color="transparent"/>
                    </linearGradient>
                </defs>
                <polygon fill="url(#${id})" points="12,2 15.09,8.26 22,9.27 17,14.14 18.18,21.02 12,17.77 5.82,21.02 7,14.14 2,9.27 8.91,8.26"/>
            </svg>`;
        }
        return `<svg viewBox="0 0 24 24" class="w-4 h-4 inline" fill="none" stroke="#6b7280" stroke-width="1.5">
            <polygon points="12,2 15.09,8.26 22,9.27 17,14.14 18.18,21.02 12,17.77 5.82,21.02 7,14.14 2,9.27 8.91,8.26"/>
        </svg>`;
    }

    function renderStars(avg, count, opts) {
        opts = opts || {};
        const showCount = opts.showCount !== false;
        const compact = opts.compact === true;
        const empty = avg == null || count == null || Number(count) === 0;

        if (empty) {
            return `<span class="text-xs text-gray-500" title="Bez recenzija">
                ${starSvg('empty')}${starSvg('empty')}${starSvg('empty')}${starSvg('empty')}${starSvg('empty')}
                ${compact ? '' : '<span class="ml-1">Bez ocjena</span>'}
            </span>`;
        }

        const value = clamp(avg, 0, 5);
        let html = '<span class="inline-flex items-center">';
        for (let i = 1; i <= 5; i++) {
            const diff = value - (i - 1);
            if (diff >= 0.75) html += starSvg('full');
            else if (diff >= 0.25) html += starSvg('half');
            else html += starSvg('empty');
        }
        if (showCount) {
            html += `<span class="ml-1 text-xs text-gray-300">${Number(value).toFixed(1)} (${count})</span>`;
        }
        html += '</span>';
        return html;
    }

    function mountStarPicker(container, opts) {
        opts = opts || {};
        let current = clamp(opts.initial || 0, 0, 5);

        function paint() {
            let html = '';
            for (let i = 1; i <= 5; i++) {
                const filled = i <= current;
                html += `<button type="button" data-star="${i}"
                    class="px-1 transition-transform hover:scale-110"
                    aria-label="${i} zvjezdic${i === 1 ? 'a' : 'e'}">
                    ${filled ? starSvg('full') : starSvg('empty')}
                </button>`;
            }
            html += `<span class="ml-3 text-sm text-gray-300" data-star-label>${current.toFixed(0)} / 5</span>`;
            container.innerHTML = html;
        }

        container.classList.add('inline-flex', 'items-center');
        container.addEventListener('click', function (e) {
            const btn = e.target.closest('[data-star]');
            if (!btn) return;
            current = parseInt(btn.dataset.star, 10);
            paint();
            if (typeof opts.onChange === 'function') opts.onChange(current);
        });

        paint();

        return {
            getValue: function () { return current; },
            setValue: function (v) { current = clamp(v, 0, 5); paint(); }
        };
    }

    window.renderStars = renderStars;
    window.mountStarPicker = mountStarPicker;
})();
