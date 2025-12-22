// 🐍 贪吃蛇游戏逻辑
// 学习点：游戏状态管理

class SnakeGame {
  constructor(width = 20, height = 20) {
    this.width = width;
    this.height = height;
    this.snake = [{ x: 10, y: 10 }];
    this.direction = { x: 1, y: 0 };
    this.food = this.generateFood();
    this.score = 0;
    this.gameOver = false;
  }
  
  generateFood() {
    let food;
    do {
      food = {
        x: Math.floor(Math.random() * this.width),
        y: Math.floor(Math.random() * this.height)
      };
    } while (this.snake.some(s => s.x === food.x && s.y === food.y));
    return food;
  }
  
  setDirection(direction) {
    // 防止反向移动
    if (Math.abs(direction.x) === Math.abs(this.direction.x)) return;
    this.direction = direction;
  }
  
  update() {
    if (this.gameOver) return;
    
    const head = {
      x: this.snake[0].x + this.direction.x,
      y: this.snake[0].y + this.direction.y
    };
    
    // 检查碰撞
    if (
      head.x < 0 || head.x >= this.width ||
      head.y < 0 || head.y >= this.height ||
      this.snake.some(s => s.x === head.x && s.y === head.y)
    ) {
      this.gameOver = true;
      return { gameOver: true, score: this.score };
    }
    
    this.snake.unshift(head);
    
    // 检查是否吃到食物
    if (head.x === this.food.x && head.y === this.food.y) {
      this.score += 10;
      this.food = this.generateFood();
    } else {
      this.snake.pop();
    }
    
    return { score: this.score, length: this.snake.length };
  }
}

const snake = new SnakeGame(10, 10);
console.log('贪吃蛇游戏初始化');
console.log('方向控制: setDirection({x:1,y:0}) 等');
console.log('更新游戏: update()');

for (let i = 0; i < 5; i++) {
  const result = snake.update();
  console.log(`步骤${i + 1}:`, result);
}
