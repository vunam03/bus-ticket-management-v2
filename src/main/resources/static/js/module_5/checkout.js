// Module 5: Dem nguoc giu ghe 10 phut & cap nhat huong dan thanh toan
document.addEventListener('DOMContentLoaded', function () {
    const timerEl = document.getElementById('holdCountdownTimer');
    const confirmBtn = document.getElementById('confirmPaymentBtn');

    if (timerEl) {
        const rawExpiresAt = timerEl.getAttribute('data-expires-at');
        let expiresTimestamp = NaN;

        if (rawExpiresAt) {
            if (/^\d+$/.test(rawExpiresAt.trim())) {
                const num = parseInt(rawExpiresAt.trim(), 10);
                expiresTimestamp = num > 1000000000000 ? num : Date.now() + num * 1000;
            } else {
                expiresTimestamp = Date.parse(rawExpiresAt);
            }
        }

        if (!isNaN(expiresTimestamp)) {
            function updateCountdown() {
                const diffMs = expiresTimestamp - Date.now();
                if (diffMs <= 0) {
                    timerEl.textContent = '00:00';
                    if (confirmBtn) {
                        confirmBtn.disabled = true;
                        confirmBtn.innerHTML = '<i class="bi bi-clock-history me-2"></i>Đã hết thời gian giữ ghế';
                    }
                    return false;
                }
                const totalSeconds = Math.floor(diffMs / 1000);
                const mins = Math.floor(totalSeconds / 60);
                const secs = totalSeconds % 60;
                timerEl.textContent = String(mins).padStart(2, '0') + ':' + String(secs).padStart(2, '0');
                return true;
            }

            if (updateCountdown()) {
                const intervalId = setInterval(function () {
                    if (!updateCountdown()) {
                        clearInterval(intervalId);
                    }
                }, 1000);
            }
        }
    }

    const instructionBox = document.getElementById('paymentInstructionBox');
    const paymentRadios = document.querySelectorAll('input[name="paymentMethod"]');

    const instructionsMap = {
        BANK_TRANSFER: '<strong>Hướng dẫn Chuyển khoản VietQR:</strong> Sau khi nhấn Xác nhận, hệ thống mô phỏng đối soát tức thì qua cổng ngân hàng 24/7 và xuất vé điện tử kèm mã QR ngay.',
        E_WALLET: '<strong>Hướng dẫn Ví điện tử (MoMo / VNPay / ZaloPay):</strong> Giao dịch được xử lý qua cổng thanh toán ví điện tử, vé xác nhận tự động trong 2 giây.',
        CASH: '<strong>Hướng dẫn Thanh toán Tiền mặt:</strong> Vé của bạn sẽ ở trạng thái giữ chỗ chờ thu tiền. Vui lòng thanh toán trực tiếp tại quầy vé bến xe trước giờ khởi hành 30 phút.'
    };

    function updatePaymentUI() {
        let selectedVal = 'BANK_TRANSFER';
        paymentRadios.forEach(function (radio) {
            const card = radio.closest('.payment-method-card');
            if (radio.checked) {
                selectedVal = radio.value;
                if (card) card.classList.add('active');
            } else if (card) {
                card.classList.remove('active');
            }
        });

        if (instructionBox) {
            instructionBox.innerHTML = instructionsMap[selectedVal] || instructionsMap.BANK_TRANSFER;
        }
    }

    if (paymentRadios.length > 0) {
        paymentRadios.forEach(function (radio) {
            radio.addEventListener('change', updatePaymentUI);
        });
        updatePaymentUI();
    }
});


// Module 5: Sao chép mã vé nhanh và xác nhận trạng thái mô phỏng cổng thanh toán
document.addEventListener('DOMContentLoaded', function () {
    const copyButtons = document.querySelectorAll('.copy-ticket-code-btn');
    copyButtons.forEach(function (btn) {
        btn.addEventListener('click', function () {
            const ticketCode = btn.getAttribute('data-ticket-code');
            if (!ticketCode) {
                return;
            }
            const originalHtml = btn.innerHTML;
            const markCopied = function () {
                btn.classList.add('copied');
                btn.innerHTML = '<i class="bi bi-check2 me-1"></i>Đã sao chép';
                setTimeout(function () {
                    btn.classList.remove('copied');
                    btn.innerHTML = originalHtml;
                }, 1800);
            };
            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(ticketCode).then(markCopied).catch(markCopied);
            } else {
                markCopied();
            }
        });
    });

    const checkoutForm = document.getElementById('bookingCheckoutForm');
    const confirmPaymentBtn = document.getElementById('confirmPaymentBtn');
    if (checkoutForm && confirmPaymentBtn) {
        checkoutForm.addEventListener('submit', function () {
            confirmPaymentBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang xử lý thanh toán...';
        });
    }
});
