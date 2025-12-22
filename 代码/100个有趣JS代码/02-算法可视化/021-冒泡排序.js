// 🔍 冒泡排序可视化
// 学习点：基础排序算法

function bubbleSort(arr) {
  const array = [...arr];
  const steps = [];
  
  for (let i = 0; i < array.length - 1; i++) {
    for (let j = 0; j < array.length - 1 - i; j++) {
      steps.push({
        array: [...array],
        comparing: [j, j + 1],
        sorted: Array.from({length: i}, (_, k) => array.length - 1 - k)
      });
      
      if (array[j] > array[j + 1]) {
        [array[j], array[j + 1]] = [array[j + 1], array[j]];
        steps.push({
          array: [...array],
          swapped: [j, j + 1],
          sorted: Array.from({length: i}, (_, k) => array.length - 1 - k)
        });
      }
    }
  }
  
  return { sorted: array, steps };
}

const arr = [64, 34, 25, 12, 22, 11, 90];
console.log('原数组:', arr);
const result = bubbleSort(arr);
console.log('排序后:', result.sorted);
console.log('步骤数:', result.steps.length);
console.log('💡 时间复杂度: O(n²)');
