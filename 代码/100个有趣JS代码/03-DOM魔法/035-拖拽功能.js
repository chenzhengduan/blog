// 🖱️ 拖拽功能实现
// 学习点：鼠标事件处理

function makeDraggable(element) {
  let pos1 = 0, pos2 = 0, pos3 = 0, pos4 = 0;
  
  element.onmousedown = dragMouseDown;
  
  function dragMouseDown(e) {
    e.preventDefault();
    pos3 = e.clientX;
    pos4 = e.clientY;
    
    document.onmouseup = closeDragElement;
    document.onmousemove = elementDrag;
  }
  
  function elementDrag(e) {
    e.preventDefault();
    pos1 = pos3 - e.clientX;
    pos2 = pos4 - e.clientY;
    pos3 = e.clientX;
    pos4 = e.clientY;
    
    element.style.top = (element.offsetTop - pos2) + 'px';
    element.style.left = (element.offsetLeft - pos1) + 'px';
  }
  
  function closeDragElement() {
    document.onmouseup = null;
    document.onmousemove = null;
  }
}

console.log('拖拽函数已定义');
console.log('使用方法:');
console.log(`
const box = document.createElement('div');
box.style.cssText = 'position: absolute; width: 100px; height: 100px; background: blue;';
document.body.appendChild(box);
makeDraggable(box);
`);
