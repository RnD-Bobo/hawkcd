/* Copyright (C) 2016 R&D Solutions Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

angular
    .module('hawk')
    .factory('loginService', ['$http', '$q', 'CONSTANTS', 'loggerService', 'authenticationService', 'authDataService', 'viewModel', 'viewModelUpdater', '$location', '$state', '$auth', function ($http, $q, CONSTANTS, loggerService, authenticationService, authDataService, viewModel, viewModelUpdater, $location, $state, $auth) {
        var loginService = this;
        var tokenEndPoint = '/Token';

        var userInfo;
        var deferred;

        this.login = function (userName, password) {
            deferred = $q.defer();
            var data = "grant_type=password&username=" + userName + "&password=" + password;
            $http.post(tokenEndPoint, data, {
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    }
                }).success(function (response) {
                    var o = response;
                    console.log(response.userName);
                    userInfo = {
                        accessToken: response.access_token,
                        refreshToken: response.refresh_token,
                        username: response.userName,
                        email: response.userName,
                        issued: response['.issued'],
                        expires: response['.expires']
                    };
                    authenticationService.setTokenInfo(userInfo);
                    authDataService.authenticationData.IsAuthenticated = true;
                    authDataService.authenticationData.userName = response.userName;


                    $state.go('index.pipelines');
                    deferred.resolve(null);

                })
                .error(function (err, status) {
                    authDataService.authenticationData.IsAuthenticated = false;
                    authDataService.authenticationData.userName = "";
                    deferred.reject(err);
                });
            return deferred.promise;
        };


//         this.logout = function () {
//
// //           $http.post('http://localhost:8080/auth/logout', viewModel.user.username, {
// //               headers: {
// //                   'Content-Type': 'application/json'
// //               }
// //           }).then(function (res){
// //            console.log(res);
// //            }).resolve(function (err){
// //            });
//
//                 $http({
//                 method: 'POST',
//                 url: 'http://localhost:8080/auth/logout',
//                 data: viewModel.user.username
//               }).then(function successCallback(response) {
//                   console.log(response);
//                 }, function errorCallback(response) {
//                   console.log(response);
//                });
//
//
//            // //Api for logout?
//        };

        this.logout = function () {
            $http({
                method: 'POST',
                url: CONSTANTS.SERVER_URL + '/auth/logout',
                data: viewModel.user.username
            })
                .then(function(res) {
                    $auth.logout();
                    localStorage.clear();
                    $location.path("/authenticate");
                    loggerService.log(res);
                }, function(err) {
                    loggerService.log(err);
                });
        };

       this.logoutUser = function (username) {
           //$auth.removeToken();
            debugger;
           $auth.logout();
           $location.path("/authenticate");
           viewModelUpdater.flushViewModel();
       };




        return loginService;
    }]);
