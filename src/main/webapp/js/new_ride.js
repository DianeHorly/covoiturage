// src/main/webapp/js/new_ride.js

document.addEventListener('DOMContentLoaded', function () {

    var depInput   = document.getElementById('departureCity');
    var arrInput   = document.getElementById('arrivalCity');
    var stopsArea  = document.getElementById('stops');
    var priceInput = document.getElementById('pricePerSeat');
    var container  = document.getElementById('segmentsContainer');
    var hiddenCsv  = document.getElementById('segmentPricesCsv');
    var form       = document.getElementById('rideForm');

    if (!depInput || !arrInput || !stopsArea || !container || !hiddenCsv || !form) {
        return; 
    }

    /**
     * Reconstruit la liste des segments et affiche un input de prix pour chacun.
     */
    function updateSegments() {
        var dep  = depInput.value.trim();
        var arr  = arrInput.value.trim();
        var txt  = stopsArea.value || "";
        var basePrice = parseInt(priceInput.value, 10);

        // On reparse la valeur actuelle du champ caché pour garder les prix saisis
        var savedPrices = [];
        if (hiddenCsv.value && hiddenCsv.value.trim().length > 0) {
            var partsSaved = hiddenCsv.value.split(',');
            for (var i = 0; i < partsSaved.length; i++) {
                var p = parseInt(partsSaved[i], 10);
                savedPrices.push(isNaN(p) ? null : p);
            }
        }

        // Si pas de villes principales, on affiche juste le message d'aide
        if (!dep || !arr) {
            container.innerHTML =
                '<p class="text-muted small mb-0">' +
                'Saisissez d\'abord la ville de départ, les arrêts et la ville d\'arrivée ' +
                'pour voir les segments.' +
                '</p>';
            return;
        }

        // Récupération des arrêts intermédiaires: une ville par ligne
        var lines = txt.split(/\r?\n/);
        var stops = [];
        for (var i = 0; i < lines.length; i++) {
            var city = lines[i].trim();
            if (city.length > 0) {
                stops.push(city);
            }
        }

        // Construction du chemin complet: départ, arrêts et arrivée
        var path = [];
        path.push(dep);
        for (var j = 0; j < stops.length; j++) {
            path.push(stops[j]);
        }
        path.push(arr);

        // Si moins de 2 villes, pas de segments possibles
        if (path.length < 2) {
            container.innerHTML =
                '<p class="text-muted small mb-0">' +
                'Ajoutez au moins une ville de départ et une d\'arrivée.' +
                '</p>';
            return;
        }

        // Construction du HTML des segments
        var html = '<div class="small mb-2 text-muted">' +
                   'Définissez un prix par place pour chaque tronçon :' +
                   '</div>';

        html += '<div class="list-group list-group-flush">';

        for (var k = 0; k < path.length - 1; k++) {
            var label = path[k] + ' \u2192 ' + path[k + 1]; // flèche →

            // Prix pré-rempli: soit celui déjà saisi, soit le prix global
            var segPrice = savedPrices[k];
            if (segPrice == null || isNaN(segPrice)) {
                segPrice = (isNaN(basePrice) ? '' : basePrice);
            }

            html += '' +
              '<div class="list-group-item d-flex justify-content-between align-items-center">' +
                '<div>' + label + '</div>' +
                '<div class="ms-2" style="max-width:130px;">' +
                    '<div class="input-group input-group-sm">' +
                        '<input type="number" class="form-control segment-price" ' +
                               'data-index="' + k + '" ' +
                               'min="0" ' +
                               'value="' + (segPrice === '' ? '' : segPrice) + '">' +
                        '<span class="input-group-text">€</span>' +
                    '</div>' +
                '</div>' +
              '</div>';
        }

        html += '</div>';

        container.innerHTML = html;
    }

    /**
     * Avant l'envoi du formulaire, on récupère les prix des inputs
     * et on remplit le champ caché segmentPricesCsv.
     */
    function syncSegmentPricesToHiddenField() {
        var inputs = container.querySelectorAll('.segment-price');
        var prices = [];
        for (var i = 0; i < inputs.length; i++) {
            var val = inputs[i].value.trim();
            if (val === '') {
                prices.push(''); // Si laisser vide = prix global
            } else {
                var p = parseInt(val, 10);
                prices.push(isNaN(p) ? '' : p);
            }
        }
        hiddenCsv.value = prices.join(',');
    }

    // Recalcule les segments quand on modifie villes ou arrêts
    depInput.addEventListener('blur', updateSegments);
    arrInput.addEventListener('blur', updateSegments);
    stopsArea.addEventListener('blur', updateSegments);

    // Si on modifie le prix global, on ne touche pas aux champs déjà saisis,
    // mais pour un premier calcul ça servira de valeur par défaut.
    priceInput.addEventListener('blur', updateSegments);

    // Lors de la soumission du formulaire
    form.addEventListener('submit', function () {
        syncSegmentPricesToHiddenField();
    });

    // Si la page arrive avec des valeurs déjà présentes (après une erreur),
    // on génère les segments au chargement.
    updateSegments();
});
