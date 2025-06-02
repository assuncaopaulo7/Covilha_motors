package pt.ubi.lojaveiculos.dto;

public class SummaryDTO {
    private long totalSales;
    private double totalRevenue;

    public SummaryDTO(long totalSales, double totalRevenue) {
        this.totalSales = totalSales;
        this.totalRevenue = totalRevenue;
    }

    public long getTotalSales() {
        return totalSales;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }
}
