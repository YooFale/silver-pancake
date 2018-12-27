// 定义模块:
var app = angular.module("pinyougou",[]);
//定义过滤器(前面的$sce有''是因为要加载组件)
app.filter('trustHtml',['$sce',function($sce){
	return function(data){
		//传入参数是被过滤的内容,返回的是过滤后的内容(信任html的转换)
		return $sce.trustAsHtml(data);
	}
}]);