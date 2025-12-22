// 🎲 扫雷游戏逻辑
// 学习点：二维数组与游戏逻辑

class Minesweeper {
  constructor(rows = 10, cols = 10, mines = 10) {
    this.rows = rows;
    this.cols = cols;
    this.mineCount = mines;
    this.board = [];
    this.revealed = [];
    this.flags = [];
    this.gameOver = false;
    this.init();
  }
  
  init() {
    // 初始化棋盘
    for (let i = 0; i < this.rows; i++) {
      this.board[i] = [];
      this.revealed[i] = [];
      this.flags[i] = [];
      for (let j = 0; j < this.cols; j++) {
        this.board[i][j] = 0;
        this.revealed[i][j] = false;
        this.flags[i][j] = false;
      }
    }
    
    // 放置地雷
    let placed = 0;
    while (placed < this.mineCount) {
      const row = Math.floor(Math.random() * this.rows);
      const col = Math.floor(Math.random() * this.cols);
      
      if (this.board[row][col] !== -1) {
        this.board[row][col] = -1;
        placed++;
      }
    }
    
    // 计算数字
    for (let i = 0; i < this.rows; i++) {
      for (let j = 0; j < this.cols; j++) {
        if (this.board[i][j] !== -1) {
          this.board[i][j] = this.countAdjacentMines(i, j);
        }
      }
    }
  }
  
  countAdjacentMines(row, col) {
    let count = 0;
    for (let i = -1; i <= 1; i++) {
      for (let j = -1; j <= 1; j++) {
        const r = row + i;
        const c = col + j;
        if (r >= 0 && r < this.rows && c >= 0 && c < this.cols) {
          if (this.board[r][c] === -1) count++;
        }
      }
    }
    return count;
  }
  
  reveal(row, col) {
    if (this.gameOver || this.revealed[row][col] || this.flags[row][col]) {
      return false;
    }
    
    this.revealed[row][col] = true;
    
    // 踩到地雷
    if (this.board[row][col] === -1) {
      this.gameOver = true;
      return { gameOver: true, result: 'lose' };
    }
    
    // 自动展开空白区域
    if (this.board[row][col] === 0) {
      for (let i = -1; i <= 1; i++) {
        for (let j = -1; j <= 1; j++) {
          const r = row + i;
          const c = col + j;
          if (r >= 0 && r < this.rows && c >= 0 && c < this.cols && !this.revealed[r][c]) {
            this.reveal(r, c);
          }
        }
      }
    }
    
    // 检查胜利
    if (this.checkWin()) {
      this.gameOver = true;
      return { gameOver: true, result: 'win' };
    }
    
    return { value: this.board[row][col] };
  }
  
  toggleFlag(row, col) {
    if (!this.revealed[row][col]) {
      this.flags[row][col] = !this.flags[row][col];
    }
  }
  
  checkWin() {
    for (let i = 0; i < this.rows; i++) {
      for (let j = 0; j < this.cols; j++) {
        if (this.board[i][j] !== -1 && !this.revealed[i][j]) {
          return false;
        }
      }
    }
    return true;
  }
  
  display() {
    console.log('扫雷游戏 (-1=地雷)');
    this.board.forEach((row, i) => {
      console.log(row.map((cell, j) => 
        this.revealed[i][j] ? (cell === -1 ? '💣' : cell || '·') : 
        this.flags[i][j] ? '🚩' : '□'
      ).join(' '));
    });
  }
}

const game = new Minesweeper(8, 8, 10);
console.log('扫雷游戏初始化完成\n');
game.display();

console.log('\n点击(0,0):');
game.reveal(0, 0);
game.display();
