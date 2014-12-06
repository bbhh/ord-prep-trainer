'use strict';

angular
  .module('ordPrepTrainerApp', [
    'ngResource',
    'ngSanitize',
    'ngRoute',
    'ui.bootstrap',
    'mgcrea.ngStrap.navbar'
  ])
  .config(function ($routeProvider, $sceDelegateProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'partials/main.html'
      })
      .when('/about', {
        templateUrl: 'partials/about.html'
      })
      .otherwise({
        redirectTo: '/'
      });
  });
