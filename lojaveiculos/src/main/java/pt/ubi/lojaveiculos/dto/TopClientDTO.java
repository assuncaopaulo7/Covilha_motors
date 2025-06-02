package pt.ubi.lojaveiculos.dto;

public interface TopClientDTO {
    String getNome();     // Nome do cliente
    Long getCount();      // Quantidade de compras feitas pelo cliente
    Double getTotal();    // Soma do total gasto pelo cliente
}
