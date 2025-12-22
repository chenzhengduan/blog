// 🌊 广度优先搜索 (BFS)
// 学习点：图/树遍历算法

const graph = {
  A: ['B', 'C'],
  B: ['A', 'D', 'E'],
  C: ['A', 'F'],
  D: ['B'],
  E: ['B', 'F'],
  F: ['C', 'E']
};

function bfs(graph, start) {
  const visited = new Set([start]);
  const queue = [start];
  
  console.log('访问顺序:');
  
  while (queue.length > 0) {
    const node = queue.shift();
    console.log('访问节点:', node);
    
    for (const neighbor of graph[node]) {
      if (!visited.has(neighbor)) {
        visited.add(neighbor);
        queue.push(neighbor);
      }
    }
  }
  
  return visited;
}

console.log('广度优先搜索（从A开始）:');
bfs(graph, 'A');

console.log('\n💡 应用场景: 最短路径、层序遍历等');
