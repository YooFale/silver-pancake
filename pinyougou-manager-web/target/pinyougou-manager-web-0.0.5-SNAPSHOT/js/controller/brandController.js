app.controller('brandController',function($scope,brandService){
			//查询品牌列表
			$scope.findAll=function(){
				brandService.findAll().success(
					function(response){
						$scope.list=response;
					}		
				);				
			}
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
			//分页 
			$scope.findPage=function(page,size){
				brandService.findPage(page,size).success(
					function(response){
						$scope.list=response.rows;//显示当前页数据 	
						$scope.paginationConf.totalItems=response.total;//更新总记录数 
					}		
				);				
			}
			//新增
			$scope.save=function(){
				//设定方法名
				var object = null;
				if($scope.entity.id!=null){
					object=brandService.update($scope.entity);
				}else{
					object=brandService.add($scope.entity);
				}
				//进行拼接
				object.success(
				function(response){
					if(response.success){
						//成功刷新页面
						$scope.reloadList();
					}else{
						//失败提示错误信息
						alert(response.msg)
					}
				}		
				)
			}
			//修改前查询实体对象
			$scope.findOne=function(id){
				brandService.findOne(id).success(
				function(response){
					$scope.entity=response;
				}		
			  );
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
			//删除,delete是关键字,避免使用
			$scope.dele=function(){
				brandService.dele($scope.selectIds).success(
				function(response){
					if(response.success){
						$scope.reloadList();
					}else{
						alert(response.message);
					}
				}		
				)
			}
			//主动初始化,不然他可能是null,没有类型
			$scope.searchEntity={};
			//条件查询
			$scope.search=function(page,size){
				//和上面的分页查询类似,但是有requestbody注解,必然是post
				brandService.search(page,size,$scope.searchEntity).success(
						function(response){
							$scope.list=response.rows;//显示当前页数据 	
							$scope.paginationConf.totalItems=response.total;//更新总记录数 
						}		
					);		
			}
		});