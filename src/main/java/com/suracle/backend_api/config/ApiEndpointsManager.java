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
 *
 * 변경 요약 (2025-10-09):
 * - Commerce(Census) 국제무역 HS 엔드포인트 추가 (imports/hs, exports/hs 등)
 * - CBP(AES/ACE) 엔드포인트는 "인증 필요" 주석 및 비수집 권고 안내
 * - EPA Envirofacts TRI 실테이블 tri.tri_facility 확인 반영 (기존 유지)
 * - FCC Device Authorization(Socrata) 사용 시 X-App-Token 권장 및 백오프 필요 주석
 * - (가이드) RequirementsApiClient에서 공통 헤더 권장:
 *     Accept: application/json
 *     User-Agent: LawGenie-Backend/1.0
 *     (필요시) X-App-Token: <Socrata App Token>  // FCC/OpenData만
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
            "1000/day without key, higher with key (openFDA)",
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
                    "enforcement", "https://api.fda.gov/food/enforcement.json"
                    // "event" 엔드포인트는 openFDA에 존재하지 않아 사용하지 않음
                ),
                "cosmetics", Map.of(
                    "event", "https://api.fda.gov/cosmetics/event.json"
                ),
                "animalandveterinary", Map.of(
                    "event", "https://api.fda.gov/animalandveterinary/event.json"
                )
            )
        ));

        // USDA (U.S. Department of Agriculture) - FDC는 API 키 필요
        agencyMap.put("usda", new AgencyConfig(
            "USDA",
            "https://api.nal.usda.gov",
            true,
            "1000/day (FDC requires API key)",
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
            "Typical: 1000/hour (varies by service)",
            Map.of(
                // Envirofacts(시설/사업장 중심)는 상품 키워드 탐색과 목적 불일치 → 기본 비활성화 권고
                // 필요 시 아래 블록을 복구해서 사용하세요.
                // "envirofacts", Map.of(
                //     "base", "https://data.epa.gov/efservice",
                //     "tri_search", "https://data.epa.gov/efservice/tri.tri_facility/facility_name/CONTAINING/{query}/JSON",
                //     "facility_search", "https://data.epa.gov/efservice/frs.frs_facilities/facility_name/CONTAINING/{query}/JSON",
                //     "chemical_search", "https://data.epa.gov/efservice/srs.srs_chemicals/chem_name/CONTAINING/{query}/JSON",
                //     "rcra_search", "https://data.epa.gov/efservice/rcra.rcra_handler/handler_name/CONTAINING/{query}/JSON",
                //     "sdwis_search", "https://data.epa.gov/efservice/sdwis.sdwis_public_water_systems/pws_name/CONTAINING/{query}/JSON"
                // ),
                // CompTox Dashboard API (성분/화학물질 키워드 검색에 유용)
                "comptox", Map.of(
                    "base", "https://comptox.epa.gov/dashboard/api",
                    "search", "https://comptox.epa.gov/dashboard/api/chemical/search",
                    "details", "https://comptox.epa.gov/dashboard/api/chemical/details",
                    "lists", "https://comptox.epa.gov/dashboard/api/chemical/lists"
                ),
                // Air Quality System (측정치 중심, 키워드 검색보다는 파라미터)
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
        // 방송국 공개 파일 — 장비인증과 별개
        fccEndpoints.put("public_files", Map.of(
            "base", "https://publicfiles.fcc.gov",
            "api", "https://publicfiles.fcc.gov/api",
            "search", "https://publicfiles.fcc.gov/api/search",
            "stations", "https://publicfiles.fcc.gov/api/stations",
            "file_by_id", "https://publicfiles.fcc.gov/api/service/file/id/{fileId}.json",
            "file_history", "https://publicfiles.fcc.gov/api/service/file/history.json",
            "station_files", "https://publicfiles.fcc.gov/api/service/station/{callsign}/files.json"
        ));
        // 방송 서비스 메타
        fccEndpoints.put("service_data", Map.of(
            "facility_search", "https://publicfiles.fcc.gov/api/service/facility/search/{keyword}",
            "relationship_frn", "https://publicfiles.fcc.gov/api/service/relationship/frn/{frn}",
            "service_facility_all", "https://publicfiles.fcc.gov/api/service/{serviceType}/facility/getall",
            "service_facility_details", "https://publicfiles.fcc.gov/api/service/{serviceType}/facility/id/{entityID}",
            "service_applications", "https://publicfiles.fcc.gov/api/service/{serviceType}/applications/facility/{entityID}",
            "service_eeo", "https://publicfiles.fcc.gov/api/service/{serviceType}/eeo/facilityid/{entityID}",
            "service_ownership", "https://publicfiles.fcc.gov/api/service/{serviceType}/ownership/facilityid/{entityID}"
        ));
        // 케이블/위성 파생 데이터
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
        // Device Authorization (회사/그랜티 기반으로 상품 키워드 탐색과 불일치) → 기본 비활성화 권고
        // 필요 시 아래 블록을 복구해서 사용하세요.
        // fccEndpoints.put("device_authorization", Map.of(
        //     "base", "https://opendata.fcc.gov/resource/3b3k-34jp.json",
        //     "grants", "https://opendata.fcc.gov/resource/3b3k-34jp.json",
        //     "applications", "https://opendata.fcc.gov/resource/3b3k-34jp.json",
        //     "search_by_grantee_code", "https://opendata.fcc.gov/resource/3b3k-34jp.json?grantee_code={code}&$limit=50",
        //     "search_by_name", "https://opendata.fcc.gov/resource/3b3k-34jp.json?$select=grantee_code,grantee_name,state&$where=upper(grantee_name)%20like%20%27{name}%25%27&$limit=50"
        // ));
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
            "Socrata throttling may apply; use X-App-Token; implement retry/backoff",
            fccEndpoints
        ));

        // CBP (Customs and Border Protection)
        // ⚠️ 주의: AESDirect/ACE 연동은 인증/승인 사용자만 가능. 공개 수집 대상 아님.
        // 아래 엔드포인트는 참고용으로만 유지하고, 카탈로그 사용 시 "인증 필요"로 표기하거나 제외 권장.
        agencyMap.put("cbp", new AgencyConfig(
            "CBP",
            "https://trade.cbp.dhs.gov",
            true,
            "Restricted: ACE/AES credentials required (not for public harvesting)",
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
                )
                // 참고: HS 기준 무역통계는 CBP가 아니라 Commerce(Census) API 사용 권장
            )
        ));

        // CPSC (Consumer Product Safety Commission)
        agencyMap.put("cpsc", new AgencyConfig(
            "CPSC",
            "https://www.saferproducts.gov",
            false,
            "Typical public API limits",
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

        // Commerce (Department of Commerce) — International Trade (HS 지원)
        agencyMap.put("commerce", new AgencyConfig(
            "Commerce",
            "https://api.census.gov",
            true,  // API Key 권장 (무키도 되지만 일일 호출 제한 낮음)
            "Default ~500/day without key; higher with key",
            Map.of(
                "trade_data", Map.of(
                    "base", "https://api.census.gov/data/timeseries/intltrade/",
                    // 신규: HS 코드 기반 시계열 (월별)
                    "imports_hs", "https://api.census.gov/data/timeseries/intltrade/imports/hs",
                    "exports_hs", "https://api.census.gov/data/timeseries/intltrade/exports/hs",
                    // (옵션) 항만 단위 HS (일부 공개 세부): 필요 시 사용
                    "imports_porths", "https://api.census.gov/data/timeseries/intltrade/imports/porths",
                    "exports_porths", "https://api.census.gov/data/timeseries/intltrade/exports/porths"
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

        // NTIA (National Telecommunications and Information Administration)
        agencyMap.put("ntia", new AgencyConfig(
            "NTIA",
            "https://nbam.ntia.gov",
            false,
            "Typical public API limits",
            Map.of(
                "nbam_api", Map.of(
                    "base", "https://nbam.ntia.gov/api",
                    "search_definition", "https://nbam.ntia.gov/api/search/definition/",
                    "search", "https://nbam.ntia.gov/api/search"
                ),
                "spectrum_data", Map.of(
                    "base", "https://www.ntia.gov/data",
                    "spectrum_map", "https://www.ntia.gov/data/spectrum-map",
                    "5g_data", "https://www.ntia.gov/data/5g-data"
                ),
                "broadband_data", Map.of(
                    "base", "https://www.ntia.gov/data/broadband",
                    "mapping", "https://www.ntia.gov/data/broadband-mapping",
                    "availability", "https://www.ntia.gov/data/broadband-availability"
                )
            )
        ));

        // DOT (Department of Transportation)
        agencyMap.put("dot", new AgencyConfig(
            "DOT",
            "https://data.transportation.gov",
            false,
            "Typical public API limits",
            Map.of(
                "safety_data", Map.of(
                    "base", "https://data.transportation.gov",
                    "nhtsa", "https://www.nhtsa.gov/api",
                    "faa_safety", "https://www.faa.gov/data_research/accident_incident"
                ),
                "traffic_data", Map.of(
                    "base", "https://data.transportation.gov",
                    "traffic_volume", "https://data.transportation.gov/Traffic-Volume",
                    "traffic_safety", "https://data.transportation.gov/Traffic-Safety"
                ),
                "aviation_data", Map.of(
                    "base", "https://data.transportation.gov",
                    "faa_data", "https://www.faa.gov/data_research",
                    "airline_data", "https://data.transportation.gov/Aviation"
                )
            )
        ));

        // DOE (Department of Energy)
        agencyMap.put("doe", new AgencyConfig(
            "DOE",
            "https://www.osti.gov",
            false,
            "Typical public API limits",
            Map.of(
                "pages_api", Map.of(
                    "base", "https://www.osti.gov/pages/api/v1",
                    "records", "https://www.osti.gov/pages/api/v1/records",
                    "search", "https://www.osti.gov/pages/api/v1/records"
                ),
                "doecode_api", Map.of(
                    "base", "https://www.osti.gov/doecodeapi/services",
                    "search", "https://www.osti.gov/doecodeapi/services/search"
                ),
                "energy_data", Map.of(
                    "base", "https://api.eia.gov",
                    "petroleum", "https://api.eia.gov/petroleum",
                    "natural_gas", "https://api.eia.gov/naturalgas",
                    "electricity", "https://api.eia.gov/electricity",
                    "coal", "https://api.eia.gov/coal"
                )
            )
        ));

        // DOI (Department of the Interior)
        agencyMap.put("doi", new AgencyConfig(
            "DOI",
            "https://data.doi.gov",
            false,
            "Typical public API limits",
            Map.of(
                "natural_resources", Map.of(
                    "base", "https://data.doi.gov",
                    "minerals", "https://data.doi.gov/Minerals",
                    "water", "https://data.doi.gov/Water",
                    "land", "https://data.doi.gov/Land"
                ),
                "wildlife_data", Map.of(
                    "base", "https://data.doi.gov",
                    "fws_data", "https://www.fws.gov/data",
                    "endangered_species", "https://data.doi.gov/Endangered-Species"
                ),
                "national_parks", Map.of(
                    "base", "https://data.doi.gov",
                    "nps_data", "https://www.nps.gov/data",
                    "recreation", "https://data.doi.gov/Recreation"
                )
            )
        ));

        // DOL (Department of Labor)
        agencyMap.put("dol", new AgencyConfig(
            "DOL",
            "https://dataportal.dol.gov",
            false,
            "Typical public API limits",
            Map.of(
                "data_portal", Map.of(
                    "base", "https://dataportal.dol.gov",
                    "api_examples", "https://dataportal.dol.gov/api-examples",
                    "api", "https://dataportal.dol.gov/api"
                ),
                "employment_data", Map.of(
                    "base", "https://api.dol.gov",
                    "unemployment", "https://api.dol.gov/unemployment",
                    "employment_statistics", "https://api.dol.gov/employment-statistics",
                    "wage_data", "https://api.dol.gov/wage-data"
                ),
                "safety_data", Map.of(
                    "base", "https://api.dol.gov",
                    "osha_data", "https://api.dol.gov/osha",
                    "workplace_safety", "https://api.dol.gov/workplace-safety"
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

        public AgencyConfig(String name, String baseUrl, boolean apiKeyRequired, String rateLimit, Map<String, Map<String, String>> endpoints) {
            this.name = name;
            this.baseUrl = baseUrl;
            this.apiKeyRequired = apiKeyRequired;
            this.rateLimit = rateLimit;
            this.endpoints = endpoints;
        }
    }
}
