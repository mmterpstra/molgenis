(function($, w) {
	"use strict";
	var molgenis = w.molgenis = w.molgenis || {};
	
	molgenis.Pagination = function Pagination(){
		this.offSet = 1;
		this.currentPage = 1;
		this.pager = 10;
		this.totalPage = 0;
	};
	
	molgenis.Pagination.prototype.reset = function(){
		this.offSet = 1;
		this.currentPage = 1;
	};
	
	molgenis.Pagination.prototype.setTotalPage = function(totalPage){
		this.totalPage = totalPage;
	};
	
	molgenis.Pagination.prototype.getPager = function(){
		return this.pager;
	};
	
	molgenis.Pagination.prototype.updateMatrixPagination = function(pageElement, callback) {
		if(this.totalPage !== 0){
			pageElement.empty();
			pageElement.append('<li><a href="#">Prev</a></li>');
			var displayedPage = (this.totalPage < 10 ? this.totalPage : 9) + this.offSet; 
			for(var i = this.offSet; i <= displayedPage ; i++){
				var element = $('<li />');
				if(i == this.currentPage)
					element.addClass('active');
				element.append('<a href="/">' + i + '</a>');
				pageElement.append(element);
			}
			var lastPage = this.totalPage + 1 > 10 ? this.totalPage + 1 : 10;
			if(this.totalPage - this.offSet > 9){
				pageElement.append('<li class="active"><a href="#">...</a></li>');
				pageElement.append('<li><a href="#">' + lastPage + ' </a></li>');
			}
			pageElement.append('<li><a href="#">Next</a></li>');
			var pagination = this;
			pageElement.find('li').each(function(){
				$(this).click($.proxy(function(){
					var pageNumber = this.clickElement.find('a').html();
					if(pageNumber === "Prev"){
						if(this.data.currentPage > this.data.offSet) this.data.currentPage--;
						else if(this.data.offSet > 1) {
							this.data.offSet--;
							this.data.currentPage--;	
						}
					}else if(pageNumber === "Next"){
						if(this.data.currentPage <= this.data.totalPage) {
							this.data.currentPage++;
							if(this.data.currentPage >= this.data.offSet + 9) this.data.offSet++;
						}
					}else if(pageNumber !== "..."){
						this.data.currentPage = parseInt(pageNumber);
						if(this.data.currentPage > this.data.offSet + 9){
							this.data.offSet = this.data.currentPage - 9;
						} 
					}
					callback();
					return false;
				},{'clickElement' : $(this), 'data' : pagination}));
			});
		}
	};
	
	molgenis.Pagination.prototype.createSearchRequest = function (selectedDataSet) {
		var queryRules = [];
		//todo: how to unlimit the search result
		queryRules.push({
			operator : 'LIMIT',
			value : this.pager
		});
		queryRules.push({
			operator : 'OFFSET',
			value : (this.currentPage - 1) * this.pager
		});
		queryRules.push({
			operator : 'SEARCH',
			value : 'observablefeature'
		});
		var searchRequest = {
			documentType : 'protocolTree-' + selectedDataSet,
			queryRules : queryRules
		};
		return searchRequest;
	};
	
}($, window.top));