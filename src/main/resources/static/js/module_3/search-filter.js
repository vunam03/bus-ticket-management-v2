// Module 3: Tự động nộp bộ lọc tìm kiếm, đảo chiều tỉnh thành, chọn nhanh khoảng giá và lọc tuyến
document.addEventListener('DOMContentLoaded', function () {
    const filterForm = document.getElementById('tripSearchFilterForm');
    if (filterForm) {
        const autoSubmitControls = filterForm.querySelectorAll('.auto-submit-filter');
        autoSubmitControls.forEach(function (control) {
            control.addEventListener('change', function () {
                filterForm.submit();
            });
        });

        const resetBtn = document.getElementById('resetSearchFilterBtn');
        if (resetBtn) {
            resetBtn.addEventListener('click', function () {
                const actionUrl = filterForm.getAttribute('action') || '/trips/search';
                window.location.href = actionUrl;
            });
        }

        const pricePresetButtons = filterForm.querySelectorAll('[data-price-preset]');
        pricePresetButtons.forEach(function (btn) {
            btn.addEventListener('click', function () {
                const minInput = filterForm.querySelector('input[name="minPrice"]');
                const maxInput = filterForm.querySelector('input[name="maxPrice"]');
                if (minInput) {
                    minInput.value = btn.getAttribute('data-min-price') || '';
                }
                if (maxInput) {
                    maxInput.value = btn.getAttribute('data-max-price') || '';
                }
                filterForm.submit();
            });
        });

        const sortPills = filterForm.querySelectorAll('[data-sort-value]');
        const sortSelect = filterForm.querySelector('select[name="sortBy"]');
        sortPills.forEach(function (pill) {
            pill.addEventListener('click', function () {
                if (sortSelect) {
                    sortSelect.value = pill.getAttribute('data-sort-value') || 'TIME_ASC';
                    filterForm.submit();
                }
            });
        });
    }

    const swapBtn = document.getElementById('swapProvinceBtn');
    const depSelect = document.getElementById('depProvinceSelect');
    const arrSelect = document.getElementById('arrProvinceSelect');
    if (swapBtn && depSelect && arrSelect) {
        swapBtn.addEventListener('click', function () {
            const previousDep = depSelect.value;
            const previousArr = arrSelect.value;
            const arrHasPrevDep = Array.from(arrSelect.options).some(opt => opt.value === previousDep);
            const depHasPrevArr = Array.from(depSelect.options).some(opt => opt.value === previousArr);
            if (depHasPrevArr) {
                depSelect.value = previousArr;
            }
            if (arrHasPrevDep) {
                arrSelect.value = previousDep;
            }
        });
    }

    const quickInput = document.getElementById('quickRouteFilterInput');
    const routeItems = document.querySelectorAll('.route-directory-item');
    if (quickInput && routeItems.length > 0) {
        quickInput.addEventListener('input', function () {
            const keyword = quickInput.value.trim().toLowerCase();
            routeItems.forEach(function (item) {
                const text = item.textContent ? item.textContent.toLowerCase() : '';
                item.style.display = (!keyword || text.includes(keyword)) ? '' : 'none';
            });
        });
    }
});
