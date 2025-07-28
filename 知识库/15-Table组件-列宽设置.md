# Table 组件-列宽设置

## 概述

1、当列宽不设置时，单元格宽度按照内容自动缩放
2、当列宽设置百分比，单元格宽度按照百分比缩放
3、当列宽设置像素值（px），单元格宽度按照像素比缩放
4、表格的固定宽度，需要设置外层容器宽度

## 列宽不设置

| Name | Date | Hobby | Address |
|------|------|-------|---------|
| John | 1900-05-20 | coding and coding repeat | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

当列宽不设置时，单元格宽度按照内容自动缩放

```vue
<template>
    <fan-table
        :columns="columns"
        :table-data="tableData"
        :border-around="true"
        :border-x="true"
        :border-y="true"
    />
</template>

<script>
export default {
  data() {
    return {
      columns: [
        { field: 'name', key: 'a', title: 'Name' },
        { field: 'date', key: 'b', title: 'Date' },
        { field: 'hobby', key: 'c', title: 'Hobby' },
        { field: 'address', key: 'd', title: 'Address' },
      ],
      tableData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shanghai',
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Beijing',
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
        },
        {
          name: 'Jami',
          date: '2020-09-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shenzhen',
        },
      ],
    }
  },
}
</script>
```

## 列宽百分比

| Name 40% | Tel 20% | Hobby 20% | Address 20% |
|----------|---------|-----------|-------------|
| John | 1900-05-20 | coding and coding repeat | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

当列宽设置百分比，单元格宽度按照百分比缩放

```vue
<template>
    <fan-table
        :columns="columns"
        :table-data="tableData"
        :border-around="true"
        :border-x="true"
        :border-y="true"
    />
</template>

<script>
export default {
  data() {
    return {
      columns: [
        {
          field: 'name',
          key: 'a',
          title: 'Name 40%',
          width: '40%',
        },
        { field: 'date', key: 'b', title: 'Tel 20%', width: '20%' },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby 20%',
          width: '20%',
        },
        {
          field: 'address',
          key: 'd',
          title: 'Address 20%',
          width: '20%',
        },
      ],
      tableData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shanghai',
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Beijing',
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
        },
        {
          name: 'Jami',
          date: '2020-09-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shenzhen',
        },
      ],
    }
  },
}
</script>
```

## 列宽像素值

| Name 400px | Tel 200px | Hobby 200px | Address 200px |
|------------|-----------|-------------|---------------|
| John | 1900-05-20 | coding and coding repeat | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

1、当列宽设置像素值（px），单元格宽度按照像素比缩放。如果不希望缩放，需要设置外层容器宽度
2、设置像素值，记得不要加单位

```vue
<template>
    <fan-table
        :columns="columns"
        :table-data="tableData"
        :border-around="true"
        :border-x="true"
        :border-y="true"
    />
</template>

<script>
export default {
  data() {
    return {
      columns: [
        {
          field: 'name',
          key: 'a',
          title: 'Name 400px',
          width: 400,
        },
        { field: 'date', key: 'b', title: 'Tel 200px', width: 200 },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby 200px',
          width: 200,
        },
        {
          field: 'address',
          key: 'd',
          title: 'Address 200px',
          width: 200,
        },
      ],
      tableData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shanghai',
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Beijing',
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
        },
        {
          name: 'Jami',
          date: '2020-09-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shenzhen',
        },
      ],
    }
  },
}
</script>
```

## 长文本破坏布局

word-break：

| Name 40% | Tel 20% | Hobby 20% | Address 20% |
|----------|---------|-----------|-------------|
| John | 1900-05-20 | Honorificabilitudinitatibus califragilisticexpialidocious Taumatawhakatangihangakoauauotamateaturipukakapikimaungahoronukupokaiwhenuakitanatahu 大江东去浪淘尽千古风流人物故垒西边人道是三国周郎赤壁乱石穿空惊涛拍岸卷起千堆雪江山如画一时多少豪杰 | No.1 Century Avenue, Xiamen |
| Dickerson | 1910-06-20 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

1、当单元格文本内容过多时会破坏布局，此时可以通过样式 `word-break` 控制
2、你也可以结合单元格省略功能一起使用

```vue
<template>
    <div>
        word-break：
        <el-radio-group v-model="wordBreak" size="small">
            <el-radio-button label="normal">normal</el-radio-button>
            <el-radio-button label="keep-all">keep-all</el-radio-button>
            <el-radio-button label="break-all">break-all</el-radio-button>
            <el-radio-button label="break-word">break-word</el-radio-button>
        </el-radio-group>
        <br />
        <br />
        <fan-table
            :style="{'word-break':wordBreak}"
            :columns="columns"
            :table-data="tableData"
            :border-around="true"
            :border-x="true"
            :border-y="true"
        />
    </div>
</template>

<script>
export default {
  data() {
    return {
      wordBreak: 'normal',
      columns: [
        {
          field: 'name',
          key: 'a',
          title: 'Name 40%',
          width: '40%',
        },
        { field: 'date', key: 'b', title: 'Tel 20%', width: '20%' },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby 20%',
          width: '20%',
          /*   ellipsis: {
              showTitle: true,
              lineClamp: 5,
            }, */
        },
        {
          field: 'address',
          key: 'd',
          title: 'Address 20%',
          width: '20%',
        },
      ],
      tableData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'Honorificabilitudinitatibus califragilisticexpialidocious Taumatawhakatangihangakoauauotamateaturipukakapikimaungahoronukupokaiwhenuakitanatahu 大江东去浪淘尽千古风流人物故垒西边人道是三国周郎赤壁乱石穿空惊涛拍岸卷起千堆雪江山如画一时多少豪杰',
          address: 'No.1 Century Avenue, Xiamen',
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Beijing',
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
        },
        {
          name: 'Jami',
          date: '2020-09-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shenzhen',
        },
      ],
    }
  },
}
</script>
```

## API

### width 列宽配置

| 参数值 | 说明 | 示例 |
|--------|------|------|
| 不设置 | 单元格宽度按照内容自动缩放 | `{ field: 'name', title: 'Name' }` |
| 百分比 | 单元格宽度按照百分比缩放 | `{ field: 'name', title: 'Name', width: '40%' }` |
| 像素值 | 单元格宽度按照像素值缩放 | `{ field: 'name', title: 'Name', width: 400 }` |

### 使用说明

1. **自动缩放**：不设置 `width` 时，列宽会根据内容自动调整
2. **百分比缩放**：设置百分比时，列宽会按照容器宽度的比例分配
3. **像素缩放**：设置像素值时，列宽会按照指定的像素值显示
4. **容器宽度**：表格的固定宽度需要设置外层容器的宽度
5. **文本换行**：使用 `word-break` 样式控制长文本的换行方式

### 适用场景

- **响应式布局**：使用百分比实现响应式的列宽分配
- **固定布局**：使用像素值实现固定宽度的列布局
- **内容自适应**：不设置宽度让内容自动调整列宽
- **长文本处理**：结合 `word-break` 和省略功能处理长文本 