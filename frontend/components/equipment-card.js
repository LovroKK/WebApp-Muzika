class EquipmentCard extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
    }

    connectedCallback() {
        const name = this.getAttribute('name') || 'Equipment';
        const price = this.getAttribute('price') || '0';
        const category = this.getAttribute('category') || 'Other';
        const image = this.getAttribute('image') || 'http://static.photos/technology/200x200/0';
        
        this.shadowRoot.innerHTML = `
            <style>
                .card {
                    transition: transform 0.2s, box-shadow 0.2s;
                }
                .card:hover {
                    transform: translateY(-4px);
                    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
                }
            </style>
            <div class="card bg-gray-800 rounded-lg overflow-hidden">
                <img src="${image}" alt="${name}" class="w-full h-48 object-cover">
                <div class="p-4">
                    <div class="flex justify-between items-start mb-2">
                        <h3 class="font-semibold text-lg">${name}</h3>
                        <span class="bg-purple-600 text-xs px-2 py-1 rounded">${category}</span>
                    </div>
                    <p class="text-gray-400 text-sm mb-4">Available in Zagreb</p>
                    <div class="flex justify-between items-center">
                        <span class="text-purple-400 font-medium">€${price}/day</span>
                        <button class="bg-purple-600 hover:bg-purple-700 px-3 py-1 rounded text-sm">
                            Rent Now
                        </button>
                    </div>
                </div>
            </div>
        `;
    }
}

customElements.define('equipment-card', EquipmentCard);