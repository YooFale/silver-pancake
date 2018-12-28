app.controller('contentController',function($scope,contentService){
	
	$scope.contentList=[];//广告列表
	//根据分类id查询广告的方法
	$scope.findByCategoryId=function(categoryId){
		contentService.findByCategoryId(categoryId).success(
			function(response){
				$scope.contentList[categoryId]=response;
			}
		);		
	}
	
	//搜索(传递参数)首页跳转到搜索页面
	$scope.search = function(){
		location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
	}
	
});