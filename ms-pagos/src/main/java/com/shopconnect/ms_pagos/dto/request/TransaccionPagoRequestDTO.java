package com.shopconnect.ms_pagos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TransaccionPagoRequestDTO {

    @NotBlank(message = "El código de transacción es obligatorio")
    private String codigoTransaccion;

    private String estado = "PENDIENTE";

    @NotNull(message = "El pagoId es obligatorio")
    private Long pagoId;

    public String getCodigoTransaccion() {
        return codigoTransaccion;
    }

    public void setCodigoTransaccion(String codigoTransaccion) {
        this.codigoTransaccion = codigoTransaccion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Long getPagoId() {
        return pagoId;
    }

    public void setPagoId(Long pagoId) {
        this.pagoId = pagoId;
    }
}
