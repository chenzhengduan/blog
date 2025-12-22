// ⭕ 井字棋游戏
// 学习点：游戏逻辑判断

class TicTacToe {
  constructor() {
    this.board = Array(9).fill(null);
    this.currentPlayer = 'X';
    this.winner = null;
  }
  
  move(position) {
    if (this.board[position] || this.winner) {
      return { success: false, message: '无效移动' };
    }
    
    this.board[position] = this.currentPlayer;
    
    if (this.checkWinner()) {
      this.winner = this.currentPlayer;
      return { success: true, winner: this.winner, message: `${this.winner} 获胜！` };
    }
    
    if (!this.board.includes(null)) {
      return { success: true, draw: true, message: '平局！' };
    }
    
    this.currentPlayer = this.currentPlayer === 'X' ? 'O' : 'X';
    return { success: true, nextPlayer: this.currentPlayer };
  }
  
  checkWinner() {
    const lines = [
      [0, 1, 2], [3, 4, 5], [6, 7, 8],  // 行
      [0, 3, 6], [1, 4, 7], [2, 5, 8],  // 列
      [0, 4, 8], [2, 4, 6]              // 对角线
    ];
    
    return lines.some(([a, b, c]) => 
      this.board[a] && 
      this.board[a] === this.board[b] && 
      this.board[a] === this.board[c]
    );
  }
  
  display() {
    const display = this.board.map(cell => cell || '-');
    console.log(`
  ${display[0]} | ${display[1]} | ${display[2]}
  ---------
  ${display[3]} | ${display[4]} | ${display[5]}
  ---------
  ${display[6]} | ${display[7]} | ${display[8]}
    `);
  }
}

const game = new TicTacToe();
console.log('井字棋游戏开始！');
game.display();

game.move(0);  // X
game.move(1);  // O
game.move(4);  // X
game.move(2);  // O
const result = game.move(8);  // X
game.display();
console.log(result.message);
