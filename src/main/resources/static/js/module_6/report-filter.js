/**
 * Module 6 - Report Quick Date Range & Export Link Synchronizer
 */
function applyQuickDateFilter(rangeType) {
    const startInput = document.getElementById('startDateInput');
    const endInput = document.getElementById('endDateInput');
    const form = document.getElementById('revenueFilterForm');
    if (!startInput || !endInput || !form) return;

    const today = new Date();
    let start = new Date();

    if (rangeType === 'TODAY') {
        start = today;
    } else if (rangeType === 'LAST_7_DAYS') {
        start.setDate(today.getDate() - 6);
    } else if (rangeType === 'THIS_MONTH') {
        start = new Date(today.getFullYear(), today.getMonth(), 1);
    } else if (rangeType === 'THIS_YEAR') {
        start = new Date(today.getFullYear(), 0, 1);
    }

    startInput.value = formatDateIso(start);
    endInput.value = formatDateIso(today);
    form.submit();
}

function formatDateIso(dateObj) {
    const y = dateObj.getFullYear();
    const m = String(dateObj.getMonth() + 1).padStart(2, '0');
    const d = String(dateObj.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}
