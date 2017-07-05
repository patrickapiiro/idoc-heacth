/**
 * Created by Administrator on 2017/7/5.
 */

var htmlCtrl = documentApp.controller("htmlCtrl",['$scope','$compile','$http',function($scope,$compile,$http){

    $scope.setValue=function(){
        alert("nima")
        console.log(htmlObject.htmlResult);
    }


}])