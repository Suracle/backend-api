package com.suracle.backend_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

@RestController
@RequestMapping("/api/agency-search-strategies")
public class AgencySearchStrategyController {

    @GetMapping
    public Map<String, Object> getAgencySearchStrategies() {
        Map<String, Object> strategies = new HashMap<>();
        
        // FDA 전략
        Map<String, Object> fdaStrategy = new HashMap<>();
        fdaStrategy.put("enabled", true);
        fdaStrategy.put("priority", 1);
        fdaStrategy.put("base_urls", Arrays.asList(
            "https://www.fda.gov/cosmetics/cosmetics-laws-regulations",
            "https://www.fda.gov/food/guidance-regulation-food-and-dietary-supplements",
            "https://www.fda.gov/medical-devices/licensing-enforcement-and-compliance"
        ));
        fdaStrategy.put("search_queries", Arrays.asList(
            "cosmetic regulations",
            "food safety requirements", 
            "medical device registration"
        ));
        strategies.put("FDA", fdaStrategy);
        
        // CPSC 전략
        Map<String, Object> cpscStrategy = new HashMap<>();
        cpscStrategy.put("enabled", true);
        cpscStrategy.put("priority", 2);
        cpscStrategy.put("base_urls", Arrays.asList(
            "https://www.cpsc.gov/BusinessManufacturing",
            "https://www.cpsc.gov/Regulations-LawsStandards"
        ));
        cpscStrategy.put("search_queries", Arrays.asList(
            "consumer product safety",
            "manufacturing standards",
            "recalls regulations"
        ));
        strategies.put("CPSC", cpscStrategy);
        
        // EPA 전략
        Map<String, Object> epaStrategy = new HashMap<>();
        epaStrategy.put("enabled", true);
        epaStrategy.put("priority", 3);
        epaStrategy.put("base_urls", Arrays.asList(
            "https://www.epa.gov/compliance/environmental-regulations",
            "https://www.epa.gov/introduction-environmental-chemicals"
        ));
        epaStrategy.put("search_queries", Arrays.asList(
            "environmental regulations",
            "chemical safety requirements",
            "waste management"
        ));
        strategies.put("EPA", epaStrategy);
        
        // USDA 전략
        Map<String, Object> usdaStrategy = new HashMap<>();
        usdaStrategy.put("enabled", true);
        usdaStrategy.put("priority", 4);
        usdaStrategy.put("base_urls", Arrays.asList(
            "https://www.usda.gov/topics/operations",
            "https://www.fsis.usda.gov/policy/food-safety-guidance"
        ));
        usdaStrategy.put("search_queries", Arrays.asList(
            "agricultural requirements",
            "food processing standards",
            "organic certification"
        ));
        strategies.put("USDA", usdaStrategy);
        
        // FCC 전략
        Map<String, Object> fccStrategy = new HashMap<>();
        fccStrategy.put("enabled", true);
        fccStrategy.put("priority", 5);
        fccStrategy.put("base_urls", Arrays.asList(
            "https://www.fcc.gov/wireless/buisiness",
            "https://www.fcc.gov/device-authorization-rules"
        ));
        fccStrategy.put("search_queries", Arrays.asList(
            "wireless communications",
            "device authorization",
            "emissions standards"
        ));
        strategies.put("FCC", fccStrategy);
        
        // CBP 전략
        Map<String, Object> cbpStrategy = new HashMap<>();
        cbpStrategy.put("enabled", true);
        cbpStrategy.put("priority", 6);
        cbpStrategy.put("base_urls", Arrays.asList(
            "https://www.cbp.gov/trade/programs-administration",
            "https://www.cbp.gov/trade/basic-import-export"
        ));
        cbpStrategy.put("search_queries", Arrays.asList(
            "trade programs",
            "import documentation",
            "customs requirements"
        ));
        strategies.put("CBP", cbpStrategy);
        
        Map<String, Object> response = new HashMap<>();
        response.put("strategies", strategies);
        response.put("total_agencies", strategies.size());
        response.put("active_strategies", Arrays.asList("FDA", "CPSC", "EPA", "USDA", "FCC", "CBP"));
        
        return response;
    }
}
