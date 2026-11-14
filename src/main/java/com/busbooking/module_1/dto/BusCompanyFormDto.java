package com.busbooking.module_1.dto;

import jakarta.validation.constraints.*;

public class BusCompanyFormDto {

    private Long id;

    @NotBlank(message = "Tên nhà xe không được để trống")
    @Size(max = 150, message = "Tên nhà xe tối đa 150 ký tự")
    private String name;

    @NotBlank(message = "Số hotline không được để trống")
    @Size(max = 30, message = "Số hotline tối đa 30 ký tự")
    private String hotline;

    @NotBlank(message = "Trụ sở chính không được để trống")
    @Size(max = 255, message = "Địa chỉ trụ sở tối đa 255 ký tự")
    private String headquarters;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;

    @NotNull(message = "Điểm đánh giá không được để trống")
    @DecimalMin(value = "1.0", message = "Đánh giá tối thiểu là 1.0")
    @DecimalMax(value = "5.0", message = "Đánh giá tối đa là 5.0")
    private Double rating = 4.8;

    private boolean active = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHotline() {
        return hotline;
    }

    public void setHotline(String hotline) {
        this.hotline = hotline;
    }

    public String getHeadquarters() {
        return headquarters;
    }

    public void setHeadquarters(String headquarters) {
        this.headquarters = headquarters;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
