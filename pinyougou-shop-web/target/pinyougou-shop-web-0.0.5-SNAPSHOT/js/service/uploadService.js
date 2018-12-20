app.service('uploadService',function($http){
//	上传文件
this.uploadFile=function(){
	var formdata = new formData();
	//文件上传框的name
	fromdata.appent('file',file.file[0]);
	return $http({
		url:'../upload.do',
		method:'post',
		data:formdata,
//		制定上传类型
		headers:{'Content-Type':undefiend},
		transformRequest:angular.identity
	});
}
});