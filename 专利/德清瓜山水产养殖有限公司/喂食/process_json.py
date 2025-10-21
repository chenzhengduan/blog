import json
from datetime import datetime
import sys

# 文件列表
files = [
    ("09-赤眼鳟/CYZ_feeding_data.json", "赤眼鳟"),
    ("08-草鱼/cy_feeding_data.json", "草鱼"),
    ("07-翘嘴红鲌/QZHB_feeding_data.json", "翘嘴红鲌"),
    ("06-白鲢/bl_feeding_data.json", "白鲢"),
    ("01-青鱼/qy_feeding_data.json", "青鱼"),
]

print("=" * 60)
print("开始处理JSON文件")
print("=" * 60)

for file_path, fish_name in files:
    try:
        print(f"\n处理 {fish_name}: {file_path}")
        
        # 读取
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        # 处理
        new_data = []
        for record in data:
            new_record = {}
            for key, value in record.items():
                new_record[key] = value
                if key == "记录时间":
                    dt = datetime.strptime(value, "%Y-%m-%d %H:%M")
                    week_num = dt.isocalendar()[1]
                    new_record["周数"] = f"第{week_num}周"
            new_data.append(new_record)
        
        # 保存
        with open(file_path, 'w', encoding='utf-8') as f:
            json.dump(new_data, f, ensure_ascii=False, indent=2)
        
        print(f"✅ 完成！共处理 {len(new_data)} 条记录")
    
    except Exception as e:
        print(f"❌ 失败: {e}")

print("\n" + "=" * 60)
print("全部处理完成")
print("=" * 60)
