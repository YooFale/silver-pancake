//购物车控制层
app.controller('cartController',function($scope,cartService){
	
	
	//查询购物车列表
	$scope.findCartList=function(){
		cartService.findCartList().success(
			function(response){
				$scope.cartList=response;
				$scope.totalValue=cartService.sum($scope.cartList);//求合计数
			}
		);
	}
	//向购物车中添加商品详细
	$scope.addGoodsToCartList=function(itemId,num){
		cartService.addGoodsToCartList(itemId,num).success(
				function(response){
					if(response.success){//如果成功
						$scope.findCartList();//刷新列表
					}else{
						alert(response.msg);
					}
				}
		);
	}
	//获取当前用户的地址列表
	$scope.findAddressList=function(){
		cartService.findAddressList().success(
		function(response){
			$scope.addressList = response;
			//实现默认列表打开就是选中的
			for(var i=0;i<$scope.addressList.length;i++){
				if($scope.addressList[i].isDefault=="1"){
					$scope.address = $scope.addressList[i];
					break;
				}
			}
			
		}		
		);
	}
	//选择地址
	$scope.selectAddress=function(address){
		$scope.address = address;
	}
	//判断是否是选择的地址
	$scope.isSeletedAddress =function(address){
		if(address==$scope.address){
			return true;
		}else{
			return false;
		}
	}
	
	$scope.order={paymentType:'1'};//订单对象
	
	//选择支付方式
	$scope.selectPayType=function(type){
		$scope.order.paymentType=type;
	}
	
	//保存订单
	$scope.submitOrder=function(){
		$scope.order.ReceiverAreaName=$scope.address.address;//地址
		$scope.order.ReceiverMobile=$scope.address.mobile;//手机
		$scope.order.Receiver=$scope.address.contact;//联系人
		
		cartService.submitOrder($scope.order).success(
				function(response){
//					alert(response.msg);
					if(response.success){
						//页面跳转
						if($scope.order.paymentType="1"){//是微信
							location.href="pay.html";
						}else{//是货到付款,跳转到提示页面
							location.href="paysuccess.html";
						}
					}else{
						alert(response.msg); //订单提交失败也可以跳到提示页面
					}
				}		
		)
	}
});