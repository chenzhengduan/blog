<template>
  <div class="appoint-course">
    <h1>首页</h1>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

interface Course {
  id: string;
  title: string;
  teacher: string;
  date: string;
  timeSlot: string;
  type: string;
}

// 生成200条约课计划数据
const generateCourses = (): Course[] => {
  const courses: Course[] = [];
  const courseTypes = ['语文', '数学', '英语', '物理', '化学', '生物', '历史', '地理', '政治'];
  const teachers = ['张老师', '李老师', '王老师', '赵老师', '刘老师', '陈老师', '杨老师', '黄老师', '周老师'];
  const timeSlots = ['08:00-09:00', '09:10-10:10', '10:20-11:20', '13:30-14:30', '14:40-15:40', '15:50-16:50'];
  
  // 生成2025年5月19-24日之间的日期
  const generateRandomDate = () => {
    const day = Math.floor(Math.random() * 6) + 19;
    return `2025-05-${day}`;
  };
  
  for (let i = 0; i < 200; i++) {
    courses.push({
      id: `course-${i + 1}`,
      title: `${courseTypes[Math.floor(Math.random() * courseTypes.length)]}课程`,
      teacher: teachers[Math.floor(Math.random() * teachers.length)],
      date: generateRandomDate(),
      timeSlot: timeSlots[Math.floor(Math.random() * timeSlots.length)],
      type: courseTypes[Math.floor(Math.random() * courseTypes.length)]
    });
  }
  
  return courses;
};

const courses = ref<Course[]>(generateCourses());
</script>

<style scoped>
.appoint-course {
  padding: 20px;
}

h1 {
  color: #2878E8;
  margin-bottom: 20px;
}

.course-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.course-item {
  border: 1px solid #E4E7ED;
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.3s;
}

.course-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.course-header {
  background-color: #F0F2F5;
  padding: 10px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-title {
  font-weight: bold;
  color: #303133;
}

.course-date {
  color: #606266;
  font-size: 0.9em;
}

.course-body {
  padding: 15px;
}

.course-body p {
  margin: 5px 0;
  color: #606266;
}
</style>
