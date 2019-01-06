app.controller('payController' ,function($scope ,$location,payService){
	
	$scope.createNative=function(){
		payService.createNative().success(
				function(response){
					
					//显示订单号和金额
					$scope.money = (response.totalfee/100).toFixed(2);
					$scope.out_trade_no=response.out_trade_no;
					
					//生成二维码
					var qr = new QRious({
						element:document.getElementById('qrious'),
						size:200,
						value:response.code_url,
						level:'H'
					});		
					queryPayStatus(out_trade_no);//调用查询
				}
		);
	}
	
	//查询支付状态 
	queryPayStatus=function(out_trade_no){
		payService.queryPayStatus(out_trade_no).success(
			function(response){
				if(response.success){
					location.href="paysuccess.html#?money="+$scope.money;
				}else{					
					if(response.msg=="二维码超时"){
						$scope.createNative();//重新生成二维码
					}else{
						location.href="payfail.html";								
					}
				}				
			}
		);
	}
	
	//获取订单金额
	$scope.getMoney=function(){
		return $location.search()['money'];
	}
});