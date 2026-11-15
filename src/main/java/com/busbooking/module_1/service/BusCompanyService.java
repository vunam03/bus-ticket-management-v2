package com.busbooking.module_1.service;

import com.busbooking.common.exception.BusinessException;
import com.busbooking.common.exception.ResourceNotFoundException;
import com.busbooking.module_1.dto.BusCompanyFormDto;
import com.busbooking.module_1.model.BusCompany;
import com.busbooking.module_1.repository.BusCompanyRepository;
import com.busbooking.module_1.repository.BusTripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BusCompanyService {

    private final BusCompanyRepository companyRepository;
    private final BusTripRepository tripRepository;

    public BusCompanyService(BusCompanyRepository companyRepository, BusTripRepository tripRepository) {
        this.companyRepository = companyRepository;
        this.tripRepository = tripRepository;
    }

    @Transactional(readOnly = true)
    public List<BusCompany> getAllCompanies() {
        return companyRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<BusCompany> searchCompanies(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllCompanies();
        }
        return companyRepository.searchCompanies(keyword.trim());
    }

    @Transactional(readOnly = true)
    public List<BusCompany> getActiveCompanies() {
        return companyRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public BusCompany getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xe với ID: " + id));
    }

    @Transactional
    public BusCompany createCompany(BusCompanyFormDto dto) {
        String normalizedName = dto.getName().trim();
        if (companyRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new BusinessException("DUPLICATE_COMPANY", "Tên nhà xe '" + normalizedName + "' đã tồn tại trong hệ thống.");
        }
        validateCompanyFields(dto);
        BusCompany company = new BusCompany(
                normalizedName,
                dto.getHotline().trim(),
                dto.getHeadquarters().trim(),
                dto.getDescription() != null ? dto.getDescription().trim() : "",
                dto.getRating() != null ? dto.getRating() : 4.8,
                true
        );
        return companyRepository.save(company);
    }

    @Transactional
    public BusCompany updateCompany(Long id, BusCompanyFormDto dto) {
        BusCompany company = getCompanyById(id);
        validateCompanyFields(dto);
        String normalizedName = dto.getName().trim();
        companyRepository.findByNameIgnoreCase(normalizedName).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessException("DUPLICATE_COMPANY", "Tên nhà xe '" + normalizedName + "' đã thuộc về nhà xe khác.");
            }
        });
        company.setName(normalizedName);
        company.setHotline(dto.getHotline().trim());
        company.setHeadquarters(dto.getHeadquarters().trim());
        company.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
        company.setRating(dto.getRating() != null ? dto.getRating() : company.getRating());
        company.setActive(dto.isActive());
        return companyRepository.save(company);
    }

    @Transactional
    public BusCompany toggleCompanyStatus(Long id) {
        BusCompany company = getCompanyById(id);
        company.setActive(!company.isActive());
        return companyRepository.save(company);
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> getTripCountByCompany() {
        Map<Long, Long> stats = new LinkedHashMap<>();
        for (BusCompany company : getAllCompanies()) {
            stats.put(company.getId(), tripRepository.countByBusCompanyId(company.getId()));
        }
        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCompanyOverviewStats() {
        List<BusCompany> all = getAllCompanies();
        long activeCount = companyRepository.countByActiveTrue();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCompanies", all.size());
        summary.put("activeCompanies", activeCount);
        summary.put("inactiveCompanies", Math.max(0, all.size() - activeCount));
        summary.put("averageRating", all.stream()
                .mapToDouble(c -> c.getRating() != null ? c.getRating() : 4.8)
                .average()
                .orElse(4.8));
        return summary;
    }

    @Transactional
    public void deleteCompanyIfUnused(Long id) {
        BusCompany company = getCompanyById(id);
        long tripCount = tripRepository.countByBusCompanyId(id);
        if (tripCount > 0) {
            throw new BusinessException("COMPANY_IN_USE",
                    "Nhà xe '" + company.getName() + "' đang có " + tripCount + " chuyến xe liên kết, hãy chuyển sang Tạm ngưng thay vì xóa.");
        }
        companyRepository.delete(company);
    }

    private void validateCompanyFields(BusCompanyFormDto dto) {
        if (dto.getRating() != null && (dto.getRating() < 1.0 || dto.getRating() > 5.0)) {
            throw new BusinessException("INVALID_COMPANY_RATING", "Điểm đánh giá nhà xe phải nằm trong khoảng từ 1.0 đến 5.0.");
        }
        if (dto.getHotline() == null || dto.getHotline().trim().length() < 8) {
            throw new BusinessException("INVALID_HOTLINE", "Số hotline nhà xe phải có ít nhất 8 ký tự.");
        }
    }
}
