// 🧩 拼图游戏逻辑
// 学习点：数组操作与游戏状态

class PuzzleGame {
  constructor(size = 3) {
    this.size = size;
    this.tiles = [];
    this.emptyPos = { row: size - 1, col: size - 1 };
    this.moves = 0;
    this.init();
  }
  
  init() {
    // 初始化有序状态
    let num = 1;
    for (let i = 0; i < this.size; i++) {
      this.tiles[i] = [];
      for (let j = 0; j < this.size; j++) {
        this.tiles[i][j] = num++;
      }
    }
    this.tiles[this.size - 1][this.size - 1] = 0; // 空格
    this.shuffle();
  }
  
  shuffle() {
    // 随机移动100次
    for (let i = 0; i < 100; i++) {
      const moves = this.getPossibleMoves();
      const randomMove = moves[Math.floor(Math.random() * moves.length)];
      this.move(randomMove.row, randomMove.col, false);
    }
    this.moves = 0;
  }
  
  getPossibleMoves() {
    const moves = [];
    const { row, col } = this.emptyPos;
    const directions = [
      { row: row - 1, col },
      { row: row + 1, col },
      { row, col: col - 1 },
      { row, col: col + 1 }
    ];
    
    directions.forEach(pos => {
      if (pos.row >= 0 && pos.row < this.size && 
          pos.col >= 0 && pos.col < this.size) {
        moves.push(pos);
      }
    });
    
    return moves;
  }
  
  move(row, col, countMove = true) {
    const { row: emptyRow, col: emptyCol } = this.emptyPos;
    
    // 检查是否相邻
    const isAdjacent = 
      (Math.abs(row - emptyRow) === 1 && col === emptyCol) ||
      (Math.abs(col - emptyCol) === 1 && row === emptyRow);
    
    if (!isAdjacent) return false;
    
    // 交换
    this.tiles[emptyRow][emptyCol] = this.tiles[row][col];
    this.tiles[row][col] = 0;
    this.emptyPos = { row, col };
    
    if (countMove) this.moves++;
    
    return true;
  }
  
  isSolved() {
    let num = 1;
    for (let i = 0; i < this.size; i++) {
      for (let j = 0; j < this.size; j++) {
        if (i === this.size - 1 && j === this.size - 1) {
          return this.tiles[i][j] === 0;
        }
        if (this.tiles[i][j] !== num++) {
          return false;
        }
      }
    }
    return true;
  }
  
  display() {
    console.log('移动次数:', this.moves);
    this.tiles.forEach(row => {
      console.log(row.map(n => n || ' ').join(' | '));
    });
  }
}

const puzzle = new PuzzleGame(3);
console.log('拼图游戏 (3x3)\n');
puzzle.display();

console.log('\n移动 (1,1):');
puzzle.move(1, 2);
puzzle.display();

console.log('\n是否完成:', puzzle.isSolved());
