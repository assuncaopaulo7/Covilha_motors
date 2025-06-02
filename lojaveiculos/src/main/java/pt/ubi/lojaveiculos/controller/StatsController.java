package pt.ubi.lojaveiculos.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.ubi.lojaveiculos.dto.*;
import pt.ubi.lojaveiculos.service.StatsService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public String showStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Model model) {

        try {
            // Validar filtro de datas
            if (start != null && end != null) {
                if (start.isAfter(end)) {
                    model.addAttribute("error", "A data de início deve ser anterior à data de fim");
                } else {
                    model.addAttribute("filteredSales", safeGetFilteredSales(start, end));
                    model.addAttribute("filterStart", start);
                    model.addAttribute("filterEnd", end);
                }
            }

            // Adicionar estatísticas gerais
            model.addAttribute("salesByBrand", safeGetSalesByBrand());
            model.addAttribute("monthlySales", safeGetMonthlySales());
            model.addAttribute("salesByCategory", safeGetSalesByCategory());
            model.addAttribute("topClients", safeGetTopClients());
            model.addAttribute("summary", safeGetSummaryStats());

        } catch (Exception e) {
            model.addAttribute("error", "Ocorreu um erro ao carregar as estatísticas");
            System.err.println("Erro ao carregar estatísticas: " + e.getMessage());
            e.printStackTrace();
        }

        return "stats";
    }

    // Métodos auxiliares para chamadas seguras

    private List<?> safeGetFilteredSales(LocalDate start, LocalDate end) {
        try {
            return statsService.getFilteredSales(start, end);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<BrandSalesDTO> safeGetSalesByBrand() {
        try {
            return statsService.getSalesByBrand();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<MonthlySalesDTO> safeGetMonthlySales() {
        try {
            return statsService.getMonthlySales();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<CategorySalesDTO> safeGetSalesByCategory() {
        try {
            return statsService.getSalesByCategory();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<TopClientDTO> safeGetTopClients() {
        try {
            return statsService.getTopClients();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private SummaryDTO safeGetSummaryStats() {
        try {
            return statsService.getSummaryStatsDTO();
        } catch (Exception e) {
            return new SummaryDTO(0L, 0.0);
        }
    }


}
