app.controller('baseController',function($scope){
	//分页控件配置currentPage:当前页   totalItems :总记录数  itemsPerPage:每页记录数  perPageOptions :分页选项  onChange:当页码变更后自动触发的方法 
	$scope.paginationConf = {
		currentPage: 1,
		totalItems: 10,
		itemsPerPage: 10,
		perPageOptions: [10, 20, 30, 40, 50],
		onChange: function(){
			$scope.reloadList(); 
		}
	};
	//刷新列表
	$scope.reloadList=function(){
		$scope.search( $scope.paginationConf.currentPage ,  $scope.paginationConf.itemsPerPage );
	}
	//用户勾选的id集合
	$scope.selectIds=[];
	//用户勾选复选框
	$scope.updateSeleciton=function($event,id){
		//$event本身是源的意思,加上target传递的是input本身,$event.target.checked表示为框的勾选状态
		if($event.target.checked){
		    //push方法向集合中添加元素
		    $scope.selectIds.push(id);
		}else{
			//下面是两个原生的js方法
			//查找值的位置0-x
			var index = $scope.selectIds.indexOf(id);
			//参数1:移除的位置 参数2:移除的个数
			$scope.selectIds.splice(index,1);
		}
	}
});