package com.suracle.backend_api.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 미국 정부 기관 API 엔드포인트 관리 클래스
 * Python의 api_endpoints.py와 동일한 정보를 Java로 제공
 */
@Component
@Getter
public class ApiEndpointsManager {
    
    private final Map<String, AgencyConfig> agencies;
    
    public ApiEndpointsManager() {
        this.agencies = initializeAgencies();
    }
    
    private Map<String, AgencyConfig> initializeAgencies() {
        Map<String, AgencyConfig> agencyMap = new HashMap<>();
        
        // FDA (Food and Drug Administration)
        agencyMap.put("fda", new AgencyConfig(
            "FDA",
            "https://api.fda.gov",
            false,
            "1000/day without key, 120000/day with key",
            Map.of(
                "drug", Map.of(
                    "event", "https://api.fda.gov/drug/event.json",
                    "label", "https://api.fda.gov/drug/label.json",
                    "ndc", "https://api.fda.gov/drug/ndc.json",
                    "enforcement", "https://api.fda.gov/drug/enforcement.json",
                    "drugsfda", "https://api.fda.gov/drug/drugsfda.json",
                    "shortages", "https://api.fda.gov/drug/shortages.json"
                ),
                "device", Map.of(
                    "event", "https://api.fda.gov/device/event.json",
                    "510k", "https://api.fda.gov/device/510k.json",
                    "enforcement", "https://api.fda.gov/device/enforcement.json"
                ),
                "food", Map.of(
                    "enforcement", "https://api.fda.gov/food/enforcement.json",
                    "event", "https://api.fda.gov/food/event.json"
                ),
                "cosmetic", Map.of(
                    "event", "https://api.fda.gov/cosmetic/event.json"
                ),
                "animalandveterinary", Map.of(
                    "event", "https://api.fda.gov/animalandveterinary/event.json"
                )
            )
        ));
        
        // USDA (U.S. Department of Agriculture)
        agencyMap.put("usda", new AgencyConfig(
            "USDA",
            "https://api.nal.usda.gov",
            true,
            "1000/day",
            Map.of(
                "fooddata_central", Map.of(
                    "foods", "https://api.nal.usda.gov/fdc/v1/foods",
                    "search", "https://api.nal.usda.gov/fdc/v1/foods/search",
                    "nutrients", "https://api.nal.usda.gov/fdc/v1/nutrients"
                ),
                "plants", Map.of(
                    "plants", "https://plants.usda.gov/api/plants",
                    "search", "https://plants.usda.gov/api/plants/search"
                ),
                "biopreferred", Map.of(
                    "products", "https://www.biopreferred.gov/BioPreferred/faces/pages/ProductCatalog.xhtml"
                )
            )
        ));
        
        // EPA (Environmental Protection Agency)
        agencyMap.put("epa", new AgencyConfig(
            "EPA",
            "https://data.epa.gov",
            false,
            "1000/hour",
            Map.of(
                "envirofacts", Map.of(
                    "base", "https://data.epa.gov/efservice",
                    "chemical_search", "https://data.epa.gov/efservice/srs.srs_chemicals/chem_name/LIKE/{query}/JSON",
                    "facility_search", "https://data.epa.gov/efservice/frs.frs_facilities/facility_name/LIKE/{query}/JSON",
                    "tri_search", "https://data.epa.gov/efservice/tri.tri_facility/facility_name/LIKE/{query}/JSON",
                    "rcra_search", "https://data.epa.gov/efservice/rcra.rcra_handler/handler_name/LIKE/{query}/JSON",
                    "sdwis_search", "https://data.epa.gov/efservice/sdwis.sdwis_public_water_systems/pws_name/LIKE/{query}/JSON"
                ),
                "comptox", Map.of(
                    "base", "https://comptox.epa.gov/dashboard/api",
                    "search", "https://comptox.epa.gov/dashboard/api/chemical/search",
                    "details", "https://comptox.epa.gov/dashboard/api/chemical/details",
                    "lists", "https://comptox.epa.gov/dashboard/api/chemical/lists"
                ),
                "aqs", Map.of(
                    "base", "https://aqs.epa.gov/data/api",
                    "sample_data", "https://aqs.epa.gov/data/api/sampleData/byState",
                    "daily_data", "https://aqs.epa.gov/data/api/dailyData/byState",
                    "annual_data", "https://aqs.epa.gov/data/api/annualData/byState"
                )
            )
        ));
        
        // FCC (Federal Communications Commission)
        Map<String, Map<String, String>> fccEndpoints = new HashMap<>();
        fccEndpoints.put("public_files", Map.of(
            "base", "https://publicfiles.fcc.gov",
            "api", "https://publicfiles.fcc.gov/api",
            "search", "https://publicfiles.fcc.gov/api/search",
            "stations", "https://publicfiles.fcc.gov/api/stations",
            "file_by_id", "https://publicfiles.fcc.gov/api/service/file/id/{fileId}.json",
            "file_history", "https://publicfiles.fcc.gov/api/service/file/history.json",
            "station_files", "https://publicfiles.fcc.gov/api/service/station/{callsign}/files.json"
        ));
        fccEndpoints.put("service_data", Map.of(
            "facility_search", "https://publicfiles.fcc.gov/api/service/facility/search/{keyword}",
            "relationship_frn", "https://publicfiles.fcc.gov/api/service/relationship/frn/{frn}",
            "service_facility_all", "https://publicfiles.fcc.gov/api/service/{serviceType}/facility/getall",
            "service_facility_details", "https://publicfiles.fcc.gov/api/service/{serviceType}/facility/id/{entityID}",
            "service_applications", "https://publicfiles.fcc.gov/api/service/{serviceType}/applications/facility/{entityID}",
            "service_eeo", "https://publicfiles.fcc.gov/api/service/{serviceType}/eeo/facilityid/{entityID}",
            "service_ownership", "https://publicfiles.fcc.gov/api/service/{serviceType}/ownership/facilityid/{entityID}"
        ));
        fccEndpoints.put("cable_data", Map.of(
            "cable_relationship", "https://publicfiles.fcc.gov/api/service/cable/relationship/username/{COALSID}",
            "cable_eeo", "https://publicfiles.fcc.gov/api/service/cable/eeo/{groupBy}",
            "cable_details", "https://publicfiles.fcc.gov/api/service/cable/psid/{psid}",
            "cable_communities", "https://publicfiles.fcc.gov/api/service/cable/communities/psid/{psid}"
        ));
        fccEndpoints.put("sdars_data", Map.of(
            "sdars_entity", "https://publicfiles.fcc.gov/api/service/sdars/frn/{frn}",
            "sdars_eeo", "https://publicfiles.fcc.gov/api/service/sdars/eeo/facility/{facilityID}"
        ));
        fccEndpoints.put("dbs_data", Map.of(
            "dbs_entity", "https://publicfiles.fcc.gov/api/service/dbs/frn/{frn}",
            "dbs_eeo", "https://publicfiles.fcc.gov/api/service/dbs/eeo/facility/{facilityID}"
        ));
        fccEndpoints.put("contour_api", Map.of(
            "contour_by_service", "https://publicfiles.fcc.gov/api/contour/{serviceType}/{idType}/{idValue}.{format}"
        ));
        fccEndpoints.put("manager_api", Map.of(
            "token_manager", "https://publicfiles.fcc.gov/api/manager/token/get/entityAccessToken.{format}",
            "folder_manager", "https://publicfiles.fcc.gov/api/manager/folder/id/{folderId}.{format}",
            "file_manager", "https://publicfiles.fcc.gov/api/manager/file/id/{fileId}.{format}",
            "search_files_folders", "https://publicfiles.fcc.gov/api/manager/search/key/{searchKey}.{format}"
        ));
        fccEndpoints.put("device_authorization", Map.of(
            "base", "https://api.fcc.gov/device/authorization",
            "grants", "https://api.fcc.gov/device/authorization/grants",
            "applications", "https://api.fcc.gov/device/authorization/applications"
        ));
        fccEndpoints.put("ecfs", Map.of(
            "base", "https://api.fcc.gov/ecfs",
            "proceedings", "https://api.fcc.gov/ecfs/proceedings",
            "filings", "https://api.fcc.gov/ecfs/filings"
        ));
        fccEndpoints.put("consumer_help", Map.of(
            "complaints", "https://opendata.fcc.gov/resource/sr6c-syda.json"
        ));
        fccEndpoints.put("eas_equipment", Map.of(
            "base", "https://opendata.fcc.gov/api/views/"
        ));
        
        agencyMap.put("fcc", new AgencyConfig(
            "FCC",
            "https://publicfiles.fcc.gov",
            false,
            "1000/hour",
            fccEndpoints
        ));
        
        // CBP (Customs and Border Protection)
        agencyMap.put("cbp", new AgencyConfig(
            "CBP",
            "https://trade.cbp.dhs.gov",
            true,
            "1000/day",
            Map.of(
                "aesdirect", Map.of(
                    "test_base", "https://trade-test.cbp.dhs.gov/ace/aes/aesdirect-ui/secured",
                    "prod_base", "https://trade.cbp.dhs.gov/ace/aes/aesdirect-ui/secured",
                    "weblink_inquiry", "https://trade.cbp.dhs.gov/ace/aes/aesdirect-ui/secured/weblinkFilingInquiry",
                    "create_weblink", "https://trade.cbp.dhs.gov/ace/aes/aesdirect-ui/secured/createWeblinkFiling"
                ),
                "public_data_portal", Map.of(
                    "base", "https://www.cbp.gov/newsroom/stats/cbp-public-data-portal",
                    "trade_stats", "https://www.cbp.gov/newsroom/stats/cbp-public-data-portal",
                    "travel_stats", "https://www.cbp.gov/newsroom/stats/travel"
                ),
                "trade_statistics", Map.of(
                    "base", "https://api.cbp.gov/trade/statistics",
                    "imports", "https://api.cbp.gov/trade/statistics/imports",
                    "exports", "https://api.cbp.gov/trade/statistics/exports",
                    "hs_codes", "https://api.cbp.gov/trade/statistics/hs-codes"
                ),
                "ace_portal", Map.of(
                    "base", "https://api.cbp.gov/ace/",
                    "api", "https://api.cbp.gov/ace/"
                )
            )
        ));
        
        // CPSC (Consumer Product Safety Commission)
        agencyMap.put("cpsc", new AgencyConfig(
            "CPSC",
            "https://www.saferproducts.gov",
            false,
            "1000/day",
            Map.of(
                "saferproducts", Map.of(
                    "base", "https://www.saferproducts.gov/RestWebServices/Recall",
                    "recalls", "https://www.saferproducts.gov/RestWebServices/Recall",
                    "api", "https://www.saferproducts.gov/RestWebServices/Recall"
                ),
                "cpsc_official", Map.of(
                    "api", "https://www.cpsc.gov/Recalls/CPSC-Recalls-API",
                    "recalls", "https://www.cpsc.gov/Recalls/CPSC-Recalls-API/recalls",
                    "search", "https://www.cpsc.gov/Recalls/CPSC-Recalls-API/recalls/search",
                    "json", "https://www.cpsc.gov/Recalls/CPSC-Recalls-API/recalls.json"
                ),
                "penalties", Map.of(
                    "base", "https://www.cpsc.gov/",
                    "civil_criminal", "https://www.cpsc.gov/"
                ),
                "data", Map.of(
                    "base", "https://www.cpsc.gov/api",
                    "recalls", "https://www.cpsc.gov/api/recalls",
                    "products", "https://www.cpsc.gov/api/products"
                )
            )
        ));
        
        // Commerce (Department of Commerce)
        agencyMap.put("commerce", new AgencyConfig(
            "Commerce",
            "https://api.census.gov",
            true,
            "1000/day",
            Map.of(
                "trade_data", Map.of(
                    "base", "https://api.census.gov/data/timeseries/intltrade/",
                    "imports", "https://api.census.gov/data/timeseries/intltrade/imports",
                    "exports", "https://api.census.gov/data/timeseries/intltrade/exports"
                ),
                "steel_import", Map.of(
                    "base", "https://www.trade.gov/steel-import-monitoring-analysis-system-sima",
                    "monitoring", "https://www.trade.gov/steel-import-monitoring-analysis-system-sima"
                ),
                "aluminum_import", Map.of(
                    "base", "https://www.trade.gov/aluminum-import-monitor",
                    "monitoring", "https://www.trade.gov/aluminum-import-monitor"
                )
            )
        ));
        
        return agencyMap;
    }
    
