
export function transToConfigDescript(html:string){
	var str = Object.keys(window.$xgj.configDescript).join('|');
	var reg = new RegExp(str, 'g');
	return html.replace(reg,function(val){
		return window.$xgj.configDescript[val]||val;
	});
}

export function fieldAlias (val:string){
    return val.replace('就读年级', function (v) {
        return window.$xgj.fieldAliasObj[v] || v;
    })
}

export function inputFloatFormat(value:string,precision:number){
    // 移除非数字和小数点之外的字符
    value = value.replace(/[^0-9.]/g, '');

    // 根据指定精度截取小数部分
    if (value.includes('.')) {
      let parts:any = value.split('.');
      value = parts[0]*1 + '.' + (parts[1] ? parts[1].slice(0, precision) : '');
    }else{
        value = value==''?'':parseInt(value)+''
    }
   return value
}

export function inputIntFormat(value:string){
    // 移除非数字的字符
    value = value.replace(/[^0-9]/g, '');
    let val=value!=''?parseInt(value):''
    value =val +''
    return value
}

export function toFixedFix(n:any, prec:number) {
    var k = Math.pow(10, prec);
    return '' + (Math.round(n * k) / k).toFixed(prec);
}

// 格式化数字
// @param number 必需，要格式化的数字
// @param decimals 可选，规定多少个小数位，默认两位。
// @param thousands 可选，规定用作千位分隔符的字符串（默认为 , ），如果设置了该参数，那么所有其他参数都是必需的。
export function formatNumber (number:string|number, decimals:number, thousands:string) {
    //https://github.com/txgruppi/number_format
    //form http://phpjs.org/functions/number_format/
    number = (number + '').replace(/[^0-9+\-Ee.]/g, '');
    var n = !isFinite(+number) ? 0 : +number,
        prec = !isFinite(+decimals) ? 2 : Math.abs(decimals),
        sep = typeof thousands === 'string' ? thousands : ",",
        s:any = '';

    // Fix for IE parseFloat(0.55).toFixed(0) = 0;
    s = (prec ? toFixedFix(n, prec) : '' + Math.round(n)).split('.');
    if (s[0].length > 3) {
        s[0] = s[0].replace(/\B(?=(?:\d{3})+(?!\d))/g, sep);
    }
    return s.join('.');
}

/**
 * 将源对象的值赋给目标对象
 * 此函数仅赋值源对象中目标对象已有的属性
 * 
 * @param dest 目标对象，将被赋值
 * @param source 源对象，其属性值将被复制到目标对象
 */
export function form_valueAssignin2(dest:any,source:any){
    const obj:any = {}
	for(var i in source){
		if(dest.hasOwnProperty(i)){
			obj[i]=source[i];
		}
	}
    return obj
}
