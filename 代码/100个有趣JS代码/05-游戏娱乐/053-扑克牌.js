// 🃏 扑克牌洗牌
// 学习点：数组操作与随机化

class PokerDeck {
  constructor() {
    this.suits = ['♠', '♥', '♣', '♦'];
    this.ranks = ['A', '2', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K'];
    this.reset();
  }
  
  reset() {
    this.cards = [];
    this.suits.forEach(suit => {
      this.ranks.forEach(rank => {
        this.cards.push({ suit, rank, display: `${suit}${rank}` });
      });
    });
  }
  
  shuffle() {
    // Fisher-Yates 洗牌算法
    for (let i = this.cards.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [this.cards[i], this.cards[j]] = [this.cards[j], this.cards[i]];
    }
  }
  
  draw(count = 1) {
    return this.cards.splice(0, count);
  }
  
  remaining() {
    return this.cards.length;
  }
}

const deck = new PokerDeck();
console.log('扑克牌总数:', deck.remaining());

deck.shuffle();
console.log('\n洗牌后抽5张:');
const hand = deck.draw(5);
console.log(hand.map(card => card.display).join(' '));

console.log('\n剩余:', deck.remaining(), '张');
