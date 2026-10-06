package com.mario.inventory.web;

import com.mario.inventory.exception.NotFoundException;
import com.mario.inventory.service.ProductService;
import com.mario.inventory.web.dto.ProductDtos.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean ProductService service;

    @Test
    void returnsProductById() throws Exception {
        when(service.get(1L)).thenReturn(new ProductResponse(1L, "MON-27", "Monitor 27\"", new BigDecimal("289.90"), 8));

        mvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("MON-27"))
                .andExpect(jsonPath("$.price").value(289.90));
    }

    @Test
    void returns404AsProblemDetail() throws Exception {
        when(service.get(99L)).thenThrow(new NotFoundException("Producto", 99L));

        mvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.detail").value("Producto con id 99 no existe"));
    }

    @Test
    void createsProductAndReturnsLocation() throws Exception {
        when(service.create(any())).thenReturn(new ProductResponse(5L, "DOCK-USBC", "Dock USB-C", new BigDecimal("119.00"), 4));

        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"DOCK-USBC","name":"Dock USB-C","price":119.00,"minStock":4}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/products/5"));
    }

    @Test
    void rejectsInvalidProduct() throws Exception {
        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"","name":"Sin SKU","price":-1,"minStock":0}
                                """))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }
}
