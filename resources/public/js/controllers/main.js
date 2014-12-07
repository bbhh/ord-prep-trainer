'use strict';

angular.module('ordPrepTrainerApp')
  .controller('MainCtrl', MainCtrl);

function MainCtrl($scope, $http, $routeParams) {
    var API_URL = '/api';

    $scope.userId = 1;     // TODO: implement login and set to current user's ID

    $scope.sections = [
        {name: 'Keywords', _id: 'keywords', selected: false},
        {name: 'Purpose', _id: 'purpose', selected: false},
        {name: 'Author/Date', _id: 'author/date', selected: false},
        {name: 'Parallels', _id: 'parallels', selected: false},
        {name: 'Outline', _id: 'outline', selected: false},
        {name: 'Key Passages', _id: 'key-passages', selected: false},
        {name: 'Key Dates', _id: 'key-dates', selected: false},
        {name: 'Key People', _id: 'key-people', selected: false}
    ];
    $scope.selectedSections = [];

    $scope.books = [];
    $scope.selectedBooks = [];

    $scope.results = [];

    $scope.stars = [];

    $scope.data = {
        showStarredOnly: false,
        hideAnswers: false
    }

    var init = function() {
        $scope.getBooks();
        $scope.getStars();
    };

    ///// section selection
    $scope.selectAllSections = function() {
        $scope.selectedSections.length = 0;
        angular.forEach($scope.sections, function(value, key) {
            $scope.selectedSections.push(value['_id']);
        });
    };

    $scope.selectNoSections = function() {
        $scope.selectedSections.length = 0;
    };

    $scope.toggleSectionSelection = function(sectionName) {
        var idx = $scope.selectedSections.indexOf(sectionName);

        // currently selected
        if (idx > -1) {
            $scope.selectedSections.splice(idx, 1);
        }
        // newly selected
        else {
            $scope.selectedSections.push(sectionName);
        }
    };

    ///// book selection
    $scope.selectAllBooks = function() {
        $scope.selectedBooks.length = 0;
        angular.forEach($scope.books, function(value, key) {
            $scope.selectedBooks.push(value['name']);
        });
    };

    $scope.selectOTBooks = function() {
        $scope.selectedBooks.length = 0;
        var i = 1;
        angular.forEach($scope.books, function(value, key) {
            if (i <= 39) {
                $scope.selectedBooks.push(value['name']);
            }
            i++;
        });
    };

    $scope.selectNTBooks = function() {
        $scope.selectedBooks.length = 0;
        var i = 1;
        angular.forEach($scope.books, function(value, key) {
            if (i > 39) {
                $scope.selectedBooks.push(value['name']);
            }
            i++;
        });
    };

    $scope.selectNoBooks = function() {
        $scope.selectedBooks.length = 0;
    };

    $scope.toggleBookSelection = function(bookName) {
        var idx = $scope.selectedBooks.indexOf(bookName);

        // currently selected
        if (idx > -1) {
            $scope.selectedBooks.splice(idx, 1);
        }
        // newly selected
        else {
            $scope.selectedBooks.push(bookName);
        }
    };

    // whenever section/book selections change...
    $scope.$watch(function() { 
        return angular.toJson([$scope.selectedSections, $scope.selectedBooks]); 
    }, function(newValue, oldValue) {
        if (newValue == null) return;

        $http({url: API_URL + '/results',
               method: 'GET',
               params: {sections: $scope.selectedSections.join(':'),
                        books: $scope.selectedBooks.join(':')}}).success(function(data, status) {
                            $scope.results.length = 0;
                            $.each(data, function(index, value) {
                                value['id'] = value['book-name'] + ':' + value['section-name'];
                                $scope.results.push(value);
                            });
                        });
    }, true);

    ///// results filtering by star
    $scope.showAllFilter = function(result) {
        return true;
    }
    $scope.starredOnlyFilter = function(result) {
        return $scope.stars.indexOf(result['id']) != -1;
    };

    $scope.resultsFilter = $scope.showAllFilter;

    // whenever filter changes...
    $scope.$watch('data.showStarredOnly', function(newValue, oldValue) {
        $scope.resultsFilter = (newValue == true) ? $scope.starredOnlyFilter : $scope.showAllFilter;
    });

    ///// remote calls
    $scope.getBooks = function() {
        // retrieve book names
        $http({url: API_URL + '/book-names',
               method: 'GET'}).success(function(data, status) {
                   $scope.books = [];
                   angular.forEach(data, function(value, key) {
                       this.push({name: value, selected: false});
                   }, $scope.books);
               });
    };

    $scope.getStars = function() {
        // retrieve stars for user
        $http({url: API_URL + '/stars',
               method: 'GET',
               params: {user: $scope.userId}}).success(function(data, status) {
                   $scope.stars = [];
                   angular.forEach(data, function(value, key) {
                       this.push(value);
                   }, $scope.stars);
               });
    };

    $scope.starItem = function(bookName, sectionName) {
        var item = bookName + ':' + sectionName;

        // toggle in stars
        var idx = $.inArray(item, $scope.stars);
        if (idx != -1) {
            $scope.stars.splice(idx, 1);
        } else {
            $scope.stars.push(item);
        }

        // update stars in DB
        $http({url: API_URL + '/stars',
               method: 'PUT',
               params: {user: $scope.userId,
                        stars: $scope.stars.join(',')}}).success(function(data, status) {
                            // ...
               });
    };

    init();
}
