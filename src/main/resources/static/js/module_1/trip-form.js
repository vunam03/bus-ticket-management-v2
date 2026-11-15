/**
 * Module 1 - Live Form Validator, Seat Preview & Duration Estimator
 */
document.addEventListener('DOMContentLoaded', function () {
    const busTypeSelect = document.getElementById('busTypeSelect');
    const seatPreviewBadge = document.getElementById('seatCountPreview');
    const seatDiagramMiniPreview = document.getElementById('seatDiagramMiniPreview');
    const departureTimeInput = document.getElementById('departureTimeInput');
    const arrivalTimeInput = document.getElementById('arrivalTimeInput');
    const durationPreviewBadge = document.getElementById('durationPreviewBadge');
    const depProvinceInput = document.getElementById('depProvinceInput');
    const arrProvinceInput = document.getElementById('arrProvinceInput');
    const routeCheckBadge = document.getElementById('routeCheckBadge');
    const licensePlateInput = document.getElementById('licensePlateInput');
    const licensePlateHint = document.getElementById('licensePlateHint');
    const ticketPriceInput = document.getElementById('ticketPriceInput');
    const pricePreviewHint = document.getElementById('pricePreviewHint');

    function renderMiniSeatGrid(seatCount, twoFloors) {
        if (!seatDiagramMiniPreview) return;
        seatDiagramMiniPreview.innerHTML = '';
        const previewLimit = Math.min(seatCount, 18);
        for (let i = 1; i <= previewLimit; i++) {
            const cell = document.createElement('span');
            const isSecondFloor = twoFloors && i > Math.ceil(previewLimit / 2);
            cell.className = 'seat-preview-cell' + (isSecondFloor ? ' floor-2' : '');
            if (twoFloors) {
                cell.textContent = (isSecondFloor ? 'B' : 'A') + String(i).padStart(2, '0');
            } else {
                cell.textContent = 'G' + String(i).padStart(2, '0');
            }
            seatDiagramMiniPreview.appendChild(cell);
        }
    }

    function updateBusTypePreview() {
        if (!busTypeSelect) return;
        const val = busTypeSelect.value || '';
        if (val.includes('36')) {
            if (seatPreviewBadge) seatPreviewBadge.textContent = 'Tự động tạo 36 ghế Limousine (2 tầng)';
            renderMiniSeatGrid(36, true);
        } else if (val.includes('40')) {
            if (seatPreviewBadge) seatPreviewBadge.textContent = 'Tự động tạo 40 ghế Giường nằm (2 tầng)';
            renderMiniSeatGrid(40, true);
        } else if (val.includes('45')) {
            if (seatPreviewBadge) seatPreviewBadge.textContent = 'Tự động tạo 45 ghế Ngồi tiêu chuẩn (1 tầng)';
            renderMiniSeatGrid(45, false);
        }
    }

    function updateDurationPreview() {
        if (!departureTimeInput || !arrivalTimeInput || !durationPreviewBadge) return;
        const dep = new Date(departureTimeInput.value);
        const arr = new Date(arrivalTimeInput.value);
        if (!departureTimeInput.value || !arrivalTimeInput.value || isNaN(dep.getTime()) || isNaN(arr.getTime())) {
            durationPreviewBadge.textContent = 'Chưa xác định';
            return;
        }
        const diffMinutes = Math.round((arr.getTime() - dep.getTime()) / 60000);
        if (diffMinutes <= 0) {
            durationPreviewBadge.textContent = 'Giờ đến phải sau giờ đi!';
            durationPreviewBadge.className = 'text-danger';
            return;
        }
        const hrs = Math.floor(diffMinutes / 60);
        const mins = diffMinutes % 60;
        durationPreviewBadge.textContent = mins === 0 ? hrs + ' giờ' : hrs + ' giờ ' + mins + ' phút';
        durationPreviewBadge.className = 'text-dark';
    }

    function updateRouteCheck() {
        if (!depProvinceInput || !arrProvinceInput || !routeCheckBadge) return;
        const dep = depProvinceInput.value.trim().toLowerCase();
        const arr = arrProvinceInput.value.trim().toLowerCase();
        if (dep && arr && dep === arr) {
            routeCheckBadge.textContent = 'Điểm đi & đến trùng nhau!';
            routeCheckBadge.className = 'text-danger';
        } else {
            routeCheckBadge.textContent = 'Tuyến đường hợp lệ';
            routeCheckBadge.className = 'text-success';
        }
    }

    function updatePlateCheck() {
        if (!licensePlateInput || !licensePlateHint) return;
        const plate = licensePlateInput.value.trim().toUpperCase();
        const valid = /^[0-9]{2}[A-Z]{1,2}-?[0-9]{3,5}(\.[0-9]{2})?$/.test(plate.replace(/\s+/g, ''));
        if (!plate) {
            licensePlateHint.textContent = 'VD: 29B-123.45 hoặc 51B-67890';
            licensePlateHint.className = 'form-text text-muted';
        } else if (valid) {
            licensePlateHint.textContent = 'Biển kiểm soát hợp lệ: ' + plate;
            licensePlateHint.className = 'form-text text-success';
        } else {
            licensePlateHint.textContent = 'Kiểm tra lại định dạng biển số (VD: 29B-123.45)';
            licensePlateHint.className = 'form-text text-warning';
        }
    }

    function updatePricePreview() {
        if (!ticketPriceInput || !pricePreviewHint) return;
        const val = Number(ticketPriceInput.value);
        if (!isNaN(val) && val > 0) {
            pricePreviewHint.textContent = new Intl.NumberFormat('vi-VN').format(val) + ' VNĐ / ghế';
        } else {
            pricePreviewHint.textContent = '';
        }
    }

    if (busTypeSelect) {
        busTypeSelect.addEventListener('change', updateBusTypePreview);
        updateBusTypePreview();
    }
    if (departureTimeInput && arrivalTimeInput) {
        departureTimeInput.addEventListener('change', updateDurationPreview);
        arrivalTimeInput.addEventListener('change', updateDurationPreview);
        updateDurationPreview();
    }
    if (depProvinceInput && arrProvinceInput) {
        depProvinceInput.addEventListener('input', updateRouteCheck);
        arrProvinceInput.addEventListener('input', updateRouteCheck);
        updateRouteCheck();
    }
    if (licensePlateInput) {
        licensePlateInput.addEventListener('input', updatePlateCheck);
        updatePlateCheck();
    }
    if (ticketPriceInput) {
        ticketPriceInput.addEventListener('input', updatePricePreview);
        updatePricePreview();
    }
});
