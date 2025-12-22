// 🔗 并查集 (Union-Find)
// 学习点：数据结构与算法

class UnionFind {
  constructor(size) {
    this.parent = Array.from({ length: size }, (_, i) => i);
    this.rank = Array(size).fill(0);
    this.count = size; // 连通分量数量
  }
  
  // 查找根节点（路径压缩）
  find(x) {
    if (this.parent[x] !== x) {
      this.parent[x] = this.find(this.parent[x]);
    }
    return this.parent[x];
  }
  
  // 合并两个集合（按秩合并）
  union(x, y) {
    const rootX = this.find(x);
    const rootY = this.find(y);
    
    if (rootX === rootY) return false;
    
    if (this.rank[rootX] < this.rank[rootY]) {
      this.parent[rootX] = rootY;
    } else if (this.rank[rootX] > this.rank[rootY]) {
      this.parent[rootY] = rootX;
    } else {
      this.parent[rootY] = rootX;
      this.rank[rootX]++;
    }
    
    this.count--;
    return true;
  }
  
  // 判断是否连通
  isConnected(x, y) {
    return this.find(x) === this.find(y);
  }
  
  // 获取连通分量数量
  getCount() {
    return this.count;
  }
}

// 示例：朋友圈问题
console.log('并查集演示：朋友圈问题\n');

const n = 5; // 5个人
const uf = new UnionFind(n);

console.log('初始状态: 5个独立的人');
console.log('连通分量数:', uf.getCount());

// 添加关系
const friendships = [
  [0, 1], // 0和1是朋友
  [1, 2], // 1和2是朋友
  [3, 4]  // 3和4是朋友
];

friendships.forEach(([a, b]) => {
  uf.union(a, b);
  console.log(`${a}和${b}成为朋友`);
});

console.log('\n最终连通分量数:', uf.getCount());
console.log('0和2是否在同一朋友圈?', uf.isConnected(0, 2));
console.log('0和3是否在同一朋友圈?', uf.isConnected(0, 3));

console.log('\n💡 应用: 社交网络、图论、最小生成树等');
