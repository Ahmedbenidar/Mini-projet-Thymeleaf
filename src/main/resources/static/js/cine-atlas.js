document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('form[data-confirmation]').forEach(function (formulaire) {
        formulaire.addEventListener('submit', function (evenement) {
            var question = formulaire.getAttribute('data-confirmation');
            if (question && !window.confirm(question)) {
                evenement.preventDefault();
            }
        });
    });
});
