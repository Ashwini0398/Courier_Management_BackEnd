package com.courier.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor @Data @AllArgsConstructor
public class PaginationDTO {
    @NotBlank
    private int page;
    @NotBlank
    private int size;
}