    /**
     * 특정 기관의 엔드포인트 URL을 반환
     */
    public String getEndpoint(String agency, String category, String endpoint) {
        AgencyConfig agencyConfig = agencies.get(agency);
        if (agencyConfig == null) {
            throw new IllegalArgumentException("Agency not found: " + agency);
        }
        
        Map<String, Map<String, String>> endpoints = agencyConfig.getEndpoints();
        Map<String, String> categoryEndpoints = endpoints.get(category);
        if (categoryEndpoints == null) {
            throw new IllegalArgumentException("Category not found: " + agency + "." + category);
        }
        
        String endpointUrl = categoryEndpoints.get(endpoint);
        if (endpointUrl == null) {
            throw new IllegalArgumentException("Endpoint not found: " + agency + "." + category + "." + endpoint);
        }
        
        return endpointUrl;
    }
    
    /**
     * 특정 기관의 모든 엔드포인트를 반환
     */
    public Map<String, Map<String, String>> getAllEndpoints(String agency) {
        AgencyConfig agencyConfig = agencies.get(agency);
        if (agencyConfig == null) {
            throw new IllegalArgumentException("Agency not found: " + agency);
        }
        return agencyConfig.getEndpoints();
    }
    
    /**
     * 특정 기관이 API 키를 요구하는지 확인
     */
    public boolean isApiKeyRequired(String agency) {
        AgencyConfig agencyConfig = agencies.get(agency);
        return agencyConfig != null && agencyConfig.isApiKeyRequired();
    }
    
    /**
     * 특정 기관의 API 제한 정보를 반환
     */
    public String getRateLimit(String agency) {
        AgencyConfig agencyConfig = agencies.get(agency);
        return agencyConfig != null ? agencyConfig.getRateLimit() : "Unknown";
    }
    
    /**
     * 특정 기관의 기본 URL을 반환
     */
    public String getBaseUrl(String agency) {
        AgencyConfig agencyConfig = agencies.get(agency);
        return agencyConfig != null ? agencyConfig.getBaseUrl() : "";
    }
    
    /**
     * 모든 기관 목록 반환
     */
    public List<String> getAllAgencies() {
        return agencies.keySet().stream().sorted().toList();
    }
    
    @Data
    public static class AgencyConfig {
        private final String name;
        private final String baseUrl;
        private final boolean apiKeyRequired;
        private final String rateLimit;
        private final Map<String, Map<String, String>> endpoints;
    }
}
