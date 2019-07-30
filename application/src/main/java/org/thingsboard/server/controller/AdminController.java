/**
 * Copyright © 2016-2019 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ibm.cloud.sdk.core.security.basicauth.BasicAuthConfig;
import com.ibm.watson.discovery.v1.Discovery;
import com.ibm.watson.discovery.v1.model.*;
import com.ibm.watson.language_translator.v3.LanguageTranslator;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.rule.engine.api.MailService;
import org.thingsboard.server.common.data.AdminSettings;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.settings.AdminSettingsService;
import org.thingsboard.server.service.security.model.SecuritySettings;
import org.thingsboard.server.service.security.permission.Operation;
import org.thingsboard.server.service.security.permission.Resource;
import org.thingsboard.server.service.security.system.SystemSecurityService;
import org.thingsboard.server.service.update.UpdateService;
import org.thingsboard.server.service.update.model.UpdateMessage;

@RestController
@RequestMapping("/api/admin")
@Slf4j
public class AdminController extends BaseController {

    @Autowired
    private MailService mailService;
    
    @Autowired
    private AdminSettingsService adminSettingsService;

    @Autowired
    private SystemSecurityService systemSecurityService;

    @Autowired
    private UpdateService updateService;

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/settings/{key}", method = RequestMethod.GET)
    @ResponseBody
    public AdminSettings getAdminSettings(@PathVariable("key") String key) throws ThingsboardException {
        try {
            accessControlService.checkPermission(getCurrentUser(), Resource.ADMIN_SETTINGS, Operation.READ);
            return checkNotNull(adminSettingsService.findAdminSettingsByKey(TenantId.SYS_TENANT_ID, key));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/settings", method = RequestMethod.POST)
    @ResponseBody 
    public AdminSettings saveAdminSettings(@RequestBody AdminSettings adminSettings) throws ThingsboardException {
        try {
            accessControlService.checkPermission(getCurrentUser(), Resource.ADMIN_SETTINGS, Operation.WRITE);
            adminSettings = checkNotNull(adminSettingsService.saveAdminSettings(TenantId.SYS_TENANT_ID, adminSettings));
            if (adminSettings.getKey().equals("mail")) {
                mailService.updateMailConfiguration();
            }
            return adminSettings;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/ibmCloudSettings/{key}", method = RequestMethod.GET)
    @ResponseBody
    public AdminSettings getIbmCloudSettings(@PathVariable("key") String key) throws ThingsboardException {
        try {
            log.info(key);
            accessControlService.checkPermission(getCurrentUser(), Resource.ADMIN_SETTINGS, Operation.READ);
            return checkNotNull(adminSettingsService.findAdminSettingsByKey(TenantId.SYS_TENANT_ID, key));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/ibmCloudSettings", method = RequestMethod.POST)
    @ResponseBody
    public AdminSettings saveIbmCloudSettings(@RequestBody AdminSettings adminSettings) throws ThingsboardException {
        try {
            log.info(adminSettings.toString());
            accessControlService.checkPermission(getCurrentUser(), Resource.ADMIN_SETTINGS, Operation.WRITE);
            adminSettings = checkNotNull(adminSettingsService.saveAdminSettings(TenantId.SYS_TENANT_ID, adminSettings));
            if (adminSettings.getKey().equals("ibm")) {
                return adminSettings;
            }
            return null;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

//    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/settings/testIBMCloud", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> sendTestIBMCloud(@RequestBody AdminSettings adminSettings) throws ThingsboardException {
        try {
            log.info(adminSettings.toString());
            accessControlService.checkPermission(getCurrentUser(), Resource.ADMIN_SETTINGS, Operation.READ);
            adminSettings = checkNotNull(adminSettingsService.saveAdminSettings(TenantId.SYS_TENANT_ID, adminSettings));
            if (adminSettings.getKey().equals("ibm")) {
                String username = adminSettings.getJsonValue().get("username").asText();
                String password = adminSettings.getJsonValue().get("password").asText();

                BasicAuthConfig config = new BasicAuthConfig.Builder()
                        .username(username)
                        .password(password)
                        .build();

                Discovery discovery = new Discovery("2017-11-07", config);

                String environmentId = null;
                String response = null;

                //See if an environment already exists
                log.info("Check if environment exists");
                ListEnvironmentsOptions listOptions = new ListEnvironmentsOptions.Builder().build();
                ListEnvironmentsResponse listResponse = discovery.listEnvironments(listOptions).execute().getResult();
                for (Environment environment : listResponse.getEnvironments()) {
                    //look for an existing environment that isn't read only
                    if (!environment.isReadOnly()) {
                        environmentId = environment.getEnvironmentId();
                        response = "Found existing environment ID: " + environmentId;
                        break;
                    }
                }

                if (environmentId == null) {
                    log.info("No environment found, creating new one...");
                    //no environment found, create a new one (assuming we are a FREE plan)
                    String environmentName = "watson_developer_cloud_test_environment";
                    CreateEnvironmentOptions createOptions = new CreateEnvironmentOptions.Builder()
                            .name(environmentName)
                            .size(null)  /* FREE */
                            .build();
                    Environment createResponse = discovery.createEnvironment(createOptions).execute().getResult();
                    environmentId = createResponse.getEnvironmentId();
                    response = "Created new environment ID: " + environmentId;

                    //wait for environment to be ready
                    log.info("Waiting for environment to be ready...");
                    boolean environmentReady = false;
                    while (!environmentReady) {
                        GetEnvironmentOptions getEnvironmentOptions = new GetEnvironmentOptions.Builder(environmentId).build();
                        Environment getEnvironmentResponse = discovery.getEnvironment(getEnvironmentOptions).execute().getResult();
                        environmentReady = getEnvironmentResponse.getStatus().equals(Environment.Status.ACTIVE);
                        try {
                            if (!environmentReady) {
                                Thread.sleep(500);
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Interrupted", e);
                        }
                    }
                    log.info("Environment Ready!");
                }

                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
            return null;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/securitySettings", method = RequestMethod.GET)
    @ResponseBody
    public SecuritySettings getSecuritySettings() throws ThingsboardException {
        try {
            accessControlService.checkPermission(getCurrentUser(), Resource.ADMIN_SETTINGS, Operation.READ);
            return checkNotNull(systemSecurityService.getSecuritySettings(TenantId.SYS_TENANT_ID));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/securitySettings", method = RequestMethod.POST)
    @ResponseBody
    public SecuritySettings saveSecuritySettings(@RequestBody SecuritySettings securitySettings) throws ThingsboardException {
        try {
            accessControlService.checkPermission(getCurrentUser(), Resource.ADMIN_SETTINGS, Operation.WRITE);
            securitySettings = checkNotNull(systemSecurityService.saveSecuritySettings(TenantId.SYS_TENANT_ID, securitySettings));
            return securitySettings;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/settings/testMail", method = RequestMethod.POST)
    public void sendTestMail(@RequestBody AdminSettings adminSettings) throws ThingsboardException {
        try {
            accessControlService.checkPermission(getCurrentUser(), Resource.ADMIN_SETTINGS, Operation.READ);
            adminSettings = checkNotNull(adminSettings);
            if (adminSettings.getKey().equals("mail")) {
               String email = getCurrentUser().getEmail();
               mailService.sendTestMail(adminSettings.getJsonValue(), email);
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/updates", method = RequestMethod.GET)
    @ResponseBody
    public UpdateMessage checkUpdates() throws ThingsboardException {
        try {
            return updateService.checkUpdates();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
