// Module 4: Tuong tac chon ghe & tinh tong tien real-time
document.addEventListener('DOMContentLoaded', function () {
    const seatButtons = document.querySelectorAll('.seat-btn.seat-available');
    const inputsContainer = document.getElementById('selectedSeatInputsContainer');
    const countDisplay = document.getElementById('selectedSeatsCount');
    const seatsDisplay = document.getElementById('selectedSeatsDisplay');
    const totalPriceDisplay = document.getElementById('selectedTotalPrice');
    const submitBtn = document.getElementById('submitHoldSeatsBtn');

    if (!seatButtons.length || !inputsContainer) {
        return;
    }

    const selectedSeats = new Map();

    function formatCurrencyVnd(amount) {
        return Math.round(amount).toLocaleString('en-US') + ' đ';
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

        if (countDisplay) {
            countDisplay.textContent = selectedSeats.size + ' ghế';
        }
        if (seatsDisplay) {
            seatsDisplay.textContent = seatNumbers.length > 0 ? seatNumbers.join(', ') : 'Chưa chọn ghế';
        }
        if (totalPriceDisplay) {
            totalPriceDisplay.textContent = formatCurrencyVnd(totalAmount);
        }
        if (submitBtn) {
            submitBtn.disabled = selectedSeats.size === 0;
        }
    }

    seatButtons.forEach(function (btn) {
        btn.addEventListener('click', function () {
            const seatId = btn.getAttribute('data-seat-id');
            const seatNumber = btn.getAttribute('data-seat-number');
            const seatPrice = parseFloat(btn.getAttribute('data-seat-price') || '0');

            if (!seatId) {
                return;
            }

            if (selectedSeats.has(seatId)) {
                selectedSeats.delete(seatId);
                btn.classList.remove('seat-selected');
            } else {
                selectedSeats.set(seatId, {
                    id: seatId,
                    number: seatNumber,
                    price: seatPrice
                });
                btn.classList.add('seat-selected');
            }

            renderSelectionSummary();
        });
    });

    renderSelectionSummary();
});
