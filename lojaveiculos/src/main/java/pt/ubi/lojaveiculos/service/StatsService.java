package pt.ubi.lojaveiculos.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pt.ubi.lojaveiculos.dto.*;
import pt.ubi.lojaveiculos.repository.SaleRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class StatsService {

    private final SaleRepository saleRepository;

    public StatsService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    public List<BrandSalesDTO> getSalesByBrand() {
        return saleRepository.findSalesByBrand();
    }

    public List<MonthlySalesDTO> getMonthlySales() {
        return saleRepository.findMonthlySales();
    }

    public List<CategorySalesDTO> getSalesByCategory() {
        return saleRepository.findSalesByCategory();
    }

    public List<TopClientDTO> getTopClients() {
        return saleRepository.findTopClients(PageRequest.of(0, 5));
    }

    public List<Object[]> getFilteredSales(LocalDate start, LocalDate end) {
        return saleRepository.findByDateBetween(start, end).stream()
                .map(s -> new Object[]{
                        s.getDate(),
                        s.getCar() != null ? s.getCar().getModel() : "Carro Removido",
                        s.getQuantity(),
                        s.getTotal()
                })
                .toList();
    }

    public SummaryDTO getSummaryStatsDTO() {
        Object[] stats = getSummaryStats();

        long totalSales = stats.length > 0 && stats[0] != null ? ((Number) stats[0]).longValue() : 0L;
        double totalRevenue = stats.length > 1 && stats[1] != null ? ((Number) stats[1]).doubleValue() : 0.0;

        return new SummaryDTO(totalSales, totalRevenue);
    }



    public Object[] getSummaryStats() {
        Object[] totals = saleRepository.getTotalSalesAndRevenue();
        Long totalSales = totals[0] != null ? ((Number) totals[0]).longValue() : 0L;
        Double totalRevenue = totals[1] != null ? ((Number) totals[1]).doubleValue() : 0.0;

        return new Object[]{
                totalSales,
                totalRevenue
        };
    }


}
