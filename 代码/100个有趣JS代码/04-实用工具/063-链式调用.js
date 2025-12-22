// 🔗 链式调用封装
// 学习点：方法链与this

class QueryBuilder {
  constructor(data) {
    this.data = data;
    this.result = data;
  }
  
  where(predicate) {
    this.result = this.result.filter(predicate);
    return this;
  }
  
  select(...fields) {
    this.result = this.result.map(item => {
      const selected = {};
      fields.forEach(field => {
        if (item.hasOwnProperty(field)) {
          selected[field] = item[field];
        }
      });
      return selected;
    });
    return this;
  }
  
  orderBy(field, order = 'asc') {
    this.result.sort((a, b) => {
      if (order === 'asc') {
        return a[field] > b[field] ? 1 : -1;
      }
      return a[field] < b[field] ? 1 : -1;
    });
    return this;
  }
  
  limit(count) {
    this.result = this.result.slice(0, count);
    return this;
  }
  
  get() {
    return this.result;
  }
  
  reset() {
    this.result = this.data;
    return this;
  }
}

const users = [
  { id: 1, name: 'Alice', age: 25, city: 'Beijing' },
  { id: 2, name: 'Bob', age: 30, city: 'Shanghai' },
  { id: 3, name: 'Charlie', age: 28, city: 'Beijing' },
  { id: 4, name: 'David', age: 35, city: 'Guangzhou' }
];

const query = new QueryBuilder(users);

console.log('链式查询示例:\n');

const result = query
  .where(user => user.age > 26)
  .where(user => user.city === 'Beijing')
  .select('name', 'age')
  .orderBy('age', 'desc')
  .get();

console.log('结果:', result);
