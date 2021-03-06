app.controller('brandController',function($scope,brandService,$controller){
	
	$controller('baseController',{$scope:$scope});
	
			//查询品牌列表
			$scope.findAll=function(){
				brandService.findAll().success(
					function(response){
						$scope.list=response;
					}		
				);				
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