package org.ekstep.genieservices.config;

import com.google.gson.internal.LinkedTreeMap;

import org.ekstep.genieservices.BaseService;
import org.ekstep.genieservices.IConfigService;
import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.CommonConstants;
import org.ekstep.genieservices.commons.GenieResponseBuilder;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.MasterData;
import org.ekstep.genieservices.commons.bean.enums.MasterDataType;
import org.ekstep.genieservices.commons.utils.DateUtil;
import org.ekstep.genieservices.commons.utils.FileUtil;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.ekstep.genieservices.commons.utils.StringUtil;
import org.ekstep.genieservices.config.db.model.MasterDataModel;
import org.ekstep.genieservices.config.db.model.OrdinalsModel;
import org.ekstep.genieservices.config.db.model.ResourceBundleModel;
import org.ekstep.genieservices.config.network.OrdinalsAPI;
import org.ekstep.genieservices.config.network.ResourceBundleAPI;
import org.ekstep.genieservices.config.network.TermsAPI;
import org.ekstep.genieservices.telemetry.TelemetryLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the implementation of the interface {@link IConfigService}
 *
 */
public class ConfigServiceImpl extends BaseService implements IConfigService {

    private static final String TAG = ConfigServiceImpl.class.getSimpleName();

    private static final String DB_KEY_ORDINALS = "ordinals_key";

    public ConfigServiceImpl(AppContext appContext) {
        super(appContext);
    }

    @Override
    public GenieResponse<List<MasterData>> getAllMasterData() {
        return null;
    }

