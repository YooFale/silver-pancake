app.controller('searchController', function($scope, searchService) {

	// 定义搜索对象的结构
	$scope.searchMap = {
		'keywords' : '',
		'category' : '',
		'brand' : '',
		'spec' : {}
	};// 搜索对象

	// 搜索
	$scope.search = function() {
		searchService.search($scope.searchMap).success(function(response) {
			$scope.resultMap = response;
		});
	}

	// 添加搜索项,改变searchMap的值(更改变量来实现)
	$scope.addSearchItem = function(key, value) {
		if (key == 'category' || key == 'brand') {// 如果用户点击的是分类或者品牌
			$scope.searchMap[key] = value;
		} else {// 用户点击的是规格,不确定,所以else方便处理
			$scope.searchMap.spec[key] = value;
		}
		$scope.search();//查询
	}
	
	//撤销搜索项
	$scope.removeSearchItem = function(key){
		if (key == 'category' || key == 'brand') {// 如果用户点击的是分类或者品牌
			$scope.searchMap[key] = "";
		} else {// 用户点击的是规格,不确定,所以else方便处理
			delete $scope.searchMap.spec[key];
		}
		$scope.search();//查询
	}
});