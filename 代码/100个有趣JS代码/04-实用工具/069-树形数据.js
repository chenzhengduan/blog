// 🌳 树形数据处理
// 学习点：递归与树结构

const treeUtils = {
  // 扁平数组转树形结构
  arrayToTree(items, parentId = null, idKey = 'id', parentKey = 'parentId') {
    return items
      .filter(item => item[parentKey] === parentId)
      .map(item => ({
        ...item,
        children: this.arrayToTree(items, item[idKey], idKey, parentKey)
      }));
  },
  
  // 树形结构转扁平数组
  treeToArray(tree, childrenKey = 'children') {
    const result = [];
    
    const traverse = (nodes, parent = null) => {
      nodes.forEach(node => {
        const { [childrenKey]: children, ...rest } = node;
        result.push({ ...rest, parent });
        
        if (children && children.length) {
          traverse(children, node.id);
        }
      });
    };
    
    traverse(tree);
    return result;
  },
  
  // 查找节点
  findNode(tree, predicate, childrenKey = 'children') {
    for (const node of tree) {
      if (predicate(node)) return node;
      
      if (node[childrenKey]) {
        const found = this.findNode(node[childrenKey], predicate, childrenKey);
        if (found) return found;
      }
    }
    return null;
  },
  
  // 获取所有叶子节点
  getLeaves(tree, childrenKey = 'children') {
    const leaves = [];
    
    const traverse = (nodes) => {
      nodes.forEach(node => {
        if (!node[childrenKey] || node[childrenKey].length === 0) {
          leaves.push(node);
        } else {
          traverse(node[childrenKey]);
        }
      });
    };
    
    traverse(tree);
    return leaves;
  }
};

const flatData = [
  { id: 1, name: '总公司', parentId: null },
  { id: 2, name: '研发部', parentId: 1 },
  { id: 3, name: '市场部', parentId: 1 },
  { id: 4, name: '前端组', parentId: 2 },
  { id: 5, name: '后端组', parentId: 2 }
];

console.log('扁平数据:');
console.log(flatData);

console.log('\n转换为树形结构:');
const tree = treeUtils.arrayToTree(flatData);
console.log(JSON.stringify(tree, null, 2));

console.log('\n查找节点(name=前端组):');
console.log(treeUtils.findNode(tree, node => node.name === '前端组'));

console.log('\n叶子节点:');
console.log(treeUtils.getLeaves(tree).map(n => n.name));
