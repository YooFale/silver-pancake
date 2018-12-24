app.service('uploadService', function($http) {
	// 上传文件
	this.uploadFile = function() {
		var formdata = new FormData();
		// 文件上传框的name
		formdata.append('file', file.files[0]);
		return $http({
			url : '../upload.do',
			method : 'post',
			data : formdata,
			// 制定上传类型
			headers :{'Content-Type':undefined},

			transformRequest : angular.identity
		});
	}
});