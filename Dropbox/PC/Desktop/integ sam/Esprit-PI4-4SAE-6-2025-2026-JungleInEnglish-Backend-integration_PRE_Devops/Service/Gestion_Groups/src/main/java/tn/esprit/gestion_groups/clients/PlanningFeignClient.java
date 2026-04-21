package tn.esprit.gestion_groups.clients;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "planning-client", url = "${planning.service.url:http://localhost:8089/pi}")
public interface PlanningFeignClient {

    @GetMapping("/plannings/advanced/weekly-load")
    WeeklyLoadReportDTO getWeeklyLoad(@RequestParam("date") String date);

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    class WeeklyLoadReportDTO {
        private Integer weekNumber;
        private Integer year;
        private Map<Long, Double> heuresParEnseignant;
        private Map<Long, Double> heuresParGroupe;
        private Map<Long, Double> tauxOccupationParSalle;
        private List<String> alertes;
    }
}