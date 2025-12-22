// 🌳 深度优先搜索 (DFS)
// 学习点：图/树遍历算法

const graph = {
  A: ['B', 'C'],
  B: ['A', 'D', 'E'],
  C: ['A', 'F'],
  D: ['B'],
  E: ['B', 'F'],
  F: ['C', 'E']
};

function dfs(graph, start, visited = new Set()) {
  console.log('访问节点:', start);
  visited.add(start);
  
  for (const neighbor of graph[start]) {
    if (!visited.has(neighbor)) {
      dfs(graph, neighbor, visited);
    }
  }
  
  return visited;
}

console.log('深度优先搜索（从A开始）:');
dfs(graph, 'A');

console.log('\n💡 应用场景: 路径查找、拓扑排序、检测环等');