    @Override
    public GenieResponse<MasterData> getMasterData(MasterDataType type) {

        HashMap params = new HashMap();
        params.put("type", type.getValue());
        params.put("logLevel", CommonConstants.LOG_LEVEL);

        if (getLongFromKeyValueStore(ConfigConstants.PreferenceKey.MASTER_DATA_API_EXPIRATION_KEY) == 0) {
            initializeMasterData();
        } else if (hasExpired(ConfigConstants.PreferenceKey.MASTER_DATA_API_EXPIRATION_KEY)) {
            refreshMasterData();
        }

        MasterDataModel masterDataModel = MasterDataModel.findByType(mAppContext.getDBSession(), type.getValue());

        String result = masterDataModel.getMasterDataJson();

        MasterData masterData = GsonUtil.fromJson(result, MasterData.class);

        GenieResponse<MasterData> response;
        if (result != null) {
            response = GenieResponseBuilder.getSuccessResponse("MasterData retrieved successfully", MasterData.class);
            response.setResult(masterData);
            TelemetryLogger.logSuccess(mAppContext, response, new HashMap(), TAG, "getMasterData@ConfigServiceImpl", params);
            return response;
        } else {
            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.NO_DATA_FOUND, "", ServiceConstants.SERVICE_ERROR, MasterData.class);
            TelemetryLogger.logFailure(mAppContext, response, TAG, "getMasterData@ConfigServiceImpl", params, "Unable to get masterdata");
            return response;
        }
    }

    private void initializeMasterData() {
        String storedData = FileUtil.readFileFromClasspath(ConfigConstants.ResourceFile.MASTER_DATA_JSON_FILE);
        if (!StringUtil.isNullOrEmpty(storedData)) {
            saveMasterData(storedData);
        }
        refreshMasterData();
    }

    private void saveMasterData(String response) {
        LinkedTreeMap map = GsonUtil.fromJson(response, LinkedTreeMap.class);
        Map result = ((LinkedTreeMap) map.get("result"));
        if (result != null) {
            Double ttl = (Double) result.get("ttl");
            saveDataExpirationTime(ttl, ConfigConstants.PreferenceKey.MASTER_DATA_API_EXPIRATION_KEY);
            result.remove("ttl");
            for (Object key : result.keySet()) {
                MasterDataModel eachMasterData = MasterDataModel.build(mAppContext.getDBSession(), (String) key, GsonUtil.toJson(result.get(key)));
                MasterDataModel masterDataInDb = MasterDataModel.findByType(mAppContext.getDBSession(), String.valueOf(key));
                if (masterDataInDb != null) {
                    eachMasterData.update();
                } else {
                    eachMasterData.save();
                }
            }
        }
    }

    private void refreshMasterData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TermsAPI termsAPI = new TermsAPI(mAppContext);
                GenieResponse genieResponse = termsAPI.get();
                if (genieResponse.getStatus()) {
                    String body = genieResponse.getResult().toString();
                    saveMasterData(body);
                }
            }
        }).start();
    }

    @Override
    public GenieResponse<Map<String, Object>> getResourceBundle(String languageIdentifier) {
        HashMap params = new HashMap();
        params.put("ResourceBundle", languageIdentifier);
        params.put("mode", TelemetryLogger.getNetworkMode(mAppContext.getConnectionInfo()));
        params.put("logLevel", CommonConstants.LOG_LEVEL);

        if (getLongFromKeyValueStore(ConfigConstants.PreferenceKey.RESOURCE_BUNDLE_API_EXPIRATION_KEY) == 0) {
            initializeResourceBundle();
        } else if (hasExpired(ConfigConstants.PreferenceKey.RESOURCE_BUNDLE_API_EXPIRATION_KEY)) {
            refreshResourceBundle();
        }
        ResourceBundleModel resourceBundle = ResourceBundleModel.findById(mAppContext.getDBSession(), languageIdentifier);
        String result = resourceBundle.getResourceString();
        Map<String, Object> resourceBundleMap = new HashMap<>();
        resourceBundleMap.put(resourceBundle.getIdentifier(), resourceBundle.getResourceString());

        GenieResponse<Map<String, Object>> response;
        if (result != null) {
            response = GenieResponseBuilder.getSuccessResponse("");
            response.setResult(resourceBundleMap);
            TelemetryLogger.logSuccess(mAppContext, response, new HashMap(), TAG, "getResourceBundle@ConfigServiceImpl", params);
            return response;
        } else {
            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.NO_DATA_FOUND, "", ServiceConstants.SERVICE_ERROR);
            TelemetryLogger.logFailure(mAppContext, response, TAG, "getResourceBundle@ConfigServiceImpl", params, "Unable to get resourcebundle");
            return response;
        }
    }

    private void initializeResourceBundle() {
        String storedData = FileUtil.readFileFromClasspath(ConfigConstants.ResourceFile.RESOURCE_BUNDLE_JSON_FILE);
        if (!StringUtil.isNullOrEmpty(storedData)) {
            saveResourceBundle(storedData);
        }
        refreshResourceBundle();
    }

    private void saveResourceBundle(String response) {
        LinkedTreeMap map = GsonUtil.fromJson(response, LinkedTreeMap.class);
        Map resultMap = (LinkedTreeMap) map.get("result");
        Map result = null;

        if (resultMap.containsKey("resourcebundles")) {
            result = (Map) resultMap.get("resourcebundles");
        }

        if (result != null) {
            Double ttl = (Double) resultMap.get("ttl");
            saveDataExpirationTime(ttl, ConfigConstants.PreferenceKey.RESOURCE_BUNDLE_API_EXPIRATION_KEY);
            for (Object key : result.keySet()) {
                ResourceBundleModel eachResourceBundle = ResourceBundleModel.build(mAppContext.getDBSession(), (String) key, GsonUtil.toJson(result.get(key)));
                ResourceBundleModel resourceBundleInDb = ResourceBundleModel.findById(mAppContext.getDBSession(), String.valueOf(key));
                if (resourceBundleInDb != null) {
                    eachResourceBundle.update();
                } else {
                    eachResourceBundle.save();
                }
            }
        }
    }

    private void refreshResourceBundle() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ResourceBundleAPI resourceBundleAPI = new ResourceBundleAPI(mAppContext);
                GenieResponse genieResponse = resourceBundleAPI.get();
                if (genieResponse.getStatus()) {
                    String body = genieResponse.getResult().toString();
                    saveResourceBundle(body);
                }
            }
        }).start();
    }

    public GenieResponse<Map<String, Object>> getOrdinals() {
        HashMap params = new HashMap();
        params.put("Ordinals", "Get ordinals");
        params.put("mode", TelemetryLogger.getNetworkMode(mAppContext.getConnectionInfo()));
        params.put("logLevel", CommonConstants.LOG_LEVEL);

        if (getLongFromKeyValueStore(ConfigConstants.PreferenceKey.ORDINAL_API_EXPIRATION_KEY) == 0) {
            initializeOrdinalsData();
        } else if (hasExpired(ConfigConstants.PreferenceKey.ORDINAL_API_EXPIRATION_KEY)) {
            refreshOrdinals();
        }

        OrdinalsModel ordinals = OrdinalsModel.findById(mAppContext.getDBSession(), DB_KEY_ORDINALS);
        HashMap ordinalsMap = GsonUtil.fromJson(ordinals.getJSON(), HashMap.class);

        GenieResponse<Map<String, Object>> response;
        if (ordinalsMap != null) {
            response = GenieResponseBuilder.getSuccessResponse("");
            response.setResult(ordinalsMap);
            TelemetryLogger.logSuccess(mAppContext, response, new HashMap(), TAG, "getOrdinals@ConfigServiceImpl", params);
            return response;
        } else {
            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.NO_DATA_FOUND, "", ServiceConstants.SERVICE_ERROR);
            TelemetryLogger.logFailure(mAppContext, response, TAG, "getOrdinals@ConfigServiceImpl", params, "Unable to get ordinals");
            return response;
        }
    }

    private void initializeOrdinalsData() {
        String storedData = FileUtil.readFileFromClasspath(ConfigConstants.ResourceFile.ORDINALS_JSON_FILE);
        if (!StringUtil.isNullOrEmpty(storedData)) {
            saveOrdinals(storedData);
        }
        refreshOrdinals();
    }

    private void saveOrdinals(String response) {
        LinkedTreeMap map = GsonUtil.fromJson(response, LinkedTreeMap.class);
        LinkedTreeMap resultLinkedTreeMap = (LinkedTreeMap) map.get("result");
        if (resultLinkedTreeMap.containsKey("ordinals")) {
            Double ttl = (Double) map.get("ttl");
            saveDataExpirationTime(ttl, ConfigConstants.PreferenceKey.ORDINAL_API_EXPIRATION_KEY);
            OrdinalsModel ordinals = OrdinalsModel.build(mAppContext.getDBSession(), DB_KEY_ORDINALS, GsonUtil.toJson(resultLinkedTreeMap.get("ordinals")));
            OrdinalsModel ordinalsInDb = OrdinalsModel.findById(mAppContext.getDBSession(), DB_KEY_ORDINALS);
            if (ordinalsInDb != null) {
                ordinals.update();
            } else {
                ordinals.save();
            }
        }
    }

    private void refreshOrdinals() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OrdinalsAPI api = new OrdinalsAPI(mAppContext);
                GenieResponse genieResponse = api.get();

                if (genieResponse.getStatus()) {
                    String body = genieResponse.getResult().toString();
                    saveOrdinals(body);
                }
            }
        }).start();
    }

    private void saveDataExpirationTime(Double ttl, String key) {
        if (ttl != null) {
            long ttlInMilliSeconds = (long) (ttl * DateUtil.MILLISECONDS_IN_AN_HOUR);
            Long currentTime = DateUtil.getEpochTime();
            long expiration_time = ttlInMilliSeconds + currentTime;

            putToKeyValueStore(key, expiration_time);
        }
    }

    private boolean hasExpired(String key) {
        Long currentTime = DateUtil.getEpochTime();
        long expirationTime = getLongFromKeyValueStore(key);
        return currentTime > expirationTime;
    }

}