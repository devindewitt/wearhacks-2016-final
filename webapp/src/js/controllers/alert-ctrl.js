/**
 * Alerts Controller
 */

angular.module('RDash')
.controller('AlertsCtrl', ['$scope','$firebaseObject', function ($scope, $firebaseObject) {
    var ref = new Firebase("https://watchdog-app.firebaseio.com/Bill");
    var refSync = $firebaseObject(ref);
    $scope.alert = {};

    refSync.$bindTo($scope, "alert");

    $scope.alerts = [{
        type: 'danger',
        msg: 'Old person fell down'
    }];

    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };
}]);
