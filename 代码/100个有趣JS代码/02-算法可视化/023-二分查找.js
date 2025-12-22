// 🔍 二分查找
// 学习点：搜索算法

function binarySearch(arr, target) {
  let left = 0;
  let right = arr.length - 1;
  let steps = 0;
  
  while (left <= right) {
    steps++;
    const mid = Math.floor((left + right) / 2);
    
    console.log(`第${steps}步: 查找区间[${left}, ${right}], 中间位置${mid}, 值=${arr[mid]}`);
    
    if (arr[mid] === target) {
      console.log(`✅ 找到目标值 ${target}，位置: ${mid}`);
      return mid;
    } else if (arr[mid] < target) {
      left = mid + 1;
    } else {
      right = mid - 1;
    }
  }
  
  console.log('❌ 未找到目标值');
  return -1;
}

const sortedArr = [1, 3, 5, 7, 9, 11, 13, 15, 17, 19];
console.log('数组:', sortedArr);
console.log('\n查找 13:');
binarySearch(sortedArr, 13);

console.log('\n💡 时间复杂度: O(log n)');
console.log('💡 前提条件: 数组必须有序');
