document.addEventListener('DOMContentLoaded', function () {
    const busTypeSelect = document.getElementById('busTypeSelect');
    const seatPreviewBadge = document.getElementById('seatCountPreview');
    if (busTypeSelect && seatPreviewBadge) {
        const updatePreview = () => {
            const val = busTypeSelect.value;
            if (val.includes('36')) seatPreviewBadge.textContent = 'Tự động tạo 36 ghế (2 tầng)';
            else if (val.includes('40')) seatPreviewBadge.textContent = 'Tự động tạo 40 ghế (2 tầng)';
            else if (val.includes('45')) seatPreviewBadge.textContent = 'Tự động tạo 45 ghế (1 tầng)';
        };
        busTypeSelect.addEventListener('change', updatePreview);
        updatePreview();
    }
});
