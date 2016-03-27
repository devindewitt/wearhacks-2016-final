'use strict';

angular.module('RDash').config(['$stateProvider', '$urlRouterProvider',
    function($stateProvider, $urlRouterProvider) {

        $urlRouterProvider.otherwise('/');

        $stateProvider
            .state('index', {
                url: '/',
                templateUrl: 'templates/dashboard.html',
            })
            .state('profile', {
                url: '/:user/profiles/:profile',
                templateUrl: 'templates/dashboard.html',
            })
            .state('tables', {
                url: '/tables',
                templateUrl: 'templates/tables.html'
            });
    }
]);
