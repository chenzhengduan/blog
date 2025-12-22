// 🎮 贪食蛇AI（简单版）
// 学习点：路径查找算法

class SnakeAI {
  constructor(grid) {
    this.grid = grid; // 网格大小
  }
  
  // 曼哈顿距离
  manhattan(pos1, pos2) {
    return Math.abs(pos1.x - pos2.x) + Math.abs(pos1.y - pos2.y);
  }
  
  // A*寻路
  findPath(snake, food) {
    const head = snake[0];
    const openSet = [{ pos: head, g: 0, h: this.manhattan(head, food), f: 0, path: [] }];
    const closedSet = new Set();
    
    while (openSet.length > 0) {
      // 找到f值最小的节点
      openSet.sort((a, b) => a.f - b.f);
      const current = openSet.shift();
      
      // 到达食物
      if (current.pos.x === food.x && current.pos.y === food.y) {
        return current.path;
      }
      
      closedSet.add(`${current.pos.x},${current.pos.y}`);
      
      // 探索邻居
      const neighbors = [
        { x: current.pos.x + 1, y: current.pos.y, dir: 'right' },
        { x: current.pos.x - 1, y: current.pos.y, dir: 'left' },
        { x: current.pos.x, y: current.pos.y + 1, dir: 'down' },
        { x: current.pos.x, y: current.pos.y - 1, dir: 'up' }
      ];
      
      for (const neighbor of neighbors) {
        // 检查边界和蛇身
        if (neighbor.x < 0 || neighbor.x >= this.grid ||
            neighbor.y < 0 || neighbor.y >= this.grid ||
            closedSet.has(`${neighbor.x},${neighbor.y}`) ||
            snake.some(s => s.x === neighbor.x && s.y === neighbor.y)) {
          continue;
        }
        
        const g = current.g + 1;
        const h = this.manhattan(neighbor, food);
        const f = g + h;
        
        const existing = openSet.find(n => n.pos.x === neighbor.x && n.pos.y === neighbor.y);
        if (!existing || g < existing.g) {
          const node = {
            pos: { x: neighbor.x, y: neighbor.y },
            g, h, f,
            path: [...current.path, neighbor.dir]
          };
          
          if (existing) {
            openSet[openSet.indexOf(existing)] = node;
          } else {
            openSet.push(node);
          }
        }
      }
    }
    
    return null; // 无路径
  }
  
  getNextMove(snake, food) {
    const path = this.findPath(snake, food);
    return path ? path[0] : null;
  }
}

// 演示
const ai = new SnakeAI(10);
const snake = [{ x: 5, y: 5 }, { x: 5, y: 6 }];
const food = { x: 8, y: 3 };

console.log('贪食蛇AI');
console.log('蛇头位置:', snake[0]);
console.log('食物位置:', food);

const move = ai.getNextMove(snake, food);
console.log('建议移动:', move);

const path = ai.findPath(snake, food);
console.log('完整路径:', path);
