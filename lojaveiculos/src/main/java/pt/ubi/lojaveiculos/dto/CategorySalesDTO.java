package pt.ubi.lojaveiculos.dto;

public interface CategorySalesDTO {
    String getCategory(); // Categoria do carro (pode ser 'Sem Categoria')
    Long getCount();      // Quantidade de vendas
    Double getTotal();    // Soma do total das vendas
}
