// 🔍 模糊搜索实现
// 学习点：字符串匹配算法

function fuzzySearch(pattern, text) {
  pattern = pattern.toLowerCase();
  text = text.toLowerCase();
  
  let patternIdx = 0;
  let textIdx = 0;
  const matches = [];
  
  while (patternIdx < pattern.length && textIdx < text.length) {
    if (pattern[patternIdx] === text[textIdx]) {
      matches.push(textIdx);
      patternIdx++;
    }
    textIdx++;
  }
  
  // 检查是否所有字符都匹配
  if (patternIdx === pattern.length) {
    return {
      matched: true,
      score: matches.length / text.length,
      matches
    };
  }
  
  return { matched: false };
}

function fuzzyFilter(items, query, key = null) {
  return items
    .map(item => {
      const text = key ? item[key] : item;
      const result = fuzzySearch(query, text);
      return { item, ...result };
    })
    .filter(r => r.matched)
    .sort((a, b) => b.score - a.score)
    .map(r => r.item);
}

const files = [
  'README.md',
  'package.json',
  'src/index.js',
  'src/components/Header.jsx',
  'src/utils/helper.js',
  'tests/unit.test.js'
];

console.log('模糊搜索演示:\n');
console.log('搜索 "rcm":', fuzzyFilter(files, 'rcm'));
console.log('搜索 "sjs":', fuzzyFilter(files, 'sjs'));
console.log('搜索 "test":', fuzzyFilter(files, 'test'));
