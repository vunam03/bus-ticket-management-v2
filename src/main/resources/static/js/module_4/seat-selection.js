// Module 4: Sơ đồ ghế trực quan, đồng bộ trạng thái gần thời gian thực (polling),
// gợi ý ghế thông minh theo tầng, giới hạn 6 ghế, gia hạn & hủy giữ chỗ.
document.addEventListener('DOMContentLoaded', function () {
    const appRoot = document.getElementById('seatSelectionApp');
    const seatForm = document.getElementById('seatHoldForm');
    const inputsContainer = document.getElementById('selectedSeatInputsContainer');
    const countDisplay = document.getElementById('selectedSeatsCount');
    const seatsDisplay = document.getElementById('selectedSeatsDisplay');
    const totalPriceDisplay = document.getElementById('selectedTotalPrice');
    const submitBtn = document.getElementById('submitHoldSeatsBtn');
    const limitWarning = document.getElementById('seatLimitWarning');
    const realtimeAlert = document.getElementById('seatRealtimeAlert');
    const clearBtn = document.getElementById('clearSelectedSeatsBtn');
    const sidebarClearBtn = document.getElementById('sidebarClearSeatsBtn');
    const quickPickButtons = document.querySelectorAll('.quick-pick-seat-btn');
    const suggestFloorSelect = document.getElementById('suggestFloorSelect');
    const suggestFeedback = document.getElementById('smartSuggestFeedback');
    const liveSyncBadge = document.getElementById('liveSyncBadge');
    const liveAvailEl = document.getElementById('liveAvailableSeatsCount');
    const liveHeldEl = document.getElementById('liveHeldSeatsCount');
    const liveBookedEl = document.getElementById('liveBookedSeatsCount');
    const floor1AvailBadge = document.getElementById('floor1AvailBadge');
    const floor2AvailBadge = document.getElementById('floor2AvailBadge');

    // Khối quản lý phiên giữ ghế đang chạy (nếu người dùng đã giữ chỗ trước đó)
    const activeHoldPanel = document.getElementById('activeHoldPanel');
    const activeHoldCodeText = document.getElementById('activeHoldCodeText');
    const activeHoldCountdown = document.getElementById('activeHoldCountdown');
    const extendHoldBtn = document.getElementById('extendHoldBtn');
    const releaseActiveHoldBtn = document.getElementById('releaseActiveHoldBtn');
    const resumeCheckoutLink = document.getElementById('resumeCheckoutLink');
    const holdActionFeedback = document.getElementById('holdActionFeedback');

    if (!appRoot || !inputsContainer) {
        return;
    }

    const tripId = appRoot.getAttribute('data-trip-id');
    const tripBookable = appRoot.getAttribute('data-trip-bookable') !== 'false';
    const MAX_SEATS = 6;
    const POLL_INTERVAL_MS = 6000;

    const selectedSeats = new Map();
    let seatButtons = Array.from(document.querySelectorAll('.seat-btn'));
    let activeHoldCode = appRoot.getAttribute('data-active-hold-code') || '';
    let holdRemainingSeconds = parseInt(appRoot.getAttribute('data-hold-remaining-seconds') || '0', 10);
    let canExtendHold = appRoot.getAttribute('data-can-extend-hold') === 'true';

    let pollTimerId = null;
    let countdownTimerId = null;
    let isPolling = false;

    function getCsrfHeaders() {
        const tokenInput = seatForm ? seatForm.querySelector('input[name="_csrf"]') : null;
        if (tokenInput && tokenInput.value) {
            return { 'X-CSRF-TOKEN': tokenInput.value };
        }
        return {};
    }

    function formatCurrencyVnd(amount) {
        return Math.round(amount).toLocaleString('en-US') + ' đ';
    }

    function formatCountdown(seconds) {
        const safe = Math.max(0, Math.floor(seconds));
        const mins = Math.floor(safe / 60);
        const secs = safe % 60;
        return String(mins).padStart(2, '0') + ':' + String(secs).padStart(2, '0');
    }

    function showLimitWarning(visible) {
        if (!limitWarning) {
            return;
        }
        limitWarning.classList.toggle('d-none', !visible);
    }

    function showRealtimeNotice(message, type) {
        if (!realtimeAlert) {
            return;
        }
        if (!message) {
            realtimeAlert.classList.add('d-none');
            realtimeAlert.textContent = '';
            return;
        }
        realtimeAlert.classList.remove('d-none', 'alert-danger', 'alert-warning', 'alert-info');
        realtimeAlert.classList.add(type === 'info' ? 'alert-info' : (type === 'warning' ? 'alert-warning' : 'alert-danger'));
        realtimeAlert.innerHTML = '<i class="bi bi-bell-fill me-2"></i>' + escapeHtml(message);
    }

    function showSuggestFeedback(message, isWarning) {
        if (!suggestFeedback) {
            return;
        }
        if (!message) {
            suggestFeedback.classList.add('d-none');
            return;
        }
        suggestFeedback.classList.remove('d-none', 'text-success', 'text-warning', 'text-danger');
        suggestFeedback.classList.add(isWarning ? 'text-danger' : 'text-success');
        suggestFeedback.innerHTML = '<i class="bi ' + (isWarning ? 'bi-exclamation-circle' : 'bi-check-circle-fill') + ' me-1"></i>' + escapeHtml(message);
    }

    function showHoldFeedback(message, isError) {
        if (!holdActionFeedback) {
            return;
        }
        if (!message) {
            holdActionFeedback.classList.add('d-none');
            return;
        }
        holdActionFeedback.classList.remove('d-none', 'text-success', 'text-danger');
        holdActionFeedback.classList.add(isError ? 'text-danger' : 'text-success');
        holdActionFeedback.textContent = message;
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function setSyncState(online, labelText) {
        if (!liveSyncBadge) {
            return;
        }
        liveSyncBadge.classList.toggle('is-error', !online);
        liveSyncBadge.classList.toggle('is-online', online);
        const textNode = liveSyncBadge.querySelector('.sync-text');
        if (textNode && labelText) {
            textNode.textContent = labelText;
        }
    }

    function syncButtonClasses() {
        seatButtons.forEach(function (btn) {
            const seatId = btn.getAttribute('data-seat-id');
            if (seatId && selectedSeats.has(seatId)) {
                btn.classList.add('seat-selected');
                btn.setAttribute('aria-pressed', 'true');
            } else {
                btn.classList.remove('seat-selected');
                btn.setAttribute('aria-pressed', 'false');
            }
        });
    }

    function renderSelectionSummary() {
        inputsContainer.innerHTML = '';
        const seatNumbers = [];
        let totalAmount = 0;

        selectedSeats.forEach(function (seatInfo, seatId) {
            seatNumbers.push(seatInfo.number);
            totalAmount += seatInfo.price;

            const hiddenInput = document.createElement('input');
            hiddenInput.type = 'hidden';
            hiddenInput.name = 'seatIds';
            hiddenInput.value = seatId;
            inputsContainer.appendChild(hiddenInput);
        });

        seatNumbers.sort();
        syncButtonClasses();

        if (countDisplay) {
            countDisplay.textContent = selectedSeats.size + ' / ' + MAX_SEATS + ' ghế';
        }
        if (seatsDisplay) {
            if (seatNumbers.length > 0) {
                seatsDisplay.innerHTML = seatNumbers
                    .map(function (code) {
                        return '<span class="selected-seat-pill">' + escapeHtml(code) + '</span>';
                    })
                    .join('');
            } else {
                seatsDisplay.textContent = 'Chưa chọn ghế';
            }
        }
        if (totalPriceDisplay) {
            totalPriceDisplay.textContent = formatCurrencyVnd(totalAmount);
        }
        if (submitBtn) {
            submitBtn.disabled = !tripBookable || selectedSeats.size === 0;
        }
        if (sidebarClearBtn) {
            sidebarClearBtn.disabled = selectedSeats.size === 0;
        }
    }

    function updateActiveHoldPanel() {
        if (!activeHoldPanel) {
            return;
        }
        if (!activeHoldCode || holdRemainingSeconds <= 0) {
            activeHoldPanel.classList.add('d-none');
            return;
        }
        activeHoldPanel.classList.remove('d-none');
        if (activeHoldCodeText) {
            activeHoldCodeText.textContent = activeHoldCode;
        }
        if (activeHoldCountdown) {
            activeHoldCountdown.textContent = formatCountdown(holdRemainingSeconds);
        }
        if (resumeCheckoutLink) {
            resumeCheckoutLink.href = '/booking/checkout?holdCode=' + encodeURIComponent(activeHoldCode);
        }
        if (extendHoldBtn) {
            extendHoldBtn.disabled = !canExtendHold;
            extendHoldBtn.title = canExtendHold ? 'Gia hạn thêm 3 phút (tối đa 15 phút)' : 'Đã đạt giới hạn giữ tối đa 15 phút';
        }
    }

    function addSeatToSelection(btn) {
        const seatId = btn.getAttribute('data-seat-id');
        const seatNumber = btn.getAttribute('data-seat-number');
        const seatPrice = parseFloat(btn.getAttribute('data-seat-price') || '0');
        const status = btn.getAttribute('data-status');
        const heldByMe = btn.getAttribute('data-held-by-me') === 'true';

        if (!seatId || !tripBookable) {
            return false;
        }
        if (status !== 'AVAILABLE' && !heldByMe) {
            return false;
        }
        if (!selectedSeats.has(seatId) && selectedSeats.size >= MAX_SEATS) {
            showLimitWarning(true);
            return false;
        }
        selectedSeats.set(seatId, {
            id: seatId,
            number: seatNumber,
            price: seatPrice
        });
        showLimitWarning(false);
        return true;
    }

    function clearAllSelections() {
        selectedSeats.clear();
        showLimitWarning(false);
        showSuggestFeedback('', false);
        renderSelectionSummary();
    }

    function bindSeatButtons() {
        seatButtons = Array.from(document.querySelectorAll('.seat-btn'));
        seatButtons.forEach(function (btn) {
            if (btn.dataset.bound === 'true') {
                return;
            }
            btn.dataset.bound = 'true';
            btn.addEventListener('click', function () {
                const seatId = btn.getAttribute('data-seat-id');
                if (!seatId || btn.disabled) {
                    return;
                }
                if (selectedSeats.has(seatId)) {
                    selectedSeats.delete(seatId);
                    showLimitWarning(false);
                } else {
                    addSeatToSelection(btn);
                }
                renderSelectionSummary();
            });
        });
    }

    // Khởi tạo chọn sẵn các ghế mà chính người dùng hiện tại đang giữ (nếu có)
    function preselectOwnHeldSeats() {
        seatButtons.forEach(function (btn) {
            if (btn.getAttribute('data-held-by-me') === 'true') {
                addSeatToSelection(btn);
            }
        });
        renderSelectionSummary();
    }

    function applySeatLayoutSnapshot(layout) {
        if (!layout) {
            return;
        }
        if (liveAvailEl) {
            liveAvailEl.textContent = String(layout.availableCount ?? 0);
        }
        if (liveHeldEl) {
            liveHeldEl.textContent = String(layout.heldCount ?? 0);
        }
        if (liveBookedEl) {
            liveBookedEl.textContent = String(layout.bookedCount ?? 0);
        }
        if (floor1AvailBadge) {
            floor1AvailBadge.textContent = String(layout.floor1AvailableCount ?? 0);
        }
        if (floor2AvailBadge) {
            floor2AvailBadge.textContent = String(layout.floor2AvailableCount ?? 0);
        }

        const seatMap = new Map();
        (layout.floor1Seats || []).forEach(function (dto) {
            seatMap.set(String(dto.seatId), dto);
        });
        (layout.floor2Seats || []).forEach(function (dto) {
            seatMap.set(String(dto.seatId), dto);
        });

        const evictedSeatNumbers = [];

        seatButtons.forEach(function (btn) {
            const seatId = btn.getAttribute('data-seat-id');
            const dto = seatMap.get(seatId);
            if (!dto) {
                return;
            }
            btn.setAttribute('data-status', dto.status);
            btn.setAttribute('data-held-by-me', dto.heldByCurrentUser ? 'true' : 'false');
            btn.classList.remove('seat-available', 'seat-held-me', 'seat-held', 'seat-booked', 'seat-unavailable');

            const subEl = btn.querySelector('.seat-sub');
            if (dto.heldByCurrentUser) {
                btn.classList.add('seat-held-me');
                btn.disabled = !layout.tripBookable;
                if (subEl) subEl.textContent = 'Đang giữ';
            } else if (dto.status === 'AVAILABLE') {
                btn.classList.add('seat-available');
                btn.disabled = !layout.tripBookable;
                if (subEl) subEl.textContent = Math.round(dto.price / 1000).toLocaleString('en-US') + 'k';
            } else if (dto.status === 'HELD') {
                btn.classList.add('seat-held');
                btn.disabled = true;
                if (subEl) subEl.textContent = 'Đang giữ';
            } else if (dto.status === 'UNAVAILABLE') {
                btn.classList.add('seat-unavailable');
                btn.disabled = true;
                if (subEl) subEl.textContent = 'Khóa';
            } else {
                btn.classList.add('seat-booked');
                btn.disabled = true;
                if (subEl) subEl.textContent = 'Đã bán';
            }

            // Nếu ghế user đang chọn trên màn hình vừa bị người khác giữ hoặc bán -> chỉ bỏ ghế xung đột đó
            if (selectedSeats.has(seatId) && !dto.selectable) {
                evictedSeatNumbers.push(dto.seatNumber);
                selectedSeats.delete(seatId);
            }
        });

        activeHoldCode = layout.activeHoldCode || '';
        holdRemainingSeconds = layout.holdRemainingSeconds || 0;
        canExtendHold = Boolean(layout.canExtendHold);
        updateActiveHoldPanel();

        if (evictedSeatNumbers.length > 0) {
            showRealtimeNotice(
                'Ghế ' + evictedSeatNumbers.join(', ') + ' vừa được hành khách khác giữ hoặc đã đặt và đã được bỏ khỏi danh sách chọn.',
                'warning'
            );
        }
        renderSelectionSummary();
    }

    async function pollSeatStatus() {
        if (!tripId || isPolling || document.visibilityState === 'hidden') {
            return;
        }
        isPolling = true;
        try {
            const response = await fetch('/api/seats/trip/' + encodeURIComponent(tripId), {
                headers: { 'Accept': 'application/json' },
                cache: 'no-store'
            });
            if (!response.ok) {
                throw new Error('HTTP ' + response.status);
            }
            const layout = await response.json();
            applySeatLayoutSnapshot(layout);
            setSyncState(true, 'Đồng bộ trực tiếp');
        } catch (err) {
            setSyncState(false, 'Mất kết nối, đang thử lại...');
        } finally {
            isPolling = false;
        }
    }

    function startPolling() {
        if (pollTimerId !== null) {
            return;
        }
        pollTimerId = window.setInterval(pollSeatStatus, POLL_INTERVAL_MS);
    }

    function stopPolling() {
        if (pollTimerId !== null) {
            window.clearInterval(pollTimerId);
            pollTimerId = null;
        }
        if (countdownTimerId !== null) {
            window.clearInterval(countdownTimerId);
            countdownTimerId = null;
        }
    }

    // Đồng hồ đếm ngược cho phiên giữ chỗ đang hiệu lực trên trang chọn ghế
    function startHoldCountdown() {
        updateActiveHoldPanel();
        if (countdownTimerId !== null) {
            window.clearInterval(countdownTimerId);
        }
        countdownTimerId = window.setInterval(function () {
            if (!activeHoldCode || holdRemainingSeconds <= 0) {
                return;
            }
            holdRemainingSeconds -= 1;
            updateActiveHoldPanel();
            if (holdRemainingSeconds <= 0) {
                activeHoldCode = '';
                showRealtimeNotice('Phiên giữ ghế trước đó của bạn đã hết hạn.', 'warning');
                pollSeatStatus();
            }
        }, 1000);
    }

    // Gọi API gợi ý ghế thông minh (liền kề & ưu tiên tầng)
    quickPickButtons.forEach(function (quickBtn) {
        quickBtn.addEventListener('click', async function () {
            const desiredCount = Math.min(
                MAX_SEATS,
                Math.max(1, parseInt(quickBtn.getAttribute('data-pick-count') || '1', 10))
            );
            const preferredFloor = suggestFloorSelect ? suggestFloorSelect.value : '';
            const query = new URLSearchParams({ count: String(desiredCount) });
            if (preferredFloor) {
                query.set('preferredFloor', preferredFloor);
            }

            try {
                const res = await fetch('/api/seats/trip/' + encodeURIComponent(tripId) + '/suggest?' + query.toString(), {
                    headers: { 'Accept': 'application/json' },
                    cache: 'no-store'
                });
                const payload = await res.json();
                if (!res.ok) {
                    showSuggestFeedback(payload.message || 'Không thể gợi ý ghế lúc này.', true);
                    return;
                }
                if (!payload.sufficient || !payload.seatIds || !payload.seatIds.length) {
                    showSuggestFeedback(payload.message || 'Không đủ ghế trống theo yêu cầu.', true);
                    return;
                }
                selectedSeats.clear();
                const btnById = new Map(seatButtons.map(function (b) { return [b.getAttribute('data-seat-id'), b]; }));
                payload.seatIds.forEach(function (id) {
                    const targetBtn = btnById.get(String(id));
                    if (targetBtn) {
                        addSeatToSelection(targetBtn);
                    }
                });
                renderSelectionSummary();
                showSuggestFeedback(payload.message, false);
            } catch (err) {
                showSuggestFeedback('Mất kết nối máy chủ khi tìm ghế gợi ý.', true);
            }
        });
    });

    if (clearBtn) {
        clearBtn.addEventListener('click', clearAllSelections);
    }
    if (sidebarClearBtn) {
        sidebarClearBtn.addEventListener('click', clearAllSelections);
    }

    // Gia hạn giữ ghế trực tiếp tại trang chọn ghế
    if (extendHoldBtn) {
        extendHoldBtn.addEventListener('click', async function () {
            if (!activeHoldCode) {
                return;
            }
            extendHoldBtn.disabled = true;
            try {
                const headers = Object.assign({ 'Accept': 'application/json', 'Content-Type': 'application/json' }, getCsrfHeaders());
                const res = await fetch('/api/seats/hold/extend?holdCode=' + encodeURIComponent(activeHoldCode) + '&minutes=3', {
                    method: 'POST',
                    headers: headers
                });
                const payload = await res.json();
                if (!res.ok) {
                    showHoldFeedback(payload.message || 'Không thể gia hạn giữ ghế.', true);
                    canExtendHold = payload.error !== 'HOLD_MAX_LIMIT_REACHED';
                    updateActiveHoldPanel();
                    return;
                }
                holdRemainingSeconds = payload.remainingSeconds || holdRemainingSeconds;
                canExtendHold = Boolean(payload.canExtend);
                updateActiveHoldPanel();
                showHoldFeedback(payload.message || 'Đã gia hạn giữ ghế thành công.', false);
            } catch (err) {
                showHoldFeedback('Lỗi kết nối khi gia hạn giữ ghế.', true);
                updateActiveHoldPanel();
            }
        });
    }

    // Hủy phiên giữ ghế hiện tại
    if (releaseActiveHoldBtn) {
        releaseActiveHoldBtn.addEventListener('click', async function () {
            if (!activeHoldCode) {
                return;
            }
            try {
                const headers = Object.assign({ 'Accept': 'application/json' }, getCsrfHeaders());
                const res = await fetch('/api/seats/hold/' + encodeURIComponent(activeHoldCode), {
                    method: 'DELETE',
                    headers: headers
                });
                const payload = await res.json();
                if (!res.ok) {
                    showHoldFeedback(payload.message || 'Không thể hủy phiên giữ ghế.', true);
                    return;
                }
                activeHoldCode = '';
                holdRemainingSeconds = 0;
                selectedSeats.clear();
                updateActiveHoldPanel();
                showRealtimeNotice(payload.message || 'Đã hủy phiên giữ ghế.', 'info');
                await pollSeatStatus();
            } catch (err) {
                showHoldFeedback('Lỗi kết nối khi hủy phiên giữ ghế.', true);
            }
        });
    }

    document.addEventListener('visibilitychange', function () {
        if (document.visibilityState === 'hidden') {
            if (pollTimerId !== null) {
                window.clearInterval(pollTimerId);
                pollTimerId = null;
            }
        } else {
            pollSeatStatus();
            startPolling();
        }
    });

    window.addEventListener('pagehide', stopPolling);
    window.addEventListener('beforeunload', stopPolling);

    bindSeatButtons();
    preselectOwnHeldSeats();
    startHoldCountdown();
    startPolling();
});

// Bộ lọc hiển thị theo tầng (Tất cả / Tầng 1 / Tầng 2)
document.addEventListener('DOMContentLoaded', function () {
    const floorButtons = document.querySelectorAll('.floor-filter-btn');
    const floorWrappers = document.querySelectorAll('.floor-column-wrapper');
    if (!floorButtons.length || !floorWrappers.length) {
        return;
    }

    floorButtons.forEach(function (btn) {
        btn.addEventListener('click', function () {
            const target = btn.getAttribute('data-floor-filter');
            floorButtons.forEach(function (item) {
                item.classList.remove('active');
            });
            btn.classList.add('active');

            floorWrappers.forEach(function (wrapper) {
                const wrapperFloor = wrapper.getAttribute('data-floor');
                if (target === 'all') {
                    wrapper.classList.remove('d-none');
                } else {
                    wrapper.classList.toggle('d-none', wrapperFloor !== target);
                }
            });
        });
    });
});
