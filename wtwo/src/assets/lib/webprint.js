;(function(gl){
	var iframe = document.createElement('iframe');
	iframe.src = 'about:blank';
	iframe.style.position = 'absolute';
	iframe.style.width = '0';
	iframe.style.height = '0';
	iframe.style.left = '0';
	iframe.style.top = '0';
	document.body.appendChild(iframe);
	window.___$iframe = iframe;
	var win = iframe.contentWindow;
	
	var style_portrait="<style type='text/css'>\n"
		+"@page{margin:0;}\n"
		+"body{margin:0;padding: 0;background:white !important;color:#000 !important;}\n"
		+"@media print {.non-printing {display: none;}}\n"
		+"</style>";
	var style_landspace="<style type='text/css'>\n"
		+"@page{size:landspace;margin:0;}\n"
		+"body{margin:0;padding: 0;background:white !important;color:#000 !important;}\n"
		+"@media print {.non-printing {display: none;}}\n"
		+"</style>";
	var padding=navigator.userAgent.indexOf('Firefox') === -1?"0.3cm":"0";
	var stamp=new Date().getTime();
	/**
	 * @description 打印html文本，默认的边距为0;支持多个内容同时打印多份。
	 * @param {Array} prints 对象数组（content内容，times份数，margin份数）。
	 * @param {Array} cssLinks 外联样式链接 ["css/print.css"]。
	 * @example 
	 * htmls=[{content:"",times:2,margin:"1cm"}]
	 * cssLinks=["css/print.css"]
	 */
	gl.webPrint= function(prints,cssLinks,isLandpace, isPage, unAutoPrint){
		var doc=win.document;
		doc.write(isLandpace?style_landspace:style_portrait);
		if(cssLinks){
			var links='';
			cssLinks.forEach(function(item){
				links+="<link type='text/css' rel='stylesheet' href='" + item +"?_="+stamp+ "' />";
			});
			doc.write(links);	
		};
		var html="";
		prints.forEach(function(p){
			var div="<div "+(isPage?"class='print-class'" : "") + "style='page-break-after:always;padding:"+(p.margin||0)+"'>";
			div+="<div style='padding:"+padding+";'";
			div+=(">"+p.content+"</div></div>");
			html+=div.repeat(p.times||1);
		});
		doc.write(html);
		if (!unAutoPrint) {
			doc.close();
			setTimeout(function(){
				win.print();
			},1000);
		}else {
			iframe.style.minWidth = "750px";
			iframe.style.height = 'auto';
			iframe.style.background = "wheat";
			setTimeout(function() {
				iframe.style.minWidth = "0";
				doc.close();
			}, 1000);
		}
		return doc;
	}
	gl.webPrint.debug=function(flag){
		if(!flag) {
			iframe.style.position = "absolute";
			iframe.style.width = "0";
			iframe.style.height = "0";
			iframe.style.left = "0";
			iframe.style.top = "0";
		}else{
			iframe.style.position = "absolute";
			iframe.style.width = "750px";
			iframe.style.height = "750px";
			iframe.style.left = "0";
			iframe.style.top = "0";
			iframe.style.zIndex = "9999";
			iframe.style.background = "wheat";
		}
	}
}(window));
