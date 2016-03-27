angular.module('RDash').controller('DashboardController', ['$scope', '$firebaseObject', '$location',
    function($scope, $firebaseObject, $location) {
        var ref = new Firebase("https://watchdog-app.firebaseio.com/Bill");
        var refSync = $firebaseObject(ref);
        $scope.user = {};
        refSync.$bindTo($scope, "user");

        $scope.getStyle = function(status) {
            if (status === 0) {
                return {
                    "background-color": "#4CAF50"
                };
            } else if (status == 1) {
                return {
                    "background-color": "#FFA726"
                };
            } else if (status == 2) {
                return {
                    "background-color": "#F44336"
                };
            } else {
                return {
                    "background-color": "white"
                };
            }
        };

        $scope.getNumberOfUsers = function() {
            if ($scope.user && $scope.user.People) {
                var users = Object.keys($scope.user.People);
                return users.length;
            } else {
                return 0;
            }
        };

        $scope.getNumberOfIssues = function(requests){
            console.log('getting issues');
            var number = 0;
            if(requests){
                for(var person in $scope.user.People){
                    if($scope.user.People[person].status == 1){
                        number++;
                    }
                }
            } else {
                for(var person2 in $scope.user.People){
                    if($scope.user.People[person2].status == 2){
                        number++;
                    }
                }
            }
            return number;
        };

        $scope.debug = function() {
            console.log($scope.user.People.Andrew.sensors);
        };

        $scope.goToPerson = function(name) {
            console.log($scope.user.id + '/people/' + name);
            $location.path($scope.user.id + '/people/' + name);
        };

        $scope.getMostRecentTime = function(object) {
            var keys = Object.keys(object);
            var maxValue = Math.max.apply(Math, keys);
            return (object[maxValue]);
        };

        $scope.$watch('user', function(newVal, oldVal) {
            console.log(newVal);
        }, true);
    }
]);
