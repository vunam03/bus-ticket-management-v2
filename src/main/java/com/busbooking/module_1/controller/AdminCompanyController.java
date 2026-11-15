package com.busbooking.module_1.controller;

import com.busbooking.module_1.dto.BusCompanyFormDto;
import com.busbooking.module_1.service.BusCompanyService;
import com.busbooking.common.exception.BusinessException;
import com.busbooking.module_1.model.BusCompany;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/companies")
public class AdminCompanyController {

    private final BusCompanyService companyService;

    public AdminCompanyController(BusCompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public String listCompanies(Model model) {
        model.addAttribute("companies", companyService.getAllCompanies());
        if (!model.containsAttribute("companyForm")) {
            model.addAttribute("companyForm", new BusCompanyFormDto());
        }
        model.addAttribute("editMode", false);
        model.addAttribute("activeMenu", "companies");
        return "module_1/company-list";
    }

    @GetMapping({"/edit/{id}", "/{id}/edit"})
    public String showEditCompany(@PathVariable Long id, Model model) {
        BusCompany company = companyService.getCompanyById(id);
        BusCompanyFormDto form = new BusCompanyFormDto();
        form.setId(company.getId());
        form.setName(company.getName());
        form.setHotline(company.getHotline());
        form.setHeadquarters(company.getHeadquarters());
        form.setDescription(company.getDescription());
        form.setRating(company.getRating());
        form.setActive(company.isActive());

        model.addAttribute("companies", companyService.getAllCompanies());
        model.addAttribute("companyForm", form);
        model.addAttribute("editMode", true);
        model.addAttribute("activeMenu", "companies");
        return "module_1/company-list";
    }

    @PostMapping({"/save", "", "/{id}/edit"})
    public String saveCompany(@Valid @ModelAttribute("companyForm") BusCompanyFormDto form,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("companies", companyService.getAllCompanies());
            model.addAttribute("editMode", form.getId() != null);
            model.addAttribute("activeMenu", "companies");
            return "module_1/company-list";
        }
        try {
            if (form.getId() == null) {
                companyService.createCompany(form);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm nhà xe mới thành công!");
            } else {
                companyService.updateCompany(form.getId(), form);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin nhà xe thành công!");
            }
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/companies";
    }

    @PostMapping({"/toggle/{id}", "/{id}/toggle"})
    public String toggleCompanyStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        companyService.toggleCompanyStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thay đổi trạng thái hoạt động của nhà xe!");
        return "redirect:/admin/companies";
    }
}
