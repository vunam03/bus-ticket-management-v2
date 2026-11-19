/**
 * Module 6 - Dashboard Interactive Controller
 * Hỗ trợ đổi năm biểu đồ doanh thu trực tiếp qua AJAX hoặc query parameter.
 */
document.addEventListener('DOMContentLoaded', function () {
    const yearSelector = document.getElementById('dashboardYearSelect');
    if (yearSelector) {
        yearSelector.addEventListener('change', function () {
            const selectedYear = this.value;
            fetch('/admin/dashboard/api/revenue-chart?year=' + encodeURIComponent(selectedYear))
                .then(res => res.json())
                .then(payload => {
                    if (payload && payload.labels && payload.revenues) {
                        RevenueChartHelper.initMonthlyBarChart(
                            'monthlyRevenueBarChart',
                            payload.labels,
                            payload.revenues,
                            payload.year
                        );
                    }
                })
                .catch(() => {
                    window.location.href = '/admin/dashboard?year=' + encodeURIComponent(selectedYear);
                });
        });
    }
});
