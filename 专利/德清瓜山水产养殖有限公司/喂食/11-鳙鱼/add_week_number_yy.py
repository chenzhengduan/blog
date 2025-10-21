import json
from datetime import datetime

# 读取JSON文件
with open('yy_feeding_data.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

# 处理每条记录
for record in data:
    # 获取记录时间
    record_time = record.get("记录时间", "")
    
    if record_time:
        # 解析日期时间
        dt = datetime.strptime(record_time, "%Y-%m-%d %H:%M")
        
        # 计算是一年中的第几周（ISO周数）
        week_number = dt.isocalendar()[1]
        
        # 创建新的有序字典，在"记录时间"后插入"周数"
        new_record = {}
        for key, value in record.items():
            new_record[key] = value
            # 在"记录时间"后面插入"周数"
            if key == "记录时间":
                new_record["周数"] = f"第{week_number}周"
        
        # 更新原记录
        record.clear()
        record.update(new_record)

# 保存更新后的JSON文件
with open('yy_feeding_data.json', 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print(f"处理完成！共处理 {len(data)} 条记录")
print(f"已在每条记录的'记录时间'后添加'周数'字段")
