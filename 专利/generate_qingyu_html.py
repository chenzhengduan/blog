import pandas as pd
import numpy as np
import random
from datetime import datetime, timedelta

def generate_qingyu_survival_data(num_records=500):
    """
    生成青鱼养殖存活率综合环境分析数据
    
    Args:
        num_records: 生成记录数量，默认500条
    
    Returns:
        DataFrame: 包含所有字段的数据表
    """
    
    # 设置随机种子确保可重复性
    np.random.seed(42)
    random.seed(42)
    
    # 基础配置
    pool_prefixes = ['QY', 'CY', 'BY', 'DY', 'EY']
    death_reasons = ['pH值异常', '疾病', '操作损伤', '天敌', '水质异常', '缺氧', '温度异常']
    managers = ['张三', '李四', '王五', '赵六', '钱七', '孙八', '周九', '吴十']
    
    data = []
    
    for i in range(num_records):
        # 生成基础数据
        pool_id = f"{random.choice(pool_prefixes)}{str(random.randint(1, 99)).zfill(2)}"
        
        # 投放时间：2023年1月1日到2024年12月31日之间随机
        start_date = datetime(2023, 1, 1) + timedelta(days=random.randint(0, 730))
        release_date = start_date.strftime('%Y-%m-%d')
        
        # 投放数量：3000-8000尾
        release_count = random.randint(3000, 8000)
        
        # 养殖周期：60-180天
        culture_period = random.randint(60, 180)
        
        # 检测时间：投放后随机1-30天
        check_days = random.randint(1, 30)
        check_date = start_date + timedelta(days=check_days)
        check_date_str = check_date.strftime('%Y-%m-%d')
        
        # 环境参数（基于最优值生成合理范围）
        water_temp = round(random.uniform(20.0, 28.0), 1)  # 水温20-28℃
        dissolved_oxygen = round(random.uniform(5.0, 9.0), 1)  # 溶氧5-9mg/L
        ph_value = round(random.uniform(6.5, 8.0), 1)  # pH 6.5-8.0
        ammonia_nitrogen = round(random.uniform(0.05, 0.3), 2)  # 氨氮0.05-0.3mg/L
        
        # 根据环境参数计算存活率
        # 环境修正因子计算
        do_deviation = abs(dissolved_oxygen - 6.0) / 6.0
        temp_deviation = abs(water_temp - 24.0) / 24.0
        ph_deviation = abs(ph_value - 7.0) / 7.0
        nh3_deviation = abs(ammonia_nitrogen - 0.1) / 0.1
        
        env_correction_factor = 1 - (0.35 * do_deviation + 0.30 * temp_deviation + 
                                    0.20 * ph_deviation + 0.15 * nh3_deviation)
        env_correction_factor = max(0.5, min(1.0, env_correction_factor))  # 限制在0.5-1.0之间
        
        # 基础存活率：85%-98%
        base_survival_rate = random.uniform(0.85, 0.98)
        
        # 综合存活率
        comprehensive_survival_rate = base_survival_rate * env_correction_factor
        
        # 存活数量
        survival_count = int(release_count * comprehensive_survival_rate)
        
        # 死亡原因（根据环境参数判断）
        if ph_value < 6.8 or ph_value > 7.5:
            death_reason = 'pH值异常'
        elif dissolved_oxygen < 5.5:
            death_reason = '缺氧'
        elif water_temp < 22 or water_temp > 26:
            death_reason = '温度异常'
        elif ammonia_nitrogen > 0.2:
            death_reason = '水质异常'
        else:
            death_reason = random.choice(death_reasons)
        
        # 构建记录
        record = {
            '养殖池编号': pool_id,
            '投放时间': release_date,
            '投放数量（尾）': release_count,
            '存活数量（尾）': survival_count,
            '死亡原因': death_reason,
            '养殖周期（天）': culture_period,
            '检测时间': check_date_str,
            '负责人': random.choice(managers),
            '水温（℃）': water_temp,
            '溶氧（mg/L）': dissolved_oxygen,
            'pH': ph_value,
            '氨氮（mg/L）': ammonia_nitrogen,
            '基础存活率（%）': round(base_survival_rate * 100, 1),
            '综合存活率（%）': round(comprehensive_survival_rate * 100, 1)
        }
        
        data.append(record)
    
    return pd.DataFrame(data)

