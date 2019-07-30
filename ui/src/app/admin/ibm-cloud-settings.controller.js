/*
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

/*@ngInject*/
export default function IbmCloudSettingsController(adminService, toast, $mdExpansionPanel, $state, $translate, $log) {

    var vm = this;
    vm.$mdExpansionPanel = $mdExpansionPanel;

    vm.save = save;
    vm.sendTestIBMCloudConnection = sendTestIBMCloudConnection;


    $translate('admin.imb-cloud-test-sent').then(function (translation) {
        vm.testIBMCloudSent = translation;
    }, function (translationId) {
        vm.testIBMCloudSent = translationId;
    });

    loadSettings();

    function loadSettings() {
        $log.log($state.$current.data.key);
        adminService.getIbmCloudSettings($state.$current.data.key).then(function success(ibmCloudSettings) {
            vm.ibmCloudSettings = ibmCloudSettings;
        });
    }

    function save() {
        adminService.saveIbmCloudSettings(vm.ibmCloudSettings).then(function success(ibmCloudSettings) {
            vm.ibmCloudSettings = ibmCloudSettings;
            vm.settingsForm.$setPristine();
        });
    }

    function sendTestIBMCloudConnection(){
        adminService.sendTestIBMCloudConnection(vm.ibmCloudSettings).then(function success(ibmAuth) {
            $log.log(ibmAuth);
            toast.showSuccess($translate.instant('admin.imb-cloud-test-sent'));
        });
    }

}
