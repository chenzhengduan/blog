import os

# 文件列表
files = [
    ("09-赤眼鳟/德清瓜山水产赤眼鳟喂食.md", "赤眼鳟"),
    ("08-草鱼/德清瓜山水产草鱼喂食.md", "草鱼"),
    ("07-翘嘴红鲌/德清瓜山水产翘嘴红鲌喂食.md", "翘嘴红鲌"),
    ("06-白鲢/德清瓜山水产白鲢喂食.md", "白鲢"),
    ("01-青鱼/德清瓜山水产青鱼喂食.md", "青鱼"),
]

print("=" * 60)
print("开始更新MD文档")
print("=" * 60)

for file_path, fish_name in files:
    try:
        print(f"\n处理 {fish_name}: {file_path}")
        
        if not os.path.exists(file_path):
            print(f"❌ 文件不存在")
            continue
        
        # 读取文件
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # 1. 更新算法规则说明
        old_text = "整理后获得原始数据字段：养殖池编号、鱼类、记录时间、季节、活跃度"
        new_text = "整理后获得原始数据字段：养殖池编号、鱼类、记录时间、周数、季节、活跃度"
        
        if old_text in content:
            content = content.replace(old_text, new_text)
            print(f"  ✅ 算法规则说明已更新")
        else:
            print(f"  ⚠️  算法规则说明已包含周数或未找到匹配文本")
        
        # 2. 更新数据结构表格
        lines = content.split('\n')
        updated = False
        
        for i, line in enumerate(lines):
            if '| 3 | 记录时间 |' in line:
                # 检查下一行是否已经有周数
                if i + 1 < len(lines) and '周数' in lines[i + 1]:
                    print(f"  ⚠️  数据结构表格已包含周数字段")
                    break
                
                # 插入周数行
                lines.insert(i + 1, '| 4 | 周数 | 第39周 |')
                
                # 更新后续序号
                for j in range(i + 2, len(lines)):
                    if lines[j].startswith('|') and len(lines[j].split('|')) > 2:
                        parts = lines[j].split('|')
                        try:
                            num = int(parts[1].strip())
                            if num >= 4:
                                parts[1] = f' {num + 1} '
                                lines[j] = '|'.join(parts)
                        except:
                            break
                
                updated = True
                print(f"  ✅ 数据结构表格已更新")
                break
        
        if updated:
            content = '\n'.join(lines)
        
        # 保存文件
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        
        print(f"✅ {fish_name} 文档更新完成")
    
    except Exception as e:
        print(f"❌ 失败: {e}")

print("\n" + "=" * 60)
print("全部处理完成")
print("=" * 60)
