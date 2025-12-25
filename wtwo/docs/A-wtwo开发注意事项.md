## Wtwo 
1. 子应用往主应用发信息，window.microApp.dispatch({type:'scheduleManage:excelImportCourse'}) 
    - 连续多次重复的话，主应用不会接收后续信息,想要每次生效的话，需要保证每次发过去的信息都不会重复
    - 示例 {type:'scheduleManage:excelImportCourse',time:Date.now()},补充个时间戳

2. 表格排课ElInput删除事件被吞

3. 动画性能优化will-change

4. 主应用中给子应用标签传的属性好像不会每次更新，需要每次setData传一下
    ```html 
    <micro-app name="studentScoreQuery" url="/wtwo/index.html" baseroute="/" iframe style="width: 100%;min-height:710px" 
    router-mode='pure' ms-attr-data="{loginInfo:loginInfo.$model,studentInfo:studentInfo.$model}"></micro-app>
    ```
    ```javascript
    window.microApp.default.setData('studentScoreQuery', {
        type: 'app:init',
        loginInfo: vmodel.loginInfo,
        studentInfo: vmodel.studentInfo // 如果用的时候这里没传，可能还是上次的
    }) 
    ```

5. 主应用中的$在子应用打开过后，会和子应用污染（目前没有从框架层面隔绝）
    ```javascript
    // 必要时，改为document使用，例如：
    document.getElementById('woneBody');
    document.body;
    ```