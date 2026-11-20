/**
 * Module 2 - Authentication, Password Strength Meter, Toggle Visibility & Availability Check
 */
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.toggle-password-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            const targetId = this.getAttribute('data-target');
            const input = document.getElementById(targetId);
            if (!input) return;
            const showing = input.type === 'text';
            input.type = showing ? 'password' : 'text';
            const icon = this.querySelector('i');
            if (icon) {
                icon.className = showing ? 'bi bi-eye' : 'bi bi-eye-slash';
            }
        });
    });

    const passwordInput = document.querySelector('.password-strength-target');
    const strengthBar = document.getElementById('passwordStrengthBar');
    const strengthLabel = document.getElementById('passwordStrengthLabel');
    const confirmInput = document.getElementById('confirmPassword');
    const confirmMatchHint = document.getElementById('confirmMatchHint');

    function evaluateLocalStrength(pwd) {
        let score = 0;
        if (!pwd) return { score: 0, text: 'Chưa nhập', cls: '' };
        if (pwd.length >= 6) score += 25;
        if (pwd.length >= 10) score += 15;
        if (/[A-Z]/.test(pwd)) score += 15;
        if (/[a-z]/.test(pwd)) score += 15;
        if (/[0-9]/.test(pwd)) score += 15;
        if (/[^A-Za-z0-9]/.test(pwd)) score += 15;
        score = Math.min(100, score);

        if (score >= 85) return { score, text: 'Rất mạnh (' + score + '%)', cls: 'strength-very-strong' };
        if (score >= 60) return { score, text: 'Mạnh (' + score + '%)', cls: 'strength-strong' };
        if (score >= 35) return { score, text: 'Trung bình (' + score + '%)', cls: 'strength-medium' };
        return { score, text: 'Yếu (' + score + '%)', cls: 'strength-weak' };
    }

    function updateConfirmHint() {
        if (!passwordInput || !confirmInput || !confirmMatchHint) return;
        const p1 = passwordInput.value;
        const p2 = confirmInput.value;
        if (!p2) {
            confirmMatchHint.textContent = '';
            return;
        }
        if (p1 === p2) {
            confirmMatchHint.textContent = 'Mật khẩu xác nhận khớp';
            confirmMatchHint.className = 'form-text text-success fw-semibold';
        } else {
            confirmMatchHint.textContent = 'Mật khẩu xác nhận chưa khớp';
            confirmMatchHint.className = 'form-text text-danger';
        }
    }

    if (passwordInput && strengthBar && strengthLabel) {
        passwordInput.addEventListener('input', function () {
            const res = evaluateLocalStrength(this.value);
            strengthBar.style.width = res.score + '%';
            strengthBar.className = 'password-strength-bar ' + res.cls;
            strengthLabel.textContent = 'Độ mạnh mật khẩu: ' + res.text;
            updateConfirmHint();
        });
    }

    if (confirmInput) {
        confirmInput.addEventListener('input', updateConfirmHint);
    }

    const regUsername = document.getElementById('regUsername');
    const usernameHint = document.getElementById('usernameAvailabilityHint');
    if (regUsername && usernameHint) {
        regUsername.addEventListener('blur', function () {
            const val = this.value.trim();
            if (val.length < 4) return;
            fetch('/auth/api/check-availability?username=' + encodeURIComponent(val))
                .then(r => r.json())
                .then(data => {
                    if (data.usernameAvailable === true) {
                        usernameHint.textContent = 'Tên đăng nhập khả dụng';
                        usernameHint.className = 'form-text text-success';
                    } else if (data.usernameAvailable === false) {
                        usernameHint.textContent = 'Tên đăng nhập đã tồn tại!';
                        usernameHint.className = 'form-text text-danger';
                    }
                })
                .catch(() => {});
        });
    }

    const regEmail = document.getElementById('regEmail');
    const emailHint = document.getElementById('emailAvailabilityHint');
    if (regEmail && emailHint) {
        regEmail.addEventListener('blur', function () {
            const val = this.value.trim();
            if (!val.includes('@')) return;
            fetch('/auth/api/check-availability?email=' + encodeURIComponent(val))
                .then(r => r.json())
                .then(data => {
                    if (data.emailAvailable === true) {
                        emailHint.textContent = 'Email có thể sử dụng';
                        emailHint.className = 'form-text text-success';
                    } else if (data.emailAvailable === false) {
                        emailHint.textContent = 'Email này đã được đăng ký!';
                        emailHint.className = 'form-text text-danger';
                    }
                })
                .catch(() => {});
        });
    }
});