def create_html_table(df, filename='青鱼养殖存活率综合环境分析数据.html'):
    """
    创建美观的HTML表格
    
    Args:
        df: DataFrame数据
        filename: 输出文件名
    """
    
    # HTML模板
    html_template = """
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>青鱼养殖存活率综合环境分析数据</title>
        <style>
            body {
                font-family: 'Microsoft YaHei', Arial, sans-serif;
                margin: 20px;
                background-color: #f5f5f5;
            }
            .container {
                max-width: 1400px;
                margin: 0 auto;
                background-color: white;
                border-radius: 8px;
                box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                overflow: hidden;
            }
            .header {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                padding: 20px;
                text-align: center;
            }
            .header h1 {
                margin: 0;
                font-size: 28px;
                font-weight: 300;
            }
            .header p {
                margin: 10px 0 0 0;
                opacity: 0.9;
                font-size: 14px;
            }
            .stats {
                background-color: #f8f9fa;
                padding: 15px 20px;
                border-bottom: 1px solid #e9ecef;
                display: flex;
                justify-content: space-around;
                flex-wrap: wrap;
            }
            .stat-item {
                text-align: center;
                margin: 5px 15px;
            }
            .stat-value {
                font-size: 24px;
                font-weight: bold;
                color: #495057;
            }
            .stat-label {
                font-size: 12px;
                color: #6c757d;
                margin-top: 5px;
            }
            .table-container {
                overflow-x: auto;
                padding: 20px;
            }
            table {
                width: 100%;
                border-collapse: collapse;
                font-size: 12px;
                background-color: white;
            }
            th {
                background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
                color: white;
                padding: 12px 8px;
                text-align: center;
                font-weight: 500;
                position: sticky;
                top: 0;
                z-index: 10;
            }
            td {
                padding: 8px;
                text-align: center;
                border-bottom: 1px solid #e9ecef;
            }
            tr:nth-child(even) {
                background-color: #f8f9fa;
            }
            tr:hover {
                background-color: #e3f2fd;
                transition: background-color 0.3s;
            }
            .survival-rate {
                font-weight: bold;
            }
            .survival-high {
                color: #28a745;
            }
            .survival-medium {
                color: #ffc107;
            }
            .survival-low {
                color: #dc3545;
            }
            .footer {
                background-color: #f8f9fa;
                padding: 15px 20px;
                text-align: center;
                color: #6c757d;
                font-size: 12px;
                border-top: 1px solid #e9ecef;
            }
            @media (max-width: 768px) {
                .stats {
                    flex-direction: column;
                }
                .stat-item {
                    margin: 10px 0;
                }
                table {
                    font-size: 10px;
                }
                th, td {
                    padding: 6px 4px;
                }
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <h1>青鱼养殖存活率综合环境分析数据</h1>
                <p>德清瓜山水产养殖有限公司 | 数据生成时间：{generation_time}</p>
            </div>
            
            <div class="stats">
                <div class="stat-item">
                    <div class="stat-value">{total_records}</div>
                    <div class="stat-label">总记录数</div>
                </div>
                <div class="stat-item">
                    <div class="stat-value">{avg_base_survival:.1f}%</div>
                    <div class="stat-label">平均基础存活率</div>
                </div>
                <div class="stat-item">
                    <div class="stat-value">{avg_comprehensive_survival:.1f}%</div>
                    <div class="stat-label">平均综合存活率</div>
                </div>
                <div class="stat-item">
                    <div class="stat-value">{avg_temp:.1f}℃</div>
                    <div class="stat-label">平均水温</div>
                </div>
                <div class="stat-item">
                    <div class="stat-value">{avg_oxygen:.1f}mg/L</div>
                    <div class="stat-label">平均溶氧</div>
                </div>
                <div class="stat-item">
                    <div class="stat-value">{avg_ph:.1f}</div>
                    <div class="stat-label">平均pH值</div>
                </div>
            </div>
            
            <div class="table-container">
                {table_html}
            </div>
            
            <div class="footer">
                <p>© 2024 德清瓜山水产养殖有限公司 | 数据仅供参考，请以实际情况为准</p>
            </div>
        </div>
    </body>
    </html>
    """
    
    # 计算统计数据
    total_records = len(df)
    avg_base_survival = df['基础存活率（%）'].mean()
    avg_comprehensive_survival = df['综合存活率（%）'].mean()
    avg_temp = df['水温（℃）'].mean()
    avg_oxygen = df['溶氧（mg/L）'].mean()
    avg_ph = df['pH'].mean()
    
    # 为存活率添加颜色样式
    def style_survival_rate(val):
        if val >= 90:
            return f'<span class="survival-rate survival-high">{val}%</span>'
        elif val >= 80:
            return f'<span class="survival-rate survival-medium">{val}%</span>'
        else:
            return f'<span class="survival-rate survival-low">{val}%</span>'
    
    # 格式化DataFrame
    df_formatted = df.copy()
    df_formatted['基础存活率（%）'] = df_formatted['基础存活率（%）'].apply(style_survival_rate)
    df_formatted['综合存活率（%）'] = df_formatted['综合存活率（%）'].apply(style_survival_rate)
    
    # 生成表格HTML
    table_html = df_formatted.to_html(
        index=False,
        escape=False,
        classes='data-table',
        table_id='qingyu-table'
    )
    
    # 填充模板
    html_content = html_template.format(
        generation_time=datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
        total_records=total_records,
        avg_base_survival=avg_base_survival,
        avg_comprehensive_survival=avg_comprehensive_survival,
        avg_temp=avg_temp,
        avg_oxygen=avg_oxygen,
        avg_ph=avg_ph,
        table_html=table_html
    )
    
    # 保存HTML文件
    with open(filename, 'w', encoding='utf-8') as f:
        f.write(html_content)
    
    return filename

def main():
    """主函数"""
    print("开始生成青鱼养殖存活率HTML数据...")
    
    # 生成数据
    df = generate_qingyu_survival_data(500)
    
    # 创建HTML文件
    filename = create_html_table(df)
    
    print(f"HTML数据生成完成！共生成 {len(df)} 条记录")
    print(f"文件保存为: {filename}")
    
    # 显示数据统计信息
    print("\n数据统计信息:")
    print(f"平均基础存活率: {df['基础存活率（%）'].mean():.1f}%")
    print(f"平均综合存活率: {df['综合存活率（%）'].mean():.1f}%")
    print(f"平均水温: {df['水温（℃）'].mean():.1f}℃")
    print(f"平均溶氧: {df['溶氧（mg/L）'].mean():.1f}mg/L")
    print(f"平均pH值: {df['pH'].mean():.1f}")
    print(f"平均氨氮: {df['氨氮（mg/L）'].mean():.2f}mg/L")
    
    print(f"\n请在浏览器中打开 {filename} 查看完整的HTML表格")

if __name__ == "__main__":
    main() 