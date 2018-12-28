app.controller('searchController', function($scope, $location,searchService) {

	// 定义搜索对象的结构
	$scope.searchMap = {
		'keywords' : '',
		'category' : '',
		'brand' : '',
		'spec' : {},
		'price':'',
		'pageNo':1,
		'pageSize':40,
		'sort':'',
		'sortField':''
	};// 搜索对象

	// 搜索
	$scope.search = function() {
		$scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
		searchService.search($scope.searchMap).success(function(response) {
			$scope.resultMap = response;
			//查询后重置页面为第一页
			buildPageLabel();//构建分页栏
		});
	}
	//构建分页栏
	buildPageLabel=function(){
		//构建分页标签
		$scope.pageLabel=[];
		var maxPageNo= $scope.resultMap.totalPages;//得到最后页码
		var firstPage=1;//开始页码
		var lastPage=maxPageNo;//截止页码			
		
		$scope.firstDot=true;//前面有...
		$scope.lastDot=true;//后面有...
		
		if($scope.resultMap.totalPages> 5){  //如果总页数大于5页,显示部分页码		
			if($scope.searchMap.pageNo<=3){//如果当前页小于等于3
				lastPage=5; //前5页
				$scope.firstDot=false;	//前面没...
			}else if( $scope.searchMap.pageNo>=lastPage-2  ){//如果当前页大于等于最大页码-2
				firstPage= maxPageNo-4;	//后5页
				$scope.firstDot=false;	//后面没...
			}else{ //显示当前页为中心的5页
				firstPage=$scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2;			
			}
		}else{
			$scope.firstDot=false;//前面无点
			$scope.lastDot=false;//后边无点

		}	

		//循环产生页码标签
		for(var i=firstPage;i<=lastPage;i++){
			$scope.pageLabel.push(i);
		}
	}

	// 添加搜索项,改变searchMap的值(更改变量来实现)
	$scope.addSearchItem = function(key, value) {
		if (key == 'category' || key == 'brand'||key=='price') {// 如果用户点击的是分类或者品牌
			$scope.searchMap[key] = value;
		} else {// 用户点击的是规格,不确定,所以else方便处理
			$scope.searchMap.spec[key] = value;
		}
		$scope.search();//查询
	}
	
	//撤销搜索项
	$scope.removeSearchItem = function(key){
		if (key == 'category' || key == 'brand'||key=='price') {// 如果用户点击的是分类或者品牌
			$scope.searchMap[key] = "";
		} else {// 用户点击的是规格,不确定,所以else方便处理
			delete $scope.searchMap.spec[key];
		}
		$scope.search();//查询
	}
	//根据页码查询
	$scope.queryByPage=function(pageNo){
		//页码验证
		if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
			return;
		}		
		$scope.searchMap.pageNo=pageNo;			
		$scope.search();
	}
	// 判断当前页是否为第一页
	$scope.isTopPage = function() {
		if ($scope.searchMap.pageNo == 1) {
			return true;
		} else {
			return false;
		}
	}
	// 判断当前页是否为最后一页
	$scope.isEndPage = function() {
		if ($scope.searchMap.pageNo == $scope.resultMap.totalPages) {
			return true;
		} else {
			return false;
		}
	}
	
	//排序查询
	$scope.sortSearch = function(sort,sortField){
		$scope.searchMap.sort = sort ;
		$scope.searchMap.sortField = sortField ;
		
		$scope.search();//查询
	}
	//判断关键字是否是品牌
	$scope.keywordsIsBrand=function(){		
		for(var i=0;i< $scope.resultMap.brandList.length;i++){			
			if( $scope.searchMap.keywords.indexOf( $scope.resultMap.brandList[i].text )>=0  ){
				return true;				
			}			
		}
		return false;
	}
	
	//加载查询字符串
	$scope.loadkeywords=function(){
		$scope.searchMap.keywords=  $location.search()['keywords'];
		$scope.search();
	}
});