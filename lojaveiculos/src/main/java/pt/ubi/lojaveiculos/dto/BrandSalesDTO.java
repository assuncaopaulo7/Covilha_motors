package pt.ubi.lojaveiculos.dto;

public interface BrandSalesDTO {
    String getBrand();    // Marca do carro (pode ser 'Removido')
    Long getCount();      // Quantidade de vendas (count)
    Double getTotal();    // Soma do total das vendas
}
