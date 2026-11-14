package com.busbooking.module_1.service;

import com.busbooking.module_1.dto.BusCompanyFormDto;
import com.busbooking.module_1.repository.BusCompanyRepository;
import com.busbooking.shared.exception.BusinessException;
import com.busbooking.shared.exception.ResourceNotFoundException;
import com.busbooking.shared.model.BusCompany;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BusCompanyService {

    private final BusCompanyRepository companyRepository;

    public BusCompanyService(BusCompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public List<BusCompany> getAllCompanies() {
        return companyRepository.findAllByOrderByCreatedAtDesc();
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
    public void toggleCompanyStatus(Long id) {
        BusCompany company = getCompanyById(id);
        company.setActive(!company.isActive());
        companyRepository.save(company);
    }
}
