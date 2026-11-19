/**
 * Module 6 - Chart.js Visualization Helper
 * Định dạng tiền tệ Việt Nam VNĐ, hỗ trợ biểu đồ cột, đường và donut.
 */
const RevenueChartHelper = (function () {
    let monthlyBarChartInstance = null;
    let dailyLineChartInstance = null;
    let ticketDonutChartInstance = null;

    function formatCurrencyVnd(value) {
        return new Intl.NumberFormat('vi-VN').format(value || 0) + ' ₫';
    }

    function initMonthlyBarChart(canvasId, labels, dataValues, year) {
        const ctx = document.getElementById(canvasId);
        if (!ctx || typeof Chart === 'undefined') return null;

        if (monthlyBarChartInstance) {
            monthlyBarChartInstance.destroy();
        }

        monthlyBarChartInstance = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Doanh thu năm ' + year + ' (VNĐ)',
                    data: dataValues,
                    backgroundColor: 'rgba(15, 118, 110, 0.82)',
                    hoverBackgroundColor: 'rgba(13, 148, 136, 1)',
                    borderColor: '#0f766e',
                    borderWidth: 1,
                    borderRadius: 8,
                    maxBarThickness: 38
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: true, position: 'top' },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                return ' Doanh thu: ' + formatCurrencyVnd(context.parsed.y);
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function (value) {
                                return formatCurrencyVnd(value);
                            }
                        }
                    }
                }
            }
        });
        return monthlyBarChartInstance;
    }

    function initDailyLineChart(canvasId, labels, dataValues) {
        const ctx = document.getElementById(canvasId);
        if (!ctx || typeof Chart === 'undefined') return null;

        if (dailyLineChartInstance) {
            dailyLineChartInstance.destroy();
        }

        dailyLineChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Doanh thu theo ngày (VNĐ)',
                    data: dataValues,
                    borderColor: '#2563eb',
                    backgroundColor: 'rgba(37, 99, 235, 0.14)',
                    borderWidth: 2.5,
                    pointBackgroundColor: '#1d4ed8',
                    pointRadius: 4,
                    fill: true,
                    tension: 0.32
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: true, position: 'top' },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                return ' Doanh thu: ' + formatCurrencyVnd(context.parsed.y);
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function (value) {
                                return formatCurrencyVnd(value);
                            }
                        }
                    }
                }
            }
        });
        return dailyLineChartInstance;
    }

    function initTicketStatusDonut(canvasId, paidCount, unpaidCount, cancelledCount) {
        const ctx = document.getElementById(canvasId);
        if (!ctx || typeof Chart === 'undefined') return null;

        if (ticketDonutChartInstance) {
            ticketDonutChartInstance.destroy();
        }

        ticketDonutChartInstance = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Đã thanh toán', 'Chưa thanh toán', 'Đã hủy'],
                datasets: [{
                    data: [paidCount, unpaidCount, cancelledCount],
                    backgroundColor: ['#10b981', '#f59e0b', '#ef4444'],
                    borderWidth: 2,
                    borderColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '66%',
                plugins: {
                    legend: { position: 'bottom' }
                }
            }
        });
        return ticketDonutChartInstance;
    }

    return {
        initMonthlyBarChart,
        initDailyLineChart,
        initTicketStatusDonut,
        formatCurrencyVnd
    };
})();
