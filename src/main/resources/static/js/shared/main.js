// Shared utilities for BusBooking VN
document.addEventListener('DOMContentLoaded', function () {
    // Auto-dismiss flash alerts after 5.5 seconds
    const alerts = document.querySelectorAll('.alert.alert-dismissible');
    alerts.forEach(function (alertEl) {
        setTimeout(function () {
            if (typeof bootstrap !== 'undefined' && bootstrap.Alert) {
                const bsAlert = bootstrap.Alert.getOrCreateInstance(alertEl);
                bsAlert.close();
            }
        }, 5500);
    });

    // Global [data-confirm] handler on buttons and links
    document.addEventListener('click', function (event) {
        const trigger = event.target.closest('[data-confirm]');
        if (!trigger) {
            return;
        }
        const message = trigger.getAttribute('data-confirm') || 'Bạn có chắc chắn muốn thực hiện thao tác này?';
        if (!window.confirm(message)) {
            event.preventDefault();
            event.stopPropagation();
        }
    });
});
