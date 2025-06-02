package pt.ubi.lojaveiculos.dto;

public interface MonthlySalesDTO {
    Integer getMonth();   // Mês (1-12)
    Integer getYear();    // Ano
    Long getCount();      // Quantidade de vendas
    Long getQuantity();   // Soma da quantidade de carros vendidos
    Double getTotal();    // Soma do total das vendas
}
